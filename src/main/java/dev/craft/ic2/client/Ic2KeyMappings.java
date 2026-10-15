package dev.craft.ic2.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.craft.Craft;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import dev.craft.ic2.keyboard.KeyStatePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** IC2 {@code KeyboardClient}: the IC2 keys, sent to the server whenever their combined state changes. */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class Ic2KeyMappings {
    private static final String CATEGORY = "key.categories.craft.ic2";

    public static final KeyMapping ALT = key("alt", GLFW.GLFW_KEY_LEFT_ALT);
    public static final KeyMapping BOOST = key("boost", GLFW.GLFW_KEY_LEFT_CONTROL);
    public static final KeyMapping MODE_SWITCH = key("mode_switch", GLFW.GLFW_KEY_M);
    public static final KeyMapping SIDE_INVENTORY = key("side_inventory", GLFW.GLFW_KEY_C);

    private static int lastKeyState;
    private static LocalPlayer lastPlayer;

    private Ic2KeyMappings() {
    }

    private static KeyMapping key(String name, int key) {
        return new KeyMapping("key.craft." + name, InputConstants.Type.KEYSYM, key, CATEGORY);
    }

    static void register(RegisterKeyMappingsEvent event) {
        event.register(ALT);
        event.register(BOOST);
        event.register(MODE_SWITCH);
        event.register(SIDE_INVENTORY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.getConnection() == null) {
            lastPlayer = null;
            return;
        }
        int keyState = (ALT.isDown() ? Ic2Keyboard.ALT : 0)
                | (BOOST.isDown() ? Ic2Keyboard.BOOST : 0)
                | (minecraft.options.keyUp.isDown() ? Ic2Keyboard.FORWARD : 0)
                | (MODE_SWITCH.isDown() ? Ic2Keyboard.MODE_SWITCH : 0)
                | (minecraft.options.keyJump.isDown() ? Ic2Keyboard.JUMP : 0)
                | (SIDE_INVENTORY.isDown() ? Ic2Keyboard.SIDE_INVENTORY : 0);
        // A new player object (world change, respawn) starts with an unknown state on the server: send again.
        if (keyState != lastKeyState || player != lastPlayer) {
            PacketDistributor.sendToServer(new KeyStatePayload(keyState));
            Ic2Keyboard.processKeyUpdate(player, keyState);
            lastKeyState = keyState;
            lastPlayer = player;
        }
    }
}
