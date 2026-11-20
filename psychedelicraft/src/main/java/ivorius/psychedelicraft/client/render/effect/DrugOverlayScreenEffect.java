package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;

public abstract class DrugOverlayScreenEffect<D extends Drug> implements ScreenEffect {

    private final DrugType<?> type;

    public DrugOverlayScreenEffect(DrugType<?> type) {
        this.type = type;
    }

    @Override
    public boolean shouldApply(float tickDelta) {
        return DrugProperties.of((Entity)Minecraft.getInstance().player).filter(properties -> properties.isDrugActive(type)).isPresent();
    }

    @Override
    public void update(float tickDelta) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(GuiGraphics context, Window window, float tickDelta) {
        if (Minecraft.getInstance().player != null) {
            DrugProperties properties = DrugProperties.of(Minecraft.getInstance().player);
            context.pose().pushPose();
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            render(context, window, tickDelta, properties, (D)properties.getDrug(type));
            RenderSystem.enableDepthTest();
            context.pose().popPose();
        }
    }

    protected abstract void render(GuiGraphics context, Window window, float tickDelta, DrugProperties properties, D drug);

    @Override
    public void close() throws Exception {
    }

}
