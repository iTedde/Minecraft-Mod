package dev.craft.nuclearcontrol.card;

import javax.annotation.Nullable;

/** {@code CardState}: result of the last card update, kept in the card data as {@code state}. */
public enum CardState {
    OK(1, null),
    NO_TARGET(2, "no_target"),
    OUT_OF_RANGE(3, "out_of_range"),
    INVALID_CARD(4, "invalid_card");

    private final int index;
    @Nullable
    private final String message;

    CardState(int index, @Nullable String message) {
        this.index = index;
        this.message = message;
    }

    public int getIndex() {
        return index;
    }

    /** Translation key suffix of the centred message the panel shows instead of the data. */
    @Nullable
    public String getMessage() {
        return message;
    }

    /** {@code fromInteger}; a card that was never updated has no state. */
    @Nullable
    public static CardState fromIndex(int index) {
        for (CardState state : values()) {
            if (state.index == index) {
                return state;
            }
        }
        return null;
    }
}
