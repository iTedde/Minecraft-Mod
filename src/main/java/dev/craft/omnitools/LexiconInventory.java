package dev.craft.omnitools;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Forge Lexicon contents ({@code TileLexicon} / portable lexicon, spec 50 §1.3): input, output and the remembered
 * choice. The preview (slot 2 of the original) is derived: the remembered item while it belongs to the input's group,
 * otherwise the group's first item.
 */
public class LexiconInventory extends SimpleContainer {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;

    private Item selected = Items.AIR;

    public LexiconInventory() {
        super(2);
    }

    public Optional<TagKey<Item>> group() {
        return LexiconGroups.groupOf(getItem(INPUT));
    }

    public List<Item> variants() {
        return group().map(LexiconGroups::variants).orElse(List.of());
    }

    /** The variant the input turns into, or empty. */
    public ItemStack preview() {
        List<Item> variants = variants();
        if (variants.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(variants.contains(selected) ? selected : variants.get(0));
    }

    public boolean hasMultipleEntries() {
        return variants().size() > 1;
    }

    /** {@code handlePacket(0/1)}: previous / next variant. */
    public void cycle(int step) {
        List<Item> variants = variants();
        if (variants.isEmpty()) {
            return;
        }
        int index = Math.max(0, variants.indexOf(preview().getItem()));
        selected = variants.get(Math.floorMod(index + step, variants.size()));
        setChanged();
    }

    /** {@code canConvert}: a different variant of the input's group, output empty or the same item and not full. */
    public boolean canConvert() {
        ItemStack input = getItem(INPUT);
        ItemStack target = preview();
        ItemStack output = getItem(OUTPUT);
        return !target.isEmpty() && !input.is(target.getItem())
                && (output.isEmpty() || output.is(target.getItem()) && output.getCount() < output.getMaxStackSize());
    }

    /** {@code convert}: moves the whole input stack over as far as the output takes it. */
    public void convert() {
        if (!canConvert()) {
            return;
        }
        ItemStack input = getItem(INPUT);
        ItemStack target = preview();
        selected = target.getItem();
        ItemStack output = getItem(OUTPUT);
        if (output.isEmpty()) {
            setItem(OUTPUT, target.copyWithCount(input.getCount()));
            setItem(INPUT, ItemStack.EMPTY);
        } else {
            int moved = Math.min(input.getCount(), output.getMaxStackSize() - output.getCount());
            output.grow(moved);
            input.shrink(moved);
            setChanged();
        }
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerHelper.saveAllItems(tag, getItems(), registries);
        tag.putString("selected", BuiltInRegistries.ITEM.getKey(selected).toString());
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        getItems().clear();
        ContainerHelper.loadAllItems(tag, getItems(), registries);
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("selected"));
        selected = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
    }
}
