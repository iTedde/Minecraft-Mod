package ivorius.psychedelicraft.command;

import java.util.Optional;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.*;

import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationTypeKeys;
import ivorius.psychedelicraft.network.Channel;
import ivorius.psychedelicraft.network.MsgHallucinate;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
class HallucinateCommand {
    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggestResource(HallucinationTypeKeys.REGISTRY, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registries) {
        dispatcher.register(Commands.literal("hallucinate")
                .requires(source -> source.hasPermission(2))
            .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("type", ResourceLocationArgument.id()).suggests(SUGGESTIONS).executes(ctx -> {
                        sendHallucination(ctx, ctx.getSource().getPlayerOrException(), Optional.empty());
                        return 0;
                    })
                    .then(Commands.argument("target", EntityArgument.players()).executes(ctx -> {
                        EntityArgument.getPlayers(ctx, "target").forEach(player -> {
                            sendHallucination(ctx, player, Optional.empty());
                        });
                        return 0;
                    }))
                    .then(Commands.argument("position", BlockPosArgument.blockPos()).executes(ctx -> {
                        sendHallucination(ctx, ctx.getSource().getPlayerOrException(), Optional.of(BlockPosArgument.getBlockPos(ctx, "position")));
                        return 0;
                    }))
            )
        );
    }

    private static void sendHallucination(CommandContext<CommandSourceStack> ctx, ServerPlayer player, Optional<BlockPos> position) {
        ResourceLocation type = ResourceLocationArgument.getId(ctx, "type");
        Channel.HALLUCINATE.sendToPlayer(new MsgHallucinate(player.getId(), type, position), player);
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.hallucinate.success"), true);
    }
}
