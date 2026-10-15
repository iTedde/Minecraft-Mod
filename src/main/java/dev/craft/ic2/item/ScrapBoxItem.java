package dev.craft.ic2.item;

import java.util.List;

import dev.craft.Craft;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

/**
 * Scrap box (IC2 {@code ItemScrapbox}): right-click consumes one box and drops one random item. The weighted drop list
 * is the loot table {@link #DROPS} (pack state: GregTech replaces IC2's list, see {@code CraftScrapBoxLoot}).
 */
public class ScrapBoxItem extends Item {
    public static final ResourceKey<LootTable> DROPS = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "gameplay/scrap_box"));

    public ScrapBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (level instanceof ServerLevel serverLevel) {
            for (ItemStack drop : getDrop(serverLevel)) {
                player.drop(drop, false);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** One random drop (a single stack, empty only if the table is). */
    public static List<ItemStack> getDrop(ServerLevel level) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(DROPS);
        return table.getRandomItems(new LootParams.Builder(level).create(LootContextParamSets.EMPTY));
    }

    /** IC2 {@code BehaviorScrapboxDispense}: a dispenser opens one box and shoots out the drop. */
    public static void registerDispenserBehavior(Item item) {
        DispenserBlock.registerBehavior(item, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                stack.shrink(1);
                for (ItemStack drop : getDrop(source.level())) {
                    spawnItem(source.level(), drop, 6, direction, DispenserBlock.getDispensePosition(source));
                }
                return stack;
            }
        });
    }
}
