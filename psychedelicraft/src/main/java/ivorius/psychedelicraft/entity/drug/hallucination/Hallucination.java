/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

public abstract class Hallucination {

    public static final int UNLIMITED = -1;

    protected final Player player;

    protected int age;

    protected Optional<ChatBot> chatBot = Optional.empty();

    public Hallucination(Player player) {
        this.player = player;
    }

    public void update() {
        age++;
        chatBot.ifPresent(ChatBot::tick);
    }

    public Optional<ChatBot> getChatBot() {
        return chatBot;
    }

    public abstract void render(PoseStack matrices, MultiBufferSource vertices, Camera camera, float tickDelta, float alpha);

    public abstract boolean isDead();

    public void setDead() {
        age = Integer.MAX_VALUE;
    }

    public abstract int getMaxHallucinations();
}
