package ivorius.psychedelicraft.item;

import net.minecraft.world.entity.item.ItemEntity;

public interface TickableItem {
    void onGroundTick(ItemEntity entity);
}
