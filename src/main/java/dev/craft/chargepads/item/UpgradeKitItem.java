package dev.craft.chargepads.item;

import dev.craft.chargepads.ChargePadsConfig;
import dev.craft.chargepads.PadTier;
import dev.craft.chargepads.block.ChargePadBlock;
import dev.craft.chargepads.block.ChargePadBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Crystalizor and lapotronic upgrade kits (spec 47 §8, original {@code o}): right-clicking the bottom face of the
 * matching pad turns it into the next pad. A wrong pad swallows the click and nothing happens.
 */
public class UpgradeKitItem extends Item {
    private final PadTier from;

    public UpgradeKitItem(Properties properties, PadTier from) {
        super(properties);
        this.from = from;
    }

    /** The pad this kit upgrades. */
    public PadTier getFrom() {
        return from;
    }

    /** {@code onItemUseFirst}: runs before the pad's own click (GUI). */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (!ChargePadsConfig.UPGRADE_ANY_SIDE && context.getClickedFace() != Direction.DOWN) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ChargePadBlockEntity pad)) {
            return InteractionResult.PASS;
        }
        if (pad.getTier() != from || context.getLevel().isClientSide()) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        ChargePadBlock.upgrade(context.getLevel(), context.getClickedPos(), pad, context.getPlayer());
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
