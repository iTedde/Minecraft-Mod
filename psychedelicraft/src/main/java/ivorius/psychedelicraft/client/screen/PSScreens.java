package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * @author Sollace
 * @since 13 Jan 2023
 */
public interface PSScreens {
    static void bootstrap(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(PSScreenHandlers.DRYING_TABLE, DryingTableScreen::new);
        event.register(PSScreenHandlers.BARREL, BarrelScreen::new);
        event.register(PSScreenHandlers.DISTILLERY, DistilleryScreen::new);
        event.register(PSScreenHandlers.FLASK, FlaskScreen<FlaskBlockEntity>::new);
        event.register(PSScreenHandlers.MASH_TUB, MushTubScreen::new);
    }
}
