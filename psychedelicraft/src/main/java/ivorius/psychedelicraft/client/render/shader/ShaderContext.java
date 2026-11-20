package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public interface ShaderContext {
    static HallucinationManager hallucinations() {
        return DrugProperties.of(Minecraft.getInstance().player).getHallucinations();
    }

    static DrugProperties properties() {
        return DrugProperties.of(Minecraft.getInstance().player);
    }

    static float drug(DrugType<?> type) {
        return properties().getDrugValue(type);
    }

    static float modifier(Attribute type) {
        return type.get(properties());
    }

    static float ticks() {
        if (Minecraft.getInstance().player == null) {
            return 0;
        }
        return Minecraft.getInstance().player.tickCount + tickDelta();
    }

    static float tickDelta() {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
    }

    static long time() {
        return Minecraft.getInstance().level.getGameTime();
    }

    static float viewDistace() {
        return Minecraft.getInstance().options.renderDistance().get() * 16;
    }

    static Vec3 position() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
    }
}
