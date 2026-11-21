package dev.craft.chargepads;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.chargepads.block.ChargePadBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CpBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    /** One type for all pads; the tier comes from the block. */
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<ChargePadBlockEntity>> CHARGE_PAD =
            BLOCK_ENTITY_TYPES.register("charge_pad", () -> BlockEntityType.Builder.of(ChargePadBlockEntity::new,
                    CpBlocks.STATIC_CHARGE_PAD.get(), CpBlocks.CRYSTALIZOR_CHARGE_PAD.get(),
                    CpBlocks.LAPOTRONIC_CHARGE_PAD.get()).build(null));

    private CpBlockEntityTypes() {
    }
}
