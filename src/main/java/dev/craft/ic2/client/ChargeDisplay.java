package dev.craft.ic2.client;

import java.util.Locale;

import dev.craft.Craft;
import dev.craft.ic2.item.CustomElectricItem;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.armor.JetpackItem;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Charge display (not in IC2, which only shows the damage bar): tooltip "x / y EU" on every electric item, fuel in
 * percent on the fuel jetpack, and a HUD list of the worn electric armor and jetpacks left of the hotbar.
 */
public final class ChargeDisplay {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private ChargeDisplay() {
    }

    @EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.HOTBAR,
                    ResourceLocation.fromNamespaceAndPath(Craft.MODID, "charge"), ChargeDisplay::render);
        }
    }

    @EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
    public static final class GameEvents {
        private GameEvents() {
        }

        @SubscribeEvent
        public static void tooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.getItem() instanceof CustomElectricItem) {
                // Shows its own energy line, like IC2's canShowChargeToolTip = false.
                return;
            }
            if (stack.getItem() instanceof ElectricItem item) {
                event.getToolTip().add(1, Component.translatable("tooltip.craft.charge",
                        group(ElectricItems.getCharge(stack)), group(item.getMaxCharge())).withColor(0xAAAAAA));
            } else if (stack.getItem() instanceof JetpackItem jetpack) {
                event.getToolTip().add(1, Component.translatable("tooltip.craft.fuel",
                        percent(jetpack.getCharge(stack), jetpack.getMaxCharge(stack))).withColor(0xAAAAAA));
            }
        }
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || minecraft.getDebugOverlay().showDebugScreen()) {
            return;
        }
        // Left of the hotbar (Xaero's minimap sits top left), right-aligned, skipping the left offhand slot
        // and the left attack indicator; laid out from the hotbar outwards, so iterate the slots in reverse
        int x = graphics.guiWidth() / 2 - 91 - 6;
        boolean offhandLeft = player.getMainArm() == HumanoidArm.RIGHT;
        if (offhandLeft && !player.getOffhandItem().isEmpty()) {
            x -= 29;
        } else if (!offhandLeft && minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
            x -= 24;
        }
        int y = graphics.guiHeight() - 19;
        for (int i = ARMOR_SLOTS.length - 1; i >= 0; i--) {
            ItemStack stack = player.getItemBySlot(ARMOR_SLOTS[i]);
            int percent;
            if (stack.getItem() instanceof ElectricItem item) {
                percent = percent(ElectricItems.getCharge(stack), item.getMaxCharge(stack));
            } else if (stack.getItem() instanceof JetpackItem jetpack) {
                percent = percent(jetpack.getCharge(stack), jetpack.getMaxCharge(stack));
            } else {
                continue;
            }
            String text = percent + " %";
            x -= minecraft.font.width(text);
            int color = percent <= 10 ? 0xFF5555 : percent <= 25 ? 0xFFFF55 : 0xFFFFFF;
            graphics.drawString(minecraft.font, text, x, y + 4, color);
            x -= 18;
            graphics.renderItem(stack, x, y);
            x -= 6;
        }
    }

    static int percent(int charge, int max) {
        return max <= 0 ? 0 : (int) ((long) charge * 100 / max);
    }

    /** Thousands grouped with spaces, e.g. "1 000 000". */
    static String group(int value) {
        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ');
    }
}
