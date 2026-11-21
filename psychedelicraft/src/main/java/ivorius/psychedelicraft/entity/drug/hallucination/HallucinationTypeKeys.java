package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import ivorius.psychedelicraft.Psychedelicraft;

public interface HallucinationTypeKeys {
    Set<ResourceLocation> REGISTRY = new HashSet<>();

    ResourceLocation RASTA_HEAD = register("rasta_head");
    ResourceLocation MULTIPLE_ENTITY = register("multiple_entity");
    ResourceLocation SINGLE_ENTITY = register("single_entity");
    ResourceLocation HOSTILE_VILLAGERS = register("hostile_villagers");
    ResourceLocation FRIENDLY_ZOMBIES = register("friendly_zombies");

    static ResourceLocation register(String name) {
        ResourceLocation id = Psychedelicraft.id(name);
        REGISTRY.add(id);
        return id;
    }
}
