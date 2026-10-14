package dev.craft.data;

import com.google.gson.JsonObject;
import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.client.CableModel;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CraftBlockStateProvider extends BlockStateProvider {
    public CraftBlockStateProvider(PackOutput output, ExistingFileHelper files) {
        super(output, Craft.MODID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        for (CableType type : CableType.values()) {
            BlockModelBuilder model = models().getBuilder("block/cable/" + type.getName())
                    .texture("particle", CableModel.cableTexture(type, 0))
                    .customLoader((parent, files) -> new CableLoaderBuilder(parent, files, type)).end();
            getMultipartBuilder(Ic2Blocks.cable(type).get()).part().modelFile(model).addModel().end();
        }
    }

    private static final class CableLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder> {
        private final CableType type;

        CableLoaderBuilder(BlockModelBuilder parent, ExistingFileHelper files, CableType type) {
            super(CableModel.LOADER_ID, parent, files, false);
            this.type = type;
        }

        @Override
        public JsonObject toJson(JsonObject json) {
            json = super.toJson(json);
            json.addProperty("cable", type.getName());
            return json;
        }
    }
}
