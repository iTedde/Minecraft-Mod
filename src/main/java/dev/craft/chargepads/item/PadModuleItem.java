package dev.craft.chargepads.item;

import java.util.List;

import dev.craft.chargepads.ChargePadsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * An emitter module item (spec 47 §7). Tooltip as in the original: "Restricted: Emitter Slot", "Modulation:
 * Projector/Field" and for the field expansion modules "Strength: Alpha/Delta/Theta" (grey/aqua/red).
 */
public class PadModuleItem extends Item {
    private static final ChatFormatting[] STRENGTH_COLOURS = {ChatFormatting.GRAY, ChatFormatting.AQUA,
            ChatFormatting.RED};
    private static final String[] STRENGTHS = {"alpha", "delta", "theta"};

    private final PadModule module;

    public PadModuleItem(Properties properties, PadModule module) {
        super(properties);
        this.module = module;
    }

    public PadModule getModule() {
        return module;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        boolean enabled = ChargePadsConfig.ALLOW_EMITTER_UPGRADE || !module.isEmitter();
        tooltip.add(Component.translatable("tooltip.craft.chargepads.restricted").withStyle(ChatFormatting.RED)
                .append(Component.translatable(enabled ? "tooltip.craft.chargepads.emitter_slot"
                        : "tooltip.craft.chargepads.disabled").withStyle(enabled ? ChatFormatting.GRAY
                        : ChatFormatting.ITALIC)));
        tooltip.add(Component.translatable("tooltip.craft.chargepads.modulation").withStyle(ChatFormatting.RED)
                .append(Component.translatable(module.isField() ? "gui.craft.chargepads.field"
                        : "gui.craft.chargepads.projector").withStyle(ChatFormatting.GRAY)));
        int expansion = module.getExpansion();
        if (expansion > 0) {
            tooltip.add(Component.translatable("tooltip.craft.chargepads.strength").withStyle(ChatFormatting.WHITE)
                    .append(Component.translatable("tooltip.craft.chargepads.strength." + STRENGTHS[expansion - 1])
                            .withStyle(STRENGTH_COLOURS[expansion - 1])));
        }
    }
}
