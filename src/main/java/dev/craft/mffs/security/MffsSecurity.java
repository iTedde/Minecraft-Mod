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

    /** Access check with the original's chat message on failure. */
    public static boolean checkAccess(MffsMachineBlockEntity machine, Player player, SecurityRight right) {
        if (isAccessGranted(machine, player, right)) {
            return true;
        }
        player.sendSystemMessage(Component.translatable("message.craft.mffs.access_denied"));
        return false;
    }
}
