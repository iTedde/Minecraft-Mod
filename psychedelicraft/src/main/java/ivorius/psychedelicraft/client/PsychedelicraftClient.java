package ivorius.psychedelicraft.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.item.PSModelPredicates;
import ivorius.psychedelicraft.client.particle.PSParticleFactories;
import ivorius.psychedelicraft.client.render.*;
import ivorius.psychedelicraft.client.render.shader.PSShaders;
import ivorius.psychedelicraft.client.render.shader.ShaderLoader;
import ivorius.psychedelicraft.client.screen.PSScreens;
import ivorius.psychedelicraft.client.screen.SettingsScreen;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemDrugs;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.ItemFluidsMixture;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Client entry point (upstream: Fabric {@code ClientModInitializer}), called from the mod constructor on the client.
 */
public final class PsychedelicraftClient {
    private static final Supplier<PSClientConfig> CONFIG = Suppliers.memoize(() -> {
        return new PSClientConfig(FMLPaths.CONFIGDIR.get().resolve("psychedelicraft_client.json"));
    });

    private PsychedelicraftClient() {
    }

    public static PSClientConfig getConfig() {
        return CONFIG.get();
    }

    public static void init(IEventBus modBus, ModContainer container) {
        try {
            getConfig().load();
        } catch (Throwable t) {}
        Psychedelicraft.globalDrugProperties = () -> DrugProperties.of((Entity)Minecraft.getInstance().player);
        Psychedelicraft.crossHairTarget = () -> Optional.ofNullable(Minecraft.getInstance().hitResult);

        container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> new SettingsScreen(parent));

        modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(() -> {
            PSRenderers.setup();
            PSModelPredicates.bootstrap();
        }));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, PSRenderers::registerRenderers);
        modBus.addListener(RegisterClientExtensionsEvent.class, PSRenderers::registerClientExtensions);
        modBus.addListener(RegisterColorHandlersEvent.Item.class, PSModelPredicates::registerColors);
        modBus.addListener(RegisterParticleProvidersEvent.class, PSParticleFactories::bootstrap);
        modBus.addListener(RegisterMenuScreensEvent.class, PSScreens::bootstrap);
        modBus.addListener(RegisterShadersEvent.class, PSShaders::registerShaders);
        modBus.addListener(ModelEvent.RegisterAdditional.class, PlacedDrinksModelProvider.INSTANCE::registerModels);
        modBus.addListener(RegisterClientReloadListenersEvent.class, event -> event.registerReloadListener(ShaderLoader.POST_EFFECTS));

        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, event -> {
            Minecraft client = Minecraft.getInstance();
            DrugProperties.of((Entity)client.player).ifPresent(properties -> {
                DrugRenderer.INSTANCE.update(properties, client.player);
                SmoothCameraHelper.INSTANCE.tick(properties);
            });
        });

        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, event -> {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
                return;
            }
            Minecraft client = Minecraft.getInstance();
            DrugProperties.of((Entity)client.player).ifPresent(properties -> {
                var consumers = client.renderBuffers().bufferSource();
                DrugRenderer.INSTANCE.renderAllHallucinations(event.getPoseStack(), consumers, event.getCamera(),
                        event.getPartialTick().getGameTimeDeltaPartialTick(false), properties);
                consumers.endBatch();
            });
        });

        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, event -> {
            var stack = event.getItemStack();
            var context = event.getContext();
            var type = event.getFlags();
            var lines = event.getToolTip();

            List<Component> tooltip = new ArrayList<>();

            Processable.ProcessType.appendTooltip(stack, context, tooltip, type);

            if (FluidCapacity.get(stack) > 0) {
                Consumer<Component> consumer = tooltip::add;
                FluidCapacity.appendTooltip(stack, context, consumer, type);
                ItemFluids.of(stack).addToTooltip(context, consumer, type);
                ItemFluidsMixture.of(stack).addToTooltip(context, consumer, type);
            }

            ItemDrugs.get(stack).addToTooltip(context, tooltip::add, type);
            Impurities.get(stack).addToTooltip(context, tooltip::add, type);

            if (!lines.isEmpty()) {
                lines.addAll(1, tooltip);
            } else {
                lines.addAll(tooltip);
            }
        });
    }
}
