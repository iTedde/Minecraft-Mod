package dev.craft.ic2.block;

import com.mojang.serialization.MapCodec;
import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;

/**
 * IC2 {@code BlockPoleFence}: a fence that only connects to iron fences. A free-standing pole charged by the
 * magnetizer ({@link #MAGNETIZATION}, IC2 metadata) lifts players wearing metal boots (spec 05, iron fence).
 */
public class IronFenceBlock extends FenceBlock {
    public static final MapCodec<IronFenceBlock> CODEC = simpleCodec(IronFenceBlock::new);
    public static final IntegerProperty MAGNETIZATION = IntegerProperty.create("magnetization", 0, 15);
    /** IC2 {@code IMetalArmor} and vanilla iron, gold and chain boots. */
    public static final TagKey<Item> METAL_BOOTS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Craft.MODID, "metal_boots"));

    public IronFenceBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(MAGNETIZATION, 0));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public MapCodec<FenceBlock> codec() {
        return (MapCodec) CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MAGNETIZATION);
    }

    @Override
    public boolean connectsTo(BlockState state, boolean isSideSolid, Direction direction) {
        return state.is(this);
    }

    /** {@code isPole}: no iron fence on any of the four sides. */
    public static boolean isPole(BlockState state) {
        return !state.getValue(NORTH) && !state.getValue(EAST) && !state.getValue(SOUTH) && !state.getValue(WEST);
    }

    /** {@code onEntityCollidedWithBlock}. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof Player player) || !isPole(state)) {
            return;
        }
        int magnetization = state.getValue(MAGNETIZATION);
        Vec3 motion = player.getDeltaMovement();
        double motionY = motion.y;
        if (magnetization > 0 && player.getItemBySlot(EquipmentSlot.FEET).is(METAL_BOOTS)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(MAGNETIZATION, magnetization - 1), Block.UPDATE_CLIENTS);
            }
            motionY += 0.075F;
            if (motionY > 0.0) {
                motionY *= 1.03F;
                player.fallDistance = 0.0F;
            }
            if (player.isShiftKeyDown()) {
                motionY = Math.min(motionY, 0.3F);
            } else {
                motionY = Math.min(motionY, 1.5);
            }
        } else if (player.isShiftKeyDown()) {
            if (motionY < -0.25) {
                motionY *= 0.9F;
            } else {
                player.fallDistance = 0.0F;
            }
        }
        player.setDeltaMovement(motion.x, motionY, motion.z);
    }
}
