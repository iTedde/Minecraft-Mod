package dev.craft.gravisuite.item;

import dev.craft.ic2.item.tool.DrillItem;
import dev.craft.ic2.item.tool.ElectricToolItem;

/**
 * GraviSuite {@code ItemAdvDDrill} (spec 28 §9.1): the diamond drill's block list with 15 000 EU, transfer 500,
 * tier 2, efficiency 35 and 160 EU per block; hits are free, no sounds.
 */
public class AdvancedDrillItem extends ElectricToolItem {
    public static final int OPERATION_COST = 160;

    public AdvancedDrillItem(Properties properties) {
        super(properties, 15000, 500, 2, OPERATION_COST, 35.0F, DrillItem.MINEABLE);
    }
}
