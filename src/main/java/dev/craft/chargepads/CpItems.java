package dev.craft.chargepads;

import dev.craft.Craft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** ChargePads items (spec 47 §1); pads stack to 4, the lapotronic pad is uncommon. */
public final class CpItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<BlockItem> STATIC_CHARGE_PAD = ITEMS.registerSimpleBlockItem(
            CpBlocks.STATIC_CHARGE_PAD, new Item.Properties().stacksTo(4));
    public static final DeferredItem<BlockItem> CRYSTALIZOR_CHARGE_PAD = ITEMS.registerSimpleBlockItem(
            CpBlocks.CRYSTALIZOR_CHARGE_PAD, new Item.Properties().stacksTo(4));
    public static final DeferredItem<BlockItem> LAPOTRONIC_CHARGE_PAD = ITEMS.registerSimpleBlockItem(
            CpBlocks.LAPOTRONIC_CHARGE_PAD, new Item.Properties().stacksTo(4).rarity(Rarity.UNCOMMON));

    private CpItems() {
    }
}
