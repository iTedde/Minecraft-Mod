package dev.craft.ironchest;

import java.util.List;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Cat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/** Entry point of the Iron Chests content (spec 22). */
public final class IronChests {
    private IronChests() {
    }

    public static void init(IEventBus modBus) {
        IronChestBlocks.BLOCKS.register(modBus);
        IronChestItems.ITEMS.register(modBus);
        IronChestBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        IronChestMenus.MENUS.register(modBus);
        modBus.addListener(IronChests::registerCapabilities);
        // Pack config ocelotsSitOnChests=true.
        NeoForge.EVENT_BUS.addListener(IronChests::onEntityJoin);
    }

    /** A plain {@code IInventory}: every slot from every side. */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IronChestBlockEntityTypes.IRON_CHEST.get(),
                (chest, side) -> new InvWrapper(chest));
    }

    /** {@code OcelotsSitOnChestsHandler}: swaps the cat's sit-on-block goal for one that accepts the chests. */
    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Cat cat)) {
            return;
        }
        List<WrappedGoal> goals = cat.goalSelector.getAvailableGoals().stream()
                .filter(goal -> goal.getGoal() instanceof CatSitOnBlockGoal
                        && !(goal.getGoal() instanceof IronChestCatSitGoal))
                .toList();
        for (WrappedGoal goal : goals) {
            cat.goalSelector.removeGoal(goal.getGoal());
            cat.goalSelector.addGoal(goal.getPriority(), new IronChestCatSitGoal(cat, 0.8));
        }
    }
}
