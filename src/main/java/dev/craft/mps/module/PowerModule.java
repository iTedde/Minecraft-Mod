package dev.craft.mps.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * A module definition (MPS {@code PowerModuleBase} with the pack config values, spec 29 §3): valid items, category,
 * install cost and the property modifiers. Base values add a constant, tradeoffs add {@code multiplier · slider}.
 */
public final class PowerModule {
    public enum Kind {
        /** Always on (plain {@code PowerModule}). */
        PASSIVE,
        /** Can be switched off with a keybinding ({@code IToggleableModule}). */
        TOGGLEABLE,
        /** Power tool mode, only active while selected ({@code IRightClickModule}). */
        RIGHT_CLICK
    }

    /** {@code PropertyModifierFlatAdditive} (tweak {@code null}) or {@code PropertyModifierLinearAdditive}. */
    public record Modifier(String tweak, double value) {
        double apply(ModuleData data, double current) {
            return tweak == null ? current + value : current + value * data.tweak(tweak);
        }
    }

    private final String name;
    private final Set<ModuleTarget> targets;
    private final String category;
    private final Kind kind;
    private final ResourceLocation icon;
    private final List<Supplier<ItemStack>> installCost = new ArrayList<>();
    private final Map<String, List<Modifier>> properties = new LinkedHashMap<>();
    private final Set<String> tweaks = new LinkedHashSet<>();

    PowerModule(String name, Set<ModuleTarget> targets, String category, Kind kind, ResourceLocation icon) {
        this.name = name;
        this.targets = targets;
        this.category = category;
        this.kind = kind;
        this.icon = icon;
    }

    PowerModule cost(Supplier<? extends ItemLike> item, int count) {
        installCost.add(() -> new ItemStack(item.get(), count));
        return this;
    }

    PowerModule base(String property, double value) {
        properties.computeIfAbsent(property, key -> new ArrayList<>()).add(new Modifier(null, value));
        return this;
    }

    PowerModule tradeoff(String tweak, String property, double multiplier) {
        properties.computeIfAbsent(property, key -> new ArrayList<>()).add(new Modifier(tweak, multiplier));
        tweaks.add(tweak);
        return this;
    }

    /** The key in item data, the pack's English module name. */
    public String getName() {
        return name;
    }

    /** Lower-case key for translations, e.g. {@code jet_boots}. */
    public String getKey() {
        return key(name);
    }

    public static String key(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
    }

    public boolean isValidFor(ModuleTarget target) {
        return targets.contains(target);
    }

    public String getCategory() {
        return category;
    }

    public Kind getKind() {
        return kind;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    /** Fresh copies of the cost stacks. */
    public List<ItemStack> getInstallCost() {
        List<ItemStack> cost = new ArrayList<>();
        for (Supplier<ItemStack> stack : installCost) {
            cost.add(stack.get());
        }
        return cost;
    }

    public Map<String, List<Modifier>> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /** Slider names in definition order. */
    public Set<String> getTweaks() {
        return Collections.unmodifiableSet(tweaks);
    }

    /** {@code applyPropertyModifiers}: adds this module's share of {@code property}. */
    public double apply(ModuleData data, String property, double value) {
        List<Modifier> modifiers = properties.get(property);
        if (modifiers != null) {
            for (Modifier modifier : modifiers) {
                value = modifier.apply(data, value);
            }
        }
        return value;
    }
}
