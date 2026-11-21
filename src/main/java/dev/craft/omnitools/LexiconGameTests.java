package dev.craft.omnitools;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 50.2 (spec 50 §1.2/§1.3): Forge Lexicon conversion and the Mystical Plinth. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class LexiconGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(3, 1, 3);

    private LexiconGameTests() {
    }

    /** c:ores/iron holds iron and deepslate iron ore: the whole stack turns into the chosen variant. */
    @GameTest(template = EMPTY)
    public static void lexiconConvertsWholeStack(GameTestHelper helper) {
        LexiconInventory lexicon = new LexiconInventory();
        lexicon.setItem(LexiconInventory.INPUT, new ItemStack(Items.IRON_ORE, 10));
        helper.assertTrue(lexicon.hasMultipleEntries(), "iron ore has variants");
        if (lexicon.preview().is(Items.IRON_ORE)) {
            helper.assertFalse(lexicon.canConvert(), "no conversion to itself");
        }
        while (!lexicon.preview().is(Items.DEEPSLATE_IRON_ORE)) {
            lexicon.cycle(1);
        }
        helper.assertTrue(lexicon.canConvert(), "deepslate variant selected");
        lexicon.convert();
        helper.assertTrue(lexicon.getItem(LexiconInventory.OUTPUT).is(Items.DEEPSLATE_IRON_ORE)
                && lexicon.getItem(LexiconInventory.OUTPUT).getCount() == 10, "10 deepslate iron ore");
        helper.assertTrue(lexicon.getItem(LexiconInventory.INPUT).isEmpty(), "input used up");
        lexicon.setItem(LexiconInventory.INPUT, new ItemStack(Items.IRON_ORE, 60));
        helper.assertTrue(lexicon.preview().is(Items.DEEPSLATE_IRON_ORE), "choice remembered for the same group");
        lexicon.convert();
        helper.assertTrue(lexicon.getItem(LexiconInventory.OUTPUT).getCount() == 64
                && lexicon.getItem(LexiconInventory.INPUT).getCount() == 6, "output fills up to 64, rest stays");
        lexicon.setItem(LexiconInventory.INPUT, new ItemStack(Items.DIRT, 5));
        helper.assertFalse(lexicon.hasMultipleEntries() || lexicon.canConvert(), "dirt is in no group");
        helper.succeed();
    }

    /** Menu buttons 0/1/2 reach the lexicon; the portable one gives its contents back. */
    @GameTest(template = EMPTY)
    public static void lexiconMenuButtons(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        LexiconInventory lexicon = new LexiconInventory();
        LexiconMenu menu = new LexiconMenu(1, player.getInventory(), lexicon, p -> true, true);
        lexicon.setItem(LexiconInventory.INPUT, new ItemStack(Items.IRON_ORE, 3));
        ItemStack first = lexicon.preview();
        menu.clickMenuButton(player, LexiconMenu.BUTTON_NEXT);
        helper.assertFalse(ItemStack.isSameItem(first, lexicon.preview()), "next variant");
        menu.clickMenuButton(player, LexiconMenu.BUTTON_PREVIOUS);
        helper.assertTrue(ItemStack.isSameItem(first, lexicon.preview()), "back again");
        while (!lexicon.preview().is(Items.DEEPSLATE_IRON_ORE)) {
            menu.clickMenuButton(player, LexiconMenu.BUTTON_NEXT);
        }
        helper.assertTrue(menu.preview().is(Items.DEEPSLATE_IRON_ORE), "preview slot synced");
        menu.clickMenuButton(player, LexiconMenu.BUTTON_CONVERT);
        helper.assertTrue(menu.preview().isEmpty(), "no preview without input");
        // Giving the contents back on close (clearContainer) only works for a real ServerPlayer, not a mock player.
        helper.assertTrue(lexicon.getItem(LexiconInventory.OUTPUT).getCount() == 3, "converted");
        helper.succeed();
    }

    /** The lexicon goes onto the plinth (light 15, block entity) and comes off again with its contents. */
    @GameTest(template = EMPTY)
    public static void plinthTakesAndReturnsLexicon(GameTestHelper helper) {
        helper.setBlock(POS, OmniTools.MYSTICAL_PLINTH.get());
        BlockPos absolute = helper.absolutePos(POS);
        ForgeLexiconItem.place(helper.getLevel(), absolute, helper.getBlockState(POS));
        helper.assertBlockProperty(POS, PlinthBlock.LEXICON, true);
        helper.assertTrue(helper.getBlockState(POS).getLightEmission(helper.getLevel(), absolute) == 15, "glows");
        LexiconBlockEntity book = (LexiconBlockEntity) helper.getBlockEntity(POS);
        book.lexicon().setItem(LexiconInventory.INPUT, new ItemStack(Items.IRON_ORE, 4));
        PlinthBlock.removeLexicon(helper.getLevel(), absolute, helper.getBlockState(POS));
        helper.assertBlockProperty(POS, PlinthBlock.LEXICON, false);
        helper.assertTrue(helper.getLevel().getBlockEntity(absolute) == null, "empty plinth has no block entity");
        long lexicons = helper.getEntities(EntityType.ITEM).stream().map(ItemEntity::getItem)
                .filter(stack -> stack.is(OmniTools.FORGE_LEXICON.get())).count();
        long ores = helper.getEntities(EntityType.ITEM).stream().map(ItemEntity::getItem)
                .filter(stack -> stack.is(Items.IRON_ORE)).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(lexicons == 1 && ores == 4, "lexicon and contents dropped");
        helper.killAllEntities();
        helper.succeed();
    }
}
