package dev.craft.ic2.item.armor;

import dev.craft.core.CraftDamageTypes;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/** IC2 {@code ItemArmorHazmat}: hazmat suit and rubber boots, 64 durability. */
public class HazmatArmorItem extends Ic2ArmorItem {
    public HazmatArmorItem(Type type, Properties properties) {
        super(Ic2ArmorMaterials.HAZMAT, type, properties.durability(64));
    }

    public static boolean hasCompleteHazmat(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        for (EquipmentSlot slot : ArmorProperties.SLOTS) {
            if (!(player.getItemBySlot(slot).getItem() instanceof HazmatArmorItem)) {
                return false;
            }
        }
        return true;
    }

    /** Fire, burning, lava, suffocation, electricity and radiation (the campfire is fire, too). */
    public static boolean hazmatAbsorbs(DamageSource source) {
        return isFireOrLava(source)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(CraftDamageTypes.ELECTRICITY)
                || source.is(CraftDamageTypes.RADIATION);
    }

    private static boolean isFireOrLava(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.CAMPFIRE) || source.is(DamageTypes.LAVA);
    }

    private static int getRemaining(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    @Override
    public ArmorProperties getProperties(LivingEntity entity, ItemStack stack, DamageSource source, double damage,
                                         int slot) {
        if (type == Type.HELMET && hazmatAbsorbs(source) && hasCompleteHazmat(entity)) {
            if (isFireOrLava(source)) {
                entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 1));
            }
            return new ArmorProperties(10, 1.0, Integer.MAX_VALUE);
        }
        if (type == Type.BOOTS && source.is(DamageTypes.FALL)) {
            return new ArmorProperties(10, damage < 8.0 ? 1.0 : 0.875, (getRemaining(stack) + 1) * 2 * 25);
        }
        return new ArmorProperties(0, 1.0, (getRemaining(stack) + 1) / 2 * 25);
    }

    @Override
    public int getArmorDisplay(ItemStack stack) {
        return 1;
    }

    @Override
    public void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, int damage, int slot) {
        if (hazmatAbsorbs(source) && hasCompleteHazmat(entity)) {
            return;
        }
        int damageTotal = damage * 2;
        if (type == Type.BOOTS && source.is(DamageTypes.FALL)) {
            damageTotal = (damage + 1) / 2;
        }
        stack.hurtAndBreak(damageTotal, entity, type.getSlot());
    }

    @Override
    public boolean onFall(Player player, ItemStack stack, int fallDamage) {
        if (type != Type.BOOTS || fallDamage >= 8) {
            return false;
        }
        int armorDamage = (fallDamage + 1) / 2;
        if (armorDamage <= getRemaining(stack) && armorDamage >= 0) {
            stack.hurtAndBreak(armorDamage, player, type.getSlot());
            return true;
        }
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return true;
    }

    @Override
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
        if (type != Type.HELMET || level.isClientSide()) {
            return;
        }
        if (player.isOnFire() && hasCompleteHazmat(player)) {
            if (isInLava(player)) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20, 0, true, true));
            }
            player.clearFire();
        }
        if (player.getAirSupply() <= 100) {
            int slot = player.getInventory().findSlotMatchingItem(Ic2Items.AIR_CELL.toStack());
            if (slot >= 0) {
                player.getInventory().removeItem(slot, 1);
                player.getInventory().add(Ic2Items.EMPTY_CELL.toStack());
                player.setAirSupply(player.getAirSupply() + 150);
            }
        }
    }

    /** IC2 {@code isInLava}: the block at the feet (+0.02) is lava and the feet are below its surface. */
    private static boolean isInLava(Player player) {
        double y = player.getY() + 0.02;
        BlockPos pos = BlockPos.containing(player.getX(), Mth.floor(y), player.getZ());
        BlockState state = player.level().getBlockState(pos);
        if (!state.getFluidState().is(FluidTags.LAVA) || !state.hasProperty(LiquidBlock.LEVEL)) {
            return false;
        }
        int meta = state.getValue(LiquidBlock.LEVEL);
        float emptyPart = (meta >= 8 ? 0 : meta + 1) / 9.0F - 0.11111111F;
        return y < pos.getY() + 1 - emptyPart;
    }
}
