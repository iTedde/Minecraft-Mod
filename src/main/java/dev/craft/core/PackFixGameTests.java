package dev.craft.core;

import dev.craft.Craft;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The built-in pack with the Create Encased recipe fix is always active; without Create Encased it adds nothing. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class PackFixGameTests {
    private PackFixGameTests() {
    }

    @GameTest(template = "empty_8x4x8")
    public static void createEncasedFixPackIsActive(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getServer().getPackRepository().getSelectedIds().stream()
                .anyMatch(id -> id.contains("createcasing_fix")), "pack selected");
        helper.assertFalse(helper.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(
                "createcasing", "crafting/shafts/birch_manual_only")).isPresent(), "no recipe without Create Encased");
        helper.succeed();
    }
}
