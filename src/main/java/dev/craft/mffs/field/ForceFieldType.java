package dev.craft.mffs.field;

import net.minecraft.util.StringRepresentable;

/**
 * {@code ForceFieldTyps} (spec 48 §9.1): block state {@code type} of the force field block. The ordinal order is the
 * original one – the cost rules compare against {@link #DEFAULT} (ordinal 1).
 */
public enum ForceFieldType implements StringRepresentable {
    CAMOUFLAGE("camouflage"),
    DEFAULT("default"),
    ZAPPER("zapper"),
    AREA("area"),
    CONTAINMENT("containment");

    private final String name;

    ForceFieldType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
