package dev.craft.buildcraft.transport;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;

/**
 * The BuildCraft pipe types ({@code BuildCraftTransport.createPipe}). Each type is its own block and item; textures
 * are the tiles of the original {@code blocks.png} under {@code block/pipe/}.
 */
public enum PipeType {
    WOODEN_TRANSPORT("wooden_transport_pipe", Transport.ITEMS, PipeMaterial.WOOD, "wooden", "wooden_source"),
    COBBLESTONE_TRANSPORT("cobblestone_transport_pipe", Transport.ITEMS, PipeMaterial.COBBLESTONE, "cobblestone", null),
    STONE_TRANSPORT("stone_transport_pipe", Transport.ITEMS, PipeMaterial.STONE, "stone", null),
    SANDSTONE_TRANSPORT("sandstone_transport_pipe", Transport.ITEMS, PipeMaterial.SANDSTONE, "sandstone", null),
    IRON_TRANSPORT("iron_transport_pipe", Transport.ITEMS, PipeMaterial.IRON, "iron_output", "iron", "iron_output"),
    GOLDEN_TRANSPORT("golden_transport_pipe", Transport.ITEMS, PipeMaterial.GOLD, "golden", null),
    DIAMOND_TRANSPORT("diamond_transport_pipe", Transport.ITEMS, PipeMaterial.DIAMOND, "diamond_item", null),
    OBSIDIAN_TRANSPORT("obsidian_transport_pipe", Transport.ITEMS, PipeMaterial.OBSIDIAN, "obsidian", null),
    EMERALD_TRANSPORT("emerald_transport_pipe", Transport.ITEMS, PipeMaterial.EMERALD, "emerald", "emerald_source"),
    VOID_TRANSPORT("void_transport_pipe", Transport.ITEMS, PipeMaterial.VOID, "void", null),
    WOODEN_FLUID("wooden_fluid_pipe", Transport.FLUIDS, PipeMaterial.WOOD, "wooden_fluid", "wooden_source"),
    COBBLESTONE_FLUID("cobblestone_fluid_pipe", Transport.FLUIDS, PipeMaterial.COBBLESTONE, "cobblestone_fluid", null),
    STONE_FLUID("stone_fluid_pipe", Transport.FLUIDS, PipeMaterial.STONE, "stone_fluid", null),
    SANDSTONE_FLUID("sandstone_fluid_pipe", Transport.FLUIDS, PipeMaterial.SANDSTONE, "sandstone_fluid", null),
    IRON_FLUID("iron_fluid_pipe", Transport.FLUIDS, PipeMaterial.IRON, "iron_fluid_output", "iron",
            "iron_fluid_output"),
    GOLDEN_FLUID("golden_fluid_pipe", Transport.FLUIDS, PipeMaterial.GOLD, "golden_fluid", null),
    EMERALD_FLUID("emerald_fluid_pipe", Transport.FLUIDS, PipeMaterial.EMERALD, "emerald_fluid", "emerald_source"),
    VOID_FLUID("void_fluid_pipe", Transport.FLUIDS, PipeMaterial.VOID, "void_fluid", null),
    WOODEN_POWER("wooden_power_pipe", Transport.POWER, PipeMaterial.WOOD, "wooden_power", "wooden_source"),
    STONE_POWER("stone_power_pipe", Transport.POWER, PipeMaterial.STONE, "stone_power", null),
    GOLDEN_POWER("golden_power_pipe", Transport.POWER, PipeMaterial.GOLD, "golden_power", null),
    COBBLESTONE_STRUCTURE("cobblestone_structure_pipe", Transport.STRUCTURE, PipeMaterial.COBBLESTONE,
            "cobblestone_structure", null);

    private final String name;
    private final Transport transport;
    private final PipeMaterial material;
    private final String texture;
    private final String plainTexture;
    @Nullable
    private final String sideTexture;

    PipeType(String name, Transport transport, PipeMaterial material, String texture, @Nullable String sideTexture) {
        this(name, transport, material, texture, texture, sideTexture);
    }

    /** Iron: core and output arm use {@code baseTexture}, the other arms {@code plainTexture}. */
    PipeType(String name, Transport transport, PipeMaterial material, String texture, String plainTexture,
             @Nullable String sideTexture) {
        this.name = name;
        this.transport = transport;
        this.material = material;
        this.texture = texture;
        this.plainTexture = plainTexture;
        this.sideTexture = sideTexture;
    }

    public String getName() {
        return name;
    }

    public Transport getTransport() {
        return transport;
    }

    public PipeMaterial getMaterial() {
        return material;
    }

    /** Texture of the core (and of the plain arms, see {@link #getArmTexture}), also used for the item. */
    public String getTexture() {
        return texture;
    }

    /** Texture of a plain arm: the diamond pipe has one per side ({@code getTextureIndex(direction)}). */
    public String getArmTexture(Direction side) {
        return material == PipeMaterial.DIAMOND ? "diamond_" + side.getName() : plainTexture;
    }

    /** Texture of the source arm (wood, emerald) or the output arm (iron); {@code null} for the other pipes. */
    @Nullable
    public String getSideTexture() {
        return sideTexture;
    }

    /** {@code PipeTransport*} classes: pipes only connect to pipes of the same transport. */
    public enum Transport {
        ITEMS, FLUIDS, POWER, STRUCTURE
    }
}
