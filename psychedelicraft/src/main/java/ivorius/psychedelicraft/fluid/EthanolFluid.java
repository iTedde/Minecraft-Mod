package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public class EthanolFluid extends DrugFluid {
    public EthanolFluid(ResourceLocation id, ivorius.psychedelicraft.fluid.DrugFluid.Settings settings) {
        super(id, settings);
    }

    @Override
    public boolean hasRandomTicks() {
        return true;
    }

    @Override
    public float getFireStrength(ItemFluids fluidStack) {
        return 1;
    }

    @Override
    public float getExplosionStrength(ItemFluids fluidStack) {
        return 1;
    }

    @Override
    public void onRandomTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
        super.onRandomTick(world, pos, state, random);
        world.getEntities(null, AABB.ofSize(pos.getCenter(), 5, 5, 5)).forEach(entity -> {
            if (random.nextInt(30) == 0) {
                DrugProperties.of(entity).ifPresentOrElse(properties -> {
                    properties.addToDrug(DrugType.ALCOHOL, 0.1);
                }, () -> {
                    PSDamageTypes.damage(world, entity, world.damageSources().source(PSDamageTypes.ALCOHOL_POSIONING), 1);
                });
            }
        });
    }

    @Override
    public boolean isSuitableContainer(ItemStack container) {
        return container.is(Tags.Items.BUCKETS);
    }

    @Override
    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.DRINK_RECEPTICALS;
    }
}
