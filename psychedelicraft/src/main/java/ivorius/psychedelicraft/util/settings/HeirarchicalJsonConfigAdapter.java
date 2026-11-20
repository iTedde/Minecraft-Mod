package ivorius.psychedelicraft.util.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Stores settings as {@code {category: {name: value}}}. Comments are written as sibling {@code "name.comment"}
 * arrays because json has no comments.
 */
public final class HeirarchicalJsonConfigAdapter {
    private final Gson gson;

    public HeirarchicalJsonConfigAdapter(GsonBuilder builder) {
        this.gson = builder.setPrettyPrinting().disableHtmlEscaping().create();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void read(Path path, List<Setting<?>> settings) throws IOException {
        JsonObject root;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        }
        for (Setting setting : settings) {
            if (root.get(setting.category()) instanceof JsonObject category && category.has(setting.name())) {
                try {
                    setting.set(gson.fromJson(category.get(setting.name()), setting.type()));
                } catch (RuntimeException e) {
                    setting.set(setting.getDefault());
                }
            }
        }
    }

    void write(Path path, List<Setting<?>> settings) throws IOException {
        JsonObject root = new JsonObject();
        for (Setting<?> setting : settings) {
            JsonObject category = root.has(setting.category()) ? root.getAsJsonObject(setting.category()) : new JsonObject();
            root.add(setting.category(), category);
            if (!setting.getComments().isEmpty()) {
                JsonArray comments = new JsonArray();
                setting.getComments().forEach(comments::add);
                category.add(setting.name() + ".comment", comments);
            }
            category.add(setting.name(), gson.toJsonTree(setting.get(), setting.type()));
        }
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        }
    }
}
