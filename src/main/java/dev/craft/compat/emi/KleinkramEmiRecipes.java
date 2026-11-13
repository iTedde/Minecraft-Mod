package dev.craft.compat.emi;

import java.util.List;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/** Phase 50 small mods (spec 50 §5): info pages; recipes EMI shows anyway. */
final class KleinkramEmiRecipes {
    private KleinkramEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        info(registry, dev.craft.omnitools.OmniTools.OMNIWRENCH.get(), "omniwrench");
        info(registry, dev.craft.obsidiplates.ObsidiPlates.OBSIDIAN_PRESSURE_PLATE.get(), "obsidian_pressure_plate");
        info(registry, dev.craft.omnitools.OmniTools.FORGE_LEXICON.get(), "forge_lexicon");
        info(registry, dev.craft.omnitools.OmniTools.MYSTICAL_PLINTH.get(), "mystical_plinth");
        info(registry, dev.craft.gravitygun.GravityGun.GRAVITY_GUN.get(), "gravity_gun");
        info(registry, dev.craft.gravitygun.GravityGun.CHARGED_GRAVITY_GUN.get(), "charged_gravity_gun");
        info(registry, dev.craft.secretrooms.SecretRooms.CAMO_PASTE.get(), "camo_paste");
        info(registry, dev.craft.secretrooms.SecretRooms.GHOST_BLOCK.get(), "ghost_block");
        info(registry, dev.craft.secretrooms.SecretRooms.ONE_WAY_GLASS.get(), "one_way_glass");
        info(registry, dev.craft.secretrooms.SecretRooms.CAMO_REDSTONE.get(), "camo_redstone");
        infoAll(registry, List.of(dev.craft.secretrooms.SecretRooms.CAMO_LEVER.get(), dev.craft.secretrooms.SecretRooms.CAMO_BUTTON.get(), dev.craft.secretrooms.SecretRooms.CAMO_PRESSURE_PLATE.get(),
                dev.craft.secretrooms.SecretRooms.CAMO_PLAYER_PRESSURE_PLATE.get()), "camo_switches");
        info(registry, dev.craft.secretrooms.SecretRooms.CAMO_GATE.get(), "camo_gate");
        infoAll(registry, List.of(dev.craft.secretrooms.SecretRooms.CAMO_WOODEN_DOOR.get(), dev.craft.secretrooms.SecretRooms.CAMO_IRON_DOOR.get(), dev.craft.secretrooms.SecretRooms.CAMO_TRAPDOOR.get(),
                dev.craft.secretrooms.SecretRooms.CAMO_STAIRS.get(), dev.craft.secretrooms.SecretRooms.CAMO_CHEST.get(), dev.craft.secretrooms.SecretRooms.TORCH_LEVER.get()), "camo_doors");
    }

    static void infoAll(EmiRegistry registry, List<? extends ItemLike> items, String key) {
        registry.addRecipe(new EmiInfoRecipe(items.stream().map(item -> (dev.emi.emi.api.stack.EmiIngredient)
                EmiStack.of(item)).toList(), List.of(Component.translatable("emi.craft.info." + key)),
                Ic2EmiRecipes.id("info/" + key)));
    }

    static void info(EmiRegistry registry, ItemLike item, String key) {
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(item)),
                List.of(Component.translatable("emi.craft.info." + key)), Ic2EmiRecipes.id("info/" + key)));
    }
}
