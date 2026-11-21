package dev.craft.ironchest;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An unwaxed copper chest that oxidizes like a copper block (spec 35 §1). The next stage, waxing, scraping and
 * lightning come from the NeoForge {@code oxidizables}/{@code waxables} data maps.
 */
public class WeatheringCopperChestBlock extends CopperChestBlock implements WeatheringCopper {
    private final MapCodec<WeatheringCopperChestBlock> codec;

    public WeatheringCopperChestBlock(WeatherState weatherState, Properties properties) {
        super(weatherState, properties);
        this.codec = simpleCodec(p -> new WeatheringCopperChestBlock(weatherState, p));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    public WeatherState getAge() {
        return getWeatherState();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return WeatheringCopper.getNext(state.getBlock()).isPresent();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        changeOverTime(state, level, pos, random);
    }
}
