package dev.craft.mffs.item;

import dev.craft.mffs.MffsItems;
import dev.craft.mffs.field.ProjectorModule;
import dev.craft.mffs.field.ProjectorUpgrade;
import dev.craft.mffs.field.ProjectorUpgradeItem;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * {@code ItemProjectorOptionBase} (spec 48 §9.3): one of the nine projector upgrades (stack 8) for slots 2–4. The
 * tooltip lists the compatible modules while Shift is held.
 */
public class ProjectorUpgradeOptionItem extends Item implements ProjectorUpgradeItem {
    private final ProjectorUpgrade upgrade;

    public ProjectorUpgradeOptionItem(ProjectorUpgrade upgrade, Properties properties) {
        super(properties);
        this.upgrade = upgrade;
    }

    @Override
    public ProjectorUpgrade getUpgrade() {
        return upgrade;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!FMLEnvironment.dist.isClient() || !Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.craft.mffs.compatible_with_hint"));
            return;
        }
        tooltip.add(Component.translatable("tooltip.craft.mffs.compatible_with"));
        // Original order: wall, diagonal wall, deflector, tube, sphere, cube, adv. cube, containment.
        for (ProjectorModule module : List.of(ProjectorModule.WALL, ProjectorModule.DIAGONAL_WALL,
                ProjectorModule.DEFLECTOR, ProjectorModule.TUBE, ProjectorModule.SPHERE, ProjectorModule.CUBE,
                ProjectorModule.ADV_CUBE, ProjectorModule.CONTAINMENT)) {
            if (module.supports(upgrade)) {
                tooltip.add(Component.translatable("tooltip.craft.mffs.module."
                        + MffsItems.module(module).getId().getPath()));
            }
        }
    }
}
