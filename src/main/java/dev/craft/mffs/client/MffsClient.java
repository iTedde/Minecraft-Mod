package dev.craft.mffs.client;

import dev.craft.Craft;
import dev.craft.mffs.MffsBlocks;
import dev.craft.mffs.MffsItems;
import dev.craft.mffs.MffsMenus;
import dev.craft.mffs.block.ForceFieldBlock;
import dev.craft.mffs.block.ForceFieldBlockEntity;
import dev.craft.mffs.field.ForceFieldType;
import dev.craft.mffs.item.ForceEnergyCrystalItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class MffsClient {
    /** Fill step 0–4 of the force energy crystal (icons 112–116 of {@code items.png}). */
    public static final ResourceLocation FILL = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "fill");

    private MffsClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MffsMenus.EXTRACTOR.get(), ExtractorScreen::new);
        event.register(MffsMenus.CAPACITOR.get(), CapacitorScreen::new);
        event.register(MffsMenus.CONVERTER.get(), ConverterScreen::new);
        event.register(MffsMenus.PROJECTOR.get(), ProjectorScreen::new);
        event.register(MffsMenus.SECURITY_STATION.get(), SecurityStationScreen::new);
        event.register(MffsMenus.SECURITY_STORAGE.get(), SecurityStorageScreen::new);
        event.register(MffsMenus.CONTROL_SYSTEM.get(), ControlSystemScreen::new);
        event.register(MffsMenus.DEFENSE_STATION.get(), DefenseStationScreen::new);
    }

    /** Camouflage force fields draw the look their block entity names ({@link CamouflageFieldModel}). */
    @SubscribeEvent
    public static void wrapCamouflageModel(ModelEvent.ModifyBakingResult event) {
        for (var state : MffsBlocks.FORCE_FIELD.get().getStateDefinition().getPossibleStates()) {
            if (state.getValue(ForceFieldBlock.TYPE) == ForceFieldType.CAMOUFLAGE) {
                event.getModels().computeIfPresent(BlockModelShaper.stateToModelLocation(state),
                        (location, model) -> new CamouflageFieldModel(model));
            }
        }
    }

    /** Tints of the camouflage look (grass, leaves, water …) come from the camouflage block's own colour handler. */
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        var colors = event.getBlockColors();
        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null && level.getBlockEntity(pos) instanceof ForceFieldBlockEntity field
                    && field.getCamouflage() != null) {
                return colors.getColor(field.getCamouflage(), level, pos, tintIndex);
            }
            return -1;
        }, MffsBlocks.FORCE_FIELD.get());
    }

    /** {@code BlockForceField} has an empty selection box: no outline on force fields. */
    @SubscribeEvent
    public static void hideFieldOutline(RenderHighlightEvent.Block event) {
        var level = Minecraft.getInstance().level;
        if (level != null && level.getBlockState(event.getTarget().getBlockPos()).is(MffsBlocks.FORCE_FIELD.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(MffsItems.FORCE_ENERGY_CRYSTAL.get(), FILL,
                (stack, level, entity, seed) -> ForceEnergyCrystalItem.fillStep(stack) / 4.0F));
    }
}
