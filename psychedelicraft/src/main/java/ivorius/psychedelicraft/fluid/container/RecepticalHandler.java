package ivorius.psychedelicraft.fluid.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;

public class RecepticalHandler {
    private static final List<Pair<Predicate<ItemStack>, RecepticalHandler>> REGISTRY = new ArrayList<>();
    private static final RecepticalHandler DEFAULT = new RecepticalHandler();

    public static RecepticalHandler get(ItemStack stack) {
        return REGISTRY.stream().filter(pair -> pair.getFirst().test(stack)).map(Pair::getSecond).findFirst().orElse(DEFAULT);
    }

    public static void register(Item item, RecepticalHandler handler) {
        register(i -> i.is(item), handler);
    }

    public static void register(TagKey<Item> tag, RecepticalHandler handler) {
        register(i -> i.is(tag), handler);
    }

    public static void register(Predicate<ItemStack> item, RecepticalHandler handler) {
        REGISTRY.add(new Pair<>(item, handler));
    }

    public static void registerPair(Item empty, Item filled, RecepticalHandler handler) {
        register(i -> i.is(empty) || i.is(filled), handler);
    }

    public static void registerPair(Item empty, Item filled) {
        registerPair(empty, filled, new RecepticalHandler() {
            @Override
            public ItemStack toFilled(ItemStack item, ItemFluids contents) {
                return applyFluid(changeStackType(item, filled), contents);
            }

            @Override
            public ItemStack toEmpty(ItemStack item) {
                return applyFluid(changeStackType(item, empty), ItemFluids.EMPTY);
            }
        });
    }

    public static ItemStack changeStackType(ItemStack item, Item newType) {
        return item.getItem() == newType ? item : item.transmuteCopy(newType);
    }

    public static ItemStack applyFluid(ItemStack stack, ItemFluids contents) {
        if (contents.isEmpty()) {
            if (stack.has(PSComponents.FLUIDS)) {
                stack.remove(PSComponents.FLUIDS);
            }
        } else {
            stack.set(PSComponents.FLUIDS, contents);
        }
        return stack;
    }

    public ItemStack toFilled(ItemStack item, ItemFluids contents) {
        return applyFluid(item.copy(), contents);
    }

    public ItemStack toEmpty(ItemStack item) {
        return applyFluid(item.copy(), ItemFluids.EMPTY);
    }

    static {
        register(stack -> stack.is(Tags.Items.BUCKETS) || stack.is(Tags.Items.BUCKETS_EMPTY) || stack.is(PSItems.FILLED_BUCKET), new RecepticalHandler() {
            private final Function<SimpleFluid, Optional<Item>> filledBuckets = Util.memoize(fluid -> {
                String path = fluid.getId().getPath() + "_bucket";
                return BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getPath().equals(path)).findFirst().map(BuiltInRegistries.ITEM::get);
            });

            @Override
            public ItemStack toFilled(ItemStack item, ItemFluids contents) {
                Item newType = contents.amount() < FluidVolumes.BUCKET || !contents.isBaseForm()
                        ? PSItems.FILLED_BUCKET
                        : filledBuckets.apply(contents.fluid()).orElse(PSItems.FILLED_BUCKET);
                return applyFluid(changeStackType(item, newType), newType == PSItems.FILLED_BUCKET ? contents : ItemFluids.EMPTY);
            }

            @Override
            public ItemStack toEmpty(ItemStack item) {
                return applyFluid(changeStackType(item, Items.BUCKET), ItemFluids.EMPTY);
            }
        });
        registerPair(Items.BOWL, PSItems.FILLED_BOWL);
        register(stack -> stack.getItem() instanceof BottleItem || stack.getItem() instanceof PotionItem || stack.getItem() == PSItems.FILLED_GLASS_BOTTLE, new RecepticalHandler() {
            private final Function<SimpleFluid, Optional<Item>> filledBottles = Util.memoize(fluid -> {
                String path = fluid.getId().getPath() + "_bottle";
                String altPath = fluid.getId().getPath() + "_glass_bottle";
                return BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getPath().equals(path) || id.getPath().equals(altPath)).findFirst().map(BuiltInRegistries.ITEM::get);
            });

            @Override
            public ItemStack toFilled(ItemStack item, ItemFluids contents) {
                if (contents.amount() < FluidVolumes.GLASS_BOTTLE) {
                    return applyFluid(changeStackType(item, PSItems.FILLED_GLASS_BOTTLE), contents);
                }
                if (contents.isOf(Fluids.WATER)) {
                    return PotionContents.createItemStack(Items.POTION, Potions.WATER);
                }
                Item newType = contents.amount() < FluidVolumes.GLASS_BOTTLE || !contents.isBaseForm()
                        ? PSItems.FILLED_GLASS_BOTTLE
                        : filledBottles.apply(contents.fluid()).orElse(PSItems.FILLED_GLASS_BOTTLE);
                return applyFluid(changeStackType(item, newType), newType == PSItems.FILLED_GLASS_BOTTLE ? contents : ItemFluids.EMPTY);
            }

            @Override
            public ItemStack toEmpty(ItemStack item) {
                return applyFluid(changeStackType(item, Items.GLASS_BOTTLE), ItemFluids.EMPTY);
            }
        });
    }
}
