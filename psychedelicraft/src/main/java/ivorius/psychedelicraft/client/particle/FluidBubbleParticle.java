package ivorius.psychedelicraft.client.particle;

import org.joml.Vector3f;

import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;

public class FluidBubbleParticle extends TextureSheetParticle {

    private boolean survivesInAir;

    FluidBubbleParticle(FluidParticleEffect effect, SpriteSet spriteProvider, ClientLevel world, double x, double y, double z, double vX, double vY, double vZ) {
        this(spriteProvider, world, x, y, z, vX, vY, vZ);
        PSParticleFactories.setColor(this, effect);
    }

    FluidBubbleParticle(DrugDustParticleEffect effect, SpriteSet spriteProvider, ClientLevel world, double x, double y, double z, double vX, double vY, double vZ) {
        this(spriteProvider, world, x, y, z, vX, vY, vZ);
        Vector3f color = effect.getColor();
        rCol = color.x;
        gCol = color.y;
        bCol = color.z;
        survivesInAir = true;
    }

    private FluidBubbleParticle(SpriteSet spriteProvider, ClientLevel world,
            double x, double y, double z,
            double vX, double vY, double vZ) {
        super(world, x, y, z);
        pickSprite(spriteProvider);
        setSize(0.02F, 0.02F);
        quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
        xd = vX * 0.2F + (Math.random() * 2 - 1) * 0.02F;
        yd = vY * 0.2F + (Math.random() * 2 - 1) * 0.02F;
        zd = vZ * 0.2F + (Math.random() * 2 - 1) * 0.02F;
        lifetime = (int)(8 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (lifetime-- <= 0) {
            remove();
            return;
        }
        yd += 0.002;
        move(xd, yd, zd);
        xd *= 0.85F;
        yd *= 0.85F;
        zd *= 0.85F;
        if (!survivesInAir && !level.getFluidState(BlockPos.containing(this.x, this.y, this.z)).getType().is(FluidTags.WATER)) {
            remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}