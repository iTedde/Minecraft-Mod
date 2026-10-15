package dev.craft.ic2.item.tool;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Mining drill and diamond drill (IC2 {@code ItemElectricToolDrill}/{@code ItemElectricToolDDrill}). Their mining
 * sounds ({@code drill}, {@code drillOne} …) do not exist in the pack, so the drills are silent as there.
 */
public class DrillItem extends ElectricToolItem {
    /** Stone/iron material blocks and the drill's block list (obsidian is stone material, so both drills share it). */
    public static final TagKey<Block> MINEABLE = mineableTag("mining_drill");

    public DrillItem(Properties properties, int operationEnergyCost, float efficiency) {
        super(properties, 10000, 100, 1, operationEnergyCost, efficiency, MINEABLE);
    }
}
