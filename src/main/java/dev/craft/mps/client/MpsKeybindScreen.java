package dev.craft.mps.client;

import java.util.ArrayList;
import java.util.List;

import dev.craft.mps.ModularItems;
import dev.craft.mps.client.MpsKeybinds.Binding;
import dev.craft.mps.client.MpsKeybinds.BoundModule;
import dev.craft.mps.module.PowerModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * MPS {@code KeyConfigGui}/{@code KeybindConfigFrame} (spec 29 §9), 256×226: "New" waits for a free key (Escape cancels), toggleable
 * modules of the equipped items are dragged onto keys (within 32 px), keys dropped on "Trash" are removed. Bound
 * modules gather around their key and push each other apart like the original. Saved on close.
 */
public class MpsKeybindScreen extends Screen {
    private static final int WIDTH = 256;
    private static final int HEIGHT = 226;
    private static final int BORDER = 0xCC19E619;
    private static final int FILL = 0xCC003300;
    private static final int BUTTON_OUTSIDE = 0xFF8099CC;
    private static final int BUTTON_INSIDE = 0xFF4D4D4D;
    private static final int LIGHT_BLUE = 0xFF8080FF;
    /** {@code Colour.YELLOW} is dark blue in MPS 0.3.2. */
    private static final int YELLOW = 0xFF000080;

    private double centreX;
    private double centreY;
    /** Frame corners relative to the centre. */
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    private final List<BoundModule> modules = new ArrayList<>();
    private Object selected;
    private Binding closestKeybind;
    private boolean selecting;
    private long takenTime;

    public MpsKeybindScreen() {
        super(Component.translatable("gui.craft.mps.keybinds"));
        for (Binding binding : MpsKeybinds.bindings()) {
            modules.addAll(binding.modules);
        }
    }

    @Override
    protected void init() {
        centreX = width / 2.0;
        centreY = height / 2.0;
        minX = -0.95 * WIDTH / 2.0;
        maxX = 0.95 * WIDTH / 2.0;
        minY = -0.95 * HEIGHT / 2.0;
        maxY = 0.95 * HEIGHT / 2.0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        MpsKeybinds.write();
    }

    /** {@code refreshModules}: new toggleable modules of the equipped items in a line along the left edge. */
    private void refreshModules() {
        List<PowerModule> installed = new ArrayList<>();
        for (ItemStack stack : ModularItems.equipped(minecraft.player)) {
            if (ModularItems.isModular(stack)) {
                installed.addAll(ModularItems.getInstalledModules(stack));
            }
        }
        int num = installed.size();
        int index = 0;
        for (PowerModule module : installed) {
            if (module.getKind() == PowerModule.Kind.TOGGLEABLE && !alreadyAdded(module)) {
                index++;
                double ax = minX + 10.0;
                double ay = minY + 10.0;
                double by = maxY - 10.0;
                double y = num < 2 ? (ay + by) / 2.0 : ay + (by - ay) / (num + 1) * index;
                modules.add(new BoundModule(module, ax, y));
            }
        }
    }

    private boolean alreadyAdded(PowerModule module) {
        for (BoundModule bound : modules) {
            if (bound.module.getName().equals(module.getName())) {
                return true;
            }
        }
        return false;
    }

