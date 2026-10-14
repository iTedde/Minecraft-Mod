package dev.craft.data;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.cable.CableType;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public abstract class CraftLanguageProvider extends LanguageProvider {
    protected CraftLanguageProvider(PackOutput output, String locale) {
        super(output, Craft.MODID, locale);
    }

    protected void cable(CableType type, String name) {
        addBlock(Ic2Blocks.cable(type), name);
    }

    /** English names as in the pack's {@code IC2.lang}. */
    public static final class English extends CraftLanguageProvider {
        public English(PackOutput output) {
            super(output, "en_us");
        }

        @Override
        protected void addTranslations() {
            add("itemGroup.craft.ic2", "IndustrialCraft 2");
            add("death.attack.craft.electricity", "%1$s was electrocuted");
            add("death.attack.craft.electricity.player", "%1$s was electrocuted whilst fighting %2$s");
            cable(CableType.INSULATED_COPPER, "Copper Cable");
            cable(CableType.COPPER, "Uninsulated Copper Cable");
            cable(CableType.GOLD, "Gold Cable");
            cable(CableType.INSULATED_GOLD, "Insulated Gold Cable");
            cable(CableType.DOUBLE_INSULATED_GOLD, "2xIns. Gold Cable");
            cable(CableType.HV, "HV Cable");
            cable(CableType.INSULATED_HV, "Insulated HV Cable");
            cable(CableType.DOUBLE_INSULATED_HV, "2xIns. HV Cable");
            cable(CableType.QUADRUPLE_INSULATED_HV, "4xIns. HV Cable");
            cable(CableType.GLASS_FIBRE, "Glass Fibre Cable");
            cable(CableType.TIN, "Ultra-Low-Current Cable");
            cable(CableType.DETECTOR, "EU-Detector Cable");
            cable(CableType.SPLITTER, "EU-Splitter Cable");
        }
    }

    public static final class German extends CraftLanguageProvider {
        public German(PackOutput output) {
            super(output, "de_de");
        }

        @Override
        protected void addTranslations() {
            add("itemGroup.craft.ic2", "IndustrialCraft 2");
            add("death.attack.craft.electricity", "%1$s erlitt einen Stromschlag");
            add("death.attack.craft.electricity.player", "%1$s erlitt im Kampf mit %2$s einen Stromschlag");
            cable(CableType.INSULATED_COPPER, "Kupferkabel");
            cable(CableType.COPPER, "Unisoliertes Kupferkabel");
            cable(CableType.GOLD, "Goldkabel");
            cable(CableType.INSULATED_GOLD, "Isoliertes Goldkabel");
            cable(CableType.DOUBLE_INSULATED_GOLD, "2x isoliertes Goldkabel");
            cable(CableType.HV, "HV-Kabel");
            cable(CableType.INSULATED_HV, "Isoliertes HV-Kabel");
            cable(CableType.DOUBLE_INSULATED_HV, "2x isoliertes HV-Kabel");
            cable(CableType.QUADRUPLE_INSULATED_HV, "4x isoliertes HV-Kabel");
            cable(CableType.GLASS_FIBRE, "Glasfaserkabel");
            cable(CableType.TIN, "Ultra-Low-Current-Kabel");
            cable(CableType.DETECTOR, "EU-Detektorkabel");
            cable(CableType.SPLITTER, "EU-Splitterkabel");
        }
    }
}
