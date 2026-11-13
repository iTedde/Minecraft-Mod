package dev.craft.gravitygun.client;

import dev.craft.Craft;
import dev.craft.gravitygun.GravityGun;
import dev.craft.gravitygun.GravityGunItem;
import dev.craft.gravitygun.GravityGunPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * GraviGun {@code TickHandlerClient} (spec 50 §3): with the gun in the main hand the attack key throws and the use
 * key grabs/drops (holding it pulls, releasing stops); both no longer break, place or hit anything.
 */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class GravityGunClient {
    private static boolean useDown;
    private static boolean attackDown;

    private GravityGunClient() {
    }

    private static boolean holding(LocalPlayer player) {
        return player != null && player.getMainHandItem().getItem() instanceof GravityGunItem;
    }

    /** Only the press counts ({@code primaryKeyDown}/{@code secondaryKeyDown}); held keys repeat this event. */
    @SubscribeEvent
    public static void interaction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!holding(Minecraft.getInstance().player) || event.isPickBlock()) {
            return;
        }
        event.setCanceled(true);
        event.setSwingHand(false);
        if (event.isAttack() && !attackDown) {
            attackDown = true;
            PacketDistributor.sendToServer(new GravityGunPayload(GravityGunPayload.THROW));
        } else if (event.isUseItem() && !useDown) {
            useDown = true;
            PacketDistributor.sendToServer(new GravityGunPayload(GravityGunPayload.USE));
        }
    }

    /** Key releases; use released stops pulling ({@code sendKeybind(3)}). */
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (attackDown && !minecraft.options.keyAttack.isDown()) {
            attackDown = false;
        }
        if (useDown && !minecraft.options.keyUse.isDown()) {
            useDown = false;
            if (minecraft.player != null) {
                PacketDistributor.sendToServer(new GravityGunPayload(GravityGunPayload.RELEASE));
            }
        }
    }

    @EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(GravityGun.GRABBED_BLOCK.get(), GrabbedBlockRenderer::new);
        }
    }
}
