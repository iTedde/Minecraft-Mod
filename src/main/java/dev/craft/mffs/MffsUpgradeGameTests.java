package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.core.CraftDamageTypes;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.ForceFieldBlock;
import dev.craft.mffs.block.ForceFieldBlockEntity;
import dev.craft.mffs.block.MffsMachineBlock;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.block.ProjectorBlockEntity;
import dev.craft.mffs.field.ForceFieldType;
import dev.craft.mffs.field.ProjectorModule;
import dev.craft.mffs.field.ProjectorUpgrade;
import dev.craft.mffs.item.CardItem;
import dev.craft.mffs.item.multitool.FieldTeleporterMultitoolItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** MFFS projector upgrades and field teleporter, phase 48.3 (spec 48 §8, §9.3). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class MffsUpgradeGameTests {
    private static final String LARGE = "empty_16x8x16";
    private static final BlockPos CAPACITOR = new BlockPos(2, 1, 1);
    private static final BlockPos OTHER_CAPACITOR = new BlockPos(8, 2, 8);
    /** Containment facing south: x 2–6, y 0–4, z 3–7; interior x 3–5, y 1–3, z 4–6. */
    private static final BlockPos BIG_BOX = new BlockPos(4, 2, 2);
    /** Small containment facing south: x 3–5, y 1–3, z 5–7; the only interior point (4, 2, 6). */
    private static final BlockPos SMALL_BOX = new BlockPos(4, 2, 4);
    private static final BlockPos SMALL_INTERIOR = new BlockPos(4, 2, 6);
    private static final BlockPos WALL_PROJECTOR = new BlockPos(4, 1, 4);
    /** Wall facing south, distance 1, focus up 2: the column x 4, z 6, y 1–3. */
    private static final List<BlockPos> WALL = List.of(new BlockPos(4, 1, 6), new BlockPos(4, 2, 6),
            new BlockPos(4, 3, 6));
    private static final int START = 1_000_000;

    private MffsUpgradeGameTests() {
    }

    private static CapacitorBlockEntity capacitor(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, MffsBlocks.CAPACITOR.get());
        CapacitorBlockEntity capacitor = helper.getBlockEntity(pos);
        helper.runAfterDelay(1, () -> capacitor.setForcePower(START));
        return capacitor;
    }

    private static ProjectorBlockEntity projector(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, MffsBlocks.PROJECTOR.get().defaultBlockState().setValue(MffsMachineBlock.FACING, facing));
        return helper.getBlockEntity(pos);
    }

    private static void link(ProjectorBlockEntity projector, CapacitorBlockEntity capacitor) {
        ItemStack card = new ItemStack(MffsItems.CARD_POWER_LINK.get());
        CardItem.link(card, capacitor, capacitor.getPowerStorageId(), capacitor.getDeviceName());
        projector.setStack(ProjectorBlockEntity.SLOT_POWER_LINK, card);
    }

    /** Containment with focus {@code focus} on all four sides and {@code strength} strength modulators. */
    private static void equipBox(ProjectorBlockEntity projector, CapacitorBlockEntity capacitor, int focus,
                                 int strength, ProjectorUpgrade... upgrades) {
        link(projector, capacitor);
        projector.setStack(ProjectorBlockEntity.SLOT_MODULE,
                new ItemStack(MffsItems.module(ProjectorModule.CONTAINMENT).get()));
        for (int slot = ProjectorBlockEntity.SLOT_FOCUS_UP; slot <= ProjectorBlockEntity.SLOT_FOCUS_LEFT; slot++) {
            projector.setStack(slot, new ItemStack(MffsItems.PROJECTOR_FOCUS_MATRIX.get(), focus));
        }
        if (strength > 0) {
            projector.setStack(ProjectorBlockEntity.SLOT_STRENGTH,
                    new ItemStack(MffsItems.FIELD_MODULATOR_STRENGTH.get(), strength));
        }
        upgrades(projector, upgrades);
    }

    private static void upgrades(ProjectorBlockEntity projector, ProjectorUpgrade... upgrades) {
        for (int i = 0; i < upgrades.length; i++) {
            projector.setStack(ProjectorBlockEntity.SLOT_UPGRADE_FIRST + i,
                    new ItemStack(MffsItems.upgrade(upgrades[i]).get()));
        }
    }

    private static void equipWall(ProjectorBlockEntity projector, CapacitorBlockEntity capacitor,
                                  ProjectorUpgrade... upgrades) {
        link(projector, capacitor);
        projector.setStack(ProjectorBlockEntity.SLOT_MODULE,
                new ItemStack(MffsItems.module(ProjectorModule.WALL).get()));
        projector.setStack(ProjectorBlockEntity.SLOT_DISTANCE,
                new ItemStack(MffsItems.FIELD_MODULATOR_DISTANCE.get()));
        projector.setStack(ProjectorBlockEntity.SLOT_FOCUS_UP,
                new ItemStack(MffsItems.PROJECTOR_FOCUS_MATRIX.get(), 2));
        upgrades(projector, upgrades);
    }

    private static void switchOn(MffsMachineBlockEntity machine) {
        while (machine.getSwitchMode() != 2) {
            machine.toggleSwitchMode();
        }
        if (!machine.getSwitchValue()) {
            machine.toggleSwitchValue();
        }
    }

    private static boolean isField(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).is(MffsBlocks.FORCE_FIELD.get());
    }

    private static void use(GameTestHelper helper, Player player, ItemStack stack, BlockPos relative,
                            Direction side) {
        BlockPos pos = helper.absolutePos(relative);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), side, pos, false));
        stack.getItem().onItemUseFirst(stack, context);
    }

    /** Sponge: the water in the interior goes when the field is calculated; the water on the box becomes field. */
    @GameTest(template = LARGE, timeoutTicks = 300)
    public static void spongeDrainsInterior(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, CAPACITOR);
        ProjectorBlockEntity projector = projector(helper, SMALL_BOX, Direction.SOUTH);
        for (BlockPos pos : BlockPos.betweenClosed(3, 1, 5, 5, 3, 7)) {
            helper.setBlock(pos, Blocks.WATER);
        }
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipBox(projector, capacitor, 1, 1, ProjectorUpgrade.SPONGE);
                    switchOn(projector);
                })
                .thenWaitUntil(() -> helper.assertTrue(projector.isActive(), "active"))
                .thenExecute(() -> {
                    helper.assertBlockPresent(Blocks.AIR, SMALL_INTERIOR);
                    helper.assertTrue(isField(helper, new BlockPos(3, 1, 5)), "water corner replaced by field");
                    helper.assertValueEqual(projector.getFieldPoints().size(), 26, "26 box points");
                })
                .thenSucceed();
    }

    /** Camouflage: the field blocks have the camouflage type and the block of slot 11; a new block updates them. */
    @GameTest(template = LARGE, timeoutTicks = 300)
    public static void camouflageTakesSlotBlock(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, CAPACITOR);
        ProjectorBlockEntity projector = projector(helper, SMALL_BOX, Direction.SOUTH);
        BlockPos corner = new BlockPos(3, 1, 5);
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipBox(projector, capacitor, 1, 1, ProjectorUpgrade.CAMOUFLAGE);
                    helper.assertTrue(projector.isItemValid(ProjectorBlockEntity.SLOT_CAMOUFLAGE,
                            new ItemStack(Items.STONE)), "block allowed");
                    helper.assertTrue(projector.isItemValid(ProjectorBlockEntity.SLOT_CAMOUFLAGE,
                            new ItemStack(Items.WATER_BUCKET)), "water bucket allowed");
                    helper.assertFalse(projector.isItemValid(ProjectorBlockEntity.SLOT_CAMOUFLAGE,
                            new ItemStack(Items.DIAMOND)), "no block");
                    projector.setStack(ProjectorBlockEntity.SLOT_CAMOUFLAGE, new ItemStack(Items.STONE));
                    switchOn(projector);
                })
                .thenWaitUntil(() -> helper.assertTrue(isField(helper, corner), "field"))
                .thenExecute(() -> {
                    helper.assertValueEqual(helper.getBlockState(corner).getValue(ForceFieldBlock.TYPE),
                            ForceFieldType.CAMOUFLAGE, "camouflage type");
                    ForceFieldBlockEntity field = helper.getBlockEntity(corner);
                    helper.assertValueEqual(field.getCamouflage(), Blocks.STONE.defaultBlockState(), "stone look");
                    projector.setStack(ProjectorBlockEntity.SLOT_CAMOUFLAGE, new ItemStack(Items.WATER_BUCKET));
                    helper.assertValueEqual(field.getCamouflage(), Blocks.WATER.defaultBlockState(), "water look");
                })
                .thenSucceed();
    }

    /**
     * Jammer: a projector of another capacitor whose field point lies in the jammer's interior burns out when it
     * switches on – every slot falls out; the multitool wrench repairs it for 10 000 FE.
     */
    @GameTest(template = LARGE, timeoutTicks = 400)
    public static void jammerBurnsOutForeignProjector(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, CAPACITOR);
        CapacitorBlockEntity other = capacitor(helper, OTHER_CAPACITOR);
        ProjectorBlockEntity jammer = projector(helper, BIG_BOX, Direction.SOUTH);
        // A wall of distance 0 inside the jammer's interior: its one point (4, 2, 5).
        BlockPos victimPos = new BlockPos(4, 2, 4);
        ProjectorBlockEntity victim = projector(helper, victimPos, Direction.SOUTH);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipBox(jammer, capacitor, 2, 3, ProjectorUpgrade.FIELD_JAMMER);
                    switchOn(jammer);
                })
                .thenWaitUntil(() -> helper.assertTrue(jammer.isActive(), "jammer on"))
                .thenExecute(() -> {
                    link(victim, other);
                    victim.setStack(ProjectorBlockEntity.SLOT_MODULE,
                            new ItemStack(MffsItems.module(ProjectorModule.WALL).get()));
                    switchOn(victim);
                })
                .thenWaitUntil(() -> helper.assertTrue(victim.isBurnedOut(), "burnt out"))
                .thenExecute(() -> {
                    helper.assertFalse(victim.isActive(), "off");
                    helper.assertTrue(victim.getStack(ProjectorBlockEntity.SLOT_MODULE).isEmpty(), "module out");
                    helper.assertTrue(victim.getStack(ProjectorBlockEntity.SLOT_POWER_LINK).isEmpty(), "link out");
                    helper.assertItemEntityPresent(MffsItems.module(ProjectorModule.WALL).get(), victimPos, 2.0);
                    helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 2, 5));
                    ItemStack wrench = MffsItems.MULTITOOL_WRENCH.get().charged();
                    use(helper, player, wrench, victimPos, Direction.UP);
                    helper.assertFalse(victim.isBurnedOut(), "repaired");
                    helper.assertValueEqual(MffsItems.MULTITOOL_WRENCH.get().getAvailablePower(wrench), 990000,
                            "10 000 FE");
                    helper.assertTrue(jammer.isActive(), "jammer stays on");
                })
                .thenSucceed();
    }

    /**
     * Field fusion: a fused projector of the same capacitor removes the field blocks of the first one that lie in its
     * interior; outside the interior the first field stays.
     */
    @GameTest(template = LARGE, timeoutTicks = 400)
    public static void fieldFusionMergesRooms(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, CAPACITOR);
        ProjectorBlockEntity big = projector(helper, BIG_BOX, Direction.SOUTH);
        // Facing north from z 8: x 3–5, y 1–3, z 5–7 – its faces at z 5 and 6 lie in the big box's interior.
        ProjectorBlockEntity small = projector(helper, new BlockPos(4, 2, 8), Direction.NORTH);
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipBox(small, capacitor, 1, 1, ProjectorUpgrade.FIELD_FUSION);
                    switchOn(small);
                })
                .thenWaitUntil(() -> helper.assertTrue(isField(helper, new BlockPos(3, 1, 5)), "small field"))
                .thenExecute(() -> {
                    equipBox(big, capacitor, 2, 3, ProjectorUpgrade.FIELD_FUSION);
                    switchOn(big);
                })
                .thenWaitUntil(() -> helper.assertTrue(big.isActive(), "big on"))
                .thenExecute(() -> {
                    helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 1, 5));
                    helper.assertBlockPresent(Blocks.AIR, new BlockPos(5, 3, 6));
                    helper.assertTrue(isField(helper, new BlockPos(3, 1, 7)), "shared face stays");
                })
                .thenSucceed();
    }

    /**
     * Block breaker on a wall: the glowstone on the field positions goes; the chest next to the projector gets one
     * single dust per block (original error), the rest is lost.
     */
    @GameTest(template = LARGE, timeoutTicks = 300)
    public static void blockBreakerClearsWall(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, CAPACITOR);
        ProjectorBlockEntity projector = projector(helper, WALL_PROJECTOR, Direction.SOUTH);
        BlockPos chestPos = WALL_PROJECTOR.east();
        helper.setBlock(chestPos, Blocks.CHEST);
        WALL.forEach(pos -> helper.setBlock(pos, Blocks.GLOWSTONE));
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipWall(projector, capacitor, ProjectorUpgrade.BLOCK_BREAKER);
                    switchOn(projector);
                })
                .thenWaitUntil(() -> WALL.forEach(pos -> helper.assertTrue(isField(helper, pos), "field " + pos)))
                .thenExecute(() -> {
                    ChestBlockEntity chest = helper.getBlockEntity(chestPos);
                    int dust = 0;
                    for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                        ItemStack stack = chest.getItem(slot);
                        if (stack.is(Items.GLOWSTONE_DUST)) {
                            dust += stack.getCount();
                        }
                    }
                    helper.assertValueEqual(dust, 3, "one dust per block");
                })
                .thenSucceed();
    }

    /** NPC defence: a spider in the field box takes 10 (field defence) for 10 000 FE every 20 ticks. */
    @GameTest(template = LARGE, timeoutTicks = 300)
    public static void npcDefenseHurtsMonsters(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, CAPACITOR);
        ProjectorBlockEntity projector = projector(helper, BIG_BOX, Direction.SOUTH);
        Spider spider = helper.spawnWithNoFreeWill(EntityType.SPIDER, new BlockPos(4, 1, 5));
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipBox(projector, capacitor, 2, 3, ProjectorUpgrade.NPC_DEFENSE);
                    switchOn(projector);
                })
                .thenWaitUntil(() -> helper.assertTrue(spider.getHealth() < spider.getMaxHealth(), "hurt"))
                .thenExecute(() -> {
                    helper.assertValueEqual(spider.getHealth(), spider.getMaxHealth() - 10, "10 damage");
                    helper.assertTrue(spider.getLastDamageSource() != null
                            && spider.getLastDamageSource().is(CraftDamageTypes.FIELD_DEFENSE), "field defence");
                    helper.assertTrue(START - capacitor.getStorageAvailablePower() >= 10000, "10 000 FE");
                })
                .thenSucceed();
    }

    /**
     * Field teleporter: with access type 1 a player looking south through a wall one block thick lands behind it
     * for 10 000 FE; access type 0 refuses (no admin).
     */
    @GameTest(template = LARGE, timeoutTicks = 300)
    public static void fieldTeleporterCrossesWall(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = capacitor(helper, CAPACITOR);
        ProjectorBlockEntity projector = projector(helper, WALL_PROJECTOR, Direction.SOUTH);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos start = helper.absolutePos(new BlockPos(4, 1, 5));
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    equipWall(projector, capacitor);
                    switchOn(projector);
                })
                .thenWaitUntil(() -> helper.assertTrue(isField(helper, WALL.get(0)), "wall"))
                .thenExecute(() -> {
                    player.moveTo(start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 0.0F, 0.0F);
                    ItemStack tool = MffsItems.MULTITOOL_FIELD_TELEPORTER.get().charged();
                    use(helper, player, tool, WALL.get(0), Direction.NORTH);
                    helper.assertValueEqual(player.blockPosition(), start, "access type 0: no admin");
                    projector.setAccessType(1);
                    use(helper, player, tool, WALL.get(0), Direction.NORTH);
                    helper.assertValueEqual(player.blockPosition(), helper.absolutePos(new BlockPos(4, 1, 7)),
                            "behind the wall");
                    helper.assertValueEqual(MffsItems.MULTITOOL_FIELD_TELEPORTER.get().getAvailablePower(tool),
                            FieldTeleporterMultitoolItem.MAX_POWER - FieldTeleporterMultitoolItem.COST, "cost");
                })
                .thenSucceed();
    }
}