    /** {@code update}: called every frame like the original. */
    private void update(double mouseX, double mouseY) {
        if (selecting) {
            return;
        }
        refreshModules();
        closestKeybind = null;
        double closestDistance = Double.MAX_VALUE;
        if (selected instanceof BoundModule module) {
            module.x = mouseX;
            module.y = mouseY;
            for (Binding binding : MpsKeybinds.bindings()) {
                double distance = Math.hypot(binding.x - module.x, binding.y - module.y);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    if (closestDistance < 32.0) {
                        closestKeybind = binding;
                    }
                }
            }
        } else if (selected instanceof Binding binding) {
            binding.x = mouseX;
            binding.y = mouseY;
        }
        for (Binding binding : MpsKeybinds.bindings()) {
            if (binding != selected) {
                unbindFarModules(binding);
            }
            attractBoundModules(binding);
        }
        for (BoundModule module : modules) {
            if (module != selected) {
                repelOtherModules(module.x, module.y, module);
            }
        }
        for (Binding binding : MpsKeybinds.bindings()) {
            if (binding != selected) {
                repelOtherModules(binding.x, binding.y, null);
            }
        }
        for (BoundModule module : modules) {
            module.x = Mth.clamp(module.x, minX, maxX);
            module.y = Mth.clamp(module.y, minY, maxY);
        }
        for (Binding binding : MpsKeybinds.bindings()) {
            binding.x = Mth.clamp(binding.x, minX, maxX);
            binding.y = Mth.clamp(binding.y, minY, maxY);
        }
    }

    private static int targetDistance(Binding binding) {
        int targetDistance = 16;
        if (binding.modules.size() > 6) {
            targetDistance += (binding.modules.size() - 6) * 3;
        }
        return targetDistance;
    }

    private static void unbindFarModules(Binding binding) {
        int maxDistance = targetDistance(binding) * 2;
        binding.modules.removeIf(module -> {
            double dx = module.x - binding.x;
            double dy = module.y - binding.y;
            return dx * dx + dy * dy > maxDistance * maxDistance;
        });
    }

    /** Pulls the bound modules halfway towards a circle around the key. */
    private void attractBoundModules(Binding binding) {
        for (BoundModule module : binding.modules) {
            if (module == selected) {
                continue;
            }
            double dx = module.x - binding.x;
            double dy = module.y - binding.y;
            double length = Math.hypot(dx, dy);
            if (length == 0.0) {
                continue;
            }
            double targetX = dx / length * targetDistance(binding) + binding.x;
            double targetY = dy / length * targetDistance(binding) + binding.y;
            module.x = (module.x + targetX) / 2.0;
            module.y = (module.y + targetY) / 2.0;
        }
    }

    /** Pushes the other modules out to 16 px around a module or key. */
    private void repelOtherModules(double x, double y, BoundModule self) {
        for (BoundModule other : modules) {
            if (other == selected || other == self) {
                continue;
            }
            double dx = other.x - x;
            double dy = other.y - y;
            double distance = Math.hypot(dx, dy);
            if (distance < 16.0 && distance > 0.0) {
                double targetX = dx / distance * 16.0 + x;
                double targetY = dy / distance * 16.0 + y;
                double midX = (other.x + targetX) / 2.0;
                double midY = (other.y + targetY) / 2.0;
                if (Math.hypot(midX - x, midY - y) > 2.0) {
                    other.x = midX;
                    other.y = midY;
                }
            }
        }
    }

    private static boolean moduleHit(BoundModule module, double x, double y) {
        return Math.abs(x - module.x) < 8.0 && Math.abs(y - module.y) < 8.0;
    }

    private double halfWidth(String label) {
        return font.width(label) / 2 + 2;
    }

    private boolean buttonHit(String label, double bx, double by, double x, double y) {
        return Math.abs(x - bx) < halfWidth(label) && Math.abs(y - by) < 6.0;
    }

    private boolean bindingHit(Binding binding, double x, double y) {
        return buttonHit(binding.label(), binding.x, binding.y, x, y);
    }

    private String newLabel() {
        return Component.translatable("gui.craft.mps.keybinds.new").getString();
    }

    private String trashLabel() {
        return Component.translatable("gui.craft.mps.keybinds.trash").getString();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = mouseX - centreX;
        double y = mouseY - centreY;
        if (button == 0) {
            if (selected == null) {
                for (BoundModule module : modules) {
                    if (moduleHit(module, x, y)) {
                        selected = module;
                        return true;
                    }
                }
                for (Binding binding : MpsKeybinds.bindings()) {
                    if (bindingHit(binding, x, y)) {
                        selected = binding;
                        return true;
                    }
                }
            }
            if (buttonHit(newLabel(), 0.0, -8.0, x, y)) {
                selecting = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double x = mouseX - centreX;
            double y = mouseY - centreY;
            if (selected instanceof BoundModule module && closestKeybind != null) {
                if (!closestKeybind.modules.contains(module)) {
                    closestKeybind.modules.add(module);
                }
            } else if (selected instanceof Binding binding && buttonHit(trashLabel(), 0.0, 8.0, x, y)) {
                MpsKeybinds.bindings().remove(binding);
            }
            selected = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /** {@code handleKeyboard}: the next key becomes a new binding unless it is taken. */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selecting) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                selecting = false;
            } else if (MpsKeybinds.isTaken(keyCode)) {
                takenTime = System.currentTimeMillis();
            } else {
                MpsKeybinds.bindings().add(new Binding(keyCode, 0.0, -28.0));
            }
            selecting = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        update(mouseX - centreX, mouseY - centreY);
        int left = (int) centreX - WIDTH / 2;
        int top = (int) centreY - HEIGHT / 2;
        graphics.fill(left + 1, top + 1, left + WIDTH - 1, top + HEIGHT - 1, FILL);
        graphics.renderOutline(left, top, WIDTH, HEIGHT, BORDER);
        int cx = (int) centreX;
        int cy = (int) centreY;
        if (selecting) {
            graphics.drawCenteredString(font, Component.translatable("gui.craft.mps.keybinds.press_key"), cx,
                    cy - 4, 0xFFFFFF);
            return;
        }
        drawButton(graphics, newLabel(), 0.0, -8.0);
        drawButton(graphics, trashLabel(), 0.0, 8.0);
        for (int line = 0; line < 3; line++) {
            graphics.drawCenteredString(font, Component.translatable("gui.craft.mps.keybinds.help." + line), cx,
                    cy + 40 + 10 * line - 4, 0xFFFFFF);
        }
        if (takenTime + 1000L > System.currentTimeMillis()) {
            graphics.drawCenteredString(font, Component.translatable("gui.craft.mps.keybinds.taken"), cx,
                    cy - 28 - 4, 0xFFFFFF);
        }
        for (BoundModule module : modules) {
            graphics.blit(module.module.getIcon(), (int) (centreX + module.x) - 8, (int) (centreY + module.y) - 8,
                    0, 0, 16, 16, 16, 16);
        }
        for (Binding binding : MpsKeybinds.bindings()) {
            drawButton(graphics, binding.label(), binding.x, binding.y);
            for (BoundModule module : binding.modules) {
                drawLine(graphics, binding.x, binding.y, module.x, module.y, LIGHT_BLUE);
            }
        }
        if (selected != null && closestKeybind != null && selected instanceof BoundModule module) {
            drawLine(graphics, module.x, module.y, closestKeybind.x, closestKeybind.y, YELLOW);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
    }

    /** {@code ClickableButton.draw}: outside colour as frame, inside colour as fill, label centred. */
    private void drawButton(GuiGraphics graphics, String label, double x, double y) {
        int half = (int) halfWidth(label);
        int px = (int) (centreX + x);
        int py = (int) (centreY + y);
        graphics.fill(px - half, py - 6, px + half, py + 6, BUTTON_OUTSIDE);
        graphics.fill(px - half + 1, py - 5, px + half - 1, py + 5, BUTTON_INSIDE);
        graphics.drawCenteredString(font, label, px, py - 4, 0xFFFFFF);
    }

    private void drawLine(GuiGraphics graphics, double x1, double y1, double x2, double y2, int colour) {
        int steps = (int) Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0.0 : (double) i / steps;
            int px = (int) (centreX + x1 + (x2 - x1) * t);
            int py = (int) (centreY + y1 + (y2 - y1) * t);
            graphics.fill(px, py, px + 1, py + 1, colour);
        }
    }

    /** Module name and description while left shift is held ({@code Config.doAdditionalInfo}). */
    private void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!Screen.hasShiftDown()) {
            return;
        }
        for (BoundModule module : modules) {
            if (moduleHit(module, mouseX - centreX, mouseY - centreY)) {
                List<FormattedCharSequence> lines = new ArrayList<>();
                lines.add(Component.translatable("mps.module." + module.module.getKey()).getVisualOrderText());
                lines.addAll(font.split(FormattedText.of(Component.translatable(
                        "mps.module." + module.module.getKey() + ".desc").getString()), 180));
                graphics.renderTooltip(font, lines, mouseX, mouseY);
                return;
            }
        }
    }
}
