package dev.craft.advancedmachines;

import dev.craft.Craft;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Items of Advanced Machines (spec 25 §1); the machine block items live with the other IC2 machines. */
public final class AdvancedMachinesItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    /** {@code ItemDust} "refinedIronDust": smelts into refined iron. */
    public static final DeferredItem<Item> REFINED_IRON_DUST = ITEMS.registerSimpleItem("refined_iron_dust");

    private AdvancedMachinesItems() {
    }
}
