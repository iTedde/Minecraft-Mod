package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class JollyRancherItem extends EdibleItem {

    public JollyRancherItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide) {
            DrugProperties.of(user).ifPresent(properties -> {
                if (properties.cureAll()) {
                    properties.asEntity().displayClientMessage(Component.translatable(getDescriptionId(stack) + ".use"), true);
                }
            });
        }
        return super.finishUsingItem(stack, world, user);
    }
}
