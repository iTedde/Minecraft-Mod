package dev.craft.lightshield.client;

import dev.craft.ic2.item.ElectricItems;
import dev.craft.lightshield.ElectricLightShieldItem;
import dev.craft.lightshield.Flashlight;
import dev.craft.lightshield.FlashlightPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Flashlight GUI of the light shield (spec 51 §2): range and cone sliders, cost and runtime. */
public class FlashlightScreen extends Screen {
    private static final int SLIDER_WIDTH = 200;

    private Flashlight setting;

    public FlashlightScreen(Flashlight setting) {
        super(Component.translatable("gui.craft.flashlight"));
        this.setting = setting;
    }

    /** Opens the GUI for the held light shield. */
    public static void open() {
        ItemStack shield = heldShield();
        if (!shield.isEmpty()) {
            Minecraft.getInstance().setScreen(new FlashlightScreen(ElectricLightShieldItem.flashlight(shield)));
        }
    }

    private static ItemStack heldShield() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return ItemStack.EMPTY;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ElectricLightShieldItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        int x = (width - SLIDER_WIDTH) / 2;
        int y = height / 2 - 40;
        addRenderableWidget(new StageSlider(x, y, setting.range()) {
            @Override
            protected Component label(int stage) {
                return Component.translatable("gui.craft.flashlight.range", 16 * stage);
            }

            @Override
            protected void changed(int stage) {
                apply(Flashlight.of(stage, setting.width()));
            }
        });
        addRenderableWidget(new StageSlider(x, y + 26, setting.width()) {
            @Override
            protected Component label(int stage) {
                return Component.translatable("gui.craft.flashlight.width", 15 * stage);
            }

            @Override
            protected void changed(int stage) {
                apply(Flashlight.of(setting.range(), stage));
            }
        });
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x + 50, y + 90, SLIDER_WIDTH - 100, 20).build());
    }

    private void apply(Flashlight next) {
        if (!next.equals(setting)) {
            setting = next;
            PacketDistributor.sendToServer(new FlashlightPayload(next));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int y = height / 2 - 40;
        graphics.drawCenteredString(font, title, width / 2, y - 20, 0xFFFFFF);
        ItemStack shield = heldShield();
        long seconds = setting.runtimeTicks(shield.isEmpty() ? 0 : ElectricItems.getCharge(shield)) / 20;
        graphics.drawCenteredString(font, Component.translatable("gui.craft.flashlight.cost",
                setting.euPerTickText(), seconds / 3600, String.format("%02d", seconds / 60 % 60),
                String.format("%02d", seconds % 60)), width / 2, y + 56, 0xC0C0C0);
        if (shield.isEmpty()) {
            onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Slider snapping to the stages 1–4. */
    private abstract static class StageSlider extends AbstractSliderButton {
        StageSlider(int x, int y, int stage) {
            super(x, y, SLIDER_WIDTH, 20, Component.empty(), toValue(stage));
            updateMessage();
        }

        private static double toValue(int stage) {
            return (stage - Flashlight.MIN_STAGE) / (double) (Flashlight.MAX_STAGE - Flashlight.MIN_STAGE);
        }

        int stage() {
            return Flashlight.MIN_STAGE + (int) Math.round(value * (Flashlight.MAX_STAGE - Flashlight.MIN_STAGE));
        }

        protected abstract Component label(int stage);

        protected abstract void changed(int stage);

        @Override
        protected void updateMessage() {
            setMessage(label(stage()));
        }

        @Override
        protected void applyValue() {
            value = toValue(stage());
            changed(stage());
        }
    }
}
