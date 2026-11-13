package dev.craft.secretrooms;

import java.util.List;
import java.util.function.Supplier;

import dev.craft.Craft;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** SecretRoomsMod (spec 50 §4): camouflaged blocks. */
public final class SecretRooms {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    public static final DeferredItem<Item> CAMO_PASTE = ITEMS.registerSimpleItem("camo_paste");

    /** Wood material and sound; the full camo blocks have no hardness in the original (break at once). */
    private static BlockBehaviour.Properties camo(float hardness) {
        return CamoBlock.camoProperties(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD).strength(hardness));
    }

    public static final DeferredBlock<GhostBlock> GHOST_BLOCK =
            BLOCKS.register("ghost_block", () -> new GhostBlock(camo(1.5F)));
    public static final DeferredBlock<OneWayGlassBlock> ONE_WAY_GLASS =
            BLOCKS.register("one_way_glass", () -> new OneWayGlassBlock(camo(1.0F)));

    static {
        ITEMS.registerSimpleBlockItem(GHOST_BLOCK);
        ITEMS.registerSimpleBlockItem(ONE_WAY_GLASS);
    }

    public static final Supplier<BlockEntityType<CamoBlockEntity>> CAMO_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("camo", () -> BlockEntityType.Builder.of(CamoBlockEntity::new,
                    camoBlocks().toArray(Block[]::new)).build(null));

    private SecretRooms() {
    }

    /** All blocks with a {@link CamoBlockEntity}. */
    public static List<Block> camoBlocks() {
        return BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())
                .filter(block -> block instanceof CamoBlock).toList();
    }

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        modBus.addListener(SecretRooms::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(FacingKey.TYPE, FacingKey.STREAM_CODEC, (payload, context) -> {
            boolean towards = OneWayGlassBlock.toggleFacing(context.player());
            context.player().displayClientMessage(Component.translatable(towards
                    ? "message.craft.secret_rooms.facing_towards" : "message.craft.secret_rooms.facing_away"), true);
        });
    }

    /** Client → server: the one-way facing key ({@code SecretKey}) was pressed. */
    public record FacingKey() implements CustomPacketPayload {
        public static final Type<FacingKey> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "one_way_facing"));
        public static final StreamCodec<ByteBuf, FacingKey> STREAM_CODEC = StreamCodec.unit(new FacingKey());

        @Override
        public Type<FacingKey> type() {
            return TYPE;
        }
    }
}
