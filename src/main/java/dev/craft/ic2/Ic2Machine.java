package dev.craft.ic2;

import dev.craft.ic2.machine.FacingMode;

/**
 * IC2 blocks with a block entity, their sprite sheet column and block-level values ({@code BlockGenerator},
 * {@code BlockMachine}, {@code BlockElectric}).
 */
public enum Ic2Machine {
    GENERATOR("generator", Sheet.GENERATOR, 0, 3.0F, FacingMode.HORIZONTAL, 0.9F, Drop.GENERATOR),
    GEOTHERMAL_GENERATOR("geothermal_generator", Sheet.GENERATOR, 1, 3.0F, FacingMode.HORIZONTAL, 0.9F, Drop.GENERATOR),
    WATER_MILL("water_mill", Sheet.GENERATOR, 2, 3.0F, FacingMode.HORIZONTAL, 0.9F, Drop.SELF),
    SOLAR_PANEL("solar_panel", Sheet.GENERATOR, 3, 3.0F, FacingMode.HORIZONTAL, 0.9F, Drop.GENERATOR),
    WIND_MILL("wind_mill", Sheet.GENERATOR, 4, 3.0F, FacingMode.HORIZONTAL, 0.9F, Drop.GENERATOR),
    IRON_FURNACE("iron_furnace", Sheet.MACHINE, 1, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.SELF),
    ELECTRIC_FURNACE("electric_furnace", Sheet.MACHINE, 2, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.SELF),
    MACERATOR("macerator", Sheet.MACHINE, 3, 2.0F, FacingMode.HORIZONTAL, 0.85F, Drop.MACHINE_CASING),
    EXTRACTOR("extractor", Sheet.MACHINE, 4, 2.0F, FacingMode.HORIZONTAL, 0.85F, Drop.MACHINE_CASING),
    COMPRESSOR("compressor", Sheet.MACHINE, 5, 2.0F, FacingMode.HORIZONTAL, 0.85F, Drop.MACHINE_CASING),
    RECYCLER("recycler", Sheet.MACHINE, 11, 2.0F, FacingMode.HORIZONTAL, 0.85F, Drop.MACHINE_CASING),
    BATBOX("batbox", Sheet.ELECTRIC, 0, 1.5F, FacingMode.ALL, 1.0F, Drop.SELF);

    private final String name;
    private final Sheet sheet;
    private final int column;
    private final float hardness;
    private final FacingMode facingMode;
    private final float wrenchDropRate;
    private final Drop drop;

    Ic2Machine(String name, Sheet sheet, int column, float hardness, FacingMode facingMode, float wrenchDropRate,
               Drop drop) {
        this.name = name;
        this.sheet = sheet;
        this.column = column;
        this.hardness = hardness;
        this.facingMode = facingMode;
        this.wrenchDropRate = wrenchDropRate;
        this.drop = drop;
    }

    public String getName() {
        return name;
    }

    public Sheet getSheet() {
        return sheet;
    }

    /** Column in the sprite sheet (= IC2 metadata). */
    public int getColumn() {
        return column;
    }

    public float getHardness() {
        return hardness;
    }

    public FacingMode getFacingMode() {
        return facingMode;
    }

    public float getWrenchDropRate() {
        return wrenchDropRate;
    }

    public Drop getDrop() {
        return drop;
    }

    /** IC2 sprite sheets: 16 columns (metadata), rows 0–5 inactive, 6–11 active. */
    public enum Sheet {
        GENERATOR("block_generator"),
        MACHINE("block_machine"),
        ELECTRIC("block_electric");

        private final String file;

        Sheet(String file) {
            this.file = file;
        }

        public String getFile() {
            return file;
        }
    }

    /** What the block drops when broken without a wrench. */
    public enum Drop {
        SELF,
        GENERATOR,
        MACHINE_CASING
    }
}
