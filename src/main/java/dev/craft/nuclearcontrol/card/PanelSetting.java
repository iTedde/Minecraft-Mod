package dev.craft.nuclearcontrol.card;

/** {@code PanelSetting}: a check box in the panel GUI that switches the display bit {@code bit} of a card type. */
public record PanelSetting(String key, int bit) {
    public String translationKey() {
        return "gui.craft.nuclearcontrol.panel_setting." + key;
    }
}
