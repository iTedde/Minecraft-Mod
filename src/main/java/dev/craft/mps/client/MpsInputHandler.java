package dev.craft.mps.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.craft.Craft;
import dev.craft.mps.ModularItems;
import dev.craft.mps.MpsInput;
import dev.craft.mps.MpsMovement;
import dev.craft.mps.MpsPayloads;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * MPS {@code ClientTickHandler.tickEnd} (spec 29 §6, §9): collects the movement keys at the end of each client tick
 * for the local movement and sends them with the motion to the server; "down" for flight control defaults to Z.
 */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class MpsInputHandler {
    public static final KeyMapping DOWN = new KeyMapping("key.craft.mps_down", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z, "key.categories.craft");

    private MpsInputHandler() {
    }

    @EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(DOWN);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.getConnection() == null || !ModularItems.usesPowersuit(player)) {
            return;
        }
        MpsInput input = new MpsInput(player.input.forwardImpulse, player.input.leftImpulse, player.input.jumping,
                player.input.shiftKeyDown, DOWN.isDown() && minecraft.screen == null);
        MpsMovement.setInput(player, input);
        Vec3 motion = player.getDeltaMovement();
        PacketDistributor.sendToServer(new MpsPayloads.Input(input, motion.x, motion.y, motion.z));
    }
}
