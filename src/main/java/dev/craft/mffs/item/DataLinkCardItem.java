package dev.craft.mffs.item;

import dev.craft.mffs.MffsDataComponents;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * {@code ItemCardDataLink} (spec 48 §7.4): device kind, name, ID and place of an MFFS machine, read by the control
 * system. Made by the multitool ‹ID writer›.
 */
public class DataLinkCardItem extends CardItem {
    public DataLinkCardItem(Properties properties) {
        super(properties);
    }

    public static MffsDevices.Kind getKind(ItemStack stack) {
        String kind = stack.get(MffsDataComponents.DEVICE_KIND.get());
        return kind != null ? MffsDevices.Kind.byName(kind) : null;
    }

    /** {@code setInformation}: link to {@code machine} with its kind. */
    public static void link(ItemStack stack, MffsMachineBlockEntity machine) {
        CardItem.link(stack, machine, machine.getDeviceId(), machine.getDeviceName());
        stack.set(MffsDataComponents.DEVICE_KIND.get(), machine.getDeviceKind().name());
    }

    /** {@code getTileEntityMachines(displayName, DeviceID)}: the linked machine over kind and ID, or null. */
    public static MffsMachineBlockEntity getTarget(ItemStack stack, Level level) {
        CardLink link = getLink(stack);
        MffsDevices.Kind kind = getKind(stack);
        if (link == null || kind == null || link.id() == 0) {
            return null;
        }
        return MffsDevices.get(level, kind, link.id());
    }

    /** Name refresh every 600 ticks while in an inventory. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || level.getGameTime() % 600 != 0) {
            return;
        }
        CardLink link = getLink(stack);
        MffsMachineBlockEntity device = getTarget(stack, level);
        if (link != null && device != null && !device.getDeviceName().equals(link.name())) {
            setLink(stack, link.withName(device.getDeviceName()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        MffsDevices.Kind kind = getKind(stack);
        CardLink link = getLink(stack);
        tooltip.add(Component.translatable("tooltip.craft.mffs.device_type",
                kind != null ? Component.translatable(kind.translationKey()) : Component.literal("-")));
        tooltip.add(Component.translatable("tooltip.craft.mffs.device_name", link != null ? link.name() : "not set"));
        if (link != null) {
            tooltip.add(Component.translatable("tooltip.craft.mffs.world",
                    link.target().dimension().location().toString()));
            BlockPos pos = link.target().pos();
            tooltip.add(Component.translatable("tooltip.craft.mffs.coords", pos.getX(), pos.getY(), pos.getZ()));
            tooltip.add(link.valid()
                    ? Component.translatable("tooltip.craft.mffs.valid").withStyle(ChatFormatting.DARK_GREEN)
                    : Component.translatable("tooltip.craft.mffs.invalid").withStyle(ChatFormatting.DARK_RED));
        }
    }
}
