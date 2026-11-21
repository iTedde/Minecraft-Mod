package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.block.entity.BarrelBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Created by lukas on 13.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class BarrelScreen extends FluidProcessingContraptionScreen<BarrelBlockEntity> {

    public BarrelScreen(FluidContraptionScreenHandler<BarrelBlockEntity> handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawAdditionalInfo(GuiGraphics context, int baseX, int baseY, float tickDelta) {
        float progress = menu.getBlockEntity().getProgress(tickDelta);
        if (progress > 0 && progress < 1) {
            context.blit(background, baseX + 110, baseY + 20, 233, 22, 23, 22);
            int barHeight = (int)(22 * (1 - progress));
            context.blit(background, baseX + 110, baseY + 20 + barHeight, 233, barHeight, 23, 23 - barHeight);
        }
    }
}
