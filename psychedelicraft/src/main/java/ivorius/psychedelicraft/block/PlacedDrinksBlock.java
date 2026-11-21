package ivorius.psychedelicraft.block;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.SyncedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PlacedDrinksBlock extends BaseEntityBlock {
    public static final MapCodec<PlacedDrinksBlock> CODEC = simpleCodec(PlacedDrinksBlock::new);
    private static final Optional<InteractionResultHolder<ItemStack>> FAILURE = Optional.of(InteractionResultHolder.fail(ItemStack.EMPTY));
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);

    protected PlacedDrinksBlock(Properties settings) {
        super(settings.noCollission());
    }

    @Override
    protected MapCodec<? extends PlacedDrinksBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        return Psychedelicraft.getCrossHairTarget()
                .filter(hit -> hit.getType() == HitResult.Type.BLOCK)
                .map(hit -> (BlockHitResult)hit)
                .filter(hit -> hit.getBlockPos().equals(pos))
                .flatMap(Data::getHitPos)
                .flatMap(hitPos -> world.getBlockEntity(pos, PSBlockEntities.PLACED_DRINK).flatMap(be -> be.getDrink(hitPos)))
                .orElseGet(() -> super.getCloneItemStack(world, pos, state));
    }

    public static boolean canPlace(ItemStack stack) {
        return stack.is(PSTags.Items.PLACEABLE_RECEPTICALS);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldStack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, PSBlockEntities.PLACED_DRINK).flatMap(be -> {

            if (heldStack.isEmpty()) {
                return Data.getHitPos(hit).map(be::removeDrink).map(extracted -> {
                    ItemStack stack = extracted.getObject();
                    if (!stack.isEmpty()) {
                        player.addItem(stack);
                    }
                    return extracted;
                });
            }

            if (!canPlace(heldStack)) {
                return FAILURE;
            }

            return Data.getHitPos(hit).map(position -> {
                return be.placeDrink(position, player.isCreative() ? heldStack.copyWithCount(1) : heldStack, player.getYHeadRot());
            });
        }).map(InteractionResultHolder::getResult).orElse(InteractionResult.FAIL).consumesAction() ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof Data be) {
            builder = builder.withDynamicDrop(BlockWithFluid.CONTENTS_DYNAMIC_DROP_ID, lootConsumer -> {
                be.forEachDrink((y, entry) -> {
                    lootConsumer.accept(entry.stack());
                    return 0;
                });
                be.entries.clear();
            });
        }
        return super.getDrops(state, builder);
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && (entity.getY() > pos.getY() || entity.getY() >= pos.getY() && !entity.isShiftKeyDown()) && entity instanceof LivingEntity && Math.max(
                Math.abs(entity.getX() - entity.xOld),
                Math.abs(entity.getZ() - entity.zOld)
            ) >= 0.003F) {
            Block.dropResources(state, world, pos, world.getBlockEntity(pos), entity, ItemStack.EMPTY);
            world.removeBlock(pos, false);
            world.playSound(null, pos, SoundEvents.CANDLE_PLACE, SoundSource.BLOCKS);
        }
    }

    @Override
    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state) {

    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Data(pos, state);
    }

    public static InteractionResult tryPlace(UseOnContext context) {
        if (!canPlace(context.getItemInHand())) {
            return InteractionResult.PASS;
        }

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        boolean replaceable = state.canBeReplaced(new BlockPlaceContext(context));

        if (!replaceable && context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        if (state.is(PSBlocks.PLACED_DRINK)) {
            return state.useItemOn(context.getItemInHand(), context.getLevel(), context.getPlayer(), context.getHand(), new BlockHitResult(
                    context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), true
            )).consumesAction() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        BlockPos blockPos = replaceable ? context.getClickedPos() : context.getClickedPos().relative(context.getClickedFace());
        if (!replaceable && !context.getLevel().isEmptyBlock(blockPos)) {
            return InteractionResult.PASS;
        }
        BlockPos hitPos = Data.getHitPos(blockPos, context.getClickLocation());
        context.getLevel().setBlockAndUpdate(blockPos, PSBlocks.PLACED_DRINK.defaultBlockState());
        return context.getLevel().getBlockEntity(blockPos, PSBlockEntities.PLACED_DRINK).map(be -> {
            return be.placeDrink(hitPos, context.getItemInHand().split(1), context.getRotation()).getResult().consumesAction() ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
        }).orElse(ItemInteractionResult.FAIL).consumesAction() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public static class Data extends SyncedBlockEntity {
        static final int MAX_COORD = 16;
        static final int MAX_INDEX = MAX_COORD * MAX_COORD;
        static final int MAX_STACK_HEIGHT = 5;

        private final IntObjectMap<Stack<Entry>> entries = new IntObjectHashMap<>(MAX_INDEX);

        public Data(BlockPos pos, BlockState state) {
            super(PSBlockEntities.PLACED_DRINK, pos, state);
        }

        public void forEachDrink(DrinkConsumer consumer) {
            entries.values().forEach(list -> {
                final float[] y = new float[1];
                list.forEach(drink -> {
                    y[0] += consumer.accept(y[0], drink);
                });
            });
        }

        public InteractionResultHolder<ItemStack> removeDrink(BlockPos center) {
            return StreamSupport.stream(BlockPos.spiralAround(center, 2, Direction.EAST, Direction.NORTH).spliterator(), false).map(pos -> {
                int index = getIndex(pos);
                Stack<Entry> list = entries.get(index);
                if (list != null) {
                    Entry entry = list.pop();
                    if (entry != null) {
                        if (list.isEmpty()) {
                            entries.remove(index);
                            if (entries.isEmpty()) {
                                getLevel().removeBlock(getBlockPos(), false);
                            }
                        }
                        setChanged();
                        return InteractionResultHolder.success(entry.stack());
                    }
                }

                return InteractionResultHolder.fail(ItemStack.EMPTY);
            }).filter(i -> i.getResult().consumesAction()).findFirst().orElseGet(() -> InteractionResultHolder.fail(ItemStack.EMPTY));
        }

        public boolean hasDrink(BlockPos center) {
            return StreamSupport.stream(BlockPos.spiralAround(center, 2, Direction.EAST, Direction.NORTH).spliterator(), false)
                    .map(pos -> entries.get(getIndex(pos)))
                    .anyMatch(list -> list != null && !list.empty());
        }

        public Optional<ItemStack> getDrink(BlockPos center) {
            return StreamSupport.stream(BlockPos.spiralAround(center, 2, Direction.EAST, Direction.NORTH).spliterator(), false)
                    .map(pos -> entries.get(getIndex(pos)))
                    .filter(list -> list != null && !list.empty())
                    .map(Stack::peek)
                    .map(Entry::stack)
                    .findFirst();
        }

        public InteractionResultHolder<ItemStack> placeDrink(BlockPos position, ItemStack stack, float yaw) {
            int index = getIndex(position);
            Stack<Entry> list = entries.get(index);
            if (list == null) {
                list = new Stack<>();
                entries.put(index, list);
            }
            if (list.size() < MAX_STACK_HEIGHT) {
                list.add(new Entry(position.getX() / 16F - 0.5F, position.getZ() / 16F - 0.5F, (-yaw) % 360, stack.split(1)));
                getLevel().playSound(null, getBlockPos(), SoundEvents.CANDLE_PLACE, SoundSource.BLOCKS);
                setChanged();
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.fail(stack);
        }

        @Override
        public void loadAdditional(CompoundTag nbt, Provider lookup) {
            readEntriesFromNbt(nbt.getCompound("entries"));
        }

        @Override
        protected void saveAdditional(CompoundTag nbt, Provider lookup) {
            nbt.put("entries", writeEntriesToNbt(new CompoundTag()));
        }

        private void readEntriesFromNbt(CompoundTag nbt) {
            entries.clear();
            nbt.getAllKeys().forEach(key -> {
                int index = Integer.parseInt(key);
                ListTag list = nbt.getList(key, Tag.TAG_COMPOUND);
                if (!list.isEmpty()) {
                    entries.put(index, Entry.STACK_CODEC.decode(NbtOps.INSTANCE, list).getOrThrow().getFirst());
                }
            });
        }

        private CompoundTag writeEntriesToNbt(CompoundTag nbt) {
            entries.entries().forEach(entry -> {
                if (!entry.value().isEmpty()) {
                    Entry.STACK_CODEC.encodeStart(NbtOps.INSTANCE, entry.value()).result().ifPresent(stack -> {
                        nbt.put(entry.key() + "", stack);
                    });
                }
            });
            return nbt;
        }

        public static Optional<BlockPos> getHitPos(BlockHitResult hit) {
            Direction direction = hit.getDirection();

            if (direction.getAxis() != Direction.Axis.Y) {
                return Optional.empty();
            }

            return Optional.of(getHitPos(hit.getBlockPos().relative(direction), hit.getLocation()));
        }

        public static BlockPos getHitPos(BlockPos pos, Vec3 relativePos) {
            return BlockPos.containing(
                    (relativePos.x() - pos.getX()) * MAX_COORD,
                    0,
                    (relativePos.z() - pos.getZ()) * MAX_COORD
            );
        }

        private static int getIndex(BlockPos position) {
            return ((Math.max(0, position.getX()) * MAX_COORD) + Math.max(0, position.getZ())) % MAX_INDEX;
        }

        public record Entry (float x, float z, float rotation, ItemStack stack) {
            static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("x").forGetter(Entry::x),
                    Codec.FLOAT.fieldOf("z").forGetter(Entry::z),
                    Codec.FLOAT.fieldOf("rotation").forGetter(Entry::rotation),
                    ItemStack.CODEC.fieldOf("stack").forGetter(Entry::stack)
            ).apply(instance, Entry::new));
            static final Codec<Stack<Entry>> STACK_CODEC = CODEC.listOf().xmap(
                    list -> list.stream().collect(Collectors.toCollection(Stack::new)),
                    stack -> List.copyOf(stack)
            );
        }

        public interface DrinkConsumer {
            float accept(float y, Entry drink);
        }
    }
}

