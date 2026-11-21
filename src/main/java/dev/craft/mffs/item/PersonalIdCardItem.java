package dev.craft.mffs.item;

import dev.craft.mffs.MffsDataComponents;
import dev.craft.mffs.security.SecurityRight;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * {@code ItemCardPersonalID} (spec 48 §7.1): owner and rights; tooltip "Owner", rights with Shift. Machines drop it
 * as a blank card.
 */
public class PersonalIdCardItem extends Item {
    public PersonalIdCardItem(Properties properties) {
        super(properties);
    }

    public static IdCardData getData(ItemStack stack) {
        IdCardData data = stack.get(MffsDataComponents.ID_CARD.get());
        return data != null ? data : IdCardData.EMPTY;
    }

    public static void setData(ItemStack stack, IdCardData data) {
        stack.set(MffsDataComponents.ID_CARD.get(), data);
    }

    public static boolean hasRight(ItemStack stack, SecurityRight right) {
        return getData(stack).hasRight(right);
    }

    public static void setRight(ItemStack stack, SecurityRight right, boolean value) {
        setData(stack, getData(stack).withRight(right, value));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.craft.mffs.owner", getData(stack).owner()));
        appendRights(stack, tooltip);
    }

    protected static void appendRights(ItemStack stack, List<Component> tooltip) {
        if (!shiftDown()) {
            tooltip.add(Component.translatable("tooltip.craft.mffs.rights_hint"));
            return;
        }
        tooltip.add(Component.translatable("tooltip.craft.mffs.rights"));
        for (SecurityRight right : SecurityRight.values()) {
            if (hasRight(stack, right)) {
                tooltip.add(Component.literal("-").append(Component.translatable(right.translationKey())));
            }
        }
    }

    private static boolean shiftDown() {
        return FMLEnvironment.dist.isClient() && Screen.hasShiftDown();
    }
}
