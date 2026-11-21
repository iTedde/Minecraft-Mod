package ivorius.psychedelicraft.client.screen;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import ivorius.psychedelicraft.block.DistilleryBlock;
import ivorius.psychedelicraft.block.entity.DistilleryBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;

/**
 * Created by lukas on 13.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class DistilleryScreen extends FluidProcessingContraptionScreen<DistilleryBlockEntity> {
    public DistilleryScreen(FluidContraptionScreenHandler<DistilleryBlockEntity> handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawAdditionalInfo(GuiGraphics context, int baseX, int baseY, float tickDelta) {
        float progress = menu.getBlockEntity().getProgress(tickDelta);

        context.blit(background, baseX + 110, baseY + 14, 233, 22, 23, 22);
        int barHeight = (int)(22 * (1 - progress));
        context.blit(background, baseX + 110, baseY + 14 + barHeight, 233, barHeight, 23, 23 - barHeight);
    }


    @Override
    protected List<Component> getAdditionalTankText() {
        List<Component> text = super.getAdditionalTankText();
        if (menu.getBlockEntity().getBlockState().getOptionalValue(DistilleryBlock.FACING).orElse(Direction.UP) == Direction.UP) {
            text.add(Component.literal("* Missing flask").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
        return text;
    }
}
