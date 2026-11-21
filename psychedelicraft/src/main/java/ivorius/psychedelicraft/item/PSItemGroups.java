/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.List;
import java.util.stream.Stream;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.RiftFractionComponent;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.material.Fluids;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSItemGroups {
    ResourceKey<CreativeModeTab> GENERAL = register("general", CreativeModeTab.builder()
            .icon(PSItems.CANNABIS_LEAF::getDefaultInstance)
            .displayItems((context, entries) -> {
                entries.accept(PSItems.DRYING_TABLE);
                entries.accept(PSItems.IRON_DRYING_TABLE);
                entries.accept(PSItems.FLASK);
                entries.accept(PSItems.DISTILLERY);
                entries.accept(PSItems.BOTTLE_RACK);
                entries.accept(PSItems.MASH_TUB);
                entries.accept(PSItems.PUMP);

                PSItems.ALL_BARRELS.stream().filter(i -> i.getBlock() != PSBlocks.PALE_OAK_BARREL).forEach(entries::accept);

                if (Psychedelicraft.getConfig().enableRiftJars.get()) {
                    entries.accept(PSItems.RIFT_JAR.getDefaultInstance());
                    entries.accept(RiftFractionComponent.set(PSItems.RIFT_JAR.getDefaultInstance(), 0.25F));
                    entries.accept(RiftFractionComponent.set(PSItems.RIFT_JAR.getDefaultInstance(), 0.55F));
                    entries.accept(RiftFractionComponent.set(PSItems.RIFT_JAR.getDefaultInstance(), 0.75F));
                    entries.accept(RiftFractionComponent.set(PSItems.RIFT_JAR.getDefaultInstance(), 0.9F));
                }

                entries.accept(PSItems.SMOKING_PIPE);
                entries.accept(PSItems.CIGARETTE);
                entries.accept(PSItems.CIGAR);
                entries.accept(PSItems.JOINT);
                entries.accept(PSItems.BLUNT);

                entries.accept(PSItems.BONG);
                entries.accept(PSItems.SYRINGE);
                List.of(PSFluids.COCAINE, PSFluids.CAFFEINE, PSFluids.BATH_SALTS, PSFluids.MORPHINE, PSFluids.ATROPINE).forEach(fluid -> {
                    entries.accept(ItemFluids.set(PSItems.SYRINGE.getDefaultInstance(), fluid.getDefaultStack(FluidVolumes.SYRINGE)));
                });
                List.of(PSFluids.MORNING_GLORY_EXTRACT, PSFluids.BELLADONA_EXTRACT, PSFluids.JIMSONWEED_EXTRACT).forEach(fluid -> {
                    entries.accept(ItemFluids.set(PSItems.SYRINGE.getDefaultInstance(), fluid.getDefaultStack(FluidVolumes.SYRINGE)));
                    entries.accept(ItemFluids.set(PSItems.SYRINGE.getDefaultInstance(), ChemicalExtractFluid.DISTILLATION.set(fluid.getDefaultStack(FluidVolumes.SYRINGE), 2)));
                });

                entries.accept(PSItems.COFFEA_CHERRIES);
                entries.accept(PSItems.COFFEE_BEANS);

                entries.accept(PSItems.TOMATO_LEAF);
                entries.accept(PSItems.TOMATO_SEEDS);
                entries.accept(PSItems.TOMATO);

                entries.accept(PSItems.OBSIDIAN_BOTTLE);
                entries.accept(PSItems.OBSIDIAN_DUST);

               // entries.add(PSItems.KAVA_SEEDS);
               // entries.add(PSItems.KAVA_ROOT);

                entries.accept(PSItems.MORNING_GLORY);
                entries.accept(PSItems.MORNING_GLORY_SEEDS);

                entries.accept(PSItems.JIMSONWEED_SEEDS);
                entries.accept(PSItems.JIMSONWEED_SEED_POD);
                entries.accept(PSItems.JIMSONWEED_LEAF);
                entries.accept(PSItems.DRIED_JIMSONWEED_LEAF);

                entries.accept(PSItems.BELLADONNA_SEEDS);
                entries.accept(PSItems.BELLADONNA_BERRIES);
                entries.accept(PSItems.BELLADONNA_LEAF);
                entries.accept(PSItems.DRIED_BELLADONNA_LEAF);

                entries.accept(PSItems.TOBACCO_SEEDS);
                entries.accept(PSItems.TOBACCO_LEAVES);
                entries.accept(PSItems.DRIED_TOBACCO);

                entries.accept(PSItems.COCA_SEEDS);
                entries.accept(PSItems.COCA_LEAVES);
                entries.accept(PSItems.DRIED_COCA_LEAVES);
                entries.accept(PSItems.COCAINE_POWDER);

                entries.accept(PSItems.AGAVE_LEAF);
                entries.accept(PSItems.PEYOTE);
                entries.accept(PSItems.DRIED_PEYOTE);
                entries.accept(PSItems.PEYOTE_JOINT);

                entries.accept(PSItems.HOP_SEEDS);
                entries.accept(PSItems.HOP_CONES);

                entries.accept(PSItems.CANNABIS_SEEDS);
                entries.accept(PSItems.CANNABIS_LEAF);
                entries.accept(PSItems.DRIED_CANNABIS_LEAF);
                entries.accept(PSItems.CANNABIS_BUDS);
                entries.accept(PSItems.DRIED_CANNABIS_BUDS);

                entries.accept(PSItems.HASH_MUFFIN);
                entries.accept(PSItems.JOLLY_RANCHER);

                entries.accept(PSItems.BROWN_MAGIC_MUSHROOMS);
                entries.accept(PSItems.RED_MAGIC_MUSHROOMS);

                entries.accept(PSItems.LATTICE);
                entries.accept(PSItems.WINE_GRAPES);

                entries.accept(PSItems.JUNIPER_LEAVES);
                entries.accept(PSItems.FRUITING_JUNIPER_LEAVES);
                entries.accept(PSItems.JUNIPER_LOG);
                entries.accept(PSItems.JUNIPER_WOOD);
                entries.accept(PSItems.STRIPPED_JUNIPER_LOG);
                entries.accept(PSItems.STRIPPED_JUNIPER_WOOD);
                entries.accept(PSItems.JUNIPER_SAPLING);
                entries.accept(PSItems.JUNIPER_BERRIES);

                entries.accept(PSItems.JUNIPER_PLANKS);
                entries.accept(PSItems.JUNIPER_STAIRS);
                entries.accept(PSItems.JUNIPER_SIGN);
                entries.accept(PSItems.JUNIPER_DOOR);
                entries.accept(PSItems.JUNIPER_HANGING_SIGN);
                entries.accept(PSItems.JUNIPER_PRESSURE_PLATE);
                entries.accept(PSItems.JUNIPER_FENCE);
                entries.accept(PSItems.JUNIPER_TRAPDOOR);
                entries.accept(PSItems.JUNIPER_FENCE_GATE);
                entries.accept(PSItems.JUNIPER_BUTTON);
                entries.accept(PSItems.JUNIPER_SLAB);
                entries.accept(PSItems.JUNIPER_BOAT);
                entries.accept(PSItems.JUNIPER_CHEST_BOAT);

                entries.accept(PSItems.PAPER_BAG);

                if (Psychedelicraft.getConfig().enableHarmonium.get()) {
                    for (DyeColor dye : DyeColor.values()) {
                        ItemStack harmonium = PSItems.HARMONIUM.getDefaultInstance();
                        harmonium.set(DataComponents.DYED_COLOR, new DyedItemColor(dye.getTextColor(), true));
                        entries.accept(harmonium);
                    }
                }
            }));
    ResourceKey<CreativeModeTab> PHARMACEUTICALS = register("pharmaceuticals", CreativeModeTab.builder()
            .icon(PSItems.MORPHINE_TABLET::getDefaultInstance)
            .displayItems((context, entries) -> {
                entries.accept(PSItems.BUNSEN_BURNER);
                entries.accept(PSItems.TRAY);
                entries.accept(PSItems.GLASS_TUBE);
                entries.accept(PSItems.GLASS_VALVE);
                entries.accept(PSItems.MORPHINE_TABLET);
                entries.accept(PSItems.LSD_PILL);
                entries.accept(PSItems.EXTACY);
                entries.accept(PSItems.CRYSTAL_METH);
                entries.accept(PSItems.BLUE_CRYSTAL_METH);
                entries.accept(PSItems.LSA_SQUARE);
                entries.accept(PSItems.PACIFIER);
                entries.accept(PSItems.BROKEN_GLASS);
                entries.accept(PSItems.CRACK_COCAINE);
                entries.accept(PSItems.HEROINE_POWDER);
            }));
    ResourceKey<CreativeModeTab> DRINKS = register("drinks", CreativeModeTab.builder()
            .icon(() -> ItemFluids.set(PSItems.BOTTLE.getDefaultInstance(), SimpleFluid.of(Fluids.WATER).getDefaultStack(FluidVolumes.BOTTLE)))
            .displayItems((context, entries) -> {
                streamContainers().map(Item::getDefaultInstance).forEach(container -> {
                    streamFluids().flatMap(fluid -> fluid.getDefaultStacks(container)).forEach(entries::accept);
                });
            }));
    ResourceKey<CreativeModeTab> WEAPONS = register("weapons", CreativeModeTab.builder()
            .icon(() -> ItemFluids.set(PSItems.MOLOTOV_COCKTAIL.getDefaultInstance(), PSFluids.GASOLINE.getDefaultStack(FluidVolumes.BOTTLE)))
            .displayItems((context, entries) -> {
                if (!Psychedelicraft.getConfig().disableMolotovs.get()) {
                    streamFluids().flatMap(fluid -> fluid.getDefaultStacks(PSItems.MOLOTOV_COCKTAIL.getDefaultInstance())).forEach(entries::accept);
                }
            }));

    private static Stream<Item> streamContainers() {
        return Stream.of(PSItems.STONE_CUP, PSItems.WOODEN_MUG, PSItems.GLASS_CHALICE, PSItems.SHOT_GLASS, PSItems.BOTTLE, Items.BUCKET, Items.BOWL, Items.GLASS_BOTTLE);
    }

    private static Stream<SimpleFluid> streamFluids() {
        return Stream.concat(Stream.of(PSFluids.EMPTY), Stream.concat(
                Stream.of(Fluids.WATER, Fluids.LAVA).map(SimpleFluid::of),
                SimpleFluid.REGISTRY.stream().filter(i -> !i.isEmpty())
        )).distinct();
    }

    static ResourceKey<CreativeModeTab> register(String name, CreativeModeTab.Builder builder) {
        ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Psychedelicraft.id(name));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key.location(), builder
                .title(Component.translatable(Util.makeDescriptionId("itemGroup", key.location())))
                .build()
        );
        return key;
    }

    static void bootstrap() { }
}
