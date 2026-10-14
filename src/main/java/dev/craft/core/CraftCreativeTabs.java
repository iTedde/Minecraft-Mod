package dev.craft.core;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CraftCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Craft.MODID);

    public static final Supplier<CreativeModeTab> IC2 = TABS.register("ic2", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.craft.ic2"))
            .icon(() -> Ic2Items.cable(CableType.INSULATED_COPPER).toStack())
            .displayItems((parameters, output) -> {
                for (CableType type : CableType.values()) {
                    output.accept(Ic2Items.cable(type));
                }
            })
            .build());

    private CraftCreativeTabs() {
    }
}
