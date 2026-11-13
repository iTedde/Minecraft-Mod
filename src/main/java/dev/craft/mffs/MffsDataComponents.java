package dev.craft.mffs;

import com.mojang.serialization.Codec;
import dev.craft.Craft;
import dev.craft.mffs.item.AccessLicense;
import dev.craft.mffs.item.CardLink;
import dev.craft.mffs.item.IdCardData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Item data of MFFS (spec 48 §2, §6.2, §8): force energy, forcicium level, cell switch and card links. */
public final class MffsDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** NBT {@code ForceEnergy} of the crystal and the multitool. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FORCE_ENERGY =
            COMPONENTS.registerComponentType("force_energy", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** NBT {@code Forceciumlevel} of the compact forcicium cell. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FORCICIUM_LEVEL =
            COMPONENTS.registerComponentType("forcicium_level", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Cell active/inactive, per item instead of the original's one switch for all cells (spec 48 §14). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CELL_ACTIVE =
            COMPONENTS.registerComponentType("cell_active", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** Target of a link card ({@code ItemCard}: linkTarget, worldname, Areaname, valid and the target ID). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CardLink>> CARD_LINK =
            COMPONENTS.registerComponentType("card_link", builder -> builder
                    .persistent(CardLink.CODEC)
                    .networkSynchronized(CardLink.STREAM_CODEC));

    /** Owner and rights of a personal ID card or access license ({@code name}, {@code rights}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<IdCardData>> ID_CARD =
            COMPONENTS.registerComponentType("id_card", builder -> builder
                    .persistent(IdCardData.CODEC)
                    .networkSynchronized(IdCardData.STREAM_CODEC));

    /** Validity and station of an access license. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessLicense>> ACCESS_LICENSE =
            COMPONENTS.registerComponentType("access_license", builder -> builder
                    .persistent(AccessLicense.CODEC)
                    .networkSynchronized(AccessLicense.STREAM_CODEC));

    /** Device kind of a data link card ({@code displayName}: the {@link MffsDevices.Kind} name). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> DEVICE_KIND =
            COMPONENTS.registerComponentType("device_kind", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private MffsDataComponents() {
    }
}
