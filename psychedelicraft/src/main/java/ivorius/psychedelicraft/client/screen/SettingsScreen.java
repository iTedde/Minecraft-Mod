/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import java.util.Arrays;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.PSClientConfig;
import ivorius.psychedelicraft.config.MessageDistortion;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.util.settings.Setting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/**
 * Psychedelicraft options. Upstream used Kirin widgets; this port uses the vanilla options list.
 */
public class SettingsScreen extends OptionsSubScreen {
    private final PSClientConfig config;
    @Nullable
    private final PSConfig serverConfig;

    public SettingsScreen(@Nullable Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable("gui.psychedelicraft.options.title"));
        Minecraft client = Minecraft.getInstance();
        config = PsychedelicraftClient.getConfig();
        serverConfig = client.level == null || client.hasSingleplayerServer() ? Psychedelicraft.getConfig() : null;
    }

    @Override
    protected void addOptions() {
        header("gui.psychedelicraft.options.shaders");
        list.addSmall(
                toggle("gui.psychedelicraft.option.shaders_2d", config.shader2DEnabled),
                toggle("gui.psychedelicraft.option.shaders_3d", config.shader3DEnabled),
                toggle("gui.psychedelicraft.option.heat_distortion", config.doHeatDistortion),
                toggle("gui.psychedelicraft.option.water_distortion", config.doWaterDistortion),
                toggle("gui.psychedelicraft.option.motion_blur", config.doMotionBlur)
        );

        header("gui.psychedelicraft.options.dof");
        list.addSmall(
                slider("gui.psychedelicraft.option.focal_point.near", 0, 99, config.dofFocalPointNear),
                slider("gui.psychedelicraft.option.focal_point.blur", 0, 8, config.dofFocalBlurNear),
                slider("gui.psychedelicraft.option.focal_point.far", 100, 400, config.dofFocalPointFar),
                slider("gui.psychedelicraft.option.focal_point.blur", 0, 8, config.dofFocalBlurFar)
        );

        header("gui.psychedelicraft.options.overlays");
        list.addSmall(
                toggle("gui.psychedelicraft.option.water_overlay", config.waterOverlayEnabled),
                toggle("gui.psychedelicraft.option.hurt_overlay", config.hurtOverlayEnabled),
                slider("gui.psychedelicraft.option.sun_glare_intensity", 0, 1, config.sunFlareIntensity)
        );

        header("gui.psychedelicraft.options.compatibility");
        list.addSmall(
                toggle("gui.psychedelicraft.option.shaders_iris", config.irisSupport),
                toggle("gui.psychedelicraft.option.shaders_sodium", config.sodiumSupport)
        );

        header("gui.psychedelicraft.options.sounds");
        list.addSmall(
                toggle("gui.psychedelicraft.option.themes", config.drugsBackgroundMusic),
                slider("gui.psychedelicraft.option.themes_threshold", 0.01F, 1F, config.drugsBackgroundMusicThreshold)
        );

        if (serverConfig != null) {
            header("gui.psychedelicraft.options.gameplay");
            list.addSmall(
                    enumOption("gui.psychedelicraft.options.message_distortion", MessageDistortion.class, serverConfig.messageDistortion),
                    toggle("gui.psychedelicraft.option.gameplay.harmonium", serverConfig.enableHarmonium),
                    toggle("gui.psychedelicraft.option.gameplay.rift_jars", serverConfig.enableRiftJars),
                    OptionInstance.createBoolean("gui.psychedelicraft.option.gameplay.molotovs", !serverConfig.disableMolotovs.get(),
                            z -> serverConfig.disableMolotovs.set(!z)),
                    slider("gui.psychedelicraft.option.gameplay.rift_spawnrate", 0, 1800,
                            serverConfig.randomTicksUntilRiftSpawn.get() / (float)PSConfig.MINUTE,
                            z -> serverConfig.randomTicksUntilRiftSpawn.set((int)(z * PSConfig.MINUTE)))
            );
        }
    }

    private void header(String key) {
        list.addSmall(new StringWidget(Component.translatable(key).withStyle(ChatFormatting.YELLOW), font), null);
    }

    private static OptionInstance<Boolean> toggle(String key, Setting<Boolean> setting) {
        return OptionInstance.createBoolean(key, setting.get(), setting::set);
    }

    private static OptionInstance<Float> slider(String key, float min, float max, Setting<Float> setting) {
        return slider(key, min, max, setting.get(), setting::set);
    }

    private static OptionInstance<Float> slider(String key, float min, float max, float value, Consumer<Float> setter) {
        return new OptionInstance<>(key, OptionInstance.noTooltip(),
                (caption, v) -> formatSliderValue(caption, v),
                OptionInstance.UnitDouble.INSTANCE.xmap(d -> (float)(min + d * (max - min)), v -> (v - min) / (max - min)),
                Codec.FLOAT,
                value, setter);
    }

    private static <T extends Enum<T>> OptionInstance<T> enumOption(String key, Class<T> type, Setting<T> setting) {
        T[] values = type.getEnumConstants();
        Codec<T> codec = Codec.STRING.xmap(name -> Enum.valueOf(type, name), Enum::name);
        return new OptionInstance<>(key, OptionInstance.noTooltip(),
                (caption, v) -> Component.literal(v.name()),
                new OptionInstance.Enum<>(Arrays.asList(values), codec),
                setting.get(), setting::set);
    }

    @Override
    public void removed() {
        config.save();
        if (serverConfig != null) {
            serverConfig.save();
        }
    }

    private static Component formatSliderValue(Component label, float value) {
        if (value < 0.001F) {
            return Component.translatable("gui.psychedelicraft.slider.value.off", label);
        }
        return Component.translatable("gui.psychedelicraft.slider.value", label, Math.round(value * 100F) / 100F);
    }
}
