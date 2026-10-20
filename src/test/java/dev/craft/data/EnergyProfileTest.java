package dev.craft.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/** Energy profile report (spec 24 §4, phase 23.6): generated, complete and matching the spec values. */
class EnergyProfileTest {
    private static Path projectDir() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null && !Files.isDirectory(dir.resolve("src/generated"))) {
            dir = dir.getParent();
        }
        return dir == null ? Path.of("") : dir;
    }

    private static Map<String, JsonObject> blocks(JsonObject root) {
        Map<String, JsonObject> blocks = new HashMap<>();
        for (JsonElement e : root.getAsJsonArray("blocks")) {
            blocks.put(e.getAsJsonObject().get("id").getAsString(), e.getAsJsonObject());
        }
        return blocks;
    }

    @Test
    void reportMatchesSpecValues() throws IOException {
        Path report = projectDir().resolve("src/generated/resources/reports/craft/energy_profile.json");
        assertTrue(Files.exists(report), "run gradlew runData");
        JsonObject root = JsonParser.parseString(Files.readString(report)).getAsJsonObject();
        Map<String, JsonObject> blocks = blocks(root);
        // spec 03/04/13: safe inputs and packet sizes
        assertEquals(32, blocks.get("macerator").get("maxSafeInput").getAsInt());
        assertEquals(128, blocks.get("mfe").get("maxSafeInput").getAsInt());
        assertEquals(512, blocks.get("mfsu").get("maxEnergyOutput").getAsInt());
        assertEquals(10000000, blocks.get("mfsu").getAsJsonObject("values").get("getMaxStorage").getAsInt());
        assertEquals(128, blocks.get("industrial_electrolyzer").get("maxSafeInput").getAsInt());
        assertEquals(Integer.MAX_VALUE, blocks.get("hv_transformer").get("maxSafeInput").getAsInt());
        assertEquals(33, blocks.get("insulated_copper_cable").get("conductorBreakdown").getAsInt());
        assertTrue(root.getAsJsonArray("items").size() >= 20, "electric items");
    }
}
