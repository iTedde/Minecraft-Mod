package dev.craft.gravisuite.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.craft.Craft;
import dev.craft.gravisuite.GsKeyPayload;
import dev.craft.gravisuite.item.AdvancedJetpackItem;
import dev.craft.gravisuite.item.GraviChestplateItem;
import dev.craft.ic2.client.Ic2KeyMappings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * GraviSuite {@code KeyboardClient}: the "Gravi Fly Key" (pack F, here G – F swaps hands in 1.21) and the press edge
 * of the IC2 mode key ({@code keyModePressed}).
 */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class GsKeyMappings {
    public static final KeyMapping FLY = new KeyMapping("key.craft.gravi_fly", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G, "key.categories.craft");

    private static boolean lastModeDown;

    private GsKeyMappings() {
    }

    @EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(FLY);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.getConnection() == null) {
            return;
        }
        while (FLY.consumeClick()) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (minecraft.screen == null && (chest.getItem() instanceof AdvancedJetpackItem
                    || chest.getItem() instanceof GraviChestplateItem)) {
                PacketDistributor.sendToServer(new GsKeyPayload(GsKeyPayload.FLY));
            }
        }
        boolean modeDown = Ic2KeyMappings.MODE_SWITCH.isDown();
        if (modeDown && !lastModeDown) {
            PacketDistributor.sendToServer(new GsKeyPayload(GsKeyPayload.MODE));
        }
        lastModeDown = modeDown;
    }
}
