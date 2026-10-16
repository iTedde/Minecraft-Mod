package dev.craft.data;

import java.util.function.BiConsumer;

import dev.craft.gregtech.GtItems;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.ScrapBoxItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Scrap box drops as in the pack: GregTech empties IC2's list in pre-init, adds scrap with chance 200 and re-adds its
 * own list in post-init ({@code GT_Mod.preload}/{@code postload}). Weights are the GT chances × 100 (exact integers).
 * Entries for GregTech items are added once those items exist (phase 6b); the full list is in
 * {@code docs/spec/05-items.md} §1.1.
 */
public record CraftScrapBoxLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
        add(pool, Ic2Items.SCRAP.get(), 20000);
        add(pool, Items.WOODEN_HOE, 950);
        add(pool, Items.WOODEN_AXE, 200);
        add(pool, Items.WOODEN_SWORD, 200);
        add(pool, Items.WOODEN_SHOVEL, 200);
        add(pool, Items.WOODEN_PICKAXE, 200);
        add(pool, Items.OAK_SIGN, 200);
        add(pool, Items.STICK, 950);
        add(pool, Blocks.DIRT, 500);
        add(pool, Blocks.GRASS_BLOCK, 300);
        add(pool, Blocks.GRAVEL, 300);
        add(pool, Blocks.PUMPKIN, 50);
        add(pool, Blocks.SOUL_SAND, 100);
        add(pool, Blocks.NETHERRACK, 200);
        add(pool, Items.BONE, 100);
        add(pool, Items.ROTTEN_FLESH, 900);
        add(pool, Items.COOKED_PORKCHOP, 40);
        add(pool, Items.COOKED_BEEF, 40);
        add(pool, Items.COOKED_CHICKEN, 40);
        add(pool, Items.APPLE, 50);
        add(pool, Items.BREAD, 50);
        add(pool, Items.CAKE, 10);
        add(pool, Ic2Items.FILLED_TIN_CAN.get(), 100);
        pool.add(LootItem.lootTableItem(Ic2Items.FILLED_TIN_CAN.get()).setWeight(200)
                .apply(SetComponentsFunction.setComponent(Ic2DataComponents.CAN_META.get(), 1)));
        add(pool, GtItems.SILICON_CELL.get(), 20);
        add(pool, Ic2Items.WATER_CELL.get(), 100);
        add(pool, Ic2Items.EMPTY_CELL.get(), 200);
        add(pool, Items.PAPER, 500);
        add(pool, Items.LEATHER, 100);
        add(pool, Items.FEATHER, 100);
        add(pool, Ic2Items.PLANT_BALL.get(), 70);
        // wood pulp 380: not yet available
        add(pool, Items.SLIME_BALL, 60);
        add(pool, Ic2Items.RUBBER.get(), 80);
        add(pool, Ic2Items.SU_BATTERY.get(), 270);
        add(pool, GtItems.MACHINE_PARTS.get(), 80);
        // advanced circuit parts 120, circuit boards 180/40/20: not yet available
        add(pool, Ic2Blocks.cable(CableType.INSULATED_COPPER).get(), 200);
        add(pool, Ic2Blocks.cable(CableType.DOUBLE_INSULATED_GOLD).get(), 40);
        add(pool, Items.REDSTONE, 90);
        add(pool, Items.GLOWSTONE_DUST, 80);
        add(pool, Ic2Items.COAL_DUST.get(), 80);
        // charcoal dust 250: not yet available
        add(pool, Ic2Items.IRON_DUST.get(), 100);
        add(pool, Ic2Items.GOLD_DUST.get(), 100);
        add(pool, Ic2Items.SILVER_DUST.get(), 50);
        // electrum dust 50: not yet available
        add(pool, Ic2Items.TIN_DUST.get(), 120);
        add(pool, Ic2Items.COPPER_DUST.get(), 120);
        add(pool, GtItems.BAUXITE_DUST.get(), 50);
        add(pool, GtItems.ALUMINIUM_DUST.get(), 50);
        add(pool, GtItems.LEAD_DUST.get(), 50);
        add(pool, GtItems.NICKEL_DUST.get(), 50);
        add(pool, GtItems.ZINC_DUST.get(), 50);
        // brass dust 50: not yet available
        add(pool, GtItems.STEEL_DUST.get(), 50);
        // obsidian dust 150: not yet available
        add(pool, GtItems.SULFUR_DUST.get(), 150);
        add(pool, GtItems.SALTPETER_DUST.get(), 200);
        add(pool, GtItems.LAZURITE_DUST.get(), 200);
        add(pool, GtItems.PYRITE_DUST.get(), 200);
        add(pool, GtItems.CALCITE_DUST.get(), 200);
        add(pool, GtItems.SODALITE_DUST.get(), 200);
        add(pool, GtItems.TUNGSTEN_DUST.get(), 3);
        add(pool, GtItems.PLATINUM_DUST.get(), 3);
        // other GT dusts (netherrack, flint, chrome, titanium, magnesium, endstone): not yet available
        add(pool, GtItems.RED_GARNET.get(), 50);
        add(pool, GtItems.YELLOW_GARNET.get(), 50);
        add(pool, GtItems.OLIVINE.get(), 5);
        add(pool, GtItems.RUBY.get(), 5);
        add(pool, GtItems.SAPPHIRE.get(), 5);
        add(pool, GtItems.GREEN_SAPPHIRE.get(), 5);
        add(pool, Items.EMERALD, 5);
        add(pool, Items.DIAMOND, 5);
        output.accept(ScrapBoxItem.DROPS, LootTable.lootTable().withPool(pool));
    }

    private static void add(LootPool.Builder pool, ItemLike item, int weight) {
        pool.add(LootItem.lootTableItem(item).setWeight(weight));
    }
}
