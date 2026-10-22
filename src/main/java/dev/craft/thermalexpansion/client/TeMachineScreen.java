package dev.craft.thermalexpansion.client;

import java.util.List;

import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.thermalexpansion.TeRedstoneControl;
import dev.craft.thermalexpansion.machine.AqueousAccumulatorBlockEntity;
import dev.craft.thermalexpansion.machine.SelectionMachineBlockEntity;
import dev.craft.thermalexpansion.machine.TankMachineBlockEntity;
import dev.craft.thermalexpansion.machine.TeMachineBlock;
import dev.craft.thermalexpansion.machine.TeMachineBlockEntity;
import dev.craft.thermalexpansion.machine.TeMachineMenu;
import dev.craft.thermalexpansion.machine.TeMachineType;
import dev.craft.thermalexpansion.machine.TeSides;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GuiFurnace}, {@code GuiPulverizer}, {@code GuiSawmill} ({@code GuiAdv}): gauges, coloured slot overlays for the
 * side groups in use (slot borders on, as in the pack config), the energy, redstone and configuration tabs on the
 * right and the information and tutorial tabs on the left.
 */
public class TeMachineScreen extends TeTabScreen<TeMachineMenu> {
    private static final ResourceLocation COMMON = TeTab.texture("common");
    private static final int SCALE_PROGRESS = 24;

    private final ResourceLocation texture;

    public TeMachineScreen(TeMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        TeMachineType type = menu.getMachineType();
        texture = TeTab.texture(switch (type) {
            case POWERED_FURNACE -> "furnace";
            case PULVERIZER -> "pulverizer";
            case SAWMILL -> "sawmill";
            case INDUCTION_SMELTER -> "smelter";
            case MAGMA_CRUCIBLE -> "crucible";
            case LIQUID_TRANSPOSER -> "transposer";
            case GLACIAL_PRECIPITATOR -> "ice_gen";
            case IGNEOUS_EXTRUDER -> "rock_gen";
            case AQUEOUS_ACCUMULATOR -> "water_gen";
        });
        // GuiRockGen/GuiWaterGen: no energy tab, the tutorial without the MJ line.
        if (type.isPowered()) {
            addTab(new EnergyTab());
        }
        addTab(new RedstoneTab());
        addTab(new ConfigTab());
        addTab(new TextTab(true, 0x555555, 0, "gui.craft.te.information",
                Component.translatable("gui.craft.te.info." + type.getName())));
        MutableComponent tutorial = Component.translatable("gui.craft.te.tutorial.redstone").append("\n\n")
                .append(Component.translatable("gui.craft.te.tutorial.config"));
        if (type.isPowered()) {
            tutorial.append("\n\n").append(Component.translatable("gui.craft.te.tutorial.powered"));
        }
        addTab(new TextTab(true, 0x5A09BB, 1, "gui.craft.te.tutorial", tutorial));
    }

    @Nullable
    private TeMachineBlockEntity machine() {
        return minecraft != null && minecraft.level != null
                && minecraft.level.getBlockEntity(menu.getPos()) instanceof TeMachineBlockEntity machine ? machine : null;
    }

    @Nullable
    @Override
    protected TeRedstoneControl redstoneControl() {
        return machine();
    }

    /** The progress arrows open the machine's recipe category (the transposer's by its mode). */
    @Override
    protected List<RecipeArea> getRecipeAreas() {
        return switch (menu.getMachineType()) {
            case POWERED_FURNACE -> List.of(arrow(79, 34, RecipeCategories.TE_FURNACE));
            case PULVERIZER -> List.of(arrow(79, 34, RecipeCategories.TE_PULVERIZER));
            case SAWMILL -> List.of(arrow(79, 34, RecipeCategories.TE_SAWMILL));
            case INDUCTION_SMELTER -> List.of(arrow(79, 34, RecipeCategories.TE_SMELTER));
            case MAGMA_CRUCIBLE -> List.of(arrow(103, 34, RecipeCategories.TE_CRUCIBLE));
            case LIQUID_TRANSPOSER -> List.of(arrow(112, 19, menu.isReverse()
                    ? RecipeCategories.TE_TRANSPOSER_EXTRACT : RecipeCategories.TE_TRANSPOSER_FILL));
            case GLACIAL_PRECIPITATOR -> List.of(arrow(112, 49, RecipeCategories.TE_GLACIAL_PRECIPITATOR));
            case IGNEOUS_EXTRUDER -> List.of(arrow(40, 49, RecipeCategories.TE_IGNEOUS_EXTRUDER),
                    arrow(112, 49, RecipeCategories.TE_IGNEOUS_EXTRUDER));
            case AQUEOUS_ACCUMULATOR -> List.of();
        };
    }

