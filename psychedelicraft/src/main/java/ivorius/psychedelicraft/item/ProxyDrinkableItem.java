/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Created by Sollace on Jan 1 2023
 */
public class ProxyDrinkableItem extends DrinkableItem {

    private final Item basis;

    public ProxyDrinkableItem(Item basis, Properties settings, int capacity, ConsumableFluid.ConsumptionType consumptionType) {
        super(settings.craftRemainder(basis), capacity, DEFAULT_MAX_USE_TIME, consumptionType);
        this.basis = basis;
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemFluids fluids = ItemFluids.of(stack);

        if (!fluids.isEmpty()) {
            return Component.translatable("%s %s", fluids.fluid().getName(fluids), basis.getName(stack));
        }

        return basis.getName(stack);
    }
}
