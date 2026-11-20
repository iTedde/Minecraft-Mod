/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import ivorius.psychedelicraft.util.PacketCodecUtils;

/**
 * A fluid that can processed in the correct container, e.g. distiller.
 */
public interface Processable {
    /**
     * Tick value indicating that the fluid is in its most base form (cannot be converted to another type of fluid).
     */
    int UNCONVERTABLE = -1;
    /**
     * Returns the ticks needed for the fluid to pass through a particular conversion process.
     * Return {@link #UNCONVERTABLE} if the fluid cannot perform the requested conversion.
     *
     * @param stack The fluid currently being processed.
     * @return The time it needs to distill, in ticks.
     */
    int getProcessingTime(Resovoir tank, ProcessType type);

    /**
     * Notifies the fluid that the stack has distilled, and is expected to apply this change to the stack.
     *
     * @param stack The fluid currently being distilled.
     * @param output Consumer for byproducts of the conversion process
     */
    void process(Context context, ProcessType type, ByProductConsumer output);

    Stream<Process> getProcesses();

    default ProcessType modifyProcess(Resovoir tank, ProcessType type) {
        return type;
    }

    record Process(SimpleFluid fluid, ResourceLocation id, List<Transition> transitions) {}

    record Transition(
            ProcessType type,
            int time,
            int multiplier,
            Function<ItemFluids, ItemFluids> input,
            Function<ItemFluids, ItemFluids> output
    ) {}

    interface ByProductConsumer {
        void accept(ItemStack stack);

        void accept(ItemFluids stack);
    }

    interface Context extends Container {
        default Resovoir getTankOnSide(Direction direction) {
            return getPrimaryTank();
        }

        default List<Resovoir> getAuxiliaryTanks() {
            return List.of(getPrimaryTank());
        }

        default int getTotalFluidVolume() {
            return getAuxiliaryTanks().stream().mapToInt(r -> r.getContents().amount()).sum();
        }

        Resovoir getPrimaryTank();
    }

    enum ProcessType implements StringRepresentable {
        /**
         * Nothing is happening, probably due to unmet requirements
         */
        IDLE,
        /**
         * When processed in a distiller, used to increase the purity (proof) of existing liquors.
         */
        DISTILL,
        /**
         * When processed in a barrel, used to age grape juice into wines of increasing quality.
         */
        MATURE,
        /**
         * When processed in a vat/mash tub, used to ferment sugars into alcohol
         */
        FERMENT,
        /**
         * When processed past its full fermentation in a vat/mash tub, starts producing acids instead of alcohols
         */
        ACETIFY,
        /**
         * When a hot fluid is placed in a vat/mash tub it will slowly cool back to its base form
         */
        COOL,
        /**
         * When processed in the benzene burner, used to chemically extract purified substances
         */
        PURIFY,
        /**
         * When slurry is left to sit for a long time it will separate into water and solids
         */
        SEPARATE;

        private final String name = name().toLowerCase(Locale.ROOT);
        private final Component status = Component.translatable("fluid.status." + name);
        private final String timeLabel = "time.until." + name;

        public static final Codec<ProcessType> CODEC = StringRepresentable.fromValues(ProcessType::values);
        public static final StreamCodec<RegistryFriendlyByteBuf, ProcessType> PACKET_CODEC = PacketCodecUtils.ofEnum(ProcessType.class);

        public Component getStatus() {
            return status;
        }

        public String getTimeLabelTranslationKey() {
            return timeLabel;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public static void appendTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
            Processable.ProcessType processType = stack.get(PSComponents.PROCESS_TYPE);
            if (processType != null) {
                tooltip.add(Component.translatable("psychedelicraft.container.process_type." + processType.getSerializedName()).withStyle(ChatFormatting.BLUE));
            }
        }
    }
}
