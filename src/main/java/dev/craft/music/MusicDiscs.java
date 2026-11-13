package dev.craft.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.craft.Craft;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom music discs (new content, user 2026-11-13): streamed mono OGGs under {@code sounds/records/}, one jukebox
 * song and one disc item per track. Creative tab / commands only, no recipe or loot.
 */
public final class MusicDiscs {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Craft.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    /** A track: file/id name, shown title and length (seconds, rounded up). */
    public record Track(String name, String title, int seconds, DeferredHolder<SoundEvent, SoundEvent> sound,
            ResourceKey<JukeboxSong> song, DeferredItem<Item> disc) {
    }

    private static final List<Track> TRACKS = new ArrayList<>();

    static {
        track("be_the_one", "Be the One", 205);
        track("break_my_heart", "Break My Heart", 230);
        track("dont_start_now", "Don't Start Now", 182);
        track("fever", "Fever", 245);
        track("hallucinate", "Hallucinate", 208);
        track("houdini", "Houdini", 189);
        track("illusion", "Illusion", 187);
        track("kiss_and_makeup", "Kiss and Make Up", 205);
        track("levitating", "Levitating", 231);
        track("love_again", "Love Again", 263);
        track("new_rules", "New Rules", 225);
        track("no_lie", "No Lie", 229);
        track("one_kiss", "One Kiss", 224);
        track("physical", "Physical", 244);
        track("sonne_geht_auf", "Sonne geht auf", 274);
        track("toter_schmetterling", "Toter Schmetterling", 234);
        track("training_season", "Training Season", 211);
        track("were_good", "We're Good", 192);
    }

    private MusicDiscs() {
    }

    private static void track(String name, String title, int seconds) {
        String path = "records/" + name;
        DeferredHolder<SoundEvent, SoundEvent> sound = SOUNDS.register(path,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Craft.MODID, path)));
        ResourceKey<JukeboxSong> song = ResourceKey.create(Registries.JUKEBOX_SONG,
                ResourceLocation.fromNamespaceAndPath(Craft.MODID, name));
        DeferredItem<Item> disc = ITEMS.registerSimpleItem("music_disc_" + name,
                new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(song));
        TRACKS.add(new Track(name, title, seconds, sound, song, disc));
    }

    public static List<Track> tracks() {
        return Collections.unmodifiableList(TRACKS);
    }

    public static void init(IEventBus modBus) {
        SOUNDS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(MusicDiscs::addToCreativeTab);
    }

    /** Datagen: {@code data/craft/jukebox_song/<name>.json}. */
    public static void bootstrapSongs(BootstrapContext<JukeboxSong> context) {
        for (int i = 0; i < TRACKS.size(); i++) {
            Track track = TRACKS.get(i);
            context.register(track.song(), new JukeboxSong(track.sound(),
                    Component.translatable(Util.makeDescriptionId("jukebox_song", track.song().location())),
                    track.seconds(), i % 15 + 1));
        }
    }

    /** Next to the vanilla discs. */
    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            TRACKS.forEach(track -> event.accept(track.disc()));
        }
    }
}
