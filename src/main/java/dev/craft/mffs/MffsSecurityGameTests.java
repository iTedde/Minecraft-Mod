package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.ControlSystemBlockEntity;
import dev.craft.mffs.block.DefenseStationBlockEntity;
import dev.craft.mffs.block.ExtractorBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.block.ProjectorBlockEntity;
import dev.craft.mffs.block.SecurityStationBlockEntity;
import dev.craft.mffs.block.SecurityStorageBlockEntity;
import dev.craft.mffs.item.AccessCardItem;
import dev.craft.mffs.item.CardItem;
import dev.craft.mffs.item.DataLinkCardItem;
import dev.craft.mffs.item.IdCardData;
import dev.craft.mffs.item.PersonalIdCardItem;
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/** MFFS security, phase 48.4 (spec 48 §7, §8, §10). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class MffsSecurityGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final String LARGE = "empty_16x8x16";
    private static final BlockPos STATION = new BlockPos(1, 1, 1);
    private static final BlockPos CAPACITOR = new BlockPos(3, 1, 1);
    private static final BlockPos MACHINE = new BlockPos(5, 1, 1);

    private MffsSecurityGameTests() {
    }

    private static <T extends MffsMachineBlockEntity> T place(GameTestHelper helper, BlockPos pos, Block block) {
        helper.setBlock(pos, block);
        return helper.getBlockEntity(pos);
    }

    private static ItemStack idCard(Player owner, SecurityRight... rights) {
        ItemStack card = new ItemStack(MffsItems.CARD_PERSONAL_ID.get());
        IdCardData data = IdCardData.of(owner);
        for (SecurityRight right : rights) {
            data = data.withRight(right, true);
        }
        PersonalIdCardItem.setData(card, data);
        return card;
    }

    private static ItemStack securityLink(SecurityStationBlockEntity station) {
        ItemStack card = new ItemStack(MffsItems.CARD_SECURITY_LINK.get());
        CardItem.link(card, station, station.getDeviceId(), station.getDeviceName());
        return card;
    }

    private static ItemStack powerLink(CapacitorBlockEntity capacitor) {
        ItemStack card = new ItemStack(MffsItems.CARD_POWER_LINK.get());
        CardItem.link(card, capacitor, capacitor.getPowerStorageId(), capacitor.getDeviceName());
        return card;
    }

    private static void switchOn(MffsMachineBlockEntity machine) {
        while (machine.getSwitchMode() != 2) {
            machine.toggleSwitchMode();
        }
        if (!machine.getSwitchValue()) {
            machine.toggleSwitchValue();
        }
    }

    /**
     * A master card in slot 0 is read at the first 10-tick check and activates the station at the second; the master may everything, another
     * player only what his first ID card in the station grants; an inactive station allows all.
     */
    @GameTest(template = EMPTY)
    public static void stationGrantsByCards(GameTestHelper helper) {
        Player master = helper.makeMockPlayer(GameType.SURVIVAL);
        Player guest = helper.makeMockPlayer(GameType.SURVIVAL);
        SecurityStationBlockEntity station = place(helper, STATION, MffsBlocks.SECURITY_STATION.get());
        helper.assertTrue(station.isAccessGranted(guest, SecurityRight.EB), "inactive station allows all");
        station.setStack(SecurityStationBlockEntity.SLOT_MASTER, idCard(master));
        station.setStack(5, idCard(guest, SecurityRight.EB));
        station.setStack(6, idCard(guest, SecurityRight.SR));
        helper.succeedWhen(() -> {
            helper.assertTrue(station.isActive(), "active with master card");
            helper.assertTrue(PersonalIdCardItem.hasRight(station.getStack(0), SecurityRight.CSR), "master gets CSR");
            helper.assertTrue(station.isAccessGranted(master, SecurityRight.OSS), "master may all");
            helper.assertTrue(station.isAccessGranted(guest, SecurityRight.SR), "guest card in slot 6 grants SR");
            helper.assertFalse(station.isAccessGranted(guest, SecurityRight.EB),
                    "slots are searched 35 → 1: the card in slot 6 decides, slot 5 is never read");
        });
    }

    /**
     * "+" on a blank card in the editing slot makes a 5-minute license of the station; a right is toggled on it; in
     * the player's inventory it grants that right only; at validity 0 it becomes a blank card.
     */
    @GameTest(template = EMPTY)
    public static void accessLicense(GameTestHelper helper) {
        Player master = helper.makeMockPlayer(GameType.SURVIVAL);
        Player guest = helper.makeMockPlayer(GameType.SURVIVAL);
        SecurityStationBlockEntity station = place(helper, STATION, MffsBlocks.SECURITY_STATION.get());
        station.setStack(SecurityStationBlockEntity.SLOT_MASTER, idCard(master));
        station.setStack(SecurityStationBlockEntity.SLOT_EDIT, new ItemStack(MffsItems.CARD_BLANK.get()));
        helper.startSequence()
                .thenExecuteAfter(22, () -> {
                    station.handleEvent(SecurityStationBlockEntity.EVENT_VALIDITY_UP, "null");
                    station.handleEvent(SecurityStationBlockEntity.EVENT_VALIDITY_UP, "null");
                    station.handleEvent(SecurityStationBlockEntity.EVENT_TOGGLE_RIGHT, "SR");
                    ItemStack license = station.getStack(SecurityStationBlockEntity.SLOT_EDIT);
                    helper.assertTrue(license.is(MffsItems.CARD_ACCESS.get()), "license made");
                    helper.assertValueEqual(AccessCardItem.getValidity(license), 10, "5 + 5 minutes");
                    helper.assertValueEqual(AccessCardItem.getLicense(license).stationId(), station.getDeviceId(),
                            "station ID");
                    guest.getInventory().setItem(3, license.copy());
                    helper.assertTrue(station.isAccessGranted(guest, SecurityRight.SR), "license grants SR");
                    helper.assertFalse(station.isAccessGranted(guest, SecurityRight.EB), "but not EB");
                    AccessCardItem.setValidity(guest.getInventory().getItem(3), 0);
                    helper.assertFalse(station.isAccessGranted(guest, SecurityRight.SR), "expired");
                    helper.assertTrue(guest.getInventory().getItem(3).is(MffsItems.CARD_BLANK.get()),
                            "expired license becomes a blank card");
                    station.handleEvent(SecurityStationBlockEntity.EVENT_VALIDITY_DOWN, "null");
                    station.handleEvent(SecurityStationBlockEntity.EVENT_VALIDITY_DOWN, "null");
                    helper.assertTrue(station.getStack(SecurityStationBlockEntity.SLOT_EDIT)
                            .is(MffsItems.CARD_BLANK.get()), "≤ 5 minutes minus 5 gives a blank card");
                })
                .thenSucceed();
    }

    /**
     * A capacitor with a security link asks the station; an extractor linked to that capacitor too; a projector with a
     * security link card switches to access type 3; the debugger is a free pass.
     */
    @GameTest(template = EMPTY)
    public static void machinesFollowTheirStation(GameTestHelper helper) {
        Player master = helper.makeMockPlayer(GameType.SURVIVAL);
        Player guest = helper.makeMockPlayer(GameType.SURVIVAL);
        SecurityStationBlockEntity station = place(helper, STATION, MffsBlocks.SECURITY_STATION.get());
        CapacitorBlockEntity capacitor = place(helper, CAPACITOR, MffsBlocks.CAPACITOR.get());
        ExtractorBlockEntity extractor = place(helper, MACHINE, MffsBlocks.EXTRACTOR.get());
        ProjectorBlockEntity projector = place(helper, new BlockPos(5, 1, 4), MffsBlocks.PROJECTOR.get());
        station.setStack(SecurityStationBlockEntity.SLOT_MASTER, idCard(master));
        helper.startSequence()
                .thenExecuteAfter(22, () -> {
                    capacitor.setStack(CapacitorBlockEntity.SLOT_SECURITY_LINK, securityLink(station));
                    extractor.setStack(ExtractorBlockEntity.SLOT_POWER_LINK, powerLink(capacitor));
                    projector.setStack(ProjectorBlockEntity.SLOT_SECURITY_LINK, securityLink(station));
                })
                .thenExecuteAfter(2, () -> {
                    helper.assertFalse(MffsSecurity.isAccessGranted(capacitor, guest, SecurityRight.EB),
                            "capacitor denies");
                    helper.assertTrue(MffsSecurity.isAccessGranted(capacitor, master, SecurityRight.EB),
                            "master allowed");
                    helper.assertFalse(MffsSecurity.isAccessGranted(extractor, guest, SecurityRight.EB),
                            "extractor via capacitor");
                    helper.assertValueEqual(projector.getAccessType(), 3, "own station");
                    helper.assertFalse(MffsSecurity.isAccessGranted(projector, guest, SecurityRight.SR),
                            "projector denies SR");
                    helper.assertTrue(MffsSecurity.isDeniedByStation(projector, guest, SecurityRight.SR),
                            "defence killswitch");
                    guest.getInventory().setItem(8, MffsItems.MULTITOOL_DEBUGGER.get().charged());
                    helper.assertTrue(MffsSecurity.isAccessGranted(capacitor, guest, SecurityRight.EB),
                            "debugger is a free pass");
                    helper.assertTrue(MffsSecurity.isAdmin(guest), "debugger counts as admin");
                })
                .thenSucceed();
    }

    /**
     * The security storage is active with a linked station and switched on; automation then sees no slots, inactive
     * slots 1–54. A station that disappears turns the link card into a blank card.
     */
    @GameTest(template = EMPTY)
    public static void storageHidesWhileActive(GameTestHelper helper) {
        Player master = helper.makeMockPlayer(GameType.SURVIVAL);
        SecurityStationBlockEntity station = place(helper, STATION, MffsBlocks.SECURITY_STATION.get());
        SecurityStorageBlockEntity storage = place(helper, MACHINE, MffsBlocks.SECURITY_STORAGE.get());
        station.setStack(SecurityStationBlockEntity.SLOT_MASTER, idCard(master));
        helper.startSequence()
                .thenExecuteAfter(22, () -> {
                    IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,
                            helper.absolutePos(MACHINE), null);
                    helper.assertValueEqual(handler.getSlots(), 54, "inactive: 54 slots");
                    storage.setStack(SecurityStorageBlockEntity.SLOT_SECURITY_LINK, securityLink(station));
                    switchOn(storage);
                })
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(storage.isActive(), "active");
                    IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,
                            helper.absolutePos(MACHINE), null);
                    helper.assertValueEqual(handler.getSlots(), 0, "active: no automation");
                    helper.setBlock(STATION, net.minecraft.world.level.block.Blocks.AIR);
                })
                .thenExecuteAfter(2, () -> {
                    helper.assertFalse(storage.isActive(), "station gone");
                    helper.assertTrue(storage.getStack(0).is(MffsItems.CARD_BLANK.get()), "link card cleared");
                })
                .thenSucceed();
    }

    /** The control system reads the data link target every 20 ticks and toggles its switch value. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void controlSystemRemote(GameTestHelper helper) {
        Player master = helper.makeMockPlayer(GameType.SURVIVAL);
        SecurityStationBlockEntity station = place(helper, STATION, MffsBlocks.SECURITY_STATION.get());
        CapacitorBlockEntity capacitor = place(helper, CAPACITOR, MffsBlocks.CAPACITOR.get());
        ControlSystemBlockEntity control = place(helper, MACHINE, MffsBlocks.CONTROL_SYSTEM.get());
        station.setStack(SecurityStationBlockEntity.SLOT_MASTER, idCard(master));
        helper.startSequence()
                .thenExecuteAfter(22, () -> {
                    capacitor.setDeviceName("Cap");
                    ItemStack data = new ItemStack(MffsItems.CARD_DATA_LINK.get());
                    DataLinkCardItem.link(data, capacitor);
                    control.setStack(ControlSystemBlockEntity.SLOT_SECURITY_LINK, securityLink(station));
                    control.setStack(ControlSystemBlockEntity.SLOT_READER, data);
                })
                .thenExecuteAfter(22, () -> {
                    helper.assertTrue(control.isActive(), "active with station");
                    helper.assertValueEqual(control.getRemoteName(), "Cap", "remote name");
                    helper.assertValueEqual(control.getRemoteKind(), MffsDevices.Kind.CAPACITOR.translationKey(),
                            "remote kind");
                    helper.assertTrue(control.isRemoteGuiInRange(), "in range");
                    boolean before = capacitor.getSwitchValue();
                    control.handleEvent(ControlSystemBlockEntity.EVENT_REMOTE_VALUE, "");
                    helper.assertTrue(capacitor.getSwitchValue() != before, "switch value toggled");
                })
                .thenSucceed();
    }

    /**
     * Search mode with a banned list: a player without AAI in the action perimeter loses the listed item into the
     * confiscation storage (1 000 FE); other items stay.
     */
    @GameTest(template = LARGE, timeoutTicks = 100, batch = "mffs_defense_station")
    public static void defenseStationConfiscates(GameTestHelper helper) {
        Player master = helper.makeMockPlayer(GameType.SURVIVAL);
        SecurityStationBlockEntity station = place(helper, STATION, MffsBlocks.SECURITY_STATION.get());
        CapacitorBlockEntity capacitor = place(helper, CAPACITOR, MffsBlocks.CAPACITOR.get());
        BlockPos defensePos = new BlockPos(6, 1, 4);
        DefenseStationBlockEntity defense = place(helper, defensePos, MffsBlocks.DEFENSE_STATION.get());
        station.setStack(SecurityStationBlockEntity.SLOT_MASTER, idCard(master));
        helper.startSequence()
                .thenExecuteAfter(22, () -> {
                    capacitor.setForcePower(1_000_000);
                    capacitor.setStack(CapacitorBlockEntity.SLOT_RANGE,
                            new ItemStack(MffsItems.CAPACITOR_UPGRADE_RANGE.get(), 2));
                    defense.setStack(DefenseStationBlockEntity.SLOT_POWER_LINK, powerLink(capacitor));
                    defense.setStack(DefenseStationBlockEntity.SLOT_SECURITY_LINK, securityLink(station));
                    defense.setStack(DefenseStationBlockEntity.SLOT_ACTION_DISTANCE,
                            new ItemStack(MffsItems.FIELD_MODULATOR_DISTANCE.get(), 3));
                    defense.setStack(DefenseStationBlockEntity.FIRST_LIST, new ItemStack(Items.DIAMOND));
                    defense.handleEvent(DefenseStationBlockEntity.EVENT_ACTION_MODE, "");
                    defense.handleEvent(DefenseStationBlockEntity.EVENT_ACTION_MODE, "");
                    switchOn(defense);
                })
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(defense.isActive(), "defence station active");
                    helper.assertValueEqual(defense.getActionMode(), 2, "search mode");
                    ServerPlayer intruder = helper.makeMockServerPlayerInLevel();
                    intruder.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 5));
                    intruder.getInventory().setItem(1, new ItemStack(Items.APPLE, 2));
                    BlockPos absolute = helper.absolutePos(defensePos.east(2));
                    intruder.moveTo(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
                })
                .thenExecuteAfter(3, () -> {
                    helper.assertTrue(defense.getStack(DefenseStationBlockEntity.FIRST_STORAGE).is(Items.DIAMOND),
                            "diamonds confiscated");
                    helper.assertValueEqual(defense.getStack(DefenseStationBlockEntity.FIRST_STORAGE).getCount(), 5,
                            "all five");
                })
                .thenSucceed();
    }
}
