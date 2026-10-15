package dev.craft.data;

import com.google.gson.JsonObject;
import dev.craft.Craft;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.builders.MarkerBlock;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.factory.TankBlock;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.block.RubberWoodBlock;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.client.CableModel;
import dev.craft.ic2.machine.MachineBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.PipeBlock;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.joml.Vector3f;

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
        simpleBlock(BcBlocks.OIL.get(), models().getBuilder("oil").texture("particle", modLoc("block/oil_still")));
        landmark();
        quarry();
        frame();
        tank();
        pump();
        refinery();
        miningWell();
        miningPipe();
    }

    /** BuildCraft {@code RenderingMarkers}: a torch-like stick, turned so that it points away from its support. */
    private void landmark() {
        ResourceLocation texture = modLoc("block/landmark");
        BlockModelBuilder model = models().withExistingParent("landmark", mcLoc("block/block"))
                .texture("particle", texture).texture("torch", texture).renderType("cutout").ao(false)
                .element().from(7, 0, 7).to(9, 10, 9)
                .face(Direction.DOWN).uvs(7, 13, 9, 15).texture("#torch").end()
                .face(Direction.UP).uvs(7, 6, 9, 8).texture("#torch").end()
                .face(Direction.NORTH).uvs(7, 6, 9, 16).texture("#torch").end()
                .face(Direction.SOUTH).uvs(7, 6, 9, 16).texture("#torch").end()
                .face(Direction.WEST).uvs(7, 6, 9, 16).texture("#torch").end()
                .face(Direction.EAST).uvs(7, 6, 9, 16).texture("#torch").end()
                .end();
        getVariantBuilder(BcBlocks.MARKER.get()).forAllStates(state -> {
            Direction facing = state.getValue(MarkerBlock.FACING);
            int x = facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 90 : 0;
            int y = facing.getAxis().isHorizontal() ? ((int) facing.toYRot() + 180) % 360 : 0;
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
        });
    }

    /** BuildCraft {@code BlockQuarry}: front 39 on the facing side, top 40, every other side (incl. bottom) 41. */
    private void quarry() {
        ModelFile model = models().orientableWithBottom("quarry", modLoc("block/quarry_side"),
                modLoc("block/quarry_front"), modLoc("block/quarry_side"), modLoc("block/quarry_top"));
        horizontalBlock(BcBlocks.QUARRY.get(), model);
        simpleBlockItem(BcBlocks.QUARRY.get(), model);
    }

    /** BuildCraft {@code BlockTank}: 2–14 px glass box; top/bottom 98, sides 96 or 97 with a tank below. */
    private void tank() {
        ModelFile single = tankModel("tank", modLoc("block/tank_side"));
        ModelFile joined = tankModel("tank_joined", modLoc("block/tank_side_joined"));
        getVariantBuilder(BcBlocks.TANK.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(TankBlock.JOINED) ? joined : single).build());
        simpleBlockItem(BcBlocks.TANK.get(), single);
    }

    private ModelFile tankModel(String name, ResourceLocation side) {
        ResourceLocation top = modLoc("block/tank_top");
        return models().withExistingParent(name, mcLoc("block/block")).texture("particle", side)
                .texture("side", side).texture("top", top).renderType("cutout")
                .element().from(2, 0, 2).to(14, 16, 14)
                .allFaces((d, f) -> f.texture(d.getAxis() == Direction.Axis.Y ? "#top" : "#side")).end();
    }

    /** BuildCraft {@code BlockPump}: bottom 100, top 101, sides 99. */
    private void pump() {
        simpleBlockWithItem(BcBlocks.PUMP.get(), models().cubeBottomTop("pump", modLoc("block/pump_side"),
                modLoc("block/pump_bottom"), modLoc("block/pump_top")));
    }

    /** The refinery is drawn by its block entity renderer; texture 0 of the sheet gives the (empty) particles. */
    private void refinery() {
        horizontalBlock(BcBlocks.REFINERY.get(), models().getBuilder("refinery")
                .texture("particle", modLoc("block/refinery_particle")));
    }

    /** BuildCraft {@code BlockMiningWell}: front 35, top 36, bottom and back 38, other sides 37. */
    private void miningWell() {
        ModelFile model = models().cube("mining_well", modLoc("block/mining_well_back"),
                        modLoc("block/mining_well_top"), modLoc("block/mining_well_front"),
                        modLoc("block/mining_well_back"), modLoc("block/mining_well_side"),
                        modLoc("block/mining_well_side"))
                .texture("particle", modLoc("block/mining_well_side"));
        horizontalBlock(BcBlocks.MINING_WELL.get(), model);
        simpleBlockItem(BcBlocks.MINING_WELL.get(), model);
    }

    /** BuildCraft {@code BlockPlainPipe}: 4–12 px column with texture 32. */
    private void miningPipe() {
        ResourceLocation texture = modLoc("block/mining_pipe");
        simpleBlock(BcBlocks.MINING_PIPE.get(), models().withExistingParent("mining_pipe", mcLoc("block/block"))
                .texture("particle", texture).texture("pipe", texture).renderType("cutout")
                .element().from(4, 0, 4).to(12, 16, 12).allFaces((d, f) -> f.texture("#pipe")).end());
    }

    /** BuildCraft {@code BlockFrame}: legacy pipe shape, core plus an arm to every neighbouring frame. */
    private void frame() {
        ResourceLocation texture = modLoc("block/frame");
        ModelFile core = models().withExistingParent("frame_core", mcLoc("block/block"))
                .texture("particle", texture).texture("frame", texture).renderType("cutout")
                .element().from(4, 4, 4).to(12, 12, 12).allFaces((d, f) -> f.texture("#frame")).end();
        MultiPartBlockStateBuilder builder = getMultipartBuilder(BcBlocks.FRAME.get()).part().modelFile(core)
                .addModel().end();
        for (Direction side : Direction.values()) {
            Vector3f from = new Vector3f(4, 4, 4);
            Vector3f to = new Vector3f(12, 12, 12);
            switch (side) {
                case DOWN -> { from.y = 0; to.y = 4; }
                case UP -> { from.y = 12; to.y = 16; }
                case NORTH -> { from.z = 0; to.z = 4; }
                case SOUTH -> { from.z = 12; to.z = 16; }
                case WEST -> { from.x = 0; to.x = 4; }
                case EAST -> { from.x = 12; to.x = 16; }
            }
            ModelFile arm = models().withExistingParent("frame_" + side.getName(), mcLoc("block/block"))
                    .texture("particle", texture).texture("frame", texture).renderType("cutout")
                    .element().from(from.x, from.y, from.z).to(to.x, to.y, to.z)
                    .allFaces((d, f) -> f.texture("#frame").cullface(d == side ? side : null)).end();
            builder.part().modelFile(arm).addModel().condition(PipeBlock.PROPERTY_BY_DIRECTION.get(side), true).end();
        }
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
