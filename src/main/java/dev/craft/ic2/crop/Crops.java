package dev.craft.ic2.crop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.mojang.logging.LogUtils;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Crop registry (IC2 {@code CropCard} statics, {@code IC2Crops}, {@code Crops}): 256 crop ids, base seeds and the
 * biome bonuses for humidity and nutrients. Only touched at runtime, after item registration.
 */
public final class Crops {
    public static final int MAX_CROPS = 256;

    private static final CropCard[] CROPS = new CropCard[MAX_CROPS];
    private static final Map<Item, BaseSeed> BASE_SEEDS = new LinkedHashMap<>();
    private static final Map<ResourceKey<Biome>, Integer> HUMIDITY_BONUS = new HashMap<>();
    private static final Map<ResourceKey<Biome>, Integer> NUTRIENT_BONUS = new HashMap<>();

    public static final CropCard WEED = register(new WeedCrop(), 0);
    public static final CropCard WHEAT = register(new WheatCrop(), 1);
    public static final CropCard PUMPKIN = register(new PumpkinCrop(), 2);
    public static final CropCard MELON = register(new MelonCrop(), 3);
    public static final CropCard DANDELION = register(new ColorFlowerCrop("dandelion", "Notch",
            new String[]{"Yellow", "Flower"}, 15, Items.YELLOW_DYE), 4);
    public static final CropCard ROSE = register(new ColorFlowerCrop("rose", "Notch",
            new String[]{"Red", "Flower", "Rose"}, 21, Items.RED_DYE), 5);
    public static final CropCard BLACKTHORN = register(new ColorFlowerCrop("blackthorn", "Alblaka",
            new String[]{"Black", "Flower", "Rose"}, 22, Items.INK_SAC), 6);
    public static final CropCard TULIP = register(new ColorFlowerCrop("tulip", "Alblaka",
            new String[]{"Purple", "Flower", "Tulip"}, 23, Items.PURPLE_DYE), 7);
    public static final CropCard CYAZINT = register(new ColorFlowerCrop("cyazint", "Alblaka",
            new String[]{"Blue", "Flower"}, 24, Items.CYAN_DYE), 8);
    public static final CropCard VENOMILIA = register(new VenomiliaCrop(), 9);
    public static final CropCard REED = register(new ReedCrop(), 10);
    public static final CropCard STICKREED = register(new StickreedCrop(), 11);
    public static final CropCard COCOA = register(new CocoaCrop(), 12);
    // IC2 checks the single ore block; 1.21 split it into stone and deepslate ore.
    public static final CropCard FERRU = register(new MetalCrop("ferru", 6, 1, new String[]{"Gray", "Leaves", "Metal"},
            35, new Block[]{Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE}, Ic2Items.SMALL_IRON_DUST, 2.0F, 800, 2000),
            13);
    public static final CropCard AURELIA = register(new MetalCrop("aurelia", 8, 2,
            new String[]{"Gold", "Leaves", "Metal"}, 36, new Block[]{Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE},
            () -> Items.GOLD_NUGGET, 1.0F, 1000, 2200), 14);
    public static final CropCard REDWHEAT = register(new RedwheatCrop(), 15);
    public static final CropCard NETHER_WART = register(new WartCrop("nether_wart", "Notch",
            new int[]{4, 2, 0, 2, 1}, new String[]{"Red", "Nether", "Ingredient", "Soulsand"}, false,
            () -> Blocks.SOUL_SAND, () -> Blocks.SNOW_BLOCK, () -> Crops.TERRA_WART, () -> Items.NETHER_WART, 2.0F),
            16);
    public static final CropCard TERRA_WART = register(new WartCrop("terra_wart", "Alblaka",
            new int[]{2, 4, 0, 3, 0}, new String[]{"Blue", "Aether", "Consumable", "Snow"}, true,
            () -> Blocks.SNOW_BLOCK, () -> Blocks.SOUL_SAND, () -> Crops.NETHER_WART, Ic2Items.TERRA_WART, 0.8F), 17);
    public static final CropCard COFFEE = register(new CoffeeCrop(), 18);
    public static final CropCard HOPS = register(new HopsCrop(), 19);
    public static final CropCard CARROTS = register(new SeedFoodCrop("carrots", 50, "Orange", "Carrots",
            Items.CARROT, false), 20);
    public static final CropCard POTATO = register(new SeedFoodCrop("potato", 51, "Yellow", "Potato",
            Items.POTATO, true), 21);

