package ivorius.psychedelicraft.client.particle;

import org.joml.Vector3f;

import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;

public class ExhaledSmokeParticle extends SmokeParticle {
    public ExhaledSmokeParticle(DrugDustParticleEffect effect, SpriteSet spriteProvider, ClientLevel world,
            double x, double y, double z,
            double vX, double vY, double vZ) {
        super(world, x, y, z, vX, vY, vZ, 1, spriteProvider);
        Vector3f color = effect.getColor();
        rCol = color.x;
        gCol = color.y;
        bCol = color.z;
    }
}
