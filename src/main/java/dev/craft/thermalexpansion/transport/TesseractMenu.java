package dev.craft.thermalexpansion.transport;

import javax.annotation.Nullable;

import dev.craft.thermalexpansion.TeMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * {@code ContainerTeleport}: no slots. Buttons 0–2 set the redstone mode; frequency, mode, access and the saved names
 * travel as {@link TesseractPayloads}.
 */
public class TesseractMenu extends AbstractContainerMenu {
    @Nullable
    private final TesseractBlockEntity tesseract;
    private final BlockPos pos;

    public TesseractMenu(int id, Inventory inventory, TesseractBlockEntity tesseract) {
        this(id, tesseract, tesseract.getBlockPos());
    }

    public TesseractMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, (TesseractBlockEntity) null, pos);
    }

    private TesseractMenu(int id, @Nullable TesseractBlockEntity tesseract, BlockPos pos) {
        super(TeMenus.TESSERACT.get(), id);
        this.tesseract = tesseract;
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    /** Server side: the tesseract behind the menu. */
    @Nullable
    public TesseractBlockEntity getTesseract() {
        return tesseract;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (tesseract == null || id < 0 || id > 2) {
            return false;
        }
        tesseract.setRedstone(id == 0, id == 2);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return tesseract == null || tesseract.stillValid(player);
    }
}
