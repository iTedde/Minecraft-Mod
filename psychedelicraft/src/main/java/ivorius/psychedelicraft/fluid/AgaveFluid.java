package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Created by lukas on 25.11.14.
 */
public class AgaveFluid extends AlcoholicFluid {
    public AgaveFluid(ResourceLocation id, ivorius.psychedelicraft.fluid.AlcoholicFluid.Settings settings) {
        super(id, settings);
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return container.is(PSItems.SHOT_GLASS);
    }
    @Override
    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.SUITABLE_SHOT_GLASS_RECEPTICALTS;
    }
}
