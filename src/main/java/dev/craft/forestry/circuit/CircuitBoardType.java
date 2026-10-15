package dev.craft.forestry.circuit;

/** Forestry {@code EnumCircuitBoardType}: sockets for electron tubes and the two item colours. */
public enum CircuitBoardType {
    BASIC("basic", 1, 0x191919, 0x6DCFF6),
    ENHANCED("enhanced", 2, 0x191919, 0xCB7C32),
    REFINED("refined", 3, 0x191919, 0xC9C9C9),
    INTRICATE("intricate", 4, 0x191919, 0xE2CB6B);

    private final String name;
    private final int sockets;
    private final int primaryColor;
    private final int secondaryColor;

    CircuitBoardType(String name, int sockets, int primaryColor, int secondaryColor) {
        this.name = name;
        this.sockets = sockets;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public String getName() {
        return name;
    }

    public int getSockets() {
        return sockets;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }
}
