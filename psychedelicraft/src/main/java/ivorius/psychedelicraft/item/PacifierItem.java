package ivorius.psychedelicraft.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PacifierItem extends Item implements Equipable {
    public PacifierItem(Properties settings) {
        super(settings);

    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.OFFHAND;
    }

    public static boolean consumePacifier(LivingEntity entity) {
        return consumePacifier(entity, InteractionHand.MAIN_HAND) || consumePacifier(entity, InteractionHand.OFF_HAND);
    }

    private static boolean consumePacifier(LivingEntity entity, InteractionHand hand) {
        ItemStack pacifier = entity.getItemInHand(hand);
        if (pacifier.is(PSItems.PACIFIER)) {
            pacifier.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(hand));
            return true;
        }
        return false;
    }
}
