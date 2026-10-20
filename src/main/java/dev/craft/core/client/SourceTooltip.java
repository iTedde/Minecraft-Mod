package dev.craft.core.client;

import java.util.List;

import dev.craft.Craft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Shift tooltip with the source text of a raw material (spec 24 §2.2): a hint line, and the wrapped text from
 * {@code source.<namespace>.<path>} while shift is held. Items without such a text are left alone.
 */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class SourceTooltip {
    private static final int WIDTH = 220;

    private SourceTooltip() {
    }

    /** Lang key of the source text of {@code item}; it may not exist. */
    public static String key(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return "source." + id.getNamespace() + "." + id.getPath();
    }

    public static boolean hasSource(Item item) {
        return I18n.exists(key(item));
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        if (!hasSource(item)) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.craft.source.hint").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        Style style = Style.EMPTY.withColor(ChatFormatting.GRAY);
        for (FormattedText line : Minecraft.getInstance().font.getSplitter()
                .splitLines(Component.translatable(key(item)), WIDTH, style)) {
            tooltip.add(Component.literal(line.getString()).withStyle(style));
        }
    }
}
