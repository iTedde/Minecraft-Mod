package dev.craft.redpower;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import dev.craft.Craft;
import dev.craft.redpower.table.PlanItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * RedPower World gem tools (spec 19): seven tools for each of ruby, green sapphire and sapphire; project table and plans
 * (spec 20).
 */
public final class RpItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    /** Handsaw durability ({@code setMaxDamage(640)}); RedPower's only use, cutting microblocks, is not in scope. */
    public static final int HANDSAW_DURABILITY = 640;

    /**
     * Tool kinds with the dust count of GregTech's recovery grids (0 = no grid fits). Damage and attack speed are the
     * 1.21 diamond tool values (equal to diamond in 1.4.7 as well); the sickle keeps its 1.4.7 damage 6.
     */
    public enum Tool {
        PICKAXE("pickaxe", 3, tier -> new PickaxeItem(tier, new Item.Properties()
                .attributes(PickaxeItem.createAttributes(tier, 1.0F, -2.8F)))),
        SHOVEL("shovel", 1, tier -> new ShovelItem(tier, new Item.Properties()
                .attributes(ShovelItem.createAttributes(tier, 1.5F, -3.0F)))),
        AXE("axe", 3, tier -> new AxeItem(tier, new Item.Properties()
                .attributes(AxeItem.createAttributes(tier, 5.0F, -3.0F)))),
        SWORD("sword", 2, tier -> new SwordItem(tier, new Item.Properties()
                .attributes(SwordItem.createAttributes(tier, 3, -2.4F)))),
        HOE("hoe", 2, tier -> new HoeItem(tier, new Item.Properties()
                .attributes(HoeItem.createAttributes(tier, -3.0F, 0.0F)))),
        SICKLE("sickle", 3, tier -> new SickleItem(tier, new Item.Properties()
                .attributes(DiggerItem.createAttributes(tier, 2.0F, -2.8F)))),
        HANDSAW("handsaw", 0, tier -> new Item(new Item.Properties().durability(HANDSAW_DURABILITY)));

        private final String name;
        private final int recoveredDust;
        private final Function<Tier, Item> factory;

        Tool(String name, int recoveredDust, Function<Tier, Item> factory) {
            this.name = name;
            this.recoveredDust = recoveredDust;
            this.factory = factory;
        }

        public String getName() {
            return name;
        }

        public int getRecoveredDust() {
            return recoveredDust;
        }
    }

    /** {@code itemPlanBlank}: paper + indigo dye. */
    public static final DeferredItem<Item> BLANK_PLAN = ITEMS.registerSimpleItem("blank_plan");
    /** {@code ItemPlan}: written by the project table, stack size 1, rare. */
    public static final DeferredItem<PlanItem> PLAN = ITEMS.register("plan",
            () -> new PlanItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<BlockItem> PROJECT_TABLE = ITEMS.registerSimpleBlockItem(RpBlocks.PROJECT_TABLE);
    public static final DeferredItem<BlockItem> LARGE_PROJECT_TABLE =
            ITEMS.registerSimpleBlockItem(RpBlocks.LARGE_PROJECT_TABLE);

    private static final Map<RpGem, Map<Tool, DeferredItem<Item>>> TOOLS = new EnumMap<>(RpGem.class);

    static {
        for (RpGem gem : RpGem.values()) {
            Map<Tool, DeferredItem<Item>> tools = new EnumMap<>(Tool.class);
            for (Tool tool : Tool.values()) {
                tools.put(tool, ITEMS.register(gem.getName() + "_" + tool.getName(),
                        () -> tool.factory.apply(gem.getTier())));
            }
            TOOLS.put(gem, tools);
        }
    }

    private RpItems() {
    }

    public static DeferredItem<Item> tool(RpGem gem, Tool tool) {
        return TOOLS.get(gem).get(tool);
    }

    public static Stream<DeferredItem<Item>> tools() {
        return TOOLS.values().stream().flatMap(tools -> tools.values().stream());
    }
}
