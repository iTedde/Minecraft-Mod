package dev.craft.forestry.circuit;

/**
 * The electron tubes ({@code thermionicTubes} meta 0–3) that solder Electrical Engine circuits: overlay colour and
 * the circuit on the {@link CircuitLayout#ENGINE_TIN} layout ({@code PluginForestryEnergy.registerRecipes}).
 */
public enum ElectronTube {
    COPPER("copper", 0xE3B78E, Circuit.ELECTRIC_CHOKE_1),
    TIN("tin", 0xE1EEF4, Circuit.ELECTRIC_BOOST_1),
    BRONZE("bronze", 0xDDC276, Circuit.ELECTRIC_BOOST_2),
    IRON("iron", 0xD8D8D8, Circuit.ELECTRIC_EFFICIENCY_1);

    /** Colour of the tube body (render pass 0). */
    public static final int PRIMARY_COLOR = 0xFFFFFF;

    private final String name;
    private final int secondaryColor;
    private final Circuit engineCircuit;

    ElectronTube(String name, int secondaryColor, Circuit engineCircuit) {
        this.name = name;
        this.secondaryColor = secondaryColor;
        this.engineCircuit = engineCircuit;
    }

    public String getName() {
        return name;
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    /** {@code SolderManager.getMatchingRecipe}: the circuit this tube solders on {@code layout}, or null. */
    public Circuit getCircuit(CircuitLayout layout) {
        return layout == CircuitLayout.ENGINE_TIN ? engineCircuit : null;
    }
}
