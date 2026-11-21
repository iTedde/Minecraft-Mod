package ivorius.psychedelicraft.item.component;

import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.Processable;

public interface PSComponents {
    DataComponentType<BagContentsComponent> BAG_CONTENTS = register("bag_contents", builder -> builder.persistent(BagContentsComponent.CODEC).networkSynchronized(BagContentsComponent.PACKET_CODEC));
    DataComponentType<RiftFractionComponent> RIFT_FRACTION = register("rift_fraction", builder -> builder.persistent(RiftFractionComponent.CODEC).networkSynchronized(RiftFractionComponent.PACKET_CODEC));
    DataComponentType<ItemFluids> FLUIDS = register("fluids", builder -> builder.persistent(ItemFluids.CODEC).networkSynchronized(ItemFluids.PACKET_CODEC));
    DataComponentType<ItemFluidsMixture> FLUIDS_MIXTURE = register("fluids_mixture", builder -> builder.persistent(ItemFluidsMixture.CODEC).networkSynchronized(ItemFluidsMixture.PACKET_CODEC));
    DataComponentType<FluidCapacity> FLUID_CAPACITY = register("fluid_capacity", builder -> builder.persistent(FluidCapacity.CODEC).networkSynchronized(FluidCapacity.PACKET_CODEC));
    DataComponentType<Processable.ProcessType> PROCESS_TYPE = register("process_type", builder -> builder.persistent(Processable.ProcessType.CODEC).networkSynchronized(Processable.ProcessType.PACKET_CODEC));
    DataComponentType<ItemDrugs> DRUGS = register("drugs", builder -> builder.persistent(ItemDrugs.CODEC).networkSynchronized(ItemDrugs.PACKET_CODEC));
    DataComponentType<Impurities> IMPURITIES = register("impurities", builder -> builder.persistent(Impurities.CODEC).networkSynchronized(Impurities.PACKET_CODEC));

    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Psychedelicraft.id(id), builderOperator.apply(DataComponentType.builder()).build());
    }

    static void bootstrap() {
        PSSubPredicates.bootstrap();
    }
}
