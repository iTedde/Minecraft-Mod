package dev.craft.data;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.thermalexpansion.TeBlocks;
import dev.craft.compactsolars.CsBlocks;
import dev.craft.compactsolars.SolarArrayType;
import dev.craft.powerplants.PpBlocks;
import dev.craft.enderstorage.EsBlocks;
import dev.craft.ironchest.IronChestBlocks;
import dev.craft.redpower.RpBlocks;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.forestry.FrBlocks;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.world.GtOre;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import dev.craft.ic2.item.tool.ChainsawItem;
import dev.craft.ic2.item.tool.DrillItem;
import dev.craft.ic2.item.tool.ElectricHoeItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public final class CraftBlockTagsProvider extends BlockTagsProvider {
    public static final TagKey<Block> ORES_TIN = commonTag("ores/tin");
    public static final TagKey<Block> ORES_URANIUM = commonTag("ores/uranium");
    public static final TagKey<Block> STORAGE_BLOCKS_URANIUM = commonTag("storage_blocks/uranium");
    public static final TagKey<Block> STORAGE_BLOCKS_TIN = commonTag("storage_blocks/tin");
    public static final TagKey<Block> STORAGE_BLOCKS_BRONZE = commonTag("storage_blocks/bronze");
    public static final TagKey<Block> STORAGE_BLOCKS_STEEL = commonTag("storage_blocks/steel");

    public CraftBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                  ExistingFileHelper files) {
        super(output, lookup, Craft.MODID, files);
    }

    private static TagKey<Block> commonTag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    /** GT ore: pickaxe of the GT harvest level, not destroyed by the dragon, {@code c:ores/<material>}. */
    private void gtOre(GtOre ore, Block block, @Nullable TagKey<Block> ground) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
        tag(switch (ore.getToolLevel()) {
            case STONE -> BlockTags.NEEDS_STONE_TOOL;
            case IRON -> BlockTags.NEEDS_IRON_TOOL;
            case DIAMOND -> BlockTags.NEEDS_DIAMOND_TOOL;
        }).add(block);
        tag(BlockTags.DRAGON_IMMUNE).add(block);
        TagKey<Block> ores = commonTag("ores/" + ore.getMaterial());
        tag(ores).add(block);
        tag(Tags.Blocks.ORES).addTag(ores);
        if (ground != null) {
            tag(ground).add(block);
        }
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Ic2Explosion.WHITELIST).add(Blocks.BEDROCK);
        for (Ic2Machine machine : Ic2Machine.values()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Ic2Blocks.machine(machine).get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(RpBlocks.PROJECT_TABLE.get());
        IronChestBlocks.BLOCKS.getEntries().forEach(chest -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(chest.get()));
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(EsBlocks.ENDER_CHEST.get(), EsBlocks.ENDER_TANK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Ic2Blocks.MACHINE_CASING.get(),
                Ic2Blocks.ADVANCED_MACHINE_CASING.get(), Ic2Blocks.TIN_ORE.get(), Ic2Blocks.DEEPSLATE_TIN_ORE.get(), Ic2Blocks.TIN_BLOCK.get(), Ic2Blocks.BRONZE_BLOCK.get(),
                Ic2Blocks.IRON_FENCE.get());
        tag(BlockTags.NEEDS_STONE_TOOL).add(Ic2Blocks.TIN_ORE.get(), Ic2Blocks.DEEPSLATE_TIN_ORE.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Ic2Blocks.MINING_PIPE.get(), Ic2Blocks.MINING_TIP.get());
        // IC2 BlockPersonal.canDragonDestroy = false.
        tag(BlockTags.DRAGON_IMMUNE).add(Ic2Blocks.machine(Ic2Machine.PERSONAL_SAFE).get(),
                Ic2Blocks.machine(Ic2Machine.TRADE_O_MAT).get(), Ic2Blocks.machine(Ic2Machine.ENERGY_O_MAT).get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Ic2Blocks.URANIUM_ORE.get(), Ic2Blocks.DEEPSLATE_URANIUM_ORE.get(),
                Ic2Blocks.URANIUM_BLOCK.get(), Ic2Blocks.REACTOR_CHAMBER.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(Ic2Blocks.URANIUM_ORE.get(), Ic2Blocks.DEEPSLATE_URANIUM_ORE.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Ic2Blocks.REINFORCED_STONE.get(), Ic2Blocks.REINFORCED_DOOR.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(Ic2Blocks.REINFORCED_STONE.get(), Ic2Blocks.REINFORCED_DOOR.get());
        tag(BlockTags.DOORS).add(Ic2Blocks.REINFORCED_DOOR.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Ic2Blocks.CONSTRUCTION_FOAM_WALL.get(),
                Ic2Blocks.IRON_SCAFFOLD.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(Ic2Blocks.WOODEN_SCAFFOLD.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(BcBlocks.AUTO_WORKBENCH.get());
        // Reactor meltdown (1.4.7 materials): unbreakable rock/iron blocks → lava; wood, leaves, cloth → fire.
        tag(NuclearReactorBlockEntity.MELTDOWN_LAVA).add(Blocks.BEDROCK, Blocks.REINFORCED_DEEPSLATE,
                Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK);
        tag(NuclearReactorBlockEntity.MELTDOWN_FLAMMABLE).addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(BlockTags.LEAVES).addTag(BlockTags.WOOL);
        for (EngineType type : EngineType.values()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BcBlocks.engine(type).get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BcBlocks.QUARRY.get(), BcBlocks.PUMP.get(), BcBlocks.REFINERY.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FrBlocks.THERMIONIC_FABRICATOR.get(), FrBlocks.CARPENTER.get(),
                FrBlocks.ELECTRICAL_ENGINE.get());
        for (SolarArrayType type : SolarArrayType.values()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(CsBlocks.array(type).get());
        }
        for (var block : PpBlocks.BLOCKS.getEntries()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get());
        }
        for (GtMachine machine : GtMachine.values()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(GtBlocks.machine(machine).get());
        }
        for (GtCasingBlock.Type type : GtCasingBlock.Type.values()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(GtBlocks.casing(type).get());
            tag(BlockTags.NEEDS_IRON_TOOL).add(GtBlocks.casing(type).get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(GtBlocks.HIGHLY_ADVANCED_MACHINE_BLOCK.get(),
                GtBlocks.STEEL_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(GtBlocks.HIGHLY_ADVANCED_MACHINE_BLOCK.get(), GtBlocks.STEEL_BLOCK.get());
        for (GtOre ore : GtOre.values()) {
            gtOre(ore, GtBlocks.ore(ore).get(), switch (ore.getGround()) {
                case STONE -> Tags.Blocks.ORES_IN_GROUND_STONE;
                case NETHERRACK -> Tags.Blocks.ORES_IN_GROUND_NETHERRACK;
                case END_STONE -> null;
            });
            if (ore.hasDeepslateVariant()) {
                gtOre(ore, GtBlocks.deepslateOre(ore).get(), Tags.Blocks.ORES_IN_GROUND_DEEPSLATE);
            }
        }
        // Material.ground: the shovel is the effective tool.
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(BcBlocks.MINING_WELL.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(Ic2Blocks.RUBBER_WOOD.get());
        tag(BlockTags.MINEABLE_WITH_HOE).add(Ic2Blocks.RUBBER_LEAVES.get());
        tag(BlockTags.LOGS_THAT_BURN).add(Ic2Blocks.RUBBER_WOOD.get());
        tag(BlockTags.LEAVES).add(Ic2Blocks.RUBBER_LEAVES.get());
        tag(BlockTags.SAPLINGS).add(Ic2Blocks.RUBBER_SAPLING.get());
        tag(ORES_TIN).add(Ic2Blocks.TIN_ORE.get(), Ic2Blocks.DEEPSLATE_TIN_ORE.get());
        tag(Tags.Blocks.ORES).addTag(ORES_TIN);
        tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(Ic2Blocks.TIN_ORE.get());
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(Ic2Blocks.DEEPSLATE_TIN_ORE.get());
        tag(STORAGE_BLOCKS_TIN).add(Ic2Blocks.TIN_BLOCK.get());
        tag(ORES_URANIUM).add(Ic2Blocks.URANIUM_ORE.get(), Ic2Blocks.DEEPSLATE_URANIUM_ORE.get());
        tag(Tags.Blocks.ORES).addTag(ORES_URANIUM);
        tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(Ic2Blocks.URANIUM_ORE.get());
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(Ic2Blocks.DEEPSLATE_URANIUM_ORE.get());
        tag(STORAGE_BLOCKS_URANIUM).add(Ic2Blocks.URANIUM_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS).addTag(STORAGE_BLOCKS_URANIUM);
        tag(STORAGE_BLOCKS_BRONZE).add(Ic2Blocks.BRONZE_BLOCK.get());
        tag(STORAGE_BLOCKS_STEEL).add(GtBlocks.STEEL_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS).addTag(STORAGE_BLOCKS_TIN).addTag(STORAGE_BLOCKS_BRONZE)
                .addTag(STORAGE_BLOCKS_STEEL);
        // Thermal Expansion ores (BlockOre harvest level 2 = iron) and hardened glass.
        for (String metal : List.of("silver", "lead", "nickel")) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores/" + metal));
            Block ore = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Craft.MODID, metal + "_ore"));
            Block deepslate = BuiltInRegistries.BLOCK.get(
                    ResourceLocation.fromNamespaceAndPath(Craft.MODID, "deepslate_" + metal + "_ore"));
            tag(tag).add(ore, deepslate);
            tag(Tags.Blocks.ORES).addTag(tag);
            tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(ore);
            tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(deepslate);
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ore, deepslate);
            tag(BlockTags.NEEDS_IRON_TOOL).add(ore, deepslate);
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TeBlocks.HARDENED_GLASS.get(), TeBlocks.POWERED_FURNACE.get(),
                TeBlocks.PULVERIZER.get(), TeBlocks.SAWMILL.get(), TeBlocks.INDUCTION_SMELTER.get(),
                TeBlocks.MAGMA_CRUCIBLE.get(), TeBlocks.LIQUID_TRANSPOSER.get(), TeBlocks.GLACIAL_PRECIPITATOR.get(),
                TeBlocks.IGNEOUS_EXTRUDER.get(), TeBlocks.AQUEOUS_ACCUMULATOR.get(), TeBlocks.STEAM_ENGINE.get(),
                TeBlocks.MAGMATIC_ENGINE.get(), TeBlocks.ENERGY_CELL.get(), TeBlocks.ENERGY_TESSERACT.get(),
                TeBlocks.LIQUID_TESSERACT.get(), TeBlocks.ITEM_TESSERACT.get());
        tag(Tags.Blocks.GLASS_BLOCKS).add(TeBlocks.HARDENED_GLASS.get());
        addElectricToolTags();
    }

    /**
     * IC2 electric tools: Forge tool class, material rules and block lists as one tag per tool. Blocks split up
     * since 1.4.7 (coarse dirt, podzol, red sand, soul soil …) count like their base block.
     */
    private void addElectricToolTags() {
        tag(DrillItem.MINEABLE).addTag(BlockTags.MINEABLE_WITH_PICKAXE).addTag(BlockTags.SAND).add(
                Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.ROOTED_DIRT,
                Blocks.MYCELIUM, Blocks.FARMLAND, Blocks.GRAVEL, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.CLAY,
                Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
        tag(ChainsawItem.MINEABLE).addTag(BlockTags.MINEABLE_WITH_AXE).addTag(BlockTags.LEAVES).add(Blocks.COBWEB);
        tag(ChainsawItem.SHEARABLE).addTag(BlockTags.LEAVES).add(Blocks.SHORT_GRASS, Blocks.FERN, Blocks.VINE,
                Blocks.DEAD_BUSH);
        tag(ElectricHoeItem.MINEABLE).add(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.ROOTED_DIRT,
                Blocks.GRASS_BLOCK, Blocks.MYCELIUM);
    }
}
