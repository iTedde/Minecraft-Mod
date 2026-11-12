package dev.craft.chargepads;

import dev.craft.Craft;
import dev.craft.chargepads.block.ChargePadBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The three charge pads (spec 47 §2.1): hardness 1.5, material iron, stone sound. {@code canHarvestBlock = false}:
 * breaking is always as slow as without the right tool and never drops the pad (only the wrench does).
 */
public final class CpBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    public static final DeferredBlock<ChargePadBlock> STATIC_CHARGE_PAD = pad("static_charge_pad", PadTier.STATIC);
    public static final DeferredBlock<ChargePadBlock> CRYSTALIZOR_CHARGE_PAD =
            pad("crystalizor_charge_pad", PadTier.CRYSTALIZOR);
    public static final DeferredBlock<ChargePadBlock> LAPOTRONIC_CHARGE_PAD =
            pad("lapotronic_charge_pad", PadTier.LAPOTRONIC);

    private CpBlocks() {
    }

    private static DeferredBlock<ChargePadBlock> pad(String name, PadTier tier) {
        return BLOCKS.register(name, () -> new ChargePadBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                .strength(1.5F).sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion(), tier));
    }

    public static DeferredBlock<ChargePadBlock> pad(PadTier tier) {
        return switch (tier) {
            case STATIC -> STATIC_CHARGE_PAD;
            case CRYSTALIZOR -> CRYSTALIZOR_CHARGE_PAD;
            case LAPOTRONIC -> LAPOTRONIC_CHARGE_PAD;
        };
    }
}
