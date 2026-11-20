package ivorius.psychedelicraft.recipe;

import java.util.stream.Stream;
import net.minecraft.world.item.crafting.Recipe;

public interface MultiResultRecipe<T extends Recipe<?>> {
    Stream<T> flatten();
}
