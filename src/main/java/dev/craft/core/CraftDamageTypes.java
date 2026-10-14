package dev.craft.core;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class CraftDamageTypes {
    /** Electric shock from energy paths; not armor piercing (IC2 {@code electricity}). */
    public static final ResourceKey<DamageType> ELECTRICITY = key("electricity");

    private CraftDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Craft.MODID, name));
    }

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(ELECTRICITY, new DamageType("craft.electricity", 0.1F));
    }
}
