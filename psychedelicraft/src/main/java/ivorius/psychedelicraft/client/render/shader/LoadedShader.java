package ivorius.psychedelicraft.client.render.shader;

import java.io.IOException;
import java.util.*;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import ivorius.psychedelicraft.client.render.shader.UniformBinding.UniformSetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

class LoadedShader extends PostChain {
    private final UniformBinding.Set bindings;

    private int width;
    private int height;
    private float time;
    private float lastTickDelta;

    private List<Pass> passes;

    public LoadedShader(Minecraft client, ResourceLocation id, UniformBinding.Set bindings) throws IOException, JsonSyntaxException {
        super(client.getTextureManager(), new ModdedResourceManager(client.getResourceManager(), id.getNamespace()), client.getMainRenderTarget(), id);
        this.bindings = bindings;
        if (passes == null) {
            passes = new ArrayList<>();
        }
        resize(
            client.getWindow().getWidth(),
            client.getWindow().getHeight()
        );
    }

    @Override
    public void process(float tickDelta) {
        if (tickDelta < lastTickDelta) {
            time += 1 - lastTickDelta;
            time += tickDelta;
        } else {
            time += tickDelta - lastTickDelta;
        }
        this.lastTickDelta = tickDelta;
        while (time > 20) {
            time -= 20;
        }

        boolean rendered = false;
        final float passRenderFrame = time / 20F;
        for (Pass pass : passes) {
            rendered |= pass.render(passRenderFrame, tickDelta, rendered);
        }
    }

    @Override
    public void resize(int targetsWidth, int targetsHeight) {
        this.width = targetsWidth;
        this.height = targetsHeight;
        super.resize(targetsWidth, targetsHeight);
    }

    @Override
    public PostPass addPass(String programName, RenderTarget source, RenderTarget dest, boolean linear) throws IOException {
        PostPass pass = super.addPass(programName, source, dest, linear);
        if (passes == null) {
            passes = new ArrayList<>();
        }
        passes.add(new Pass(pass));
        return pass;
    }

    class Pass implements UniformSetter {
        private final EffectInstance program;
        private final PostPass pass;

        private final List<FloatConsumer> replay = new ArrayList<>();
        private int updateCount;

        private boolean rendered;

        public Pass(PostPass pass) {
            this.pass = pass;
            this.program = pass.getEffect();
        }

        public boolean render(float passRenderTime, float tickDelta, boolean rendered) {
            if (pass.getName().equals("blit")) {
                return false;
            }

            if (updateCount == 0) {
                replay.clear();
                var programBindings = bindings.programBindings.getOrDefault(pass.getName(), UniformBinding.EMPTY);
                bindings.global.bindUniforms(this, tickDelta, width, height, () -> {
                    programBindings.bindUniforms(this, tickDelta, width, height, () -> {
                        replay.add(this::renderPass);
                    });
                });
            }

            updateCount = (updateCount + 1) % 2;
            this.rendered = false;
            if (replay.isEmpty()) {
                return this.rendered;
            }

            try {
                for (FloatConsumer action : replay) {
                    action.accept(passRenderTime);
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception rendering shader " + pass.getName(), t);
            }
            return this.rendered;
        }

        private void renderPass(float passRenderTime) {
            pass.process(passRenderTime);
            passes.get(passes.size() - 1).pass.process(passRenderTime);
            rendered = true;
        }

        @Override
        public void set(String name, float value) {
            var uniform = program.getUniform(name);
            if (uniform != null) {
                replay.add(uniformSetter(name, f -> uniform.set(value)));
            }
        }

        @Override
        public void set(String name, float... values) {
            var uniform = program.getUniform(name);
            if (uniform != null) {
                var copy = Arrays.copyOf(values, values.length);
                replay.add(uniformSetter(name, f -> uniform.set(copy)));
            }
        }

        @Override
        public void set(String name, Vector3fc values) {
            var uniform = program.getUniform(name);
            if (uniform != null) {
                var copy = new Vector3f(values);
                replay.add(uniformSetter(name, f -> uniform.set(copy)));
            }
        }

        @Override
        public void set(String name, Vector4fc values) {
            var uniform = program.getUniform(name);
            if (uniform != null) {
                var copy = new Vector4f(values);
                replay.add(uniformSetter(name, f -> uniform.set(copy)));
            }
        }

        private static FloatConsumer uniformSetter(String name, FloatConsumer consumer) {
            return f -> {
                try {
                    consumer.accept(f);
                } catch (Throwable t) {
                    throw new RuntimeException("Exception setting uniform: " + name, t);
                }
            };
        }
    }
}