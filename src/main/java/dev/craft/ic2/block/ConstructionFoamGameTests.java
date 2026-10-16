package dev.craft.ic2.block;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableBlock;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.armor.CfPackItem;
import dev.craft.ic2.item.tool.FoamSprayerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for construction foam, scaffolds and the luminator (phase 12.2). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ConstructionFoamGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private ConstructionFoamGameTests() {
    }

    private static BlockHitResult hit(GameTestHelper helper, BlockPos relative, Direction face) {
        BlockPos pos = helper.absolutePos(relative);
        return new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false);
    }

    private static int countFoam(GameTestHelper helper, BlockPos center) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -3, -3), center.offset(3, 3, 3))) {
            if (helper.getLevel().getBlockState(helper.absolutePos(pos)).is(Ic2Blocks.CONSTRUCTION_FOAM.get())) {
                count++;
            }
        }
        return count;
    }

    @GameTest(template = EMPTY)
    public static void sprayerFloodFillsThirteenBlocksAndFoamsCables(GameTestHelper helper) {
        BlockPos start = new BlockPos(4, 2, 4);
        BlockPos cable = start.east();
        helper.setBlock(cable, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        boolean[] all = {true, true, true, true, true, true};
        helper.assertTrue(FoamSprayerItem.sprayFoam(helper.getLevel(), helper.absolutePos(start), all, false),
                "sprayed");
        int cableFoamed = helper.getBlockState(cable).getValue(CableBlock.FOAM) == CableBlock.Foam.WET ? 1 : 0;
        helper.assertValueEqual(countFoam(helper, start) + cableFoamed, FoamSprayerItem.SPRAY_MASS, "foamed blocks");
        helper.assertBlockPresent(Ic2Blocks.CONSTRUCTION_FOAM.get(), start);
        BlockPos stone = new BlockPos(1, 1, 1);
        helper.setBlock(stone, Blocks.STONE);
        helper.assertFalse(FoamSprayerItem.sprayFoam(helper.getLevel(), helper.absolutePos(stone), all, false),
                "no foam into stone");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sprayerCostsAndCfPack(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sprayer = Ic2Items.FOAM_SPRAYER.toStack();
        sprayer.setDamageValue(FoamSprayerItem.MAX_USABLE_DAMAGE);
        player.setItemInHand(InteractionHand.MAIN_HAND, sprayer);
        BlockPos floor = new BlockPos(1, 1, 1);
        helper.setBlock(floor, Blocks.STONE);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, floor, Direction.UP));
        helper.assertValueEqual(sprayer.useOn(context), InteractionResult.SUCCESS, "spray");
        helper.assertBlockPresent(Ic2Blocks.CONSTRUCTION_FOAM.get(), floor.above());
        helper.assertValueEqual(sprayer.getDamageValue(), FoamSprayerItem.EMPTY_DAMAGE, "damage after spray");
        helper.assertValueEqual(sprayer.useOn(context), InteractionResult.FAIL, "empty sprayer");

        ItemStack pack = Ic2Items.CF_PACK.toStack();
        pack.setDamageValue(CfPackItem.EMPTY_DAMAGE - 1);
        player.setItemSlot(EquipmentSlot.CHEST, pack);
        BlockPos other = new BlockPos(5, 1, 5);
        helper.setBlock(other, Blocks.STONE);
        helper.assertValueEqual(sprayer.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                hit(helper, other, Direction.UP))), InteractionResult.SUCCESS, "spray from pack");
        helper.assertValueEqual(sprayer.getDamageValue(), FoamSprayerItem.EMPTY_DAMAGE, "sprayer untouched");
        helper.assertValueEqual(pack.getDamageValue(), CfPackItem.EMPTY_DAMAGE, "pack used one pellet");
        helper.assertFalse(((CfPackItem) pack.getItem()).getCfPellet(pack), "pack empty");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sprayerTurnsWoodenScaffoldIntoFoam(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sprayer = Ic2Items.FOAM_SPRAYER.toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, sprayer);
        BlockPos base = new BlockPos(2, 1, 2);
        helper.setBlock(base, Blocks.STONE);
        ScaffoldBlock scaffold = Ic2Blocks.WOODEN_SCAFFOLD.get();
        helper.setBlock(base.above(), scaffold.defaultBlockState());
        helper.setBlock(base.above(2), scaffold.defaultBlockState());
        sprayer.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, base.above(), Direction.NORTH)));
        helper.assertBlockPresent(Ic2Blocks.CONSTRUCTION_FOAM.get(), base.above());
        helper.assertBlockPresent(Ic2Blocks.CONSTRUCTION_FOAM.get(), base.above(2));
        helper.assertItemEntityCountIs(Ic2Items.WOODEN_SCAFFOLD.get(), base.above(), 3.0, 2);
        helper.assertValueEqual(sprayer.getDamageValue(), FoamSprayerItem.DAMAGE_PER_SPRAY, "damage");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void foamHardensWithSandAndWallTakesPaint(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sand = new ItemStack(Items.SAND, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, sand);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Ic2Blocks.CONSTRUCTION_FOAM.get());
        BlockState foam = helper.getBlockState(pos);
        foam.useItemOn(sand, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(helper, pos, Direction.UP));
        helper.assertBlockProperty(pos, ConstructionFoamWallBlock.COLOR, ConstructionFoamBlock.HARDENED_COLOR);
        helper.assertValueEqual(sand.getCount(), 1, "sand used");
        ConstructionFoamWallBlock wall = Ic2Blocks.CONSTRUCTION_FOAM_WALL.get();
        helper.assertTrue(wall.colorBlock(helper.getLevel(), helper.absolutePos(pos), 1), "painted");
        helper.assertFalse(wall.colorBlock(helper.getLevel(), helper.absolutePos(pos), 1), "same color");
        helper.assertBlockProperty(pos, ConstructionFoamWallBlock.COLOR, 1);
        helper.assertTrue(ConstructionFoamBlock.canFoamReplace(helper.getLevel(), helper.absolutePos(pos.above())),
                "air takes foam");
        helper.assertFalse(ConstructionFoamBlock.canFoamReplace(helper.getLevel(), helper.absolutePos(pos)),
                "wall takes no foam");
        helper.succeed();
    }

    /** Wood: 2 on a pillar, minus one per step sideways; reinforced 5; left click extends the column. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void scaffoldStrengthReinforceAndExtend(GameTestHelper helper) {
        ScaffoldBlock scaffold = Ic2Blocks.WOODEN_SCAFFOLD.get();
        BlockPos base = new BlockPos(1, 1, 1);
        helper.setBlock(base, Blocks.STONE);
        int[] expected = {2, 1, 0, -1};
        for (int i = 0; i < expected.length; i++) {
            BlockPos pos = base.above().east(i);
            int strength = scaffold.getStrengthFrom(helper.getLevel(), helper.absolutePos(pos));
            helper.assertValueEqual(strength, expected[i], "strength " + i);
            if (strength >= 0) {
                helper.setBlock(pos, scaffold.defaultBlockState().setValue(ScaffoldBlock.STRENGTH, strength));
            }
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sticks = new ItemStack(Items.STICK, 3);
        BlockPos pillar = base.above();
        helper.getBlockState(pillar).useItemOn(sticks, helper.getLevel(), player, InteractionHand.MAIN_HAND,
                hit(helper, pillar, Direction.NORTH));
        helper.assertBlockProperty(pillar, ScaffoldBlock.STRENGTH, scaffold.getReinforcedStrength());
        helper.assertValueEqual(sticks.getCount(), 1, "two sticks used");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.WOODEN_SCAFFOLD.get(), 2));
        helper.getBlockState(pillar).attack(helper.getLevel(), helper.absolutePos(pillar), player);
        helper.assertBlockProperty(pillar.above(), ScaffoldBlock.STRENGTH, 4);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "one scaffold placed");
        helper.succeedWhen(() -> helper.assertBlockProperty(pillar.east(), ScaffoldBlock.STRENGTH, 4));
    }

    /** Breaking the pillar's base drops the reinforcement, then the unsupported scaffolds fall apart. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void scaffoldBreaksWithoutSupport(GameTestHelper helper) {
        ScaffoldBlock scaffold = Ic2Blocks.IRON_SCAFFOLD.get();
        BlockPos base = new BlockPos(2, 1, 2);
        helper.setBlock(base, Blocks.STONE);
        helper.setBlock(base.above(), scaffold.defaultBlockState()
                .setValue(ScaffoldBlock.STRENGTH, scaffold.getReinforcedStrength()));
        helper.setBlock(base, Blocks.AIR);
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(scaffold, base.above());
            helper.assertItemEntityPresent(Ic2Items.IRON_FENCE.get(), base.above(), 2.0);
            helper.assertItemEntityPresent(Ic2Items.IRON_SCAFFOLD.get(), base.above(), 2.0);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void luminatorBurnsEnergyAndConnectsOnlyToItsCable(GameTestHelper helper) {
        BlockPos cablePos = new BlockPos(2, 1, 2);
        BlockPos pos = cablePos.above();
        BlockPos sideCable = pos.east();
        helper.setBlock(cablePos, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.setBlock(pos, Ic2Blocks.LUMINATOR.get().defaultBlockState().setValue(LuminatorBlock.FACING, Direction.UP)
                .setValue(LuminatorBlock.FULL_COVER, true));
        helper.setBlock(sideCable, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.assertBlockProperty(cablePos, CableBlock.CONNECTIONS.get(Direction.UP), true);
        helper.assertBlockProperty(sideCable, CableBlock.CONNECTIONS.get(Direction.WEST), false);

        LuminatorBlockEntity luminator = helper.getBlockEntity(pos);
        helper.assertValueEqual(luminator.injectEnergy(Direction.DOWN, 3), 0, "accepted");
        helper.assertBlockProperty(pos, LuminatorBlock.LIT, true);
        helper.assertValueEqual(luminator.demandsEnergy(), LuminatorBlockEntity.MAX_ENERGY - 3, "demand");
        helper.succeedWhen(() -> helper.assertBlockProperty(pos, LuminatorBlock.LIT, false));
    }

    @GameTest(template = EMPTY)
    public static void luminatorNeedsSupportAndExplodesOnOvervoltage(GameTestHelper helper) {
        BlockPos base = new BlockPos(2, 1, 2);
        helper.setBlock(base, Blocks.STONE);
        BlockPos pos = base.above();
        helper.setBlock(pos, Ic2Blocks.LUMINATOR.get().defaultBlockState().setValue(LuminatorBlock.FACING, Direction.UP));
        LuminatorBlockEntity luminator = helper.getBlockEntity(pos);
        luminator.injectEnergy(Direction.DOWN, LuminatorBlockEntity.MAX_INPUT + 1);
        helper.assertBlockNotPresent(Ic2Blocks.LUMINATOR.get(), pos);

        helper.setBlock(base, Blocks.STONE);
        helper.setBlock(pos, Ic2Blocks.LUMINATOR.get().defaultBlockState().setValue(LuminatorBlock.FACING, Direction.UP));
        helper.setBlock(base, Blocks.AIR);
        helper.assertBlockNotPresent(Ic2Blocks.LUMINATOR.get(), pos);
        helper.assertItemEntityNotPresent(Ic2Items.LUMINATOR.get(), pos, 2.0);
        helper.succeed();
    }
}
