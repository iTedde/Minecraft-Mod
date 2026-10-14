package dev.craft.core.explosion;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ExplosionGameTests {
    private ExplosionGameTests() {
    }

    /**
     * Power 2.5: dirt (absorption 1.85) next to the machine breaks, stone (3.5) and obsidian survive. Block rows are
     * filled on both sides because IC2 truncates coordinates, which shifts the blast by one block at negative
     * coordinates.
     */
    @GameTest(template = "empty_8x4x8", batch = "explosion_machine")
    public static void machineExplosionBreaksWeakBlocksOnly(GameTestHelper helper) {
        BlockPos machine = new BlockPos(4, 2, 4);
        helper.setBlock(machine, Blocks.FURNACE);
        for (int d = -1; d <= 1; d++) {
            helper.setBlock(machine.east().south(d), Blocks.DIRT);
            helper.setBlock(machine.west().south(d), Blocks.STONE);
        }
        Ic2Explosion.explodeMachineAt(helper.getLevel(), helper.absolutePos(machine));
        helper.assertBlockPresent(Blocks.AIR, machine);
        boolean dirtBroken = false;
        for (int d = -1; d <= 1; d++) {
            dirtBroken |= helper.getBlockState(machine.east().south(d)).isAir();
            helper.assertBlockPresent(Blocks.STONE, machine.west().south(d));
        }
        helper.assertTrue(dirtBroken, "dirt next to the machine must break");
        helper.succeed();
    }

    /** Obsidian counts as resistance 36 in IC2 explosions (absorption 12): a power 20 blast breaks it. */
    @GameTest(template = "empty_8x4x8", batch = "explosion_strong")
    public static void strongExplosionBreaksObsidian(GameTestHelper helper) {
        BlockPos center = new BlockPos(4, 2, 4);
        helper.setBlock(center.east(), Blocks.OBSIDIAN);
        BlockPos abs = helper.absolutePos(center);
        new Ic2Explosion(helper.getLevel(), abs.getX() + 0.5, abs.getY() + 0.5, abs.getZ() + 0.5,
                20.0F, 1.0F, 1.0F).explode();
        helper.assertBlockPresent(Blocks.AIR, center.east());
        helper.succeed();
    }
}
