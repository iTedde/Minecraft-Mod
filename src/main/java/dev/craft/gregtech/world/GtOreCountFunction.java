package dev.craft.gregtech.world;

import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * {@code GT_BlockMetaID_Ore.getBlockDropped} amounts: {@code base + multiplier * rand(1 + fortune / fortuneDivisor)}
 * (e.g. ruby {@code 1 + rand(1 + f)}, iridium {@code 1 + rand(1 + f/2)}, sodalite dust {@code 6 + 3 * rand(1 + f)}).
 */
public final class GtOreCountFunction extends LootItemConditionalFunction {
    public static final MapCodec<GtOreCountFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
            .and(instance.group(
                    Codec.INT.fieldOf("base").forGetter(function -> function.base),
                    Codec.INT.optionalFieldOf("multiplier", 1).forGetter(function -> function.multiplier),
                    Codec.INT.optionalFieldOf("fortune_divisor", 1).forGetter(function -> function.fortuneDivisor)))
            .apply(instance, GtOreCountFunction::new));

    private final int base;
    private final int multiplier;
    private final int fortuneDivisor;

    private GtOreCountFunction(List<LootItemCondition> conditions, int base, int multiplier, int fortuneDivisor) {
        super(conditions);
        this.base = base;
        this.multiplier = multiplier;
        this.fortuneDivisor = fortuneDivisor;
    }

    public static Builder<?> count(int base, int multiplier, int fortuneDivisor) {
        return simpleBuilder(conditions -> new GtOreCountFunction(conditions, base, multiplier, fortuneDivisor));
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        int bound = 1 + fortune(context) / fortuneDivisor;
        stack.setCount(base + multiplier * context.getRandom().nextInt(bound));
        return stack;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }

    @Override
    public LootItemFunctionType<GtOreCountFunction> getType() {
        return GtWorldGen.ORE_COUNT.get();
    }

    /** Fortune level of the breaking tool (1.4.7 {@code aFortune}), 0 without a tool. */
    static int fortune(LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || tool.isEmpty()) {
            return 0;
        }
        Holder<Enchantment> fortune = context.getResolver().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FORTUNE);
        return EnchantmentHelper.getItemEnchantmentLevel(fortune, tool);
    }
}
