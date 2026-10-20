package dev.craft.data;

import java.util.ArrayList;
import java.util.List;

/**
 * "Where does it come from" texts of the raw materials (spec 24), written as {@code source.<namespace>.<path>} by
 * {@link CraftLanguageProvider} and shown by EMI (info page), the shift tooltip and the wiki. Values come from
 * {@code docs/spec} (world generation spec 07, machines spec 13) and the generated recipes.
 */
public final class SourceTexts {
    /** One text for one or more items (e.g. the stone and deepslate variant of an ore). */
    public record Entry(List<String> ids, String german, String english) {
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    static {
        ores();
        worldAndDrops();
    }

    private SourceTexts() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    private static void add(String german, String english, String... ids) {
        List<String> full = new ArrayList<>();
        for (String id : ids) {
            full.add(id.contains(":") ? id : "craft:" + id);
        }
        ENTRIES.add(new Entry(List.copyOf(full), german, english));
    }

    /** Ores (spec 07 §1 and §6, phase 23.1). */
    private static void ores() {
        add("Oberwelt und Mining-Dimension, y 0–39, sehr häufig (etwa 25 kleine Adern je Chunk). Ab "
                        + "Steinspitzhacke, droppt sich selbst. Ofen → Zinnbarren; Macerator → 2 Zinnstaub; Industrial "
                        + "Grinder → 2 Zinnstaub + kl. Eisen- und kl. Zinkstaub.",
                "Overworld and mining dimension, y 0–39, very common (about 25 small veins per chunk). Stone pickaxe "
                        + "or better, drops itself. Furnace → tin ingot; macerator → 2 tin dust; industrial grinder → "
                        + "2 tin dust + tiny iron and tiny zinc dust.",
                "tin_ore", "deepslate_tin_ore");
        add("Oberwelt und Mining-Dimension, y 0–64, etwa 20 kleine Adern (je bis 3 Erze) je Chunk. Ab "
                        + "Eisenspitzhacke; droppt einen Uranerz-Brocken (mit Behutsamkeit das Erz). Kompressor → "
                        + "Uranbarren; Macerator → 2 Uranstaub; Industrial Grinder → 2 Uranstaub + 2 kl. Plutoniumstaub.",
                "Overworld and mining dimension, y 0–64, about 20 small veins (up to 3 ores each) per chunk. Iron "
                        + "pickaxe or better; drops an uranium drop (the ore with silk touch). Compressor → uranium "
                        + "ingot; macerator → 2 uranium dust; industrial grinder → 2 uranium dust + 2 tiny plutonium "
                        + "dust.",
                "uranium_ore", "deepslate_uranium_ore");
        add("Vanilla-Kupfer mit IC2-Verteilung: Oberwelt und Mining-Dimension, y 10–68 (meist um y 39), etwa 15 "
                        + "Adern (bis 10 Erze) je Chunk; die großen Vanilla-Adern gibt es nicht. Droppt 1 Rohkupfer. "
                        + "Macerator: Erz oder Rohkupfer → 2 Kupferstaub; Industrial Grinder (Erz mit Behutsamkeit) → "
                        + "2 Kupferstaub + kl. Gold- und kl. Nickelstaub.",
                "Vanilla copper with IC2 distribution: overworld and mining dimension, y 10–68 (mostly around y 39), "
                        + "about 15 veins (up to 10 ores) per chunk; the large vanilla veins are gone. Drops 1 raw "
                        + "copper. Macerator: ore or raw copper → 2 copper dust; industrial grinder (ore via silk "
                        + "touch) → 2 copper dust + tiny gold and tiny nickel dust.",
                "minecraft:copper_ore", "minecraft:deepslate_copper_ore", "minecraft:raw_copper");
        add("Sehr selten: Oberwelt und Mining-Dimension, Einzelblöcke in etwa jedem 5. Chunk, y 0–98. Ab "
                        + "Diamantspitzhacke; droppt 1 Iridiumerz (mit Glück mehr). Macerator → 2 Iridiumerz; "
                        + "Industrial Grinder → 2 Iridiumerz + 2 kl. Platinstaub (Quecksilberzelle: 1 Platinstaub).",
                "Very rare: overworld and mining dimension, single blocks in about every 5th chunk, y 0–98. Diamond "
                        + "pickaxe or better; drops 1 iridium ore (more with fortune). Macerator → 2 iridium ore; "
                        + "industrial grinder → 2 iridium ore + 2 tiny platinum dust (mercury cell: 1 platinum dust).",
                "iridium_ore_block", "deepslate_iridium_ore_block");
        add("Oberwelt und Mining-Dimension, y 0–31, 1 Ader (bis 16 Erze) je Chunk. Ab Steinspitzhacke, droppt sich "
                        + "selbst. Macerator → 2 Galenastaub; Industrial Grinder → 2 Galenastaub + Schwefelstaub, mit "
                        + "Quecksilberzelle zusätzlich Silberstaub.",
                "Overworld and mining dimension, y 0–31, 1 vein (up to 16 ores) per chunk. Stone pickaxe or better, "
                        + "drops itself. Macerator → 2 galena dust; industrial grinder → 2 galena dust + sulfur dust, "
                        + "with a mercury cell also silver dust.",
                "galena_ore", "deepslate_galena_ore");
        add("Oberwelt nur in Wüste, Tafelberg und Savanne, in der Mining-Dimension überall; y 0–31, 8 Einzelblöcke "
                        + "je Chunk. Ab Eisenspitzhacke; droppt Rubin, selten zusätzlich roten Granat. Industrial "
                        + "Grinder (Erz mit Behutsamkeit) → Rubin + 6 kl. Rubinstaub + 2 kl. roter Granatstaub.",
                "Overworld only in desert, badlands and savanna, everywhere in the mining dimension; y 0–31, 8 single "
                        + "blocks per chunk. Iron pickaxe or better; drops a ruby, rarely also a red garnet. Industrial "
                        + "grinder (ore via silk touch) → ruby + 6 tiny ruby dust + 2 tiny red garnet dust.",
                "ruby_ore", "deepslate_ruby_ore");
        add("Oberwelt nur in Ozean, Strand und Mangrovensumpf, in der Mining-Dimension überall; y 0–31, 8 "
                        + "Einzelblöcke je Chunk. Ab Eisenspitzhacke; droppt Saphir, selten zusätzlich grünen Saphir. "
                        + "Industrial Grinder (Erz mit Behutsamkeit) → Saphir + 6 kl. Saphirstaub + 2 kl. grüner "
                        + "Saphirstaub.",
                "Overworld only in ocean, beach and mangrove swamp, everywhere in the mining dimension; y 0–31, 8 "
                        + "single blocks per chunk. Iron pickaxe or better; drops a sapphire, rarely also a green "
                        + "sapphire. Industrial grinder (ore via silk touch) → sapphire + 6 tiny sapphire dust + 2 tiny "
                        + "green sapphire dust.",
                "sapphire_ore", "deepslate_sapphire_ore");
        add("Oberwelt nur in Ebenen, Wiese, Wäldern und Kirschblütenhain, in der Mining-Dimension überall; y 32–79, "
                        + "2 Adern (bis 16 Erze) je Chunk. Ab Steinspitzhacke, droppt sich selbst. Macerator → 4 "
                        + "Bauxitstaub; Industrial Grinder → 4 Bauxitstaub + 1 Aluminiumstaub.",
                "Overworld only in plains, meadow, forests and cherry grove, everywhere in the mining dimension; "
                        + "y 32–79, 2 veins (up to 16 ores) per chunk. Stone pickaxe or better, drops itself. Macerator "
                        + "→ 4 bauxite dust; industrial grinder → 4 bauxite dust + 1 aluminium dust.",
                "bauxite_ore");
        add("Nur im Nether (im Netherrack), y 0–63, 1–16 Adern je Chunk. Ab Steinspitzhacke; droppt 2 Pyritstaub "
                        + "(mit Behutsamkeit das Erz). Erz: Ofen → Eisenbarren; Macerator → 5 Pyritstaub; Industrial "
                        + "Grinder → 5 Pyritstaub + 2 Schwefelstaub; Hochofen mit Calciumcarbonatzelle → 2 raff. Eisen.",
                "Nether only (in netherrack), y 0–63, 1–16 veins per chunk. Stone pickaxe or better; drops 2 pyrite "
                        + "dust (the ore with silk touch). Ore: furnace → iron ingot; macerator → 5 pyrite dust; "
                        + "industrial grinder → 5 pyrite dust + 2 sulfur dust; blast furnace with a calcium carbonate "
                        + "cell → 2 refined iron.",
                "pyrite_ore");
        add("Nur im Nether (im Netherrack), y 64–127, 1–8 Adern je Chunk. Ab Eisenspitzhacke; droppt 2 "
                        + "Zinnoberstaub, oft dazu Redstone (mit Behutsamkeit das Erz). Erz: Macerator → 3 "
                        + "Zinnoberstaub; Industrial Grinder → 5 Zinnoberstaub + 2 kl. Redstone + kl. Glowstonestaub.",
                "Nether only (in netherrack), y 64–127, 1–8 veins per chunk. Iron pickaxe or better; drops 2 cinnabar "
                        + "dust, often also redstone (the ore with silk touch). Ore: macerator → 3 cinnabar dust; "
                        + "industrial grinder → 5 cinnabar dust + 2 tiny redstone + tiny glowstone dust.",
                "cinnabar_ore");
        add("Nur im Nether (im Netherrack), y 32–95, 1–16 Adern je Chunk. Ab Steinspitzhacke; droppt 2 "
                        + "Sphaleritstaub, manchmal Zinkstaub oder gelben Granat (mit Behutsamkeit das Erz). Erz: "
                        + "Macerator → 4 Sphaleritstaub; Industrial Grinder → 5 Sphaleritstaub + 1 Zinkstaub + kl. "
                        + "gelber Granatstaub.",
                "Nether only (in netherrack), y 32–95, 1–16 veins per chunk. Stone pickaxe or better; drops 2 "
                        + "sphalerite dust, sometimes zinc dust or a yellow garnet (the ore with silk touch). Ore: "
                        + "macerator → 4 sphalerite dust; industrial grinder → 5 sphalerite dust + 1 zinc dust + tiny "
                        + "yellow garnet dust.",
                "sphalerite_ore");
        add("Nur im End: 4 Adern (bis 16 Erze) je Chunk im Endstein, y 0–127, dazu in schwebenden "
                        + "Endstein-Asteroiden. Ab Eisenspitzhacke, droppt sich selbst. Macerator → 2 Wolframstaub; "
                        + "Industrial Grinder → 2 Wolframstaub + 3 kl. Eisen- und 3 kl. Manganstaub.",
                "End only: 4 veins (up to 16 ores) per chunk in end stone, y 0–127, plus floating end stone asteroids. "
                        + "Iron pickaxe or better, drops itself. Macerator → 2 tungsten dust; industrial grinder → 2 "
                        + "tungsten dust + 3 tiny iron and 3 tiny manganese dust.",
                "tungstate_ore");
        add("Nur im End: 1 kleine Ader (bis 4 Erze) je Chunk im Endstein, y 0–127, dazu in schwebenden "
                        + "Endstein-Asteroiden. Ab Diamantspitzhacke, droppt sich selbst. Macerator → 2 Platinstaub; "
                        + "Ofen → Platinbarren; Industrial Grinder → 2 Platinstaub + 1 Nickelstaub + 2 Iridiumnuggets "
                        + "(Quecksilberzelle: 3 Platinstaub).",
                "End only: 1 small vein (up to 4 ores) per chunk in end stone, y 0–127, plus floating end stone "
                        + "asteroids. Diamond pickaxe or better, drops itself. Macerator → 2 platinum dust; furnace → "
                        + "platinum ingot; industrial grinder → 2 platinum dust + 1 nickel dust + 2 iridium nuggets "
                        + "(mercury cell: 3 platinum dust).",
                "sheldonite_ore");
        add("Nur im End: 5 Adern (bis 8 Erze) je Chunk im Endstein, y 0–127, dazu in schwebenden "
                        + "Endstein-Asteroiden. Ab Diamantspitzhacke; droppt Olivin. Industrial Grinder (Erz mit "
                        + "Behutsamkeit) → Olivin + 6 kl. Olivinstaub + 2 kl. Smaragdstaub.",
                "End only: 5 veins (up to 8 ores) per chunk in end stone, y 0–127, plus floating end stone asteroids. "
                        + "Diamond pickaxe or better; drops an olivine. Industrial grinder (ore via silk touch) → "
                        + "olivine + 6 tiny olivine dust + 2 tiny emerald dust.",
                "olivine_ore");
        add("Nur im End: 8 Adern (bis 16 Erze) je Chunk im Endstein, y 0–127, dazu in großen Endstein-Asteroiden. "
                        + "Ab Eisenspitzhacke; droppt 6 Sodalithstaub, oft dazu Aluminiumstaub (mit Behutsamkeit das "
                        + "Erz). Erz: Industrial Grinder → 12 Sodalithstaub + 3 Aluminiumstaub.",
                "End only: 8 veins (up to 16 ores) per chunk in end stone, y 0–127, plus large end stone asteroids. "
                        + "Iron pickaxe or better; drops 6 sodalite dust, often also aluminium dust (the ore with silk "
                        + "touch). Ore: industrial grinder → 12 sodalite dust + 3 aluminium dust.",
                "sodalite_ore");
    }

