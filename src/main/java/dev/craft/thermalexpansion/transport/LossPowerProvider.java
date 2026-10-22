package dev.craft.thermalexpansion.transport;

/**
 * CoFH {@code IPowerProviderLoss}: a buffer that loses part of what it receives (energy conduit networks, energy
 * tesseracts). TE engines push at least {@code 25 · minPower} per stroke into it.
 */
public interface LossPowerProvider {
}
