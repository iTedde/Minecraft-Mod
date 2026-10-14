package dev.craft.ic2.generator;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.menu.SolarPanelMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

/** Solar panel, 1:1 with IC2 {@code TileEntitySolarGenerator}: 1 EU/t while the sun is visible. */
public class SolarPanelBlockEntity extends BaseGeneratorBlockEntity {
    /** {@code energyGeneratorSolar} from the pack's IC2.cfg. */
    public static final int ENERGY_GENERATOR_SOLAR = 100;
    public static final int TICK_RATE = 128;

    private int ticker;
    private boolean sunIsVisible;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.SOLAR_PANEL), pos, state, 1, 1, 1);
        this.ticker = RANDOM.nextInt(TICK_RATE);
    }

    public boolean isSunVisible() {
        return sunIsVisible;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            updateSunVisibility();
        }
    }

    @Override
    public boolean gainEnergy() {
        if (ticker++ % TICK_RATE == 0) {
            updateSunVisibility();
        }
        if (!sunIsVisible) {
            return false;
        }
        double gen = ENERGY_GENERATOR_SOLAR / 100.0;
        if (gen >= 1.0 || ticker % (100 - ENERGY_GENERATOR_SOLAR) == 0) {
            storage += (int) Math.ceil(gen);
        }
        return true;
    }

    @Override
    public boolean gainFuel() {
        return false;
    }

    @Override
    public boolean needsFuel() {
        return true;
    }

    @Override
    public boolean delayActiveUpdate() {
        return true;
    }

    public void updateSunVisibility() {
        sunIsVisible = isSunVisible(level, worldPosition.above());
    }

    /** Daytime, sky light, open sky above and no rain – deserts ignore rain. */
    public static boolean isSunVisible(Level level, BlockPos pos) {
        return level.isDay()
                && level.dimensionType().hasSkyLight()
                && level.canSeeSky(pos)
                && (level.getBiome(pos).is(Tags.Biomes.IS_DESERT) || !level.isRaining() && !level.isThundering());
    }

    @Override
    protected int getSlotForSide(Direction side) {
        return 0;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new SolarPanelMenu(id, playerInventory, this);
    }
}
