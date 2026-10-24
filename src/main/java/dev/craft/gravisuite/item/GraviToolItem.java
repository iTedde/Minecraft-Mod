package dev.craft.gravisuite.item;

import java.util.ArrayList;
import java.util.List;

import dev.craft.Craft;
import dev.craft.buildcraft.item.ToolWrench;
import dev.craft.gravisuite.GsDataComponents;
import dev.craft.gravisuite.GsSounds;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.block.RubberWoodBlock;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.tool.ElectricToolItem;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import dev.craft.ic2.machine.Wrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * GraviSuite {@code ItemGraviTool} (spec 28 §9.4): 100 000 EU, transfer 10 000, tier 2. Mode key + right click cycles
 * hoe → treetap → wrench → screwdriver; each mode works like the IC2 tool for 50 EU (wrench always lossless, removal
 * 500 EU, lossless 10 000 EU), the screwdriver turns repeaters, dispensers and pistons for 500 EU. In wrench mode it
 * is also a BuildCraft wrench (500 EU per use).
 */
public class GraviToolItem extends ElectricToolItem implements ToolWrench {
    public static final int HOE = 1;
    public static final int TREETAP = 2;
    public static final int WRENCH = 3;
    public static final int SCREWDRIVER = 4;
    public static final int ENERGY_PER_HOE = 50;
    public static final int ENERGY_PER_TREETAP = 50;
    public static final int ENERGY_PER_SWITCH_SIDE = 50;
    public static final int ENERGY_PER_WRENCH_OPERATION = 500;
    public static final int ENERGY_PER_LOSSLESS_OPERATION = 10000;
    private static final String[] MODE_NAMES = {"hoe", "treetap", "wrench", "screwdriver"};

    public GraviToolItem(Properties properties) {
        super(properties, 100000, 10000, 2, 0, 16.0F, null);
    }

    /** {@code readToolMode}: anything but 1–4 is the hoe. */
    public static int getMode(ItemStack stack) {
        Integer mode = stack.get(GsDataComponents.TOOL_MODE.get());
        return mode == null || mode < HOE || mode > SCREWDRIVER ? HOE : mode;
    }

    /** {@code saveToolMode} + {@code setToolName}; the model follows the mode. */
    public static void setMode(ItemStack stack, int mode) {
        stack.set(GsDataComponents.TOOL_MODE.get(), mode);
    }

    /** The pack names the stack after its mode once it was switched or used on a block. */
    @Override
    public Component getName(ItemStack stack) {
        if (!stack.has(GsDataComponents.TOOL_MODE.get())) {
            return super.getName(stack);
        }
        return Component.translatable(getDescriptionId() + "." + MODE_NAMES[getMode(stack) - 1]);
    }

    /** {@code canDischarge}: the tool's own charge, without armor. */
    private static boolean canDischarge(ItemStack stack, int amount) {
        return ElectricItems.discharge(stack, amount, Integer.MAX_VALUE, true, true) == amount;
    }

    private static void message(Level level, Player player, String key) {
        if (!level.isClientSide()) {
            player.sendSystemMessage(Component.translatable("message.craft.gravisuite.gravitool." + key));
        }
    }

