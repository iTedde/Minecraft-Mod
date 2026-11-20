package ivorius.psychedelicraft.mixin.client.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.shaders.Program;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.resources.ResourceLocation;

/**
 * Injects the Psychedelicraft geometry code (waves, fractures) into the Sodium chunk shaders.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader", remap = false)
abstract class MixinShaderLoader {
    @Inject(method = "loadShader(Lnet/caffeinemc/mods/sodium/client/gl/shader/ShaderType;Lnet/minecraft/resources/ResourceLocation;Lnet/caffeinemc/mods/sodium/client/gl/shader/ShaderConstants;)Lnet/caffeinemc/mods/sodium/client/gl/shader/GlShader;", at = @At("HEAD"))
    private static void onLoadShader(@Coerce Object type, ResourceLocation name, @Coerce Object constants, CallbackInfoReturnable<?> info) {
        if (PsychedelicraftClient.getConfig().sodiumSupport.get()) {
            GeometryShader.INSTANCE.setup("VERTEX".contentEquals(type.toString()) ? Program.Type.VERTEX : Program.Type.FRAGMENT, name);
        }
    }

    @ModifyReturnValue(method = "getShaderSource(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/String;", at = @At("RETURN"))
    private static String modifyShaderSources(String sources, ResourceLocation name) {
        if (PsychedelicraftClient.getConfig().sodiumSupport.get()) {
            return GeometryShader.INSTANCE.injectShaderSources(sources);
        }
        return sources;
    }
}
