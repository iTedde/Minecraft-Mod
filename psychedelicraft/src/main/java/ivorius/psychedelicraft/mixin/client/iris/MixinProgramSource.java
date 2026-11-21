package ivorius.psychedelicraft.mixin.client.iris;

import java.util.Locale;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.shaders.Program;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.shader.GeometryShader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Injects the Psychedelicraft geometry code into the vertex and fragment sources of Iris shader-pack programs.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.shaderpack.programs.ProgramSource", remap = false)
abstract class MixinProgramSource {
    @Shadow
    private @Final String name;

    @Unique
    private @Nullable Optional<String> psychedelicraft_vertexSource;
    @Unique
    private @Nullable Optional<String> psychedelicraft_fragmentSource;

    @Inject(method = "getVertexSource()Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void onGetVertexSource(CallbackInfoReturnable<Optional<String>> info) {
        if (!PsychedelicraftClient.getConfig().irisSupport.get()) {
            return;
        }
        if (psychedelicraft_vertexSource == null) {
            if (!psychedelicraft_canInject(info.getReturnValue())) {
                return;
            }
            GeometryShader.INSTANCE.setup(Program.Type.VERTEX, psychedelicraft_programId());
            psychedelicraft_vertexSource = Optional.ofNullable(GeometryShader.INSTANCE.injectShaderSources(info.getReturnValue().orElse(null)));
        }
        info.setReturnValue(psychedelicraft_vertexSource);
    }

    @Inject(method = "getFragmentSource()Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void onGetFragmentSource(CallbackInfoReturnable<Optional<String>> info) {
        if (!PsychedelicraftClient.getConfig().irisSupport.get()) {
            return;
        }
        if (psychedelicraft_fragmentSource == null) {
            if (!psychedelicraft_canInject(info.getReturnValue())) {
                return;
            }
            GeometryShader.INSTANCE.setup(Program.Type.FRAGMENT, psychedelicraft_programId());
            psychedelicraft_fragmentSource = Optional.ofNullable(GeometryShader.INSTANCE.injectShaderSources(info.getReturnValue().orElse(null)));
        }
        info.setReturnValue(psychedelicraft_fragmentSource);
    }

    @Unique
    private static boolean psychedelicraft_warnedEarly;

    @Unique
    private static boolean psychedelicraft_canInject(Optional<String> source) {
        if (Minecraft.getInstance().getResourceManager() == null) {
            // Iris reads every program once while loading the pack; injection follows when the pipeline is built
            if (source.isPresent() && !psychedelicraft_warnedEarly) {
                psychedelicraft_warnedEarly = true;
                Psychedelicraft.LOGGER.info("Iris is initialising too early, Psychedelicraft injects when the pipeline is built");
            }
            return false;
        }
        return true;
    }

    @Unique
    private ResourceLocation psychedelicraft_programId() {
        // shader-pack program names are free text; fall back to a fixed id instead of crashing on odd characters
        ResourceLocation id = ResourceLocation.tryParse(name.toLowerCase(Locale.ROOT));
        return (id == null ? ResourceLocation.withDefaultNamespace("unknown") : id).withPrefix("iris/");
    }
}
