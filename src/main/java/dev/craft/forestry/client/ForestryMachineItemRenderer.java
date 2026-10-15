package dev.craft.forestry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.forestry.FrItems;
import dev.craft.forestry.machine.CarpenterBlockEntity;
import dev.craft.forestry.machine.FabricatorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** {@code inventoryRender}: the carpenter upright with empty tanks, the fabricator facing east. */
public class ForestryMachineItemRenderer extends BlockEntityWithoutLevelRenderer {
    @Nullable
    private ForestryMachineRenderer<CarpenterBlockEntity> carpenter;
    @Nullable
    private ForestryMachineRenderer<FabricatorBlockEntity> fabricator;

    public ForestryMachineItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        var models = Minecraft.getInstance().getEntityModels();
        if (stack.is(FrItems.CARPENTER.get())) {
            if (carpenter == null) {
                carpenter = new ForestryMachineRenderer<>(models.bakeLayer(ForestryMachineRenderer.CARPENTER_LAYER),
                        ForestryMachineRenderer.Kind.CARPENTER);
            }
            carpenter.render(Direction.UP, 0, pose, buffers, light, overlay);
        } else {
            if (fabricator == null) {
                fabricator = new ForestryMachineRenderer<>(models.bakeLayer(ForestryMachineRenderer.FABRICATOR_LAYER),
                        ForestryMachineRenderer.Kind.FABRICATOR);
            }
            fabricator.render(Direction.EAST, 0, pose, buffers, light, overlay);
        }
    }
}
