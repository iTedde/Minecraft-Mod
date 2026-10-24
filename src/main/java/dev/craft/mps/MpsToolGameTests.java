package dev.craft.mps;

import java.util.List;

import dev.craft.Craft;
import dev.craft.mps.MpsTool.ToolType;
import dev.craft.mps.entity.MpsEntityTypes;
import dev.craft.mps.entity.PlasmaBoltEntity;
import dev.craft.mps.module.MpsModules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 28.4: Power Tool mining, melee assist, modes and the right-click modules (spec 29 §8). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class MpsToolGameTests {
    private static final double EPSILON = 1.0E-6;

    private MpsToolGameTests() {
    }

    private static void assertNear(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < EPSILON, message + ": expected " + expected + ", got "
                + actual);
    }

    /** A power tool with a basic battery holding {@code joules} and the given modules, sliders at {@code tweak}. */
    private static ItemStack tool(double joules, double tweak, String... modules) {
        ItemStack stack = MpsItems.POWER_TOOL.toStack();
        ModularItems.addModule(stack, MpsModules.BASIC_BATTERY);
        for (String module : modules) {
            ModularItems.addModule(stack, module);
            for (String name : MpsModules.get(module).getTweaks()) {
                ModularItems.setTweak(stack, module, name, tweak);
            }
        }
        MpsEnergy.setJoules(stack, joules);
        return stack;
    }

    private static ItemStack hold(Player player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player.getMainHandItem();
    }

    /** Pickaxe → shovel → axe → diamond; the pickaxe covers obsidian too (no harvest level in Forge 1.4.7). */
    @GameTest(template = "empty_8x4x8")
    public static void toolTypes(GameTestHelper helper) {
        ItemStack all = tool(0.0, 0.0, MpsModules.PICKAXE, MpsModules.SHOVEL, MpsModules.AXE);
        helper.assertTrue(MpsTool.toolType(all, Blocks.STONE.defaultBlockState()) == ToolType.PICKAXE, "stone");
        helper.assertTrue(MpsTool.toolType(all, Blocks.OBSIDIAN.defaultBlockState()) == ToolType.PICKAXE,
                "obsidian with the pickaxe");
        helper.assertTrue(MpsTool.toolType(all, Blocks.DIRT.defaultBlockState()) == ToolType.SHOVEL, "dirt");
        helper.assertTrue(MpsTool.toolType(all, Blocks.OAK_LOG.defaultBlockState()) == ToolType.AXE, "log");
        helper.assertTrue(MpsTool.toolType(all, Blocks.POPPY.defaultBlockState()) == ToolType.AXE, "plant");
        helper.assertTrue(MpsTool.toolType(all, Blocks.GLASS.defaultBlockState()) == null, "glass");
        ModularItems.setActive(all, MpsModules.PICKAXE, false);
        helper.assertTrue(MpsTool.toolType(all, Blocks.STONE.defaultBlockState()) == null, "pickaxe off");
        ItemStack diamond = tool(0.0, 0.0, MpsModules.DIAMOND_DRILL_UPGRADE);
        helper.assertTrue(MpsTool.toolType(diamond, Blocks.OBSIDIAN.defaultBlockState()) == ToolType.DIAMOND,
                "diamond drill alone");
        helper.succeed();
    }

    /**
     * Harvest speed 8 (+17 × overclock) above the energy cost, 1 without a type or energy; the diamond drill alone
     * uses the missing pickaxe's speed 0.
     */
    @GameTest(template = "empty_8x4x8")
    public static void harvestAndSpeed(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = hold(player, tool(20000.0, 1.0, MpsModules.PICKAXE));
        assertNear(helper, MpsTool.breakSpeed(player, stack, Blocks.STONE.defaultBlockState(), 0.2), 0.2 * 25.0,
                "vanilla speed × 25");
        assertNear(helper, MpsTool.breakSpeed(player, stack, Blocks.DIRT.defaultBlockState(), 0.2), 1.0,
                "no type → 1");
        helper.assertTrue(MpsTool.canHarvest(player, stack, Blocks.OBSIDIAN.defaultBlockState()), "obsidian");
        MpsEnergy.setJoules(stack, 1000.0);
        helper.assertFalse(MpsTool.canHarvest(player, stack, Blocks.OBSIDIAN.defaultBlockState()),
                "1 000 J not above 1 000 J");
        assertNear(helper, MpsTool.breakSpeed(player, stack, Blocks.STONE.defaultBlockState(), 0.2), 1.0,
                "no energy → 1");
        ItemStack diamond = hold(player, tool(20000.0, 0.0, MpsModules.DIAMOND_DRILL_UPGRADE));
        assertNear(helper, MpsTool.breakSpeed(player, diamond, Blocks.STONE.defaultBlockState(), 1.0), 0.0,
                "diamond drill without pickaxe");
        helper.succeed();
    }

    /** Each mined block costs the type's energy from the pool; blocks without a type are free. */
    @GameTest(template = "empty_8x4x8")
    public static void miningCost(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = hold(player, tool(20000.0, 0.0, MpsModules.PICKAXE, MpsModules.AQUA_AFFINITY));
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        stack.mineBlock(helper.getLevel(), Blocks.STONE.defaultBlockState(), pos, player);
        assertNear(helper, MpsEnergy.getJoules(stack), 19900.0, "100 J for stone (dry: no aqua cost)");
        stack.mineBlock(helper.getLevel(), Blocks.DIRT.defaultBlockState(), pos, player);
        assertNear(helper, MpsEnergy.getJoules(stack), 19900.0, "dirt without shovel free");
        helper.succeed();
    }

    /** Iron golem (no armor): 10 J and a second hit of 2; right after it only damage above the first hit would pass the cooldown. */
    @GameTest(template = "empty_8x4x8")
    public static void meleeAssist(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = hold(player, tool(20000.0, 0.0, MpsModules.MELEE_ASSIST));
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(1, 1, 1));
        float health = golem.getHealth();
        MpsTool.meleeAssist(stack, golem, player);
        assertNear(helper, golem.getHealth(), health - 2.0F, "melee damage 2");
        assertNear(helper, MpsEnergy.getJoules(stack), 20000.0 - 10.0, "10 J");
        MpsTool.meleeAssist(stack, golem, player);
        assertNear(helper, golem.getHealth(), health - 2.0F, "hurt cooldown");
        assertNear(helper, MpsEnergy.getJoules(stack), 20000.0 - 20.0, "paid anyway");
        helper.succeed();
    }

    /** Modes are the installed right-click modules in order; the server accepts only those, slots 0–8. */
    @GameTest(template = "empty_8x4x8")
    public static void modes(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = hold(player, tool(0.0, 0.0, MpsModules.BLINK_DRIVE, MpsModules.RAILGUN,
                MpsModules.MELEE_ASSIST));
        helper.assertTrue(ModularItems.getModes(stack).equals(List.of(MpsModules.RAILGUN, MpsModules.BLINK_DRIVE)),
                "modes " + ModularItems.getModes(stack));
        helper.assertFalse(ModularItems.hasActiveModule(stack, MpsModules.RAILGUN), "no mode yet");
        int slot = player.getInventory().selected;
        MpsTool.changeMode(player, slot, MpsModules.BLINK_DRIVE);
        helper.assertTrue(ModularItems.getMode(stack).equals(MpsModules.BLINK_DRIVE), "blink drive");
        helper.assertTrue(ModularItems.hasActiveModule(stack, MpsModules.BLINK_DRIVE), "active as mode");
        MpsTool.changeMode(player, slot, MpsModules.PLASMA_CANNON);
        MpsTool.changeMode(player, 9, MpsModules.RAILGUN);
        helper.assertTrue(ModularItems.getMode(stack).equals(MpsModules.BLINK_DRIVE), "rejected");
        helper.assertTrue(MpsTool.clampMode(2, 2) == 0 && MpsTool.clampMode(-1, 2) == 1
                && MpsTool.clampMode(0, 3) == 0 && MpsTool.clampMode(4, 3) == 1, "wheel wraps");
        helper.succeed();
    }

    /** Impulse 500 → 5 damage for 500 J to the iron golem (no armor) ahead. */
    @GameTest(template = "empty_8x4x8")
    public static void railgun(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(1.5, 1.0, 1.5)), 0.0F, 0.0F);
        ItemStack stack = hold(player, tool(20000.0, 0.0, MpsModules.RAILGUN));
        stack.set(MpsDataComponents.MODE.get(), MpsModules.RAILGUN);
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(1, 1, 5));
        float health = golem.getHealth();
        helper.assertTrue(MpsTool.rightClick(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND),
                "railgun used");
        assertNear(helper, golem.getHealth(), health - 5.0F, "5 damage");
        assertNear(helper, MpsEnergy.getJoules(stack), 19500.0, "500 J");
        helper.succeed();
    }

    /** Range 5: hits the wall's north face 2.5 m ahead and lands half a block in front of it for 1 000 J. */
    @GameTest(template = "empty_8x4x8")
    public static void blinkDrive(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 start = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.moveTo(start, 0.0F, 0.0F);
        ItemStack stack = hold(player, tool(20000.0, 0.0, MpsModules.BLINK_DRIVE));
        stack.set(MpsDataComponents.MODE.get(), MpsModules.BLINK_DRIVE);
        helper.setBlock(new BlockPos(1, 2, 4), Blocks.STONE);
        MpsTool.rightClick(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND);
        assertNear(helper, player.getZ(), start.z + 2.0, "z in front of the wall");
        assertNear(helper, player.getX(), start.x, "x unchanged");
        assertNear(helper, MpsEnergy.getJoules(stack), 19000.0, "1 000 J");
        helper.succeed();
    }

    /** 20 ticks charge 200 J; full charge with amperage 1 kills a zombie (20 damage). */
    @GameTest(template = "empty_8x4x8", timeoutTicks = 60)
    public static void plasmaCannon(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(1.5, 1.0, 1.5)), 0.0F, 0.0F);
        ItemStack stack = tool(20000.0, 0.0, MpsModules.PLASMA_CANNON);
        ModularItems.setTweak(stack, MpsModules.PLASMA_CANNON, "Amperage", 1.0);
        stack = hold(player, stack);
        stack.set(MpsDataComponents.MODE.get(), MpsModules.PLASMA_CANNON);
        MpsTool.releasePlasma(player, helper.getLevel(), stack, MpsTool.USE_DURATION - 5);
        assertNear(helper, MpsEnergy.getJoules(stack), 20000.0 - 160.0 * 10, "at least 10 ticks");
        AABB area = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16.0);
        List<PlasmaBoltEntity> bolts = helper.getLevel().getEntities(MpsEntityTypes.PLASMA_BOLT.get(), area,
                bolt -> true);
        helper.assertTrue(bolts.size() == 1 && bolts.get(0).getSize() == 10, "bolt of size 10");
        bolts.get(0).discard();
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 5));
        MpsEnergy.setJoules(stack, 20000.0);
        MpsTool.releasePlasma(player, helper.getLevel(), stack, MpsTool.USE_DURATION - 60);
        assertNear(helper, MpsEnergy.getJoules(stack), 20000.0 - 160.0 * 60, "60 ticks charged");
        helper.succeedWhen(() -> helper.assertTrue(!zombie.isAlive(), "zombie killed by the bolt"));
    }
}
