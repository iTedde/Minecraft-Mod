package ivorius.psychedelicraft.item;

import java.util.List;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.item.component.BagContentsComponent;
import ivorius.psychedelicraft.item.component.PSComponents;

public class PaperBagItem extends Item {

    public PaperBagItem(Properties settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior(){
            @Override
            public ItemStack execute(BlockSource pointer, ItemStack stack) {
                BagContentsComponent contents = BagContentsComponent.get(stack);
                if (contents.isEmpty() || stack.getCount() > 1) {
                    return super.execute(pointer, stack);
                }
                BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);
                ItemStack dispensed = builder.split(1);
                BagContentsComponent.set(stack, builder);
                super.execute(pointer, dispensed);
                return stack;
            }
        });
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack bag, Slot slot, ClickAction clickType, Player player) {
        if (clickType != ClickAction.SECONDARY || bag.getCount() != 1) {
            return false;
        }

        BagContentsComponent contents = BagContentsComponent.get(bag);

        ItemStack slotStack = slot.getItem();

        if (slotStack.isEmpty()) {
            if (!contents.isEmpty()) {
                BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);
                // dispense into empty slot
                builder.add(slot.safeInsert(builder.split(contents.stack().getMaxStackSize())));
                player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 1, 1);
                BagContentsComponent.set(bag, builder);
                return true;
            }
        } else if (canPickUp(slotStack)) {
            BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);
            if (builder.canAdd(slotStack)) {
                // pick up from filled slot
                int maxTaken = BagContentsComponent.getMaxCountForItem(slotStack.getItem()) - contents.count();

                builder.add(slot.safeTake(slotStack.getCount(), maxTaken, player));
                player.playSound(SoundEvents.BUNDLE_INSERT, 1, 1);
                BagContentsComponent.set(bag, builder);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack clickedStack, ItemStack cursorStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if (clickType != ClickAction.SECONDARY || !(cursorStack.getItem() instanceof PaperBagItem || slot.allowModification(player)) || clickedStack.getCount() != 1) {
            return false;
        }

        BagContentsComponent contents = BagContentsComponent.get(clickedStack);
        BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);

        if (cursorStack.isEmpty()) {
            // remove from bag into held stack
            cursorStackReference.set(builder.split(64));
            player.playSound(SoundEvents.BUNDLE_INSERT, 1, 1);
            BagContentsComponent.set(clickedStack, builder);
            return true;
        }

        if (builder.canAdd(cursorStack)) {
            int maxTaken = BagContentsComponent.getMaxCountForItem(cursorStack.getItem()) - contents.count();

            if (canPickUp(cursorStack)) {
                // insert into bag from held stack
                builder.add(cursorStack.split(maxTaken));
                player.playSound(SoundEvents.BUNDLE_INSERT, 1, 1);
                BagContentsComponent.set(clickedStack, builder);
                return true;
            }

            // bag to bag transfer
            /*
            if (cursorStack.getItem() instanceof PaperBagItem) {
                Contents.Builder cursorContents = new Contents.Builder(getContents(cursorStack));
                builder.add(cursorContents.split(maxTaken));
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1, 1);
                setContents(clickedStack, builder.build());
                setContents(cursorStack, cursorContents.build());
                return true;
            }
            */
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        BagContentsComponent contents = stack.get(PSComponents.BAG_CONTENTS);
        if (contents != null) {
            contents.addToTooltip(context, tooltip::add, type);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack bag = user.getItemInHand(hand);
        ItemStack swithdrawn = BagContentsComponent.withdraw(bag, user.isDiscrete() ? 1 : BagContentsComponent.get(bag).stack().getMaxStackSize());

        if (!swithdrawn.isEmpty()) {
            user.drop(swithdrawn, false, false);
            user.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 1, 1);
            user.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(bag, world.isClientSide());
        }
        return InteractionResultHolder.fail(bag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack bag = context.getItemInHand();

        BagContentsComponent contents = BagContentsComponent.get(bag);

        BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);

        if (context.getLevel().getEntities(context.getPlayer(),
                AABB.unitCubeFromLowerCorner(context.getClickLocation()).inflate(0.25),
                e -> e instanceof ItemEntity i && canPickUp(i.getItem()))
            .stream().filter(e -> {
                if (builder.add(((ItemEntity)e).getItem())) {
                    if (((ItemEntity)e).getItem().isEmpty()) {
                        e.discard();
                    }
                    return true;
                }
                return false;
            }).findAny().isEmpty()) {

            if (!contents.isEmpty()) {
                context.getPlayer().drop(builder.split(context.getPlayer().isDiscrete() ? 1 : contents.stack().getMaxStackSize()), false, false);
                bag.set(PSComponents.BAG_CONTENTS, builder.build());
                context.getPlayer().playSound(SoundEvents.BUNDLE_REMOVE_ONE, 1, 1);
                return InteractionResult.SUCCESS;
            }
            bag.set(PSComponents.BAG_CONTENTS, builder.build());
            return InteractionResult.FAIL;
        }

        if (bag.getCount() > 1) {
            bag = bag.split(1);
            bag.set(PSComponents.BAG_CONTENTS, builder.build());
            context.getPlayer().getInventory().add(bag);
        } else {
            bag.set(PSComponents.BAG_CONTENTS, builder.build());
        }
        context.getPlayer().playSound(SoundEvents.BUNDLE_INSERT, 1, 1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (world.isClientSide() || !selected || stack.getCount() != 1) {
            return;
        }

        if (entity instanceof Player player) {
            Container inv = player.getInventory();

            if (inv.getItem(slot) != stack) {
                return;
            }

            BagContentsComponent contents = BagContentsComponent.get(stack);
            if (contents.isFull() || contents.isEmpty()) {
                return;
            }

            BagContentsComponent.Builder builder = new BagContentsComponent.Builder(contents);

            boolean changed = false;

            if (slot < inv.getContainerSize() - 1) {
                changed |= builder.add(inv.getItem(slot + 1));
            }
            if (slot > 0) {
                changed |= builder.add(inv.getItem(slot - 1));
            }

            if (changed) {
                player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 1, 1);
                stack.set(PSComponents.BAG_CONTENTS, builder.build());
                inv.setItem(slot, stack);
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        if (!BagContentsComponent.get(stack).isEmpty()) {
            return Component.translatable(getDescriptionId(stack) + ".filled", BagContentsComponent.get(stack).stack().getHoverName());
        }
        return super.getName(stack);
    }

    private boolean canPickUp(ItemStack incomingStack) {
        return incomingStack.is(PSTags.Items.CAN_GO_INTO_PAPER_BAG);
    }
}
