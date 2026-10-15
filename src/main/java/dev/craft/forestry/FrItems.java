package dev.craft.forestry;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.ElectronTube;
import dev.craft.forestry.item.CircuitBoardItem;
import dev.craft.forestry.item.ElectronTubeItem;
import dev.craft.forestry.item.SolderingIronItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FrItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<BlockItem> THERMIONIC_FABRICATOR =
            ITEMS.registerSimpleBlockItem(FrBlocks.THERMIONIC_FABRICATOR);
    public static final DeferredItem<BlockItem> CARPENTER = ITEMS.registerSimpleBlockItem(FrBlocks.CARPENTER);
    public static final DeferredItem<BlockItem> ELECTRICAL_ENGINE =
            ITEMS.registerSimpleBlockItem(FrBlocks.ELECTRICAL_ENGINE);

    /** {@code gearTin}. */
    public static final DeferredItem<Item> TIN_GEAR = ITEMS.registerSimpleItem("tin_gear");

    /** {@code sturdyMachine}. */
    public static final DeferredItem<Item> STURDY_CASING = ITEMS.registerSimpleItem("sturdy_casing");
    /** {@code solderingIron}: stack size 1, durability 5 (never used). */
    public static final DeferredItem<SolderingIronItem> SOLDERING_IRON = ITEMS.registerItem("soldering_iron",
            properties -> new SolderingIronItem(properties.durability(5)));

    private static final Map<ElectronTube, DeferredItem<ElectronTubeItem>> TUBES = new EnumMap<>(ElectronTube.class);
    private static final Map<CircuitBoardType, DeferredItem<CircuitBoardItem>> BOARDS =
            new EnumMap<>(CircuitBoardType.class);

    static {
        for (ElectronTube tube : ElectronTube.values()) {
            TUBES.put(tube, ITEMS.registerItem(tube.getName() + "_electron_tube",
                    properties -> new ElectronTubeItem(tube, properties)));
        }
        for (CircuitBoardType type : CircuitBoardType.values()) {
            BOARDS.put(type, ITEMS.registerItem(type.getName() + "_circuit_board",
                    properties -> new CircuitBoardItem(type, properties)));
        }
    }

    private FrItems() {
    }

    public static DeferredItem<ElectronTubeItem> tube(ElectronTube tube) {
        return TUBES.get(tube);
    }

    public static DeferredItem<CircuitBoardItem> circuitBoard(CircuitBoardType type) {
        return BOARDS.get(type);
    }
}
