package ivorius.psychedelicraft.util.settings;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A single named config value (replacement for the Kirin {@code Setting} used upstream).
 */
public final class Setting<T> {
    private final String category;
    private final String name;
    private final Type type;
    private final T defaultValue;
    private final List<String> comments = new ArrayList<>();
    private final List<Consumer<T>> listeners = new ArrayList<>();
    private T value;

    Setting(String category, String name, Type type, T defaultValue) {
        this.category = category;
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String category() {
        return category;
    }

    public String name() {
        return name;
    }

    Type type() {
        return type;
    }

    public T get() {
        return value;
    }

    public T getDefault() {
        return defaultValue;
    }

    public T set(T value) {
        this.value = value == null ? defaultValue : value;
        listeners.forEach(l -> l.accept(this.value));
        return this.value;
    }

    public Setting<T> addComment(String comment) {
        comments.add(comment);
        return this;
    }

    public List<String> getComments() {
        return comments;
    }

    public Setting<T> onChanged(Consumer<T> listener) {
        listeners.add(listener);
        return this;
    }
}
