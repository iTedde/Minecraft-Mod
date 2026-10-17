package dev.craft.ic2.crop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Crop registry (IC2 {@code CropCard} statics, {@code IC2Crops}, {@code Crops}): 256 crop ids, base seeds and the
 * biome bonuses for humidity and nutrients.
 */
public final class Crops {
    public static final int MAX_CROPS = 256;

    private static final CropCard[] CROPS = new CropCard[MAX_CROPS];
    private static final Map<Item, BaseSeed> BASE_SEEDS = new LinkedHashMap<>();
    private static final Map<ResourceKey<Biome>, Integer> HUMIDITY_BONUS = new HashMap<>();
    private static final Map<ResourceKey<Biome>, Integer> NUTRIENT_BONUS = new HashMap<>();

    public static final CropCard WEED = register(new WeedCrop(), 0);
    public static final CropCard WHEAT = register(new WheatCrop(), 1);

    static {
        registerBaseSeed(Items.WHEAT_SEEDS, WHEAT, 1, 1, 1, 1, 1);

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
