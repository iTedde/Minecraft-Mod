package dev.craft.thermalexpansion.transport;

/** {@code Teleporters.Types}: the three tesseracts, named like their registry and name-list categories. */
public enum TesseractType {
    ENERGY("energy"),
    LIQUID("liquid"),
    ITEM("item");

    private final String name;

    TesseractType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getBlockName() {
        return name + "_tesseract";
    }
}
