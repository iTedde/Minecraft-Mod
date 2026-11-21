package ivorius.psychedelicraft;

import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * Psychedelicraft's own registries. Kept free of side effects so they can be created during
 * {@link NewRegistryEvent} (upstream: Fabric registry builder in the owning classes).
 */
@SuppressWarnings("deprecation")
public final class PSRegistries {
    public static final ResourceKey<Registry<DrugType<?>>> DRUGS_KEY = ResourceKey.createRegistryKey(Psychedelicraft.id("drugs"));
    public static final ResourceKey<Registry<SimpleFluid>> FLUIDS_KEY = ResourceKey.createRegistryKey(Psychedelicraft.id("fluids"));

    public static final Registry<DrugType<?>> DRUGS = new RegistryBuilder<>(DRUGS_KEY).sync(true).create();
    public static final Registry<SimpleFluid> FLUIDS = new RegistryBuilder<>(FLUIDS_KEY)
            .defaultKey(Psychedelicraft.id("empty"))
            .withIntrusiveHolders() // SimpleFluid creates its holder in the constructor (upstream)
            .sync(true)
            .create();

    private PSRegistries() {
    }

    public static void register(NewRegistryEvent event) {
        event.register(DRUGS);
        event.register(FLUIDS);
    }
}
