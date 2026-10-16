package dev.craft.ic2.machine;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * IC2 {@code TileEntityTeleporter}: with a redstone signal and a linked teleporter it sends the closest entity on top
 * to the target, paid from adjacent BatBoxes/MFEs/MFSUs (spec 04, teleporter).
 */
public class TeleporterBlockEntity extends MachineBlockEntity {
    /** Config {@code enableTeleporterInventory=true} in the pack. */
    public static final boolean INVENTORY_WEIGHT = true;
    private static final DustParticleOptions BLUE = new DustParticleOptions(new Vector3f(0.0F, 0.0F, 1.0F), 1.0F);

    @Nullable
    private BlockPos target;

    public TeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.TELEPORTER), pos, state, 0);
    }

    @Nullable
    public BlockPos getTarget() {
        return target;
    }

    public void setTarget(BlockPos target) {
        this.target = target.immutable();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void serverTick() {
        if (level.hasNeighborSignal(worldPosition) && target != null) {
            // IC2 loads the target chunk if needed; getBlockState does the same on the server.
            if (level.getBlockState(target).is(Ic2Blocks.machine(Ic2Machine.TELEPORTER).get())) {
                setActive(true);
                List<Entity> nearby = level.getEntitiesOfClass(Entity.class, new AABB(worldPosition.getX() - 1,
                        worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2,
                        worldPosition.getY() + 3, worldPosition.getZ() + 2));
                if (!nearby.isEmpty()) {
                    double minDistance = Double.MAX_VALUE;
                    Entity closest = null;
                    for (Entity entity : nearby) {
                        if (entity.getVehicle() == null) {
                            double dx = worldPosition.getX() - entity.getX();
                            double dy = worldPosition.getY() + 1 - entity.getY();
                            double dz = worldPosition.getZ() - entity.getZ();
                            double distance = dx * dx + dy * dy + dz * dz;
                            if (distance < minDistance) {
                                minDistance = distance;
                                closest = entity;
                            }
                        }
                    }
                    if (closest != null) {
                        teleport(closest);
                    }
                }
            } else {
                target = null;
                setChanged();
                setActive(false);
            }
        } else {
            setActive(false);
        }
    }

    @Override
    public void clientTick() {
        MachineSoundHooks.updateLoop(this, Ic2Sounds.TELE_CHARGED_LOOP.get(), isActive());
        if (isActive()) {
            spawnBlueParticles(2, worldPosition);
        }
    }

    private void teleport(Entity user) {
        double dx = worldPosition.getX() - target.getX();
        double dy = worldPosition.getY() - target.getY();
        double dz = worldPosition.getZ() - target.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int weight = getWeightOf(user);
        if (weight == 0) {
            return;
        }
        int energyCost = (int) (weight * Math.pow(distance + 10.0, 0.7) * 5.0);
        if (energyCost > getAvailableEnergy()) {
            return;
        }
        consumeEnergy(energyCost);
        double x = target.getX() + 0.5;
        double y = target.getY() + 1.5;
        double z = target.getZ() + 0.5;
        if (user instanceof ServerPlayer player) {
            player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
        } else {
            user.moveTo(x, y, z, user.getYRot(), user.getXRot());
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
    }

    /** IC2 network event 0: use sound and 20 particles at both teleporters. */
    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 0 && level != null && level.isClientSide()) {
            level.playLocalSound(worldPosition, Ic2Sounds.TELE_USE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            spawnBlueParticles(20, worldPosition);
            if (target != null) {
                level.playLocalSound(target, Ic2Sounds.TELE_USE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
                spawnBlueParticles(20, target);
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    private void spawnBlueParticles(int n, BlockPos pos) {
        for (int i = 0; i < n; i++) {
            level.addParticle(BLUE, pos.getX() + level.random.nextFloat(), pos.getY() + 1 + level.random.nextFloat(),
                    pos.getZ() + level.random.nextFloat(), 0.0, 0.0, 0.0);
            level.addParticle(BLUE, pos.getX() + level.random.nextFloat(), pos.getY() + 2 + level.random.nextFloat(),
                    pos.getZ() + level.random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }

    // --- energy from adjacent storage blocks ---

    private List<ElectricStorageBlockEntity> adjacentStorage() {
        List<ElectricStorageBlockEntity> storage = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ElectricStorageBlockEntity block) {
                storage.add(block);
            }
        }
        return storage;
    }

    public int getAvailableEnergy() {
        int energy = 0;
        for (ElectricStorageBlockEntity storage : adjacentStorage()) {
            energy += storage.getEnergy();
        }
        return energy;
    }

    /** IC2 {@code consumeEnergy}: equal shares (rounded up) from every storage that still holds energy. */
    private void consumeEnergy(int energy) {
        List<ElectricStorageBlockEntity> sources = new ArrayList<>();
        for (ElectricStorageBlockEntity storage : adjacentStorage()) {
            if (storage.getEnergy() > 0) {
                sources.add(storage);
            }
        }
        while (energy > 0 && !sources.isEmpty()) {
            int drain = (energy + sources.size() - 1) / sources.size();
            Iterator<ElectricStorageBlockEntity> it = sources.iterator();
            while (it.hasNext()) {
                ElectricStorageBlockEntity source = it.next();
                if (drain > energy) {
                    drain = energy;
                }
                if (source.getEnergy() <= drain) {
                    energy -= source.getEnergy();
                    source.setEnergy(0);
                    it.remove();
                } else {
                    energy -= drain;
                    source.setEnergy(source.getEnergy() - drain);
                }
            }
        }
    }

    /** IC2 {@code getWeightOf}: the entity and every rider stacked on it. */
    public static int getWeightOf(Entity user) {
        int weight = 0;
        for (Entity entity = user; entity != null; entity = entity.getFirstPassenger()) {
            if (entity instanceof ItemEntity item) {
                ItemStack stack = item.getItem();
                weight += 100 * stack.getCount() / stack.getMaxStackSize();
            } else if (entity instanceof Animal || entity instanceof AbstractMinecart || entity instanceof Boat) {
                weight += 100;
            } else if (entity instanceof Player player) {
                weight += 1000;
                if (INVENTORY_WEIGHT) {
                    for (ItemStack stack : player.getInventory().items) {
                        weight += stackWeight(stack);
                    }
                    for (ItemStack stack : player.getInventory().offhand) {
                        weight += stackWeight(stack);
                    }
                }
            } else if (entity instanceof Ghast) {
                weight += 2500;
            } else if (entity instanceof EnderDragon) {
                weight += 10000;
            } else if (entity instanceof PathfinderMob) {
                weight += 500;
            }
            if (INVENTORY_WEIGHT && entity instanceof LivingEntity living) {
                // IC2 slots 0–4: held item (not for players, already in the inventory) and the four armour pieces.
                if (!(entity instanceof Player)) {
                    weight += stackWeight(living.getItemBySlot(EquipmentSlot.MAINHAND));
                }
                for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.FEET, EquipmentSlot.LEGS,
                        EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                    weight += stackWeight(living.getItemBySlot(slot));
                }
            }
        }
        return weight;
    }

    private static int stackWeight(ItemStack stack) {
        return stack.isEmpty() ? 0 : 100 * stack.getCount() / stack.getMaxStackSize();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return null;
    }

    // --- persistence and sync (IC2 networked fields targetX/Y/Z) ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        target = tag.getBoolean("targetSet")
                ? new BlockPos(tag.getInt("targetX"), tag.getInt("targetY"), tag.getInt("targetZ")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("targetSet", target != null);
        if (target != null) {
            tag.putInt("targetX", target.getX());
            tag.putInt("targetY", target.getY());
            tag.putInt("targetZ", target.getZ());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
