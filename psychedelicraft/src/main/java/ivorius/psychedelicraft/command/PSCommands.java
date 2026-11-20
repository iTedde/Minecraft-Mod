package ivorius.psychedelicraft.command;


/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSCommands {
    static void bootstrap() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.RegisterCommandsEvent.class, event -> {
            DrugCommand.register(event.getDispatcher(), event.getBuildContext());
            HallucinateCommand.register(event.getDispatcher(), event.getBuildContext());
            VomitCommand.register(event.getDispatcher(), event.getBuildContext());
        });
    }
}
