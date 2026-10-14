package dev.craft.ic2.machine;

/** Which directions a machine's front may point to. */
public enum FacingMode {
    /** Generators and machines: front faces the placer, horizontal only. */
    HORIZONTAL,
    /** Storage blocks and transformers: also up/down depending on the placer's pitch. */
    ALL
}
