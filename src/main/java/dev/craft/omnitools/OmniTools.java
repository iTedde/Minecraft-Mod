package dev.craft.omnitools;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** OmniTools 3.0.4 (spec 50 §1): OmniWrench, Mystical Plinth, Forge Lexicon. */
public final class OmniTools {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final DeferredItem<OmniWrenchItem> OMNIWRENCH =
            ITEMS.registerItem("omniwrench", OmniWrenchItem::new, new Item.Properties());

    /** Hardness 5, resistance 2000 → 1200 in 1.21 terms (like obsidian), light 15 with the lexicon. */
    public static final DeferredBlock<PlinthBlock> MYSTICAL_PLINTH = BLOCKS.register("mystical_plinth",
            () -> new PlinthBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .requiresCorrectToolForDrops().strength(5.0F, 1200.0F).noOcclusion()
                    .lightLevel(state -> state.getValue(PlinthBlock.LEXICON) ? 15 : 0)));
    public static final DeferredItem<BlockItem> MYSTICAL_PLINTH_ITEM = ITEMS.registerSimpleBlockItem(MYSTICAL_PLINTH);
    public static final DeferredItem<ForgeLexiconItem> FORGE_LEXICON =
            ITEMS.registerItem("forge_lexicon", ForgeLexiconItem::new, new Item.Properties());

    public static final Supplier<BlockEntityType<LexiconBlockEntity>> LEXICON_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("forge_lexicon", () -> BlockEntityType.Builder.of(LexiconBlockEntity::new,
                    MYSTICAL_PLINTH.get()).build(null));
    public static final Supplier<MenuType<LexiconMenu>> LEXICON_MENU = MENUS.register("forge_lexicon",
            () -> new MenuType<>(LexiconMenu::new, FeatureFlags.VANILLA_SET));

    private OmniTools() {
    }

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENUS.register(modBus);
    }
}
