package dev.craft.mps.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.craft.Craft;
import dev.craft.mps.ModularItems;
import dev.craft.mps.MpsDataComponents;
import dev.craft.mps.MpsPayloads;
import dev.craft.mps.MpsTool;
import dev.craft.mps.module.MpsModules;
import dev.craft.mps.module.PowerModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * MPS {@code ClientTickHandler} mode wheel and {@code RenderTickHandler} mode icons (spec 29 §8.3): sneaking with a
 * modular item in hand, the mouse wheel cycles the power tool modes instead of the hotbar; the current mode sits above
 * the hotbar slot with the neighbours beside it, sliding for 200 ms after a change.
 */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class MpsToolClient {
    private static final long SWAP_TIME = 200L;
    private static long lastSwapTime;
    private static int lastSwapDirection;

    private MpsToolClient() {
    }

    @EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(Craft.MODID, "mps_modes"),
                    MpsToolClient::render);
        }
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null || !player.isShiftKeyDown()) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!ModularItems.isModular(stack)) {
            return;
        }
        event.setCanceled(true);
        double delta = event.getScrollDeltaY();
        int wheel = Math.abs(delta) < 1.0 ? (int) Math.signum(delta) : (int) delta;
        List<String> modes = ModularItems.getModes(stack);
        if (modes.isEmpty() || wheel == 0) {
            return;
        }
        String mode = ModularItems.getMode(stack);
        if (mode.isEmpty()) {
            mode = modes.get(0);
        }
        String newMode = modes.get(MpsTool.clampMode(modes.indexOf(mode) + wheel, modes.size()));
        stack.set(MpsDataComponents.MODE.get(), newMode);
        lastSwapTime = System.currentTimeMillis();
        lastSwapDirection = (int) Math.signum(wheel);
        PacketDistributor.sendToServer(new MpsPayloads.ModeChange(player.getInventory().selected, newMode));
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }
        int i = player.getInventory().selected;
        ItemStack stack = player.getInventory().getItem(i);
        if (!ModularItems.isModular(stack)) {
            return;
        }
        List<String> modes = ModularItems.getModes(stack);
        String mode = ModularItems.getMode(stack);
        int modeIndex = modes.indexOf(mode);
        if (modeIndex < 0) {
            return;
        }
        ResourceLocation current = icon(mode);
        ResourceLocation prev = null;
        ResourceLocation next = null;
        if (modes.size() > 1) {
            prev = icon(modes.get((modeIndex + modes.size() - 1) % modes.size()));
            next = icon(modes.get((modeIndex + 1) % modes.size()));
        }
        double sw = graphics.guiWidth();
        double sh = graphics.guiHeight();
        double baroffset = -40.0;
        if (!player.getAbilities().instabuild) {
            baroffset -= 16.0;
            if (player.getArmorValue() > 0) {
                baroffset -= 8.0;
            }
        }
        double prevX = sw / 2.0 - 105.0 + 20.0 * i;
        double prevY = sh + baroffset + 10.0;
        double currX = sw / 2.0 - 89.0 + 20.0 * i;
        double currY = sh + baroffset;
        double nextX = sw / 2.0 - 73.0 + 20.0 * i;
        double nextY = sh + baroffset + 10.0;
        long swapTime = Math.min(System.currentTimeMillis() - lastSwapTime, SWAP_TIME);
        // icons are cut off below the current mode's row
        graphics.enableScissor(0, 0, (int) sw, (int) (sh + baroffset + 16.0));
        RenderSystem.enableBlend();
        if (swapTime != SWAP_TIME && lastSwapDirection != 0) {
            double r1 = 1.0 - swapTime / (double) SWAP_TIME;
            double r2 = swapTime / (double) SWAP_TIME;
            if (lastSwapDirection == -1) {
                nextX = currX * r1 + nextX * r2;
                nextY = currY * r1 + nextY * r2;
                currX = prevX * r1 + currX * r2;
                currY = prevY * r1 + currY * r2;
                drawIcon(graphics, current, currX, currY, 0.8F);
                drawIcon(graphics, next, nextX, nextY, 0.8F);
            } else {
                prevX = currX * r1 + prevX * r2;
                prevY = currY * r1 + prevY * r2;
                currX = nextX * r1 + currX * r2;
                currY = nextY * r1 + currY * r2;
                drawIcon(graphics, prev, prevX, prevY, 0.8F);
                drawIcon(graphics, current, currX, currY, 0.8F);
            }
        } else {
            drawIcon(graphics, prev, prevX, prevY, 0.4F);
            drawIcon(graphics, current, currX, currY, 0.8F);
            drawIcon(graphics, next, nextX, nextY, 0.4F);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        graphics.disableScissor();
    }

    private static ResourceLocation icon(String module) {
        PowerModule definition = MpsModules.get(module);
        return definition == null ? null : definition.getIcon();
    }

    private static void drawIcon(GuiGraphics graphics, ResourceLocation icon, double x, double y, float alpha) {
        if (icon != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            graphics.blit(icon, (int) x, (int) y, 0, 0, 16, 16, 16, 16);
        }
    }
}
