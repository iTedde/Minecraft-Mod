package dev.craft.ironchest;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;

/**
 * A copper chest at one oxidation stage (spec 35 §1); the waxed stages use this class, the unwaxed ones
 * {@link WeatheringCopperChestBlock}. The stages only change the texture, the chest stays a
 * {@link IronChestType#COPPER}.
 */
public class CopperChestBlock extends IronChestBlock {
    private final WeatherState weatherState;
    private final MapCodec<CopperChestBlock> codec;

    public CopperChestBlock(WeatherState weatherState, Properties properties) {
        super(IronChestType.COPPER, properties);
        this.weatherState = weatherState;
        this.codec = simpleCodec(p -> new CopperChestBlock(weatherState, p));
    }

    public WeatherState getWeatherState() {
        return weatherState;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }
}
