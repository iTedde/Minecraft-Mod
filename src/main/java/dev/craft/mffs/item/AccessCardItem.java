package dev.craft.mffs.item;

import dev.craft.mffs.MffsDataComponents;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.block.SecurityStationBlockEntity;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * {@code ItemAccessCard} (spec 48 §7.2): an ID card without owner that grants its rights at one security station
 * while its validity lasts. In an inventory the validity drops by one every minute (1 200 ticks) and the station
 * name is refreshed.
 */
public class AccessCardItem extends PersonalIdCardItem {
    public static final int MINUTE = 1200;

    public AccessCardItem(Properties properties) {
        super(properties);
    }

    public static AccessLicense getLicense(ItemStack stack) {
        AccessLicense license = stack.get(MffsDataComponents.ACCESS_LICENSE.get());
        return license != null ? license : new AccessLicense(0, 0, "not set", 0);
    }

    public static void setLicense(ItemStack stack, AccessLicense license) {
        stack.set(MffsDataComponents.ACCESS_LICENSE.get(), license);
    }

    public static int getValidity(ItemStack stack) {
        return getLicense(stack).validity();
    }

    public static void setValidity(ItemStack stack, int minutes) {
        setLicense(stack, getLicense(stack).withValidity(minutes));
    }

    /** {@code setforArea} + {@code setlinkID}: a fresh license of {@code station} for {@code minutes}. */
    public static void link(ItemStack stack, SecurityStationBlockEntity station, int minutes) {
        long now = station.getLevel() != null ? station.getLevel().getGameTime() : 0;
        setLicense(stack, new AccessLicense(minutes, station.getDeviceId(), station.getDeviceName(), now));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide()) {
            return;
        }
        AccessLicense license = getLicense(stack);
        long now = level.getGameTime();
        if (license.lastStep() == 0 || license.lastStep() > now) {
            setLicense(stack, license.withLastStep(now));
            return;
        }
        if (now - license.lastStep() <= MINUTE) {
            return;
        }
        license = license.withLastStep(now);
        if (license.validity() > 0) {
            license = license.withValidity(license.validity() - 1);
            if (license.stationId() != 0
                    && MffsDevices.get(level, MffsDevices.Kind.SECURITY_STATION, license.stationId())
                    instanceof SecurityStationBlockEntity station
                    && !station.getDeviceName().equals(license.areaName())) {
                license = license.withAreaName(station.getDeviceName());
            }
        }
        setLicense(stack, license);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        AccessLicense license = getLicense(stack);
        tooltip.add(Component.translatable("tooltip.craft.mffs.security_area", license.areaName()));
        tooltip.add(Component.translatable("tooltip.craft.mffs.validity", license.validity()));
        appendRights(stack, tooltip);
    }
}
