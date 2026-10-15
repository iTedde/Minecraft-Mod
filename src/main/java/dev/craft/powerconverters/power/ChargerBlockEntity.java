package dev.craft.powerconverters.power;

import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.powerconverters.PcBlockEntityTypes;
import dev.craft.powerconverters.PowerSystem;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Power Converters {@code TileEntityCharger}: charges the IC2 electric items of the player standing on it, otherwise
 * those in the adjacent inventories. Only {@code ChargeHandlerIndustrialCraft} exists here.
 */
public class ChargerBlockEntity extends EnergyProducerBlockEntity<IItemHandler> {
    private @Nullable Player player;

    public ChargerBlockEntity(BlockPos pos, BlockState state) {
        super(PcBlockEntityTypes.UNIVERSAL_CHARGER.get(), pos, state, PowerSystem.INDUSTRIALCRAFT, 0);
    }

    @Override
    protected @Nullable IItemHandler findAdjacent(Direction side, BlockPos neighbor) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, neighbor, side.getOpposite());
    }

    @Override
    protected void tick() {
        if (player != null && (player.isRemoved() || Math.sqrt(player.distanceToSqr(worldPosition.getX(),
                worldPosition.getY(), worldPosition.getZ())) > 2.0)) {
            setPlayer(null);
        }
    }

    public void setPlayer(@Nullable Player player) {
        if (this.player != player) {
            this.player = player;
            updateConnectedSides();
        }
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    @Override
    public int produceEnergy(int energy) {
        if (energy == 0) {
            return 0;
        }
        int energyRemaining = energy;
        if (player != null) {
            energyRemaining = chargeInventory(new PlayerInvWrapper(player.getInventory()), energyRemaining);
        }
        if (energyRemaining < energy) {
            return energyRemaining;
        }
        for (Map.Entry<Direction, IItemHandler> inventory : getTiles().entrySet()) {
            energyRemaining = chargeInventory(inventory.getValue(), energyRemaining);
            if (energyRemaining < energy) {
                return energyRemaining;
            }
        }
        return energyRemaining;
    }

    /** The handler of an adjacent inventory already exposes only the slots of the side facing the charger. */
    private int chargeInventory(IItemHandler inventory, int energy) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ElectricItem item) {
                int energyRemaining = charge(stack, item, energy);
                if (energyRemaining < energy) {
                    if (inventory instanceof IItemHandlerModifiable modifiable) {
                        modifiable.setStackInSlot(slot, stack);
                    }
                    powerSystem = PowerSystem.INDUSTRIALCRAFT;
                    return energyRemaining;
                }
            }
        }
        return energy;
    }

    /** {@code ChargeHandlerIndustrialCraft.charge}: the PCU below one EU are lost. */
    private int charge(ItemStack stack, ElectricItem item, int energyInput) {
        int perOutput = PowerSystem.INDUSTRIALCRAFT.getEnergyPerOutput();
        int eu = energyInput / perOutput;
        eu -= ElectricItems.charge(stack, eu, item.getTier(), false, false);
        return eu * perOutput;
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() || player != null;
    }

    @Override
    public boolean isSideConnected(Direction side) {
        return side == Direction.UP && player != null || super.isSideConnected(side);
    }
}
