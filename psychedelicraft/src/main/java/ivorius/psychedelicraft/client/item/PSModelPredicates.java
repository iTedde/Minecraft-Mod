package ivorius.psychedelicraft.client.item;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.entity.MolotovCocktailEntity;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.item.*;
import ivorius.psychedelicraft.item.component.BagContentsComponent;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.material.Fluids;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSModelPredicates {
    static void bootstrap() {
        ItemProperties.registerGeneric(Psychedelicraft.id("using"), (stack, world, entity, seed) -> {
            if (entity == null || entity.getUseItem() != stack) {
                return 0;
            }
            return entity.getUseItemRemainingTicks() > 0 ? 1 : 0;
        });
        ItemProperties.registerGeneric(Psychedelicraft.id("flying"), (stack, world, entity, seed) -> {
            return (stack.getEntityRepresentation() instanceof MolotovCocktailEntity) ? 1 : 0;
        });
        ItemProperties.registerGeneric(Psychedelicraft.id("tripping"), (stack, world, entity, seed) -> {
            return DrugProperties.of(entity).filter(DrugProperties::isTripping).isPresent() ? 1 : 0;
        });
        ItemProperties.register(PSItems.WINE_GRAPE_LATTICE, Psychedelicraft.id("age"), (stack, world, entity, seed) -> stack.getDamageValue() / 10F);
        ItemProperties.register(PSItems.MORNING_GLORY_LATTICE, Psychedelicraft.id("age"), (stack, world, entity, seed) -> stack.getDamageValue() / 10F);
        ItemProperties.registerGeneric(Psychedelicraft.id("filled"), (stack, world, entity, seed) -> {
            if (stack.getItem() instanceof PaperBagItem) {
                BagContentsComponent contents = BagContentsComponent.get(stack);
                return contents.isEmpty() ? 0 : contents.count() > BagContentsComponent.FULL_COUNT
                        ? (contents.count() > BagContentsComponent.FULL_COUNT * 2F ? 1 : 0.75F) : 0.5F;
            }
            if (stack.getItem() instanceof BongItem item) {
                return item.getConsumableSlotIndex(entity) != -1 ? 1 : 0;
            }
            return ItemFluids.of(stack).isEmpty() ? 0 : 1;
        });
        ItemProperties.registerGeneric(Psychedelicraft.id("filled_with_lava"), (stack, world, entity, seed) -> ItemFluids.of(stack).isIn(FluidTags.LAVA) ? 1 : 0);
        ItemProperties.registerGeneric(Psychedelicraft.id("filled_with_water"), (stack, world, entity, seed) -> ItemFluids.of(stack).isOf(Fluids.WATER) ? 1 : 0);

    }

    static void registerColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer > 0 ? -1 : DyedItemColor.getOrDefault(stack, CommonColors.RED), PSItems.HARMONIUM);
        event.register((stack, layer) -> {
            if (layer == 0) {
                return DyedItemColor.getOrDefault(stack, CommonColors.WHITE);
            }
            if (layer == 1) {
                ItemFluids fluids = ItemFluids.of(stack);
                if (!fluids.isEmpty()) {
                    return FluidBoxRenderer.FluidAppearance.getItemColor(fluids);
                }
            }
            return CommonColors.WHITE;
        }, PSItems.BOTTLE, PSItems.MOLOTOV_COCKTAIL, PSItems.GLASS_CHALICE, PSItems.STONE_CUP, PSItems.WOODEN_MUG, PSItems.FILLED_BUCKET, PSItems.FILLED_BOWL, PSItems.SYRINGE);
        event.register((stack, layer) -> {
            if (layer == 0) {
                ItemFluids fluids = ItemFluids.of(stack);
                if (!fluids.isEmpty()) {
                    return FluidBoxRenderer.FluidAppearance.getItemColor(fluids);
                }
            }
            return CommonColors.WHITE;
        }, PSItems.FILLED_GLASS_BOTTLE);
    }
}
