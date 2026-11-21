package ivorius.psychedelicraft.client.render.shader;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

class ModdedResourceManager implements ResourceManager {
    private final ResourceManager parent;
    private final String defaultNamespace;
    private final ModdedResourceFactory proxy;

    public ModdedResourceManager(ResourceManager parent, String defaultNamespace) {
        this.parent = parent;
        this.defaultNamespace = defaultNamespace;
        this.proxy = new ModdedResourceFactory(parent, defaultNamespace);
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation id) {
        return proxy.getResource(id);
    }

    @Override
    public Set<String> getNamespaces() {
        return parent.getNamespaces();
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation id) {
        List<Resource> resources = parent.getResourceStack(ResourceLocation.fromNamespaceAndPath(defaultNamespace, id.getPath()));
        if (resources.isEmpty()) {
            return parent.getResourceStack(id);
        }
        return resources;
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String path, Predicate<ResourceLocation> filter) {
        return parent.listResources(path, filter);
    }

    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String path, Predicate<ResourceLocation> filter) {
        return parent.listResourceStacks(path, filter);
    }

    @Override
    public Stream<PackResources> listPacks() {
        return parent.listPacks();
    }
}
