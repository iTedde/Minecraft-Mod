package ivorius.psychedelicraft.world.gen.loot;

/**
 * Upstream merged the pools of {@code psychedelicraftmc:chests/...} into the matching vanilla chest tables through the
 * Fabric loot events. On NeoForge this is done by {@code neoforge:add_table} global loot modifiers
 * ({@code data/psychedelicraft/loot_modifiers}).
 */
public interface PSLootTableEntryType {
    static void bootstrap() {
    }
}
