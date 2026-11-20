package ivorius.psychedelicraft.command;

import com.mojang.brigadier.CommandDispatcher;
import ivorius.psychedelicraft.entity.drug.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Sollace
 * @since 18 April 2023
 */
class VomitCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registries) {
        dispatcher.register(Commands.literal("vomit").requires(source -> source.hasPermission(2)).executes(ctx -> {
            DrugProperties.of(ctx.getSource().getPlayerOrException()).getStomach().vomit();
            return 0;
        })
            .then(Commands.argument("target", EntityArgument.players()).executes(ctx -> {
                ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
                DrugProperties.of(player).getStomach().vomit();
                return 0;
            })
        ));
    }
}
