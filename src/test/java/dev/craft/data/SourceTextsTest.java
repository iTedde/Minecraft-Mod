package dev.craft.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/** Source texts (spec 24): known items, no duplicates, generated in both languages, every raw material covered. */
class SourceTextsTest {
    private static final Path LANG = projectDir().resolve("src/generated/resources/assets/craft/lang");

    /** Items of each finished phase-23 group that must have a source text. */
    private static final List<String> REQUIRED = List.of(
            "rubber_wood", "rubber_leaves", "rubber_sapling", "resin", "uranium_drop", "iridium_ore", "scrap",
            "uu_matter", "coffee_beans", "hops", "terra_wart", "oil", "oil_bucket",
            "minecraft:copper_ore", "minecraft:deepslate_copper_ore", "minecraft:raw_copper",
            "ruby", "sapphire", "green_sapphire", "olivine", "red_garnet", "yellow_garnet", "industrial_diamond",
            "empty_cell", "coal_ball", "compressed_coal_ball", "coal_chunk", "carbon_fiber", "carbon_mesh", "coke",
            "coke_block", "ash", "dark_ash", "plant_ball", "compressed_plant_ball", "hydrated_coal_clump", "rubber",
            "advanced_alloy", "fuel_bucket", "wood_chips", "sawdust", "compressed_sawdust", "slag", "rich_slag");

    /** The unit tests may run in a run directory below the project; walk up to the project root. */
    private static Path projectDir() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null && !Files.isDirectory(dir.resolve("src/generated"))) {
            dir = dir.getParent();
        }
        return dir == null ? Path.of("") : dir;
    }

    private static JsonObject lang(String locale) throws IOException {
        return JsonParser.parseString(Files.readString(LANG.resolve(locale + ".json"))).getAsJsonObject();
    }

    private static Set<String> ids() {
        Set<String> ids = new HashSet<>();
        for (SourceTexts.Entry entry : SourceTexts.entries()) {
            for (String id : entry.ids()) {
                assertTrue(ids.add(id), "duplicate " + id);
            }
        }
        return ids;
    }

    @Test
    void everyIdIsACraftOrVanillaItem() throws IOException {
        JsonObject german = lang("de_de");
        for (String id : ids()) {
            if (id.startsWith("craft:")) {
                String path = id.substring(6);
                assertTrue(german.has("item.craft." + path) || german.has("block.craft." + path), "unknown " + id);
            } else {
                assertTrue(id.startsWith("minecraft:"), "unknown namespace " + id);
            }
        }
    }

    @Test
    void generatedInBothLanguages() throws IOException {
        JsonObject german = lang("de_de");
        JsonObject english = lang("en_us");
        for (String id : ids()) {
            String key = "source." + id.replace(':', '.');
            assertTrue(german.has(key), "missing German " + key);
            assertTrue(english.has(key), "missing English " + key);
        }
    }

    @Test
    void everyOreAndListedMaterialIsCovered() throws IOException {
        Set<String> ids = ids();
        List<String> missing = new ArrayList<>();
        for (String key : lang("de_de").keySet()) {
            boolean ore = key.startsWith("block.craft.") && (key.endsWith("_ore") || key.endsWith("_ore_block"));
            boolean material = key.startsWith("item.craft.") && (key.endsWith("_dust") || key.endsWith("_ingot")
                    || key.endsWith("_plate") || key.endsWith("_nugget") || key.matches(".*_cell(_[0-9]+k)?"));
            if (ore || material) {
                String id = "craft:" + key.substring(key.indexOf(".craft.") + 7);
                if (!ids.contains(id)) {
                    missing.add(id);
                }
            }
        }
        for (String id : REQUIRED) {
            String full = id.contains(":") ? id : "craft:" + id;
            if (!ids.contains(full)) {
                missing.add(full);
            }
        }
        assertEquals(List.of(), missing);
    }
}
