package dev.craft.data;

import java.util.ArrayList;
import java.util.List;

import dev.craft.Craft;
import dev.craft.thermalexpansion.TeItems;
import dev.craft.advancedmachines.AdvancedMachinesItems;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.forestry.FrItems;
import dev.craft.gregtech.GtItems;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.ElectronTube;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.client.Ic2Client;
import dev.craft.powerplants.PpItems;
import dev.craft.lightshield.LsItems;
import dev.craft.portalgun.PgItems;
import dev.craft.enderstorage.EsItems;
import dev.craft.gravisuite.GsItems;
import dev.craft.mps.MpsItems;
import dev.craft.gravisuite.client.GraviSuiteClient;
import dev.craft.enderstorage.client.EnderStorageClient;
import dev.craft.ironchest.ChestUpgradeType;
import dev.craft.ironchest.IronChestItems;
import dev.craft.ironchest.IronChestType;
import dev.craft.redpower.RpItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.block.model.BlockModel;
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
        basicItem(PpItems.COKE.get());
        basicItem(AdvancedMachinesItems.REFINED_IRON_DUST.get());
        for (DeferredItem<? extends Item> item : List.of(TeItems.INVAR_DUST, TeItems.INVAR_INGOT, TeItems.WOOD_CHIPS,
                TeItems.SAWDUST, TeItems.COMPRESSED_SAWDUST, TeItems.SLAG, TeItems.RICH_SLAG, TeItems.PNEUMATIC_SERVO,
                TeItems.REDSTONE_RECEPTION_COIL, TeItems.REDSTONE_TRANSMISSION_COIL, TeItems.REDSTONE_CONDUCTANCE_COIL,
                TeItems.COPPER_GEAR, TeItems.INVAR_GEAR, TeItems.MACHINE_FRAME, TeItems.CRESCENT_HAMMER,
                TeItems.MULTIMETER)) {
            basicItem(item.get());
        }
        // Frames and the empty conduit are drawn by TE's item renderers; until their sub-phases they reuse the
        // machine frame and the hardened glass.
        for (DeferredItem<? extends Item> item : List.of(TeItems.ENERGY_CELL_FRAME, TeItems.FULL_ENERGY_CELL_FRAME,
                TeItems.TESSERACT_FRAME, TeItems.UNATTUNED_TESSERACT)) {
            withExistingParent(item.getId().getPath(), mcLoc("item/generated")).texture("layer0",
                    modLoc("item/machine_frame"));
        }
        withExistingParent(TeItems.EMPTY_ENERGY_CONDUIT.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("block/hardened_glass"));
        basicItem(PpItems.IRON_WIND_ROTOR.get());
        basicItem(PpItems.STEEL_WIND_ROTOR.get());
        basicItem(PpItems.CARBON_WIND_ROTOR.get());
        RpItems.tools().forEach(tool -> handheldItem(tool.get()));
        basicItem(RpItems.BLANK_PLAN.get());
        basicItem(RpItems.PLAN.get());
        basicItem(PgItems.LONG_FALL_BOOT.get());
        basicItem(PgItems.LONG_FALL_BOOTS.get());
        lightShield();
        for (ChestUpgradeType type : ChestUpgradeType.values()) {
            basicItem(IronChestItems.upgrade(type).get());
        }
        for (CableType type : CableType.values()) {
            basicItem(Ic2Items.cable(type).get());
        }
        for (DeferredItem<? extends Item> item : Ic2Items.simpleItems()) {
            basicItem(item.get());
        }
        for (DeferredItem<? extends Item> item : Ic2Items.armorItems()) {
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
        ItemModelBuilder toolboxFull = withExistingParent("toolbox_full", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/toolbox_full"));
        basicItem(Ic2Items.TOOLBOX.get()).override().predicate(Ic2Client.FULL_PROPERTY, 1.0F)
                .model(toolboxFull).end();
        // IC2 ItemBooze icons 192 + time ratio (beer), 200 (rum).
        ItemModelBuilder booze = withExistingParent("booze_mug", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/booze_mug_0"));
        for (int offset : new int[]{1, 2, 3, 4, 5, 8}) {
            String name = offset == 8 ? "booze_mug_rum" : "booze_mug_" + offset;
            booze.override().predicate(Ic2Client.BOOZE_PROPERTY, offset / 8.0F).model(
                    withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/" + name))).end();
        }
        withExistingParent(BcItems.LANDMARK.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("block/landmark"));
        withExistingParent(GtItems.WATER.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", mcLoc("block/water_still"));
        withExistingParent(GtItems.LAVA.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", mcLoc("block/lava_still"));
        ItemModelBuilder lithiumCharged = withExistingParent("lithium_battery_charged", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/gt/lithium_battery"));
        withExistingParent(GtItems.LITHIUM_BATTERY.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("item/gt/lithium_battery_empty")).override()
                .predicate(Ic2Client.CHARGE_PROPERTY, 1.0F).model(lithiumCharged).end();
        withExistingParent(GtItems.LITHIUM_BATPACK.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("item/gt/lithium_batpack"));
        for (DeferredItem<? extends Item> item : GtItems.simpleItems()) {
            withExistingParent(item.getId().getPath(), mcLoc("item/generated"))
                    .texture("layer0", modLoc("item/gt/" + item.getId().getPath()));
        }
        for (DeferredItem<? extends Item> item : BcItems.simpleItems()) {
            basicItem(item.get());
        }
        for (EngineType type : EngineType.values()) {
            engine(BcItems.engine(type));
        }
        // RenderRefinery.inventoryRender, drawn by RefineryItemRenderer with the same block transforms.
        engine(BcItems.REFINERY);
        engine(TeItems.PORTABLE_TANK);
        engine(TeItems.STEAM_ENGINE);
        engine(TeItems.MAGMATIC_ENGINE);
        engine(TeItems.ENERGY_CELL);
        engine(TeItems.ENERGY_CONDUIT);
        engine(TeItems.LIQUIDUCT);
        engine(TeItems.ENERGY_TESSERACT);
        engine(TeItems.LIQUID_TESSERACT);
        engine(TeItems.ITEM_TESSERACT);
        // RenderMachine/RenderBlock.inventoryRender, drawn by ForestryMachineItemRenderer.
        engine(FrItems.CARPENTER);
        engine(Ic2Items.machine(Ic2Machine.PERSONAL_SAFE));
        for (IronChestType type : IronChestType.values()) {
            engine(IronChestItems.chest(type));
        }
        engine(EsItems.ENDER_CHEST);
        engine(EsItems.ENDER_TANK);
        // Modular Powersuits: components, armor and tool as flat icons; the Tinker Table is its block entity model.
        List<DeferredItem<? extends Item>> mpsItems = new ArrayList<>(MpsItems.components());
        mpsItems.addAll(MpsItems.armor());
        mpsItems.add(MpsItems.POWER_TOOL);
        for (DeferredItem<? extends Item> item : mpsItems) {
            String name = item.getId().getPath();
            withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/mps/" + name));
        }
        engine(MpsItems.TINKER_TABLE);
        for (List<? extends DeferredItem<? extends Item>> items : List.of(GsItems.simpleItems(), GsItems.armorItems())) {
            for (DeferredItem<? extends Item> item : items) {
                String name = item.getId().getPath();
                withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/gravisuite/" + name));
            }
        }
        for (DeferredItem<? extends Item> tool : GsItems.toolItems()) {
            String name = tool.getId().getPath();
            withExistingParent(name, mcLoc("item/handheld")).texture("layer0", modLoc("item/gravisuite/" + name));
        }
        // GraviTool: one model per mode, the hoe is the base.
        ItemModelBuilder graviTool = withExistingParent(GsItems.GRAVITOOL.getId().getPath(), mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/gravisuite/gravitool_hoe"));
        String[] graviToolModes = {"treetap", "wrench", "screwdriver"};
        for (int i = 0; i < graviToolModes.length; i++) {
            String name = "gravitool_" + graviToolModes[i];
            graviTool.override().predicate(GraviSuiteClient.TOOL_MODE_PROPERTY, (i + 2) / 4.0F)
                    .model(withExistingParent(name, mcLoc("item/handheld"))
                            .texture("layer0", modLoc("item/gravisuite/" + name))).end();
        }
        // Ender pouch: base (global/personal) plus three tinted colour bands.
        ItemModelBuilder pouchPersonal = pouch("ender_pouch_personal", "ender_pouch_personal");
        pouch(EsItems.ENDER_POUCH.getId().getPath(), "ender_pouch").override()
                .predicate(EnderStorageClient.PERSONAL_PROPERTY, 1.0F).model(pouchPersonal).end();
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
    private ItemModelBuilder pouch(String name, String base) {
        return withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/enderstorage/" + base))
                .texture("layer1", modLoc("item/enderstorage/ender_pouch_band_1"))
                .texture("layer2", modLoc("item/enderstorage/ender_pouch_band_2"))
                .texture("layer3", modLoc("item/enderstorage/ender_pouch_band_3"));
    }

    /** Electric light shield: vanilla shield transforms (models/item/shield*.json), drawn by its BEWLR. */
    private void lightShield() {
        String name = LsItems.ELECTRIC_LIGHT_SHIELD.getId().getPath();
        ModelFile blocking = getBuilder(name + "_blocking").parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .guiLight(BlockModel.GuiLight.FRONT).texture("particle", mcLoc("block/iron_block"))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(45, 155, 0).translation(-3.49F, 11, -2)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(45, 155, 0).translation(11.51F, 7, 2.5F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 180, -5).translation(-15, 5, -11)
                .scale(1.25F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 180, -5).translation(5, 5, -11)
                .scale(1.25F).end()
                .transform(ItemDisplayContext.GUI).rotation(15, -25, -5).translation(2, 3, 0).scale(0.65F).end()
                .end();
        getBuilder(name).parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .guiLight(BlockModel.GuiLight.FRONT).texture("particle", mcLoc("block/iron_block"))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0, 90, 0).translation(10, 6, -4).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0, 90, 0).translation(10, 6, 12).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 180, 5).translation(-10, 2, -10)
                .scale(1.25F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 180, 5).translation(10, 0, -10)
                .scale(1.25F).end()
                .transform(ItemDisplayContext.GUI).rotation(15, -25, -5).translation(2, 3, 0).scale(0.65F).end()
                .transform(ItemDisplayContext.FIXED).rotation(0, 180, 0).translation(-4.5F, 4.5F, -5).scale(0.55F)
                .end()
                .transform(ItemDisplayContext.GROUND).translation(2, 4, 2).scale(0.25F).end()
                .end()
                .override().predicate(mcLoc("blocking"), 1.0F).model(blocking).end();
    }

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
