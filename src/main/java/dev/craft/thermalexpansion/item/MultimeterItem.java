package dev.craft.thermalexpansion.item;

import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.thermalexpansion.transport.EnergyConduitBlockEntity;
import dev.craft.thermalexpansion.transport.GridPowerProvider;
import dev.craft.thermalexpansion.transport.LiquidGrid;
import dev.craft.thermalexpansion.transport.LiquiductBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * {@code ItemMultimeter}: on a liquiduct liquid, amount/capacity and throughput of the network; on an energy conduit the
 * network saturation, or (output node) a 20-tick average of what it hands out (first click starts, second shows); on
 * any other MJ receiver the energy it asks for.
 */
public class MultimeterItem extends Item {
    public MultimeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) {
            return InteractionResult.PASS;
        }
        BlockEntity tile = level.getBlockEntity(context.getClickedPos());
        if (tile instanceof EnergyConduitBlockEntity conduit) {
            if (!conduit.isNode() || !conduit.canTransfer()) {
                PowerProvider provider = conduit.getPowerProvider();
                float saturation = provider.getMaxEnergyStored() > 0
                        ? 100.0F * provider.getEnergyStored() / provider.getMaxEnergyStored() : 0.0F;
                player.sendSystemMessage(Component.translatable("chat.craft.multimeter.saturation",
                        String.format("%.1f", saturation)));
            } else {
                float[] average = conduit.toggleMonitoring();
                player.sendSystemMessage(average == null
                        ? Component.translatable("chat.craft.multimeter.monitoring")
                        : Component.translatable("chat.craft.multimeter.average", (int) average[0], average[1]));
            }
            return InteractionResult.SUCCESS;
        }
        if (tile instanceof LiquiductBlockEntity liquiduct) {
            LiquidGrid grid = liquiduct.getGrid();
            if (grid == null || grid.getLiquid().isEmpty()) {
                player.sendSystemMessage(Component.translatable("chat.craft.multimeter.liquid_empty"));
            } else {
                player.sendSystemMessage(Component.translatable("chat.craft.multimeter.liquid",
                        grid.getLiquid().getHoverName()));
                player.sendSystemMessage(Component.translatable("chat.craft.multimeter.amount",
                        grid.getLiquidAmount(), grid.getCapacity()));
                player.sendSystemMessage(Component.translatable("chat.craft.multimeter.throughput",
                        grid.getLiquidThroughput()));
            }
            return InteractionResult.SUCCESS;
        }
        if (tile != null) {
            PowerReceptor receptor = level.getCapability(BcCapabilities.POWER_RECEPTOR, context.getClickedPos(),
                    context.getClickedFace());
            PowerProvider provider = receptor != null ? receptor.getPowerProvider() : null;
            if (provider != null && !(provider instanceof GridPowerProvider)) {
                float requested = Math.min(receptor.powerRequest(),
                        provider.getMaxEnergyStored() - provider.getEnergyStored());
                player.sendSystemMessage(Component.translatable("chat.craft.multimeter.requested", requested));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
