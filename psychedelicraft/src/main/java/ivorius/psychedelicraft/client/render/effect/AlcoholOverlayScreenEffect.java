package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.platform.Window;
import ivorius.psychedelicraft.client.SodiumCompat;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.type.AlcoholDrug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class AlcoholOverlayScreenEffect extends DrugOverlayScreenEffect<AlcoholDrug> {
    public AlcoholOverlayScreenEffect() {
        super(DrugType.ALCOHOL);
    }

    @Override
    protected void render(GuiGraphics context, Window window, float tickDelta, DrugProperties properties, AlcoholDrug drug) {
        float alcohol = (float)drug.getActiveValue();
        if (alcohol <= 0) {
            return;
        }

        float overlayAlpha = Math.min(0.8F, (Mth.sin(tickDelta / 80F) * alcohol * 0.5F + alcohol));
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer()
                .getBlockModelShaper()
                .getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        context.blit(0, 0, -90, window.getGuiScaledWidth(), window.getGuiScaledHeight(), sprite, 1, 1, 1, overlayAlpha);
        SodiumCompat.markSpriteActive(sprite);
    }
}
