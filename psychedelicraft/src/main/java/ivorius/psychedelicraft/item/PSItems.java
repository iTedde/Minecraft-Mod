/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.List;

import org.joml.Vector3f;


import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.*;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.influence.DelayType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.ConsumableFluid;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.container.FluidCauldronBehavior;
import ivorius.psychedelicraft.item.component.BagContentsComponent;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemDrugs;
import ivorius.psychedelicraft.item.component.PSComponents;
import ivorius.psychedelicraft.item.component.RiftFractionComponent;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.CommonColors;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * Created by lukas on 25.04.14.
 * Updated by Sollace on 1 Jan 2023
 */
public interface PSItems {
    DrinkableItem WOODEN_MUG = register("wooden_mug", new DrinkableItem(new Properties()
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.MUG)), FluidVolumes.GULP, DrinkableItem.DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.DRINK));
    DrinkableItem STONE_CUP = register("stone_cup", new DrinkableItem(new Properties()
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.CUP)), FluidVolumes.GULP, DrinkableItem.DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.DRINK));
    DrinkableItem GLASS_CHALICE = register("glass_chalice", new DrinkableItem(new Properties()
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.CHALLICE)), FluidVolumes.GULP, DrinkableItem.DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.DRINK));
    DrinkableItem SHOT_GLASS = register("shot_glass", new DrinkableItem(new Properties()
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.SHOT)), FluidVolumes.GULP, DrinkableItem.DEFAULT_MAX_USE_TIME / 4, ConsumableFluid.ConsumptionType.DRINK));
    DrinkableItem BOTTLE = register("bottle", new DrinkableItem(new Properties()
            .component(DataComponents.DYED_COLOR, new DyedItemColor(CommonColors.WHITE, true))
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BOTTLE)), FluidVolumes.GULP, DrinkableItem.DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.DRINK));
    MolotovCocktailItem MOLOTOV_COCKTAIL = register("molotov_cocktail", new MolotovCocktailItem(new Properties()
            .component(DataComponents.DYED_COLOR, new DyedItemColor(CommonColors.WHITE, true))
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BOTTLE))
            .stacksTo(16)));

    List<FlaskItem> ALL_BARRELS = PSBlocks.ALL_BARRELS.stream().map(block -> register(BuiltInRegistries.BLOCK.getKey(block).getPath(), barrel(block))).toList();

    FlaskItem MASH_TUB = register("mash_tub", new MashTubItem(PSBlocks.MASH_TUB, new Properties()
            .stacksTo(16)
            .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.FERMENT)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.VAT))));
    FlaskItem FLASK = register("flask", new FlaskItem(PSBlocks.FLASK, new Properties()
            .stacksTo(16)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.FLASK))));
    FlaskItem DISTILLERY = register("distillery", new FlaskItem(PSBlocks.DISTILLERY, new Properties()
            .stacksTo(16)
            .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.DISTILL)
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.FLASK))));
    RiftJarItem RIFT_JAR = register("rift_jar", new RiftJarItem(PSBlocks.RIFT_JAR, new Properties()
            .component(PSComponents.RIFT_FRACTION, RiftFractionComponent.DEFAULT)));

    DrinkableItem FILLED_GLASS_BOTTLE = register("filled_glass_bottle", new ProxyDrinkableItem(Items.GLASS_BOTTLE, new Properties().component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.GLASS_BOTTLE)), FluidVolumes.GLASS_BOTTLE, ConsumableFluid.ConsumptionType.DRINK));
    FilledBucketItem FILLED_BUCKET = register("filled_bucket", new FilledBucketItem(new Properties().stacksTo(1).component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BUCKET))));
    DrinkableItem FILLED_BOWL = register("filled_bowl", new ProxyDrinkableItem(Items.BOWL, new Properties().component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BOWL)), FluidVolumes.BOWL, ConsumableFluid.ConsumptionType.DRINK));

    Item WINE_GRAPES = register("wine_grapes", new WineGrapesItem(new Properties().food(
            new FoodProperties.Builder().nutrition(1).saturationModifier(0.5F).fast().build()
    ), 15));

    Item CANNABIS_SEEDS = register("cannabis_seeds", new ItemNameBlockItem(PSBlocks.CANNABIS, new Properties()));
    Item CANNABIS_LEAF = register("cannabis_leaf");
    Item CANNABIS_BUDS = register("cannabis_buds");
    Item DRIED_CANNABIS_LEAF = register("dried_cannabis_leaf");
    Item DRIED_CANNABIS_BUDS = register("dried_cannabis_buds");

    Item HASH_MUFFIN = register("hash_muffin", new EdibleItem(new Properties()
            .food(EdibleItem.HASH_MUFFIN)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.CANNABIS, DelayType.METABOLISED, 0.004, 0.002, 0.7f)))
    ));

    Item HOP_CONES = register("hop_cones");
    Item HOP_SEEDS = register("hop_seeds", new ItemNameBlockItem(PSBlocks.HOP, new Properties()));

    Item BROWN_MAGIC_MUSHROOMS = register("brown_magic_mushrooms", new EdibleItem(new Properties()
            .food(EdibleItem.NON_FILLING_EDIBLE)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.BROWN_SHROOMS, DelayType.INGESTED, 0.005, 0.003, 0.25f)))
    ));
    Item RED_MAGIC_MUSHROOMS = register("red_magic_mushrooms", new EdibleItem(new Properties()
            .food(EdibleItem.NON_FILLING_EDIBLE)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.RED_SHROOMS, DelayType.INGESTED, 0.005, 0.003, 0.25f)))
    ));

    Item JOLLY_RANCHER = register("jolly_rancher", new JollyRancherItem(
            new Properties()
            .food(EdibleItem.NON_FILLING_EDIBLE)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.SUGAR, DelayType.INGESTED, 0.005, 0.003, 0.05f)))
    ));

    Item TOBACCO_LEAVES = register("tobacco");
    Item TOBACCO_SEEDS = register("tobacco_seeds", new ItemNameBlockItem(PSBlocks.TOBACCO, new Properties()));
    Item DRIED_TOBACCO = register("dried_tobacco");
    Item DRIED_POPPY = register("dried_poppy");

    SmokeableItem CIGARETTE = register("cigarette", new SmokeableItem(2, new Properties().stacksTo(1).durability(1)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.TOBACCO, DelayType.IMMEDIATE, 0.1, 0.02, 0.7F)).withSmoke(ItemDrugs.DEFAULT_SMOKE_COLOR))
    ));
    SmokeableItem CIGAR = register("cigar", new SmokeableItem(4, new Properties().stacksTo(1).durability(3)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.TOBACCO, DelayType.IMMEDIATE, 0.1, 0.02, 0.7F)).withSmoke(new Vector3f(0.6F, 0.6F, 0.5F)))
    ));
    SmokeableItem JOINT = register("joint", new SmokeableItem(2, new Properties().stacksTo(1).durability(2)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.CANNABIS, DelayType.INHALED, 0.002, 0.001, 0.20F)).withSmoke(new Vector3f(0.9F, 0.9F, 0.9F)))
    ));
    SmokeableItem BLUNT = register("blunt", new SmokeableItem(4, new Properties().stacksTo(1).durability(3)
            .component(PSComponents.DRUGS, ItemDrugs.of(
                    new DrugInfluence(DrugType.TOBACCO, DelayType.IMMEDIATE, 0.1, 0.02, 0.7F),
                    new DrugInfluence(DrugType.CANNABIS, DelayType.IMMEDIATE, 0.1, 0.02, 0.7F)
            ).withSmoke(new Vector3f(0.6F, 0.6F, 0.5F)))
    ));

    Item COCA_SEEDS = register("coca_seeds", new ItemNameBlockItem(PSBlocks.COCA, new Properties()));
    Item COCA_LEAVES = register("coca_leaves");
    Item DRIED_COCA_LEAVES = register("dried_coca_leaves");
    Item COCAINE_POWDER = register("cocaine_powder", new SnortableItem(new Properties()
            .food(EdibleItem.NON_FILLING_EDIBLE)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.COCAINE, DelayType.IMMEDIATE, 0.002, 0.003, 0.35f)))
    ));
    Item CRACK_COCAINE = register("crack_cocaine", new SnortableItem(new Properties()
            .food(EdibleItem.NON_FILLING_EDIBLE)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.COCAINE, DelayType.IMMEDIATE, 0.002, 0.03, 0.65f)))
    ));

    DrinkableItem SYRINGE = register("syringe", new SyringeItem(new Properties()
            .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(DrinkableItem.FLUID_PER_INJECTION)
    )));

    Item JUNIPER_LEAVES = register("juniper_leaves", PSBlocks.JUNIPER_LEAVES);
    Item FRUITING_JUNIPER_LEAVES = register("fruiting_juniper_leaves", PSBlocks.FRUITING_JUNIPER_LEAVES);
    Item JUNIPER_LOG = register("juniper_log", PSBlocks.JUNIPER_LOG);
    Item JUNIPER_WOOD = register("juniper_wood", PSBlocks.JUNIPER_WOOD);
    Item STRIPPED_JUNIPER_LOG = register("stripped_juniper_log", PSBlocks.STRIPPED_JUNIPER_LOG);
    Item STRIPPED_JUNIPER_WOOD = register("stripped_juniper_wood", PSBlocks.STRIPPED_JUNIPER_WOOD);
    Item JUNIPER_BERRIES = register("juniper_berries", new SpecialFoodItem(
            new Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.5F).fast().build()), 15
    ));
    Item JUNIPER_SAPLING = register("juniper_sapling", PSBlocks.JUNIPER_SAPLING);
    Item JUNIPER_PLANKS = register("juniper_planks", PSBlocks.JUNIPER_PLANKS);
    Item JUNIPER_STAIRS = register("juniper_stairs", PSBlocks.JUNIPER_STAIRS);
    Item JUNIPER_SIGN = register("juniper_sign", new SignItem(new Properties().stacksTo(16), PSBlocks.JUNIPER_SIGN, PSBlocks.JUNIPER_WALL_SIGN));
    Item JUNIPER_DOOR = register("juniper_door", PSBlocks.JUNIPER_DOOR);
    Item JUNIPER_HANGING_SIGN = register("juniper_hanging_sign", new HangingSignItem(PSBlocks.JUNIPER_HANGING_SIGN, PSBlocks.JUNIPER_WALL_HANGING_SIGN, new Properties().stacksTo(16)));
    Item JUNIPER_PRESSURE_PLATE = register("juniper_pressure_plate", PSBlocks.JUNIPER_PRESSURE_PLATE);
    Item JUNIPER_FENCE = register("juniper_fence", PSBlocks.JUNIPER_FENCE);
    Item JUNIPER_TRAPDOOR = register("juniper_trapdoor", PSBlocks.JUNIPER_TRAPDOOR);
    Item JUNIPER_FENCE_GATE = register("juniper_fence_gate", PSBlocks.JUNIPER_FENCE_GATE);
    Item JUNIPER_BUTTON = register("juniper_button", PSBlocks.JUNIPER_BUTTON);
    Item JUNIPER_SLAB = register("juniper_slab", PSBlocks.JUNIPER_SLAB);
    Item JUNIPER_BOAT = register("juniper_boat", new net.minecraft.world.item.BoatItem(false, ivorius.psychedelicraft.entity.PSBoats.juniper(), new Properties().stacksTo(1)));
    Item JUNIPER_CHEST_BOAT = register("juniper_chest_boat", new net.minecraft.world.item.BoatItem(true, ivorius.psychedelicraft.entity.PSBoats.juniper(), new Properties().stacksTo(1)));

    Item COFFEA_CHERRIES = register("coffea_cherries", new ItemNameBlockItem(PSBlocks.COFFEA, new Properties()));
    Item COFFEE_BEANS = register("coffee_beans");

    Item PEYOTE = register("peyote", PSBlocks.PEYOTE);
    Item DRIED_PEYOTE = register("dried_peyote", new EdibleItem(new Properties()
            .food(EdibleItem.NON_FILLING_EDIBLE)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.PEYOTE, DelayType.INGESTED, 0.005, 0.003, 0.5f)))
    ));
    Item PEYOTE_JOINT = register("peyote_joint", new SmokeableItem(2, new Properties().stacksTo(1).durability(2)
            .component(PSComponents.DRUGS, ItemDrugs.of(
                    new DrugInfluence(DrugType.PEYOTE, DelayType.INHALED, 0.003, 0.0015, 0.4f),
                    new DrugInfluence(DrugType.TOBACCO, DelayType.IMMEDIATE, 0.1, 0.02, 0.1f)
            ).withSmoke(new Vector3f(0.5F, 0.9F, 0.4F)))
    ));

    Item LATTICE = register("lattice", PSBlocks.LATTICE);
    Item WINE_GRAPE_LATTICE = register("wine_grape_lattice", PSBlocks.WINE_GRAPE_LATTICE);
    Item MORNING_GLORY_LATTICE = register("morning_glory_lattice", PSBlocks.MORNING_GLORY_LATTICE);
    Item BOTTLE_RACK = register("bottle_rack", new StandingAndWallBlockItem(PSBlocks.BOTTLE_RACK, PSBlocks.WALL_BOTTLE_RACK, new Properties(), Direction.DOWN));
    Item DRYING_TABLE = register("drying_table", PSBlocks.DRYING_TABLE);
    Item IRON_DRYING_TABLE = register("iron_drying_table", PSBlocks.IRON_DRYING_TABLE);

    Item HARMONIUM = register("harmonium", new Item(new Properties().component(DataComponents.DYED_COLOR, new DyedItemColor(CommonColors.RED, true))));

    Item OBSIDIAN_BOTTLE = register("obsidian_bottle", new Item(new Properties().stacksTo(16)));
    Item OBSIDIAN_DUST = register("obsidian_dust", new SnortableItem(new Properties()
            .food(EdibleItem.NON_FILLING_EDIBLE)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.BATH_SALTS, DelayType.IMMEDIATE, 0.002, 0.003, 0.35f)))
    ));

    // TODO: https://www.erowid.org/plants/kava/kava.shtml
    //Item KAVA_SEEDS = register("kava_seeds", new Item(new Settings()));
    //Item KAVA_ROOT = register("kava_root", new Item(new Settings()));

    Item MORNING_GLORY = register("morning_glory");
    Item MORNING_GLORY_SEEDS = register("morning_glory_seeds", new ItemNameBlockItem(PSBlocks.MORNING_GLORY, new Properties()));
    Item LSA_SQUARE = register("lsd_square", new EdibleItem(new Properties()
            .food(EdibleItem.NON_FILLING_EDIBLE)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.LSD, DelayType.CONTACT, 0.05, 0.003, 0.1F)))
    ));
    Item LSD_PILL = register("lsd_pill", new EdibleItem(new Properties()
            .food(EdibleItem.NON_FILLING_EDIBLE)
            .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.LSD, DelayType.INGESTED, 0.05, 0.003, 0.6F)))
    ));

    Item JIMSONWEED_SEEDS = register("jimsonweed_seeds", new ItemNameBlockItem(PSBlocks.JIMSONWEED, new Properties()));
    Item JIMSONWEED_SEED_POD = register("jimsonweed_seed_pod");
    Item JIMSONWEED_LEAF = register("jimsonweed_leaf");
    Item DRIED_JIMSONWEED_LEAF = register("dried_jimsonweed_leaf");

    Item TOMATO_SEEDS = register("tomato_seeds", new ItemNameBlockItem(PSBlocks.TOMATOES, new Properties()));
    Item TOMATO = register("tomato", new Item(new Properties().food(EdibleItem.TOMATO)));
    Item TOMATO_LEAF = register("tomato_leaf");

    Item BELLADONNA_SEEDS = register("belladonna_seeds", new ItemNameBlockItem(PSBlocks.BELLADONNA, new Properties()));
    Item BELLADONNA_LEAF = register("belladonna_leaf");
    Item DRIED_BELLADONNA_LEAF = register("dried_belladonna_leaf");
    Item BELLADONNA_BERRIES = register("belladonna_berries", new EdibleItem(new Properties()
                .food(new FoodProperties.Builder().nutrition(1).saturationModifier(1.5F).alwaysEdible().build())
                .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.ATROPINE, DelayType.INGESTED, 0.005, 0.003, 0.5f)))
    ));

    Item AGAVE_LEAF = register("agave_leaf", new ItemNameBlockItem(PSBlocks.AGAVE_PLANT, new Properties()));

    BongItem SMOKING_PIPE = register("smoking_pipe", new BongItem(new Properties().durability(50)))
            .consumes(new BongItem.Consumable(DRIED_CANNABIS_BUDS.getDefaultInstance(), new DrugInfluence(DrugType.CANNABIS, DelayType.INHALED, 0.002, 0.001, 0.25F)))
            .consumes(new BongItem.Consumable(DRIED_TOBACCO.getDefaultInstance(), new DrugInfluence(DrugType.TOBACCO, DelayType.INHALED, 0.1, 0.02, 0.8F)))
            .consumes(new BongItem.Consumable(DRIED_POPPY.getDefaultInstance(), new DrugInfluence(DrugType.MORPHINE, DelayType.INHALED, 0.1, 0.02, 0.8F)))
            .consumes(new BongItem.Consumable(DRIED_BELLADONNA_LEAF.getDefaultInstance(), new DrugInfluence(DrugType.ATROPINE, DelayType.INHALED, 0.4, 0.1, 0.9F)))
            .consumes(new BongItem.Consumable(DRIED_JIMSONWEED_LEAF.getDefaultInstance(), new DrugInfluence(DrugType.ATROPINE, DelayType.INHALED, 0.5, 0.1, 0.2F)))
            .consumes(new BongItem.Consumable(HARMONIUM.getDefaultInstance(), stack -> new DrugInfluence(DrugType.HARMONIUM, DelayType.INHALED, 0.04, 0.01, 0.65F, MathUtils.unpackRgb(DyedItemColor.getOrDefault(stack, CommonColors.WHITE)))));
    // TODO: Play around with the bongs benefits
    BongItem BONG = register("bong", new BongItem(new Properties().durability(128)))
            .consumes(new BongItem.Consumable(DRIED_CANNABIS_BUDS.getDefaultInstance(), new DrugInfluence(DrugType.CANNABIS, DelayType.IMMEDIATE, 0.002, 0.001, 0.2F)))
            .consumes(new BongItem.Consumable(DRIED_TOBACCO.getDefaultInstance(), new DrugInfluence(DrugType.TOBACCO, DelayType.IMMEDIATE, 0.1, 0.02, 0.6F)))
            .consumes(new BongItem.Consumable(DRIED_POPPY.getDefaultInstance(), new DrugInfluence(DrugType.MORPHINE, DelayType.IMMEDIATE, 0.1, 0.02, 0.6F)))
            .consumes(new BongItem.Consumable(DRIED_BELLADONNA_LEAF.getDefaultInstance(), new DrugInfluence(DrugType.ATROPINE, DelayType.IMMEDIATE, 0.4, 0.1, 0.4F)))
            .consumes(new BongItem.Consumable(DRIED_JIMSONWEED_LEAF.getDefaultInstance(), new DrugInfluence(DrugType.ATROPINE, DelayType.IMMEDIATE, 0.5, 0.1, 0.1F)))
            .consumes(new BongItem.Consumable(HARMONIUM.getDefaultInstance(), stack -> new DrugInfluence(DrugType.HARMONIUM, DelayType.IMMEDIATE, 0.04, 0.01, 0.9F, MathUtils.unpackRgb(DyedItemColor.getOrDefault(stack, CommonColors.WHITE)))));

    Item VOMIT = register("vomit", new VomitItem(new Properties()));
    Item PAPER_BAG = register("paper_bag", new PaperBagItem(new Properties().component(PSComponents.BAG_CONTENTS, BagContentsComponent.EMPTY)));
    Item BAG_O_VOMIT = register("bag_o_vomit", new SuspiciousItem(new Properties()
            .food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8f).fast().alwaysEdible().build()
    ), SuspiciousItem.createForms(Items.COOKIE, Items.MUSHROOM_STEW, Items.GOLDEN_APPLE, Items.COOKED_BEEF, Items.COOKED_CHICKEN)));

    Item TRAY = register("tray", PSBlocks.TRAY);
    Item BUNSEN_BURNER = register("bunsen_burner", PSBlocks.BUNSEN_BURNER);
    Item GLASS_TUBE = register("glass_tube", PSBlocks.GLASS_TUBE);
    Item GLASS_VALVE = register("glass_valve", PSBlocks.GLASS_VALVE);
    Item PUMP = register("pump", PSBlocks.PUMP);

    Item CRYSTAL_METH = register("methamphetamine_powder", new SnortableItem(new Properties()
                .food(EdibleItem.NON_FILLING_EDIBLE)
                .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.METHAMPHETAMINE, DelayType.IMMEDIATE, 0.002, 0.003, 0.15F)))
    ));
    Item BLUE_CRYSTAL_METH = register("blue_methamphetamine_powder", new SnortableItem(new Properties()
                .food(EdibleItem.NON_FILLING_EDIBLE)
                .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.METHAMPHETAMINE, DelayType.IMMEDIATE, 0.002, 0.003, 0.25F)))
    ));
    // TODO: Different variants of the pills (use dyes to control appearance)
    Item EXTACY = register("extacy", new EdibleItem(new Properties()
                .food(EdibleItem.NON_FILLING_EDIBLE)
                .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.METHAMPHETAMINE, DelayType.INGESTED, 0.005, 0.003, 0.15F)))
    ));
    Item PACIFIER = register("pacifier", new PacifierItem(new Item.Properties().durability(50)));

    Item HEROINE_POWDER = register("heroine_powder", new SnortableItem(new Properties()
                .food(EdibleItem.NON_FILLING_EDIBLE)
                .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.MORPHINE, DelayType.IMMEDIATE, 0.002, 0.003, 0.15F)))
    ));
    Item MORPHINE_TABLET = register("morphine_tablet", new EdibleItem(new Properties()
                .food(EdibleItem.NON_FILLING_EDIBLE)
                .component(PSComponents.DRUGS, ItemDrugs.of(new DrugInfluence(DrugType.MORPHINE, DelayType.INGESTED, 0.005, 0.003, 0.5F)))
    ));
    Item BROKEN_GLASS = register("broken_glass", new Item(new Item.Properties()));

    static Item register(String name, Block block) {
        return register(name, new BlockItem(block, new Properties()));
    }

    static Item register(String name) {
        return register(name, new Item(new Properties()));
    }

    static FlaskItem barrel(Block block) {
        return new FlaskItem(block, new Properties().stacksTo(16)
                .stacksTo(16)
                .component(PSComponents.PROCESS_TYPE, Processable.ProcessType.MATURE)
                .component(PSComponents.FLUID_CAPACITY, FluidCapacity.create(FluidVolumes.BARREL)));
    }

    static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, Psychedelicraft.id(name), item);
    }

    static void registerVegitationWasteItems(float compostChance, int fuelValue, ItemLike...items) {
        // Compost chances and burn times live in data/neoforge/data_maps/item (NeoForge data maps)
    }

    static void bootstrap() {
        PSComponents.bootstrap();

        CauldronInteraction.WATER.map().put(HARMONIUM, CauldronInteraction.WATER.map().get(Items.LEATHER_BOOTS));

        registerVegitationWasteItems(0.2F, 5, CANNABIS_BUDS, DRIED_CANNABIS_BUDS, HOP_CONES, JIMSONWEED_SEED_POD);
        registerVegitationWasteItems(0.65F, -1,
            WINE_GRAPES, TOMATO, BELLADONNA_BERRIES, AGAVE_LEAF, JUNIPER_BERRIES, COFFEA_CHERRIES, COFFEE_BEANS,
            PEYOTE, DRIED_PEYOTE);
        registerVegitationWasteItems(0.3F, -1,
            CANNABIS_SEEDS, TOBACCO_SEEDS, COCA_SEEDS, HOP_SEEDS, MORNING_GLORY_SEEDS, JIMSONWEED_SEEDS, TOMATO_SEEDS, BELLADONNA_SEEDS,
            JUNIPER_LEAVES, FRUITING_JUNIPER_LEAVES, JUNIPER_SAPLING);
        registerVegitationWasteItems(0.6F, -1, CANNABIS_LEAF, TOBACCO_LEAVES, COCA_LEAVES, JIMSONWEED_LEAF, TOMATO_LEAF, BELLADONNA_LEAF, MORNING_GLORY);
        registerVegitationWasteItems(0.4F, -1, DRIED_CANNABIS_LEAF, DRIED_TOBACCO, DRIED_COCA_LEAVES, DRIED_JIMSONWEED_LEAF, DRIED_BELLADONNA_LEAF);

        List.of(Items.BUCKET, Items.BOWL, Items.GLASS_BOTTLE, Items.MILK_BUCKET, Items.LAVA_BUCKET).forEach(item -> {
            FluidCauldronBlock.BEHAVIOUR.map().put(item, FluidCauldronBehavior.OTHER_FLUID);
        });
        List.of(Items.GLASS_BOTTLE, Items.BOWL).forEach(item -> {
            CauldronInteraction.LAVA.map().put(item, FluidCauldronBehavior.LAVA);
        });
        List.of(
            WOODEN_MUG, STONE_CUP, GLASS_CHALICE, SHOT_GLASS, BOTTLE, FILLED_BUCKET, FILLED_BOWL, FILLED_GLASS_BOTTLE
        ).forEach(FluidCauldronBehavior::register);
    }
}
