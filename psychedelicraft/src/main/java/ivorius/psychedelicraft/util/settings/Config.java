package ivorius.psychedelicraft.util.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ivorius.psychedelicraft.Psychedelicraft;

/**
 * A json backed collection of settings grouped by category (replacement for the Kirin {@code Config} used upstream).
 */
public abstract class Config {
    private final HeirarchicalJsonConfigAdapter adapter;
    private final Path path;
    private final List<Setting<?>> settings = new ArrayList<>();

    protected Config(HeirarchicalJsonConfigAdapter adapter, Path path) {
        this.adapter = adapter;
        this.path = path;
    }

    protected <T> Setting<T> value(String category, String name, T defaultValue) {
        Setting<T> setting = new Setting<>(category, name, defaultValue.getClass(), defaultValue);
        settings.add(setting);
        return setting;
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public Path getPath() {
        return path;
    }

    public void load() {
        if (Files.exists(path)) {
            try {
                adapter.read(path, settings);
            } catch (IOException | RuntimeException e) {
                Psychedelicraft.LOGGER.warn("Could not read config {}, using defaults", path, e);
            }
        }
        save();
    }

    public void save() {
        try {
            Files.createDirectories(path.getParent());
            adapter.write(path, settings);
        } catch (IOException e) {
            Psychedelicraft.LOGGER.warn("Could not write config {}", path, e);
        }
    }
}
