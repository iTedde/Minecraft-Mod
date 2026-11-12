package dev.craft.nuclearcontrol.card;

import javax.annotation.Nullable;

import dev.craft.nuclearcontrol.NcDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * {@code CardWrapperImpl}: a working copy of the values a card carries (data component {@code craft:card_data}) –
 * title, state, the values of the last update, text lines and the targets of an energy array card. The target of a
 * sensor location card is the separate component {@code craft:sensor_target}.
 */
public final class CardData {
    private final CompoundTag tag;
    @Nullable
    private final BlockPos target;

    private CardData(CompoundTag tag, @Nullable BlockPos target) {
        this.tag = tag;
        this.target = target;
    }

    public static CardData of(ItemStack stack) {
        return new CardData(stack.getOrDefault(NcDataComponents.CARD_DATA.get(), CustomData.EMPTY).copyTag(),
                stack.get(NcDataComponents.SENSOR_TARGET.get()));
    }

    /** Writes the values back; returns whether anything changed. */
    public boolean save(ItemStack stack) {
        CustomData old = stack.getOrDefault(NcDataComponents.CARD_DATA.get(), CustomData.EMPTY);
        if (old.copyTag().equals(tag)) {
            return false;
        }
        stack.set(NcDataComponents.CARD_DATA.get(), CustomData.of(tag));
        return true;
    }

    @Nullable
    public BlockPos getTarget() {
        return target;
    }

    public CompoundTag tag() {
        return tag;
    }

    public int getInt(String key) {
        return tag.getInt(key);
    }

    public void setInt(String key, int value) {
        tag.putInt(key, value);
    }

    public long getLong(String key) {
        return tag.getLong(key);
    }

    public void setLong(String key, long value) {
        tag.putLong(key, value);
    }

    public boolean getBoolean(String key) {
        return tag.getBoolean(key);
    }

    public void setBoolean(String key, boolean value) {
        tag.putBoolean(key, value);
    }

    public String getString(String key) {
        return tag.getString(key);
    }

    public void setString(String key, String value) {
        tag.putString(key, value);
    }

    public boolean has(String key) {
        return tag.contains(key);
    }

    public String getTitle() {
        return tag.getString("title");
    }

    public void setTitle(String title) {
        tag.putString("title", title);
    }

    @Nullable
    public CardState getState() {
        return CardState.fromIndex(tag.getInt("state"));
    }

    public void setState(CardState state) {
        tag.putInt("state", state.getIndex());
    }
}
