package dev.craft.ic2;

import dev.craft.ic2.reactor.ReactorChamberBlockEntity;
import dev.craft.ic2.generator.GeothermalGeneratorBlockEntity;
import dev.craft.ic2.item.ScrapBoxItem;
import dev.craft.ic2.item.armor.ArmorEvents;
import dev.craft.ic2.item.armor.Ic2ArmorMaterials;
import dev.craft.ic2.item.tool.ElectricToolItem;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import dev.craft.ic2.machine.MachineBlockEntity;
import dev.craft.ic2.world.Ic2Features;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/** Entry point of the IndustrialCraft 2 part. */
public final class Ic2 {
    private Ic2() {
    }

    public static void init(IEventBus modBus) {
        Ic2Blocks.BLOCKS.register(modBus);
        Ic2Items.ITEMS.register(modBus);
        Ic2BlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        Ic2DataComponents.COMPONENTS.register(modBus);
        Ic2Recipes.TYPES.register(modBus);
        Ic2Recipes.SERIALIZERS.register(modBus);
        Ic2Menus.MENUS.register(modBus);
        Ic2Sounds.SOUNDS.register(modBus);
        Ic2Features.FEATURES.register(modBus);
        Ic2EntityTypes.ENTITY_TYPES.register(modBus);
        Ic2Keyboard.ATTACHMENT_TYPES.register(modBus);
        Ic2ArmorMaterials.MATERIALS.register(modBus);
        Ic2MobEffects.EFFECTS.register(modBus);
        ArmorEvents.register(NeoForge.EVENT_BUS);
        modBus.addListener(Ic2Keyboard::registerPayloads);
        NeoForge.EVENT_BUS.addListener(ElectricToolItem::onBlockBreak);
        modBus.addListener(Ic2::registerCapabilities);
        modBus.addListener(Ic2::commonSetup);
        modBus.addListener(Ic2::modifyDefaultComponents);
    }

    private static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modify(Ic2Items.FILLED_TIN_CAN.get(), builder -> builder.set(Ic2DataComponents.CAN_META.get(), 0));
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ScrapBoxItem.registerDispenserBehavior(Ic2Items.SCRAP_BOX.get());
            DispenserBlock.registerProjectileBehavior(Ic2Items.DYNAMITE.get());
            DispenserBlock.registerProjectileBehavior(Ic2Items.STICKY_DYNAMITE.get());
        });
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Ic2Machine machine : Ic2Machine.values()) {
            if (!machine.hasMenu()) {
                continue;
            }
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Ic2BlockEntityTypes.get(machine),
                    MachineBlockEntity::getItemHandler);
        }
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Ic2BlockEntityTypes.REACTOR_CHAMBER.get(),
                ReactorChamberBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                Ic2BlockEntityTypes.get(Ic2Machine.GEOTHERMAL_GENERATOR),
                (be, side) -> ((GeothermalGeneratorBlockEntity) be).getFluidHandler());
    }
}
