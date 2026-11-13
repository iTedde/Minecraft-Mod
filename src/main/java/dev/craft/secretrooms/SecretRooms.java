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
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
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

    /** Wood sound; hardness as in the original classes. */
    private static BlockBehaviour.Properties camo(float hardness) {
        return CamoBlock.camoProperties(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD).strength(hardness));
    }

    public static final DeferredBlock<GhostBlock> GHOST_BLOCK =
            BLOCKS.register("ghost_block", () -> new GhostBlock(camo(1.5F)));
    public static final DeferredBlock<OneWayGlassBlock> ONE_WAY_GLASS =
            BLOCKS.register("one_way_glass", () -> new OneWayGlassBlock(camo(1.0F)));
    public static final DeferredBlock<CamoRedstoneBlock> CAMO_REDSTONE =
            BLOCKS.register("camo_redstone", () -> new CamoRedstoneBlock(camo(1.5F)));
    public static final DeferredBlock<CamoSwitchBlock> CAMO_LEVER = BLOCKS.register("camo_lever",
            () -> new CamoSwitchBlock(camo(1.5F), CamoSwitchBlock.Kind.LEVER));
    public static final DeferredBlock<CamoSwitchBlock> CAMO_BUTTON = BLOCKS.register("camo_button",
            () -> new CamoSwitchBlock(camo(0.0F), CamoSwitchBlock.Kind.BUTTON));
    public static final DeferredBlock<CamoSwitchBlock> CAMO_PRESSURE_PLATE = BLOCKS.register("camo_pressure_plate",
            () -> new CamoSwitchBlock(camo(0.5F), CamoSwitchBlock.Kind.PLATE_ALL));
    public static final DeferredBlock<CamoSwitchBlock> CAMO_PLAYER_PRESSURE_PLATE = BLOCKS.register(
            "camo_player_pressure_plate", () -> new CamoSwitchBlock(camo(0.5F), CamoSwitchBlock.Kind.PLATE_PLAYERS));
    public static final DeferredBlock<CamoStairsBlock> CAMO_STAIRS =
            BLOCKS.register("camo_stairs", () -> new CamoStairsBlock(camo(1.5F)));
    public static final DeferredBlock<CamoChestBlock> CAMO_CHEST =
            BLOCKS.register("camo_chest", () -> new CamoChestBlock(camo(1.5F)));
    public static final DeferredBlock<CamoGateBlock> CAMO_GATE =
            BLOCKS.register("camo_gate", () -> new CamoGateBlock(camo(1.5F)));
    /** {@code BlockCamoGateExt}: no item, no drops, cannot be pushed. */
    public static final DeferredBlock<CamoBlock> CAMO_GATE_EXTENSION = BLOCKS.register("camo_gate_extension",
            () -> new CamoBlock(camo(2.5F).noLootTable().pushReaction(PushReaction.BLOCK)));
    public static final DeferredBlock<CamoDoorBlock> CAMO_WOODEN_DOOR = BLOCKS.register("camo_wooden_door",
            () -> new CamoDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<CamoDoorBlock> CAMO_IRON_DOOR = BLOCKS.register("camo_iron_door",
            () -> new CamoDoorBlock(BlockSetType.IRON, BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(3.0F).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<CamoTrapDoorBlock> CAMO_TRAPDOOR = BLOCKS.register("camo_trapdoor",
            () -> new CamoTrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(3.0F).noOcclusion()));
    public static final DeferredBlock<TorchLeverBlock> TORCH_LEVER = BLOCKS.register("torch_lever",
            () -> new TorchLeverBlock(BlockBehaviour.Properties.of().noCollission().instabreak()
                    .lightLevel(state -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)));

    static {
        for (DeferredBlock<? extends Block> block : List.of(GHOST_BLOCK, ONE_WAY_GLASS, CAMO_REDSTONE, CAMO_LEVER,
                CAMO_BUTTON, CAMO_PRESSURE_PLATE, CAMO_PLAYER_PRESSURE_PLATE, CAMO_STAIRS, CAMO_CHEST, CAMO_GATE,
                CAMO_TRAPDOOR, TORCH_LEVER)) {
            ITEMS.registerSimpleBlockItem(block);
        }
        ITEMS.register("camo_wooden_door", () -> new DoubleHighBlockItem(CAMO_WOODEN_DOOR.get(), new Item.Properties()));
        ITEMS.register("camo_iron_door", () -> new DoubleHighBlockItem(CAMO_IRON_DOOR.get(), new Item.Properties()));
    }

    public static final Supplier<BlockEntityType<CamoBlockEntity>> CAMO_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("camo", () -> BlockEntityType.Builder.of(CamoBlockEntity::new,
                    camoBlocks().stream().filter(block -> !(block instanceof CamoChestBlock)).toArray(Block[]::new))
                    .build(null));
    public static final Supplier<BlockEntityType<CamoChestBlock.Entity>> CAMO_CHEST_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("camo_chest", () -> BlockEntityType.Builder.of(CamoChestBlock.Entity::new,
                    CAMO_CHEST.get()).build(null));

    private SecretRooms() {
    }

    /** All camouflaged blocks (with a {@link CamoBlockEntity}). */
    public static List<Block> camoBlocks() {
        return BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())
                .filter(block -> block instanceof CamoBlock || block instanceof CamoDoorBlock
                        || block instanceof CamoTrapDoorBlock).toList();
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
