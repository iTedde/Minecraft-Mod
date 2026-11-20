package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.LockableHungerManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

@Mixin(Gui.class)
abstract class MixinInGameHud {
    @Shadow
    private int tickCount;
    private int originalTicks;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"))
    private void onRender(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo info) {
        DrugRenderer.INSTANCE.onRenderOverlay(context, tickCounter);
    }

    @Inject(method = "renderFood", at = @At("HEAD"))
    private void onBeforeRenderShank(GuiGraphics context, Player player, int top, int right, CallbackInfo info) {
        originalTicks = tickCount;
        tickCount = DrugProperties.of(player).getStomach().setVisibleState(tickCount);
    }

    @Inject(method = "renderFood", at = @At("RETURN"))
    private void onAfterRenderShank(GuiGraphics context, Player player, int top, int right, CallbackInfo info) {
        tickCount = originalTicks;
        ((LockableHungerManager)player.getFoodData()).setShanksShaking(false);
    }
}
