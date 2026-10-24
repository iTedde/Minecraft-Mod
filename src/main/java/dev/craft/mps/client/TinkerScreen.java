package dev.craft.mps.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.craft.mps.ModularItems;
import dev.craft.mps.MpsEnergy;
import dev.craft.mps.MpsPayloads;
import dev.craft.mps.TinkerActions;
import dev.craft.mps.item.ModularItem;
import dev.craft.mps.item.PowerArmorItem;
import dev.craft.mps.module.ModuleData;
import dev.craft.mps.module.MpsModules;
import dev.craft.mps.module.PowerModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * MPS {@code GuiTinkerTable} (spec 29 §4), 256×200 in the original's frames: modular items of the inventory on the
 * left, the item's modules by category, install/salvage with the cost, item info and the sliders of an installed
 * module. All changes are requests to the server ({@link MpsPayloads}).
 */
public class TinkerScreen extends Screen {
    private static final int WIDTH = 256;
    private static final int HEIGHT = 200;
    private static final int BORDER = 0xCC19E619;
    private static final int FILL = 0xCC003300;
    private static final int FRAME_BORDER = 0xCC8080FF;
    private static final int FRAME_FILL = 0xCC000080;
    private static final int SLIDER_FILL = 0xFFE6991A;

    private int left;
    private int top;
    /** Inventory slots holding modular items, bottom to top as the original lays them out. */
    private final List<Integer> itemSlots = new ArrayList<>();
    private int selectedSlot = -1;
    private String selectedModule;
    private String draggedTweak;
    /** Slider values while dragging, sent on release ({@code MusePacketTweakRequest}). */
    private final Map<String, Double> sliderValues = new HashMap<>();

    public TinkerScreen() {
        super(Component.translatable("block.craft.tinker_table"));
    }

