package ivorius.psychedelicraft.world.gen.loot;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public interface PSLootFunctionTypes {
    LootItemFunctionType<SetFluidsLootFunction> SET_FLUIDS = register("set_fluids", SetFluidsLootFunction.CODEC);

    private static <T extends LootItemFunction> LootItemFunctionType<T> register(String name, MapCodec<T> codec) {
        return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Psychedelicraft.id(name), new LootItemFunctionType<>(codec));
    }

    static void bootstrap() {}
}
