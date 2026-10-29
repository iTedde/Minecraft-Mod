package dev.craft.coppergolem;

import dev.craft.ironchest.IronChestBlock;
import dev.craft.ironchest.IronChestBlocks;
import java.util.Map;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Builds a copper golem from a copper block with a carved pumpkin or jack o'lantern placed last (spec 35 §2.1).
 * No mixin: placements by players and endermen arrive as {@link BlockEvent.EntityPlaceEvent}, dispensers get a
 * wrapped pumpkin behaviour.
 */
public final class CopperGolemBuilder {
    /** Full copper blocks only, waxed or not; the golem takes the stage but never the wax. */
    private static final Map<Block, WeatherState> COPPER_BLOCKS = Map.of(
            Blocks.COPPER_BLOCK, WeatherState.UNAFFECTED, Blocks.WAXED_COPPER_BLOCK, WeatherState.UNAFFECTED,
            Blocks.EXPOSED_COPPER, WeatherState.EXPOSED, Blocks.WAXED_EXPOSED_COPPER, WeatherState.EXPOSED,
            Blocks.WEATHERED_COPPER, WeatherState.WEATHERED, Blocks.WAXED_WEATHERED_COPPER, WeatherState.WEATHERED,
            Blocks.OXIDIZED_COPPER, WeatherState.OXIDIZED, Blocks.WAXED_OXIDIZED_COPPER, WeatherState.OXIDIZED);

    /** Below first, so the usual upright golem wins over a sideways one. */
    private static final Direction[] SEARCH = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST,
            Direction.EAST, Direction.UP};

    private CopperGolemBuilder() {
    }

    private static boolean isHead(BlockState state) {
        return state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN);
    }

    /** The copper block next to the head, or {@code null}. */
    private static BlockPos findBody(Level level, BlockPos head) {
        for (Direction direction : SEARCH) {
            BlockPos pos = head.relative(direction);
            if (COPPER_BLOCKS.containsKey(level.getBlockState(pos).getBlock())) {
                return pos;
            }
        }
        return null;
    }

    /**
     * Endermen fire the event before the block is set (and for the block below it), players after; checking once
     * the tick is over covers both and leaves vanilla's snow and iron golems untouched.
     */
    public static void onEntityPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (event.getEntity() instanceof EnderMan enderman) {
            if (enderman.getCarriedBlock() == null || !isHead(enderman.getCarriedBlock())) {
                return;
            }
            pos = pos.above();
        } else if (!isHead(event.getPlacedBlock())) {
            return;
        }
        BlockPos head = pos.immutable();
        MinecraftServer server = level.getServer();
        server.tell(new TickTask(server.getTickCount(), () -> trySpawn(level, head)));
    }

    /** Turns the pattern into a copper chest with a golem at the head's place; true if it did. */
    public static boolean trySpawn(ServerLevel level, BlockPos head) {
        BlockState headState = level.getBlockState(head);
        if (!isHead(headState)) {
            return false;
        }
        BlockPos body = findBody(level, head);
        if (body == null) {
            return false;
        }
        CopperGolem golem = CopperGolems.COPPER_GOLEM.get().create(level);
        if (golem == null) {
            return false;
        }
        BlockState bodyState = level.getBlockState(body);
        WeatherState stage = COPPER_BLOCKS.get(bodyState.getBlock());
        Direction facing = headState.getValue(CarvedPumpkinBlock.FACING);

        level.setBlock(head, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.levelEvent(2001, head, Block.getId(headState));
        level.setBlock(body, IronChestBlocks.copperChest(stage, false).get().defaultBlockState()
                .setValue(IronChestBlock.FACING, facing), Block.UPDATE_ALL);
        level.levelEvent(2001, body, Block.getId(bodyState));

        golem.setWeatherState(stage);
        golem.moveTo(head.getX() + 0.5, head.getY() + 0.05, head.getZ() + 0.5, facing.toYRot(), 0.0F);
        golem.setYHeadRot(facing.toYRot());
        level.addFreshEntity(golem);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, golem.getBoundingBox().inflate(5.0))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(player, golem);
        }
        level.blockUpdated(head, Blocks.AIR);
        return true;
    }

    /**
     * Wraps vanilla's dispenser behaviour (snow/iron golem or putting the pumpkin on) and adds the copper pattern:
     * the head is placed facing away from the dispenser when a copper block touches the target.
     */
    public static void registerDispenserBehaviors() {
        for (Block head : new Block[] {Blocks.CARVED_PUMPKIN, Blocks.JACK_O_LANTERN}) {
            DispenseItemBehavior fallback = DispenserBlock.DISPENSER_REGISTRY
                    .getOrDefault(head.asItem(), new DefaultDispenseItemBehavior());
            DispenserBlock.registerBehavior(head, (source, stack) -> {
                ServerLevel level = source.level();
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                BlockPos target = source.pos().relative(direction);
                boolean vanillaGolem = head == Blocks.CARVED_PUMPKIN
                        && ((CarvedPumpkinBlock) Blocks.CARVED_PUMPKIN).canSpawnGolem(level, target);
                if (vanillaGolem || !level.isEmptyBlock(target) || findBody(level, target) == null) {
                    return fallback.dispense(source, stack);
                }
                Direction facing = direction.getAxis().isHorizontal() ? direction : Direction.NORTH;
                level.setBlock(target, head.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, facing),
                        Block.UPDATE_ALL);
                level.gameEvent(null, GameEvent.BLOCK_PLACE, target);
                trySpawn(level, target);
                stack.shrink(1);
                level.levelEvent(1000, source.pos(), 0);
                level.levelEvent(2000, source.pos(), direction.get3DDataValue());
                return stack;
            });
        }
    }
}
