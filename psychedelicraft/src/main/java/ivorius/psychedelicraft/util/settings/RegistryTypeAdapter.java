package ivorius.psychedelicraft.util.settings;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

/**
 * Serializes registry entries by id.
 */
public final class RegistryTypeAdapter<T> extends TypeAdapter<T> {
    private final Registry<T> registry;

    private RegistryTypeAdapter(Registry<T> registry) {
        this.registry = registry;
    }

    public static <T> RegistryTypeAdapter<T> of(Registry<T> registry) {
        return new RegistryTypeAdapter<>(registry);
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        ResourceLocation id = value == null ? null : registry.getKey(value);
        if (id == null) {
            out.nullValue();
        } else {
            out.value(id.toString());
        }
    }

    @Override
    public T read(JsonReader in) throws IOException {
        ResourceLocation id = ResourceLocation.tryParse(in.nextString());
        return id == null ? null : registry.get(id);
    }
}
