package dev.craft.lightshield;

import dev.craft.core.CraftCreativeTabs;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Entry point of the electric light shield (spec 30, new content without pack original). */
public final class LightShield {
    private LightShield() {
    }

    public static void init(IEventBus modBus) {
        LsDataComponents.COMPONENTS.register(modBus);
        LsBlocks.BLOCKS.register(modBus);
        LsItems.ITEMS.register(modBus);
        modBus.addListener(LightShield::addToCreativeTab);
        NeoForge.EVENT_BUS.addListener(LightShield::playerTick);
        NeoForge.EVENT_BUS.addListener(LightShield::shieldBlock);
    }

    /** Next to the other IC2 electric tools, empty and charged. */
    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == CraftCreativeTabs.IC2.get()) {
            ItemStack stack = new ItemStack(LsItems.ELECTRIC_LIGHT_SHIELD.get());
            event.accept(stack);
            event.accept(ElectricItems.charged(stack));
        }
    }

    private static void playerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            ShieldLights.tick(event.getEntity());
        }
    }

    /** EU instead of durability for blocked hits (spec 30 §2). */
    private static void shieldBlock(LivingShieldBlockEvent event) {
        if (event.getBlocked() && event.getEntity() instanceof Player player
                && player.level() instanceof ServerLevel level
                && player.getUseItem().getItem() instanceof ElectricLightShieldItem) {
            ElectricLightShieldItem.chargeForBlock(player.getUseItem(), event.shieldDamage(), level, player);
        }
    }
}
