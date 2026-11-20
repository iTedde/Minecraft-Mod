/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.PSTags;

public class MultipleEntityHallucination extends EntityHallucination {
    private final List<Vec3> positions;

    public MultipleEntityHallucination(Player player) {
        super(player, PSTags.Entities.MULTIPLE_ENTITY_HALLUCINATIONS);
        positions = IntStream.range(0, random.nextIntBetweenInclusive(5, 15)).mapToObj(i -> {
            double distance = Math.max(2, i / 10F);
            return new Vec3(
                    random.triangle(0, distance),
                    random.triangle(0, distance),
                    random.triangle(0, distance)
            );
        }).toList();
    }

    @Override
    protected void renderModel(PoseStack matrices, MultiBufferSource vertices, double x, double y, double z, float pitch, float yaw, float tickDelta) {
        positions.forEach(pos -> {
            float scale = entity.getBbWidth();
            super.renderModel(matrices, vertices,
                    x + pos.x * scale,
                    y + pos.y * scale,
                    z + pos.z * scale,
                    pitch, yaw, tickDelta);
        });
    }
}
