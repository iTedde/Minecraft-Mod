package dev.craft.ic2.client;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.item.BatteryItem;
import dev.craft.ic2.machine.MachineSoundHooks;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.FoliageColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class Ic2Client {
    /** Item property for the battery icons: charge step 0–4 divided by 4. */
    public static final ResourceLocation CHARGE_PROPERTY = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "charge");

    private Ic2Client() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        MachineSoundHooks.setHandler(new MachineSoundHandler());
        event.enqueueWork(() -> {
            for (var battery : new BatteryItem[]{Ic2Items.RE_BATTERY.get(), Ic2Items.ENERGY_CRYSTAL.get(),
                    Ic2Items.LAPOTRON_CRYSTAL.get()}) {
                ItemProperties.register(battery, CHARGE_PROPERTY,
                        (stack, level, entity, seed) -> battery.getIconLevel(stack) / 4.0F);
            }
        });
    }

    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(CableModel.LOADER_ID, CableModel.LOADER);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        for (Ic2Machine machine : Ic2Machine.values()) {
            switch (machine) {
                case GENERATOR, GEOTHERMAL_GENERATOR, WATER_MILL, SOLAR_PANEL, WIND_MILL ->
                        event.register(Ic2Menus.get(machine), GeneratorScreen::new);
                case IRON_FURNACE -> event.register(Ic2Menus.get(machine), IronFurnaceScreen::new);
                case ELECTRIC_FURNACE, MACERATOR, EXTRACTOR, COMPRESSOR, RECYCLER ->
                        event.register(Ic2Menus.get(machine), ElectricMachineScreen::new);
                case BATBOX -> event.register(Ic2Menus.get(machine), ElectricStorageScreen::new);
            }
        }
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> FoliageColor.getBirchColor(), Ic2Blocks.RUBBER_LEAVES.get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> FoliageColor.getBirchColor(), Ic2Items.RUBBER_LEAVES.get());
    }
}
