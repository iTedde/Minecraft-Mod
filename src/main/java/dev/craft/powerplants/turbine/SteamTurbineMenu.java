package dev.craft.powerplants.turbine;

import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.SyncedInts;
import dev.craft.powerplants.PpMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

/** Steam turbine GUI (Railcraft steam engine layout): steam tank, output in EU/t, buffer. */
public class SteamTurbineMenu extends MachineMenu<SteamTurbineBlockEntity> {
    private static final int STEAM = 0;
    private static final int BUFFER = 1;
    private static final int PRODUCTION = 2;
    private static final int FORMED = 3;
    private static final int TANK_CAPACITY = 4;
    private static final int BUFFER_CAPACITY = 5;

    public SteamTurbineMenu(int id, Inventory inventory, SteamTurbineBlockEntity turbine) {
        this(id, inventory, turbine, turbine.getBlockPos(), new SyncedInts(turbine::getSteam, turbine::getBuffer,
                turbine::getProduction, () -> turbine.isFormed() ? 1 : 0, () -> turbine.getTier().tankCapacity(),
                () -> turbine.getTier().bufferCapacity()));
    }

    public SteamTurbineMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(6));
    }

    private SteamTurbineMenu(int id, Inventory inventory, @Nullable SteamTurbineBlockEntity turbine, BlockPos pos,
                             SyncedInts data) {
        super(PpMenus.STEAM_TURBINE.get(), id, turbine, pos, data);
        addPlayerInventory(inventory);
    }

    public int getSteam() {
        return data.getInt(STEAM);
    }

    public int getBuffer() {
        return data.getInt(BUFFER);
    }

    public int getProduction() {
        return data.getInt(PRODUCTION);
    }

    public boolean isFormed() {
        return data.getInt(FORMED) != 0;
    }

    /** Steam tank capacity of this turbine size (at least 1 until the first sync). */
    public int getTankCapacity() {
        return Math.max(data.getInt(TANK_CAPACITY), 1);
    }

    public int getBufferCapacity() {
        return Math.max(data.getInt(BUFFER_CAPACITY), 1);
    }
}
