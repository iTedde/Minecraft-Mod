package dev.craft.mps.module;

import java.util.EnumSet;
import java.util.Set;

/** The five modular items a module can go into ({@code validItems} in MPS). */
public enum ModuleTarget {
    HEAD, TORSO, LEGS, FEET, TOOL;

    public static final Set<ModuleTarget> ARMOR = EnumSet.of(HEAD, TORSO, LEGS, FEET);
    public static final Set<ModuleTarget> ALL = EnumSet.allOf(ModuleTarget.class);
}
