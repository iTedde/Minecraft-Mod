package dev.craft.gregtech.world;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.Nullable;

/**
 * The twelve GregTech ores ({@code GT_BlockMetaID_Ore} meta 1–12, spec 07 §6.2): hardness, harvest level, experience,
 * IC2 ore value and the stone they are generated in.
 */
public enum GtOre {
    GALENA("galena", "Galena Ore", 3.0F, ToolLevel.STONE, null, 4, Ground.STONE, true),
    IRIDIUM("iridium_ore_block", "iridium", "Iridium Ore", 20.0F, ToolLevel.DIAMOND, UniformInt.of(30, 50), 10,
            Ground.STONE, true),
    RUBY("ruby", "Ruby Ore", 4.0F, ToolLevel.IRON, UniformInt.of(3, 7), 4, Ground.STONE, true),
    SAPPHIRE("sapphire", "Sapphire Ore", 4.0F, ToolLevel.IRON, UniformInt.of(3, 7), 4, Ground.STONE, true),
    BAUXITE("bauxite", "Bauxite Ore", 3.0F, ToolLevel.STONE, null, 2, Ground.STONE, false),
    PYRITE("pyrite", "Pyrite Ore", 2.0F, ToolLevel.STONE, ConstantInt.of(1), 1, Ground.NETHERRACK, false),
    CINNABAR("cinnabar", "Cinnabar Ore", 3.0F, ToolLevel.IRON, UniformInt.of(3, 5), 3, Ground.NETHERRACK, false),
    SPHALERITE("sphalerite", "Sphalerite Ore", 2.0F, ToolLevel.STONE, ConstantInt.of(1), 2, Ground.NETHERRACK, false),
    TUNGSTATE("tungstate", "Tungstate Ore", 4.0F, ToolLevel.IRON, null, 4, Ground.END_STONE, false),
    SHELDONITE("sheldonite", "Sheldonite Ore", 3.5F, ToolLevel.DIAMOND, null, 10, Ground.END_STONE, false),
    OLIVINE("olivine", "Olivine Ore", 3.0F, ToolLevel.DIAMOND, null, 4, Ground.END_STONE, false),
    SODALITE("sodalite", "Sodalite Ore", 3.0F, ToolLevel.IRON, null, 3, Ground.END_STONE, false);

    /** {@code setResistance(10)} in 1.4.7 = explosion resistance 6. */
    public static final float RESISTANCE = 6.0F;
    /** Vanilla deepslate ores are 1.5 harder than their stone variant. */
    public static final float DEEPSLATE_EXTRA_HARDNESS = 1.5F;

    private final String blockName;
    private final String material;
    private final String englishName;
    private final float hardness;
    private final ToolLevel toolLevel;
    @Nullable
    private final IntProvider experience;
    private final int oreValue;
    private final Ground ground;
    private final boolean deepslate;

    GtOre(String material, String englishName, float hardness, ToolLevel toolLevel, @Nullable IntProvider experience,
          int oreValue, Ground ground, boolean deepslate) {
        this(material + "_ore", material, englishName, hardness, toolLevel, experience, oreValue, ground, deepslate);
    }

    GtOre(String blockName, String material, String englishName, float hardness, ToolLevel toolLevel,
          @Nullable IntProvider experience, int oreValue, Ground ground, boolean deepslate) {
        this.blockName = blockName;
        this.material = material;
        this.englishName = englishName;
        this.hardness = hardness;
        this.toolLevel = toolLevel;
        this.experience = experience;
        this.oreValue = oreValue;
        this.ground = ground;
        this.deepslate = deepslate;
    }

    /** Registry name; the iridium ore block is {@code iridium_ore_block} because IC2's iridium ore item is {@code iridium_ore}. */
    public String getBlockName() {
        return blockName;
    }

    public String getDeepslateBlockName() {
        return "deepslate_" + blockName;
    }

    /** Material name as in the {@code c:ores/<material>} tag and the texture names. */
    public String getMaterial() {
        return material;
    }

    public String getEnglishName() {
        return englishName;
    }

    public float getHardness() {
        return hardness;
    }

    public ToolLevel getToolLevel() {
        return toolLevel;
    }

    /** Experience on normal mining, {@code null} for ores that drop themselves. */
    @Nullable
    public IntProvider getExperience() {
        return experience;
    }

    /** {@code addValuableOre} value for scanners and the miner. */
    public int getOreValue() {
        return oreValue;
    }

    public Ground getGround() {
        return ground;
    }

    /** Whether a deepslate variant exists (overworld ores whose height range reaches the deepslate layer). */
    public boolean hasDeepslateVariant() {
        return deepslate;
    }

    /** Forge harvest level 1/2/3. */
    public enum ToolLevel {
        STONE, IRON, DIAMOND
    }

    /** Background of the texture and stone the ore replaces. */
    public enum Ground {
        STONE, NETHERRACK, END_STONE
    }
}
