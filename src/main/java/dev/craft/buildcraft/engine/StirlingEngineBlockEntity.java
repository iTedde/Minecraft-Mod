package dev.craft.buildcraft.engine;

import dev.craft.buildcraft.menu.EngineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stirling engine, 1:1 with BuildCraft {@code EngineStone}: burns furnace fuel only while powered, +1 MJ per burn
 * tick, at most 100 MJ per stroke. Explodes when it keeps burning with a full buffer.
 */
public class StirlingEngineBlockEntity extends EngineBlockEntity implements MenuProvider {
    private static final float[] PISTON_SPEEDS = {0.02F, 0.04F, 0.08F, 0.16F};

    private int burnTime;
    private int totalBurnTime;

    public StirlingEngineBlockEntity(BlockPos pos, BlockState state) {
        super(EngineType.STIRLING, pos, state, 10000, 100, 1);
    }

    @Override
    public int explosionRange() {
        return 4;
    }

    @Override
    public int maxEnergyReceived() {
        return 200;
    }

    @Override
    protected float[] pistonSpeeds() {
        return PISTON_SPEEDS;
    }

    @Override
    public boolean isBurning() {
        return burnTime > 0;
    }

    @Override
    protected void burn() {
        currentOutput = 0.0F;
        if (burnTime > 0) {
            burnTime--;
            currentOutput = 1.0F;
            addEnergy(1.0F);
            if (isRemoved()) {
                return;
            }
        }
        if (burnTime == 0 && isRedstonePowered()) {
            ItemStack stack = inventory.getStackInSlot(0);
            burnTime = totalBurnTime = stack.isEmpty() ? 0 : stack.getBurnTime(RecipeType.SMELTING);
            if (burnTime > 0) {
                inventory.setStackInSlot(0, consumeItem(stack));
            }
        }
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getTotalBurnTime() {
        return totalBurnTime;
    }

    /** Heat shown in the ledger: the stored energy. */
    @Override
    public int getHeat() {
        return Math.round(energy);
    }

    @Override
    public void onBlockBreak() {
        dropSlot(0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("burnTime", burnTime);
        tag.putInt("totalBurnTime", totalBurnTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        burnTime = tag.getInt("burnTime");
        totalBurnTime = tag.getInt("totalBurnTime");
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new EngineMenu(id, playerInventory, this);
    }
}
