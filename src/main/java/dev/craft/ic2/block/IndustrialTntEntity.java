package dev.craft.ic2.block;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2EntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** IC2 {@code EntityItnt}: fuse 60, power 5.5, drop rate 0.9, entity damage 0.3. */
public class IndustrialTntEntity extends Ic2ExplosiveEntity {
    public IndustrialTntEntity(EntityType<? extends IndustrialTntEntity> type, Level level) {
        super(type, level);
    }

    public IndustrialTntEntity(Level level, double x, double y, double z) {
        this(Ic2EntityTypes.INDUSTRIAL_TNT.get(), level);
        prime(x, y, z);
    }

    @Override
    public int getFuseLength() {
        return 60;
    }

    @Override
    protected float getPower() {
        return 5.5F;
    }

    @Override
    protected float getDropRate() {
        return 0.9F;
    }

    @Override
    protected float getEntityDamage() {
        return 0.3F;
    }

    @Override
    public BlockState getRenderState() {
        return Ic2Blocks.INDUSTRIAL_TNT.get().defaultBlockState();
    }
}
