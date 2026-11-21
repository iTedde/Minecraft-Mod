package dev.craft.omnitools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileLexicon} (spec 50 §1.2/§1.3): the Forge Lexicon lying open on a plinth. Holds the conversion slots and,
 * client side, the book animation of the enchanting table (turns towards a player within 3 blocks).
 */
public class LexiconBlockEntity extends BlockEntity implements MenuProvider {
    private static final RandomSource RANDOM = RandomSource.create();

    private final LexiconInventory lexicon = new LexiconInventory() {
        @Override
        public void setChanged() {
            super.setChanged();
            LexiconBlockEntity.this.setChanged();
        }
    };

    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;

    public LexiconBlockEntity(BlockPos pos, BlockState state) {
        super(OmniTools.LEXICON_BLOCK_ENTITY.get(), pos, state);
    }

    public LexiconInventory lexicon() {
        return lexicon;
    }

    /** {@code dismantle}: drops the contents (the lexicon itself is dropped by the caller). */
    public void dropContents() {
        if (level != null) {
            Containers.dropContents(level, worldPosition, (Container) lexicon);
            lexicon.clearContent();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("item.craft.forge_lexicon");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new LexiconMenu(id, inventory, lexicon, p -> !isRemoved() && p.distanceToSqr(
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0, false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        lexicon.save(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lexicon.load(tag, registries);
    }

    /** Book animation, as {@code EnchantingTableBlockEntity.bookAnimationTick} with the player range of 3 blocks. */
    public static void clientTick(Level level, BlockPos pos, BlockState state, LexiconBlockEntity book) {
        book.oOpen = book.open;
        book.oRot = book.rot;
        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
        if (player != null) {
            double dx = player.getX() - (pos.getX() + 0.5);
            double dz = player.getZ() - (pos.getZ() + 0.5);
            book.tRot = (float) Mth.atan2(dz, dx);
            book.open += 0.1F;
            if (book.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float old = book.flipT;
                do {
                    book.flipT += RANDOM.nextInt(4) - RANDOM.nextInt(4);
                } while (old == book.flipT);
            }
        } else {
            book.tRot += 0.02F;
            book.open -= 0.1F;
        }
        book.rot = wrap(book.rot);
        book.tRot = wrap(book.tRot);
        book.rot += wrap(book.tRot - book.rot) * 0.4F;
        book.open = Mth.clamp(book.open, 0.0F, 1.0F);
        book.time++;
        book.oFlip = book.flip;
        float delta = Mth.clamp((book.flipT - book.flip) * 0.4F, -0.2F, 0.2F);
        book.flipA += (delta - book.flipA) * 0.9F;
        book.flip += book.flipA;
    }

    private static float wrap(float angle) {
        while (angle >= Mth.PI) {
            angle -= Mth.TWO_PI;
        }
        while (angle < -Mth.PI) {
            angle += Mth.TWO_PI;
        }
        return angle;
    }

    /** The Forge Lexicon item that goes back when the book is taken off. */
    public static ItemStack lexiconItem() {
        return new ItemStack(OmniTools.FORGE_LEXICON.get());
    }
}
