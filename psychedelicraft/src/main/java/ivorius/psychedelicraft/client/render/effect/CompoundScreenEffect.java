package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CompoundScreenEffect implements ScreenEffect {

    private final List<ScreenEffect> effects = new ArrayList<>();

    private final Minecraft client = Minecraft.getInstance();

    public static ScreenEffect of(ScreenEffect... effects) {
        return new CompoundScreenEffect().add(effects);
    }

    private CompoundScreenEffect() { }

    public CompoundScreenEffect add(ScreenEffect... effects) {
        for (ScreenEffect effect : effects) {
            add(effect);
        }
        return this;
    }

    public CompoundScreenEffect add(ScreenEffect effect) {
        if (effect instanceof CompoundScreenEffect comp) {
            effects.addAll(comp.effects);
        } else {
            effects.add(effect);
        }
        return this;
    }

    @Override
    public boolean shouldApply(float tickDelta) {
        return client.player != null;
    }

    @Override
    public void update(float tickDelta) {
        effects.forEach(effect -> effect.update(tickDelta));
    }

    @Override
    public void render(GuiGraphics context, Window window, float tickDelta) {
        effects.forEach(effect -> {
            if (effect.shouldApply(tickDelta)) {
                effect.render(context, window, tickDelta);
            }
        });
    }

    @Override
    public void close() {
        effects.forEach(e -> {
            try {
                e.close();
            } catch (Exception ignored) {}
        });
        effects.clear();
    }
}
