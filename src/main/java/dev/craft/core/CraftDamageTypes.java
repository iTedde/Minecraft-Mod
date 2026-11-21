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
    /** Charge pad shock (ChargePads {@code electrocution}, spec 47 §6); bypasses armor. */
    public static final ResourceKey<DamageType> ELECTROCUTION = key("electrocution");
    /** Damage conversion module of the charge pads (spec 47 §5); armor applies. */
    public static final ResourceKey<DamageType> EMITTER_RADIATION = key("emitter_radiation");
    /** MFFS {@code fieldShock}: force field touch and click (spec 48 §9.4), ignores armour. */
    public static final ResourceKey<DamageType> FIELD_SHOCK = key("field_shock");
    /** MFFS {@code fieldDefense}: NPC defence and defence station upgrades (spec 48 §9.3), ignores armour. */
    public static final ResourceKey<DamageType> FIELD_DEFENSE = key("field_defense");
    /** MFFS {@code areaDefense}: defence station warning and kill (spec 48 §10), ignores armour. */
    public static final ResourceKey<DamageType> AREA_DEFENSE = key("area_defense");

    private CraftDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Craft.MODID, name));
    }

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(ELECTRICITY, new DamageType("craft.electricity", 0.1F));
        context.register(RADIATION, new DamageType("craft.radiation", 0.1F));
        context.register(NUKE, new DamageType("craft.nuke", 0.1F));
        context.register(ELECTROCUTION, new DamageType("craft.electrocution", 0.1F));
        context.register(EMITTER_RADIATION, new DamageType("craft.emitter_radiation", 0.1F));
        context.register(FIELD_SHOCK, new DamageType("craft.field_shock", 0.1F));
        context.register(FIELD_DEFENSE, new DamageType("craft.field_defense", 0.1F));
        context.register(AREA_DEFENSE, new DamageType("craft.area_defense", 0.1F));
    }
}