    static {
        registerBaseSeed(Items.WHEAT_SEEDS, WHEAT, 1, 1, 1, 1, 1);
        registerBaseSeed(Items.PUMPKIN_SEEDS, PUMPKIN, 1, 1, 1, 1, 1);
        registerBaseSeed(Items.MELON_SEEDS, MELON, 1, 1, 1, 1, 1);
        registerBaseSeed(Items.NETHER_WART, NETHER_WART, 1, 1, 1, 1, 1);
        registerBaseSeed(Ic2Items.TERRA_WART.get(), TERRA_WART, 1, 1, 1, 1, 1);
        registerBaseSeed(Ic2Items.COFFEE_BEANS.get(), COFFEE, 1, 1, 1, 1, 1);
        registerBaseSeed(Items.SUGAR_CANE, REED, 1, 3, 0, 2, 1);
        registerBaseSeed(Items.COCOA_BEANS, COCOA, 1, 0, 0, 0, 1);
        // IC2's rose is the 1.21 poppy.
        registerBaseSeed(Items.POPPY, ROSE, 4, 1, 1, 1, 4);
        registerBaseSeed(Items.DANDELION, DANDELION, 4, 1, 1, 1, 4);
        registerBaseSeed(Items.CARROT, CARROTS, 1, 1, 1, 1, 1);
        registerBaseSeed(Items.POTATO, POTATO, 1, 1, 1, 1, 1);

        // IC2 registers forest hills, jungle hills, desert hills and mushroom shore as well; 1.21 merged them into
        // the base biomes.
        addBiomeBonus(Biomes.RIVER, 2, 0);
        addBiomeBonus(Biomes.SWAMP, 2, 2);
        addBiomeBonus(Biomes.FOREST, 1, 1);
        addBiomeBonus(Biomes.JUNGLE, 1, 2);
        addBiomeBonus(Biomes.DESERT, -1, 0);
        addBiomeBonus(Biomes.MUSHROOM_FIELDS, 0, 2);
    }

    private Crops() {
    }

    /** Base seed: planting {@code stackSize} of this item starts the crop at the given size and stats. */
    public record BaseSeed(CropCard crop, int size, int growth, int gain, int resistance, int stackSize) {
    }

    private static CropCard register(CropCard crop, int id) {
        if (CROPS[id] != null) {
            LogUtils.getLogger().error("Cannot add crop {} on id #{}, slot already occupied by crop {}", crop.name(),
                    id, CROPS[id].name());
            return crop;
        }
        CROPS[id] = crop;
        crop.setId(id);
        return crop;
    }

    private static void registerBaseSeed(Item item, CropCard crop, int size, int growth, int gain, int resistance,
            int stackSize) {
        BASE_SEEDS.putIfAbsent(item, new BaseSeed(crop, size, growth, gain, resistance, stackSize));
    }

    private static void addBiomeBonus(ResourceKey<Biome> biome, int humidity, int nutrients) {
        HUMIDITY_BONUS.put(biome, humidity);
        NUTRIENT_BONUS.put(biome, nutrients);
    }

    /** IC2 {@code getCrop}: unknown ids fall back to the weed. */
    public static CropCard get(int id) {
        if (id < 0 || id >= MAX_CROPS || CROPS[id] == null) {
            return CROPS[0];
        }
        return CROPS[id];
    }

    public static boolean exists(int id) {
        return id >= 0 && id < MAX_CROPS && CROPS[id] != null;
    }

    public static List<CropCard> all() {
        List<CropCard> crops = new ArrayList<>();
        for (CropCard crop : CROPS) {
            if (crop != null) {
                crops.add(crop);
            }
        }
        return crops;
    }

    @Nullable
    public static BaseSeed getBaseSeed(ItemStack stack) {
        return stack.isEmpty() ? null : BASE_SEEDS.get(stack.getItem());
    }

    public static Map<Item, BaseSeed> baseSeeds() {
        return Collections.unmodifiableMap(BASE_SEEDS);
    }

    public static int getHumidityBiomeBonus(Holder<Biome> biome) {
        return biome.unwrapKey().map(key -> HUMIDITY_BONUS.getOrDefault(key, 0)).orElse(0);
    }

    public static int getNutrientBiomeBonus(Holder<Biome> biome) {
        return biome.unwrapKey().map(key -> NUTRIENT_BONUS.getOrDefault(key, 0)).orElse(0);
    }
}
