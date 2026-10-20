package dev.craft.compactsolars;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class SolarArrayPatternsTest {
    @Test
    void everyTierIsARingAroundItsCentre() {
        assertEquals(Arrays.asList("RRR", "RLR", "RRR"), Arrays.asList(SolarArrayPatterns.layout(SolarArrayType.LV)));
        assertEquals(Arrays.asList("RRR", "RMR", "RRR"), Arrays.asList(SolarArrayPatterns.layout(SolarArrayType.MV)));
        assertEquals(Arrays.asList("RRR", "RHR", "RRR"), Arrays.asList(SolarArrayPatterns.layout(SolarArrayType.HV)));
    }

    @Test
    void baseValuesAreThePanelsInside() {
        assertEquals(8, SolarArrayType.LV.getBaseValue());
        assertEquals(64, SolarArrayType.MV.getBaseValue());
        assertEquals(512, SolarArrayType.HV.getBaseValue());
    }

    @Test
    void outputIsTheSunnyValueTimesTheCentresBonusRoundedDown() {
        assertEquals(19, SolarArrayType.LV.output(8 + 8));
        assertEquals(179, SolarArrayType.MV.output(64 + 8 * 8));
        assertEquals(1536, SolarArrayType.HV.output(512 + 8 * 64));
        assertEquals(1, SolarArrayType.LV.output(1));
        assertEquals(0, SolarArrayType.HV.output(0));
    }
}
