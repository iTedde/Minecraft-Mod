package ivorius.psychedelicraft;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.neoforged.fml.loading.LoadingModList;

/**
 * Applies the optional Sodium/Iris compatibility mixins only when those mods are present.
 */
public class PsychedelicraftMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE = "ivorius.psychedelicraft.mixin";

    private boolean hasSodium;
    private boolean hasIris;

    @Override
    public void onLoad(String mixinPackage) {
        hasSodium = isLoaded("sodium");
        hasIris = isLoaded("iris");
    }

    private static boolean isLoaded(String modId) {
        LoadingModList list = LoadingModList.get();
        return list != null && list.getModFileById(modId) != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(MIXIN_PACKAGE + ".client.sodium.")) {
            return hasSodium;
        }
        if (mixinClassName.startsWith(MIXIN_PACKAGE + ".client.iris.")) {
            return hasIris;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
