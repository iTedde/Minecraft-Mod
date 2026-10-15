package dev.craft.powerconverters;

import dev.craft.Craft;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PcItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<BlockItem> ENERGY_BRIDGE = ITEMS.registerSimpleBlockItem(PcBlocks.ENERGY_BRIDGE);
    public static final DeferredItem<BlockItem> UNIVERSAL_CHARGER =
            ITEMS.registerSimpleBlockItem(PcBlocks.UNIVERSAL_CHARGER);
    private static final Map<ConverterType, DeferredItem<BlockItem>> CONVERTERS = new EnumMap<>(ConverterType.class);

    static {
        for (ConverterType type : ConverterType.values()) {
            CONVERTERS.put(type, ITEMS.registerSimpleBlockItem(PcBlocks.converter(type)));
        }
    }

    private PcItems() {
    }

    public static DeferredItem<BlockItem> converter(ConverterType type) {
        return CONVERTERS.get(type);
    }
}
