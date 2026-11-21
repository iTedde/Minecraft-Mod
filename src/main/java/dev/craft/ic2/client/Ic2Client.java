package dev.craft.ic2.client;

import dev.craft.Craft;
import dev.craft.advancedmachines.AdvancedMachineMenu;
import dev.craft.advancedmachines.client.AdvancedMachineScreen;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2EntityTypes;
import dev.craft.ic2.Ic2Fluids;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.brewing.BoozeMugItem;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.item.BatteryItem;
import dev.craft.ic2.item.ToolboxItem;
import dev.craft.ic2.item.tool.NanoSaberItem;
import dev.craft.ic2.machine.MachineSoundHooks;
import dev.craft.ic2.personal.PersonalSafeBlockEntity;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
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
    /** Item property of the toolbox: 1 when packed. */
    public static final ResourceLocation FULL_PROPERTY = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "full");
    /** Item property of the booze mug: icon offset 0–8 ({@code BoozeMugItem.getIconOffset}) divided by 8. */
    public static final ResourceLocation BOOZE_PROPERTY = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "booze");

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
            ItemProperties.register(Ic2Items.TOOLBOX.get(), FULL_PROPERTY,
                    (stack, level, entity, seed) -> ToolboxItem.isFull(stack) ? 1.0F : 0.0F);
            ItemProperties.register(Ic2Items.BOOZE_MUG.get(), BOOZE_PROPERTY,
                    (stack, level, entity, seed) -> BoozeMugItem.getIconOffset(stack) / 8.0F);
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
        event.registerEntityRenderer(Ic2EntityTypes.DYNAMITE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(Ic2EntityTypes.STICKY_DYNAMITE.get(), ThrownItemRenderer::new);
        event.registerBlockEntityRenderer(personalSafeType(), PersonalSafeRenderer::new);
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<PersonalSafeBlockEntity> personalSafeType() {
        return (BlockEntityType<PersonalSafeBlockEntity>) (BlockEntityType<?>)
                Ic2BlockEntityTypes.get(Ic2Machine.PERSONAL_SAFE);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new PersonalSafeRenderer.ItemRenderer();
                }
                return renderer;
            }
        }, Ic2Items.machine(Ic2Machine.PERSONAL_SAFE).get());
        // IC2 draws liquids with their item icon: coolant = item_0.png 202.
        ResourceLocation coolant = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/coolant_still");
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return coolant;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return coolant;
            }
        }, Ic2Fluids.COOLANT_TYPE.get());
    }

    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(CableModel.LOADER_ID, CableModel.LOADER);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Ic2Menus.CROPNALYZER.get(), CropnalyzerScreen::new);
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
                case PUMP -> event.register(Ic2Menus.get(machine), PumpScreen::new);
                case MINER -> event.register(Ic2Menus.get(machine), MinerScreen::new);
                case CROP_MATRON -> event.register(Ic2Menus.get(machine), CropMatronScreen::new);
                case PERSONAL_SAFE -> event.register(Ic2Menus.get(machine), PersonalScreens.Safe::new);
                case TRADE_O_MAT -> event.register(Ic2Menus.get(machine), PersonalScreens.TradeOMat::new);
                case ENERGY_O_MAT -> event.register(Ic2Menus.get(machine), PersonalScreens.EnergyOMat::new);
                case NUCLEAR_REACTOR -> event.register(Ic2Menus.get(machine), NuclearReactorScreen::new);
                case ROTARY_MACERATOR, SINGULARITY_COMPRESSOR, CENTRIFUGE_EXTRACTOR ->
                        event.register(Ic2Menus.<AdvancedMachineMenu>get(machine), AdvancedMachineScreen::new);
                case LV_TRANSFORMER, MV_TRANSFORMER, HV_TRANSFORMER, MAGNETIZER, TELEPORTER, TESLA_COIL, TERRAFORMER -> {
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
