package dev.craft.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.craft.Craft;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.core.energy.EnergyConductor;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.ic2.item.ElectricItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Energy fact sheet of every device (spec 24 §4, phase 23.6): creates each block entity of the mod without a level and
 * reads the values the code uses (safe input, packet size, storage, consumption …), plus every electric item. Written
 * to {@code reports/craft/energy_profile.json} (not packed into the jar) and turned into the wiki page by
 * {@code wiki/content_energie.py}. Getters that need a level are skipped.
 */
public final class EnergyProfileProvider implements DataProvider {
    /** Public no-argument getters worth reporting, in output order. */
    private static final List<String> GETTERS = List.of(
            "getTier", "getMaxEnergy", "getMaxStorage", "maxEUStore", "maxEUInput", "maxEUOutput", "maxMJStore",
            "getEnergyConsume", "getOperationLength", "getProduction", "getOutput", "maxEnergyReceived",
            "minEnergyReceived", "explosionRange");
    /** Static constants of the machines without common getters (IC2 1.115 keeps them as fields). */
    private static final List<String> CONSTANTS = List.of(
            "TIER", "MAX_ENERGY", "ENERGY_PER_TICK", "OPERATION_LENGTH", "OPERATION_PROGRESS", "ENERGY_PER_MATTER",
            "COST", "APPLY_COST");

    private final PackOutput output;

    public EnergyProfileProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject root = new JsonObject();
        JsonArray blocks = new JsonArray();
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals(Craft.MODID) || !(block instanceof EntityBlock entityBlock)) {
                continue;
            }
            BlockEntity be;
            try {
                be = entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            } catch (RuntimeException e) {
                continue;
            }
            if (be == null) {
                continue;
            }
            JsonObject entry = describe(id, be);
            if (entry != null) {
                blocks.add(entry);
            }
        }
        root.add("blocks", blocks);
        JsonArray items = new JsonArray();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id.getNamespace().equals(Craft.MODID) && item instanceof ElectricItem electric) {
                JsonObject entry = new JsonObject();
                entry.addProperty("id", id.getPath());
                entry.addProperty("maxCharge", electric.getMaxCharge());
                entry.addProperty("transferLimit", electric.getTransferLimit());
                entry.addProperty("tier", electric.getTier());
                entry.addProperty("canProvideEnergy", electric.canProvideEnergy());
                items.add(entry);
            }
        }
        root.add("items", items);
        Path path = output.getOutputFolder(PackOutput.Target.REPORTS).resolve(Craft.MODID).resolve("energy_profile.json");
        return DataProvider.saveStable(cache, root, path);
    }

    private static JsonObject describe(ResourceLocation id, BlockEntity be) {
        JsonObject entry = new JsonObject();
        JsonArray kinds = new JsonArray();
        if (be instanceof EnergySink sink) {
            kinds.add("eu_sink");
            putInt(entry, "maxSafeInput", () -> sink.getMaxSafeInput());
        }
        if (be instanceof EnergySource source) {
            kinds.add("eu_source");
            putInt(entry, "maxEnergyOutput", () -> source.getMaxEnergyOutput());
        }
        if (be instanceof EnergyConductor conductor) {
            kinds.add("eu_conductor");
            try {
                entry.addProperty("conductionLoss", conductor.getConductionLoss());
                entry.addProperty("insulationAbsorption", conductor.getInsulationEnergyAbsorption());
                entry.addProperty("insulationBreakdown", conductor.getInsulationBreakdownEnergy());
                entry.addProperty("conductorBreakdown", conductor.getConductorBreakdownEnergy());
            } catch (RuntimeException ignored) {
                // depends on the level
            }
        }
        if (be instanceof PowerReceptor receptor) {
            PowerProvider provider = null;
            try {
                provider = receptor.getPowerProvider();
            } catch (RuntimeException ignored) {
                // depends on the level
            }
            if (provider != null) {
                kinds.add("mj_receiver");
                entry.addProperty("mjMinReceived", provider.getMinEnergyReceived());
                entry.addProperty("mjMaxReceived", provider.getMaxEnergyReceived());
                entry.addProperty("mjMaxStored", provider.getMaxEnergyStored());
                entry.addProperty("mjActivation", provider.getActivationEnergy());
            }
        }
        JsonObject values = new JsonObject();
        for (String name : GETTERS) {
            Method method = getter(be.getClass(), name);
            if (method == null) {
                continue;
            }
            try {
                method.setAccessible(true);
                Object value = method.invoke(be);
                if (value instanceof Number number) {
                    values.addProperty(name, number);
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // depends on the level
            }
        }
        for (String name : CONSTANTS) {
            for (Class<?> c = be.getClass(); c != null && c != BlockEntity.class; c = c.getSuperclass()) {
                try {
                    Field field = c.getDeclaredField(name);
                    if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class && !values.has(name)) {
                        field.setAccessible(true);
                        values.addProperty(name, field.getInt(null));
                    }
                    break;
                } catch (NoSuchFieldException e) {
                    // look in the superclass
                } catch (ReflectiveOperationException | RuntimeException e) {
                    break;
                }
            }
        }
        if (kinds.isEmpty() && values.isEmpty()) {
            return null;
        }
        entry.addProperty("id", id.getPath());
        entry.add("kinds", kinds);
        entry.add("values", values);
        JsonArray classes = new JsonArray();
        for (Class<?> c = be.getClass(); c != null && c != BlockEntity.class; c = c.getSuperclass()) {
            classes.add(c.getSimpleName());
        }
        entry.add("classes", classes);
        return entry;
    }

    private static Method getter(Class<?> type, String name) {
        try {
            Method method = type.getMethod(name);
            Class<?> r = method.getReturnType();
            boolean numeric = r == int.class || r == long.class || r == float.class || r == double.class;
            return numeric && !Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private interface IntSupplier {
        int get();
    }

    private static void putInt(JsonObject entry, String key, IntSupplier value) {
        try {
            entry.addProperty(key, value.get());
        } catch (RuntimeException ignored) {
            // depends on the level
        }
    }

    @Override
    public String getName() {
        return "Energy profile";
    }
}
