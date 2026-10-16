package dev.craft.gregtech.item;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.reactor.GradualItem;
import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * {@code GT_RadioactiveCell_Item}: thorium cells (25 000 cycles, pulse rate −5: only every 5th pulse gives output and
 * every 5th iteration heats/pulses the neighbours) and plutonium cells (20 000 cycles, pulse rate 2: two outputs per
 * pulse, neighbours pulsed twice; leave near-depleted uranium cells). Only a single item in a field works.
 */
public class GtRadioactiveCellItem extends GradualItem implements ReactorComponent {
    private static final Random RANDOMIZER = new Random();

    private final int maxDelay;
    private final int cellCount;
    private final int pulseRate;
    private final boolean leavesDepleted;

    public GtRadioactiveCellItem(Properties properties, int maxDelay, int cellCount, int pulseRate,
                                 boolean leavesDepleted) {
        super(properties);
        this.maxDelay = maxDelay;
        this.cellCount = Math.max(1, cellCount);
        this.pulseRate = pulseRate;
        this.leavesDepleted = leavesDepleted;
    }

    private static int increment(ItemStack stack, net.minecraft.core.component.DataComponentType<Integer> type) {
        int value = stack.getOrDefault(type, 0) + 1;
        stack.set(type, value);
        return value;
    }

    private boolean outputPulseForStack(ItemStack stack) {
        int output = increment(stack, Ic2DataComponents.OUTPUT_COUNT.get());
        return pulseRate > 0 || output % -pulseRate == 0;
    }

    private boolean incrementPulseForStack(ItemStack stack) {
        int pulse = increment(stack, Ic2DataComponents.PULSE_COUNT.get());
        return pulseRate > 0 || pulse % -pulseRate == 0;
    }

    public int getDurabilityOfStack(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.NUCLEAR_DURABILITY.get(), 0);
    }

    private void setDurabilityForStack(ItemStack stack, int durability) {
        stack.set(Ic2DataComponents.NUCLEAR_DURABILITY.get(), durability);
        if (maxDelay > 0) {
            double left = ((double) maxDelay - durability) / maxDelay;
            int value = (int) (stack.getMaxDamage() * left);
            if (value >= stack.getMaxDamage()) {
                value = stack.getMaxDamage() - 1;
            }
            stack.setDamageValue(stack.getMaxDamage() - value);
        }
    }

    public int getMaxNuclearDurability() {
        return maxDelay;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.craft.time_left", maxDelay - getDurabilityOfStack(stack))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean acceptUraniumPulse(Reactor reactor, ItemStack stack, ItemStack pulsingStack, int youX, int youY,
                                      int pulseX, int pulseY) {
        if (stack.getCount() > 1) {
            return false;
        }
        if (pulseRate > 0) {
            for (int i = 0; i < pulseRate; i++) {
                reactor.addOutput(1);
            }
        }
        if (pulseRate < 0 && incrementPulseForStack(stack)) {
            reactor.addOutput(1);
        }
        return true;
    }

    @Override
    public boolean canStoreHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return false;
    }

    @Override
    public int getMaxHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return 0;
    }

    @Override
    public int getCurrentHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return 0;
    }

    @Override
    public float influenceExplosion(Reactor reactor, ItemStack stack) {
        return stack.getCount() * cellCount * (pulseRate > 0 ? pulseRate : 1);
    }

    @Override
    public int alterHeat(Reactor reactor, ItemStack stack, int x, int y, int heat) {
        return heat;
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
        if (stack.getCount() > 1 || !reactor.produceEnergy()) {
            return;
        }
        for (int j = 0; j < cellCount; j++) {
            int pulsables = 1 + cellCount / 2;
            for (int i = 0; i < pulsables; i++) {
                acceptUraniumPulse(reactor, stack, stack, x, y, x, y);
            }
            if (pulseRate != 0 && outputPulseForStack(stack)) {
                for (int i = 0; i < pulseRate || i == 0; i++) {
                    pulsables += checkPulseable(reactor, x - 1, y, stack, x, y)
                            + checkPulseable(reactor, x + 1, y, stack, x, y)
                            + checkPulseable(reactor, x, y - 1, stack, x, y)
                            + checkPulseable(reactor, x, y + 1, stack, x, y);
                    int addedHeat = sumUp(pulsables) * 4;
                    List<StackCoord> acceptors = new ArrayList<>();
                    checkHeatAcceptor(reactor, x - 1, y, acceptors);
                    checkHeatAcceptor(reactor, x + 1, y, acceptors);
                    checkHeatAcceptor(reactor, x, y - 1, acceptors);
                    checkHeatAcceptor(reactor, x, y + 1, acceptors);
                    while (!acceptors.isEmpty() && addedHeat > 0) {
                        int perStack = addedHeat / acceptors.size();
                        addedHeat -= perStack;
                        StackCoord acceptor = acceptors.remove(0);
                        addedHeat += ((ReactorComponent) acceptor.stack().getItem()).alterHeat(reactor,
                                acceptor.stack(), acceptor.x(), acceptor.y(), perStack);
                    }
                    if (addedHeat > 0) {
                        reactor.addHeat(addedHeat);
                    }
                }
            }
        }
        if (getDurabilityOfStack(stack) >= maxDelay - 1) {
            reactor.setItemAt(x, y, ItemStack.EMPTY);
            if (leavesDepleted) {
                for (int i = 0; i < cellCount; i++) {
                    if (RANDOMIZER.nextInt(4) == 0) {
                        ItemStack current = reactor.getItemAt(x, y);
                        if (current.isEmpty()) {
                            reactor.setItemAt(x, y, new ItemStack(Ic2Items.NEAR_DEPLETED_URANIUM_CELL.get(), 4));
                        } else {
                            current.grow(4);
                        }
                    }
                }
            }
        } else {
            setDurabilityForStack(stack, getDurabilityOfStack(stack) + 1);
        }
    }

    private static int checkPulseable(Reactor reactor, int x, int y, ItemStack me, int meX, int meY) {
        ItemStack other = reactor.getItemAt(x, y);
        return !other.isEmpty() && other.getItem() instanceof ReactorComponent component
                && component.acceptUraniumPulse(reactor, other, me, x, y, meX, meY) ? 1 : 0;
    }

    private static int sumUp(int a) {
        int b = 0;
        for (int c = 1; c <= a; c++) {
            b += c;
        }
        return b;
    }

    private static void checkHeatAcceptor(Reactor reactor, int x, int y, List<StackCoord> acceptors) {
        ItemStack stack = reactor.getItemAt(x, y);
        if (!stack.isEmpty() && stack.getItem() instanceof ReactorComponent component
                && component.canStoreHeat(reactor, stack, x, y)) {
            acceptors.add(new StackCoord(stack, x, y));
        }
    }

    private record StackCoord(ItemStack stack, int x, int y) {
    }
}
