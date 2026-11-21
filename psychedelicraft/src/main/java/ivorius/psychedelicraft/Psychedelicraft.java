/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.command.*;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entity.PSEntities;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.effect.PSEffects;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.container.VariantMarshal;
import ivorius.psychedelicraft.item.PSItemGroups;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.network.Channel;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.recipe.PSRecipes;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import ivorius.psychedelicraft.world.gen.PSWorldGen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.world.phys.HitResult;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Suppliers;

@Mod(Psychedelicraft.DEFAULT_NAMESPACE)
public class Psychedelicraft {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String DEFAULT_NAMESPACE = "psychedelicraft";
    public static final String VANILLA_EXTENSIONS_NAMESPACE = DEFAULT_NAMESPACE + "mc";

    private static final Supplier<PSConfig> CONFIG = Suppliers.memoize(() -> {
        var config = new PSConfig(FMLPaths.CONFIGDIR.get().resolve(DEFAULT_NAMESPACE + ".json"));
        try {
            config.load();
        } catch (Throwable t) {}
        return config;
    });

    public static Supplier<Optional<DrugProperties>> globalDrugProperties = Optional::empty;
    public static Supplier<Optional<HitResult>> crossHairTarget = Optional::empty;

    public static Optional<DrugProperties> getGlobalDrugProperties() {
        return globalDrugProperties.get();
    }

    public static Optional<HitResult> getCrossHairTarget() {
        return crossHairTarget.get();
    }

    public static PSConfig getConfig() {
        return CONFIG.get();
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(DEFAULT_NAMESPACE, name);
    }

    private static volatile boolean bootstrapped;

    /**
     * True once the registry bootstrap ran. Client code reachable before mod loading (e.g. the window resize in the
     * Minecraft constructor) must not touch classes like DrugRenderer before that, or their static initialisers try
     * to register into frozen registries.
     */
    public static boolean isBootstrapped() {
        return bootstrapped;
    }

    public Psychedelicraft(IEventBus modBus, ModContainer container) {
        modBus.addListener(PSRegistries::register);
        // Upstream registers everything from static initialisers. NeoForge only unfreezes the registries while
        // RegisterEvent is fired, so the whole bootstrap runs in the RegisterEvent of Psychedelicraft's own fluid
        // registry: all vanilla and NeoForge registries (attributes, fluids of other mods ...) are filled by then and
        // every registry is still unfrozen.
        modBus.addListener(EventPriority.LOWEST, RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(PSRegistries.FLUIDS_KEY)) {
                bootstrap();
            }
        });
        modBus.addListener(VariantMarshal::registerCapabilities);
        modBus.addListener(Channel::register);
        PSNeoForgeEvents.register(modBus, NeoForge.EVENT_BUS);

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event -> {
            DrugProperties.of(event.getEntity()).sendCapabilities();
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.Clone.class, event -> {
            DrugProperties.of(event.getEntity()).copyFrom(DrugProperties.of(event.getOriginal()), !event.isWasDeath());
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            DrugProperties.of(event.getEntity()).sendCapabilities();
        });

        if (FMLEnvironment.dist.isClient()) {
            ivorius.psychedelicraft.client.PsychedelicraftClient.init(modBus, container);
        }
    }

    private static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        PSBlocks.bootstrap();
        PSItems.bootstrap();
        PSTags.bootstrap();
        PSItemGroups.bootstrap();
        PSFluids.bootstrap();
        PSRecipes.bootstrap();
        PSEntities.bootstrap();
        PSEffects.bootstrap();
        PSWorldGen.bootstrap();
        PSGameRules.bootstrap();
        PSCommands.bootstrap();
        PSSounds.bootstrap();
        PSScreenHandlers.bootstrap();
        Channel.bootstrap();
        PSCriteria.bootstrap();
        PSParticles.bootstrap();
        PSDamageTypes.bootstrap();
        VariantMarshal.bootstrap();
    }
}