package dev.craft.powerplants.windturbine;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Rotor of the large wind turbine (spec 23 §4); its factor scales the turbine's output. Never wears out. */
public class WindRotorItem extends Item {
    private final double factor;
    private final String material;

    /** @param material name of the blade texture ({@code textures/entity/wind_rotor/<material>.png}) */
    public WindRotorItem(double factor, String material) {
        super(new Properties().stacksTo(1));
        this.factor = factor;
        this.material = material;
    }

    public double getFactor() {
        return factor;
    }

    public String getMaterial() {
        return material;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.craft.wind_rotor.factor", (int) Math.round(factor * 100))
                .withStyle(ChatFormatting.GRAY));
    }
}
