package dev.craft.ic2.item.tool;

import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * OD and OV scanner (IC2 {@code ItemScanner}/{@code ItemScannerAdv}): scan the column below the player down to the
 * block above the world bottom and report valuable blocks (OD) or ore value (OV) per 1000 scanned blocks.
 */
public class ScannerItem extends Item implements ElectricItem {
    private final boolean advanced;

    public ScannerItem(Properties properties, boolean advanced) {
        super(properties.stacksTo(1).setNoRepair());
        this.advanced = advanced;
    }

    @Override
    public int getMaxCharge() {
        return 10000;
    }

    @Override
    public int getTransferLimit() {
        return 50;
    }

    @Override
    public int getTier() {
        return advanced ? 2 : 1;
    }

    @Override
    public boolean canProvideEnergy() {
        return false;
    }

    public int getScanCost() {
        return advanced ? 250 : 50;
    }

    public int getRange() {
        return advanced ? 4 : 2;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && ElectricItems.use(stack, getScanCost(), player)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), Ic2Sounds.OD_SCANNER.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            BlockPos pos = BlockPos.containing(player.position());
            int value = valueOfArea(level, pos.getX(), pos.getY(), pos.getZ(), advanced);
            player.sendSystemMessage(Component.translatable(advanced
                    ? "message.craft.scanner.value" : "message.craft.scanner.density", value));
        }
        return InteractionResultHolder.pass(stack);
    }

    /** Layer scan for the miner: the scan radius, or 0 without energy. */
    public int startLayerScan(ItemStack stack) {
        return ElectricItems.use(stack, getScanCost(), null) ? getRange() : 0;
    }

    /**
     * IC2 {@code valueOfArea}. Where IC2 would fail with no block scanned (at or below the world bottom) the result
     * is 0.
     */
    public static int valueOfArea(Level level, int x, int y, int z, boolean advancedMode) {
        int totalScore = 0;
        int blocksScanned = 0;
        int range = advancedMode ? 4 : 2;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int blockY = y; blockY > level.getMinBuildHeight(); blockY--) {
            for (int blockX = x - range; blockX <= x + range; blockX++) {
                for (int blockZ = z - range; blockZ <= z + range; blockZ++) {
                    int value = OreValues.valueOf(level.getBlockState(cursor.set(blockX, blockY, blockZ)));
                    if (advancedMode) {
                        totalScore += value;
                    } else if (value > 0) {
                        totalScore++;
                    }
                    blocksScanned++;
                }
            }
        }
        return blocksScanned > 0 ? (int) (1000.0 * totalScore / blocksScanned) : 0;
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
