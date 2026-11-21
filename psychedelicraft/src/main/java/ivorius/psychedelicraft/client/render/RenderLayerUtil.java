package ivorius.psychedelicraft.client.render;

import java.util.Optional;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Reads the texture of a render type (upstream: Kirin {@code RenderLayerUtil}).
 */
public interface RenderLayerUtil {
    static Optional<ResourceLocation> getTexture(RenderType layer) {
        if (layer instanceof RenderType.CompositeRenderType composite) {
            return composite.state().textureState.cutoutTexture();
        }
        return Optional.empty();
    }
}
