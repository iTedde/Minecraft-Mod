package ivorius.psychedelicraft.fluid;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.physical.PhysicalFluid;
import ivorius.psychedelicraft.fluid.physical.PlacedFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public class VanillaFluid extends SimpleFluid implements ConsumableFluid {
    static SimpleFluid register(ResourceLocation id, Fluid fluid, boolean empty) {
        if (!id.getNamespace().equals(Psychedelicraft.DEFAULT_NAMESPACE)
                && !ALIASED_IDS.containsKey(id)) {
            ResourceLocation alias = Psychedelicraft.id(id.getPath());
            if (REGISTRY.containsKey(alias)) {
                SimpleFluid aliasFluid = REGISTRY.get(alias);
                if (aliasFluid != null && !aliasFluid.isEmpty()) {
                    ALIASED_IDS.put(id, aliasFluid);
                    return aliasFluid;
                }
            }
        }

        return Registry.register(REGISTRY, id, new VanillaFluid(id, fluid, empty));
    }

    /**
     * Aliases fluids registered by other mods after Psychedelicraft's bootstrap
     * (upstream: Fabric registry entry added callback). Called at the end of the fluid RegisterEvent.
     */
    public static void registerLateFluids() {
        BuiltInRegistries.FLUID.holders().forEach(entry -> {
            if (entry.value() instanceof PlacedFluid) {
                return;
            }
            try {
                register(entry.unwrapKey().get().location(), entry.value());
            } catch (Throwable e) {
                Psychedelicraft.LOGGER.info("Could not register vanilla fluid {}", entry.unwrapKey().get().location(), e);
            }
        });
    }

    static void bootstrap() {
        BuiltInRegistries.FLUID.holders().forEach(entry -> {
            register(entry.unwrapKey().get().location(), entry.value());
        });
    }

    private static void register(ResourceLocation id, Fluid value) {
        if (VanillaFluid.toStill(value) == value && !REGISTRY.containsKey(id) && !"minecraft:empty".equals(id.toString())) {
            ResourceLocation registeredId = REGISTRY.getKey(VanillaFluid.register(id, value, false));
            if (registeredId.equals(id)) {
                Psychedelicraft.LOGGER.info("Added vanilla fluid " + id);
            } else {
                Psychedelicraft.LOGGER.info("Aliased vanilla fluid " + id + " <=> " + registeredId);
            }
        }
    }

    private VanillaFluid(ResourceLocation id, Fluid still, boolean empty) {
        super(id, 0xFFFFFFFF,
                new PhysicalFluid(() -> still, toFlowing(still), Suppliers.memoize(() -> still.defaultFluidState().createLegacyBlock().getBlock())),
                empty
        );
    }

    private static Supplier<Fluid> toFlowing(Fluid fluid) {
        return fluid instanceof FlowingFluid ? ((FlowingFluid)fluid)::getFlowing : Suppliers.ofInstance(fluid);
    }

    static Fluid toStill(Fluid fluid) {
        return fluid instanceof FlowingFluid ? ((FlowingFluid)fluid).getSource() : fluid;
    }

    @Override
    public boolean canConsume(ItemStack stack, LivingEntity entity, ConsumptionType type) {
        return getPhysical().isIn(FluidTags.LAVA) && type == ConsumptionType.INJECT;
    }

    @Override
    public void consume(ItemFluids stack, LivingEntity entity, ConsumptionType type) {
        if (getPhysical().isIn(FluidTags.LAVA)) {
            entity.lavaHurt();
            entity.igniteForSeconds(30F);
        }
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return container.is(getPreferredContainerTag()) || container.is(Items.BUCKET) || container.is(PSItems.FILLED_BUCKET) || container.is(Items.GLASS_BOTTLE);
    }
}