    private static RecipeArea arrow(int x, int y, ResourceLocation category) {
        return new RecipeArea(x, y, SCALE_PROGRESS, 16, category);
    }

    @Override
    protected void renderGaugeTooltips(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
        TeMachineType type = menu.getMachineType();
        if (type.isPowered() && x >= 10 && x < 22 && y >= 9 && y < 49) {
            graphics.renderTooltip(font, Component.literal((int) menu.getEnergy() + " / "
                    + type.getMaxEnergy() + " MJ"), mouseX, mouseY);
        }
        int capacity = tankCapacity(type);
        if (capacity > 0 && x >= 152 && x < 168 && y >= 9 && y < 69) {
            graphics.renderTooltip(font, Component.literal(menu.getFluidAmount() + " / " + capacity + " mB"),
                    mouseX, mouseY);
        }
        if (type == TeMachineType.IGNEOUS_EXTRUDER && x >= 8 && x < 24 && y >= 9 && y < 69) {
            graphics.renderTooltip(font, Component.literal(menu.getFluid2Amount() + " / " + capacity + " mB"),
                    mouseX, mouseY);
        }
        int selection = selectionAt(x, y);
        if (selection >= 0) {
            graphics.renderTooltip(font, SelectionMachineBlockEntity.products(type).get(selection), mouseX,
                    mouseY);
        }
    }

    /** Tank size of the gauge on the right, 0 without one. */
    private static int tankCapacity(TeMachineType type) {
        return switch (type) {
            case MAGMA_CRUCIBLE, LIQUID_TRANSPOSER -> TankMachineBlockEntity.MAX_LIQUID;
            case GLACIAL_PRECIPITATOR, IGNEOUS_EXTRUDER -> SelectionMachineBlockEntity.CAPACITY;
            case AQUEOUS_ACCUMULATOR -> AqueousAccumulatorBlockEntity.CAPACITY;
            default -> 0;
        };
    }

