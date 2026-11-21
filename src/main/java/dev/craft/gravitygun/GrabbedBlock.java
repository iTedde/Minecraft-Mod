package dev.craft.gravitygun;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * GraviGun {@code EntityBlock} (spec 50 §3): a block picked up by the gravity gun. The block (and its block entity data)
 * is taken out of the world; while nobody holds it, it falls ({@code grabBlockMode=1}) and sets itself down again on
 * landing, or drops as an item (with its inventory) where it cannot be placed.
 */
public class GrabbedBlock extends Entity {
    private static final EntityDataAccessor<BlockState> BLOCK =
            SynchedEntityData.defineId(GrabbedBlock.class, EntityDataSerializers.BLOCK_STATE);

    /** Negative while it is being pulled in, so it does not fall at once. */
    public int age;
    private CompoundTag blockEntityData;

    public GrabbedBlock(EntityType<? extends GrabbedBlock> type, Level level) {
        super(type, level);
        blocksBuilding = true;
    }

    /** Takes the block at {@code pos} out of the world. */
    public static GrabbedBlock lift(Level level, BlockPos pos) {
        GrabbedBlock grabbed = new GrabbedBlock(GravityGun.GRABBED_BLOCK.get(), level);
        BlockState state = level.getBlockState(pos);
        grabbed.setBlockState(state);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            grabbed.blockEntityData = blockEntity.saveWithoutMetadata(level.registryAccess());
            level.removeBlockEntity(pos);
        }
        grabbed.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return grabbed;
    }

    public BlockState getBlockState() {
        return entityData.get(BLOCK);
    }

    public void setBlockState(BlockState state) {
        entityData.set(BLOCK, state);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BLOCK, Blocks.STONE.defaultBlockState());
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void tick() {
        if (getBlockState().isAir() || getY() < level().getMinBuildHeight() - 500) {
            discard();
            return;
        }
        age++;
        xo = getX();
        yo = getY();
        zo = getZ();
        move(MoverType.SELF, getDeltaMovement());
        if (!level().isClientSide() && !GravityGunHandler.isGrabbed(this) && age > 0) {
            setDeltaMovement(getDeltaMovement().add(0.0, -0.04, 0.0));
            if (onGround()) {
                setDeltaMovement(getDeltaMovement().multiply(0.7, 1.0, 0.7));
                land();
                return;
            }
        }
        setDeltaMovement(getDeltaMovement().scale(0.98));
    }

    /** Sets the block down where it landed, or drops it (and its inventory). */
    private void land() {
        BlockPos pos = blockPosition();
        BlockState here = level().getBlockState(pos);
        if (here.is(Blocks.MOVING_PISTON)) {
            return;
        }
        discard();
        BlockState state = getBlockState();
        if (!here.is(state.getBlock()) && here.canBeReplaced() && state.canSurvive(level(), pos)
                && level().setBlock(pos, state, Block.UPDATE_ALL)) {
            if (blockEntityData != null) {
                BlockEntity blockEntity = level().getBlockEntity(pos);
                if (blockEntity != null) {
                    blockEntity.loadWithComponents(blockEntityData, level().registryAccess());
                    blockEntity.setChanged();
                }
            }
            return;
        }
        Containers.dropItemStack(level(), getX(), getY(), getZ(), new ItemStack(state.getBlock()));
        if (blockEntityData != null && blockEntityData.contains("Items")) {
            NonNullList<ItemStack> items = NonNullList.withSize(256, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(blockEntityData, items, level().registryAccess());
            items.forEach(item -> Containers.dropItemStack(level(), getX(), getY(), getZ(), item));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setBlockState(NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), tag.getCompound("BlockState")));
        age = tag.getInt("Age");
        blockEntityData = tag.contains("BlockEntityData") ? tag.getCompound("BlockEntityData") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("BlockState", NbtUtils.writeBlockState(getBlockState()));
        tag.putInt("Age", age);
        if (blockEntityData != null) {
            tag.put("BlockEntityData", blockEntityData);
        }
    }

    public Vec3 centre() {
        return getBoundingBox().getCenter();
    }
}
