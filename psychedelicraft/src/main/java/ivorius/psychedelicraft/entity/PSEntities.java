/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity;


import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Created by lukas on 25.04.14.
 *
 * Updated by Sollace on 1 Jan 2023
 */
public interface PSEntities {
    EntityType<MolotovCocktailEntity> MOLOTOV_COCKTAIL = register("molotov_cocktail", EntityType.Builder.<MolotovCocktailEntity>of(MolotovCocktailEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .updateInterval(10)
            .clientTrackingRange(4)
            .sized(0.1F, 0.1F));
    EntityType<RealityRiftEntity> REALITY_RIFT = register("reality_rift", EntityType.Builder.of(RealityRiftEntity::new, MobCategory.MISC)
            .updateInterval(3)
            .clientTrackingRange(5)
            .sized(2F, 2F));


    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        EntityType<T> type = builder.build(Psychedelicraft.id(name).toString());
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, Psychedelicraft.id(name), type);
    }

    static void bootstrap() {
        PSTradeOffers.bootstrap();



    }
}
