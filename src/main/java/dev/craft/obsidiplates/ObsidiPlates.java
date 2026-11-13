package dev.craft.obsidiplates;

import dev.craft.Craft;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.BlockItem;

/**
 * Obsidian Pressure Plates (spec 50 §2): one plate that only players trigger. Hardness 12.5 and explosion resistance
 * of obsidian ({@code setResistance(2000)}), any pickaxe.
 */
public final class ObsidiPlates {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredBlock<ObsidianPressurePlateBlock> OBSIDIAN_PRESSURE_PLATE =
            BLOCKS.register("obsidian_pressure_plate", () -> new ObsidianPressurePlateBlock(BlockBehaviour.Properties
                    .of().mapColor(MapColor.COLOR_BLACK).forceSolidOn().requiresCorrectToolForDrops().noCollission()
                    .strength(12.5F, 1200.0F).pushReaction(PushReaction.DESTROY)));
    public static final DeferredItem<BlockItem> OBSIDIAN_PRESSURE_PLATE_ITEM =
            ITEMS.registerSimpleBlockItem(OBSIDIAN_PRESSURE_PLATE);

    private ObsidiPlates() {
    }

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}
