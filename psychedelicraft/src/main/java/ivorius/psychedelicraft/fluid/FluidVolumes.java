/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.fluid.container.FluidVariant;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Created by lukas on 22.10.14.
 */
public interface FluidVolumes {
    int BUCKET = (int)FluidVariant.BUCKET;

    int CAULDRON = BUCKET;
    int GLASS_BOTTLE = (int)FluidVariant.BOTTLE;
    int BOWL = BUCKET / 20;

    int MUG = BUCKET / 2;
    int CUP = BUCKET / 4;
    int CHALLICE = BUCKET / 5;
    int SHOT = BUCKET / 25;
    int BOTTLE = BUCKET * 2;
    int SYRINGE = BUCKET / 100;

    int BARREL = BUCKET * 8;
    int VAT = BUCKET * 9;
    int FLASK = BUCKET * 8;

    int GULP = BUCKET / 4;

    static String format(long amount) {
        if (amount == 0) {
            return "0L";
        }
        if (amount > BUCKET) {
            return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format((double)amount / (double)BUCKET) + "L";
        }
        amount = (long)((amount / (float)FluidVolumes.BUCKET) * 1000F);
        return amount + "ML";
    }
}
