package dev.craft.compactsolars;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class SolarArrayPatternsTest {
    private static long count(String[] rows, char symbol) {
        return Arrays.stream(rows).flatMapToInt(String::chars).filter(c -> c == symbol).count();
    }

    @Test
    void lvIsEightPanelsAroundTheController() {
        assertEquals(Arrays.asList("PPP", "PLP", "PPP"), Arrays.asList(SolarArrayPatterns.layout(SolarArrayType.LV)));
    }

    @Test
    void mvIsEightLvArraysAndAnMvCentre() {
        String[] rows = SolarArrayPatterns.layout(SolarArrayType.MV);
        assertEquals(9, rows.length);
        assertEquals(72, count(rows, 'P'));
        assertEquals(8, count(rows, 'L'));
        assertEquals(1, count(rows, 'M'));
        assertEquals('M', rows[4].charAt(4));
        assertEquals('L', rows[1].charAt(1));
        assertEquals('L', rows[7].charAt(4));
    }

    @Test
    void hvIsEightMvArraysAndAnHvCentre() {
        String[] rows = SolarArrayPatterns.layout(SolarArrayType.HV);
        assertEquals(27, rows.length);
        assertEquals(27, rows[0].length());
        assertEquals(648, count(rows, 'P'));
        assertEquals(72, count(rows, 'L'));
        assertEquals(8, count(rows, 'M'));
        assertEquals(1, count(rows, 'H'));
        assertEquals('H', rows[13].charAt(13));
        assertEquals('M', rows[4].charAt(13));
        assertEquals('L', rows[13].charAt(10));
    }

    @Test
    void radii() {
        assertEquals(1, SolarArrayType.LV.getRadius());
        assertEquals(4, SolarArrayType.MV.getRadius());
        assertEquals(13, SolarArrayType.HV.getRadius());
    }
}
