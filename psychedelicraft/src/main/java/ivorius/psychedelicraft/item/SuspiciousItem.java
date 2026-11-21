package ivorius.psychedelicraft.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugProperties;

public class SuspiciousItem extends Item {

    private final ItemLike hallucinatedFormSupplier;

    public static ItemLike createForms(ItemLike... items) {
        RandomSource rng = RandomSource.create();
        return () -> {
            return items[rng.nextInt(items.length)].asItem();
        };
    }

    @Nullable
    private Item chosenItem;

    public SuspiciousItem(Properties settings, ItemLike hallucinatedFormSupplier) {
        super(settings);
        this.hallucinatedFormSupplier = hallucinatedFormSupplier;
    }

    public Optional<Item> getHallucinatedItem() {
        if (Psychedelicraft.getGlobalDrugProperties().filter(DrugProperties::isTripping).isPresent()) {
            if (chosenItem == null) {
                chosenItem = hallucinatedFormSupplier.asItem();
            }
            return Optional.of(chosenItem);
        }
        chosenItem = null;
        return Optional.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        getHallucinatedItem().ifPresent(item -> {
            item.appendHoverText(item.getDefaultInstance(), context, tooltip, type);
        });
    }

    @Override
    public Component getDescription() {
        return getHallucinatedItem().map(Item::getDescription).orElseGet(super::getDescription);
    }

    @Override
    public Component getName(ItemStack stack) {
        return getHallucinatedItem().map(i -> i.getName(stack)).orElseGet(() -> super.getName(stack));
    }
}
