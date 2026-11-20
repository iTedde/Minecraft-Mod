package ivorius.psychedelicraft.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Created by lukas on 14.11.14.
 */
public class SnortableItem extends EdibleItem {
    public SnortableItem(Properties settings) {
        super(settings);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.random.nextInt(3) == 0) {
            user.playSound(SoundEvents.GENERIC_EAT, 0.1F, 1.6F);
        }
    }
}
