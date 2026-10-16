package dev.craft.buildcraft.transport;

import javax.annotation.Nullable;

/**
 * The BuildCraft pipe types ({@code BuildCraftTransport.createPipe}). Each type is its own block and item; textures
 * are the tiles of the original {@code blocks.png} under {@code block/pipe/}.
 */
public enum PipeType {
    WOODEN_TRANSPORT("wooden_transport_pipe", Transport.ITEMS, PipeMaterial.WOOD, "wooden", "wooden_source"),
    COBBLESTONE_TRANSPORT("cobblestone_transport_pipe", Transport.ITEMS, PipeMaterial.COBBLESTONE, "cobblestone", null),
    STONE_TRANSPORT("stone_transport_pipe", Transport.ITEMS, PipeMaterial.STONE, "stone", null),
    SANDSTONE_TRANSPORT("sandstone_transport_pipe", Transport.ITEMS, PipeMaterial.SANDSTONE, "sandstone", null),
    IRON_TRANSPORT("iron_transport_pipe", Transport.ITEMS, PipeMaterial.IRON, "iron", "iron_output"),
    GOLDEN_TRANSPORT("golden_transport_pipe", Transport.ITEMS, PipeMaterial.GOLD, "golden", null),
    VOID_TRANSPORT("void_transport_pipe", Transport.ITEMS, PipeMaterial.VOID, "void", null);

    private final String name;
    private final Transport transport;
    private final PipeMaterial material;
    private final String texture;
    @Nullable
    private final String sideTexture;

    PipeType(String name, Transport transport, PipeMaterial material, String texture, @Nullable String sideTexture) {
        this.name = name;
        this.transport = transport;
        this.material = material;
        this.texture = texture;
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

    /** Texture of the core and the plain arms, also used for the item. */
    public String getTexture() {
        return texture;
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