    /** Rubber trees, oil, drops and machine-only materials (spec 05, 07, 04; phase 23.1). */
    private static void worldAndDrops() {
        add("Gummibäume wachsen in Taiga, Wäldern, Dschungel und Sümpfen (im Sumpf am häufigsten), nicht in der "
                        + "Mining-Dimension. Nasse Harzstellen am Stamm: Baumzapfer → 1–3 Harz, die Stelle füllt sich "
                        + "wieder. Abbauen droppt manchmal Harz. Extraktor → 1 Gummi.",
                "Rubber trees grow in taiga, forests, jungles and swamps (most often in swamps), not in the mining "
                        + "dimension. Wet resin spots on the trunk: treetap → 1–3 resin, the spot refills. Breaking "
                        + "it sometimes drops resin. Extractor → 1 rubber.",
                "rubber_wood");
        add("Blätter des Gummibaums (Taiga, Wälder, Dschungel, Sümpfe); droppen Gummisetzlinge.",
                "Leaves of the rubber tree (taiga, forests, jungles, swamps); drop rubber saplings.",
                "rubber_leaves");
        add("Droppt von Gummibaum-Blättern. Wächst bei Licht ab 9 zum Gummibaum, Knochenmehl lässt ihn sofort "
                        + "wachsen. Extraktor → 1 Gummi.",
                "Dropped by rubber tree leaves. Grows into a rubber tree at light level 9 or more; bone meal makes it "
                        + "grow at once. Extractor → 1 rubber.",
                "rubber_sapling");
        add("Baumzapfer (oder elektrischer Baumzapfer) an einer nassen Harzstelle von Gummiholz: 1–3 Harz. Außerdem "
                        + "beim Abbau von Gummiholz (1/6), von der Crop-Sorte Stickreed, im Ofen aus Schleimbällen und "
                        + "aus UU-Materie. Extraktor → 3 Gummi (Ofen nur 1).",
                "Treetap (or electric treetap) on a wet resin spot of rubber wood: 1–3 resin. Also when breaking "
                        + "rubber wood (1/6), from the Stickreed crop, from slime balls in a furnace and from UU-matter. "
                        + "Extractor → 3 rubber (furnace only 1).",
                "resin");
        add("Droppt Uranerz beim Abbau ohne Behutsamkeit. Kompressor → Uranbarren (für Brennstäbe).",
                "Dropped by uranium ore when mined without silk touch. Compressor → uranium ingot (for fuel rods).",
                "uranium_drop");
        add("Aus GT-Iridiumerz (sehr selten, Oberwelt und Mining-Dimension, Diamantspitzhacke): Abbau 1, Macerator "
                        + "oder Industrial Grinder 2 je Erz. Sonst nur aus UU-Materie. Kompressor → Iridiumbarren.",
                "From GT iridium ore (very rare, overworld and mining dimension, diamond pickaxe): 1 when mined, 2 per "
                        + "ore in a macerator or industrial grinder. Otherwise only from UU-matter. Compressor → "
                        + "iridium ingot.",
                "iridium_ore");
        add("Recycler: jedes Item (außer Glasscheibe, Stock, Schnee, Eis, Holzgerüst) wird verbraucht und gibt mit "
                        + "1/8 Chance 1 Schrott. 9 Schrott → Schrottbox; Verstärker im Massefabrikator.",
                "Recycler: every item (except glass panes, sticks, snow, ice, wooden scaffolds) is used up and gives "
                        + "1 scrap with a 1/8 chance. 9 scrap → scrap box; amplifier in the mass fabricator.",
                "scrap");
        add("Massefabrikator: 1 000 000 EU je UU-Materie. Schrott (5000) oder Schrottbox (45 000) als Verstärker "
                        + "lassen jede EU sechsfach zählen, bis der Wert aufgebraucht ist. Was man daraus machen kann, "
                        + "zeigt EMI unter „Verwendung“.",
                "Mass fabricator: 1,000,000 EU per UU-matter. Scrap (5000) or a scrap box (45,000) as amplifier makes "
                        + "every EU count six times until the value is used up. EMI shows what it makes under \"Uses\".",
                "uu_matter");
        add("Ernte der Crop-Sorte Kaffee (bei Größe 5: 3 Bohnen). Die Sorte entsteht nur durch Kreuzen auf einem "
                        + "Kreuzungsstab (Sorten mit gemeinsamen Attributen kreuzen leichter); danach lassen sich die "
                        + "Bohnen selbst auf Crop-Stäbe pflanzen. Macerator → 3 Kaffeepulver.",
                "Harvest of the coffee crop (at size 5: 3 beans). The crop only appears by cross-breeding on a "
                        + "cross-breeding crop (crops with shared attributes cross more easily); afterwards the beans "
                        + "themselves can be planted on crops. Macerator → 3 coffee powder.",
                "coffee_beans");
        add("Ernte der Crop-Sorte Hopfen (bei Größe 7: 3 Hopfen). Die Sorte entsteht nur durch Kreuzen auf einem "
                        + "Kreuzungsstab (Sorten mit gemeinsamen Attributen kreuzen leichter). Zutat für Bier im Fass.",
                "Harvest of the hops crop (at size 7: 3 hops). The crop only appears by cross-breeding on a "
                        + "cross-breeding crop (crops with shared attributes cross more easily). Ingredient for beer "
                        + "in the barrel.",
                "hops");
        add("Netherwarze auf einen Crop-Stab über einem Schneeblock pflanzen: Sie wird mit der Zeit zur Terra-Warze "
                        + "(Ernte bei Größe 3). Danach lässt sich die Terra-Warze selbst pflanzen. Zentrifuge: 16 + 1 "
                        + "Zelle → Methanzelle.",
                "Plant nether wart on a crop above a snow block: in time it turns into terra wart (harvest at size "
                        + "3). Afterwards terra wart itself can be planted. Centrifuge: 16 + 1 cell → methane cell.",
                "terra_wart");
        add("BuildCraft-Öl, nur in der Oberwelt: kleine Pfützen in Wüsten (etwa 3 % der Chunks), sehr selten "
                        + "unterirdische Ölkugeln (y 20–29) mit Fontäne bis an die Oberfläche. Mit Eimer schöpfen oder "
                        + "mit der Pumpe fördern; Raffinerie → Treibstoff.",
                "BuildCraft oil, overworld only: small pools in deserts (about 3% of chunks), very rarely underground "
                        + "oil spheres (y 20–29) with a fountain up to the surface. Scoop with a bucket or use the pump; "
                        + "refinery → fuel.",
                "oil", "oil_bucket");
    }
}
