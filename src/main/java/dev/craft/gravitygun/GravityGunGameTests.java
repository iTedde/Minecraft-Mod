package dev.craft.gravitygun;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 50.3 (spec 50 §3): Gravity Gun. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GravityGunGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private GravityGunGameTests() {
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    /** Shooter at (1.5, 1, 3.5) looking east (+x) down at {@code target}, with a gun in the main hand. */
    private static Player shooter(GameTestHelper helper, boolean charged, Vec3 target) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(1.5, 1.0, 3.5)), -90.0F, 0.0F);
        Vec3 eye = player.getEyePosition();
        Vec3 aim = helper.absoluteVec(target);
        player.setXRot((float) Math.toDegrees(Math.atan2(eye.y - aim.y, aim.x - eye.x)));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(charged
                ? GravityGun.CHARGED_GRAVITY_GUN.get() : GravityGun.GRAVITY_GUN.get()));
        return player;
    }

    @GameTest(template = EMPTY)
    public static void grabRules(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, Blocks.STONE);
        helper.assertTrue(GravityGunHandler.canGrab(level, helper.absolutePos(pos), level.getBlockState(
                helper.absolutePos(pos))), "stone");
        helper.setBlock(pos, Blocks.BEDROCK);
        helper.assertFalse(GravityGunHandler.canGrab(level, helper.absolutePos(pos), level.getBlockState(
                helper.absolutePos(pos))), "bedrock");
        helper.setBlock(pos, Blocks.CHEST);
        helper.assertTrue(GravityGunHandler.canGrab(level, helper.absolutePos(pos), level.getBlockState(
                helper.absolutePos(pos))), "chest");
        helper.setBlock(pos, Blocks.TORCH);
        helper.assertFalse(GravityGunHandler.canGrab(level, helper.absolutePos(pos), level.getBlockState(
                helper.absolutePos(pos))), "torch");
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(5, 1, 5));
        helper.assertTrue(GravityGunHandler.canGrab(pig), "pig");
        pig.discard();
        helper.succeed();
    }

    /** A grabbed chest keeps its contents, falls once released and sets itself down again. */
    @GameTest(template = EMPTY)
    public static void grabbedChestLandsWithContents(GameTestHelper helper) {
        floor(helper);
        BlockPos pos = new BlockPos(3, 3, 3);
        helper.setBlock(pos, Blocks.CHEST);
        ((ChestBlockEntity) helper.getBlockEntity(pos)).setItem(0, new ItemStack(Items.DIAMOND, 7));
        GrabbedBlock block = GrabbedBlock.lift(helper.getLevel(), helper.absolutePos(pos));
        helper.getLevel().addFreshEntity(block);
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.succeedWhen(() -> {
            BlockPos landed = new BlockPos(3, 1, 3);
            helper.assertBlockPresent(Blocks.CHEST, landed);
            ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(landed);
            helper.assertTrue(chest.getItem(0).is(Items.DIAMOND) && chest.getItem(0).getCount() == 7, "contents");
        });
    }

    /** Use key at a pig within 4 blocks grabs it and holds it ahead; pressing again drops it. */
    @GameTest(template = EMPTY)
    public static void grabHoldAndDrop(GameTestHelper helper) {
        floor(helper);
        Player player = shooter(helper, false, new Vec3(4.0, 1.45, 3.5));
        Pig pig = helper.spawn(EntityType.PIG, new Vec3(4.0, 1.0, 3.5));
        pig.setNoAi(true);
        GravityGunHandler.useKey(player);
        helper.assertTrue(GravityGunHandler.grabbed(player) == pig, "pig grabbed");
        helper.runAfterDelay(10, () -> {
            Vec3 point = player.getEyePosition().add(player.getLookAngle().scale(GravityGunHandler.HOLD_DISTANCE));
            helper.assertTrue(pig.getBoundingBox().getCenter().distanceTo(point) < 1.5, "held 3.5 ahead");
            GravityGunHandler.useKey(player);
            helper.assertTrue(GravityGunHandler.grabbed(player) == null, "dropped");
            pig.discard();
            helper.succeed();
        });
    }

    /** Attack punts a pig in range away from the shooter. */
    @GameTest(template = EMPTY)
    public static void throwPuntsTarget(GameTestHelper helper) {
        floor(helper);
        Player player = shooter(helper, true, new Vec3(5.0, 1.45, 3.5));
        Pig pig = helper.spawn(EntityType.PIG, new Vec3(5.0, 1.0, 3.5));
        pig.setNoAi(true);
        GravityGunHandler.tryThrow(player);
        helper.assertTrue(pig.getDeltaMovement().x > 0.3, "pushed east, got " + pig.getDeltaMovement());
        pig.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void lightningChargesGun(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(5, new ItemStack(GravityGun.GRAVITY_GUN.get()));
        GravityGunHandler.charge(player);
        helper.assertTrue(player.getInventory().getItem(5).is(GravityGun.CHARGED_GRAVITY_GUN.get()), "charged");
        helper.succeed();
    }
}
