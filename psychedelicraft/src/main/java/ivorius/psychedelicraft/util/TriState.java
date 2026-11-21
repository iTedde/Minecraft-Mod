package ivorius.psychedelicraft.util;

/**
 * Three valued boolean (upstream: Fabric API {@code TriState}).
 */
public enum TriState {
    FALSE, DEFAULT, TRUE;

    public static TriState of(boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean orElse(boolean value) {
        return this == DEFAULT ? value : this == TRUE;
    }

    public boolean get() {
        return this == TRUE;
    }
}
