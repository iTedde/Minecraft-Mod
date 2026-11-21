package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public interface PSWoodTypes {
    WoodType JUNIPER = register("juniper");

    static WoodType register(String name) {
        ResourceLocation id = Psychedelicraft.id(name);
        return WoodType.register(new WoodType(id.toString(), BlockSetType.register(new BlockSetType(id.toString()))));
    }
}
