/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/**
 * A fluid that is possible to be consumed.
 */
public interface ConsumableFluid {
    /**
     * Indicates if the entity can inject this fluid.
     *
     * @param fluidStack The fluid stack.
     * @param entity     The entity about to inject the fluid.
     * @return True if the entity can inject this fluid, at this point in time.
     */
    boolean canConsume(ItemStack stack, LivingEntity entity, ConsumptionType type);
    /**
     * Called when the entity has injected the fluid.
     *
     * @param fluidStack The fluid stack.
     * @param entity     The entity injecting the fluid.
     */
    void consume(ItemFluids stack, LivingEntity entity, ConsumptionType type);

    static boolean canConsume(ItemStack stack, LivingEntity entity, int maxConsumed, ConsumptionType type) {
        ItemFluids fluids = ItemFluids.of(stack);
        return fluids.amount() > 0
                && fluids.fluid() instanceof ConsumableFluid consumable
                && consumable.canConsume(stack, entity, type);
    }

    static ItemStack consume(ItemStack stack, LivingEntity entity, int maxConsumed, boolean consumeItem, ConsumptionType type) {
        if (canConsume(stack, entity, maxConsumed, type)) {
            if (ItemFluids.of(stack).fluid() instanceof ConsumableFluid consumable) {
                ItemFluids.Transaction transaction = ItemFluids.Transaction.begin(stack);
                ItemFluids withdrawn = transaction.withdraw(maxConsumed);
                if (!withdrawn.isEmpty()) {
                    consumable.consume(withdrawn, entity, type);
                    if (type == ConsumptionType.DRINK && !entity.level().isClientSide) {
                        relieveHangover(withdrawn.fluid(), entity);
                    }
                    if (entity instanceof ServerPlayer player) {
                        CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
                    }
                }
                return consumeItem ? transaction.toItemStack() : stack;
            }
        }

        return stack;
    }

    /**
     * Hangover relief (spec 52 §6): coffee and water drunk from a Psychedelicraft vessel.
     */
    private static void relieveHangover(SimpleFluid fluid, LivingEntity entity) {
        if (fluid == PSFluids.COFFEE) {
            DrugProperties.of(entity).ifPresent(p -> p.getHangover().relieve(true));
        } else if (VanillaFluid.isWater(fluid)) {
            DrugProperties.of(entity).ifPresent(p -> p.getHangover().relieve(false));
        }
    }

    public enum ConsumptionType {
        DRINK(UseAnim.DRINK),
        INJECT(UseAnim.BOW);

        private final UseAnim action;

        ConsumptionType(UseAnim action) {
            this.action = action;
        }

        public UseAnim getUseAction() {
            return action;
        }
    }
}
