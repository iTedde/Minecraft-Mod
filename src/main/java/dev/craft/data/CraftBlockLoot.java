package dev.craft.data;

import java.util.List;
import java.util.Set;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.block.RubberWoodBlock;
import dev.craft.ic2.cable.CableType;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class CraftBlockLoot extends BlockLootSubProvider {
    public CraftBlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        for (Ic2Machine machine : Ic2Machine.values()) {
            Block block = Ic2Blocks.machine(machine).get();
            switch (machine.getDrop()) {
                case SELF -> dropSelf(block);
                case GENERATOR -> dropOther(block, Ic2Blocks.machine(Ic2Machine.GENERATOR).get());
                case MACHINE_CASING -> dropOther(block, Ic2Blocks.MACHINE_CASING.get());
            }
        }
        for (CableType type : CableType.values()) {
            dropSelf(Ic2Blocks.cable(type).get());
        }
        dropSelf(Ic2Blocks.MACHINE_CASING.get());
        dropSelf(Ic2Blocks.TIN_ORE.get());
        dropSelf(Ic2Blocks.DEEPSLATE_TIN_ORE.get());
        dropSelf(Ic2Blocks.TIN_BLOCK.get());
        dropSelf(Ic2Blocks.BRONZE_BLOCK.get());
        dropSelf(Ic2Blocks.RUBBER_SAPLING.get());
        rubberWood();
        rubberLeaves();
    }

    /** IC2 {@code BlockRubWood}: drops itself, and 1/6 sticky resin unless it was placed by a player. */
    private void rubberWood() {
        RubberWoodBlock block = Ic2Blocks.RUBBER_WOOD.get();
        add(block, createSingleItemTable(block).withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(Ic2Items.RESIN.get()))
                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(RubberWoodBlock.RESIN, RubberWoodBlock.Resin.NONE)).invert())
                .when(LootItemRandomChanceCondition.randomChance(1.0F / 6.0F)))));
    }

    /** IC2 {@code BlockRubLeaves}: shears/silk touch drop the leaves, otherwise 1/35 sapling and nothing else. */
    private void rubberLeaves() {
        Block block = Ic2Blocks.RUBBER_LEAVES.get();
        add(block, createSilkTouchOrShearsDispatchTable(block, applyExplosionCondition(block,
                LootItem.lootTableItem(Ic2Blocks.RUBBER_SAPLING.get()))
                .when(LootItemRandomChanceCondition.randomChance(1.0F / 35.0F))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Ic2Blocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get()).toList();
    }

    /**
     * Vanilla blocks whose drops change: (deepslate) copper ore drops one raw copper like iron ore does, instead of
     * 2–5 (IC2 copper ore dropped itself; see {@code docs/abweichungen.md}).
     */
    public static final class Vanilla extends BlockLootSubProvider {
        public Vanilla(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            add(Blocks.COPPER_ORE, createOreDrop(Blocks.COPPER_ORE, Items.RAW_COPPER));
            add(Blocks.DEEPSLATE_COPPER_ORE, createOreDrop(Blocks.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
        }
    }
}
