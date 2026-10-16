package dev.craft.buildcraft.transport;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * An item travelling through item pipes ({@code EntityPassiveItem} plus the per-pipe {@code EntityData}). The position
 * is absolute; {@code input}, {@code output}, {@code toCenter} and the blacklist belong to the pipe it is in and are
 * reset when it enters the next one.
 */
public final class TravelingItem {
    public static final float NORMAL_SPEED = 0.01F;
    private static final AtomicInteger NEXT_ID = new AtomicInteger();

    final int id;
    ItemStack stack;
    double x;
    double y;
    double z;
    double prevX;
    double prevY;
    double prevZ;
    float speed = NORMAL_SPEED;
    Direction input = Direction.DOWN;
    /** {@code null} = {@code ForgeDirection.UNKNOWN}: no destination, the item drops at the center. */
    @Nullable
    Direction output;
    boolean toCenter = true;
    final Set<Direction> blacklist = EnumSet.noneOf(Direction.class);
    @Nullable
    ItemPipeBlockEntity container;

    TravelingItem(int id, ItemStack stack) {
        this.id = id;
        this.stack = stack;
    }

    public TravelingItem(ItemStack stack, double x, double y, double z) {
        this(NEXT_ID.incrementAndGet(), stack);
        setPosition(x, y, z);
        prevX = x;
        prevY = y;
        prevZ = z;
    }

    public ItemStack getStack() {
        return stack;
    }

    void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX(float partialTick) {
        return prevX + (x - prevX) * partialTick;
    }

    public double getY(float partialTick) {
        return prevY + (y - prevY) * partialTick;
    }

    public double getZ(float partialTick) {
        return prevZ + (z - prevZ) * partialTick;
    }

    /** Fresh per-pipe data on entering another pipe ({@code new EntityData(item, orientation)}). */
    void enter(Direction orientation) {
        input = orientation;
        output = null;
        toCenter = true;
        blacklist.clear();
    }

    CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("item", stack.save(registries));
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("speed", speed);
        tag.putByte("input", (byte) input.get3DDataValue());
        tag.putByte("output", (byte) (output == null ? 6 : output.get3DDataValue()));
        tag.putBoolean("toCenter", toCenter);
        byte blacklisted = 0;
        for (Direction side : blacklist) {
            blacklisted |= (byte) (1 << side.get3DDataValue());
        }
        tag.putByte("blacklist", blacklisted);
        return tag;
    }

    @Nullable
    static TravelingItem load(CompoundTag tag, HolderLookup.Provider registries) {
        return load(tag, registries, NEXT_ID.incrementAndGet());
    }

    /** Loads with a given id; the client keeps the server's ids so later packets find the item. */
    @Nullable
    static TravelingItem load(CompoundTag tag, HolderLookup.Provider registries, int id) {
        ItemStack stack = ItemStack.parseOptional(registries, tag.getCompound("item"));
        if (stack.isEmpty()) {
            return null;
        }
        TravelingItem item = new TravelingItem(id, stack);
        item.setPosition(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
        item.prevX = item.x;
        item.prevY = item.y;
        item.prevZ = item.z;
        item.speed = tag.getFloat("speed");
        item.input = Direction.from3DDataValue(tag.getByte("input"));
        int output = tag.getByte("output");
        item.output = output < 6 ? Direction.from3DDataValue(output) : null;
        item.toCenter = tag.getBoolean("toCenter");
        int blacklisted = tag.getByte("blacklist");
        for (Direction side : Direction.values()) {
            if ((blacklisted & 1 << side.get3DDataValue()) != 0) {
                item.blacklist.add(side);
            }
        }
        return item;
    }
}
