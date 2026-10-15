package dev.craft.compactsolars;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CsItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    private static final Map<SolarArrayType, DeferredItem<BlockItem>> ARRAYS = new EnumMap<>(SolarArrayType.class);

    static {
        for (SolarArrayType type : SolarArrayType.values()) {
            ARRAYS.put(type, ITEMS.registerSimpleBlockItem(CsBlocks.array(type)));
        }
    }

    private CsItems() {
    }

    public static DeferredItem<BlockItem> array(SolarArrayType type) {
        return ARRAYS.get(type);
    }
}