    /** The product buttons of {@code GuiIceGen}/{@code GuiRockGen} (x 49/79/109, 18 wide), or -1. */
    private int selectionAt(int x, int y) {
        TeMachineType type = menu.getMachineType();
        if (type != TeMachineType.GLACIAL_PRECIPITATOR && type != TeMachineType.IGNEOUS_EXTRUDER
                || y < 18 || y >= 36) {
            return -1;
        }
        for (int i = 0; i < 3; i++) {
            if (x >= 49 + 30 * i && x < 67 + 30 * i) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(texture, x, y, 0, 0, imageWidth, imageHeight);
        TeMachineBlockEntity machine = machine();
        switch (menu.getMachineType()) {
            case MAGMA_CRUCIBLE -> renderCrucible(graphics, machine, x, y);
            case LIQUID_TRANSPOSER -> renderTransposer(graphics, machine, x, y, mouseX - x, mouseY - y);
            case GLACIAL_PRECIPITATOR, IGNEOUS_EXTRUDER -> renderSelection(graphics, machine, x, y);
            case AQUEOUS_ACCUMULATOR -> {
                if (machine != null && machine.hasSide(1)) {
                    coloredLiquidSlot(graphics, x + 152, y + 9, SlotColor.ORANGE);
                }
                drawTank(graphics, menu.getFluid(), menu.getFluidAmount(), x + 152, y);
                graphics.blit(texture, x + 152, y + 9, 176, 98, 16, SCALE_LIQUID);
            }
            default -> {
                int flameX = menu.getMachineType() == TeMachineType.INDUCTION_SMELTER ? 44 : 56;
                drawSpeed(graphics, x + flameX, y + 44);
                int l = scaledProgress();
                if (l > 0) {
                    graphics.blit(texture, x + 79, y + 34, 176, 16, l, 16);
                }
                drawEnergy(graphics, x, y);
                if (machine != null) {
                    drawSlotOverlays(graphics, machine, x, y);
                }
            }
        }
    }

    private int scaledProgress() {
        return (int) (menu.getProgressLevel() * SCALE_PROGRESS);
    }

    private void drawSpeed(GuiGraphics graphics, int x, int y) {
        int l = Math.round(menu.getSpeedLevel() * SCALE_SPEED);
        if (l > 0 && menu.isActive()) {
            graphics.blit(texture, x, y + SCALE_SPEED - l, 176, SCALE_SPEED - l, 16, l);
        }
    }

    private void drawEnergy(GuiGraphics graphics, int x, int y) {
        int l = Math.round(menu.getEnergy() * SCALE_ENERGY / menu.getMachineType().getMaxEnergy());
        if (l > 0) {
            graphics.blit(texture, x + 8, y + 8 + SCALE_ENERGY - l, 176, 32 + SCALE_ENERGY - l, 16, l);
        }
    }

    /** {@code GuiCrucible}: tank gauge, the progress arrow filled with the liquid. */
    private void renderCrucible(GuiGraphics graphics, @Nullable TeMachineBlockEntity machine, int x, int y) {
        if (machine != null && machine.hasSide(1)) {
            coloredSlot(graphics, x + 56, y + 26, SlotColor.BLUE, SlotType.SINGLE, SlotRender.FULL);
        }
        if (machine != null && machine.hasSide(2)) {
            coloredLiquidSlot(graphics, x + 152, y + 9, SlotColor.ORANGE);
        }
        drawTank(graphics, x, y);
        int l = scaledProgress();
        if (l > 0 && menu.getFluidAmount() > 0) {
            drawLiquid(graphics, menu.getFluid(), x + 103, y + 34, l, 16);
            graphics.blit(texture, x + 103, y + 34, 192, 96, l, 16);
        }
        graphics.blit(texture, x + 152, y + 9, 176, 98, 16, SCALE_LIQUID);
        drawSpeed(graphics, x + 56, y + 44);
        drawEnergy(graphics, x, y);
    }

    /** {@code GuiTransposer}: mode button, tank gauge, the progress arrow pointing into or out of the item. */
    private void renderTransposer(GuiGraphics graphics, @Nullable TeMachineBlockEntity machine, int x, int y,
                                  int mouseX, int mouseY) {
        boolean reverse = menu.isReverse();
        boolean overButton = mouseX >= 116 && mouseX < 132 && mouseY >= 49 && mouseY < 65;
        if (reverse) {
            graphics.blit(texture, x + 112, y + 19, 216, 96, SCALE_PROGRESS, 16);
            graphics.blit(texture, x + 116, y + 49, 208, overButton ? 160 : 144, 16, 16);
        } else if (overButton) {
            graphics.blit(texture, x + 116, y + 49, 224, 160, 16, 16);
        }
        if (menu.getReverseFlag() && !reverse) {
            graphics.blit(texture, x + 116, y + 49, 208, 176, 16, 16);
        } else if (!menu.getReverseFlag() && reverse) {
            graphics.blit(texture, x + 116, y + 49, 224, 176, 16, 16);
        }
        if (machine != null && machine.hasSide(1)) {
            coloredSlot(graphics, x + 80, y + 19, SlotColor.BLUE, SlotType.SINGLE, SlotRender.FULL);
            if (!reverse) {
                coloredLiquidSlot(graphics, x + 152, y + 9, SlotColor.BLUE);
            }
        }
        if (machine != null && machine.hasSide(2)) {
            coloredSlot(graphics, x + 76, y + 45, SlotColor.RED, SlotType.OUTPUT, SlotRender.FULL);
        }
        if (machine != null && machine.hasSide(3) && reverse) {
            coloredLiquidSlot(graphics, x + 152, y + 9, SlotColor.YELLOW);
        }
        drawTank(graphics, x, y);
        int l = scaledProgress();
        if (l > 0 && menu.getFluidAmount() > 0) {
            if (!reverse) {
                drawLiquid(graphics, menu.getFluid(), x + 112 + SCALE_PROGRESS - l, y + 19, l, 16);
                graphics.blit(texture, x + 112 + SCALE_PROGRESS - l, y + 19, 192 + SCALE_PROGRESS - l, 112, l, 16);
            } else {
                drawLiquid(graphics, menu.getFluid(), x + 112, y + 19, l, 16);
                graphics.blit(texture, x + 112, y + 19, 192, 96, l, 16);
            }
        }
        graphics.blit(texture, x + 152, y + 9, 176, 98, 16, SCALE_LIQUID);
        drawSpeed(graphics, x + 44, y + 49);
        drawEnergy(graphics, x, y);
    }

    /** The tank gauge at (152, 9), 60 px for 10 000 mB. */
    private void drawTank(GuiGraphics graphics, int x, int y) {
        drawTank(graphics, menu.getFluid(), menu.getFluidAmount(), x + 152, y);
    }

    /** A tank gauge at ({@code x}, 9) for the tank size of the machine type. */
    private void drawTank(GuiGraphics graphics, Fluid fluid, int amount, int x, int y) {
        int capacity = tankCapacity(menu.getMachineType());
        int l = amount >= capacity ? SCALE_LIQUID : amount * SCALE_LIQUID / capacity;
        if (l > 0) {
            drawLiquid(graphics, fluid, x, y + 9 + SCALE_LIQUID - l, 16, l);
        }
    }

    /**
     * {@code GuiIceGen}/{@code GuiRockGen}: the three products with the chosen one framed (highlighted while it runs)
     * and the running one marked, the tanks, the progress arrow(s) filled with the liquid(s).
     */
    private void renderSelection(GuiGraphics graphics, @Nullable TeMachineBlockEntity machine, int x, int y) {
        boolean extruder = menu.getMachineType() == TeMachineType.IGNEOUS_EXTRUDER;
        int cur = menu.getCurSelection();
        int prev = menu.getPrevSelection();
        graphics.blit(texture, x + 42 + 30 * cur, y + 11, 192, prev == cur && menu.isActive() ? 64 : 32, 32, 32);
        graphics.blit(texture, x + 42 + 30 * prev, y + 11, 224, 32, 32, 32);
        if (machine != null && machine.hasSide(1)) {
            if (extruder) {
                coloredLiquidSlot(graphics, x + 8, y + 9, SlotColor.BLUE);
            }
            coloredLiquidSlot(graphics, x + 152, y + 9, SlotColor.BLUE);
        }
        if (machine != null && machine.hasSide(2)) {
            coloredSlot(graphics, x + 76, y + 45, SlotColor.ORANGE, SlotType.OUTPUT, SlotRender.FULL);
        }
        if (extruder) {
            drawTank(graphics, Fluids.LAVA, menu.getFluid2Amount(), x + 8, y);
        }
        drawTank(graphics, Fluids.WATER, menu.getFluidAmount(), x + 152, y);
        int l = scaledProgress();
        if (l > 0) {
            if (extruder) {
                drawLiquid(graphics, Fluids.LAVA, x + 40, y + 49, l, 16);
                graphics.blit(texture, x + 40, y + 49, 192, 96, l, 16);
            }
            drawLiquid(graphics, Fluids.WATER, x + 112 + SCALE_PROGRESS - l, y + 49, l, 16);
            graphics.blit(texture, x + 112 + SCALE_PROGRESS - l, y + 49, 192 + SCALE_PROGRESS - l, 112, l, 16);
        }
        if (extruder) {
            graphics.blit(texture, x + 8, y + 9, 176, 98, 16, SCALE_LIQUID);
        }
        graphics.blit(texture, x + 152, y + 9, 176, 98, 16, SCALE_LIQUID);
        if (!extruder) {
            drawSpeed(graphics, x + 44, y + 49);
            drawEnergy(graphics, x, y);
        }
        // ContainerIceGen/ContainerRockGen: the products sit in locked slots at (50|80|110, 19).
        List<ItemStack> products = SelectionMachineBlockEntity.products(menu.getMachineType());
        for (int i = 0; i < 3; i++) {
            graphics.renderItem(products.get(i), x + 50 + 30 * i, y + 19);
            graphics.renderItemDecorations(font, products.get(i), x + 50 + 30 * i, y + 19);
        }
    }

    /** {@code drawColoredLiquidSlotWithBorder}. */
    private static void coloredLiquidSlot(GuiGraphics graphics, int x, int y, SlotColor color) {
        graphics.blit(COMMON, x - 8, y - 2, color.ordinal() * 32, 96, 32, 64);
    }

    private void drawSlotOverlays(GuiGraphics graphics, TeMachineBlockEntity machine, int x, int y) {
        if (menu.getMachineType() == TeMachineType.INDUCTION_SMELTER) {
            inputOverlay(graphics, machine, x + 32, y + 26, 5, SlotColor.GREEN);
            inputOverlay(graphics, machine, x + 56, y + 26, 6, SlotColor.PURPLE);
        } else if (machine.hasSide(1)) {
            coloredSlot(graphics, x + 56, y + 26, SlotColor.BLUE, SlotType.SINGLE, SlotRender.FULL);
        }
        if (menu.getMachineType() == TeMachineType.POWERED_FURNACE) {
            if (machine.hasSide(2)) {
                coloredSlot(graphics, x + 112, y + 31, SlotColor.ORANGE, SlotType.OUTPUT, SlotRender.FULL);
            }
            return;
        }
        if (machine.hasSide(2)) {
            coloredSlot(graphics, x + 112, y + 22, SlotColor.RED, SlotType.DOUBLE_OUTPUT, SlotRender.FULL);
            if (machine.hasSide(4)) {
                coloredSlot(graphics, x + 112, y + 22, SlotColor.ORANGE, SlotType.DOUBLE_OUTPUT, SlotRender.TOP);
            }
        } else if (machine.hasSide(4)) {
            coloredSlot(graphics, x + 112, y + 22, SlotColor.ORANGE, SlotType.DOUBLE_OUTPUT, SlotRender.FULL);
        }
        if (machine.hasSide(3)) {
            coloredSlot(graphics, x + 116, y + 53, SlotColor.YELLOW, SlotType.SINGLE, SlotRender.FULL);
            if (machine.hasSide(4)) {
                coloredSlot(graphics, x + 116, y + 53, SlotColor.ORANGE, SlotType.SINGLE, SlotRender.TOP);
            }
        } else if (machine.hasSide(4)) {
            coloredSlot(graphics, x + 116, y + 53, SlotColor.ORANGE, SlotType.SINGLE, SlotRender.FULL);
        }
    }

    /** Smelter input slot: its own group colour, the upper half blue if group 1 (both inputs) is in use too. */
    private static void inputOverlay(GuiGraphics graphics, TeMachineBlockEntity machine, int x, int y, int group,
                                     SlotColor color) {
        if (machine.hasSide(group)) {
            coloredSlot(graphics, x, y, color, SlotType.SINGLE, SlotRender.FULL);
            if (machine.hasSide(1)) {
                coloredSlot(graphics, x, y, SlotColor.BLUE, SlotType.SINGLE, SlotRender.TOP);
            }
        } else if (machine.hasSide(1)) {
            coloredSlot(graphics, x, y, SlotColor.BLUE, SlotType.SINGLE, SlotRender.FULL);
        }
    }

    /** {@code drawColoredSlotWithBorder}. */
    private static void coloredSlot(GuiGraphics graphics, int x, int y, SlotColor color, SlotType type,
                                    SlotRender render) {
        int sizeX = 32;
        int sizeY = 32;
        int offsetX = color.ordinal() / 3 * 128 + type.ordinal() * 32;
        int offsetY = color.ordinal() % 3 * 32;
        switch (type) {
            case SINGLE -> {
                x -= 8;
                y -= 8;
            }
            case OUTPUT -> {
                x -= 4;
                y -= 4;
            }
            case DOUBLE_OUTPUT -> {
                sizeX = 64;
                x -= 11;
                y -= 4;
            }
        }
        if (render == SlotRender.TOP) {
            sizeY /= 2;
        }
        graphics.blit(COMMON, x, y, offsetX, offsetY, sizeX, sizeY);
    }

    @Override
    protected boolean clickBackground(int x, int y, int button) {
        if (menu.getMachineType() == TeMachineType.LIQUID_TRANSPOSER && x >= 116 && x < 132 && y >= 49 && y < 65) {
            // GuiTransposer: only while no mode change is pending.
            if (menu.isReverse() == menu.getReverseFlag()) {
                pressButton(TeMachineMenu.BUTTON_MODE, menu.isReverse() ? 0.8F : 0.6F);
            }
            return true;
        }
        int selection = selectionAt(x, y);
        if (selection >= 0) {
            // GuiIceGen.mouseClicked: no click sound.
            sendButton(TeMachineMenu.BUTTON_SELECTION + selection);
            return true;
        }
        return false;
    }

    private enum SlotColor { BLUE, RED, YELLOW, ORANGE, GREEN, PURPLE }

    private enum SlotType { SINGLE, OUTPUT, DOUBLE_OUTPUT }

    private enum SlotRender { TOP, FULL }

    /** {@code TabMachineEnergy}. */
    private final class EnergyTab extends TeTab {
        EnergyTab() {
            super(false, 0x0A77D0);
            maxHeight = 92;
            maxWidth = 100;
        }

        @Override
        public void draw(GuiGraphics graphics, Font font, int x, int y) {
            drawBackground(graphics, x, y);
            drawIcon(graphics, 3, x + 2, y + 2);
            if (isFullyOpened()) {
                graphics.drawString(font, Component.translatable("gui.craft.te.energy"), x + 22, y + 6, HEADER_COLOR);
                graphics.drawString(font, Component.translatable("gui.craft.te.power_usage").append(":"), x + 8,
                        y + 18, SUBHEADER_COLOR);
                graphics.drawString(font, format(menu.getPower()) + " MJ/t", x + 16, y + 30, TEXT_COLOR, false);
                graphics.drawString(font, Component.translatable("gui.craft.te.max_power").append(":"), x + 8,
                        y + 42, SUBHEADER_COLOR);
                graphics.drawString(font, format(menu.getMachineType().getMaxPower()) + " MJ/t", x + 16, y + 54,
                        TEXT_COLOR, false);
                graphics.drawString(font, Component.translatable("gui.craft.te.energy_stored").append(":"), x + 8,
                        y + 66, SUBHEADER_COLOR);
                graphics.drawString(font, format(menu.getEnergy()) + " MJ", x + 16, y + 78, TEXT_COLOR, false);
            }
        }

        @Override
        public Component getTooltip() {
            return isFullyOpened() ? null : Component.literal(format(menu.getPower()) + " MJ/t");
        }
    }

    /** {@code TabMachineConfig}: the six faces around the front; click cycles, right click back, shift clears. */
    private final class ConfigTab extends TeTab {
        ConfigTab() {
            super(false, 0x089E4C);
            maxHeight = 92;
            maxWidth = 100;
        }

        /** Face shown at grid cell {@code (column, row)}: top, left/front/right, bottom/back. */
        @Nullable
        private Direction faceAt(int column, int row, Direction facing) {
            int f = facing.get3DDataValue();
            return switch (row * 3 + column) {
                case 1 -> Direction.UP;
                case 3 -> Direction.from3DDataValue(TeSides.LEFT[f]);
                case 4 -> facing;
                case 5 -> Direction.from3DDataValue(TeSides.RIGHT[f]);
                case 7 -> Direction.DOWN;
                case 8 -> Direction.from3DDataValue(TeSides.OPPOSITE[f]);
                default -> null;
            };
        }

        @Override
        public void draw(GuiGraphics graphics, Font font, int x, int y) {
            drawBackground(graphics, x, y);
            if (isFullyOpened()) {
                drawPanel(graphics, x + 16, y + 20, 64, 64);
            }
            drawIcon(graphics, 4, x + 2, y + 2);
            TeMachineBlockEntity machine = machine();
            if (!isFullyOpened() || machine == null) {
                return;
            }
            graphics.drawString(font, Component.translatable("gui.craft.te.configuration"), x + 20, y + 6,
                    HEADER_COLOR);
            Direction facing = machine.getFacing();
            boolean active = machine.getBlockState().getValue(TeMachineBlock.ACTIVE);
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    Direction face = faceAt(column, row, facing);
                    if (face == null) {
                        continue;
                    }
                    ResourceLocation location = TeMachineModel.texture(menu.getMachineType(), face,
                            machine.getSide(face), facing, active);
                    TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                            .apply(location);
                    graphics.blit(x + 20 + column * 20, y + 24 + row * 20, 0, 16, 16, sprite);
                }
            }
        }

