package dev.craft.portalgun;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Long fall boots (spec 21): no fall damage while worn, never wear out. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class PortalGunGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos CENTER = new BlockPos(3, 2, 3);

    private PortalGunGameTests() {
    }

    /** Lands a zombie wearing {@code boots} after a 20 block fall and returns the health it lost. */
    private static float fall(GameTestHelper helper, ItemStack boots) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, CENTER);
        zombie.setItemSlot(EquipmentSlot.FEET, boots);
        float health = zombie.getHealth();
        zombie.causeFallDamage(20.0F, 1.0F, zombie.damageSources().fall());
        return health - zombie.getHealth();
    }

    @GameTest(template = EMPTY)
    public static void bootsCancelFallDamage(GameTestHelper helper) {
        float lost = fall(helper, PgItems.LONG_FALL_BOOTS.toStack());
        helper.assertTrue(lost == 0.0F, "no fall damage with long fall boots: " + lost);
        helper.assertTrue(fall(helper, new ItemStack(Items.DIAMOND_BOOTS)) > 0.0F, "diamond boots take fall damage");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bootsAreDiamondBootsThatNeverBreak(GameTestHelper helper) {
        ItemStack boots = PgItems.LONG_FALL_BOOTS.toStack();
        helper.assertTrue(!boots.isDamageableItem(), "setMaxDamage(0)");
        helper.assertTrue(PgItems.LONG_FALL_BOOTS.get().getDefense() == 3, "diamond boots defense 3");
        helper.assertTrue(PgItems.LONG_FALL_BOOT.toStack().getMaxStackSize() == 2, "single boot stacks to 2");
        helper.succeed();
    }
}
