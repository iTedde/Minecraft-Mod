package ivorius.psychedelicraft.mixin.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.Program.Type;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.renderer.ShaderInstance;

@Mixin(ShaderInstance.class)
abstract class MixinShaderProgram implements Shader, AutoCloseable {
    @Shadow
    private @Final List<Uniform> uniforms;
    @Shadow
    private @Final Map<String, Object> samplerMap;
    @Shadow
    private @Final List<String> samplerNames;

    @Inject(method = "apply()V", at = @At("HEAD"))
    private void onBind(CallbackInfo info) {
        GeometryShader.INSTANCE.getSamplers().forEach((name, sampler) -> samplerMap.put(name, sampler.get()));
    }

    @Inject(method = "updateLocations()V", at = @At("HEAD"))
    private void onLoadReferences(CallbackInfo info) {
        RenderSystem.assertOnRenderThread();
        GeometryShader.INSTANCE.getSamplers().keySet().forEach(samplerName -> {
            if (Uniform.glGetUniformLocation(getId(), samplerName) != -1) {
                samplerNames.add(samplerName);
                samplerMap.put(samplerName, null);
            }
        });
        GeometryShader.INSTANCE.addUniforms(this, uniform -> {
            if (Uniform.glGetUniformLocation(getId(), uniform.getName()) != -1) {
                uniforms.add(uniform);
            }
        });
    }
}

@Mixin(GlslPreprocessor.class)
abstract class MixinGLImportProcessor {
    @ModifyVariable(method = "process(Ljava/lang/String;)Ljava/util/List;", at = @At("HEAD"), argsOnly = true)
    private String modifySource(String source) {
        return GeometryShader.INSTANCE.injectShaderSources(source);
    }
}

@Mixin(Program.class)
abstract class MixinShaderStage {
    @Inject(method = "compileShaderInternal", at = @At("HEAD"))
    private static void onLoad(Type type, String name, InputStream stream, String domain, GlslPreprocessor loader, CallbackInfoReturnable<Integer> info) throws IOException {
        GeometryShader.INSTANCE.setup(type, domain, name);
    }
}
