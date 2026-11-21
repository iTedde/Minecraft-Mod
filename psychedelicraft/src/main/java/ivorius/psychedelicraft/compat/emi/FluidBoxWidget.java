package ivorius.psychedelicraft.compat.emi;

import java.util.ArrayList;
import java.util.List;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.client.screen.AbstractFluidContraptionScreen;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

/**
 * Tank drawn like in the machine screens, cycling through the possible fluids every 3 s
 * (upstream: {@code compat/tia/FluidBoxWidget}). Slots placed on top are excluded from its tooltip.
 */
final class FluidBoxWidget extends Widget {
    private final List<ItemFluids> fluids;
    private final Bounds bounds;
    private final int capacity;
    private final List<Bounds> exclusionZones = new ArrayList<>();

    static FluidBoxWidget create(ItemFluids fluids, int capacity, int x, int y, int width, int height, WidgetHolder widgets) {
        return create(List.of(fluids), capacity, x, y, width, height, widgets);
    }

    static FluidBoxWidget create(List<ItemFluids> fluids, int capacity, int x, int y, int width, int height, WidgetHolder widgets) {
        return widgets.add(new FluidBoxWidget(fluids, capacity, new Bounds(x, y, width, height)));
    }

    private FluidBoxWidget(List<ItemFluids> fluids, int capacity, Bounds bounds) {
        this.fluids = fluids.isEmpty() ? List.of(ItemFluids.EMPTY) : fluids;
        this.capacity = capacity;
        this.bounds = bounds;
    }

    void addExclusion(Widget widget) {
        exclusionZones.add(widget.getBounds());
    }

    private ItemFluids current() {
        return fluids.get((int) ((System.currentTimeMillis() / 3000) % fluids.size())).ofAmount(capacity);
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        AbstractFluidContraptionScreen.drawTank(context, current(), capacity, bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        for (Bounds exclusion : exclusionZones) {
            if (exclusion.contains(mouseX, mouseY)) {
                return List.of();
            }
        }
        return AbstractFluidContraptionScreen.getTankTooltip(current(), 0, List.of()).stream()
                .map(line -> ClientTooltipComponent.create(line.getVisualOrderText()))
                .toList();
    }
}
