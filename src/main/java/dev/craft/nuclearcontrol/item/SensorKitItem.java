package dev.craft.nuclearcontrol.item;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import dev.craft.gregtech.machine.GtDeviceInformation;
import dev.craft.nuclearcontrol.NcDataComponents;
import dev.craft.nuclearcontrol.block.CounterBlockEntity;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.EnergySources;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Sensor kit ({@code ItemSensorKitBase} and subclasses, spec 46 §5.2): right click on a fitting target turns the kit
 * in the hand into the sensor location card for that position. Reactor kit: reactor or chamber (position of the
 * reactor); energy kit: EU storage, else MJ receptor (plus {@code targetType}); liquid kit: any block with a tank;
 * counter kit: energy or average counter.
 */
public class SensorKitItem extends Item {
    private final Supplier<? extends Item> card;
    private final TargetFinder finder;

    public SensorKitItem(Properties properties, Supplier<? extends Item> card, TargetFinder finder) {
        super(properties.stacksTo(1));
        this.card = card;
        this.finder = finder;
    }

    @FunctionalInterface
    public interface TargetFinder {
        /** Target for a click on {@code pos}, or {@code null}. */
        @Nullable
        Target find(Level level, BlockPos pos);
    }

    /** {@code targetType} −1: the card gets none. */
    public record Target(BlockPos pos, int targetType) {
    }

    @Nullable
    public static Target reactor(Level level, BlockPos pos) {
        NuclearReactorBlockEntity reactor = ThermometerItem.reactorAt(level, pos);
        return reactor == null ? null : new Target(reactor.getBlockPos(), -1);
    }

    @Nullable
    public static Target energy(Level level, BlockPos pos) {
        EnergySources.Reading storage = EnergySources.at(level, pos, EnergySources.ANY);
        return storage == null ? null : new Target(pos, storage.type());
    }

    @Nullable
    public static Target liquid(Level level, BlockPos pos) {
        return EnergySources.tanks(level, pos) == null ? null : new Target(pos, -1);
    }

    @Nullable
    public static Target counter(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CounterBlockEntity ? new Target(pos, -1) : null;
    }

    /** {@code GT_SensorKit_Item}: a GregTech machine that gives information ({@code isGivingInformation}). */
    @Nullable
    public static Target gregTech(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof GtDeviceInformation ? new Target(pos, -1) : null;
    }

    /** Chat message after mounting; the GregTech kit sends none. */
    protected void announce(Player player) {
        player.sendSystemMessage(Component.translatable("message.craft.nuclearcontrol.sensor_kit"));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, net.minecraft.world.item.context.UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Target target = finder.find(context.getLevel(), context.getClickedPos());
        if (target == null) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            ItemStack result = new ItemStack(card.get());
            result.set(NcDataComponents.SENSOR_TARGET.get(), target.pos());
            if (target.targetType() >= 0) {
                CardData data = CardData.of(result);
                data.setInt("targetType", target.targetType());
                data.save(result);
            }
            player.setItemInHand(context.getHand(), result);
            announce(player);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
