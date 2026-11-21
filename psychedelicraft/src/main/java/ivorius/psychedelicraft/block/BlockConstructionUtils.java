package ivorius.psychedelicraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

interface BlockConstructionUtils {
    static BlockBehaviour.Properties leaves(SoundType soundGroup) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.2f)
                .randomTicks()
                .sound(soundGroup)
                .noOcclusion()
                .isValidSpawn(BlockConstructionUtils::canSpawnOnLeaves)
                .isSuffocating(BlockConstructionUtils::never)
                .isViewBlocking(BlockConstructionUtils::never)
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY)
                .isRedstoneConductor(BlockConstructionUtils::never);
    }

    static BarrelBlock barrel(MapColor mapColor) {
        return new BarrelBlock(Properties.of()
                .mapColor(mapColor)
                .instrument(NoteBlockInstrument.BASS)
                .sound(SoundType.WOOD)
                .destroyTime(2)
                .ignitedByLava()
                .pushReaction(PushReaction.BLOCK));
    }

    static RotatedPillarBlock log(MapColor topColor, MapColor sideColor) {
        return new RotatedPillarBlock(BlockBehaviour.Properties.of()
                .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? topColor : sideColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .ignitedByLava());
    }

    static BlockBehaviour.Properties plant(SoundType soundGroup) {
        return Properties.of()
                .randomTicks()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .instabreak()
                .sound(soundGroup)
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY);
    }

    static FlowerPotBlock pottedPlant(Block flower, FeatureFlag ... requiredFeatures) {
        BlockBehaviour.Properties settings = BlockBehaviour.Properties.of()
                .instabreak()
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY);
        if (requiredFeatures.length > 0) {
            settings = settings.requiredFeatures(requiredFeatures);
        }
        return new FlowerPotBlock(flower, settings);
    }

    static ButtonBlock woodenButton(BlockSetType blockSetType, FeatureFlag ... requiredFeatures) {
        BlockBehaviour.Properties settings = Properties.of()
                .noCollission()
                .strength(0.5f)
                .pushReaction(PushReaction.DESTROY);
        if (requiredFeatures.length > 0) {
            settings = settings.requiredFeatures(requiredFeatures);
        }
        return new ButtonBlock(blockSetType, 30, settings);
    }

    static Boolean never(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
        return false;
    }
    static boolean never(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    static Boolean always(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
        return true;
    }
    static boolean always(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    static Boolean canSpawnOnLeaves(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }

}
