package dev.craft.mffs.field;

/**
 * What a module needs to compute its field (spec 48 §9.2): the projector's facing as 3D data value (0 down, 1 up,
 * 2 north, 3 south, 4 west, 5 east – the same numbering as 1.4.7), the item counts of the distance/strength modulators
 * and the four focus matrix slots, the field manipulator ("dome") and the projector's height with the level bounds.
 *
 * @param maxY the original's 255 – the highest block index ({@code maxBuildHeight − 1})
 * @param minY the original's 0 – the lowest block ({@code minBuildHeight})
 */
public record FieldParams(int side, int distance, int strength, int focusUp, int focusDown, int focusRight,
                          int focusLeft, boolean dome, int y, int minY, int maxY) {
}
