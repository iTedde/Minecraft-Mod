package ivorius.psychedelicraft.client.render;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public interface MeteorlogicalUtil {

    static float getSunFlareIntensity(@Nullable Level world, @Nullable Entity entity, float tickDelta) {

        if (world == null || entity == null) {
            return 0;
        }

        if (world.dimensionType().hasCeiling() || !world.dimensionType().hasSkyLight()) {
            return 0;
        }

        if (entity.pick(100, 1, true).getType() != Type.MISS) {
            return 0;
        }

        float sunRadians = world.getSunAngle(tickDelta);

        Vector3f sunPositionOnScreen = PsycheMatrixHelper.projectPointCurrentView(
                PsycheMatrixHelper.fromPolar(sunRadians, 120)
        ).normalize();

        if (sunPositionOnScreen.z() > 0) {
            return 0;
        }

        Window window = Minecraft.getInstance().getWindow();
        float maxFlareDistance = 170F;
        return 1 - (Math.min(maxFlareDistance, sunPositionOnScreen
                .mul(window.getGuiScaledWidth(), window.getGuiScaledHeight(), 0)
                .length()) / maxFlareDistance);
    }

    static float getSunIntensity(Level world) {
        float skyAngle = getSkyAngle(world);
        if (skyAngle > 1) {
            return 0;
        }

        // intensity (0-1) has a peak at 0.5 (midday)
        float intensity = Mth.cos((skyAngle - 0.5F) * Mth.PI);

        if (world.isRaining()) {
            intensity *= 0.5;
        }
        if (world.isThundering()) {
            intensity *= 0.5;
        }

        return intensity;
    }

    // we translate sun angle to a scale of 0-1 (0=sunrise, 1=sunset, >1 nighttime)
    static float getSkyAngle(Level world) {
        return ((world.getTimeOfDay(1) + 0.25F) % 1F) * 2;
    }

    static float getSkyLightIntensity(Level world, BlockPos pos) {
        if (world.isClientSide) {
            world.updateSkyBrightness();
        }
        return world.getBrightness(LightLayer.SKY, pos) / 15F;
    }
}