package dev.craft.data;

import java.util.function.BiConsumer;

import dev.craft.ic2.Ic2Blocks;
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
        // filled tin can 100, spoiled tin can 200, silicon cell 20: not yet available
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
        // GT machine parts 80, advanced circuit parts 120, circuit boards 180/40/20: not yet available
        add(pool, Ic2Blocks.cable(CableType.INSULATED_COPPER).get(), 200);
        add(pool, Ic2Blocks.cable(CableType.DOUBLE_INSULATED_GOLD).get(), 40);
        add(pool, Items.REDSTONE, 90);
        add(pool, Items.GLOWSTONE_DUST, 80);
        add(pool, Ic2Items.COAL_DUST.get(), 80);
        // charcoal dust 250: not yet available
        add(pool, Ic2Items.IRON_DUST.get(), 100);
        add(pool, Ic2Items.GOLD_DUST.get(), 100);
        // silver and electrum dust 50 each: not yet available
        add(pool, Ic2Items.TIN_DUST.get(), 120);
        add(pool, Ic2Items.COPPER_DUST.get(), 120);
        // GT dusts and gems (bauxite … yellow garnet, olivine, ruby, sapphires): not yet available
        add(pool, Items.EMERALD, 5);
        add(pool, Items.DIAMOND, 5);
        output.accept(ScrapBoxItem.DROPS, LootTable.lootTable().withPool(pool));
    }

    private static void add(LootPool.Builder pool, ItemLike item, int weight) {
        pool.add(LootItem.lootTableItem(item).setWeight(weight));
    }
}
