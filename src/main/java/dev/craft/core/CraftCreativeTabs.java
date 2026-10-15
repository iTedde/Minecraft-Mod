package dev.craft.core;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.BatteryItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CraftCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Craft.MODID);

    public static final Supplier<CreativeModeTab> IC2 = TABS.register("ic2", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.craft.ic2"))
            .icon(() -> Ic2Items.cable(CableType.INSULATED_COPPER).toStack())
            .displayItems((parameters, output) -> {
                for (Ic2Machine machine : Ic2Machine.values()) {
                    output.accept(Ic2Items.machine(machine));
                }
                output.accept(Ic2Items.MACHINE_CASING);
                for (CableType type : CableType.values()) {
                    output.accept(Ic2Items.cable(type));
                }
                output.accept(Ic2Items.TIN_ORE);
                output.accept(Ic2Items.DEEPSLATE_TIN_ORE);
                output.accept(Ic2Items.TIN_BLOCK);
                output.accept(Ic2Items.BRONZE_BLOCK);
                output.accept(Ic2Items.RUBBER_WOOD);
                output.accept(Ic2Items.RUBBER_LEAVES);
                output.accept(Ic2Items.RUBBER_SAPLING);
                for (DeferredHolder<Item, ? extends Item> item : Ic2Items.ITEMS.getEntries()) {
                    Item value = item.get();
                    if (Ic2Items.simpleItems().contains(item) && value != Ic2Items.INDUSTRIAL_DIAMOND.get()) {
                        output.accept(value);
                    } else if (value instanceof BatteryItem) {
                        output.accept(new ItemStack(value));
                        output.accept(ElectricItems.charged(new ItemStack(value)));
                    }
                }
            })
            .build());

    public static final Supplier<CreativeModeTab> BUILDCRAFT = TABS.register("buildcraft", () -> CreativeModeTab
            .builder()
            .title(Component.translatable("itemGroup.craft.buildcraft"))
            .icon(() -> BcItems.engine(EngineType.REDSTONE).toStack())
            .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "ic2"))
            .displayItems((parameters, output) -> {
                for (EngineType type : EngineType.values()) {
                    output.accept(BcItems.engine(type));
                }
                output.accept(BcItems.LANDMARK);
                output.accept(BcItems.QUARRY);
                output.accept(BcItems.MINING_WELL);
                output.accept(BcItems.PUMP);
                output.accept(BcItems.TANK);
                output.accept(BcItems.REFINERY);
                for (DeferredHolder<Item, ? extends Item> item : BcItems.simpleItems()) {
                    output.accept(item.get());
                }
            })
            .build());

    private CraftCreativeTabs() {
    }
}
