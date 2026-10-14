package dev.craft.ic2;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.ic2.cable.CableType;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Items {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    private static final Map<CableType, DeferredItem<BlockItem>> CABLES = new EnumMap<>(CableType.class);

    static {
        for (CableType type : CableType.values()) {
            CABLES.put(type, ITEMS.registerSimpleBlockItem(Ic2Blocks.cable(type)));
        }
    }

    private Ic2Items() {
    }

    public static DeferredItem<BlockItem> cable(CableType type) {
        return CABLES.get(type);
    }
}
