package dev.craft.secretrooms.client;

import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;
import dev.craft.Craft;
import dev.craft.secretrooms.CamoBlockEntity;
import dev.craft.secretrooms.SecretRooms;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Secret Rooms client: camouflage models and tint, the one-way facing key ({@code SecretKey}, default "\"). */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class SecretRoomsClient {
    public static final KeyMapping FACING_KEY = new KeyMapping("key.craft.one_way_facing",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "key.categories.craft");

    private SecretRoomsClient() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(FACING_KEY);
    }

    @SubscribeEvent
    public static void wrapModels(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (Block block : SecretRooms.camoBlocks()) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                ModelResourceLocation location = BlockModelShaper.stateToModelLocation(state);
                BakedModel original = models.get(location);
                if (original != null && !(original instanceof CamoBakedModel)) {
                    models.put(location, new CamoBakedModel(original));
                }
            }
        }
    }

    /** {@code colorMultiplier}: the tint of the copied block (grass, leaves, water …). */
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (level == null || pos == null || !(level.getBlockEntity(pos) instanceof CamoBlockEntity camo)) {
                return -1;
            }
            return event.getBlockColors().getColor(camo.getCamo(), level, pos, tintIndex);
        }, SecretRooms.camoBlocks().toArray(Block[]::new));
    }

    @EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
    public static final class Keys {
        private Keys() {
        }

        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            while (FACING_KEY.consumeClick()) {
                if (Minecraft.getInstance().player != null) {
                    PacketDistributor.sendToServer(new SecretRooms.FacingKey());
                }
            }
        }
    }
}
