package dev.craft.ic2.client;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2EntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.item.BatteryItem;
import dev.craft.ic2.item.tool.NanoSaberItem;
import dev.craft.ic2.machine.MachineSoundHooks;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.FoliageColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class Ic2Client {
    /** Item property for the battery icons: charge step 0–4 divided by 4. */
    public static final ResourceLocation CHARGE_PROPERTY = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "charge");
    /** Item property of the nano saber: 1 when switched on. */
    public static final ResourceLocation ACTIVE_PROPERTY = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "active");

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
            ItemProperties.register(Ic2Items.NANO_SABER.get(), ACTIVE_PROPERTY,
                    (stack, level, entity, seed) -> NanoSaberItem.isActive(stack) ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        Ic2KeyMappings.register(event);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Ic2EntityTypes.MINING_LASER.get(), MiningLaserRenderer::new);
        event.registerEntityRenderer(Ic2EntityTypes.INDUSTRIAL_TNT.get(), Ic2ExplosiveRenderer::new);
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
                case BATBOX, MFE, MFSU -> event.register(Ic2Menus.get(machine), ElectricStorageScreen::new);
                case MASS_FABRICATOR -> event.register(Ic2Menus.get(machine), MassFabricatorScreen::new);
                case ELECTROLYZER -> event.register(Ic2Menus.get(machine), ElectrolyzerScreen::new);
                case INDUCTION_FURNACE -> event.register(Ic2Menus.get(machine), InductionFurnaceScreen::new);
                case CANNING_MACHINE -> event.register(Ic2Menus.get(machine), CanningMachineScreen::new);
                case NUCLEAR_REACTOR -> event.register(Ic2Menus.get(machine), NuclearReactorScreen::new);
                case LV_TRANSFORMER, MV_TRANSFORMER, HV_TRANSFORMER, MAGNETIZER -> {
                }
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
