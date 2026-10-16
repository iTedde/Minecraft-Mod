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

    public SteamTurbineMenu(int id, Inventory inventory, SteamTurbineBlockEntity turbine) {
        this(id, inventory, turbine, turbine.getBlockPos(), new SyncedInts(turbine::getSteam, turbine::getBuffer,
                turbine::getProduction, () -> turbine.isFormed() ? 1 : 0));
    }

    public SteamTurbineMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(4));
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
}
