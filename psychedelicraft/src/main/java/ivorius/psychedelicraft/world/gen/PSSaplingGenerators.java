package ivorius.psychedelicraft.world.gen;

import java.util.Optional;
import net.minecraft.world.level.block.grower.TreeGrower;

public interface PSSaplingGenerators {
    TreeGrower JUNIPER = new TreeGrower("psychedelicraft:juniper",
            Optional.empty(),
            Optional.of(PSFeatureConfigs.JUNIPER_TREE),
            Optional.empty()
    );
}
