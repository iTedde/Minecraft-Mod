package dev.craft.secretrooms;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Camouflaged chest ({@code BlockCamoChest}, spec 50 §4): a full camouflaged block with a single chest inventory (27).
 * A cat sitting on it blocks it; breaking it drops the contents.
 */
public class CamoChestBlock extends CamoBlock {
    public static final MapCodec<CamoChestBlock> CODEC = simpleCodec(CamoChestBlock::new);

    public CamoChestBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Entity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof Entity chest
                && level.getEntitiesOfClass(Cat.class, new AABB(pos.above())).stream()
                .noneMatch(Cat::isInSittingPose)) {
            player.openMenu(chest);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof Entity chest) {
            Containers.dropContents(level, pos, (Container) chest);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    /** {@code TileEntityCamoChest}: camouflage plus 27 slots. */
    public static class Entity extends CamoBlockEntity implements Container, MenuProvider {
        private final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

        public Entity(BlockPos pos, BlockState state) {
            super(SecretRooms.CAMO_CHEST_BLOCK_ENTITY.get(), pos, state);
        }

        @Override
        public int getContainerSize() {
            return items.size();
        }

        @Override
        public boolean isEmpty() {
            return items.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            return items.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
            setChanged();
            return stack;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ContainerHelper.takeItem(items, slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            items.set(slot, stack);
            setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return Container.stillValidBlockEntity(this, player);
        }

        @Override
        public void clearContent() {
            items.clear();
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("block.craft.camo_chest");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return ChestMenu.threeRows(id, inventory, this);
        }

        @Override
        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.saveAdditional(tag, registries);
            ContainerHelper.saveAllItems(tag, items, registries);
        }

        @Override
        protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.loadAdditional(tag, registries);
            items.clear();
            ContainerHelper.loadAllItems(tag, items, registries);
        }

        /** The client only needs the camouflage. */
        @Override
        public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
            CompoundTag tag = new CompoundTag();
            tag.put("Camo", net.minecraft.nbt.NbtUtils.writeBlockState(getCamo()));
            tag.putBoolean("Chosen", isChosen());
            return tag;
        }
    }
}
