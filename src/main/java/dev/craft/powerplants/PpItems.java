package dev.craft.powerplants;

import dev.craft.Craft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PpItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<Item> COKE = ITEMS.registerSimpleItem("coke");
    public static final DeferredItem<BlockItem> COKE_BLOCK = ITEMS.registerSimpleBlockItem(PpBlocks.COKE_BLOCK);
    public static final DeferredItem<BlockItem> COKE_OVEN_BRICKS =
            ITEMS.registerSimpleBlockItem(PpBlocks.COKE_OVEN_BRICKS);
    public static final DeferredItem<BlockItem> COKE_OVEN = ITEMS.registerSimpleBlockItem(PpBlocks.COKE_OVEN);
    public static final DeferredItem<BlockItem> BOILER_CASING = ITEMS.registerSimpleBlockItem(PpBlocks.BOILER_CASING);
    public static final DeferredItem<BlockItem> BOILER_FIREBOX =
            ITEMS.registerSimpleBlockItem(PpBlocks.BOILER_FIREBOX);
    public static final DeferredItem<BlockItem> TURBINE_CASING =
            ITEMS.registerSimpleBlockItem(PpBlocks.TURBINE_CASING);
    public static final DeferredItem<BlockItem> TURBINE_ROTOR = ITEMS.registerSimpleBlockItem(PpBlocks.TURBINE_ROTOR);
    public static final DeferredItem<BlockItem> STEAM_TURBINE = ITEMS.registerSimpleBlockItem(PpBlocks.STEAM_TURBINE);

    private PpItems() {
    }
}
