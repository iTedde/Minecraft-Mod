package dev.craft.ic2.terraformer;

import dev.craft.ic2.machine.TerraformerBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** IC2 {@code ItemTFBP}: right-click a terraformer to insert the blueprint (the old one is ejected). */
public class TerraformerBlueprintItem extends Item {
    private final TerraformerBlueprint blueprint;

    public TerraformerBlueprintItem(Properties properties, TerraformerBlueprint blueprint) {
        super(properties.stacksTo(1));
        this.blueprint = blueprint;
    }

    public TerraformerBlueprint getBlueprint() {
        return blueprint;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TerraformerBlockEntity terraformer)) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            ItemStack stack = context.getItemInHand();
            terraformer.insertBlueprint(stack.copy());
            Player player = context.getPlayer();
            if (player != null) {
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
            } else {
                stack.setCount(0);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
