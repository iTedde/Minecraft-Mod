package dev.craft.mffs.item;

import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.MffsItems;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.ControlSystemBlockEntity;
import dev.craft.mffs.block.DefenseStationBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.block.ProjectorBlockEntity;
import dev.craft.mffs.block.SecurityStationBlockEntity;
import dev.craft.mffs.block.SecurityStorageBlockEntity;
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * {@code ItemCardSecurityLink} (spec 48 §6.2): links a machine to a security station. Right-click installs it in the
 * control system (0), capacitor (4), defence station (1), security storage (0) or projector (12).
 */
public class SecurityLinkCardItem extends CardItem {
    public static final String CARD_NAME = "<Security Station Link>";

    public SecurityLinkCardItem(Properties properties) {
        super(properties);
    }

    /** Name refresh every 600 ticks while in an inventory. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || level.getGameTime() % 600 != 0) {
            return;
        }
        CardLink link = getLink(stack);
        if (link != null && link.id() != 0 && sameDimension(link, level)
                && MffsDevices.get(level, MffsDevices.Kind.SECURITY_STATION, link.id())
                instanceof SecurityStationBlockEntity station
                && !station.getDeviceName().equals(link.name())) {
            setLink(stack, link.withName(station.getDeviceName()));
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null
                || !(level.getBlockEntity(context.getClickedPos()) instanceof MffsMachineBlockEntity machine)) {
            return InteractionResult.PASS;
        }
        int slot;
        if (machine instanceof ControlSystemBlockEntity) {
            slot = ControlSystemBlockEntity.SLOT_SECURITY_LINK;
        } else if (machine instanceof CapacitorBlockEntity) {
            slot = CapacitorBlockEntity.SLOT_SECURITY_LINK;
        } else if (machine instanceof DefenseStationBlockEntity) {
            slot = DefenseStationBlockEntity.SLOT_SECURITY_LINK;
        } else if (machine instanceof SecurityStorageBlockEntity) {
            slot = SecurityStorageBlockEntity.SLOT_SECURITY_LINK;
        } else if (machine instanceof ProjectorBlockEntity) {
            slot = ProjectorBlockEntity.SLOT_SECURITY_LINK;
        } else {
            return InteractionResult.PASS;
        }
        if (!MffsSecurity.checkAccess(machine, player, SecurityRight.EB)) {
            return InteractionResult.PASS;
        }
        return installInSlot(stack, player, machine, slot, CARD_NAME) ? InteractionResult.SUCCESS
                : InteractionResult.FAIL;
    }

    /**
     * {@code getLinkedSecurityStation}: the station at the card's position with the card's ID (name refreshed); a
     * station moved elsewhere is found over its ID and the card follows. Otherwise, once the target chunk is loaded,
     * the card in the machine becomes a blank card.
     */
    public static SecurityStationBlockEntity getLinkedStation(MffsMachineBlockEntity machine, int slot) {
        Level level = machine.getLevel();
        ItemStack stack = machine.getStack(slot);
        if (level == null || !(stack.getItem() instanceof SecurityLinkCardItem)) {
            return null;
        }
        CardLink link = getLink(stack);
        if (link == null || !sameDimension(link, level)) {
            return null;
        }
        BlockPos pos = link.target().pos();
        boolean loaded = level.isLoaded(pos);
        if (loaded && level.getBlockEntity(pos) instanceof SecurityStationBlockEntity station) {
            if (station.getDeviceId() == link.id() && link.id() != 0) {
                if (!station.getDeviceName().equals(link.name())) {
                    setLink(stack, link.withName(station.getDeviceName()));
                }
                return station;
            }
        } else if (link.id() != 0 && MffsDevices.get(level, MffsDevices.Kind.SECURITY_STATION, link.id())
                instanceof SecurityStationBlockEntity station) {
            setLink(stack, link.withTarget(GlobalPos.of(level.dimension(), station.getBlockPos())));
            return station;
        }
        if (loaded) {
            machine.setStack(slot, new ItemStack(MffsItems.CARD_BLANK.get()));
        }
        return null;
    }
}
