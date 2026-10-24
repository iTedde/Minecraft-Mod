package dev.craft.mps.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import dev.craft.mps.MpsEnvironment;
import dev.craft.mps.MpsPayloads;
import dev.craft.mps.module.MpsModules;
import dev.craft.mps.module.PowerModule;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

/**
 * MPS {@code KeybindManager} and {@code ClickableKeybinding} (spec 29 §9): free keys with bound toggleable modules,
 * kept in {@code config/craft-mps-keybinds.txt} (lines {@code key:x:y} followed by {@code module~x~y}; GLFW key
 * codes, positions relative to the GUI centre).
 */
public final class MpsKeybinds {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Binding> BINDINGS = new ArrayList<>();
    private static boolean loaded;

    private MpsKeybinds() {
    }

    /** A module icon in the GUI, position relative to the GUI centre. */
    public static final class BoundModule {
        final PowerModule module;
        double x;
        double y;

        BoundModule(PowerModule module, double x, double y) {
            this.module = module;
            this.x = x;
            this.y = y;
        }
    }

    public static final class Binding {
        final int key;
        double x;
        double y;
        final List<BoundModule> modules = new ArrayList<>();
        /** The value of the next press; starts with "off" because new modules are on. */
        private boolean toggleval;
        private boolean toggled;

        Binding(int key, double x, double y) {
            this.key = key;
            this.x = x;
            this.y = y;
        }

        String label() {
            return InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString();
        }

        /** {@code doToggleIf}: toggles once per press. */
        void doToggleIf(boolean value) {
            if (value && !toggled) {
                toggleModules();
            }
            toggled = value;
        }

        /** {@code toggleModules}: local change, chat line and a request to the server for each bound module. */
        private void toggleModules() {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            for (BoundModule bound : modules) {
                String name = bound.module.getName();
                player.displayClientMessage(Component.translatable(
                        toggleval ? "message.craft.mps.toggled_on" : "message.craft.mps.toggled_off",
                        Component.translatable("mps.module." + bound.module.getKey())), false);
                MpsEnvironment.toggleForPlayer(player, name, toggleval);
                PacketDistributor.sendToServer(new MpsPayloads.Toggle(name, toggleval));
            }
            toggleval = !toggleval;
        }
    }

    public static List<Binding> bindings() {
        if (!loaded) {
            loaded = true;
            read();
        }
        return BINDINGS;
    }

    /** {@code ClientTickHandler.tickStart}: the keys only count in game, like vanilla key bindings. */
    public static void tick(Minecraft minecraft) {
        for (Binding binding : bindings()) {
            binding.doToggleIf(minecraft.screen == null
                    && InputConstants.isKeyDown(minecraft.getWindow().getWindow(), binding.key));
        }
    }

    /** Whether a key mapping or another MPS binding already uses the key ("Taken!"). */
    public static boolean isTaken(int key) {
        for (KeyMapping mapping : Minecraft.getInstance().options.keyMappings) {
            InputConstants.Key bound = mapping.getKey();
            if (bound.getType() == InputConstants.Type.KEYSYM && bound.getValue() == key) {
                return true;
            }
        }
        for (Binding binding : bindings()) {
            if (binding.key == key) {
                return true;
            }
        }
        return false;
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("craft-mps-keybinds.txt");
    }

    /** {@code writeOutKeybinds}. */
    public static void write() {
        StringBuilder out = new StringBuilder();
        for (Binding binding : bindings()) {
            out.append(binding.key).append(':').append(binding.x).append(':').append(binding.y).append('\n');
            for (BoundModule bound : binding.modules) {
                out.append(bound.module.getName()).append('~').append(bound.x).append('~').append(bound.y)
                        .append('\n');
            }
        }
        try {
            Files.writeString(file(), out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Problem writing out MPS keybinds", e);
        }
    }

    /** {@code readInKeybinds}: keys already taken and unknown modules are skipped. */
    private static void read() {
        Path file = file();
        if (!Files.exists(file)) {
            return;
        }
        try {
            Binding working = null;
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.contains(":")) {
                    String[] parts = line.split(":");
                    int key = Integer.parseInt(parts[0]);
                    working = isTaken(key) ? null
                            : new Binding(key, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                    if (working != null) {
                        BINDINGS.add(working);
                    }
                } else if (line.contains("~") && working != null) {
                    String[] parts = line.split("~");
                    PowerModule module = MpsModules.get(parts[0]);
                    if (module != null) {
                        working.modules.add(new BoundModule(module, Double.parseDouble(parts[1]),
                                Double.parseDouble(parts[2])));
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Problem reading in MPS keybinds", e);
        }
    }
}
