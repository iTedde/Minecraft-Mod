package dev.craft.ic2.block;

import dev.craft.core.CraftDamageTypes;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2EntityTypes;
import dev.craft.ic2.item.WrenchItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * IC2 {@code EntityNuke}: fuse 300, power 35 ({@code explosionPowerNuke}), drop rate 0.05, entity damage 1.5, damage
 * source {@code nuke} (hunger and radiation within ±100). Right-click with a wrench defuses it (−1 durability). The
 * fuse is saved as a byte like every IC2 explosive, so a reloaded nuke continues with {@code 300 & 0xFF = 44}.
 */
public class NukeEntity extends Ic2ExplosiveEntity {
    public NukeEntity(EntityType<? extends NukeEntity> type, Level level) {
        super(type, level);
    }

    public NukeEntity(Level level, double x, double y, double z) {
        this(Ic2EntityTypes.NUKE.get(), level);
        prime(x, y, z);
    }

    @Override
    public int getFuseLength() {
        return 300;
    }

    @Override
    protected float getPower() {
        return 35.0F;
    }

    @Override
    protected float getDropRate() {
        return 0.05F;
    }

    @Override
    protected float getEntityDamage() {
        return 1.5F;
    }

    @Override
    protected DamageSource getDamageSource(ServerLevel level) {
        return level.damageSources().source(CraftDamageTypes.NUKE);
    }

    @Override
    public BlockState getRenderState() {
        return Ic2Blocks.NUKE.get().defaultBlockState();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level().isClientSide() && stack.getItem() instanceof WrenchItem wrench
                && wrench.canTakeDamage(stack, 1)) {
            wrench.damage(stack, 1, player, hand);
            discard();
        }
        return InteractionResult.PASS;
    }
}
