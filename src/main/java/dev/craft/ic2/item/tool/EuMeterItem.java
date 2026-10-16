package dev.craft.ic2.item.tool;

import java.text.DecimalFormat;

import dev.craft.core.energy.EnergyConductor;
import dev.craft.core.energy.EnergyNet;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2DataComponents.Measurement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * EU meter (IC2 {@code ItemToolMeter}): two clicks on the same energy tile report the average EU/t that went in and
 * out between them.
 */
public class EuMeterItem extends Item {
    public EuMeterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof EnergyTile tile)
                || !(tile instanceof EnergySource || tile instanceof EnergyConductor || tile instanceof EnergySink)) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        EnergyNet net = EnergyNets.get(level);
        long emitted = net.getTotalEnergyEmitted(tile);
        long sunken = net.getTotalEnergySunken(tile);
        long time = level.getGameTime();
        BlockPos pos = context.getClickedPos();
        Measurement last = stack.get(Ic2DataComponents.EU_MEASUREMENT.get());
        if (last != null && last.pos().equals(pos)) {
            long period = Math.max(1L, time - last.time());
            double in = (double) (sunken - last.sunken()) / period;
            double out = (double) (emitted - last.emitted()) / period;
            DecimalFormat format = new DecimalFormat("0.##");
            message(player, Component.translatable("message.craft.eu_meter.result", format.format(in),
                    format.format(out), format.format(in - out), period));
        } else {
            message(player, Component.translatable("message.craft.eu_meter.start"));
        }
        stack.set(Ic2DataComponents.EU_MEASUREMENT.get(), new Measurement(pos, emitted, sunken, time));
        return InteractionResult.SUCCESS;
    }

    private static void message(Player player, Component text) {
        if (player != null) {
            player.displayClientMessage(text, false);
        }
    }
}
