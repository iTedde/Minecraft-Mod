package dev.craft.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import dev.craft.Craft;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.builders.MarkerBlock;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.factory.TankBlock;
import dev.craft.forestry.FrBlocks;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.machine.GtMachineBlock;
import dev.craft.gregtech.world.GtOre;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.block.RubberWoodBlock;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.client.CableModel;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.compactsolars.CsBlocks;
import dev.craft.compactsolars.SolarArrayType;
import dev.craft.powerconverters.ConverterType;
import dev.craft.powerconverters.PcBlocks;
import dev.craft.powerconverters.block.PowerConverterBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
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
        simpleBlockWithItem(Ic2Blocks.ADVANCED_MACHINE_CASING.get(), cubeAll(Ic2Blocks.ADVANCED_MACHINE_CASING.get()));
        simpleBlockWithItem(Ic2Blocks.TIN_ORE.get(), cubeAll(Ic2Blocks.TIN_ORE.get()));
        simpleBlockWithItem(Ic2Blocks.DEEPSLATE_TIN_ORE.get(), cubeAll(Ic2Blocks.DEEPSLATE_TIN_ORE.get()));
        simpleBlockWithItem(Ic2Blocks.TIN_BLOCK.get(), cubeAll(Ic2Blocks.TIN_BLOCK.get()));
        simpleBlockWithItem(Ic2Blocks.URANIUM_ORE.get(), cubeAll(Ic2Blocks.URANIUM_ORE.get()));
        simpleBlockWithItem(Ic2Blocks.DEEPSLATE_URANIUM_ORE.get(), cubeAll(Ic2Blocks.DEEPSLATE_URANIUM_ORE.get()));
        simpleBlockWithItem(Ic2Blocks.URANIUM_BLOCK.get(), models().cubeColumn("uranium_block",
                modLoc("block/uranium_block"), modLoc("block/uranium_block_top")));
        simpleBlockWithItem(Ic2Blocks.REACTOR_CHAMBER.get(), models().cubeBottomTop("reactor_chamber",
                modLoc("block/reactor_chamber_side"), modLoc("block/reactor_chamber_bottom"),
                modLoc("block/reactor_chamber_top")));
        simpleBlockWithItem(Ic2Blocks.BRONZE_BLOCK.get(), cubeAll(Ic2Blocks.BRONZE_BLOCK.get()));
        ic2MiningPipe();
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
        forestryMachines();
        powerConverters();
        compactSolars();
        gregTech();
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

    /** Forestry machines are drawn by their block entity renderer; the top face of the box texture gives particles. */
    private void forestryMachines() {
        horizontalBlock(FrBlocks.THERMIONIC_FABRICATOR.get(), models().getBuilder("thermionic_fabricator")
                .texture("particle", modLoc("block/thermionic_fabricator_particle")));
        horizontalBlock(FrBlocks.CARPENTER.get(), models().getBuilder("carpenter")
                .texture("particle", modLoc("block/carpenter_particle")));
        // BlockEngine: drawn by its renderer like the BuildCraft engines; particles from the base texture.
        simpleBlock(FrBlocks.ELECTRICAL_ENGINE.get(), models().getBuilder("electrical_engine")
                .texture("particle", modLoc("block/electrical_engine_particle")));
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
    /** IC2 {@code RenderBlockMiningPipe}: 6–10 px column, also in the inventory. */
    private void ic2MiningPipe() {
        ResourceLocation texture = modLoc("block/ic2_mining_pipe");
        simpleBlockWithItem(Ic2Blocks.MINING_PIPE.get(), models().withExistingParent("ic2_mining_pipe",
                        mcLoc("block/block")).texture("particle", texture).texture("pipe", texture).renderType("cutout")
                .element().from(6, 0, 6).to(10, 16, 10).allFaces((d, f) -> f.texture("#pipe")).end());
    }

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
        simpleBlockWithItem(Ic2Blocks.REINFORCED_GLASS.get(), models().cubeAll("reinforced_glass",
                modLoc("block/reinforced_glass")).renderType("cutout"));
        simpleBlockWithItem(Ic2Blocks.INDUSTRIAL_TNT.get(), models().cubeBottomTop("industrial_tnt",
                modLoc("block/industrial_tnt_side"), modLoc("block/industrial_tnt_bottom"),
                modLoc("block/industrial_tnt_top")));
        simpleBlockWithItem(Ic2Blocks.NUKE.get(), models().cubeBottomTop("nuke", modLoc("block/nuke_side"),
                modLoc("block/nuke_bottom"), modLoc("block/nuke_top")));
        fenceBlock(Ic2Blocks.IRON_FENCE.get(), modLoc("block/iron_fence"));
        simpleBlockItem(Ic2Blocks.IRON_FENCE.get(), models().fenceInventory("iron_fence_inventory",
                modLoc("block/iron_fence")));
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

    /**
     * Power Converters: all sides share one texture; a side shows the "connected" texture (index + 1) while its side
     * property is set, so the state is a multipart of one single-face model per side.
     */
    /** Compact Solars: sheet row = tier, columns side / top / bottom ({@code BlockCompactSolar}). */
    // --- GregTech machines: texture per side from facing, main facing and active state (spec 13 §1.9) ---

    private void gregTech() {
        for (GtMachine machine : GtMachine.values()) {
            GtMachineBlock block = GtBlocks.machine(machine).get();
            Map<String, ModelFile> models = new HashMap<>();
            getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(gtModel(machine, models,
                    state.getValue(GtMachineBlock.FACING), state.getValue(GtMachineBlock.FRONT).getDirection(),
                    state.getValue(GtMachineBlock.ACTIVE))).build());
            simpleBlockItem(block, gtModel(machine, models, Direction.NORTH, Direction.NORTH, false));
        }
        for (GtCasingBlock.Type type : GtCasingBlock.Type.values()) {
            simpleBlockWithItem(GtBlocks.casing(type).get(), models().cubeAll(type.getName(),
                    modLoc("block/gt/blocks_" + type.getTexture())));
        }
        // All six sides share index 10 on blocks.png.
        simpleBlockWithItem(GtBlocks.HIGHLY_ADVANCED_MACHINE_BLOCK.get(),
                models().cubeAll("highly_advanced_machine_block", modLoc("block/gt/blocks_10")));
        for (GtOre ore : GtOre.values()) {
            gtOre(GtBlocks.ore(ore).get(), ore.getBlockName(), ore.getMaterial());
            if (ore.hasDeepslateVariant()) {
                gtOre(GtBlocks.deepslateOre(ore).get(), ore.getDeepslateBlockName(), "deepslate_" + ore.getMaterial());
            }
        }
    }

    /**
     * GT picks one of six face images per side by {@code |side ^ x ^ y ^ z| % 6}; here six models with face
     * {@code (side ^ k) % 6}, chosen randomly per position.
     */
    private void gtOre(Block block, String name, String texture) {
        ConfiguredModel[] variants = new ConfiguredModel[6];
        for (int k = 0; k < variants.length; k++) {
            ResourceLocation[] faces = new ResourceLocation[6];
            for (int side = 0; side < faces.length; side++) {
                faces[side] = modLoc("block/gt/ore/" + texture + "_" + (side ^ k) % 6);
            }
            ModelFile model = models().cube("gt/ore/" + name + "_" + k, faces[0], faces[1], faces[2], faces[3],
                    faces[5], faces[4]).texture("particle", faces[0]);
            variants[k] = new ConfiguredModel(model);
        }
        getVariantBuilder(block).partialState().setModels(variants);
        simpleBlockItem(block, variants[0].model);
    }

    private ModelFile gtModel(GtMachine machine, Map<String, ModelFile> models, Direction facing,
                              @Nullable Direction front, boolean active) {
        int[] textures = new int[6];
        for (Direction side : Direction.values()) {
            textures[side.get3DDataValue()] = gtTexture(machine, side, facing, front, active);
        }
        String key = Arrays.stream(textures).mapToObj(String::valueOf).collect(Collectors.joining("_"));
        return models.computeIfAbsent(key, k -> models().cube("block/gt/" + machine.getName() + "_" + k,
                        gtLoc(textures[0]), gtLoc(textures[1]), gtLoc(textures[2]), gtLoc(textures[3]),
                        gtLoc(textures[5]), gtLoc(textures[4]))
                .texture("particle", gtLoc(machine.getFrontTexture(false))));
    }

    /** {@code getTextureIndex}: basic machine (spec 13 §1.9), side layout (bottom 32, own top and sides) or front layout. */
    private static int gtTexture(GtMachine machine, Direction side, Direction facing, @Nullable Direction front,
                                 boolean active) {
        if (machine.getLayout() == GtMachine.Layout.SIDES) {
            return side == Direction.DOWN ? 32 : side == Direction.UP ? machine.getTopTexture(active)
                    : machine.getFrontTexture(active);
        }
        if (machine.getLayout() == GtMachine.Layout.FRONT) {
            return side == facing ? machine.getFrontTexture(active) : side == facing.getOpposite()
                    ? GtMachine.FRONT_LAYOUT_BACK : machine.getSideTexture();
        }
        if (machine.getLayout() == GtMachine.Layout.TOP) {
            return switch (side) {
                case UP -> machine.getFrontTexture(active);
                case DOWN -> GtMachine.TOP_LAYOUT_BOTTOM;
                case EAST, WEST -> GtMachine.TOP_LAYOUT_EAST_WEST;
                default -> GtMachine.TOP_LAYOUT_NORTH_SOUTH;
            };
        }
        if (front == null ? side == facing : side == front) {
            return machine.getFrontTexture(active);
        }
        if (front != null && side == facing.getOpposite()) {
            return side == Direction.DOWN ? 38 : side == Direction.UP ? 79 : 36;
        }
        return side == Direction.DOWN ? 32 : side == Direction.UP ? machine.getTopTexture(active) : 40;
    }

    private ResourceLocation gtLoc(int index) {
        return modLoc("block/gt/terrain_" + index);
    }

    private void compactSolars() {
        for (SolarArrayType type : SolarArrayType.values()) {
            String texture = "block/compact_solars/" + type.getName() + "_";
            simpleBlockWithItem(CsBlocks.array(type).get(), models().cubeBottomTop(type.getName(),
                    modLoc(texture + "side"), modLoc(texture + "bottom"), modLoc(texture + "top")));
        }
    }

    private void powerConverters() {
        simpleBlockWithItem(PcBlocks.ENERGY_BRIDGE.get(),
                models().cubeAll("energy_bridge", modLoc("block/power_converter/energy_bridge")));
        for (Direction side : Direction.values()) {
            models().getBuilder("power_converter_" + side.getName()).texture("particle", "#face")
                    .element().from(0, 0, 0).to(16, 16, 16).face(side).texture("#face").cullface(side).end().end();
        }
        powerConverter(PcBlocks.UNIVERSAL_CHARGER.get(), "universal_charger");
        for (ConverterType type : ConverterType.values()) {
            powerConverter(PcBlocks.converter(type).get(), type.getName());
        }
    }

    private void powerConverter(Block block, String name) {
        ResourceLocation texture = modLoc("block/power_converter/" + name);
        ResourceLocation connected = modLoc("block/power_converter/" + name + "_connected");
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        for (Direction side : Direction.values()) {
            for (boolean on : new boolean[]{false, true}) {
                ModelFile face = models().withExistingParent(name + "_" + side.getName() + (on ? "_connected" : ""),
                        modLoc("block/power_converter_" + side.getName())).texture("face", on ? connected : texture);
                builder.part().modelFile(face).addModel().condition(PowerConverterBlock.property(side), on).end();
            }
        }
        simpleBlockItem(block, models().cubeAll(name, texture));
    }
}
