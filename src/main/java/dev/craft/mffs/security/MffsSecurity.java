package dev.craft.mffs.security;

import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.ControlSystemBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.block.ProjectorBlockEntity;
import dev.craft.mffs.block.SecurityStationBlockEntity;
import dev.craft.mffs.block.SecurityStorageBlockEntity;
import dev.craft.mffs.item.multitool.DebuggerMultitoolItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/** {@code SecurityHelper.isAccessGranted} (spec 48 §7.1). */
public final class MffsSecurity {
    private MffsSecurity() {
    }

    /**
     * Security storage and control system without a station: only the server allows (the original's {@code
     * !world.isRemote}). Security station: its own rights. Projector: access type 2 → the capacitor's station, 3 → its
     * own, 0/1 → always. Every other machine: its linked station, none → allowed.
     */
    public static boolean isAccessGranted(MffsMachineBlockEntity machine, Player player, SecurityRight right) {
        if (machine instanceof SecurityStationBlockEntity station) {
            return station.isAccessGranted(player, right);
        }
        SecurityStationBlockEntity station = stationFor(machine);
        if (station != null) {
            return station.isAccessGranted(player, right);
        }
        if (machine instanceof SecurityStorageBlockEntity || machine instanceof ControlSystemBlockEntity) {
            return !player.level().isClientSide();
        }
        return true;
    }

    /** The station that decides for {@code machine} (projector: by access type). */
    private static SecurityStationBlockEntity stationFor(MffsMachineBlockEntity machine) {
        if (machine instanceof ProjectorBlockEntity projector) {
            return switch (projector.getAccessType()) {
                case 2 -> MffsDevices.get(projector.getLevel(), MffsDevices.Kind.CAPACITOR,
                        projector.getPowerSourceId()) instanceof CapacitorBlockEntity capacitor
                        ? capacitor.getLinkedSecurityStation() : null;
                case 3 -> projector.getLinkedSecurityStation();
                default -> null;
            };
        }
        return machine.getLinkedSecurityStation();
    }

    /**
     * {@code killswitch} of the defence upgrade (spec 48 §9.3): true only when the machine has a linked security
     * station that refuses the right.
     */
    public static boolean isDeniedByStation(MffsMachineBlockEntity machine, Player player, SecurityRight right) {
        SecurityStationBlockEntity station = machine.getLinkedSecurityStation();
        return station != null && !station.isAccessGranted(player, right);
    }

    /**
     * Free pass of access type 0 (field teleporter): a name on the admin list (empty in Craft, spec 48 §11.3) or a
     * debugger in the inventory.
     */
    public static boolean isAdmin(Player player) {
        return hasDebugger(player);
    }

    public static boolean hasDebugger(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).getItem() instanceof DebuggerMultitoolItem) {
                return true;
            }
        }
        return false;
    }

    /** Access check with the original's chat message on failure. */
    public static boolean checkAccess(MffsMachineBlockEntity machine, Player player, SecurityRight right) {
        if (isAccessGranted(machine, player, right)) {
            return true;
        }
        player.sendSystemMessage(Component.translatable("message.craft.mffs.access_denied"));
        return false;
    }
}
