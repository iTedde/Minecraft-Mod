package ivorius.psychedelicraft.item.component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DelayType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.util.PacketCodecUtils;

public record Impurities(Set<Impurity> impurities) implements TooltipProvider {
    public static final Impurities EMPTY = new Impurities(Set.of());

    public static final Codec<Impurities> CODEC = RecordCodecBuilder.create(i -> i.group(
            Impurity.CODEC.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("impurities").forGetter(Impurities::impurities)
    ).apply(i, Impurities::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Impurities> PACKET_CODEC = PacketCodecUtils.ofEnum(Impurity.class)
            .apply(ByteBufCodecs.collection(i -> (Set<Impurity>)new HashSet<Impurity>(i)))
            .map(Impurities::new, Impurities::impurities);

    public Impurities {
        impurities = impurities.isEmpty() ? EnumSet.noneOf(Impurity.class) : EnumSet.copyOf(impurities);
    }

    public static Impurities get(ItemStack stack) {
        return stack.getOrDefault(PSComponents.IMPURITIES, EMPTY);
    }

    public static boolean isOn(ItemStack stack, Impurity impurity) {
        return get(stack).impurities().contains(impurity);
    }

    public static ItemStack set(ItemStack stack, Impurity...impurities) {
        return set(stack, Set.of(impurities));
    }

    public static ItemStack set(ItemStack stack, Set<Impurity> impurities) {
        stack.set(PSComponents.IMPURITIES, new Impurities(impurities));
        return stack;
    }

    public static ItemStack set(ItemStack stack, Impurities impurities) {
        stack.set(PSComponents.IMPURITIES, impurities);
        return stack;
    }

    public static Impurities combine(Impurities a, Impurities b) {
        Set<Impurity> combined = new HashSet<>(a.impurities());
        combined.addAll(b.impurities());
        return new Impurities(combined);
    }

    public static Impurities overlap(Impurities a, Set<Impurity> b) {
        Set<Impurity> combined = new HashSet<>();
        for (Impurity i : a.impurities()) {
            if (b.contains(i)) {
                combined.add(i);
            }
        }
        return new Impurities(combined);
    }

    public float getEffectStrengthModifier() {
        float strength = 1;
        if (impurities.contains(Impurity.CARBON)) {
            strength *= 0.5F;
        }
        if (impurities.contains(Impurity.PETROLIUM)) {
            strength *= 1.5F;
        }
        if (impurities.contains(Impurity.ETHANOL)) {
            strength *= 1.5F;
        }
        if (impurities.contains(Impurity.SILICA)) {
            strength *= 0.4F;
        }
        if (impurities.contains(Impurity.SUGAR)) {
            strength *= 0.9F;
        }
        return strength;
    }

    public float getEffectDelayModifier() {
        float strength = 1;
        if (impurities.contains(Impurity.GASOLINE)) {
            strength *= 0.5F;
        }
        if (impurities.contains(Impurity.PETROLIUM)) {
            strength *= 0.5F;
        }
        if (impurities.contains(Impurity.SUGAR)) {
            strength *= 1.2F;
        }
        return strength;
    }

    public Stream<DrugType<?>> getAdditionalDrugs() {
        var builder = Stream.<DrugType<?>>builder();
        if (impurities.contains(Impurity.ETHANOL)) {
            builder.add(DrugType.ALCOHOL);
        }
        if (impurities.contains(Impurity.SUGAR)) {
            builder.add(DrugType.SUGAR);
        }
        return builder.build();
    }

    public List<DrugInfluence> modifyEffects(List<DrugInfluence> influences) {
        if (impurities.isEmpty()) {
            return influences;
        }

        float strength = getEffectStrengthModifier();
        float delayModifier = getEffectDelayModifier();

        return Stream.concat(
                getAdditionalDrugs().map(type -> new DrugInfluence(DrugType.ALCOHOL, DelayType.METABOLISED, 0.1F, 1, 0.8F)),
                influences.stream()
        ).map(i -> i.copyWithTarget(i.target() * strength).copyWithDelay(Math.max(1, (int)(i.delay() * delayModifier))))
            .toList();
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> tooltip, TooltipFlag type) {
        if (!impurities.isEmpty()) {
            tooltip.accept(impurities.stream().map(i -> i.getName())
                .reduce(null, (a, b) -> a == null ? b : b == null ? a : a.copy().append(", ").append(b)));
        }
    }

    public enum Impurity implements StringRepresentable {
        CARBON,
        ETHANOL,
        PETROLIUM,
        GASOLINE,
        SILICA,
        SUGAR,
        LAPIS_LAZULI;

        public static final Codec<Impurity> CODEC = StringRepresentable.fromEnum(Impurity::values);

        private final String name = name().toLowerCase(Locale.ROOT);
        private final Component displayName = Component.translatable(Util.makeDescriptionId("impurity", Psychedelicraft.id(name)));

        @Override
        public String getSerializedName() {
            return name;
        }

        public Component getName() {
            return displayName;
        }
    }

}
