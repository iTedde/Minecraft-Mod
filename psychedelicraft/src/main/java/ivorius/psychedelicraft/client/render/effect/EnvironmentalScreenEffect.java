/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.platform.Window;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.MeteorlogicalUtil;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.levelgen.Heightmap.Types;

/**
 * @author Sollace
 * @since 15 Jan 2023
 */
public class EnvironmentalScreenEffect implements ScreenEffect {
    private static final ResourceLocation HURT_OVERLAY = Psychedelicraft.id("textures/environment/hurt_overlay.png");

    private float experiencedHealth = 5F;

    private int timeScreenWet;
    private boolean wasInWater;
    private boolean wasInRain;

    private float currentHeat;

    public float getHeatDistortion() {
        if (!PsychedelicraftClient.getConfig().doHeatDistortion.get()) {
            return 0;
        }
        return wasInWater ? 0 : Mth.clamp(((currentHeat - 1) * 0.008f), 0, 1F);
    }

    public float getWaterDistortion() {
        float peyote = ShaderContext.drug(DrugType.PEYOTE);
        float wetness = PsychedelicraftClient.getConfig().doWaterDistortion.get() && wasInWater ? 0.005125F : 0;
        return Math.max(peyote * 0.173F, wetness);
    }

    public float getWaterScreenDistortion() {
        return PsychedelicraftClient.getConfig().waterOverlayEnabled.get() && timeScreenWet > 0 && !wasInWater ? Math.min(1, timeScreenWet / 80F) : 0;
    }

    @Override
    public void update(float tickDelta) {

        Player entity = Minecraft.getInstance().player;

        experiencedHealth = MathUtils.nearValue(experiencedHealth, entity.getHealth(), 0.01f, 0.01f);
        wasInWater = entity.level().getFluidState(BlockPos.containing(entity.getEyePosition())).is(FluidTags.WATER);
        wasInRain = entity.level().getRainLevel(tickDelta) > 0
                && entity.level().getBiome(entity.blockPosition()).value().getPrecipitationAt(entity.blockPosition()) == Precipitation.RAIN
                && entity.level().getHeightmapPos(Types.MOTION_BLOCKING, entity.blockPosition()).getY() <= entity.getY();

        if (PsychedelicraftClient.getConfig().waterOverlayEnabled.get()) {
            timeScreenWet--;

            if (wasInWater) {
                timeScreenWet += 20;
            }
            if (wasInRain) {
                timeScreenWet += 4;
            }

            timeScreenWet = Mth.clamp(timeScreenWet, 0, 100);
        }

        BlockPos pos = entity.blockPosition();
        float newHeat = wasInWater ? 0 : entity.level().getBiome(pos).value().getBaseTemperature();
        if (!entity.level().dimensionType().hasCeiling()) {
            newHeat *= MeteorlogicalUtil.getSunIntensity(entity.level());
            float skyIntensity = MeteorlogicalUtil.getSkyLightIntensity(entity.level(), BlockPos.containing(entity.getEyePosition()));
            newHeat *= Math.min(skyIntensity * skyIntensity * skyIntensity, 1);
        } else {
            float multiplier = 0;
            for (BlockPos p : BlockPos.spiralAround(pos, 2, Direction.EAST, Direction.SOUTH)) {
                multiplier += Math.max(0, entity.level().getBrightness(LightLayer.BLOCK, p) - 11) / 4F;
            }
            newHeat *= Mth.clamp(multiplier, 0.5F, 1F);
        }

        this.currentHeat = MathUtils.nearValue(currentHeat, newHeat, 0.01F, newHeat > currentHeat ? 0.01F : 0.1F);
    }

    @Override
    public void render(GuiGraphics context, Window window, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        @Nullable
        Player entity = client.player;
        if (entity == null) {
            return;
        }
        DrugProperties properties = DrugProperties.of(entity);

        float cardiacArrest = properties.getCardiacArrestProgress(tickDelta);
        float pulseStrength = properties.getMusicManager().getHeartbeatPulseStrength(tickDelta) + cardiacArrest;

        if (PsychedelicraftClient.getConfig().hurtOverlayEnabled.get() && (
                (entity.hurtTime > 0 && properties.getModifier(Drug.PAIN_SUPPRESSION) <= 1F)
                || experiencedHealth < 5 || pulseStrength > 0)) {
            float p1 = Math.max((float)entity.hurtTime / entity.hurtDuration, pulseStrength);
            float p2 = (5 - (experiencedHealth * (1 - pulseStrength))) / 6F;

            float p = Mth.clamp(p1 > 0 ? p1 : p2 > 0 ? p2 : 0, 0, 1);

            int color = FastColor.ARGB32.lerp(cardiacArrest, CommonColors.RED, CommonColors.BLACK);
            RenderUtil.drawOverlay(context, HURT_OVERLAY, FastColor.ARGB32.color(FastColor.as8BitChannel(p), color), window.getGuiScaledWidth(), window.getGuiScaledHeight(), 0, 0, 1, 1, (int) ((1 - p) * 40));
        }
    }

    @Override
    public void close() {

    }
}
