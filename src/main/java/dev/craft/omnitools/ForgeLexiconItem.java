package dev.craft.omnitools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Forge Lexicon ({@code ItemArtifact} meta 0, spec 50 §1.3): sneak + right click opens the portable lexicon; right
 * click on an empty Mystical Plinth puts it there with a harmless lightning strike and explosion effect.
 */
public class ForgeLexiconItem extends Item {
    public ForgeLexiconItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new LexiconMenu(id, inventory,
                    new LexiconInventory(), q -> true, true), Component.translatable("item.craft.forge_lexicon")));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (player == null || player.isShiftKeyDown() || !state.is(OmniTools.MYSTICAL_PLINTH.get())
                || state.getValue(PlinthBlock.LEXICON)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            place(serverLevel, pos, state);
            if (!player.hasInfiniteMaterials()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** {@code EntityLightningBoltFake} + {@code Utils.doFakeExplosion}: effects only, then the plinth holds the book. */
    public static void place(ServerLevel level, BlockPos pos, BlockState state) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(pos));
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 1, 0, 0, 0,
                0);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.setBlock(pos, state.setValue(PlinthBlock.LEXICON, true), Block.UPDATE_ALL);
    }
}
