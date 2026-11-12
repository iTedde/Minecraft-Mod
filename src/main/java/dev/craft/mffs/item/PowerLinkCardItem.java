package dev.craft.mffs.item;

import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.ConverterBlockEntity;
import dev.craft.mffs.block.ExtractorBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.energy.ForceEnergyStorage;
import dev.craft.mffs.energy.PowerLinkItem;
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
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * {@code ItemCardPowerLink} (spec 48 §6.2): links a machine to a capacitor. Valid while the capacitor (same ID) exists
 * in the same dimension and the machine is within its transmit range; the coordinates follow a moved capacitor over
 * its ID; a loaded target chunk without the capacitor makes the card invalid.
 */
public class PowerLinkCardItem extends CardItem implements PowerLinkItem {
    public static final String CARD_NAME = "<Power-Link>";

    public PowerLinkCardItem(Properties properties) {
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
                && MffsDevices.get(level, MffsDevices.Kind.CAPACITOR, link.id()) instanceof CapacitorBlockEntity cap
                && !cap.getDeviceName().equals(link.name())) {
            setLink(stack, link.withName(cap.getDeviceName()));
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) {
            return InteractionResult.PASS;
        }
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof MffsMachineBlockEntity machine)
                || !MffsSecurity.checkAccess(machine, player, SecurityRight.EB)) {
            return InteractionResult.PASS;
        }
        int slot;
        if (machine instanceof ConverterBlockEntity) {
            slot = ConverterBlockEntity.SLOT_POWER_LINK;
        } else if (machine instanceof ExtractorBlockEntity) {
            slot = ExtractorBlockEntity.SLOT_POWER_LINK;
        } else if (machine instanceof CapacitorBlockEntity) {
            slot = CapacitorBlockEntity.SLOT_LINK;
        } else {
            return InteractionResult.PASS;
        }
        return installInSlot(stack, player, machine, slot, CARD_NAME) ? InteractionResult.SUCCESS
                : InteractionResult.FAIL;
    }

    /** {@code getForceEnergyStorageBlock}. */
    public static ForceEnergyStorage getStorage(ItemStack stack, MffsMachineBlockEntity machine) {
        CardLink link = getLink(stack);
        Level level = machine.getLevel();
        if (link == null || !link.valid() || level == null || !sameDimension(link, level)) {
            return null;
        }
        BlockPos pos = link.target().pos();
        boolean loaded = level.isLoaded(pos);
        if (loaded && level.getBlockEntity(pos) instanceof CapacitorBlockEntity cap) {
            if (cap.getPowerStorageId() == link.id() && link.id() != 0) {
                if (!cap.getDeviceName().equals(link.name())) {
                    setLink(stack, link.withName(cap.getDeviceName()));
                }
                return inRange(cap, machine) ? cap : null;
            }
        } else if (link.id() != 0
                && MffsDevices.get(level, MffsDevices.Kind.CAPACITOR, link.id()) instanceof CapacitorBlockEntity cap) {
            setLink(stack, link.withTarget(GlobalPos.of(level.dimension(), cap.getBlockPos())));
            return inRange(cap, machine) ? cap : null;
        }
        if (loaded) {
            setLink(stack, link.invalid());
        }
        return null;
    }

    private static boolean inRange(CapacitorBlockEntity cap, MffsMachineBlockEntity machine) {
        return cap.getTransmitRange() >= Math.sqrt(cap.getBlockPos().distSqr(machine.getBlockPos()));
    }

    @Override
    public int getAvailablePower(ItemStack stack, MffsMachineBlockEntity machine) {
        ForceEnergyStorage storage = getStorage(stack, machine);
        return storage != null ? storage.getStorageAvailablePower() : 0;
    }

    @Override
    public int getMaximumPower(ItemStack stack, MffsMachineBlockEntity machine) {
        ForceEnergyStorage storage = getStorage(stack, machine);
        return storage != null ? storage.getStorageMaxPower() : 1;
    }

    @Override
    public int getPowerSourceId(ItemStack stack, MffsMachineBlockEntity machine) {
        ForceEnergyStorage storage = getStorage(stack, machine);
        return storage != null ? storage.getPowerStorageId() : 0;
    }

    @Override
    public int getPercentageCapacity(ItemStack stack, MffsMachineBlockEntity machine) {
        ForceEnergyStorage storage = getStorage(stack, machine);
        return storage != null ? storage.getPercentageStorageCapacity() : 0;
    }

    @Override
    public boolean consumePower(ItemStack stack, int amount, boolean simulate, MffsMachineBlockEntity machine) {
        ForceEnergyStorage storage = getStorage(stack, machine);
        return storage != null && storage.consumePowerFromStorage(amount, simulate);
    }

    @Override
    public boolean insertPower(ItemStack stack, int amount, boolean simulate, MffsMachineBlockEntity machine) {
        ForceEnergyStorage storage = getStorage(stack, machine);
        return storage != null && storage.insertPowerToStorage(amount, simulate);
    }

    @Override
    public int getFreeStorageAmount(ItemStack stack, MffsMachineBlockEntity machine) {
        ForceEnergyStorage storage = getStorage(stack, machine);
        return storage != null ? storage.getFreeStorageAmount() : 0;
    }

    @Override
    public boolean isPowerSourceItem() {
        return false;
    }
}
