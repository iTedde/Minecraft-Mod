package ivorius.psychedelicraft.client.render.shader;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.client.renderer.ShaderInstance;

public interface PSShaders {
    @Nullable
    Supplier<ShaderInstance> ZERO_MATTER = register("rendertype_zero_matter", DefaultVertexFormat.POSITION_COLOR);

    java.util.List<java.util.function.Consumer<net.neoforged.neoforge.client.event.RegisterShadersEvent>> REGISTRATIONS = new java.util.ArrayList<>();

    static void bootstrap() {}

    static void registerShaders(net.neoforged.neoforge.client.event.RegisterShadersEvent event) {
        REGISTRATIONS.forEach(r -> r.accept(event));
    }

    private static Supplier<ShaderInstance> register(String name, VertexFormat format) {
        AtomicReference<ShaderInstance> program = new AtomicReference<>(null);
        REGISTRATIONS.add(event -> {
            try {
                event.registerShader(new ShaderInstance(event.getResourceProvider(), Psychedelicraft.id(name), format), program::set);
            } catch (java.io.IOException e) {
                Psychedelicraft.LOGGER.error("Could not load core shader {}", name, e);
            }
        });
        return program::get;
    }
}
