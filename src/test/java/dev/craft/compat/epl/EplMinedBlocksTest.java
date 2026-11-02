package dev.craft.compat.epl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.junit.jupiter.api.Test;

class EplMinedBlocksTest {
    @Test
    void insertsAfterDistance() {
        List<String> defaults = List.of("playtime", "deaths", "distance", "jumps", "dmgDealt", "dmgTaken", "lastSeen");
        assertEquals(List.of("playtime", "deaths", "distance", "blocksMined", "jumps", "dmgDealt", "dmgTaken",
                "lastSeen"), EplMinedBlocks.withMinedBlocks(defaults));
    }

    @Test
    void appendsWithoutDistanceAndKeepsExisting() {
        assertEquals(List.of("deaths", "blocksMined"), EplMinedBlocks.withMinedBlocks(List.of("deaths")));
        List<String> already = List.of("blocksMined", "deaths");
        assertSame(already, EplMinedBlocks.withMinedBlocks(already));
    }

    @Test
    void formatsNumbers() {
        assertEquals("0", EplMinedBlocks.format(0, false));
        assertEquals("12,345", EplMinedBlocks.format(12_345, false));
        assertEquals("999", EplMinedBlocks.format(999, true));
        assertEquals("12.3k", EplMinedBlocks.format(12_345, true));
        assertEquals("2.5M", EplMinedBlocks.format(2_500_000, true));
    }
}
