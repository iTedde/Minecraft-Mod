package dev.craft.ic2.item.tool;

import dev.craft.Craft;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

/**
 * IC2 {@code ItemElectricTool}: a tool running on EU. With at least the operation cost stored it mines its blocks at
 * full efficiency, every broken block with hardness ≠ 0 costs the operation cost. Not enchantable or repairable,
 * melee damage 1.
 *
 * <p>The mineable blocks (IC2's block list, material rules and Forge tool class) are one block tag per tool.
 */
public abstract class ElectricToolItem extends Item implements ElectricItem {
    private final int maxCharge;
    private final int transferLimit;
    private final int tier;
    protected final int operationEnergyCost;
    protected final float efficiency;
    @Nullable
    private final TagKey<Block> mineable;

    protected ElectricToolItem(Properties properties, int maxCharge, int transferLimit, int tier,
                               int operationEnergyCost, float efficiency, @Nullable TagKey<Block> mineable) {
        super(properties.stacksTo(1).setNoRepair());
        this.maxCharge = maxCharge;
        this.transferLimit = transferLimit;
        this.tier = tier;
        this.operationEnergyCost = operationEnergyCost;
        this.efficiency = efficiency;
        this.mineable = mineable;
    }

    protected static TagKey<Block> mineableTag(String tool) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Craft.MODID, "mineable/" + tool));
    }

    /** Forwards {@link BlockEvent.BreakEvent} to {@link #onBlockStartBreak} (Forge {@code onBlockStartBreak}). */
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ElectricToolItem tool && !player.level().isClientSide()) {
            tool.onBlockStartBreak(stack, player.level(), event.getPos(), event.getState(), player);
        }
    }

    /** Called on the server before {@code player} breaks a block with this tool. */
    protected void onBlockStartBreak(ItemStack stack, Level level, BlockPos pos, BlockState state, Player player) {
    }

    @Override
    public int getMaxCharge() {
        return maxCharge;
    }

    @Override
    public int getTransferLimit() {
        return transferLimit;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public boolean canProvideEnergy() {
        return false;
    }

    protected boolean isMineable(BlockState state) {
        return mineable != null && state.is(mineable);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!ElectricItems.canUse(stack, operationEnergyCost)) {
            return 1.0F;
        }
        return isMineable(state) ? efficiency : 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return isMineable(state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
            if (miningEntity instanceof Player player) {
                ElectricItems.use(stack, operationEnergyCost, player);
            } else {
                ElectricItems.discharge(stack, operationEnergyCost, tier, true, false);
            }
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ElectricItems.barWidth(this, stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ElectricItems.barColor(this, stack);
    }
}
