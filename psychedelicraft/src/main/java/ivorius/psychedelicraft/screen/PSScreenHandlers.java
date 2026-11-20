package ivorius.psychedelicraft.screen;

import java.util.concurrent.atomic.AtomicReference;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.block.entity.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * @author Sollace
 * @since 12 Jan 2023
 */
public interface PSScreenHandlers {
    MenuType<DryingTableScreenHandler> DRYING_TABLE = register("drying_table", new MenuType<>(DryingTableScreenHandler::new, FeatureFlags.VANILLA_SET));

    MenuType<FluidContraptionScreenHandler<BarrelBlockEntity>> BARREL = register("barrel", contraptionScreenHander());
    MenuType<FluidContraptionScreenHandler<DistilleryBlockEntity>> DISTILLERY = register("distillery", contraptionScreenHander());
    MenuType<FluidContraptionScreenHandler<FlaskBlockEntity>> FLASK = register("flask", contraptionScreenHander());
    MenuType<FluidContraptionScreenHandler<MashTubBlockEntity>> MASH_TUB = register("mash_tub", contraptionScreenHander());

    static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType<T> type) {
        return Registry.register(BuiltInRegistries.MENU, Psychedelicraft.id(name), type);
    }

    static <T extends FlaskBlockEntity> MenuType<FluidContraptionScreenHandler<T>> contraptionScreenHander() {
        final AtomicReference<MenuType<FluidContraptionScreenHandler<T>>> type = new AtomicReference<>(null);
        type.set(net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(
                (sync, inventory, buf) -> new FluidContraptionScreenHandler<>(type.get(), sync, inventory, BlockWithFluid.InteractionData.PACKET_CODEC.decode(buf))
        ));
        return type.get();
    }

    static void bootstrap() { }
}
