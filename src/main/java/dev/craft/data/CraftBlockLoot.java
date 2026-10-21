package dev.craft.data;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.transport.PipeType;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.forestry.FrBlocks;
import dev.craft.compactsolars.CsBlocks;
import dev.craft.powerplants.PpBlocks;
import dev.craft.ironchest.IronChestBlocks;
import dev.craft.thermalexpansion.TeBlocks;
import dev.craft.redpower.RpBlocks;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.world.GtOre;
import dev.craft.gregtech.world.GtOreChanceCondition;
import dev.craft.gregtech.world.GtOreCountFunction;
import dev.craft.powerconverters.PcBlocks;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.block.RubberWoodBlock;
import dev.craft.ic2.block.ScaffoldBlock;
import dev.craft.ic2.cable.CableType;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
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
                case ADVANCED_MACHINE_CASING -> dropOther(block, Ic2Blocks.ADVANCED_MACHINE_CASING.get());
            }
        }
        // GT_BlockMetaID_Machine.getBlockDropped: always machine parts (the wrench drops the machine itself).
        for (GtMachine machine : GtMachine.values()) {
            dropOther(GtBlocks.machine(machine).get(), GtItems.MACHINE_PARTS.get());
        }
        for (GtCasingBlock.Type type : GtCasingBlock.Type.values()) {
            dropSelf(GtBlocks.casing(type).get());
        }
        dropSelf(GtBlocks.HIGHLY_ADVANCED_MACHINE_BLOCK.get());
        dropSelf(RpBlocks.PROJECT_TABLE.get());
        IronChestBlocks.BLOCKS.getEntries().forEach(chest -> dropSelf(chest.get()));
        // Thermal Expansion (spec 26): ores and rockwool drop themselves, hardened glass only with silk touch.
        for (Block ore : List.of(TeBlocks.SILVER_ORE.get(), TeBlocks.DEEPSLATE_SILVER_ORE.get(), TeBlocks.LEAD_ORE.get(),
                TeBlocks.DEEPSLATE_LEAD_ORE.get(), TeBlocks.NICKEL_ORE.get(), TeBlocks.DEEPSLATE_NICKEL_ORE.get())) {
            dropSelf(ore);
        }
        dropWhenSilkTouch(TeBlocks.HARDENED_GLASS.get());
        dropSelf(TeBlocks.POWERED_FURNACE.get());
        dropSelf(TeBlocks.PULVERIZER.get());
        dropSelf(TeBlocks.SAWMILL.get());
        dropSelf(TeBlocks.INDUCTION_SMELTER.get());
        dropSelf(TeBlocks.MAGMA_CRUCIBLE.get());
        dropSelf(TeBlocks.LIQUID_TRANSPOSER.get());
        dropSelf(TeBlocks.GLACIAL_PRECIPITATOR.get());
        dropSelf(TeBlocks.IGNEOUS_EXTRUDER.get());
        dropSelf(TeBlocks.AQUEOUS_ACCUMULATOR.get());
        // BlockTank: broken normally the tank drops empty; the wrench keeps the liquid.
        dropSelf(TeBlocks.PORTABLE_TANK.get());
        for (DyeColor color : DyeColor.values()) {
            dropSelf(TeBlocks.rockwool(color).get());
        }
        dropSelf(GtBlocks.STEEL_BLOCK.get());
        for (GtOre ore : GtOre.values()) {
            gtOre(ore, GtBlocks.ore(ore).get());
            if (ore.hasDeepslateVariant()) {
                gtOre(ore, GtBlocks.deepslateOre(ore).get());
            }
        }
        for (CableType type : CableType.values()) {
            dropSelf(Ic2Blocks.cable(type).get());
        }
        dropSelf(Ic2Blocks.MACHINE_CASING.get());
        dropSelf(Ic2Blocks.IRON_FENCE.get());
        dropSelf(Ic2Blocks.MINING_PIPE.get());
        dropOther(Ic2Blocks.MINING_TIP.get(), Ic2Items.MINING_PIPE.get());
        dropSelf(Ic2Blocks.REINFORCED_GLASS.get());
        dropSelf(Ic2Blocks.REINFORCED_STONE.get());
        add(Ic2Blocks.REINFORCED_DOOR.get(), createDoorTable(Ic2Blocks.REINFORCED_DOOR.get()));
        dropSelf(Ic2Blocks.RUBBER_MAT.get());
        dropSelf(Ic2Blocks.CROP.get());
        add(Ic2Blocks.INDUSTRIAL_TNT.get(), noDrop());
        add(Ic2Blocks.NUKE.get(), noDrop());
        add(Ic2Blocks.DYNAMITE_STICK.get(), noDrop());
        add(Ic2Blocks.DYNAMITE_STICK_WITH_REMOTE.get(), noDrop());
        dropSelf(Ic2Blocks.ADVANCED_MACHINE_CASING.get());
        dropSelf(Ic2Blocks.TIN_ORE.get());
        dropSelf(Ic2Blocks.DEEPSLATE_TIN_ORE.get());
        dropSelf(Ic2Blocks.TIN_BLOCK.get());
        // IC2 uranium ore: one uranium drop (no fortune bonus), the block itself with silk touch.
        for (Block ore : new Block[]{Ic2Blocks.URANIUM_ORE.get(), Ic2Blocks.DEEPSLATE_URANIUM_ORE.get()}) {
            add(ore, createSilkTouchDispatchTable(ore,
                    applyExplosionDecay(ore, LootItem.lootTableItem(Ic2Items.URANIUM_DROP.get()))));
        }
        dropSelf(Ic2Blocks.URANIUM_BLOCK.get());
        dropOther(Ic2Blocks.REACTOR_CHAMBER.get(), Ic2Blocks.MACHINE_CASING.get());
        dropSelf(Ic2Blocks.BRONZE_BLOCK.get());
        dropSelf(Ic2Blocks.RUBBER_SAPLING.get());
        for (EngineType type : EngineType.values()) {
            dropSelf(BcBlocks.engine(type).get());
        }
        dropSelf(BcBlocks.MARKER.get());
        dropSelf(BcBlocks.QUARRY.get());
        dropSelf(BcBlocks.TANK.get());
        dropSelf(BcBlocks.PUMP.get());
        dropSelf(BcBlocks.REFINERY.get());
        dropSelf(BcBlocks.AUTO_WORKBENCH.get());
        dropSelf(BcBlocks.MINING_WELL.get());
        for (PipeType type : PipeType.values()) {
            dropSelf(BcBlocks.pipe(type).get());
        }
        dropSelf(FrBlocks.THERMIONIC_FABRICATOR.get());
        dropSelf(FrBlocks.CARPENTER.get());
        dropSelf(FrBlocks.ELECTRICAL_ENGINE.get());
        for (var block : PcBlocks.BLOCKS.getEntries()) {
            dropSelf(block.get());
        }
        for (var block : CsBlocks.BLOCKS.getEntries()) {
            dropSelf(block.get());
        }
        for (var block : PpBlocks.BLOCKS.getEntries()) {
            dropSelf(block.get());
        }
        resinSheet();
        add(Ic2Blocks.CONSTRUCTION_FOAM.get(), noDrop());
        add(Ic2Blocks.CONSTRUCTION_FOAM_WALL.get(), noDrop());
        add(Ic2Blocks.LUMINATOR.get(), noDrop());
        scaffold(Ic2Blocks.WOODEN_SCAFFOLD.get(), Items.STICK, 2);
        scaffold(Ic2Blocks.IRON_SCAFFOLD.get(), Ic2Items.IRON_FENCE.get(), 1);
        boozeBarrel();
        rubberWood();
        rubberLeaves();
    }

    /** {@code GT_BlockMetaID_Ore.getBlockDropped} (spec 07 §6.2); silk touch drops the ore block itself. */
    private void gtOre(GtOre ore, Block block) {
        switch (ore) {
            case GALENA, BAUXITE, TUNGSTATE, SHELDONITE -> dropSelf(block);
            case IRIDIUM -> add(block, gtOreTable(block, Ic2Items.IRIDIUM_ORE.get(), 1, 1, 2));
            case RUBY -> add(block, gtOreTable(block, GtItems.RUBY.get(), 1, 1, 1)
                    .withPool(gtOreExtra(block, GtItems.RED_GARNET.get(), 32)));
            case SAPPHIRE -> add(block, gtOreTable(block, GtItems.SAPPHIRE.get(), 1, 1, 1)
                    .withPool(gtOreExtra(block, GtItems.GREEN_SAPPHIRE.get(), 64)));
            case PYRITE -> add(block, gtOreTable(block, GtItems.PYRITE_DUST.get(), 2, 1, 1));
            case CINNABAR -> add(block, gtOreTable(block, GtItems.CINNABAR_DUST.get(), 2, 1, 1)
                    .withPool(gtOreExtra(block, Items.REDSTONE, 4)));
            case SPHALERITE -> add(block, gtOreTable(block, GtItems.SPHALERITE_DUST.get(), 2, 1, 1)
                    .withPool(gtOreExtra(block, GtItems.ZINC_DUST.get(), 4))
                    .withPool(gtOreExtra(block, GtItems.YELLOW_GARNET.get(), 32)));
            case OLIVINE -> add(block, gtOreTable(block, GtItems.OLIVINE.get(), 1, 1, 1));
            case SODALITE -> add(block, gtOreTable(block, GtItems.SODALITE_DUST.get(), 6, 3, 1)
                    .withPool(gtOreExtra(block, GtItems.ALUMINIUM_DUST.get(), 4)));
        }
    }

    /** Main drop {@code base + multiplier * rand(1 + fortune / divisor)}, the block itself with silk touch. */
    private LootTable.Builder gtOreTable(Block block, Item drop, int base, int multiplier, int divisor) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(drop)
                .apply(GtOreCountFunction.count(base, multiplier, divisor))));
    }

    /** One extra item with chance {@code 1 / max(1, range / (fortune + 1))}, not with silk touch. */
    private LootPool.Builder gtOreExtra(Block block, Item item, int range) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(doesNotHaveSilkTouch())
                .when(GtOreChanceCondition.chance(range)).add(applyExplosionDecay(block, LootItem.lootTableItem(item)));
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

    /** IC2 {@code BlockScaffold}: itself, plus the reinforcement when reinforced. */
    /** IC2 {@code BlockBarrel.getBlockDropped}: a wooden scaffold and an empty barrel. */
    private void boozeBarrel() {
        Block block = Ic2Blocks.BARREL.get();
        add(block, LootTable.lootTable()
                .withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Ic2Blocks.WOODEN_SCAFFOLD.get()))))
                .withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Ic2Items.BOOZE_BARREL.get())))));
    }

    private void scaffold(ScaffoldBlock block, Item reinforcement, int count) {
        add(block, LootTable.lootTable()
                .withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block))))
                .withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(reinforcement)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count))))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(ScaffoldBlock.STRENGTH, block.getReinforcedStrength()))))));
    }

    /** IC2 {@code BlockResin}: drops sticky resin with 4/5 chance. */
    private void resinSheet() {
        Block block = Ic2Blocks.RESIN_SHEET.get();
        add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(Ic2Items.RESIN.get()))
                .when(LootItemRandomChanceCondition.randomChance(0.8F)))));
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
        return Stream.of(Ic2Blocks.BLOCKS, BcBlocks.BLOCKS, FrBlocks.BLOCKS, PcBlocks.BLOCKS,
                        CsBlocks.BLOCKS, GtBlocks.BLOCKS, PpBlocks.BLOCKS, RpBlocks.BLOCKS, IronChestBlocks.BLOCKS,
                        TeBlocks.BLOCKS)
                .flatMap(blocks -> blocks.getEntries().stream())
                .map(holder -> (Block) holder.get()).toList();
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
