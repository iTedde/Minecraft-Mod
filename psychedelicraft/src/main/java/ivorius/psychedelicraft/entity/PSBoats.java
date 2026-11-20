package ivorius.psychedelicraft.entity;

import java.util.function.Supplier;

import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

/**
 * Juniper boat type, added to {@link Boat.Type} through NeoForge enum extension
 * ({@code META-INF/enumextensions.json}; upstream: Terraform Wood API).
 */
public final class PSBoats {
    public static final EnumProxy<Boat.Type> JUNIPER_PROXY = new EnumProxy<>(Boat.Type.class,
            (Supplier<Block>)() -> PSBlocks.JUNIPER_PLANKS,
            "psychedelicraft:juniper",
            (Supplier<Item>)() -> PSItems.JUNIPER_BOAT,
            (Supplier<Item>)() -> PSItems.JUNIPER_CHEST_BOAT,
            (Supplier<Item>)() -> Items.STICK,
            false);

    private PSBoats() {
    }

    public static Boat.Type juniper() {
        return JUNIPER_PROXY.getValue();
    }
}
