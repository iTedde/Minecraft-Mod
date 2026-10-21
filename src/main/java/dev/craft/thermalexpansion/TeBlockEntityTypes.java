package dev.craft.thermalexpansion;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.thermalexpansion.machine.PoweredFurnaceBlockEntity;
import dev.craft.thermalexpansion.machine.SecondaryOutputMachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Block entity types of Thermal Expansion (spec 26). */
public final class TeBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    public static final Supplier<BlockEntityType<PoweredFurnaceBlockEntity>> POWERED_FURNACE =
            BLOCK_ENTITY_TYPES.register("powered_furnace", () -> BlockEntityType.Builder.of(
                    PoweredFurnaceBlockEntity::new, TeBlocks.POWERED_FURNACE.get()).build(null));
    public static final Supplier<BlockEntityType<SecondaryOutputMachineBlockEntity>> PULVERIZER =
            BLOCK_ENTITY_TYPES.register("pulverizer", () -> BlockEntityType.Builder.of(
                    SecondaryOutputMachineBlockEntity::pulverizer, TeBlocks.PULVERIZER.get()).build(null));
    public static final Supplier<BlockEntityType<SecondaryOutputMachineBlockEntity>> SAWMILL =
            BLOCK_ENTITY_TYPES.register("sawmill", () -> BlockEntityType.Builder.of(
                    SecondaryOutputMachineBlockEntity::sawmill, TeBlocks.SAWMILL.get()).build(null));

    private TeBlockEntityTypes() {
    }
}