    private static void playSound(Level level, Player player, SoundEvent sound) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** No cost for mining or hitting. */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        return true;
    }

    /** {@code shouldPassSneakingClickToBlock}. */
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && Ic2Keyboard.isModeSwitchKeyDown(player)) {
            int mode = getMode(stack) % SCREWDRIVER + 1;
            setMode(stack, mode);
            player.sendSystemMessage(Component.translatable("message.craft.gravisuite.gravitool."
                    + MODE_NAMES[mode - 1] + "_activated"));
            playSound(level, player, GsSounds.TOOL_CHANGE.get());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            setMode(stack, getMode(stack));
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return switch (getMode(stack)) {
            case WRENCH -> useWrench(stack, context, player);
            case SCREWDRIVER -> useScrewdriver(stack, context, player);
            default -> InteractionResult.PASS;
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return switch (getMode(context.getItemInHand())) {
            case HOE -> useHoe(context, player);
            case TREETAP -> useTreetap(context, player);
            default -> InteractionResult.PASS;
        };
    }

    /** {@code onHoeUse}: grass from the side or top with air above, dirt always (original quirk). */
    private static InteractionResult useHoe(UseOnContext context, Player player) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (!player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.PASS;
        }
        if (!canDischarge(stack, ENERGY_PER_HOE)) {
            message(level, player, "no_energy");
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        boolean grass = state.is(Blocks.GRASS_BLOCK) && context.getClickedFace() != Direction.DOWN
                && level.getBlockState(pos.above()).isAir();
        if (!grass && !state.is(Blocks.DIRT)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            SoundType sound = Blocks.FARMLAND.defaultBlockState().getSoundType();
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound.getStepSound(),
                    SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            ElectricItems.use(stack, ENERGY_PER_HOE, player);
            level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * {@code onTreeTapUse}: the barrel is tapped by reflection on a static method that does not exist, so nothing
     * happens there (original quirk); rubber wood as with the treetap, 50 EU per success.
     */
    private static InteractionResult useTreetap(UseOnContext context, Player player) {
        Level level = context.getLevel();
        if (!(level.getBlockState(context.getClickedPos()).getBlock() instanceof RubberWoodBlock)) {
            return InteractionResult.PASS;
        }
        attemptExtract(context.getItemInHand(), player, level, context.getClickedPos(), context.getClickedFace());
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** {@code attemptExtract} with energy: wet hole 1–3 resin, dry hole small chances (two resin, original quirk). */
    private static void attemptExtract(ItemStack stack, Player player, Level level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        RubberWoodBlock.Resin resin = state.getValue(RubberWoodBlock.RESIN);
        if (!resin.hasHole() || state.getValue(RubberWoodBlock.FACING) != side) {
            return;
        }
        if (resin == RubberWoodBlock.Resin.WET) {
            if (!canDischarge(stack, ENERGY_PER_TREETAP)) {
                message(level, player, "no_energy");
                return;
            }
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(RubberWoodBlock.RESIN, RubberWoodBlock.Resin.DRY), Block.UPDATE_ALL);
                ejectResin(level, pos, side, level.random.nextInt(3) + 1);
                level.scheduleTick(pos, state.getBlock(), RubberWoodBlock.TICK_RATE);
                ElectricItems.use(stack, ENERGY_PER_TREETAP, player);
                playSound(level, player, Ic2Sounds.TREETAP.get());
            }
            return;
        }
        if (level.isClientSide()) {
            return;
        }
        if (level.random.nextInt(5) == 0) {
            level.setBlock(pos, state.setValue(RubberWoodBlock.RESIN, RubberWoodBlock.Resin.NATURAL), Block.UPDATE_ALL);
        }
        if (level.random.nextInt(5) != 0) {
            return;
        }
        if (!canDischarge(stack, ENERGY_PER_TREETAP)) {
            message(level, player, "no_energy");
            return;
        }
        ejectResin(level, pos, side, 1);
        ejectResin(level, pos, side, 1);
        ElectricItems.use(stack, ENERGY_PER_TREETAP, player);
        playSound(level, player, Ic2Sounds.TREETAP.get());
    }

    /** {@code ejectHarz}: resin 0.3 in front of the hole, pickup delay 10. */
    private static void ejectResin(Level level, BlockPos pos, Direction side, int quantity) {
        double x = pos.getX() + 0.5 + side.getStepX() * 0.3;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5 + side.getStepZ() * 0.3;
        for (int i = 0; i < quantity; i++) {
            ItemEntity entity = new ItemEntity(
                    level, x, y, z, new ItemStack(Ic2Items.RESIN.get()));
            entity.setPickUpDelay(10);
            level.addFreshEntity(entity);
        }
    }

    /**
     * {@code onWrenchUse}: like the electric wrench (ALT/sneak pick the side), turning 50 EU, removing 500 EU and
     * always lossless – a drop rate below 1 costs 10 000 EU instead. The terraformer blueprint is never ejected
     * (original quirk: class names compared with {@code ==}).
     */
    private static InteractionResult useWrench(ItemStack stack, UseOnContext context, Player player) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!canDischarge(stack, ENERGY_PER_SWITCH_SIDE)
                || !(level.getBlockEntity(pos) instanceof Wrenchable wrenchable)) {
            return InteractionResult.PASS;
        }
        Direction side = context.getClickedFace();
        if (Ic2Keyboard.isAltKeyDown(player)) {
            int facing = wrenchable.getFacing().get3DDataValue();
            side = Direction.from3DDataValue(player.isShiftKeyDown() ? (facing + 5) % 6 : (facing + 1) % 6);
        } else if (player.isShiftKeyDown()) {
            side = side.getOpposite();
        }
        if (wrenchable.wrenchCanSetFacing(player, side)) {
            if (!level.isClientSide()) {
                wrenchable.setFacing(side);
                ElectricItems.use(stack, ENERGY_PER_SWITCH_SIDE, player);
                playSound(level, player, Ic2Sounds.WRENCH.get());
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!canDischarge(stack, ENERGY_PER_WRENCH_OPERATION) || !wrenchable.wrenchCanRemove(player)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            Craft.LOGGER.info("Player {} used the wrench to remove the {} at {}/{}/{}",
                    player.getName().getString(), state.getBlock().getName().getString(),
                    pos.getX(), pos.getY(), pos.getZ());
            boolean dropOriginalBlock;
            if (wrenchable.getWrenchDropRate() < 1.0F) {
                if (!canDischarge(stack, ENERGY_PER_LOSSLESS_OPERATION)) {
                    message(level, player, "no_energy_lossless");
                    return InteractionResult.SUCCESS;
                }
                dropOriginalBlock = true;
                ElectricItems.use(stack, ENERGY_PER_LOSSLESS_OPERATION, player);
            } else {
                dropOriginalBlock = level.random.nextFloat() <= wrenchable.getWrenchDropRate();
                ElectricItems.use(stack, ENERGY_PER_WRENCH_OPERATION, player);
            }
            List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, serverLevel, pos, blockEntity));
            if (dropOriginalBlock) {
                if (drops.isEmpty()) {
                    drops.add(wrenchable.getWrenchDrop(player));
                } else {
                    drops.set(0, wrenchable.getWrenchDrop(player));
                }
            }
            for (ItemStack drop : drops) {
                GsDrops.dropAsEntity(level, pos, drop);
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            playSound(level, player, Ic2Sounds.WRENCH.get());
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * {@code onScrewdriverUse}, 500 EU each: repeater one step clockwise (delay kept), dispenser
     * {@code ((m & 3) ^ (m >> 2)) + 2} (north → west → south → east), piston +1 (down → up → north → south → west →
     * east). Extended pistons are left alone (1.4.7 dropped the extended bit and left a loose head).
     */
    private static InteractionResult useScrewdriver(ItemStack stack, UseOnContext context, Player player) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        BlockState turned;
        if (state.is(Blocks.REPEATER)) {
            turned = state.setValue(DiodeBlock.FACING, state.getValue(DiodeBlock.FACING).getClockWise());
        } else if (state.is(Blocks.DISPENSER)) {
            int meta = state.getValue(DispenserBlock.FACING).get3DDataValue();
            turned = state.setValue(DispenserBlock.FACING, Direction.from3DDataValue(((meta & 3) ^ (meta >> 2)) + 2));
        } else if ((state.is(Blocks.PISTON) || state.is(Blocks.STICKY_PISTON))
                && !state.getValue(PistonBaseBlock.EXTENDED)) {
            int meta = state.getValue(PistonBaseBlock.FACING).get3DDataValue();
            turned = state.setValue(PistonBaseBlock.FACING, Direction.from3DDataValue((meta + 1) % 6));
        } else {
            return InteractionResult.PASS;
        }
        if (!canDischarge(stack, ENERGY_PER_WRENCH_OPERATION)) {
            message(level, player, "no_energy_operation");
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ElectricItems.use(stack, ENERGY_PER_WRENCH_OPERATION, player);
            level.setBlock(pos, turned, Block.UPDATE_ALL);
            playSound(level, player, Ic2Sounds.WRENCH.get());
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** {@code IToolWrench.canWrench}: only in wrench mode and with 500 EU. */
    @Override
    public boolean canWrench(ItemStack stack, Player player, BlockPos pos) {
        if (getMode(stack) != WRENCH) {
            return false;
        }
        if (canDischarge(stack, ENERGY_PER_WRENCH_OPERATION)) {
            return true;
        }
        message(player.level(), player, "no_energy_operation");
        return false;
    }

    @Override
    public void wrenchUsed(ItemStack stack, Player player, BlockPos pos) {
        if (!player.level().isClientSide()) {
            ElectricItems.use(stack, ENERGY_PER_WRENCH_OPERATION, player);
            playSound(player.level(), player, Ic2Sounds.WRENCH.get());
        }
    }
}
