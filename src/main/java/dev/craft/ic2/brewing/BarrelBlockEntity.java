package dev.craft.ic2.brewing;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Booze barrel (IC2 {@code TileEntityBarrel}). Filled with water (bucket or cells), wheat and hops it brews beer whose
 * quality ratios are fixed ("undetailed") on the first draining; filled with sugar cane it brews rum. It ages every
 * tick while it holds liquid and has no treetap. The treetap side lives in the block state ({@link BarrelBlock}).
 */
public class BarrelBlockEntity extends BlockEntity {
    public static final int MAX_AMOUNT = 32;
    public static final int MAX_SOLIDS = 64;

    private int type;
    private int boozeAmount;
    private int age;
    private boolean detailed = true;
    private int hopsCount;
    private int wheatCount;
    private int solidRatio;
    private int hopsRatio;
    private int timeRatio;

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.BARREL.get(), pos, state);
    }

    /** IC2 {@code set}: restores the contents of a barrel item. */
    public void set(int value) {
        type = Booze.getType(value);
        if (type > Booze.TYPE_NONE) {
            boozeAmount = Booze.getAmount(value);
        }
        if (type == Booze.TYPE_BEER) {
            detailed = false;
            hopsRatio = Booze.getHopsRatio(value);
            solidRatio = Booze.getSolidRatio(value);
            timeRatio = Booze.getTimeRatio(value);
        }
        if (type == Booze.TYPE_RUM) {
            detailed = true;
            age = timeNeededForRum(boozeAmount) * Booze.getRumProgress(value) / 100;
        }
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BarrelBlockEntity barrel) {
        if (barrel.isEmpty() || BarrelBlock.isTapped(state)) {
            return;
        }
        barrel.age++;
        if (barrel.type == Booze.TYPE_BEER && barrel.timeRatio < Booze.BLACK_STUFF) {
            int x = barrel.timeRatio;
            if (x == 4) {
                x += 2;
            }
            if (barrel.age >= 24000.0 * Math.pow(3.0, x)) {
                barrel.age = 0;
                barrel.timeRatio++;
            }
        }
        barrel.setChanged();
    }

    public boolean isEmpty() {
        return type == Booze.TYPE_NONE || boozeAmount <= 0;
    }

    /**
     * IC2 {@code rightclick} with the main hand item: water bucket, water cells, wheat and hops fill a beer barrel,
     * sugar cane a rum barrel (only while young). Sneaking adds a single item; stacks shrink in creative mode too.
     */
    public boolean rightClick(Player player) {
        ItemStack cur = player.getMainHandItem();
        if (cur.isEmpty()) {
            return false;
        }
        if (cur.is(Items.WATER_BUCKET)) {
            if (detailed && boozeAmount < MAX_AMOUNT && type <= Booze.TYPE_BEER) {
                type = Booze.TYPE_BEER;
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
                boozeAmount++;
                setChanged();
                return true;
            }
            return false;
        }
        if (cur.is(Ic2Items.WATER_CELL.get())) {
            if (!detailed || type > Booze.TYPE_BEER) {
                return false;
            }
            type = Booze.TYPE_BEER;
            int give = Math.min(wantGive(player, cur), MAX_AMOUNT - boozeAmount);
            if (give <= 0) {
                return false;
            }
            boozeAmount += give;
            cur.shrink(give);
            setChanged();
            return true;
        }
        if (cur.is(Items.WHEAT) || cur.is(Ic2Items.HOPS.get())) {
            if (!detailed || type > Booze.TYPE_BEER) {
                return false;
            }
            type = Booze.TYPE_BEER;
            boolean wheat = cur.is(Items.WHEAT);
            int give = Math.min(wantGive(player, cur), MAX_SOLIDS - (wheat ? wheatCount : hopsCount));
            if (give <= 0) {
                return false;
            }
            if (wheat) {
                wheatCount += give;
            } else {
                hopsCount += give;
            }
            cur.shrink(give);
            alterComposition();
            setChanged();
            return true;
        }
        if (!cur.is(Items.SUGAR_CANE)) {
            return false;
        }
        if (age <= 600 && (type <= Booze.TYPE_NONE || type == Booze.TYPE_RUM)) {
            type = Booze.TYPE_RUM;
            int give = Math.min(wantGive(player, cur), MAX_AMOUNT - boozeAmount);
            if (give <= 0) {
                return false;
            }
            boozeAmount += give;
            cur.shrink(give);
            setChanged();
            return true;
        }
        return false;
    }

    private static int wantGive(Player player, ItemStack stack) {
        return player.isShiftKeyDown() ? 1 : stack.getCount();
    }

    /** IC2 {@code alterComposition}: adding solids restarts fresh brew and may spoil older beer. */
    private void alterComposition() {
        if (timeRatio == 0) {
            age = 0;
        }
        if (timeRatio == 1) {
            if (level.random.nextBoolean()) {
                timeRatio = 0;
            } else if (level.random.nextBoolean()) {
                timeRatio = Booze.BLACK_STUFF;
            }
        }
        if (timeRatio == 2 && level.random.nextBoolean()) {
            timeRatio = Booze.BLACK_STUFF;
        }
        if (timeRatio > 2) {
            timeRatio = Booze.BLACK_STUFF;
        }
    }

    /**
     * IC2 {@code drainLiquid}. Original quirk: for rum the progress is rescaled with integer percent arithmetic
     * ({@code progress / 100 * time}), so draining an unfinished rum barrel resets its age to 0.
     */
    public boolean drainLiquid(int amount) {
        if (isEmpty() || amount > boozeAmount) {
            return false;
        }
        enforceUndetailed();
        if (type == Booze.TYPE_RUM) {
            int progress = age * 100 / timeNeededForRum(boozeAmount);
            boozeAmount -= amount;
            age = progress / 100 * timeNeededForRum(boozeAmount);
        } else {
            boozeAmount -= amount;
        }
        if (boozeAmount <= 0) {
            if (type == Booze.TYPE_BEER) {
                hopsCount = 0;
                wheatCount = 0;
                hopsRatio = 0;
                solidRatio = 0;
                timeRatio = 0;
            }
            type = Booze.TYPE_NONE;
            detailed = true;
            boozeAmount = 0;
        }
        setChanged();
        return true;
    }

    /**
     * IC2 {@code enforceUndetailed}: fixes the hops (hops per wheat) and solid ((hops + wheat) per water) ratios.
     * Original quirks: solid ratio 1 is unreachable (contradictory condition) and ratios ≤ 0.5 keep the previous
     * solid ratio (0 for a fresh barrel); hops without wheat count as ratio 10 and spoil the beer.
     */
    private void enforceUndetailed() {
        if (!detailed) {
            return;
        }
        detailed = false;
        if (type != Booze.TYPE_BEER) {
            return;
        }
        float hops = wheatCount > 0 ? (float) hopsCount / wheatCount : 10.0F;
        if (hopsCount <= 0 && wheatCount <= 0) {
            hops = 0.0F;
        }
        float solid = boozeAmount > 0 ? (float) (hopsCount + wheatCount) / boozeAmount : 10.0F;
        if (hops <= 0.25F) {
            hopsRatio = 0;
        }
        if (hops > 0.25F && hops <= 0.33333334F) {
            hopsRatio = 1;
        }
        if (hops > 0.33333334F && hops <= 0.5F) {
            hopsRatio = 2;
        }
        if (hops > 0.5F && hops < 2.0F) {
            hopsRatio = 3;
        }
        if (hops >= 2.0F && hops < 3.0F) {
            hopsRatio = 4;
        }
        if (hops >= 3.0F && hops < 4.0F) {
            hopsRatio = 5;
        }
        if (hops >= 4.0F && hops < 5.0F) {
            hopsRatio = 6;
        }
        if (hops >= 5.0F) {
            timeRatio = Booze.BLACK_STUFF;
        }
        if (solid > 0.5F && solid < 1.0F) {
            solidRatio = 2;
        }
        if (solid == 1.0F) {
            solidRatio = 3;
        }
        if (solid > 1.0F && solid < 2.0F) {
            solidRatio = 4;
        }
        if (solid >= 2.0F && solid < 2.4F) {
            solidRatio = 5;
        }
        if (solid >= 2.4F && solid < 4.0F) {
            solidRatio = 6;
        }
        if (solid >= 4.0F) {
            timeRatio = Booze.BLACK_STUFF;
        }
    }

    /** IC2 {@code calculateMetaValue}: the packed value of the contents (fixes the ratios first). */
    public int calculateValue() {
        if (isEmpty()) {
            return 0;
        }
        if (type == Booze.TYPE_BEER) {
            enforceUndetailed();
            setChanged();
            return Booze.beer(boozeAmount, solidRatio, hopsRatio, timeRatio);
        }
        if (type == Booze.TYPE_RUM) {
            enforceUndetailed();
            setChanged();
            return Booze.rum(boozeAmount, Math.min(age * 100 / timeNeededForRum(boozeAmount), 100));
        }
        return 0;
    }

    public static int timeNeededForRum(int amount) {
        return (int) (1200 * amount * Math.pow(0.95, amount - 1));
    }

    public int getBoozeType() {
        return type;
    }

    public int getBoozeAmount() {
        return boozeAmount;
    }

    public int getAge() {
        return age;
    }

    /** For tests: sets the age directly. */
    public void setAge(int age) {
        this.age = age;
    }

    public int getTimeRatio() {
        return timeRatio;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        type = tag.getByte("type");
        boozeAmount = tag.getByte("waterCount");
        age = tag.getInt("age");
        detailed = tag.getBoolean("detailed");
        if (type == Booze.TYPE_BEER) {
            if (detailed) {
                hopsCount = tag.getByte("hopsCount");
                wheatCount = tag.getByte("wheatCount");
            }
            solidRatio = tag.getByte("solidRatio");
            hopsRatio = tag.getByte("hopsRatio");
            timeRatio = tag.getByte("timeRatio");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("type", (byte) type);
        tag.putByte("waterCount", (byte) boozeAmount);
        tag.putInt("age", age);
        tag.putBoolean("detailed", detailed);
        if (type == Booze.TYPE_BEER) {
            if (detailed) {
                tag.putByte("hopsCount", (byte) hopsCount);
                tag.putByte("wheatCount", (byte) wheatCount);
            }
            tag.putByte("solidRatio", (byte) solidRatio);
            tag.putByte("hopsRatio", (byte) hopsRatio);
            tag.putByte("timeRatio", (byte) timeRatio);
        }
    }
}
