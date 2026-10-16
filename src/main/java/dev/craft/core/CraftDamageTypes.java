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
    /** Radioactivity effect; bypasses armor (IC2 {@code radiation}). */
    public static final ResourceKey<DamageType> RADIATION = key("radiation");
    /** Reactor meltdown and nuke explosions (IC2 {@code nuke}); also triggers the nuke radiation effect. */
    public static final ResourceKey<DamageType> NUKE = key("nuke");

    private CraftDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Craft.MODID, name));
    }

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(ELECTRICITY, new DamageType("craft.electricity", 0.1F));
        context.register(RADIATION, new DamageType("craft.radiation", 0.1F));
        context.register(NUKE, new DamageType("craft.nuke", 0.1F));
    }
}
