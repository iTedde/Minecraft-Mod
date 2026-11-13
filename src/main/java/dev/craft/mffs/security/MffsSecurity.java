package dev.craft.mffs.security;

import dev.craft.mffs.block.MffsMachineBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * {@code SecurityHelper.isAccessGranted} (spec 48 §7.1). Until the security station exists (phase 48.4) no machine
 * can be linked to one, and a machine without a linked station allows everything.
 */
public final class MffsSecurity {
    private MffsSecurity() {
    }

    public static boolean isAccessGranted(MffsMachineBlockEntity machine, Player player, SecurityRight right) {
        return true;
    }

    /**
     * {@code killswitch} of the defence upgrade (spec 48 §9.3): true only when the machine has a linked security
     * station that refuses the right. Without stations (until phase 48.4) never.
     */
    public static boolean isDeniedByStation(MffsMachineBlockEntity machine, Player player, SecurityRight right) {
        return false;
    }

    /**
     * Free pass of access type 0 (field teleporter): a name on the admin list (empty in Craft, spec 48 §11.3) or a
     * debugger in the inventory – the debugger follows in phase 48.4.
     */
    public static boolean isAdmin(Player player) {
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
