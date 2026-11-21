package dev.craft.mffs.item;

import dev.craft.mffs.block.ProjectorBlockEntity;
import dev.craft.mffs.field.ProjectorModule;
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * {@code ModuleBase} (spec 48 §9.2): a projector module (stack 8). Right click on a projector (right EB) puts one into
 * the empty module slot.
 */
public class ProjectorModuleItem extends Item {
    private final ProjectorModule module;

    public ProjectorModuleItem(ProjectorModule module, Properties properties) {
        super(properties);
        this.module = module;
    }

    public ProjectorModule getModule() {
        return module;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (context.getLevel().isClientSide() || player == null
                || !(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ProjectorBlockEntity projector)
                || !MffsSecurity.checkAccess(projector, player, SecurityRight.EB)) {
            return InteractionResult.PASS;
        }
        if (projector.getStack(ProjectorBlockEntity.SLOT_MODULE).isEmpty()) {
            projector.setStack(ProjectorBlockEntity.SLOT_MODULE, stack.split(1));
            player.sendSystemMessage(Component.translatable("message.craft.mffs.module_installed",
                    getDescription()));
            return InteractionResult.SUCCESS;
        }
        player.sendSystemMessage(Component.translatable("message.craft.mffs.projector_slot_not_empty"));
        return InteractionResult.PASS;
    }
}
