package ivorius.psychedelicraft.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;

public class VomitItem extends Item implements TickableItem {

    public VomitItem(Properties settings) {
        super(settings);
    }

    @Override
    public void onGroundTick(ItemEntity entity) {
        if (entity.isInWaterOrRain() && entity.tickCount > 10) {
            entity.discard();
        }
    }
}
