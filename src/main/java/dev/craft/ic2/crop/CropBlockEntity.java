package dev.craft.ic2.crop;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.reactor.GradualItem;
import dev.craft.ic2.machine.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crop tile (IC2 {@code TileEntityCrop}). Every 256 ticks the crop updates its environment (humidity, nutrients and
 * air quality each every 1024 ticks, staggered), tries crossing or weed on an empty stick, grows and uses up its water
 * and nutrient storage. Stats are bytes 0–31 like in IC2; the Weed-EX storage is not saved (IC2 does not save it).
 */
public class CropBlockEntity extends BlockEntity {
    public static final int TICK_RATE = 256;

    private int id = -1;
    private int size;
    private int statGrowth;
    private int statGain;
    private int statResistance;
    private int scanLevel;
    private short[] customData = new short[16];
    private int nutrientStorage;
    private int waterStorage;
    private int exStorage;
    private int growthPoints;
    private boolean upgraded;
    /** 16 bit like IC2's {@code char} ticker; starts at a random phase and is not saved. */
    private int ticker = ThreadLocalRandom.current().nextInt(TICK_RATE);
    private boolean dirty = true;
    private int humidity = -1;
    private int nutrients = -1;
    private int airQuality = -1;

    public CropBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.CROP.get(), pos, state);
    }

    // --- accessors (IC2 TECrop fields) ---

    public int getCropId() {
        return id;
    }

    /** The planted species, or the weed for an empty stick (IC2 {@code crop()}). */
    public CropCard crop() {
        return Crops.get(id);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = (byte) size;
        dirty = true;
    }

    public int getGrowth() {
        return statGrowth;
    }

    public int getGain() {
        return statGain;
    }

    public int getResistance() {
        return statResistance;
    }

    public int getScanLevel() {
        return scanLevel;
    }

    public short[] getCustomData() {
        return customData;
    }

    public int getNutrientStorage() {
        return nutrientStorage;
    }

    public int getWaterStorage() {
        return waterStorage;
    }

    public int getExStorage() {
        return exStorage;
    }

    public int getGrowthPoints() {
        return growthPoints;
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    public int getLightLevel() {
        return level.getMaxLocalRawBrightness(worldPosition);
    }

    /** IC2 {@code updateState}: re-render and re-sync. */
    public void updateState() {
        dirty = true;
    }

    // --- ticking ---

    public static void serverTick(Level level, BlockPos pos, BlockState state, CropBlockEntity crop) {
        crop.ticker = (crop.ticker + 1) & 0xFFFF;
        if (crop.ticker % TICK_RATE == 0) {
            crop.tick();
        }
        if (crop.dirty) {
            crop.dirty = false;
            crop.setChanged();
            int sprite = Math.clamp(crop.getSprite(), 0, CropBlock.MAX_SPRITE);
            BlockState updated = state.setValue(CropBlock.SPRITE, sprite);
            if (updated != state) {
                level.setBlock(pos, updated, Block.UPDATE_ALL);
            } else {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            }
            level.updateNeighborsAt(pos, state.getBlock());
            level.getLightEngine().checkBlock(pos);
        }
    }

    public void tick() {
        RandomSource random = level.random;
        if (ticker % (TICK_RATE << 2) == 0) {
            humidity = updateHumidity();
        }
        if ((ticker + TICK_RATE) % (TICK_RATE << 2) == 0) {
            nutrients = updateNutrients();
        }
        if ((ticker + TICK_RATE * 2) % (TICK_RATE << 2) == 0) {
            airQuality = updateAirQuality();
        }
        if (id < 0 && (!upgraded || !attemptCrossing())) {
            if (random.nextInt(100) != 0 || hasEx()) {
                if (exStorage > 0 && random.nextInt(10) == 0) {
                    exStorage--;
                }
                return;
            }
            reset();
            id = Crops.WEED.getId();
            size = 1;
        }
        crop().tick(this);
        if (crop().canGrow(this)) {
            growthPoints += calcGrowthRate();
            if (id > -1 && growthPoints >= crop().growthDuration(this)) {
                growthPoints = 0;
                size = (byte) (size + 1);
                dirty = true;
            }
        }
        if (nutrientStorage > 0) {
            nutrientStorage--;
        }
        if (waterStorage > 0) {
            waterStorage--;
        }
        if (crop().isWeed(this) && random.nextInt(50) - statGrowth <= 2) {
            generateWeed();
        }
        setChanged();
    }

    /**
     * IC2 {@code generateWeed}. The direction switch falls through in the original, so only x − 1, z − 1 or the crop
     * itself (cases 0 and 2) are ever chosen.
     */
    public void generateWeed() {
        int dx = 0;
        int dz = 0;
        switch (level.random.nextInt(4)) {
            case 1 -> dx = -1;
            case 3 -> dz = -1;
            default -> {
            }
        }
        BlockPos target = worldPosition.offset(dx, 0, dz);
        if (level.getBlockEntity(target) instanceof CropBlockEntity crop) {
            if (crop.id == -1 || !crop.crop().isWeed(crop) && level.random.nextInt(32) >= crop.statResistance
                    && !crop.hasEx()) {
                int newGrowth = Math.max(statGrowth, crop.statGrowth);
                if (newGrowth < 31 && level.random.nextBoolean()) {
                    newGrowth++;
                }
                crop.reset();
                crop.id = Crops.WEED.getId();
                crop.size = 1;
                crop.statGrowth = newGrowth;
                crop.setChanged();
            }
        } else if (level.isEmptyBlock(target)) {
            BlockState below = level.getBlockState(target.below());
            if (below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.FARMLAND)) {
                level.setBlock(target.below(), Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(target, Blocks.SHORT_GRASS.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    /** IC2 {@code hasEx}: an active Weed-EX storage blocks weed and loses 5 per check. */
    public boolean hasEx() {
        if (exStorage > 0) {
            exStorage -= 5;
            return true;
        }
        return false;
    }

    public boolean attemptCrossing() {
        RandomSource random = level.random;
        if (random.nextInt(3) != 0) {
            return false;
        }
        List<CropBlockEntity> crops = new ArrayList<>();
        askCropJoinCross(worldPosition.west(), crops);
        askCropJoinCross(worldPosition.east(), crops);
        askCropJoinCross(worldPosition.north(), crops);
        askCropJoinCross(worldPosition.south(), crops);
        if (crops.size() < 2) {
            return false;
        }
        int[] ratios = new int[Crops.MAX_CROPS];
        for (int i = 1; i < ratios.length; i++) {
            if (Crops.exists(i) && Crops.get(i).canGrow(this)) {
                for (CropBlockEntity crop : crops) {
                    ratios[i] += calculateRatioFor(Crops.get(i), crop.crop());
                }
            }
        }
        int total = 0;
        for (int ratio : ratios) {
            total += ratio;
        }
        total = random.nextInt(total);
        for (int i = 0; i < ratios.length; i++) {
            if (ratios[i] > 0 && ratios[i] > total) {
                total = i;
                break;
            }
            total -= ratios[i];
        }
        upgraded = false;
        id = total;
        dirty = true;
        size = 1;
        int growth = 0;
        int gain = 0;
        int resistance = 0;
        for (CropBlockEntity crop : crops) {
            growth += crop.statGrowth;
            resistance += crop.statResistance;
            gain += crop.statGain;
        }
        int count = crops.size();
        statGrowth = mutate(growth / count, count, random);
        statGain = mutate(gain / count, count, random);
        statResistance = mutate(resistance / count, count, random);
        return true;
    }

    private static int mutate(int average, int count, RandomSource random) {
        return Math.clamp(average + random.nextInt(1 + 2 * count) - count, 0, 31);
    }

    /**
     * Crossing weight of species {@code a} for a parent {@code b}. The stat switch falls through in IC2: a difference
     * of 0 gives +3, 1 gives +1, 2 gives 0 and anything larger +2.
     */
    public static int calculateRatioFor(CropCard a, CropCard b) {
        if (a == b) {
            return 500;
        }
        int value = 0;
        for (int i = 0; i < 5; i++) {
            int c = Math.abs(a.stat(i) - b.stat(i));
            value += switch (c) {
                case 0 -> 3;
                case 1 -> 1;
                case 2 -> 0;
                default -> 2;
            };
        }
        for (String attributeA : a.attributes()) {
            for (String attributeB : b.attributes()) {
                if (attributeA.equalsIgnoreCase(attributeB)) {
                    value += 5;
                }
            }
        }
        if (b.tier() < a.tier() - 1) {
            value -= 2 * (a.tier() - b.tier());
        }
        if (b.tier() - 3 > a.tier()) {
            value -= b.tier() - a.tier();
        }
        return Math.max(value, 0);
    }

    /** IC2 asks {@code canGrow} of the neighbour's species with the empty crossing stick, not the neighbour. */
    private void askCropJoinCross(BlockPos pos, List<CropBlockEntity> crops) {
        if (level.getBlockEntity(pos) instanceof CropBlockEntity side && side.id > 0
                && side.crop().canGrow(this) && side.crop().canCross(side)) {
            int base = 4;
            if (side.statGrowth >= 16) {
                base++;
            }
            if (side.statGrowth >= 30) {
                base++;
            }
            if (side.statResistance >= 28) {
                base += 27 - side.statResistance;
            }
            if (base >= level.random.nextInt(20)) {
                crops.add(side);
            }
        }
    }

    // --- player interaction ---

    /** Left click (IC2 {@code leftclick}): removes the crossing stick or lets the species react (pick by default). */
    public boolean leftclick(Player player) {
        if (id < 0) {
            if (upgraded) {
                upgraded = false;
                dirty = true;
                StackUtil.dropAsEntity(level, worldPosition, new ItemStack(Ic2Items.CROP.get()));
                return true;
            }
            return false;
        }
        return crop().leftclick(this, player);
    }

    /** IC2 {@code pick}: harvests if possible, drops seed bags by chance and empties the stick. */
    public boolean pick(boolean manual) {
        if (id < 0) {
            return false;
        }
        RandomSource random = level.random;
        boolean bonus = harvest(false);
        float firstChance = crop().dropSeedChance(this);
        for (int i = 0; i < statResistance; i++) {
            firstChance *= 1.1F;
        }
        int drop = 0;
        if (bonus) {
            if (random.nextFloat() <= (firstChance + 1.0F) * 0.8F) {
                drop++;
            }
            float chance = crop().dropSeedChance(this) + statGrowth / 100.0F;
            if (!manual) {
                chance *= 0.8F;
            }
            for (int i = 23; i < statGain; i++) {
                chance *= 0.95F;
            }
            if (random.nextFloat() <= chance) {
                drop++;
            }
        } else if (random.nextFloat() <= firstChance * 1.5F) {
            drop++;
        }
        List<ItemStack> seeds = new ArrayList<>();
        for (int i = 0; i < drop; i++) {
            seeds.add(crop().getSeeds(this));
        }
        reset();
        for (ItemStack seed : seeds) {
            StackUtil.dropAsEntity(level, worldPosition, seed);
        }
        return true;
    }

    /** Right click with the main hand item (IC2 {@code rightclick}); returns true if the click was used. */
    public boolean rightclick(Player player) {
        ItemStack current = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!current.isEmpty()) {
            if (id < 0) {
                if (current.is(Ic2Items.CROP.get()) && !upgraded) {
                    if (!player.getAbilities().instabuild) {
                        current.shrink(1);
                    }
                    upgraded = true;
                    dirty = true;
                    return true;
                }
                if (applyBaseSeed(current)) {
                    return true;
                }
            } else if (current.is(Ic2Items.CROPNALYZER.get())) {
                Component description = getScanned();
                player.displayClientMessage(description != null ? description
                        : Component.translatable("message.craft.crop.unknown"), false);
                return true;
            }
            if (current.is(Items.WATER_BUCKET) || current.is(Ic2Items.WATER_CELL.get())) {
                if (waterStorage < 10) {
                    waterStorage = 10;
                    setChanged();
                    return true;
                }
                return current.is(Items.WATER_BUCKET);
            }
            if (current.is(Items.WHEAT_SEEDS)) {
                if (nutrientStorage <= 50) {
                    nutrientStorage += 25;
                    current.shrink(1);
                    setChanged();
                    return true;
                }
                return false;
            }
            if (current.is(Items.BONE_MEAL) || current.is(Ic2Items.FERTILIZER.get())) {
                if (applyFertilizer(true)) {
                    current.shrink(1);
                    return true;
                }
                return false;
            }
            if (current.is(Ic2Items.HYDRATING_CELL.get())) {
                return applyHydration(true, current);
            }
            if (current.is(Ic2Items.WEED_EX.get()) && applyWeedEx(true)) {
                current.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                return true;
            }
        }
        return id >= 0 && crop().rightclick(this, player);
    }

    /** Plants a base seed (vanilla seeds, flowers …) on an empty stick; the stack must hold the required amount. */
    public boolean applyBaseSeed(ItemStack current) {
        Crops.BaseSeed seed = Crops.getBaseSeed(current);
        if (seed == null || current.getCount() < seed.stackSize()) {
            return false;
        }
        if (tryPlantIn(seed.crop().getId(), seed.size(), seed.growth(), seed.gain(), seed.resistance(), 1)) {
            current.shrink(seed.stackSize());
            return true;
        }
        return false;
    }

    public boolean tryPlantIn(int cropId, int plantSize, int growth, int gain, int resistance, int scan) {
        if (id <= -1 && cropId > 0 && !upgraded) {
            if (!Crops.get(cropId).canGrow(this)) {
                return false;
            }
            reset();
            id = (short) cropId;
            size = (byte) plantSize;
            statGrowth = (byte) growth;
            statGain = (byte) gain;
            statResistance = (byte) resistance;
            scanLevel = (byte) scan;
            return true;
        }
        return false;
    }

    /** Bone meal or fertilizer: +100 (Crop-Matron +90) while below 100. */
    public boolean applyFertilizer(boolean manual) {
        if (nutrientStorage >= 100) {
            return false;
        }
        nutrientStorage += manual ? 100 : 90;
        setChanged();
        return true;
    }

    /** Hydration cell: fills water up to 200 by hand (Crop-Matron 180), the cell wears by the amount used. */
    public boolean applyHydration(boolean manual, ItemStack cell) {
        if ((manual || waterStorage < 180) && waterStorage < 200) {
            int apply = manual ? 200 - waterStorage : 180 - waterStorage;
            int damage = cell.getDamageValue();
            int max = GradualItem.MAX_DAMAGE;
            if (apply + damage > max) {
                apply = max - damage;
            }
            cell.setDamageValue(damage + apply);
            if (cell.getDamageValue() >= max) {
                cell.shrink(1);
            }
            waterStorage += apply;
            setChanged();
            return true;
        }
        return false;
    }

    /**
     * Weed-EX: +50 storage (by hand only below 100, never at 150 or more). With 75 or more a planted crop may lose
     * stats; the IC2 switch falls through, so case 0 lowers all three stats and case 1 gain and resistance.
     */
    public boolean applyWeedEx(boolean manual) {
        if ((exStorage < 100 || !manual) && exStorage < 150) {
            exStorage += 50;
            RandomSource random = level.random;
            boolean trigger = random.nextInt(3) == 0;
            if (manual) {
                trigger = random.nextInt(5) == 0;
            }
            if (id > 0 && exStorage >= 75 && trigger) {
                int which = random.nextInt(5);
                if (which == 0 && statGrowth > 0) {
                    statGrowth--;
                }
                if (which <= 1 && statGain > 0) {
                    statGain--;
                }
                if (statResistance > 0) {
                    statResistance--;
                }
            }
            setChanged();
            return true;
        }
        return false;
    }

    /** IC2 {@code harvest}: drop count from {@code dropGainChance · 1.03^gain}, each drop +1 with gain %. */
    public boolean harvest(boolean manual) {
        if (id >= 0 && crop().canBeHarvested(this)) {
            RandomSource random = level.random;
            float chance = crop().dropGainChance();
            for (int i = 0; i < statGain; i++) {
                chance *= 1.03F;
            }
            chance -= random.nextFloat();
            int drop = 0;
            while (chance > 0.0F) {
                drop++;
                chance -= random.nextFloat();
            }
            List<ItemStack> drops = new ArrayList<>();
            for (int i = 0; i < drop; i++) {
                ItemStack gain = crop().getGain(this);
                if (gain != null && random.nextInt(100) <= statGain) {
                    gain.grow(1);
                }
                if (gain != null) {
                    drops.add(gain);
                }
            }
            size = (byte) crop().getSizeAfterHarvest(this);
            dirty = true;
            for (ItemStack stack : drops) {
                StackUtil.dropAsEntity(level, worldPosition, stack);
            }
            return true;
        }
        return false;
    }

    // --- block hooks ---

    public void onNeighbourChange() {
        if (id >= 0) {
            crop().onNeighbourChange(this);
        }
    }

    public boolean emitRedstone() {
        return id >= 0 && crop().emitRedstone(this);
    }

    public void onBlockDestroyed() {
        if (id >= 0) {
            crop().onBlockDestroyed(this);
        }
    }

    public int getEmittedLight() {
        return id < 0 ? 0 : crop().getEmittedLight(this);
    }

    public void onEntityCollision(Entity entity) {
        if (id >= 0 && crop().onEntityCollision(this, entity)) {
            calcTrampling();
        }
    }

    /** 1 % per collision, then only if {@code rand(40) > resistance}: the crop dies and the farmland turns to dirt. */
    public void calcTrampling() {
        RandomSource random = level.random;
        if (random.nextInt(100) == 0 && random.nextInt(40) > statResistance) {
            reset();
            level.setBlock(worldPosition.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    // --- environment ---

    public int getHumidity() {
        if (humidity == -1) {
            humidity = updateHumidity();
        }
        return humidity;
    }

    public int getNutrients() {
        if (nutrients == -1) {
            nutrients = updateNutrients();
        }
        return nutrients;
    }

    public int getAirQuality() {
        if (airQuality == -1) {
            airQuality = updateAirQuality();
        }
        return airQuality;
    }

    public int updateHumidity() {
        int value = Crops.getHumidityBiomeBonus(level.getBiome(worldPosition));
        BlockState below = level.getBlockState(worldPosition.below());
        if (below.hasProperty(FarmBlock.MOISTURE) && below.getValue(FarmBlock.MOISTURE) >= 7) {
            value += 2;
        }
        if (waterStorage >= 5) {
            value += 2;
        }
        value += (waterStorage + 24) / 25;
        return (byte) value;
    }

    public int updateNutrients() {
        int value = Crops.getNutrientBiomeBonus(level.getBiome(worldPosition));
        for (int i = 2; i < 5 && level.getBlockState(worldPosition.below(i)).is(Blocks.DIRT); i++) {
            value++;
        }
        value += (nutrientStorage + 19) / 20;
        return (byte) value;
    }

    /** Height bonus, free space in the 2×2 area x − 1..x, z − 1..z (the crop itself counts) and sky access. */
    public int updateAirQuality() {
        int value = Math.clamp((worldPosition.getY() - 64) / 15, 0, 4);
        int fresh = 9;
        for (int x = worldPosition.getX() - 1; x < worldPosition.getX() + 1 && fresh > 0; x++) {
            for (int z = worldPosition.getZ() - 1; z < worldPosition.getZ() + 1 && fresh > 0; z++) {
                BlockPos pos = new BlockPos(x, worldPosition.getY(), z);
                if (level.getBlockState(pos).isSolidRender(level, pos)
                        || level.getBlockEntity(pos) instanceof CropBlockEntity) {
                    fresh--;
                }
            }
        }
        value += fresh / 2;
        if (level.canSeeSky(worldPosition.above())) {
            value += 2;
        }
        return (byte) value;
    }

    public int calcGrowthRate() {
        RandomSource random = level.random;
        int base = 3 + random.nextInt(7) + statGrowth;
        int need = Math.max((crop().tier() - 1) * 4 + statGrowth + statGain + statResistance, 0);
        int have = crop().weightInfluences(this, getHumidity(), getNutrients(), getAirQuality()) * 5;
        if (have >= need) {
            base = base * (100 + (have - need)) / 100;
        } else {
            int neg = (need - have) * 4;
            if (neg > 100 && random.nextInt(32) > statResistance) {
                reset();
                base = 0;
            } else {
                base = Math.max(base * (100 - neg) / 100, 0);
            }
        }
        return base;
    }

    /** True if {@code block} lies 1–3 blocks below, stopping at the first air block. */
    public boolean isBlockBelow(Block block) {
        for (int i = 1; i < 4; i++) {
            BlockState state = level.getBlockState(worldPosition.below(i));
            if (state.isAir()) {
                return false;
            }
            if (state.is(block)) {
                return true;
            }
        }
        return false;
    }

    public void reset() {
        id = -1;
        size = 0;
        customData = new short[16];
        dirty = true;
        statGain = 0;
        statResistance = 0;
        statGrowth = 0;
        nutrients = -1;
        airQuality = -1;
        humidity = -1;
        growthPoints = 0;
        upgraded = false;
        scanLevel = 0;
    }

    public int getSprite() {
        if (id < 0) {
            return upgraded ? 1 : 0;
        }
        return crop().getSpriteIndex(this);
    }

    /** Cropnalyzer click message: name from scan level 1, stats from 4; {@code null} if unscanned or empty. */
    @Nullable
    public Component getScanned() {
        if (scanLevel <= 0 || id < 0) {
            return null;
        }
        if (scanLevel >= 4) {
            return Component.translatable("message.craft.crop.scanned", crop().displayName(), statGrowth, statGain,
                    statResistance);
        }
        return crop().displayName();
    }

    /** Plants without the {@code canGrow} check of {@link #tryPlantIn} (tests). */
    void forcePlant(int cropId, int plantSize, int growth, int gain, int resistance, int scan) {
        reset();
        id = cropId;
        size = plantSize;
        statGrowth = growth;
        statGain = gain;
        statResistance = resistance;
        scanLevel = scan;
        setChanged();
    }

    /** Sets stats directly (tests). */
    public void setStats(int growth, int gain, int resistance) {
        statGrowth = growth;
        statGain = gain;
        statResistance = resistance;
        setChanged();
    }

    public void setStorage(int nutrient, int water, int ex) {
        nutrientStorage = nutrient;
        waterStorage = water;
        exStorage = ex;
        setChanged();
    }

    // --- persistence and sync ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        id = tag.getShort("cropid");
        size = tag.getByte("size");
        statGrowth = tag.getByte("statGrowth");
        statGain = tag.getByte("statGain");
        statResistance = tag.getByte("statResistance");
        for (int i = 0; i < customData.length; i++) {
            customData[i] = tag.getShort("data" + i);
        }
        growthPoints = tag.getInt("growthPoints");
        nutrientStorage = tag.getInt("nutrientStorage");
        waterStorage = tag.getInt("waterStorage");
        upgraded = tag.getBoolean("upgraded");
        scanLevel = tag.getByte("scanLevel");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("cropid", (short) id);
        tag.putByte("size", (byte) size);
        tag.putByte("statGrowth", (byte) statGrowth);
        tag.putByte("statGain", (byte) statGain);
        tag.putByte("statResistance", (byte) statResistance);
        for (int i = 0; i < customData.length; i++) {
            tag.putShort("data" + i, customData[i]);
        }
        tag.putInt("growthPoints", growthPoints);
        tag.putInt("nutrientStorage", nutrientStorage);
        tag.putInt("waterStorage", waterStorage);
        tag.putBoolean("upgraded", upgraded);
        tag.putByte("scanLevel", (byte) scanLevel);
    }

    /** Synced like IC2's networked fields: id, size, upgraded, custom data. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putShort("cropid", (short) id);
        tag.putByte("size", (byte) size);
        tag.putBoolean("upgraded", upgraded);
        for (int i = 0; i < customData.length; i++) {
            tag.putShort("data" + i, customData[i]);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        id = tag.getShort("cropid");
        size = tag.getByte("size");
        upgraded = tag.getBoolean("upgraded");
        for (int i = 0; i < customData.length; i++) {
            customData[i] = tag.getShort("data" + i);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }
}
