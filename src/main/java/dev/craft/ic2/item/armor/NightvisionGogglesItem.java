package dev.craft.ic2.item.armor;

import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

/** IC2 {@code ItemArmorNightvisionGoggles}: 20 000 EU, transfer 200, tier 1, 1 EU per tick. */
public class NightvisionGogglesItem extends Ic2ArmorItem implements ElectricItem {
    public NightvisionGogglesItem(Properties properties) {
        super(Ic2ArmorMaterials.NIGHTVISION, Type.HELMET, properties.stacksTo(1));
    }

    @Override
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return;
        }
        ElectricItems.chargeFromArmor(stack, player);
        if (ElectricItems.discharge(stack, 1, 1, true, false) >= 1) {
            if (level.isDay()) {
                int skylight = level.getBrightness(LightLayer.SKY, BlockPos.containing(player.position()));
                if (skylight > 12) {
                    player.removeEffect(MobEffects.NIGHT_VISION);
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100 + (skylight - 13) * 50, 0,
                            true, true));
                }
            } else {
                player.removeEffect(MobEffects.BLINDNESS);
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 250, 0, true, true));
            }
        }
    }

    @Override
    public int getMaxCharge() {
        return 20000;
    }

    @Override
    public int getTransferLimit() {
        return 200;
    }

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public boolean canProvideEnergy() {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ElectricItems.barWidth(this, stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ElectricItems.barColor(this, stack);
    }
}
