package ivorius.psychedelicraft.world.gen.loot;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;

public class SetFluidsLootFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetFluidsLootFunction> CODEC = RecordCodecBuilder.mapCodec(i -> commonFields(i).and(i.group(
        Codec.list(SimpleFluid.CODEC).fieldOf("fluid").forGetter(o -> o.fluid),
        Codec.unboundedMap(Codec.STRING, NumberProviders.CODEC).fieldOf("attributes").forGetter(o -> o.attributes)
    )).apply(i, SetFluidsLootFunction::new));

    private final List<SimpleFluid> fluid;
    private final Map<String, NumberProvider> attributes;

    protected SetFluidsLootFunction(List<LootItemCondition> conditions, Collection<SimpleFluid> fluid,  Map<String, NumberProvider> attributes) {
        super(conditions);
        this.fluid = fluid.stream().distinct().toList();
        this.attributes = attributes;
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return PSLootFunctionTypes.SET_FLUIDS;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        ItemFluids fluids = fluid.get(context.getRandom().nextInt(fluid.size())).getDefaultStack(FluidCapacity.get(stack));
        Map<String, Integer> atrs = new HashMap<>(fluids.attributes());
        attributes.forEach((attribute, value) -> atrs.put(attribute, value.getInt(context)));
        return ItemFluids.set(stack, fluids.withAttributes(atrs));
    }

    public static ivorius.psychedelicraft.world.gen.loot.SetFluidsLootFunction.Builder builder(SimpleFluid fluid) {
        return builder(Set.of(fluid));
    }

    public static ivorius.psychedelicraft.world.gen.loot.SetFluidsLootFunction.Builder builder(Set<SimpleFluid> fluid) {
        return new ivorius.psychedelicraft.world.gen.loot.SetFluidsLootFunction.Builder(fluid);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<ivorius.psychedelicraft.world.gen.loot.SetFluidsLootFunction.Builder> {
        private final Set<SimpleFluid> fluid;
        private final Map<String, NumberProvider> attributes = new HashMap<>();

        public Builder(Set<SimpleFluid> fluid) {
            this.fluid = fluid;
        }

        public ivorius.psychedelicraft.world.gen.loot.SetFluidsLootFunction.Builder attribute(SimpleFluid.Attribute<Integer> attribute, NumberProvider value) {
            attributes.put(attribute.name(), value);
            return this;
        }

        public ivorius.psychedelicraft.world.gen.loot.SetFluidsLootFunction.Builder attribute(SimpleFluid.Attribute<Boolean> attribute, boolean value) {
            attributes.put(attribute.name(), ConstantValue.exactly(value ? 1 : 0));
            return this;
        }

        @Override
        protected ivorius.psychedelicraft.world.gen.loot.SetFluidsLootFunction.Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetFluidsLootFunction(getConditions(), fluid, attributes);
        }
    }
}