    /** Called from the block on the client. */
    public static void open() {
        Minecraft.getInstance().setScreen(new TinkerScreen());
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int absX(double relative) {
        return left + (int) ((relative + 1.0) * WIDTH / 2.0);
    }

    private int absY(double relative) {
        return top + (int) ((relative + 1.0) * HEIGHT / 2.0);
    }

    private Inventory inventory() {
        return Minecraft.getInstance().player.getInventory();
    }

    private void loadItems() {
        itemSlots.clear();
        Inventory inventory = inventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).getItem() instanceof ModularItem) {
                itemSlots.add(i);
            }
        }
        if (!itemSlots.contains(selectedSlot)) {
            selectedSlot = -1;
        }
    }

    private ItemStack selectedStack() {
        return selectedSlot < 0 ? ItemStack.EMPTY : TinkerActions.modularItemAt(minecraft.player, selectedSlot);
    }

    private PowerModule selectedModule(ItemStack stack) {
        if (stack.isEmpty() || selectedModule == null) {
            return null;
        }
        PowerModule module = MpsModules.get(selectedModule);
        return module != null && ModularItems.getValidModules(stack).contains(module) ? module : null;
    }

    /** {@code pointsInLine} from the bottom to the top of the item column. */
    private int[] itemPosition(int index) {
        int x = (absX(-0.95) + absX(-0.78)) / 2;
        int bottom = absY(0.95);
        int topY = absY(-0.95);
        double step = (double) (bottom - topY) / (itemSlots.size() + 1);
        return new int[]{x, (int) (bottom - step * (index + 1))};
    }

    /** Category rows of the module frame with the icon centres ({@code ModuleSelectionSubFrame}). */
    private Map<String, List<PowerModule>> categories(ItemStack stack) {
        Map<String, List<PowerModule>> categories = new LinkedHashMap<>();
        for (PowerModule module : ModularItems.getValidModules(stack)) {
            categories.computeIfAbsent(module.getCategory(), key -> new ArrayList<>()).add(module);
        }
        return categories;
    }

    /** {@code DrawableMuseRect}: translucent fill with a one-pixel border line. */
    private void frame(GuiGraphics graphics, int x1, int y1, int x2, int y2) {
        graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, FRAME_FILL);
        graphics.renderOutline(x1, y1, x2 - x1, y2 - y1, FRAME_BORDER);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        loadItems();
        graphics.fill(left + 1, top + 1, left + WIDTH - 1, top + HEIGHT - 1, FILL);
        graphics.renderOutline(left, top, WIDTH, HEIGHT, BORDER);
        frame(graphics, absX(-0.95), absY(-0.95), absX(-0.78), absY(0.95));
        for (int i = 0; i < itemSlots.size(); i++) {
            int[] position = itemPosition(i);
            graphics.renderItem(inventory().getItem(itemSlots.get(i)), position[0] - 8, position[1] - 8);
            if (itemSlots.get(i) == selectedSlot) {
                graphics.renderOutline(position[0] - 10, position[1] - 10, 20, 20, 0xFFFFFFFF);
            }
        }
        if (itemSlots.isEmpty()) {
            for (int line = 0; line < 4; line++) {
                graphics.drawCenteredString(font, Component.translatable("gui.craft.mps.no_items." + line),
                        absX(0.0), absY(0.0) - 15 + 10 * line, 0xFFFFFF);
            }
        }
        ItemStack stack = selectedStack();
        if (!stack.isEmpty()) {
            renderModules(graphics, stack);
            renderInfo(graphics, stack);
            PowerModule module = selectedModule(stack);
            if (module != null) {
                renderInstall(graphics, stack, module);
                if (ModularItems.hasModule(stack, module.getName())) {
                    renderTweaks(graphics, stack, module);
                }
            }
        }
        renderTooltips(graphics, mouseX, mouseY, stack);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
    }

    private void renderModules(GuiGraphics graphics, ItemStack stack) {
        int x1 = absX(-0.75);
        int y1 = absY(-0.95);
        frame(graphics, x1, y1, absX(-0.05), absY(0.55));
        int row = 0;
        for (Map.Entry<String, List<PowerModule>> category : categories(stack).entrySet()) {
            int rowTop = y1 + 4 + 30 * row++;
            graphics.drawString(font, Component.translatable("mps.category." + PowerModule.key(category.getKey())),
                    x1 + 4, rowTop, 0xFFFFFF);
            int column = 0;
            for (PowerModule module : category.getValue()) {
                int cx = x1 + 4 + 8 + 16 * column++;
                int cy = rowTop + 16;
                boolean installed = ModularItems.hasModule(stack, module.getName());
                if (!installed) {
                    graphics.setColor(0.5F, 0.5F, 0.5F, 1.0F);
                }
                graphics.blit(module.getIcon(), cx - 8, cy - 8, 0, 0, 16, 16, 16, 16);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                if (module.getName().equals(selectedModule)) {
                    graphics.renderOutline(cx - 10, cy - 10, 20, 20, 0xFFFFFFFF);
                }
            }
        }
    }

    private PowerModule moduleAt(ItemStack stack, double mouseX, double mouseY) {
        int x1 = absX(-0.75);
        int y1 = absY(-0.95);
        int row = 0;
        for (List<PowerModule> modules : categories(stack).values()) {
            int rowTop = y1 + 4 + 30 * row++;
            int column = 0;
            for (PowerModule module : modules) {
                int cx = x1 + 4 + 8 + 16 * column++;
                int cy = rowTop + 16;
                if (Math.abs(mouseX - cx) < 8 && Math.abs(mouseY - cy) < 8) {
                    return module;
                }
            }
        }
        return null;
    }

    private int costRowY(ItemStack stack, PowerModule module) {
        return ModularItems.hasModule(stack, module.getName()) ? absY(0.95) - 20 : absY(0.6) + 4;
    }

    private int costRowX(List<ItemStack> cost) {
        return (absX(-0.75) + absX(-0.05)) / 2 - 8 * cost.size();
    }

    /** Install button: lower half when missing; salvage button: upper half when installed. */
    private int[] buttonCentre(ItemStack stack, PowerModule module) {
        int x1 = absX(-0.75);
        int x2 = absX(-0.05);
        int y1 = absY(0.6);
        int y2 = absY(0.95);
        int cx = (x1 + x2) / 2;
        return ModularItems.hasModule(stack, module.getName())
                ? new int[]{cx, y1 + (y2 - y1) / 4} : new int[]{cx, y2 - (y2 - y1) / 4};
    }

    private boolean canInstall(PowerModule module) {
        return minecraft.player.getAbilities().instabuild
                || TinkerActions.hasInInventory(module.getInstallCost(), inventory());
    }

    private void renderInstall(GuiGraphics graphics, ItemStack stack, PowerModule module) {
        frame(graphics, absX(-0.75), absY(0.6), absX(-0.05), absY(0.95));
        List<ItemStack> cost = module.getInstallCost();
        int x = costRowX(cost);
        int y = costRowY(stack, module);
        for (int i = 0; i < cost.size(); i++) {
            graphics.renderItem(cost.get(i), x + 16 * i, y);
            graphics.renderItemDecorations(font, cost.get(i), x + 16 * i, y);
        }
        boolean installed = ModularItems.hasModule(stack, module.getName());
        Component label = Component.translatable(installed ? "gui.craft.mps.salvage" : "gui.craft.mps.install");
        int[] centre = buttonCentre(stack, module);
        int half = font.width(label) / 2 + 3;
        boolean enabled = installed || canInstall(module);
        graphics.fill(centre[0] - half, centre[1] - 6, centre[0] + half, centre[1] + 6,
                enabled ? 0xFF8080FF : 0xFF404040);
        graphics.fill(centre[0] - half + 1, centre[1] - 5, centre[0] + half - 1, centre[1] + 5,
                enabled ? 0xFF000080 : 0xFF202020);
        graphics.drawCenteredString(font, label, centre[0], centre[1] - 4, enabled ? 0xFFFFFF : 0x808080);
    }

    /** {@code ItemInfoFrame} with {@code getLongInfo}: armor, energy storage, weight. */
    private void renderInfo(GuiGraphics graphics, ItemStack stack) {
        int x1 = absX(0.0);
        int y1 = absY(-0.9);
        int x2 = absX(0.9);
        frame(graphics, x1, y1, x2, absY(-0.05));
        List<Component[]> lines = new ArrayList<>();
        lines.add(new Component[]{Component.translatable("gui.craft.mps.summary"), Component.empty()});
        if (stack.getItem() instanceof PowerArmorItem) {
            lines.add(new Component[]{Component.translatable("gui.craft.mps.armor"), Component.literal(
                    MpsFormat.formatShort(PowerArmorItem.getArmorValue(MpsEnergy.getPlayerEnergy(minecraft.player),
                            stack)))});
        }
        lines.add(new Component[]{Component.translatable("gui.craft.mps.energy_storage"),
                Component.literal(MpsFormat.formatShort(MpsEnergy.getMaxJoules(stack)) + "J")});
        lines.add(new Component[]{Component.translatable("gui.craft.mps.weight"),
                Component.literal(MpsFormat.formatShort(ModularItems.computeProperty(stack, MpsModules.WEIGHT))
                        + "g")});
        // ItemInfoFrame.SCALEFACTOR 0.5
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        for (int i = 0; i < lines.size(); i++) {
            int y = (y1 + 4) * 2 + 10 * i;
            graphics.drawString(font, lines.get(i)[0], (x1 + 4) * 2, y, 0xFFFFFF);
            graphics.drawString(font, lines.get(i)[1], (x2 - 4) * 2 - font.width(lines.get(i)[1]), y, 0xFFFFFF);
        }
        graphics.pose().popPose();
    }

    private double sliderValue(ItemStack stack, PowerModule module, String tweak) {
        if (sliderValues.containsKey(tweak)) {
            return sliderValues.get(tweak);
        }
        ModuleData data = ModularItems.getModules(stack).get(module.getName());
        return data == null ? 0.0 : data.tweak(tweak);
    }

    /** Slider {@code i}: label centre y and bar left/right in screen coordinates. */
    private int[] slider(int index) {
        int x1 = absX(0.0) + 4;
        int x2 = absX(0.9) - 4;
        int y = absY(0.0) + 6 + 22 * index;
        return new int[]{x1, x2, y};
    }

    /** {@code ModuleTweakFrame}: one slider per tradeoff, then the module's property values. */
    private void renderTweaks(GuiGraphics graphics, ItemStack stack, PowerModule module) {
        int x1 = absX(0.0);
        int x2 = absX(0.9);
        frame(graphics, x1, absY(0.0), x2, absY(0.9));
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, Component.translatable("gui.craft.mps.tinker"), (x1 + 2) * 2, (absY(0.0) - 4) * 2,
                0xFFFFFF);
        graphics.pose().popPose();
        ModuleData data = ModularItems.getModules(stack).get(module.getName());
        Map<String, Double> tweaks = new HashMap<>(data.tweaks());
        int index = 0;
        for (String tweak : module.getTweaks()) {
            int[] bar = slider(index++);
            double value = sliderValue(stack, module, tweak);
            tweaks.put(tweak, value);
            graphics.drawCenteredString(font, Component.translatable("mps.tweak." + PowerModule.key(tweak)),
                    (bar[0] + bar[1]) / 2, bar[2], 0xFFFFFF);
            graphics.fill(bar[0], bar[2] + 10, bar[1], bar[2] + 18, FRAME_BORDER);
            graphics.fill(bar[0] + 1, bar[2] + 11, bar[1] - 1, bar[2] + 17, FRAME_FILL);
            graphics.fill(bar[0] + 1, bar[2] + 11, bar[0] + 1 + (int) ((bar[1] - bar[0] - 2) * value),
                    bar[2] + 17, SLIDER_FILL);
        }
        ModuleData preview = new ModuleData(data.active(), tweaks);
        int y = absY(0.0) + 6 + 22 * index;
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        for (String property : module.getProperties().keySet()) {
            double value = module.apply(preview, property, 0.0);
            Component name = Component.translatable("mps.property." + PowerModule.key(property)).append(":");
            String text = MpsFormat.formatWithUnit(value, MpsModules.unit(property));
            graphics.drawString(font, name, (x1 + 4) * 2, y * 2, 0xFFFFFF);
            graphics.drawString(font, text, (x2 - 4) * 2 - font.width(text), y * 2, 0xFFFFFF);
            y += 5;
        }
        graphics.pose().popPose();
    }

    private void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY, ItemStack stack) {
        for (int i = 0; i < itemSlots.size(); i++) {
            int[] position = itemPosition(i);
            if (Math.abs(mouseX - position[0]) < 8 && Math.abs(mouseY - position[1]) < 8) {
                graphics.renderTooltip(font, inventory().getItem(itemSlots.get(i)), mouseX, mouseY);
                return;
            }
        }
        if (stack.isEmpty()) {
            return;
        }
        PowerModule hovered = moduleAt(stack, mouseX, mouseY);
        if (hovered != null) {
            List<FormattedCharSequence> lines = new ArrayList<>();
            lines.add(Component.translatable("mps.module." + hovered.getKey()).getVisualOrderText());
            lines.addAll(font.split(FormattedText.of(
                    Component.translatable("mps.module." + hovered.getKey() + ".desc").getString()), 180));
            graphics.renderTooltip(font, lines, mouseX, mouseY);
            return;
        }
        PowerModule module = selectedModule(stack);
        if (module != null) {
            List<ItemStack> cost = module.getInstallCost();
            int x = costRowX(cost);
            int y = costRowY(stack, module);
            if (mouseY >= y && mouseY < y + 16 && mouseX >= x && mouseX < x + 16 * cost.size()) {
                graphics.renderTooltip(font, cost.get((mouseX - x) / 16), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        loadItems();
        for (int i = 0; i < itemSlots.size(); i++) {
            int[] position = itemPosition(i);
            if (Math.abs(mouseX - position[0]) < 8 && Math.abs(mouseY - position[1]) < 8) {
                selectedSlot = itemSlots.get(i);
                sliderValues.clear();
                return true;
            }
        }
        ItemStack stack = selectedStack();
        if (stack.isEmpty()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        PowerModule clicked = moduleAt(stack, mouseX, mouseY);
        if (clicked != null) {
            selectedModule = clicked.getName();
            sliderValues.clear();
            return true;
        }
        PowerModule module = selectedModule(stack);
        if (module == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int[] centre = buttonCentre(stack, module);
        boolean installed = ModularItems.hasModule(stack, module.getName());
        Component label = Component.translatable(installed ? "gui.craft.mps.salvage" : "gui.craft.mps.install");
        if (Math.abs(mouseX - centre[0]) <= font.width(label) / 2 + 3 && Math.abs(mouseY - centre[1]) <= 6) {
            if (installed) {
                PacketDistributor.sendToServer(new MpsPayloads.Salvage(selectedSlot, module.getName()));
            } else if (canInstall(module)) {
                PacketDistributor.sendToServer(new MpsPayloads.Install(selectedSlot, module.getName()));
            }
            return true;
        }
        if (installed && button == 0) {
            int index = 0;
            for (String tweak : module.getTweaks()) {
                int[] bar = slider(index++);
                if (mouseX >= bar[0] && mouseX <= bar[1] && mouseY >= bar[2] + 3 && mouseY <= bar[2] + 18) {
                    draggedTweak = tweak;
                    dragSlider(mouseX, bar);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void dragSlider(double mouseX, int[] bar) {
        double value = (mouseX - bar[0] - 1) / (bar[1] - bar[0] - 2);
        sliderValues.put(draggedTweak, Math.min(1.0, Math.max(0.0, value)));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        ItemStack stack = selectedStack();
        PowerModule module = selectedModule(stack);
        if (draggedTweak != null && module != null) {
            int index = new ArrayList<>(module.getTweaks()).indexOf(draggedTweak);
            if (index >= 0) {
                dragSlider(mouseX, slider(index));
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        ItemStack stack = selectedStack();
        PowerModule module = selectedModule(stack);
        if (draggedTweak != null && module != null && sliderValues.containsKey(draggedTweak)) {
            double value = sliderValues.get(draggedTweak);
            ModularItems.setTweak(stack, module.getName(), draggedTweak, value);
            PacketDistributor.sendToServer(new MpsPayloads.Tweak(selectedSlot, module.getName(), draggedTweak,
                    value));
            sliderValues.remove(draggedTweak);
        }
        if (button == 0) {
            draggedTweak = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
