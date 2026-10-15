package dev.craft.data;

import java.util.List;

import dev.craft.Craft;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.forestry.FrItems;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.ElectronTube;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.client.Ic2Client;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ModelFile;
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
        for (DeferredItem<? extends Item> tool : List.of(Ic2Items.MINING_DRILL, Ic2Items.DIAMOND_DRILL,
                Ic2Items.CHAINSAW, Ic2Items.ELECTRIC_HOE, Ic2Items.ELECTRIC_TREETAP, Ic2Items.MINING_LASER)) {
            handheldItem(tool.get());
        }
        basicItem(Ic2Items.ELECTRIC_WRENCH.get());
        basicItem(Ic2Items.OD_SCANNER.get());
        basicItem(Ic2Items.OV_SCANNER.get());
        ItemModelBuilder saberActive = withExistingParent("nano_saber_active", mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/nano_saber_active"));
        handheldItem(Ic2Items.NANO_SABER.get()).override().predicate(Ic2Client.ACTIVE_PROPERTY, 1.0F)
                .model(saberActive).end();
        withExistingParent(BcItems.LANDMARK.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("block/landmark"));
        for (DeferredItem<? extends Item> item : BcItems.simpleItems()) {
            basicItem(item.get());
        }
        for (EngineType type : EngineType.values()) {
            engine(BcItems.engine(type));
        }
        // RenderRefinery.inventoryRender, drawn by RefineryItemRenderer with the same block transforms.
        engine(BcItems.REFINERY);
        // RenderMachine/RenderBlock.inventoryRender, drawn by ForestryMachineItemRenderer.
        engine(FrItems.CARPENTER);
        engine(FrItems.THERMIONIC_FABRICATOR);
        engine(FrItems.ELECTRICAL_ENGINE);
        basicItem(FrItems.TIN_GEAR.get());
        basicItem(FrItems.STURDY_CASING.get());
        // isFull3D: held like a tool.
        handheldItem(FrItems.SOLDERING_IRON.get());
        for (ElectronTube tube : ElectronTube.values()) {
            twoLayers(FrItems.tube(tube), "electron_tube");
        }
        for (CircuitBoardType type : CircuitBoardType.values()) {
            twoLayers(FrItems.circuitBoard(type), "circuit_board");
        }
    }

    /** Engine (and refinery) item drawn by its item renderer, with the transforms of a vanilla block model. */
    private void engine(DeferredItem<? extends Item> item) {
        getBuilder(item.getId().getPath()).parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .transforms()
                .transform(ItemDisplayContext.GUI).rotation(30, 225, 0).scale(0.625F).end()
                .transform(ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.25F).end()
                .transform(ItemDisplayContext.FIXED).scale(0.5F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5F, 0)
                .scale(0.375F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 45, 0).scale(0.4F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 225, 0).scale(0.4F).end()
                .end();
    }

    /** Forestry two-pass item: tinted base (layer 0) and tinted overlay (layer 1). */
    private void twoLayers(DeferredItem<? extends Item> item, String texture) {
        withExistingParent(item.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + texture + "_base"))
                .texture("layer1", modLoc("item/" + texture + "_overlay"));
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
