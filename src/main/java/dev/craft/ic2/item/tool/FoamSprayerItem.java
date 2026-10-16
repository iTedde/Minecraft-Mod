package dev.craft.ic2.item.tool;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.block.ConstructionFoamBlock;
import dev.craft.ic2.cable.CableBlock;
import dev.craft.ic2.item.armor.CfPackItem;
import dev.craft.ic2.item.reactor.GradualItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Construction foam sprayer (IC2 {@code ItemSprayer}, an {@code ItemGradual} with 1602 damage, filled with pellets in
 * the crafting grid). A spray costs 100 damage or, if the player wears a CF pack, one pellet from the pack. It fills up
 * to {@value #SPRAY_MASS} blocks by a random flood fill away from the player, foams cables in reach and turns a
 * connected wooden scaffold into foam.
 */
public class FoamSprayerItem extends GradualItem {
    public static final int MAX_DAMAGE = 1602;
    /** Damage of an empty sprayer (as crafted). */
    public static final int EMPTY_DAMAGE = 1601;
    /** Above this damage the sprayer cannot spray on its own. */
    public static final int MAX_USABLE_DAMAGE = 1501;
    public static final int DAMAGE_PER_SPRAY = 100;
    /** IC2 {@code getSprayMass()}. */
    public static final int SPRAY_MASS = 13;

    public FoamSprayerItem(Properties properties) {
        super(properties, MAX_DAMAGE);
    }

    @Override
    public int getMaxDamageEx() {
        return MAX_DAMAGE;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide || player == null) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        // IC2 takes the pellet from the pack before it knows whether anything can be sprayed.
        ItemStack pack = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean pulledFromCfPack = pack.getItem() instanceof CfPackItem cfPack && cfPack.getCfPellet(pack);
        if (!pulledFromCfPack && stack.getDamageValue() > MAX_USABLE_DAMAGE) {
            return InteractionResult.FAIL;
        }
        boolean[] directions = calculateDirectionsFromPlayer(player);
        BlockPos pos = context.getClickedPos();
        boolean scaffold = level.getBlockState(pos).is(Ic2Blocks.WOODEN_SCAFFOLD.get());
        if (!scaffold) {
            pos = pos.relative(context.getClickedFace());
        }
        if (!sprayFoam(level, pos, directions, scaffold)) {
            return InteractionResult.FAIL;
        }
        if (!pulledFromCfPack) {
            stack.setDamageValue(stack.getDamageValue() + DAMAGE_PER_SPRAY);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * IC2 {@code calculateDirectionsFromPlayer}: flags for down, up, north, south, west, east; the side behind the
     * player and – when looking steeply – the vertical side behind are excluded. IC2 does not normalize negative yaw.
     */
    public static boolean[] calculateDirectionsFromPlayer(Player player) {
        float yaw = player.getYRot() % 360.0F;
        float pitch = player.getXRot();
        boolean[] r = {true, true, true, true, true, true};
        if (pitch >= -65.0F && pitch <= 65.0F) {
            if (yaw >= 300.0F && yaw <= 360.0F || yaw >= 0.0F && yaw <= 60.0F) {
                r[2] = false;
            }
            if (yaw >= 30.0F && yaw <= 150.0F) {
                r[5] = false;
            }
            if (yaw >= 120.0F && yaw <= 240.0F) {
                r[3] = false;
            }
            if (yaw >= 210.0F && yaw <= 330.0F) {
                r[4] = false;
            }
        }
        if (pitch <= -40.0F) {
            r[0] = false;
        }
        if (pitch >= 40.0F) {
            r[1] = false;
        }
        return r;
    }

    /**
     * IC2 {@code sprayFoam}: breadth-first over at most {@value #SPRAY_MASS} matching blocks, neighbors in random order.
     * Without {@code scaffold} it matches foamable space (see {@link ConstructionFoamBlock#canFoamReplace}) and cables
     * and only spreads in {@code directions}; with it, only wooden scaffolds in every direction (they drop as broken).
     */
    public static boolean sprayFoam(Level level, BlockPos start, boolean[] directions, boolean scaffold) {
        if (!matches(level, start, scaffold)) {
            return false;
        }
        List<BlockPos> check = new ArrayList<>();
        Set<BlockPos> checked = new HashSet<>();
        Set<BlockPos> place = new LinkedHashSet<>();
        check.add(start);
        checked.add(start);
        int foamCount = SPRAY_MASS;
        for (int i = 0; i < check.size() && foamCount > 0; i++) {
            BlockPos pos = check.get(i);
            if (matches(level, pos, scaffold)) {
                place.add(pos);
                for (int direction : generateRngSpread(level.random)) {
                    if (scaffold || directions[direction]) {
                        BlockPos next = pos.relative(Direction.from3DDataValue(direction));
                        if (checked.add(next)) {
                            check.add(next);
                        }
                    }
                }
                foamCount--;
            }
        }
        for (BlockPos pos : place) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Ic2Blocks.WOODEN_SCAFFOLD.get())) {
                Block.dropResources(state, level, pos);
                level.setBlock(pos, Ic2Blocks.CONSTRUCTION_FOAM.get().defaultBlockState(), Block.UPDATE_ALL);
            } else if (state.getBlock() instanceof CableBlock) {
                CableBlock.setFoam(level, pos, CableBlock.Foam.WET);
            } else {
                level.setBlock(pos, Ic2Blocks.CONSTRUCTION_FOAM.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        return true;
    }

    private static boolean matches(Level level, BlockPos pos, boolean scaffold) {
        if (scaffold) {
            return level.getBlockState(pos).is(Ic2Blocks.WOODEN_SCAFFOLD.get());
        }
        return ConstructionFoamBlock.canFoamReplace(level, pos)
                || level.getBlockState(pos).getBlock() instanceof CableBlock;
    }

    /** IC2 {@code generateRngSpread}: 16 random swaps of the six direction indices. */
    private static int[] generateRngSpread(RandomSource random) {
        int[] order = {0, 1, 2, 3, 4, 5};
        for (int i = 0; i < 16; i++) {
            int first = random.nextInt(6);
            int second = random.nextInt(6);
            int save = order[first];
            order[first] = order[second];
            order[second] = save;
        }
        return order;
    }

    /** The empty sprayer as crafted. */
    public static ItemStack createEmpty() {
        ItemStack stack = new ItemStack(Ic2Items.FOAM_SPRAYER.get());
        stack.setDamageValue(EMPTY_DAMAGE);
        return stack;
    }
}
