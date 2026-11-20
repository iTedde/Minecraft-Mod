package ivorius.psychedelicraft.datafix;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;

public interface Schemas {
    static void fixComponents(ItemStackComponentizationFix.ItemStackData data, Dynamic<?> dynamic) {
        PSItemStackComponentizations.run(data, dynamic);
    }
}
