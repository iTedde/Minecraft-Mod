package ivorius.psychedelicraft.world.gen.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public interface MutableStructurePool {
    /**
     * Appends the elements of {@code psychedelicraftmc:<path>} template pools to {@code minecraft:<path>}
     * (upstream: Fabric dynamic registry callback; here when the server is about to start).
     */
    static void bootstrap() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.server.ServerAboutToStartEvent.class, event -> {
            var pools = event.getServer().registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
            pools.entrySet().forEach(entry -> {
                ResourceLocation id = entry.getKey().location();
                if (id.getNamespace().equals(Psychedelicraft.VANILLA_EXTENSIONS_NAMESPACE)) {
                    pools.getOptional(ResourceLocation.withDefaultNamespace(id.getPath())).ifPresent(target -> {
                        PoolPair pair = new PoolPair(id);
                        pair.offer(true, entry.getValue());
                        pair.offer(false, target);
                    });
                }
            });
        });
    }

    static MutableStructurePool of(StructureTemplatePool pool) {
        return (MutableStructurePool)pool;
    }

    ObjectArrayList<StructurePoolElement> getElements();

    void setElements(ObjectArrayList<StructurePoolElement> elements);

    List<Pair<StructurePoolElement, Integer>> getElementCounts();

    void setElementCounts(List<Pair<StructurePoolElement, Integer>> elementCounts);

    class PoolPair {
        @Nullable
        private MutableStructurePool source;
        @Nullable
        private MutableStructurePool target;

        PoolPair(ResourceLocation id) {}

        public boolean offer(boolean isSource, StructureTemplatePool pool) {
            if (isSource) {
                source = of(pool);
            } else {
                target = of(pool);
            }
            if (source != null && target != null) {
                ObjectArrayList<StructurePoolElement> elements = new ObjectArrayList<>(target.getElements());
                elements.addAll(source.getElements());
                target.setElements(elements);

                List<Pair<StructurePoolElement, Integer>> elementCounts = new ArrayList<>(target.getElementCounts());
                elementCounts.addAll(source.getElementCounts());
                target.setElementCounts(elementCounts);
                return true;
            }
            return false;
        }
    }
}
