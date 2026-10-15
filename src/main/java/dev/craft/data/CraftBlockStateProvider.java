package dev.craft.data;

import com.google.gson.JsonObject;
import dev.craft.Craft;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.block.RubberWoodBlock;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.client.CableModel;
import dev.craft.ic2.machine.MachineBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CraftBlockStateProvider extends BlockStateProvider {
    /**
     * IC2 {@code BlockMultiID.sideAndFacingToSpriteOffset[side][facing]}: sprite row (0 bottom, 1 top, 2 back,
     * 3 front, 4/5 sides) for each block side and facing, both in 3D data order (down, up, north, south, west, east).
     */
    private static final int[][] SIDE_AND_FACING_TO_ROW = {
            {3, 2, 0, 0, 0, 0}, {2, 3, 1, 1, 1, 1}, {1, 1, 3, 2, 5, 4},
            {0, 0, 2, 3, 4, 5}, {4, 5, 4, 5, 3, 2}, {5, 4, 5, 4, 2, 3}
    };
    private static final String[] ROW_NAMES = {"bottom", "top", "back", "front", "left", "right"};

    public CraftBlockStateProvider(PackOutput output, ExistingFileHelper files) {
        super(output, Craft.MODID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        for (CableType type : CableType.values()) {
            BlockModelBuilder model = models().getBuilder("block/cable/" + type.getName())
                    .texture("particle", CableModel.cableTexture(type, 0))
                    .customLoader((parent, files) -> new CableLoaderBuilder(parent, files, type)).end();
            getMultipartBuilder(Ic2Blocks.cable(type).get()).part().modelFile(model).addModel().end();
        }
        for (Ic2Machine machine : Ic2Machine.values()) {
            machine(machine);
        }
        simpleBlockWithItem(Ic2Blocks.MACHINE_CASING.get(), cubeAll(Ic2Blocks.MACHINE_CASING.get()));
        simpleBlockWithItem(Ic2Blocks.TIN_ORE.get(), cubeAll(Ic2Blocks.TIN_ORE.get()));
        simpleBlockWithItem(Ic2Blocks.DEEPSLATE_TIN_ORE.get(), cubeAll(Ic2Blocks.DEEPSLATE_TIN_ORE.get()));
        simpleBlockWithItem(Ic2Blocks.TIN_BLOCK.get(), cubeAll(Ic2Blocks.TIN_BLOCK.get()));
        simpleBlockWithItem(Ic2Blocks.BRONZE_BLOCK.get(), cubeAll(Ic2Blocks.BRONZE_BLOCK.get()));
        rubberWood();
        simpleBlockWithItem(Ic2Blocks.RUBBER_LEAVES.get(), models().withExistingParent("rubber_leaves",
                mcLoc("block/leaves")).texture("all", modLoc("block/rubber_leaves")).renderType("cutout_mipped"));
        simpleBlock(Ic2Blocks.RUBBER_SAPLING.get(), models().cross("rubber_sapling", modLoc("block/rubber_sapling"))
                .renderType("cutout"));
        resinSheet();
        engines();
    }

    /** Engines are drawn by their block entity renderer; the block model only provides the break particles. */
    private void engines() {
        for (EngineType type : EngineType.values()) {
            String particle = switch (type) {
                case REDSTONE -> "block/oak_planks";
                case STIRLING -> "block/cobblestone";
                case COMBUSTION -> "block/iron_block";
            };
            simpleBlock(BcBlocks.engine(type).get(), models().getBuilder(type.getName())
                    .texture("particle", mcLoc(particle)));
        }
    }

    /** IC2 {@code BlockResin}: a 2 px high layer with the resin texture on every face. */
    private void resinSheet() {
        ResourceLocation texture = modLoc("block/resin_sheet");
        BlockModelBuilder model = models().withExistingParent("resin_sheet", mcLoc("block/block"))
                .texture("particle", texture).texture("texture", texture).renderType("cutout")
                .element().from(0, 0, 0).to(16, 2, 16)
                .allFaces((direction, face) -> face.texture("#texture")
                        .cullface(direction == Direction.UP ? null : direction))
                .end();
        simpleBlock(Ic2Blocks.RESIN_SHEET.get(), model);
    }

    /** One model per facing and active state, with IC2's face-to-sprite mapping (no rotations). */
    private void machine(Ic2Machine machine) {
        MachineBlock block = Ic2Blocks.machine(machine).get();
        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(MachineBlock.FACING);
            boolean active = state.getValue(MachineBlock.ACTIVE);
            return ConfiguredModel.builder().modelFile(machineModel(machine, facing, active)).build();
        });
        simpleBlockItem(block, machineModel(machine, Direction.NORTH, false));
    }

    private ModelFile machineModel(Ic2Machine machine, Direction facing, boolean active) {
        String name = machine.getName() + "_" + facing.getSerializedName() + (active ? "_active" : "");
        int f = facing.get3DDataValue();
        return models().cube("block/machine/" + name,
                        machineTexture(machine, SIDE_AND_FACING_TO_ROW[0][f], active),
                        machineTexture(machine, SIDE_AND_FACING_TO_ROW[1][f], active),
                        machineTexture(machine, SIDE_AND_FACING_TO_ROW[2][f], active),
                        machineTexture(machine, SIDE_AND_FACING_TO_ROW[3][f], active),
                        machineTexture(machine, SIDE_AND_FACING_TO_ROW[5][f], active),
                        machineTexture(machine, SIDE_AND_FACING_TO_ROW[4][f], active))
                .texture("particle", machineTexture(machine, 3, active));
    }

    private ResourceLocation machineTexture(Ic2Machine machine, int row, boolean active) {
        return modLoc("block/machine/" + machine.getName() + "_" + ROW_NAMES[row] + (active ? "_active" : ""));
    }

    /** Rubber wood: plain sides, or a wet/dry resin hole on the {@code facing} side. */
    private void rubberWood() {
        RubberWoodBlock block = Ic2Blocks.RUBBER_WOOD.get();
        ResourceLocation side = modLoc("block/rubber_wood");
        ResourceLocation top = modLoc("block/rubber_wood_top");
        ModelFile plain = models().cubeColumn("rubber_wood", side, top);
        getVariantBuilder(block).forAllStates(state -> {
            RubberWoodBlock.Resin resin = state.getValue(RubberWoodBlock.RESIN);
            if (!resin.hasHole()) {
                return ConfiguredModel.builder().modelFile(plain).build();
            }
            Direction facing = state.getValue(RubberWoodBlock.FACING);
            ResourceLocation hole = modLoc("block/rubber_wood_" + resin.getSerializedName());
            String name = "rubber_wood_" + resin.getSerializedName() + "_" + facing.getSerializedName();
            ModelFile model = models().cube(name, top, top,
                            facing == Direction.NORTH ? hole : side, facing == Direction.SOUTH ? hole : side,
                            facing == Direction.EAST ? hole : side, facing == Direction.WEST ? hole : side)
                    .texture("particle", side);
            return ConfiguredModel.builder().modelFile(model).build();
        });
        simpleBlockItem(block, plain);
    }

    private static final class CableLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder> {
        private final CableType type;

        CableLoaderBuilder(BlockModelBuilder parent, ExistingFileHelper files, CableType type) {
            super(CableModel.LOADER_ID, parent, files, false);
            this.type = type;
        }

        @Override
        public JsonObject toJson(JsonObject json) {
            json = super.toJson(json);
            json.addProperty("cable", type.getName());
            return json;
        }
    }
}
