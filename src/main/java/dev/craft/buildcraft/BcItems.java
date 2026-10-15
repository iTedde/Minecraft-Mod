package dev.craft.buildcraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.fluid.BcFluids;
import dev.craft.buildcraft.item.BcWrenchItem;
import dev.craft.buildcraft.item.FuelBucketItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BcItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    private static final Map<EngineType, DeferredItem<BlockItem>> ENGINES = new EnumMap<>(EngineType.class);
    /** Items with a plain generated item model (one texture named like the item). */
    private static final List<DeferredItem<? extends Item>> SIMPLE = new ArrayList<>();

    static {
        for (EngineType type : EngineType.values()) {
            ENGINES.put(type, ITEMS.registerSimpleBlockItem(BcBlocks.engine(type)));
        }
    }

    public static final DeferredItem<Item> WOODEN_GEAR = simple("wooden_gear");
    public static final DeferredItem<Item> STONE_GEAR = simple("stone_gear");
    public static final DeferredItem<Item> IRON_GEAR = simple("iron_gear");
    public static final DeferredItem<Item> GOLD_GEAR = simple("gold_gear");
    public static final DeferredItem<Item> DIAMOND_GEAR = simple("diamond_gear");
    public static final DeferredItem<BcWrenchItem> WRENCH = register(ITEMS.registerItem("bc_wrench",
            BcWrenchItem::new));
    /** {@code ItemBucketOil}: places oil, stack size 1 like every 1.4.7 bucket. */
    public static final DeferredItem<BucketItem> OIL_BUCKET = register(ITEMS.registerItem("oil_bucket",
            properties -> new BucketItem(BcFluids.OIL.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1))));
    public static final DeferredItem<FuelBucketItem> FUEL_BUCKET = register(ITEMS.registerItem("fuel_bucket",
            properties -> new FuelBucketItem(BcFluids.FUEL.get(),
                    properties.craftRemainder(Items.BUCKET).stacksTo(1))));

    private BcItems() {
    }

    private static DeferredItem<Item> simple(String name) {
        return register(ITEMS.registerSimpleItem(name));
    }

    private static <T extends Item> DeferredItem<T> register(DeferredItem<T> item) {
        SIMPLE.add(item);
        return item;
    }

    public static DeferredItem<BlockItem> engine(EngineType type) {
        return ENGINES.get(type);
    }

    public static List<DeferredItem<? extends Item>> simpleItems() {
        return Collections.unmodifiableList(SIMPLE);
    }
}
