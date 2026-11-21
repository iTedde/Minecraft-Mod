package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

abstract class FluidProcessingContraptionScreen<T extends FluidProcessingBlockEntity> extends FlaskScreen<T> {

    public FluidProcessingContraptionScreen(FluidContraptionScreenHandler<T> handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected List<Component> getAdditionalTankText() {
        List<Component> tooltip = new ArrayList<>();
        Resovoir tank = menu.getBlockEntity().getPrimaryTank();
        tank.getContents().fluid().appendTankTooltip(tank.getContents(), menu.getBlockEntity().getLevel(), tooltip, menu.getBlockEntity());
        return tooltip;
    }
}
