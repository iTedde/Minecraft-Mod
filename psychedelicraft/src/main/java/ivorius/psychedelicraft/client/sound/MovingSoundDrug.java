package ivorius.psychedelicraft.client.sound;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

/**
 * Created by lukas on 22.11.14.
 */
public class MovingSoundDrug extends AbstractTickableSoundInstance {
    private DrugProperties properties;
    private DrugType<?> drugType;

    private float prevVolume;

    public MovingSoundDrug(DrugProperties properties, DrugType<?> drugType, float initialVolume) {
        super(drugType.soundEvent(), SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.prevVolume = initialVolume;
        this.volume = initialVolume;
        this.relative = true;
        this.properties = properties;
        this.drugType = drugType;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public void markCompleted() {
        stop();
    }

    public DrugType<?> getType() {
        return drugType;
    }

    @Override
    public void tick() {
        prevVolume = volume;
        volume = MathUtils.approach(volume, getTargetVolume(), 0.1F);

        if (isStopped()) {
            stop();
        }
    }

    @Override
    public boolean isStopped() {
        return super.isStopped() || Mth.equal(volume, 0) || properties.asEntity().isRemoved() || Minecraft.getInstance().level == null;
    }

    @Override
    public final float getVolume() {
        float tickDelta = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        return Mth.lerp(tickDelta, prevVolume, volume) * sound.getVolume().sample(random);
    }

    private float getTargetVolume() {
        if (!PsychedelicraftClient.getConfig().drugsBackgroundMusic.get()) {
            return 0;
        }
        return Mth.clamp(properties.getDrugValue(drugType) - PsychedelicraftClient.getConfig().getBGMThreshold(), 0, 1);
    }
}
