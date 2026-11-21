package ivorius.psychedelicraft.client.render;

import java.util.function.Function;
import java.util.stream.IntStream;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.PSShaders;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ZeroScreen extends RenderType {
    private ZeroScreen() {super(null, null, null, 0, false, false, null, null);}

    public static final ResourceLocation[] TEXTURES = IntStream.range(0, 8)
            .mapToObj(i -> Psychedelicraft.id("textures/entity/reality_rift/zero_screen_" + i + ".png"))
            .toArray(ResourceLocation[]::new);
    public static final float X_PIXELS = 140 / 2F;
    public static final float Y_PIXELS = 224 / 2F;

    protected static final ShaderStateShard ZERO_MATTER_PROGRAM = new ShaderStateShard(PSShaders.ZERO_MATTER);

    private static final Function<ResourceLocation, RenderType> PS_ZERO_SCREEN = Util.memoize(texture -> create("ps_zero_screen",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setLightmapState(NO_LIGHTMAP)
            .setShaderState(ZERO_MATTER_PROGRAM)
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setTextureState(new TextureStateShard(texture, false, false))
            .createCompositeState(false)
    ));

    public static void render(float ticks, Renderable action) {
        int seed = Mth.floor(ticks * 0.5F);
        var rng = RenderUtil.random(seed);
        action.render(
                PS_ZERO_SCREEN.apply(TEXTURES[seed % TEXTURES.length]),
                rng.nextInt(10) * 0.1F * ZeroScreen.X_PIXELS,
                rng.nextInt(8) * 0.125f * ZeroScreen.Y_PIXELS
        );
    }

    @FunctionalInterface
    public interface Renderable {
        void render(RenderType layer, float u, float v);
    }
}
