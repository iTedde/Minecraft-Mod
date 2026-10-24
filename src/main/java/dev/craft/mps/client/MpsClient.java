package dev.craft.mps.client;

import java.util.ArrayList;
import java.util.List;

import dev.craft.Craft;
import dev.craft.mps.ModularItems;
import dev.craft.mps.MpsBlocks;
import dev.craft.mps.MpsEnergy;
import dev.craft.mps.MpsItems;
import dev.craft.mps.item.ModularItem;
import dev.craft.mps.module.ModuleTarget;
import dev.craft.mps.module.PowerModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Client registration of Modular Powersuits: Tinker Table renderer and the modular item tooltip. */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class MpsClient {
    private MpsClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MpsBlocks.TINKER_TABLE_ENTITY.get(), TinkerTableRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new TinkerTableRenderer.ItemRenderer();
                }
                return renderer;
            }
        }, MpsItems.TINKER_TABLE.get());
    }

    @EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
    public static final class GameEvents {
        private GameEvents() {
        }

        /**
         * {@code MuseCommonStrings.addInformation}: tool mode, energy, with shift the installed modules; components
         * show their description with shift ({@code ItemComponent.addInformation}).
         */
        @SubscribeEvent
        public static void tooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            List<Component> tooltip = event.getToolTip();
            if (stack.getItem() instanceof ModularItem item) {
                int line = 1;
                if (item.getTarget() == ModuleTarget.TOOL) {
                    String mode = ModularItems.getMode(stack);
                    if (!mode.isEmpty()) {
                        tooltip.add(line++, Component.translatable("tooltip.craft.mps.mode",
                                Component.translatable("mps.module." + PowerModule.key(mode))
                                        .withStyle(ChatFormatting.RED)));
                    }
                }
                tooltip.add(line++, Component.translatable("tooltip.craft.mps.energy",
                                MpsFormat.formatShort(MpsEnergy.getJoules(stack)),
                                MpsFormat.formatShort(MpsEnergy.getMaxJoules(stack)))
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                if (!Screen.hasShiftDown()) {
                    tooltip.add(line, shiftHint());
                    return;
                }
                List<PowerModule> installed = ModularItems.getInstalledModules(stack);
                if (installed.isEmpty()) {
                    for (String text : wrap(Component.translatable("tooltip.craft.mps.no_modules").getString())) {
                        tooltip.add(line++, Component.literal(text));
                    }
                    return;
                }
                tooltip.add(line++, Component.translatable("tooltip.craft.mps.installed"));
                for (PowerModule module : installed) {
                    tooltip.add(line++, Component.translatable("mps.module." + module.getKey()));
                }
            } else if (MpsItems.components().stream().anyMatch(component -> stack.is(component.get()))) {
                if (!Screen.hasShiftDown()) {
                    tooltip.add(1, shiftHint());
                    return;
                }
                String key = stack.getDescriptionId() + ".desc";
                tooltip.add(1, Component.translatable("tooltip.craft.mps.for_tinker_table")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                int line = 2;
                for (String text : wrap(Component.translatable(key).getString())) {
                    tooltip.add(line++, Component.literal(text));
                }
            }
        }

        /** {@code MuseStringUtils.wrapStringToLength(text, 30)}: lines of at most 30 characters, split at spaces. */
        static List<String> wrap(String text) {
            List<String> lines = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (String word : text.split(" ")) {
                if (current.length() > 0 && current.length() + 1 + word.length() > 30) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                if (current.length() > 0) {
                    current.append(' ');
                }
                current.append(word);
            }
            if (current.length() > 0) {
                lines.add(current.toString());
            }
            return lines;
        }

        private static Component shiftHint() {
            return Component.translatable("tooltip.craft.mps.shift")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        }
    }
}
