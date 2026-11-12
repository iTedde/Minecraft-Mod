package dev.craft.chargepads;

import java.util.List;

import dev.craft.Craft;
import dev.craft.chargepads.block.ChargePadBlock;
import dev.craft.chargepads.block.ChargePadBlockEntity;
import dev.craft.chargepads.item.PadModule;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Charge pads, phase 47.2: upgrade kits, emitter modules, efficiency upgrade (spec 47 §5, §7, §8, §10). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ChargePadModuleGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos PAD = new BlockPos(2, 1, 2);

    private ChargePadModuleGameTests() {
    }

    private static ChargePadBlockEntity pad(GameTestHelper helper, PadTier tier) {
        helper.setBlock(PAD, CpBlocks.pad(tier).get());
        return helper.getBlockEntity(PAD);
    }

    private static ItemStack module(PadModule module) {
        return new ItemStack(CpItems.module(module).get());
    }

    private static InteractionResult useKit(GameTestHelper helper, Player player, ItemStack kit, Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND, kit);
        BlockPos absolute = helper.absolutePos(PAD);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), face, absolute, false));
        return kit.getItem().onItemUseFirst(kit, context);
    }

    /** Puts a mock player on the pad and switches it on. */
    private static ServerPlayer playerOnPad(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos absolute = helper.absolutePos(PAD);
        player.moveTo(absolute.getX() + 0.5, absolute.getY() + 0.25, absolute.getZ() + 0.5);
        helper.getBlockState(PAD).entityInside(helper.getLevel(), absolute, player);
        return player;
    }

    /**
     * Static → crystalizor from below: storage and battery stay, the static upgrade lands in the field slot (original
     * bug); side clicks pass, the wrong kit swallows the click without being used up. Crystalizor → lapotronic keeps
     * module slots and upgrades in place.
     */
    @GameTest(template = EMPTY)
    public static void upgradeKitsKeepStorageAndSlots(GameTestHelper helper) {
        ChargePadBlockEntity pad = pad(helper, PadTier.STATIC);
        pad.setStorage(30_000);
        pad.getInventory().setStackInSlot(0, new ItemStack(Items.REDSTONE, 3));
        pad.getInventory().setStackInSlot(1, new ItemStack(Ic2Items.OVERCLOCKER_UPGRADE.get(), 2));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wrongKit = new ItemStack(CpItems.LAPOTRONIC_UPGRADE_KIT.get());
        helper.assertTrue(useKit(helper, player, wrongKit, Direction.DOWN).consumesAction(), "wrong kit consumes");
        helper.assertValueEqual(wrongKit.getCount(), 1, "wrong kit not used up");
        ItemStack kit = new ItemStack(CpItems.CRYSTALIZOR_UPGRADE_KIT.get(), 2);
        helper.assertValueEqual(useKit(helper, player, kit, Direction.UP), InteractionResult.PASS, "top face");
        useKit(helper, player, kit, Direction.DOWN);
        helper.assertBlockPresent(CpBlocks.CRYSTALIZOR_CHARGE_PAD.get(), PAD);
        helper.assertValueEqual(kit.getCount(), 1, "kit used up");
        ChargePadBlockEntity crystalizor = helper.getBlockEntity(PAD);
        helper.assertValueEqual(crystalizor.getStorage(), 30_000, "storage kept");
        helper.assertValueEqual(crystalizor.getInventory().getStackInSlot(0).getCount(), 3, "battery slot kept");
        helper.assertTrue(crystalizor.getInventory().getStackInSlot(PadTier.FIELD_SLOT)
                .is(Ic2Items.OVERCLOCKER_UPGRADE.get()), "upgrade lands in the field slot");
        helper.assertTrue(crystalizor.getInventory().getStackInSlot(3).isEmpty(), "upgrade slot empty");

        crystalizor.getInventory().setStackInSlot(PadTier.PROJECTOR_SLOT, module(PadModule.PROXIMITY_BOOSTER));
        crystalizor.getInventory().setStackInSlot(PadTier.FIELD_SLOT, module(PadModule.ARMOUR_PRIORITY));
        crystalizor.getInventory().setStackInSlot(4, new ItemStack(CpItems.EFFICIENCY_UPGRADE.get(), 5));
        useKit(helper, player, new ItemStack(CpItems.LAPOTRONIC_UPGRADE_KIT.get()), Direction.DOWN);
        helper.assertBlockPresent(CpBlocks.LAPOTRONIC_CHARGE_PAD.get(), PAD);
        ChargePadBlockEntity lapotronic = helper.getBlockEntity(PAD);
        helper.assertValueEqual(lapotronic.getStorage(), 30_000, "storage kept");
        helper.assertTrue(lapotronic.getInventory().getStackInSlot(PadTier.PROJECTOR_SLOT)
                .is(CpItems.module(PadModule.PROXIMITY_BOOSTER).get()), "projector kept");
        helper.assertTrue(lapotronic.getInventory().getStackInSlot(PadTier.FIELD_SLOT)
                .is(CpItems.module(PadModule.ARMOUR_PRIORITY).get()), "field kept");
        helper.assertValueEqual(lapotronic.getInventory().getStackInSlot(4).getCount(), 5, "upgrade slot kept");
        helper.succeed();
    }

    /** Upgrading an active full crystalizor pad: electrocution max(2 × 2, ⌈12 × 1.0⌉) = 12. */
    @GameTest(template = EMPTY)
    public static void upgradingActivePadZaps(GameTestHelper helper) {
        helper.setBlock(PAD, CpBlocks.CRYSTALIZOR_CHARGE_PAD.get().defaultBlockState()
                .setValue(ChargePadBlock.ACTIVE, true));
        ChargePadBlockEntity pad = helper.getBlockEntity(PAD);
        pad.setStorage(600_000);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        useKit(helper, player, new ItemStack(CpItems.LAPOTRONIC_UPGRADE_KIT.get()), Direction.DOWN);
        helper.assertValueEqual(player.getMaxHealth() - player.getHealth(), 12.0F, "upgrade zap");
        helper.assertBlockProperty(PAD, ChargePadBlock.ACTIVE, false);
        helper.succeed();
    }

    /** Module slots: only matching modules up to the pad tier; static pads have none. */
    @GameTest(template = EMPTY)
    public static void moduleSlotRules(GameTestHelper helper) {
        helper.assertFalse(ChargePadBlockEntity.isValidForSlot(PadTier.CRYSTALIZOR, PadTier.PROJECTOR_SLOT,
                module(PadModule.FIELD_EXPANSION_DELTA)), "delta needs tier 3");
        helper.assertTrue(ChargePadBlockEntity.isValidForSlot(PadTier.LAPOTRONIC, PadTier.PROJECTOR_SLOT,
                module(PadModule.FIELD_EXPANSION_DELTA)), "delta on lapotronic");
        helper.assertFalse(ChargePadBlockEntity.isValidForSlot(PadTier.CRYSTALIZOR, PadTier.PROJECTOR_SLOT,
                module(PadModule.DRAIN_CONVERSION)), "field module not in the projector");
        helper.assertTrue(ChargePadBlockEntity.isValidForSlot(PadTier.CRYSTALIZOR, PadTier.FIELD_SLOT,
                module(PadModule.DRAIN_CONVERSION)), "field module in the field slot");
        helper.assertFalse(ChargePadBlockEntity.isValidForSlot(PadTier.STATIC, 1,
                module(PadModule.PROXIMITY_BOOSTER)), "static has no module slot");
        helper.assertTrue(ChargePadBlockEntity.isValidForSlot(PadTier.STATIC, 1,
                new ItemStack(CpItems.EFFICIENCY_UPGRADE.get())), "efficiency in the upgrade slot");
        helper.assertValueEqual(ChargePadBlockEntity.slotLimit(PadTier.CRYSTALIZOR, PadTier.FIELD_SLOT), 1,
                "modules stack to 1");
        helper.succeed();
    }

    /** Field expansion alpha grows the field by one block and costs 2 EU/t more idle (2 + 2 = 4 per tick). */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void fieldExpansionGrowsField(GameTestHelper helper) {
        ChargePadBlockEntity pad = pad(helper, PadTier.CRYSTALIZOR);
        pad.setStorage(100);
        pad.getInventory().setStackInSlot(PadTier.PROJECTOR_SLOT, module(PadModule.FIELD_EXPANSION_ALPHA));
        ServerPlayer player = playerOnPad(helper);
        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    helper.assertValueEqual(pad.getFieldExpansion(), 1, "expansion");
                    helper.assertValueEqual(pad.getFieldBox().getXsize(), 3.0, "field 3 blocks wide");
                    pad.setStorage(100);
                })
                .thenExecuteAfter(5, () -> {
                    int used = 100 - pad.getStorage();
                    helper.assertTrue(used >= 16 && used % 4 == 0, "4 EU idle per tick, used " + used);
                    player.discard();
                })
                .thenSucceed();
    }

    /** Proximity booster: held slot ± 1 are charged, the slot beyond stays empty. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void proximityBoosterChargesNeighbours(GameTestHelper helper) {
        ChargePadBlockEntity pad = pad(helper, PadTier.CRYSTALIZOR);
        pad.setStorage(100_000);
        pad.getInventory().setStackInSlot(PadTier.PROJECTOR_SLOT, module(PadModule.PROXIMITY_BOOSTER));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        for (int slot = 0; slot < 4; slot++) {
            player.getInventory().setItem(slot, new ItemStack(Ic2Items.RE_BATTERY.get()));
        }
        player.getInventory().selected = 1;
        BlockPos absolute = helper.absolutePos(PAD);
        player.moveTo(absolute.getX() + 0.5, absolute.getY() + 0.25, absolute.getZ() + 0.5);
        helper.getBlockState(PAD).entityInside(helper.getLevel(), absolute, player);
        helper.startSequence()
                .thenExecuteAfter(5, () -> {
                    for (int slot = 0; slot < 3; slot++) {
                        helper.assertTrue(ElectricItems.getCharge(player.getInventory().getItem(slot)) > 0,
                                "slot " + slot + " charged");
                    }
                    helper.assertValueEqual(ElectricItems.getCharge(player.getInventory().getItem(3)), 0,
                            "slot 3 not charged");
                    player.discard();
                })
                .thenSucceed();
    }

    /** Drain conversion: the held battery is emptied, the energy is lost and still costs the pad. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void drainModuleDischarges(GameTestHelper helper) {
        ChargePadBlockEntity pad = pad(helper, PadTier.CRYSTALIZOR);
        pad.setStorage(50_000);
        pad.getInventory().setStackInSlot(PadTier.FIELD_SLOT, module(PadModule.DRAIN_CONVERSION));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, ElectricItems.charged(new ItemStack(Ic2Items.RE_BATTERY.get())));
        BlockPos absolute = helper.absolutePos(PAD);
        player.moveTo(absolute.getX() + 0.5, absolute.getY() + 0.25, absolute.getZ() + 0.5);
        helper.getBlockState(PAD).entityInside(helper.getLevel(), absolute, player);
        helper.startSequence()
                .thenExecuteAfter(5, () -> {
                    int charge = ElectricItems.getCharge(player.getInventory().getItem(0));
                    helper.assertTrue(charge < 10_000, "battery drained, " + charge);
                    int drained = 10_000 - charge;
                    helper.assertTrue(pad.getStorage() < 50_000 - drained, "drain costs the pad");
                    player.discard();
                })
                .thenSucceed();
    }

    /** Damage conversion: 1 emitter radiation damage and poison IX for 2 s (crystalizor), no charging. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void damageModulePoisons(GameTestHelper helper) {
        ChargePadBlockEntity pad = pad(helper, PadTier.CRYSTALIZOR);
        pad.setStorage(50_000);
        pad.getInventory().setStackInSlot(PadTier.FIELD_SLOT, module(PadModule.DAMAGE_CONVERSION));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, PAD);
        ServerPlayer player = playerOnPad(helper);
        player.getInventory().setItem(player.getInventory().selected, new ItemStack(Ic2Items.RE_BATTERY.get()));
        helper.startSequence()
                .thenExecuteAfter(3, () -> {
                    helper.assertTrue(pig.hasEffect(MobEffects.POISON), "pig poisoned");
                    helper.assertValueEqual(pig.getEffect(MobEffects.POISON).getAmplifier(), 8, "tier × 4");
                    helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "pig hurt");
                    helper.assertValueEqual(ElectricItems.getCharge(player.getInventory().getSelected()), 0,
                            "nothing charged");
                    player.discard();
                })
                .thenSucceed();
    }

    /** Efficiency upgrade recipe takes only a fully charged RE battery. */
    @GameTest(template = EMPTY)
    public static void efficiencyRecipeNeedsFullBattery(GameTestHelper helper) {
        RecipeHolder<?> holder = helper.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "efficiency_upgrade")).orElseThrow();
        CraftingRecipe recipe = (CraftingRecipe) holder.value();
        ItemStack gold = new ItemStack(Ic2Items.cable(CableType.DOUBLE_INSULATED_GOLD).get());
        ItemStack fibre = new ItemStack(Ic2Items.cable(CableType.GLASS_FIBRE).get());
        ItemStack circuit = new ItemStack(Ic2Items.ADVANCED_CIRCUIT.get());
        ItemStack full = ElectricItems.charged(new ItemStack(Ic2Items.RE_BATTERY.get()));
        ItemStack empty = new ItemStack(Ic2Items.RE_BATTERY.get());
        CraftingInput withFull = CraftingInput.of(3, 2, List.of(full, fibre, full, gold, circuit, gold));
        CraftingInput withEmpty = CraftingInput.of(3, 2, List.of(full, fibre, empty, gold, circuit, gold));
        helper.assertTrue(recipe.matches(withFull, helper.getLevel()), "full batteries match");
        helper.assertFalse(recipe.matches(withEmpty, helper.getLevel()), "empty battery does not");
        helper.succeed();
    }
}
