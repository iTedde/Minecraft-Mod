package dev.craft.gravisuite.item;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.armor.ElectricArmorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * GraviSuite {@code ItemUltimateSolarHelmet} (spec 28 §2, §8): 1 000 000 EU, transfer 20 000, tier 2, protects like a
 * QuantumSuit helmet without IC2's ×25 (original error), armor display always 3. Worn it makes 512 EU/t in the sun and
 * 64 EU/t under open sky, charging the armor (feet → head) and then the main inventory; the rest is lost.
 */
public class UltimateSolarHelmetItem extends ElectricArmorItem {
    public static final int GEN_DAY = 512;
    public static final int GEN_NIGHT = 64;
    public static final int TICK_RATE = 128;
    private static final EquipmentSlot[] ARMOR_SLOTS =
            {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craft.MODID);
    /** {@code ticker}, {@code sunIsUp}, {@code skyIsVisible}: static in the pack, here per player and not saved. */
    private static final Supplier<AttachmentType<Visibility>> VISIBILITY =
            ATTACHMENT_TYPES.register("ultimate_solar_visibility", () -> AttachmentType.builder(Visibility::new).build());

    public UltimateSolarHelmetItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties, 1000000, 20000, 2);
    }

    @Override
    public double getDamageAbsorptionRatio() {
        return 1.0;
    }

    @Override
    public int getEnergyPerDamage() {
        return 900;
    }

    /** Original error: {@code charge / 900} without IC2's ×25. */
    @Override
    protected int getDamageLimit(ItemStack stack) {
        return ElectricItems.discharge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true) / getEnergyPerDamage();
    }

    /** Always {@code round(20 · 0.15)} = 3, also when empty. */
    @Override
    public int getArmorDisplay(ItemStack stack) {
        return 3;
    }

    @Override
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return;
        }
        int energyLeft = generation(level, player);
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (energyLeft <= 0) {
                return;
            }
            energyLeft -= chargeItem(player.getItemBySlot(slot), energyLeft);
        }
        for (ItemStack item : player.getInventory().items) {
            if (energyLeft <= 0) {
                return;
            }
            energyLeft -= chargeItem(item, energyLeft);
        }
    }

    private static int chargeItem(ItemStack stack, int amount) {
        return ElectricItems.isElectric(stack) ? ElectricItems.charge(stack, amount, 3, false, false) : 0;
    }

    /** {@code gainFuel}: the visibility is checked on every 128th call, starting with the first. */
    public static int generation(Level level, Player player) {
        Visibility visibility = player.getData(VISIBILITY);
        if (visibility.ticker++ % TICK_RATE == 0) {
            updateVisibility(level, player, visibility);
        }
        if (visibility.sunIsUp && visibility.skyIsVisible) {
            return GEN_DAY;
        }
        return visibility.skyIsVisible ? GEN_NIGHT : 0;
    }

    /** For tests: fixes the visibility until the next check in 127 calls. */
    public static void setVisibility(Player player, boolean sunIsUp, boolean skyIsVisible) {
        Visibility visibility = player.getData(VISIBILITY);
        visibility.ticker = 1;
        visibility.sunIsUp = sunIsUp;
        visibility.skyIsVisible = skyIsVisible;
    }

    /** Day without rain (rain only counts in biomes with precipitation); sky above the head, dimension with sky. */
    private static void updateVisibility(Level level, Player player, Visibility visibility) {
        BlockPos feet = new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ());
        boolean wetBiome = level.getBiome(feet).value().hasPrecipitation();
        boolean rainWeather = wetBiome && (level.isRaining() || level.isThundering());
        visibility.sunIsUp = level.isDay() && !rainWeather;
        visibility.skyIsVisible = level.canSeeSky(feet.above()) && level.dimensionType().hasSkyLight();
    }

    private static final class Visibility {
        int ticker;
        boolean sunIsUp;
        boolean skyIsVisible;
    }
}
