package dev.craft.ic2.item;

import java.util.ArrayList;
import java.util.List;

import dev.craft.gregtech.GtItems;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/**
 * Scrap box drops as in the pack: GregTech empties IC2's list in pre-init, adds scrap with chance 200 and re-adds its
 * own list in post-init ({@code GT_Mod.preload}/{@code postload}). Weights are the GT chances × 100 (exact integers).
 * Source of the loot table {@link ScrapBoxItem#DROPS} (datagen) and of the recipe viewer; the full list is in
 * {@code docs/spec/05-items.md} §1.1.
 */
public final class ScrapBoxDrops {
    public record Drop(ItemStack stack, int weight) {
    }

    private ScrapBoxDrops() {
    }

    public static List<Drop> all() {
        List<Drop> drops = new ArrayList<>();
        add(drops, Ic2Items.SCRAP.get(), 20000);
        add(drops, Items.WOODEN_HOE, 950);
        add(drops, Items.WOODEN_AXE, 200);
        add(drops, Items.WOODEN_SWORD, 200);
        add(drops, Items.WOODEN_SHOVEL, 200);
        add(drops, Items.WOODEN_PICKAXE, 200);
        add(drops, Items.OAK_SIGN, 200);
        add(drops, Items.STICK, 950);
        add(drops, Blocks.DIRT, 500);
        add(drops, Blocks.GRASS_BLOCK, 300);
        add(drops, Blocks.GRAVEL, 300);
        add(drops, Blocks.PUMPKIN, 50);
        add(drops, Blocks.SOUL_SAND, 100);
        add(drops, Blocks.NETHERRACK, 200);
        add(drops, Items.BONE, 100);
        add(drops, Items.ROTTEN_FLESH, 900);
        add(drops, Items.COOKED_PORKCHOP, 40);
        add(drops, Items.COOKED_BEEF, 40);
        add(drops, Items.COOKED_CHICKEN, 40);
        add(drops, Items.APPLE, 50);
        add(drops, Items.BREAD, 50);
        add(drops, Items.CAKE, 10);
        add(drops, Ic2Items.FILLED_TIN_CAN.get(), 100);
        add(drops, FilledTinCanItem.create(1, 1), 200);
        add(drops, GtItems.SILICON_CELL.get(), 20);
        add(drops, Ic2Items.WATER_CELL.get(), 100);
        add(drops, Ic2Items.EMPTY_CELL.get(), 200);
        add(drops, Items.PAPER, 500);
        add(drops, Items.LEATHER, 100);
        add(drops, Items.FEATHER, 100);
        add(drops, Ic2Items.PLANT_BALL.get(), 70);
        add(drops, GtItems.WOOD_PULP.get(), 380);
        add(drops, Items.SLIME_BALL, 60);
        add(drops, Ic2Items.RUBBER.get(), 80);
        add(drops, Ic2Items.SU_BATTERY.get(), 270);
        add(drops, GtItems.MACHINE_PARTS.get(), 80);
        add(drops, GtItems.ADVANCED_CIRCUIT_PARTS.get(), 120);
        add(drops, GtItems.BASIC_CIRCUIT_BOARD.get(), 180);
        add(drops, GtItems.ADVANCED_CIRCUIT_BOARD.get(), 40);
        add(drops, Ic2Blocks.cable(CableType.INSULATED_COPPER).get(), 200);
        add(drops, Ic2Blocks.cable(CableType.DOUBLE_INSULATED_GOLD).get(), 40);
        add(drops, Items.REDSTONE, 90);
        add(drops, Items.GLOWSTONE_DUST, 80);
        add(drops, Ic2Items.COAL_DUST.get(), 80);
        add(drops, GtItems.CHARCOAL_DUST.get(), 250);
        add(drops, Ic2Items.IRON_DUST.get(), 100);
        add(drops, Ic2Items.GOLD_DUST.get(), 100);
        add(drops, Ic2Items.SILVER_DUST.get(), 50);
        add(drops, GtItems.ELECTRUM_DUST.get(), 50);
        add(drops, Ic2Items.TIN_DUST.get(), 120);
        add(drops, Ic2Items.COPPER_DUST.get(), 120);
        add(drops, GtItems.BAUXITE_DUST.get(), 50);
        add(drops, GtItems.ALUMINIUM_DUST.get(), 50);
        add(drops, GtItems.LEAD_DUST.get(), 50);
        add(drops, GtItems.NICKEL_DUST.get(), 50);
        add(drops, GtItems.ZINC_DUST.get(), 50);
        add(drops, GtItems.BRASS_DUST.get(), 50);
        add(drops, GtItems.STEEL_DUST.get(), 50);
        add(drops, GtItems.OBSIDIAN_DUST.get(), 150);
        add(drops, GtItems.SULFUR_DUST.get(), 150);
        add(drops, GtItems.SALTPETER_DUST.get(), 200);
        add(drops, GtItems.LAZURITE_DUST.get(), 200);
        add(drops, GtItems.PYRITE_DUST.get(), 200);
        add(drops, GtItems.CALCITE_DUST.get(), 200);
        add(drops, GtItems.SODALITE_DUST.get(), 200);
        add(drops, GtItems.TUNGSTEN_DUST.get(), 3);
        add(drops, GtItems.PLATINUM_DUST.get(), 3);
        add(drops, GtItems.CHROME_DUST.get(), 3);
        add(drops, GtItems.TITANIUM_DUST.get(), 3);
        add(drops, GtItems.MAGNESIUM_DUST.get(), 3);
        add(drops, GtItems.ELITE_CIRCUIT_BOARD.get(), 20);
        add(drops, GtItems.NETHERRACK_DUST.get(), 400);
        add(drops, GtItems.FLINT_DUST.get(), 400);
        add(drops, GtItems.RED_GARNET.get(), 50);
        add(drops, GtItems.YELLOW_GARNET.get(), 50);
        add(drops, GtItems.OLIVINE.get(), 5);
        add(drops, GtItems.RUBY.get(), 5);
        add(drops, GtItems.SAPPHIRE.get(), 5);
        add(drops, GtItems.GREEN_SAPPHIRE.get(), 5);
        add(drops, Items.EMERALD, 5);
        add(drops, Items.DIAMOND, 5);
        return drops;
    }

    private static void add(List<Drop> drops, ItemLike item, int weight) {
        add(drops, new ItemStack(item), weight);
    }

    private static void add(List<Drop> drops, ItemStack stack, int weight) {
        drops.add(new Drop(stack, weight));
    }
}
