package dev.craft.data;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CraftItemModelProvider extends ItemModelProvider {
    public CraftItemModelProvider(PackOutput output, ExistingFileHelper files) {
        super(output, Craft.MODID, files);
    }

    @Override
    protected void registerModels() {
        for (CableType type : CableType.values()) {
            basicItem(Ic2Items.cable(type).get());
        }
    }
}
