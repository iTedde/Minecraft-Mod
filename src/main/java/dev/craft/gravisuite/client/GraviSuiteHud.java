package dev.craft.gravisuite.client;

import java.util.ArrayList;
import java.util.List;

import dev.craft.Craft;
import dev.craft.gravisuite.GsItems;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * GraviSuite {@code ClientTickHandler.tickEnd} HUD (spec 28 §7): status line of the worn chest piece and
 * "Energy level: n%", top right (pack option 2, user decision – Xaero's minimap sits top left).
 */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class GraviSuiteHud {
    private GraviSuiteHud() {
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "gravisuite"), GraviSuiteHud::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }
        List<Component> lines = lines(player.getItemBySlot(EquipmentSlot.CHEST));
        Font font = minecraft.font;
        int y = 2;
        for (Component line : lines) {
            graphics.drawString(font, line, graphics.guiWidth() - 2 - font.width(line), y, 0xFFFFFF);
            y += 3 + font.lineHeight;
        }
    }

    /** The lines for a worn chest piece; empty if it is no GraviSuite pack. */
    static List<Component> lines(ItemStack chest) {
        List<Component> lines = new ArrayList<>();
        if (chest.is(GsItems.ULTIMATE_LAPPACK.get()) || chest.is(GsItems.ADVANCED_LAPPACK.get())) {
            lines.add(energyLevel(chest));
        }
        return lines;
    }

    /** "Energy level: n%" with n = charge · 100 / max; only "n%" is coloured: ≤ 5 red, ≤ 10 gold, otherwise white. */
    static Component energyLevel(ItemStack stack) {
        int max = ((ElectricItem) stack.getItem()).getMaxCharge();
        int level = (int) ((long) ElectricItems.getCharge(stack) * 100 / max);
        ChatFormatting color = level <= 5 ? ChatFormatting.RED : level <= 10 ? ChatFormatting.GOLD : ChatFormatting.WHITE;
        return Component.translatable("hud.craft.gravisuite.energy_level",
                Component.literal(level + "%").withStyle(color));
    }
}
