package dev.craft.data;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.client.Ic2Client;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public final class CraftItemModelProvider extends ItemModelProvider {
    public CraftItemModelProvider(PackOutput output, ExistingFileHelper files) {
        super(output, Craft.MODID, files);
    }

    @Override
    protected void registerModels() {
        for (CableType type : CableType.values()) {
            basicItem(Ic2Items.cable(type).get());
        }
        for (DeferredItem<? extends Item> item : Ic2Items.simpleItems()) {
            basicItem(item.get());
        }
        withExistingParent(Ic2Items.RUBBER_SAPLING.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("block/rubber_sapling"));
        battery(Ic2Items.RE_BATTERY);
        battery(Ic2Items.ENERGY_CRYSTAL);
        battery(Ic2Items.LAPOTRON_CRYSTAL);
    }

    /** Battery icon with five charge steps (IC2 {@code ItemBattery.getIconFromDamage}). */
    private void battery(DeferredItem<? extends Item> item) {
        String name = item.getId().getPath();
        ItemModelBuilder base = withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + name + "_0"));
        for (int level = 1; level <= 4; level++) {
            ItemModelBuilder step = withExistingParent(name + "_" + level, mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/" + name + "_" + level));
            base.override().predicate(Ic2Client.CHARGE_PROPERTY, level / 4.0F).model(step).end();
        }
    }
}
