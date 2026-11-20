package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ivorius.psychedelicraft.item.SuspiciousItem;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemModelShaper.class)
abstract class MixinItemModels {
    @ModifyVariable(method = "getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;",
            at = @At("HEAD"),
            index = 1)
    private ItemStack modifyStack(ItemStack stack) {
        if (stack.getItem() instanceof SuspiciousItem sus) {
            return sus.getHallucinatedItem().map(Item::getDefaultInstance).orElse(stack);
        }
        return stack;
    }
}
