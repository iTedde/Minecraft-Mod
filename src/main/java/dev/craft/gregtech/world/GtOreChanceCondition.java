package dev.craft.gregtech.world;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/** GT ore by-product chance: {@code rand(max(1, range / (fortune + 1))) == 0}. */
public record GtOreChanceCondition(int range) implements LootItemCondition {
    public static final MapCodec<GtOreChanceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("range").forGetter(GtOreChanceCondition::range)).apply(instance, GtOreChanceCondition::new));

    public static Builder chance(int range) {
        return () -> new GtOreChanceCondition(range);
    }

    @Override
    public boolean test(LootContext context) {
        int bound = Math.max(1, range / (GtOreCountFunction.fortune(context) + 1));
        return context.getRandom().nextInt(bound) == 0;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }

    @Override
    public LootItemConditionType getType() {
        return GtWorldGen.ORE_CHANCE.get();
    }
}
