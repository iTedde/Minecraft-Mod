package dev.craft.buildcraft.power;

import dev.craft.Craft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;

public final class BcCapabilities {
    /** MJ receiver on a side; replaces BuildCraft's {@code instanceof IPowerReceptor} checks. */
    public static final BlockCapability<PowerReceptor, Direction> POWER_RECEPTOR = BlockCapability.createSided(
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "power_receptor"), PowerReceptor.class);

    private BcCapabilities() {
    }
}
