/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.SyncedBlockEntity;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlassTubeBlock extends BaseEntityBlock {
    public static final MapCodec<GlassTubeBlock> CODEC = simpleCodec(GlassTubeBlock::new);

    public static final EnumProperty<IODirection> IN = EnumProperty.create("in", IODirection.class);
    public static final EnumProperty<IODirection> OUT = EnumProperty.create("out", IODirection.class);
    public static final BooleanProperty EXTENDED_IN = BooleanProperty.create("extended_in");
    public static final BooleanProperty EXTENDED_OUT = BooleanProperty.create("extended_out");

    public static final double RADIUS = 0.06;
    private static final VoxelShape DEFAULT_SHAPE = Shapes.box(0.4, 0.4, 0.4, 0.6, 0.6, 0.6);
    private static final Function<Direction, VoxelShape> SHAPE_PART_CACHE = createShapePartCache(RADIUS, 0, 1);

    public static Function<Direction, VoxelShape> createShapePartCache(double radius, double offset, double length) {
        return Util.memoize(direction -> {
            return Shapes.box(
                    direction.getStepX() * offset + 0.5 + Math.min(-radius, direction.getStepX() * 0.5 * length),
                    direction.getStepY() * offset + 0.5 + Math.min(-radius, direction.getStepY() * 0.5 * length),
                    direction.getStepZ() * offset + 0.5 + Math.min(-radius, direction.getStepZ() * 0.5 * length),
                    direction.getStepX() * offset + 0.5 + Math.max(radius, direction.getStepX() * 0.5 * length),
                    direction.getStepY() * offset + 0.5 + Math.max(radius, direction.getStepY() * 0.5 * length),
                    direction.getStepZ() * offset + 0.5 + Math.max(radius, direction.getStepZ() * 0.5 * length)
                );
        });
    }

    private static final Function<BlockState, VoxelShape> SHAPE_CACHE = Util.memoize(state -> {
        return Stream.of(state.getValue(IN), state.getValue(OUT))
            .map(IODirection::getDirection).flatMap(Optional::stream)
            .map(SHAPE_PART_CACHE).reduce(Shapes::or)
            .orElse(DEFAULT_SHAPE);
    });

    static EnumProperty<IODirection> getInverseProperty(EnumProperty<IODirection> property) {
        return property == IN ? OUT : IN;
    }

    protected GlassTubeBlock(Properties settings) {
        super(settings);
        registerDefaultState(stateDefinition.any()
                .setValue(IN, IODirection.NONE).setValue(EXTENDED_IN, false)
                .setValue(OUT, IODirection.NONE).setValue(EXTENDED_OUT, false)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IN, OUT, EXTENDED_IN, EXTENDED_OUT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE_CACHE.apply(state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        if (stack.is(Items.STICK)) {
            world.setBlock(pos, state
                    .setValue(IN, state.getValue(OUT))
                    .setValue(OUT, state.getValue(IN))
                    .setValue(EXTENDED_IN, state.getValue(EXTENDED_OUT))
                    .setValue(EXTENDED_OUT, state.getValue(EXTENDED_IN)), Block.UPDATE_KNOWN_SHAPE);
            world.playSound(player, pos, state.getSoundType().getPlaceSound(), SoundSource.PLAYERS);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        IODirection placedDir = IODirection.LOOKUP.get(ctx.getClickedFace());
        BlockState state = super.getStateForPlacement(ctx);

        Level world = ctx.getLevel();
        BlockPos neighborPos = ctx.getClickedPos().relative(ctx.getClickedFace().getOpposite());
        BlockState neighborState = world.getBlockState(neighborPos);

        return updateExtensions(getConnectionsForRedirection(world, neighborPos, neighborState)
                .findFirst()
                .map(placingConnection -> {
                    IODirection other = getValidConnectionDirections(ctx.getLevel(), ctx.getClickedPos(), placingConnection, placedDir.getOpposite())
                            .findFirst()
                            .orElse(placedDir);

                    return state
                            .setValue(getInverseProperty(placingConnection), placedDir.getOpposite())
                            .setValue(placingConnection, other);
                }).orElseGet(() -> {
                    // try to place output against neighbour
                    IODirection in = getValidConnectionDirections(ctx.getLevel(), ctx.getClickedPos(), OUT, placedDir).findFirst().orElse(placedDir.getOpposite());
                    IODirection out = getValidConnectionDirections(ctx.getLevel(), ctx.getClickedPos(), IN, in).findFirst().orElse(in.getOpposite());
                    return setDirection(state, in, out);
                }), world, ctx.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        return updateExtensions(setDirection(state,
                getConnectionStateForNeighborUpdate(pos, state, direction, neighborPos, neighborState, world, IN),
                getConnectionStateForNeighborUpdate(pos, state, direction, neighborPos, neighborState, world, OUT)
        ), world, pos);
    }

    private BlockState updateExtensions(BlockState state, LevelAccessor world, BlockPos pos) {
        IODirection in = state.getValue(IN);
        IODirection out = state.getValue(OUT);
        return state.setValue(EXTENDED_IN, in != IODirection.NONE && canConnectInto(world, pos, in.direction))
                .setValue(EXTENDED_OUT, out != IODirection.NONE && canConnectInto(world, pos, out.direction));
    }

    private boolean canConnectInto(LevelAccessor world, BlockPos pos, Direction direction) {
        pos = pos.relative(direction);
        BlockState state = world.getBlockState(pos);
        return !(state.getBlock() instanceof GlassTubeBlock) && PipeInsertable.getPipeInterableAt(world, state, pos) != null;
    }

    private IODirection getConnectionStateForNeighborUpdate(BlockPos pos, BlockState state, Direction direction, BlockPos neighborPos, BlockState neighbor, LevelAccessor world, EnumProperty<IODirection> property) {
        IODirection current = state.getValue(property);

        if (PipeInsertable.canConnectWith(world, state, pos, neighbor, neighborPos, direction, property == IN)) {
            if (current == IODirection.NONE || !PipeInsertable.canConnectWith(world, state, pos,
                    world.getBlockState(pos.relative(current.direction)),
                    pos.relative(current.direction), current.direction, property == IN)
                ) {
                return IODirection.LOOKUP.get(direction);
            }
        } else if (current.direction == direction) {
            return IODirection.NONE;
        }
        return current;
    }

    protected boolean isBlocked(BlockState state) {
        return false;
    }

    public BlockState setDirection(BlockState state, IODirection in, IODirection out) {
        if (in != IODirection.NONE || out != IODirection.NONE) {
            in = in == IODirection.NONE ? out.getOpposite() : in;
            out = out == IODirection.NONE || in == out ? in.getOpposite() : out;
        }
        return state.setValue(IN, in).setValue(OUT, out);
    }

    private static Stream<IODirection> getValidConnectionDirections(LevelAccessor world, BlockPos pos, EnumProperty<IODirection> property, IODirection exclude) {
        return List.of(IODirection.NORTH, IODirection.SOUTH, IODirection.EAST, IODirection.WEST, IODirection.DOWN, IODirection.UP).stream().filter(direction -> {
            if (direction == exclude) {
                return false;
            }
            BlockPos neighborPos = pos.relative(direction.direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            IODirection neighborComplimentaryDirection = neighborState.getOptionalValue(getInverseProperty(property)).orElse(null);
            if (neighborComplimentaryDirection == null
                    && !PipeInsertable.canConnectWith(world, neighborState, pos, neighborState, neighborPos, direction.direction, property == IN)) {
                return false;
            }
            return getConnectionsForRedirection(world, neighborPos, neighborState).anyMatch(openConnection -> openConnection == getInverseProperty(property));
        });
    }

    private static Stream<EnumProperty<IODirection>> getConnectionsForRedirection(LevelAccessor world, BlockPos pos, BlockState state) {
        return Stream.of(IN, OUT).filter(property -> {
            IODirection currentDirection = state.getOptionalValue(property).orElse(IODirection.NONE);
            if (currentDirection == IODirection.NONE) {
                return false;
            }
            BlockPos neighborPos = pos.relative(currentDirection.direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            IODirection neighborComplimentaryDirection = neighborState.getOptionalValue(getInverseProperty(property)).orElse(null);
            if (neighborComplimentaryDirection == null
                    && !PipeInsertable.canConnectWith(world, state, pos, neighborState, neighborPos, currentDirection.direction, property == IN)) {
                return true;
            }
            return neighborComplimentaryDirection != null
                && neighborComplimentaryDirection != currentDirection.getOpposite();
        });
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        world.getBlockEntity(pos, PSBlockEntities.GLASS_TUBE).ifPresent(data -> {
            data.pushContentsForward(world, pos, state.getValue(OUT));
        });
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
        if (world instanceof ServerLevel sw) {
            world.getBlockEntity(pos, PSBlockEntities.GLASS_TUBE).ifPresent(data -> {
                data.pushContentsForward(sw, pos, state.getValue(OUT));
                data.pullContentsForward(sw, pos);
            });
        }
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return world.getBlockEntity(pos, PSBlockEntities.GLASS_TUBE).map(data -> (int)((data.contents.size() / 10F) * 15)).orElse(0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Data(pos, state);
    }

    public enum IODirection implements StringRepresentable {
        NONE(null),
        UP(Direction.UP),
        DOWN(Direction.DOWN),
        NORTH(Direction.NORTH),
        SOUTH(Direction.SOUTH),
        EAST(Direction.EAST),
        WEST(Direction.WEST);

        public static final Map<Direction, IODirection> LOOKUP = Arrays.stream(values())
                .filter(i -> i.getDirection().isPresent())
                .collect(Collectors.toMap(i -> i.getDirection().get(), Function.identity()));
        static final List<IODirection> VALUES = LOOKUP.values().stream().toList();

        private final Direction direction;
        private final String name = name().toLowerCase(Locale.ROOT);

        IODirection(@Nullable Direction direction) {
            this.direction = direction;
        }

        public IODirection getOpposite() {
            return direction == null ? this : LOOKUP.getOrDefault(direction.getOpposite(), NONE);
        }

        public Optional<Direction> getDirection() {
            return Optional.ofNullable(direction);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    static int getTemperatureDropPerTransfer(ServerLevel world, BlockPos center) {
        return 1 + BlockPos.withinManhattanStream(center, 1, 1, 1).mapToInt(pos -> {
            return pos.equals(center) ? 0 : getTemperatureDropFor(world, pos);
        }).sum();
    }

    static int getTemperatureDropFor(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.is(BlockTags.ICE) || state.is(BlockTags.SNOW)) {
            return 1;
        }
        if (state.is(BlockTags.FIRE) || (state.is(BlockTags.CAMPFIRES) && state.getOptionalValue(BlockStateProperties.LIT).orElse(false))) {
            return -1;
        }
        if (state.getFluidState().is(FluidTags.LAVA)) {
            return -4;
        }

        return 0;
    }

    public static class Data extends SyncedBlockEntity implements PipeInsertable {
        private final List<PipeFluids> contents = new LinkedList<>();

        private int temperatureUpdateCooldown;
        private int temperatureDrop;

        public Data(BlockPos pos, BlockState state) {
            super(PSBlockEntities.GLASS_TUBE, pos, state);
        }

        public List<PipeFluids> getContents() {
            return contents;
        }

        private int getTemperatureDrop(ServerLevel world, BlockPos pos) {
            if (temperatureUpdateCooldown == 0) {
                temperatureUpdateCooldown = 200;
                temperatureDrop = getTemperatureDropPerTransfer(world, pos);
            }
            return temperatureDrop;
        }

        public Either<PipeFluids, Unit> receiveContents(ServerLevel world, BlockPos pos, BlockState state, PipeFluids contents) {
            pushContentsForward(world, pos, state.getValue(OUT));
            var status = Optional.of(contents).filter(i -> !i.isEmpty()).map(i -> i.withTemperature(i.temperature() - getTemperatureDrop(world, pos))).map(newContents -> {
                if (this.contents.size() < 10) {
                    this.contents.addFirst(newContents);
                } else {
                    PipeFluids first = this.contents.getFirst();
                    if (first.isEmpty()) {
                        this.contents.removeFirst();
                        this.contents.addFirst(contents);
                    } else {
                        Optional<PipeFluids> available = this.contents.stream().filter(i -> i.fluids().totalSize() < 100).findFirst();
                        if (available.isEmpty()) {
                            return PipeInsertable.reject(contents);
                        }
                        this.contents.set(this.contents.indexOf(available.get()), available.get().combine(newContents));
                    }
                }
                setChanged();

                return STATUS_ACCEPT_ALL;
            }).orElse(STATUS_ACCEPT_ALL);

            scheduleNextTick(world);
            return status;
        }

        public void pushContentsForward(ServerLevel world, BlockPos pos, IODirection direction) {
            if (((GlassTubeBlock)getBlockState().getBlock()).isBlocked(getBlockState())) {
                return;
            }

            if (contents.size() >= 10) {
                for (int i = contents.size() - 2; i > 0; i--) {
                    if (contents.get(i).isEmpty() && !contents.get(i - 1).isEmpty()) {
                        contents.remove(i);
                        contents.addFirst(PipeFluids.EMPTY);
                        setChanged();
                    }
                }

                if (!contents.isEmpty()) {
                    PipeFluids fluids = contents.removeLast();
                    if (!fluids.isEmpty()) {
                        PipeFluids pushedBack = fluids.isEmpty() ? PipeFluids.EMPTY : direction.getDirection().map(d -> PipeInsertable.tryInsert(world, pos.relative(d), d, fluids)).orElse(STATUS_VOIDED).ifRight(unit -> {
                            fluids.fluids().getFluids().forEach(fluid -> {
                                Vector3f outVec = direction.getDirection().map(Direction::step).orElseGet(Vector3f::new);
                                world.sendParticles(
                                        fluid.fluid().getPhysical().isOf(Fluids.WATER) ? ParticleTypes.DRIPPING_WATER
                                            : fluid.fluid().getPhysical().isOf(Fluids.LAVA) ? ParticleTypes.DRIPPING_LAVA
                                            : new FluidParticleEffect(PSParticles.DRIPPING_FLUID, fluid),
                                        pos.getX() + 0.5 + outVec.x * 0.5,
                                        pos.getY() + 0.5 + outVec.y * 0.5 - 0.2,
                                        pos.getZ() + 0.5 + outVec.z * 0.5, 1, 0, 0, 0, 0);
                            });
                        }).left().orElse(PipeFluids.EMPTY);
                        if (!pushedBack.isEmpty()) {
                            contents.addLast(pushedBack);
                            if (!pushedBack.equals(fluids)) {
                                setChanged();
                            }
                        } else {
                            world.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.1F, 0.001F);
                            setChanged();
                        }
                    } else {
                        setChanged();
                    }
                }
            } else {
                contents.addFirst(PipeFluids.EMPTY);
                setChanged();
            }

            scheduleNextTick(world);
        }

        private void scheduleNextTick(ServerLevel world) {
            for (var i : contents) {
                if (!i.isEmpty()) {
                    world.scheduleTick(worldPosition, getBlockState().getBlock(), 3);
                    return;
                }
            }
        }

        public void pullContentsForward(ServerLevel world, BlockPos pos) {
            if (((GlassTubeBlock)getBlockState().getBlock()).isBlocked(getBlockState())) {
                return;
            }

            if (world.getDirectSignalTo(pos) == 15) {
                getBlockState().getValue(IN).getDirection().flatMap(d -> PipeInsertable.tryExtract(world, pos.relative(d), d)).ifPresent(fluid -> {
                    contents.addFirst(fluid);
                    setChanged();
                });
            }

            scheduleNextTick(world);
        }

        @Override
        public boolean acceptsConnectionFrom(LevelAccessor world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
            return neighborState.getBlock() instanceof GlassTubeBlock && (state.getValue(input ? OUT : IN).direction == direction);
        }

        @Override
        public Either<PipeFluids, Unit> tryInsert(ServerLevel world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
            if (state.getValue(IN).direction == direction.getOpposite()) {
                return receiveContents(world, pos, state, fluids);
            }

            return PipeInsertable.reject(fluids);
        }

        @Override
        protected void saveAdditional(CompoundTag nbt, Provider lookup) {
            super.saveAdditional(nbt, lookup);
            PipeFluids.LIST_CODEC.encodeStart(NbtOps.INSTANCE, contents).result()
                .ifPresent(el -> nbt.put("contents", el));
        }

        @Override
        protected void loadAdditional(CompoundTag nbt, Provider lookup) {
            super.loadAdditional(nbt, lookup);
            contents.clear();
            PipeFluids.LIST_CODEC.decode(NbtOps.INSTANCE, nbt.get("contents")).result().map(Pair::getFirst).ifPresent(contents::addAll);
        }
    }
}