        @Override
        public Component getTooltip() {
            return isFullyOpened() ? null : Component.translatable("gui.craft.te.configuration");
        }

        @Override
        public boolean mouseClicked(int x, int y, int button) {
            TeMachineBlockEntity machine = machine();
            if (!isFullyOpened() || machine == null) {
                return false;
            }
            if (x < 16 || x > 80 || y < 20 || y > 84) {
                return false;
            }
            int column = (x - 20) / 20;
            int row = (y - 24) / 20;
            if (x < 20 || y < 24 || (x - 20) % 20 >= 16 || (y - 24) % 20 >= 16 || column > 2 || row > 2) {
                return true;
            }
            Direction face = faceAt(column, row, machine.getFacing());
            if (face == null) {
                return true;
            }
            int side = face.get3DDataValue();
            if (Screen.hasShiftDown()) {
                if (face == machine.getFacing()) {
                    pressButton(TeMachineMenu.BUTTON_RESET_SIDES, 0.2F);
                } else if (machine.getSide(face) != 0) {
                    pressButton(TeMachineMenu.BUTTON_CLEAR_SIDE + side, 0.4F);
                }
            } else if (face != machine.getFacing()) {
                if (button == 0) {
                    pressButton(TeMachineMenu.BUTTON_SIDE + 2 * side, 0.8F);
                } else if (button == 1) {
                    pressButton(TeMachineMenu.BUTTON_SIDE + 2 * side + 1, 0.6F);
                }
            }
            return true;
        }
    }
}
