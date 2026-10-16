package dev.craft.ic2.terraformer;

import net.minecraft.server.level.ServerLevel;

/** IC2 {@code ITerraformingBP}: energy per successful step, search range and the step itself. */
public interface TerraformerBlueprint {
    int getConsume();

    int getRange();

    /** One attempt at column (x, z); {@code y} is the terraformer's height. */
    boolean terraform(ServerLevel level, int x, int z, int y);
}
