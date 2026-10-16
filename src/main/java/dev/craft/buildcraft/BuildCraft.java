package dev.craft.buildcraft;

import dev.craft.buildcraft.engine.CombustionEngineBlockEntity;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.factory.QuarryChunkLoading;
import dev.craft.buildcraft.factory.RefineryRecipe;
import dev.craft.buildcraft.fluid.BcFluids;
import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.transport.PipeItemPayload;
import dev.craft.buildcraft.world.BcFeatures;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

/** Entry point of the BuildCraft part (MJ system, engines, oil, quarry, pump, tank, refinery). */
public final class BuildCraft {
    private BuildCraft() {
    }

    public static void init(IEventBus modBus) {
        BcBlocks.BLOCKS.register(modBus);
        BcItems.ITEMS.register(modBus);
        BcFluids.FLUID_TYPES.register(modBus);
        BcFluids.FLUIDS.register(modBus);
        BcBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        BcMenus.MENUS.register(modBus);
        BcFeatures.FEATURES.register(modBus);
        modBus.addListener(BuildCraft::registerCapabilities);
        modBus.addListener(BuildCraft::commonSetup);
        modBus.addListener(QuarryChunkLoading::register);
        modBus.addListener(PipeItemPayload::register);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BcFluids.registerInteractions();
            RefineryRecipe.registerDefaults();
        });
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (EngineType type : EngineType.values()) {
            event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, BcBlockEntityTypes.engine(type),
                    (be, side) -> be);
            if (type.hasMenu()) {
                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BcBlockEntityTypes.engine(type),
                        (be, side) -> be.getItemHandler());
            }
        }
        event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, BcBlockEntityTypes.QUARRY.get(), (be, side) -> be);
        event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, BcBlockEntityTypes.PUMP.get(), (be, side) -> be);
        event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, BcBlockEntityTypes.REFINERY.get(), (be, side) -> be);
        event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, BcBlockEntityTypes.MINING_WELL.get(),
                (be, side) -> be);
        // Only the pipes with a power provider (wood) receive MJ.
        event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, BcBlockEntityTypes.ITEM_PIPE.get(),
                (be, side) -> be.getPowerProvider() != null ? be : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BcBlockEntityTypes.TANK.get(),
                (be, side) -> be.getFluidHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BcBlockEntityTypes.PUMP.get(),
                (be, side) -> be.getFluidHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BcBlockEntityTypes.REFINERY.get(),
                (be, side) -> be.getFluidHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BcBlockEntityTypes.engine(EngineType.COMBUSTION),
                (be, side) -> ((CombustionEngineBlockEntity) be).getFluidHandler());
        // NeoForge only wraps items of the exact class BucketItem; the oil bucket is one, the fuel bucket is not.
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new FluidBucketWrapper(stack),
                BcItems.FUEL_BUCKET.get());
    }
}
