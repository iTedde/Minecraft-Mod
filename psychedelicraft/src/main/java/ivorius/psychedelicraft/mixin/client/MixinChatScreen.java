package ivorius.psychedelicraft.mixin.client;

import java.time.Instant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.MessageDistorter;

@Mixin(ChatScreen.class)
abstract class MixinChatScreen extends Screen {
    MixinChatScreen() { super(null); }

    @Inject(method = "normalizeChatMessage(Ljava/lang/String;)Ljava/lang/String;",
            at = @At("RETURN"),
            cancellable = true)
    public void onNormalize(String chatText, CallbackInfoReturnable<String> info) {
        info.setReturnValue(MessageDistorter.INSTANCE.distortOutgoingMessage(minecraft.player, info.getReturnValue()));
    }
}

@Mixin(ComponentRenderUtils.class)
abstract class MixinChatMessages {
    @Inject(method = "stripColor(Ljava/lang/String;)Ljava/lang/String;",
            at = @At("RETURN"),
            cancellable = true)
    private static void onGetRenderedChatMessage(String message, CallbackInfoReturnable<String> info) {
        info.setReturnValue(MessageDistorter.INSTANCE.distortIncomingMessage(Minecraft.getInstance().player, info.getReturnValue()));
    }
}

@Mixin(ChatListener.class)
abstract class MixinMessageHandler {
    @Inject(method = "showMessageToPlayer", at = @At("RETURN"))
    private void onProcessChatMessageInternal(ChatType.Bound params, PlayerChatMessage message, Component decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValueZ()) {
            DrugProperties.of((Entity)Minecraft.getInstance().player).ifPresent(properties -> {
                properties.getHallucinations().getEntities().getChatBots().forEach(chatbot -> {
                    chatbot.onMessageReceived(sender.getName(), decorated);
                });
            });
        }
    }
}