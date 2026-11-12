package dev.craft.nuclearcontrol.item;

import java.util.ArrayList;
import java.util.List;

import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.EnergySources;
import dev.craft.nuclearcontrol.card.PanelLine;
import dev.craft.nuclearcontrol.card.PanelSetting;
import dev.craft.nuclearcontrol.card.PanelText;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Liquid sensor location card ({@code ItemCardMultipleSensorLocation} damage 1, spec 46 §5.3): name, amount, free,
 * capacity and fill percentage of the first tank of the target, in mB.
 */
public class LiquidSensorCardItem extends PanelCardItem {
    public static final int DISPLAY_NAME = 1;
    public static final int DISPLAY_AMOUNT = 2;
    public static final int DISPLAY_FREE = 4;
    public static final int DISPLAY_CAPACITY = 8;
    public static final int DISPLAY_PERCENTAGE = 16;

    public LiquidSensorCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public String cardType() {
        return "liquid";
    }

    @Override
    public CardState update(Level level, BlockPos panel, CardData card, int range) {
        BlockPos target = card.getTarget();
        IFluidHandler tanks = target == null ? null : EnergySources.tanks(level, target);
        if (tanks == null) {
            return CardState.NO_TARGET;
        }
        FluidStack fluid = tanks.getFluidInTank(0);
        int amount = fluid.getAmount();
        card.setInt("capacity", tanks.getTankCapacity(0));
        card.setInt("amount", amount);
        card.setString("liquid", fluid.isEmpty() || amount <= 0 ? ""
                : BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
        return CardState.OK;
    }

    @Override
    public List<PanelLine> lines(int settings, CardData card, boolean showLabels, Level level) {
        List<PanelLine> result = new ArrayList<>();
        int capacity = card.getInt("capacity");
        int amount = card.getInt("amount");
        if (shows(settings, DISPLAY_NAME)) {
            String id = card.getString("liquid");
            Fluid fluid = id.isEmpty() ? Fluids.EMPTY : BuiltInRegistries.FLUID.get(ResourceLocation.parse(id));
            String name = fluid == Fluids.EMPTY ? PanelText.text("none")
                    : fluid.getFluidType().getDescription().getString();
            result.add(PanelLine.left(PanelText.labeled("liquid_name", name, showLabels)));
        }
        if (shows(settings, DISPLAY_AMOUNT)) {
            result.add(PanelLine.left(PanelText.labeled("liquid_amount", amount, showLabels)));
        }
        if (shows(settings, DISPLAY_FREE)) {
            result.add(PanelLine.left(PanelText.labeled("liquid_free", capacity - amount, showLabels)));
        }
        if (shows(settings, DISPLAY_CAPACITY)) {
            result.add(PanelLine.left(PanelText.labeled("liquid_capacity", capacity, showLabels)));
        }
        if (shows(settings, DISPLAY_PERCENTAGE)) {
            result.add(PanelLine.left(PanelText.labeled("liquid_percentage",
                    capacity == 0 ? 100L : amount * 100 / capacity, showLabels)));
        }
        return result;
    }

    @Override
    public List<PanelSetting> settings() {
        return List.of(new PanelSetting("liquid_name", DISPLAY_NAME),
                new PanelSetting("liquid_amount", DISPLAY_AMOUNT), new PanelSetting("liquid_free", DISPLAY_FREE),
                new PanelSetting("liquid_capacity", DISPLAY_CAPACITY),
                new PanelSetting("liquid_percentage", DISPLAY_PERCENTAGE));
    }
}
