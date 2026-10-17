package dev.craft.ic2;

import dev.craft.ic2.machine.FacingMode;
import net.minecraft.world.item.Rarity;

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
    NUCLEAR_REACTOR("nuclear_reactor", Sheet.GENERATOR, 5, 3.0F, FacingMode.HORIZONTAL, 0.8F, Drop.GENERATOR,
            Rarity.UNCOMMON, true),
    IRON_FURNACE("iron_furnace", Sheet.MACHINE, 1, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.SELF),
    ELECTRIC_FURNACE("electric_furnace", Sheet.MACHINE, 2, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.SELF),
    MACERATOR("macerator", Sheet.MACHINE, 3, 2.0F, FacingMode.HORIZONTAL, 0.85F, Drop.MACHINE_CASING),
    EXTRACTOR("extractor", Sheet.MACHINE, 4, 2.0F, FacingMode.HORIZONTAL, 0.85F, Drop.MACHINE_CASING),
    COMPRESSOR("compressor", Sheet.MACHINE, 5, 2.0F, FacingMode.HORIZONTAL, 0.85F, Drop.MACHINE_CASING),
    CANNING_MACHINE("canning_machine", Sheet.MACHINE, 6, 2.0F, FacingMode.HORIZONTAL, 0.85F, Drop.MACHINE_CASING),
    MINER("miner", Sheet.MACHINE, 7, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.MACHINE_CASING),
    PUMP("ic2_pump", Sheet.MACHINE, 8, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.MACHINE_CASING),
    RECYCLER("recycler", Sheet.MACHINE, 11, 2.0F, FacingMode.HORIZONTAL, 0.85F, Drop.MACHINE_CASING),
    MAGNETIZER("magnetizer", Sheet.MACHINE, 9, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.SELF, Rarity.COMMON, false),
    ELECTROLYZER("electrolyzer", Sheet.MACHINE, 10, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.MACHINE_CASING),
    INDUCTION_FURNACE("induction_furnace", Sheet.MACHINE, 13, 2.0F, FacingMode.HORIZONTAL, 0.8F,
            Drop.ADVANCED_MACHINE_CASING, Rarity.UNCOMMON, true),
    MASS_FABRICATOR("mass_fabricator", Sheet.MACHINE, 14, 2.0F, FacingMode.HORIZONTAL, 0.7F,
            Drop.ADVANCED_MACHINE_CASING, Rarity.RARE, true),
    PERSONAL_SAFE("personal_safe", Sheet.PERSONAL, 0, -1.0F, FacingMode.HORIZONTAL, 1.0F, Drop.SELF,
            Rarity.UNCOMMON, true),
    TRADE_O_MAT("trade_o_mat", Sheet.PERSONAL, 1, -1.0F, FacingMode.HORIZONTAL, 1.0F, Drop.SELF),
    ENERGY_O_MAT("energy_o_mat", Sheet.PERSONAL, 2, -1.0F, FacingMode.HORIZONTAL, 1.0F, Drop.SELF),
    TERRAFORMER("terraformer", Sheet.MACHINE, 15, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.ADVANCED_MACHINE_CASING,
            Rarity.UNCOMMON, false),
    BATBOX("batbox", Sheet.ELECTRIC, 0, 1.5F, FacingMode.ALL, 1.0F, Drop.SELF),
    MFE("mfe", Sheet.ELECTRIC, 1, 1.5F, FacingMode.ALL, 1.0F, Drop.MACHINE_CASING),
    MFSU("mfsu", Sheet.ELECTRIC, 2, 1.5F, FacingMode.ALL, 1.0F, Drop.MACHINE_CASING, Rarity.UNCOMMON, true),
    LV_TRANSFORMER("lv_transformer", Sheet.ELECTRIC, 3, 1.5F, FacingMode.ALL, 1.0F, Drop.SELF, Rarity.COMMON, false),
    MV_TRANSFORMER("mv_transformer", Sheet.ELECTRIC, 4, 1.5F, FacingMode.ALL, 1.0F, Drop.MACHINE_CASING,
            Rarity.COMMON, false),
    HV_TRANSFORMER("hv_transformer", Sheet.ELECTRIC, 5, 1.5F, FacingMode.ALL, 1.0F, Drop.MACHINE_CASING,
            Rarity.UNCOMMON, false),
    TELEPORTER("teleporter", Sheet.MACHINE2, 0, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.ADVANCED_MACHINE_CASING,
            Rarity.RARE, false),
    TESLA_COIL("tesla_coil", Sheet.MACHINE2, 1, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.MACHINE_CASING,
            Rarity.COMMON, false),
    CROP_MATRON("crop_matron", Sheet.MACHINE2, 2, 2.0F, FacingMode.HORIZONTAL, 1.0F, Drop.MACHINE_CASING);

    private final String name;
    private final Sheet sheet;
    private final int column;
    private final float hardness;
    private final FacingMode facingMode;
    private final float wrenchDropRate;
    private final Drop drop;
    private final Rarity rarity;
    private final boolean hasMenu;

    Ic2Machine(String name, Sheet sheet, int column, float hardness, FacingMode facingMode, float wrenchDropRate,
               Drop drop) {
        this(name, sheet, column, hardness, facingMode, wrenchDropRate, drop, Rarity.COMMON, true);
    }

    Ic2Machine(String name, Sheet sheet, int column, float hardness, FacingMode facingMode, float wrenchDropRate,
               Drop drop, Rarity rarity, boolean hasMenu) {
        this.name = name;
        this.sheet = sheet;
        this.column = column;
        this.hardness = hardness;
        this.facingMode = facingMode;
        this.wrenchDropRate = wrenchDropRate;
        this.drop = drop;
        this.rarity = rarity;
        this.hasMenu = hasMenu;
    }

    public Rarity getRarity() {
        return rarity;
    }

    /**
     * Transformers, the magnetizer, the teleporter and the tesla coil have neither an inventory nor a GUI; the
     * terraformer has a blueprint slot but no GUI.
     */
    public boolean hasMenu() {
        return hasMenu;
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

    /** −1: unbreakable (the personal blocks, only the owner's wrench removes them). */
    public float getHardness() {
        return hardness;
    }

    public boolean isPersonal() {
        return sheet == Sheet.PERSONAL;
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
        MACHINE2("block_machine2"),
        PERSONAL("block_personal"),
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
        MACHINE_CASING,
        ADVANCED_MACHINE_CASING
    }
}
