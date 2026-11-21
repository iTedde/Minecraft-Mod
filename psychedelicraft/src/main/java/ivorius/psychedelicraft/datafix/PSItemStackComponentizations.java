package ivorius.psychedelicraft.datafix;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;

final class PSItemStackComponentizations {
    static void run(ItemStackComponentizationFix.ItemStackData data, Dynamic<?> dynamic) {
        data.removeTag("fluid").result().ifPresent(fluid -> {
            String fluidId = fluid.get("id").asString().result().orElse("psychedelicraft:empty");
            int level = fluid.get("level").asInt(0);

            if (level > 0 && !fluidId.equalsIgnoreCase("psychedelicraft:empty")) {
                data.setComponent("psychedelicraft:fluids", dynamic.emptyMap()
                        .set("fluid", dynamic.createString(fluidId))
                        .set("amount", dynamic.createInt(level))
                        .set("attributes", fluid.get("attributes").orElseEmptyMap())
                );
            }
        });

        if (data.is("psychedelicraft:rift_jar")) {
            data.removeTag("riftFraction").result().ifPresent(riftFraction -> {
                if (riftFraction.asFloat(0) > 0) {
                    data.setComponent("psychedelicraft:rift_fraction", dynamic.emptyMap()
                            .set("amount", riftFraction)
                    );
                }
            });
        }

        if (data.is("psychedelicraft:paper_bag")) {
            data.moveTagToComponent("contents", "psychedelicraft:bag_contents");
        }
    }
}
