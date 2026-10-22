package dev.craft.ic2.menu;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Armor slot of the player's own inventory inside a machine GUI (the vanilla {@code ArmorSlot} is package-private). */
public class ArmorSlot extends Slot {
    /** Top to bottom: helmet, chestplate, leggings, boots. */
    public static final EquipmentSlot[] ORDER = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET};

    private final Player owner;
    private final EquipmentSlot slot;

    public ArmorSlot(Player owner, EquipmentSlot slot, int x, int y) {
        super(owner.getInventory(), 36 + slot.getIndex(), x, y);
        this.owner = owner;
        this.slot = slot;
    }

    @Override
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
        owner.onEquipItem(slot, oldStack, newStack);
        super.setByPlayer(newStack, oldStack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.canEquip(slot, owner);
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack stack = getItem();
        if (!stack.isEmpty() && !player.isCreative()
                && EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        return super.mayPickup(player);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        ResourceLocation icon = switch (slot) {
            case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
            case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
            case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
            default -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
        };
        return Pair.of(InventoryMenu.BLOCK_ATLAS, icon);
    }
}
