package dev.craft.core.client;

import java.util.Locale;

import dev.craft.Craft;
import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.OutputPayloads;
import dev.craft.core.energy.OutputReadout;
import dev.craft.core.energy.OutputReadout.OutputReading;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.Nullable;

/**
 * Shows the current output and/or consumption of the block under the crosshair (e.g. "Output: 12.5 EU/t",
 * "Consumes: 2 EU/t") below the crosshair.
 */
public final class OutputOverlay {
    private static final int QUERY_INTERVAL = 10;

    @Nullable
    private static BlockPos target;
    private static int ticksToQuery;

    private OutputOverlay() {
    }

    @EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR,
                    ResourceLocation.fromNamespaceAndPath(Craft.MODID, "output"), OutputOverlay::render);
        }
    }

    @EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
    public static final class GameEvents {
        private GameEvents() {
        }

        @SubscribeEvent
        public static void clientTick(ClientTickEvent.Post event) {
            BlockPos looked = lookedAtReadout();
            if (looked == null) {
                target = null;
                return;
            }
            if (!looked.equals(target)) {
                target = looked;
                ticksToQuery = 0;
            }
            if (--ticksToQuery <= 0) {
                ticksToQuery = QUERY_INTERVAL;
                OutputPayloads.query(looked);
            }
        }
    }

    @Nullable
    private static BlockPos lookedAtReadout() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null
                || !(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = hit.getBlockPos();
        BlockEntity entity = minecraft.level.getBlockEntity(pos);
        return entity instanceof OutputReadout || entity instanceof ConsumptionReadout ? pos : null;
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        OutputPayloads.Answer answer = OutputPayloads.lastAnswer();
        if (target == null || answer == null || !answer.pos().equals(target) || minecraft.options.hideGui
                || minecraft.screen != null) {
            return;
        }
        int y = graphics.guiHeight() / 2 + 10;
        if (answer.output().isPresent()) {
            drawLine(graphics, minecraft, "gui.craft.output", answer.output().get(), y);
            y += minecraft.font.lineHeight + 1;
        }
        if (answer.consumption().isPresent()) {
            drawLine(graphics, minecraft, "gui.craft.consumption", answer.consumption().get(), y);
        }
    }

    private static void drawLine(GuiGraphics graphics, Minecraft minecraft, String key, OutputReading reading, int y) {
        Component text = Component.translatable(key, format(reading.rate()), reading.unit().symbol());
        int x = (graphics.guiWidth() - minecraft.font.width(text)) / 2;
        graphics.drawString(minecraft.font, text, x, y, 0xFFFFFF);
    }

    /** Whole numbers without decimals, otherwise up to two. */
    static String format(double rate) {
        if (Math.abs(rate - Math.rint(rate)) < 0.005) {
            return Long.toString(Math.round(rate));
        }
        String text = String.format(Locale.ROOT, "%.2f", rate);
        return text.endsWith("0") ? text.substring(0, text.length() - 1) : text;
    }
}
