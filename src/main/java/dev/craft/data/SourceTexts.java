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
        dusts();
        smallDusts();
        ingots();
        plates();
        gems();
        cells();
        fuelRods();
        misc();
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
        add("Oberwelt und Mining-Dimension, y 5–29, 3 Adern (Größe 8) je Chunk, zu 10 % mit Bleierz gemischt "
                        + "(Thermal Expansion). Ab Eisenspitzhacke, droppt sich selbst. Ofen → Silberbarren; Macerator → 2 "
                        + "Silberstaub; Industrial Grinder → 2 Silberstaub + 2 kl. Bleistaub (Quecksilberzelle: 3).",
                "Overworld and mining dimension, y 5–29, 3 veins (size 8) per chunk, mixed with 10 % lead ore (Thermal "
                        + "Expansion). Iron pickaxe or better, drops itself. Furnace → silver ingot; macerator → 2 silver "
                        + "dust; industrial grinder → 2 silver dust + 2 tiny lead dust (mercury cell: 3).",
                "silver_ore", "deepslate_silver_ore");
        add("Oberwelt und Mining-Dimension, y 10–34, 4 Adern (Größe 8) je Chunk, zu 20 % mit Silbererz gemischt "
                        + "(Thermal Expansion). Ab Eisenspitzhacke, droppt sich selbst. Ofen → Bleibarren; Macerator → 2 "
                        + "Bleistaub; Industrial Grinder → 2 Bleistaub + kl. Silberstaub (Quecksilberzelle: + Silberstaub).",
                "Overworld and mining dimension, y 10–34, 4 veins (size 8) per chunk, mixed with 20 % silver ore (Thermal "
                        + "Expansion). Iron pickaxe or better, drops itself. Furnace → lead ingot; macerator → 2 lead dust; "
                        + "industrial grinder → 2 lead dust + tiny silver dust (mercury cell: + silver dust).",
                "lead_ore", "deepslate_lead_ore");
        add("„Ferrous Ore“ (Thermal Expansion): Oberwelt und Mining-Dimension, y 5–19, 2 kleine Adern (Größe 4) je "
                        + "Chunk. Ab Eisenspitzhacke, droppt sich selbst. Ofen → Nickelbarren; Macerator → 2 Nickelstaub; "
                        + "Industrial Grinder → 3 Nickelstaub + kl. Platin- und kl. Kupferstaub (Quecksilberzelle: + Platinstaub).",
                "Ferrous ore (Thermal Expansion): overworld and mining dimension, y 5–19, 2 small veins (size 4) per chunk. "
                        + "Iron pickaxe or better, drops itself. Furnace → nickel ingot; macerator → 2 nickel dust; industrial "
                        + "grinder → 3 nickel dust + tiny platinum and tiny copper dust (mercury cell: + platinum dust).",
                "nickel_ore", "deepslate_nickel_ore");
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

    /** Dusts (spec 07 §6.4–6.6, spec 13 centrifuge/electrolyzer lists, scrap box; phase 23.2). */
    private static void dusts() {
        add("Macerator: Eisenerz, Rohkupfer oder Kupfer → 2, Barren/Platte → 1. Industrial Grinder: Kupfererz → 2 "
                        + "(+ kl. Gold- und kl. Nickelstaub). 4 kl. Kupferstaub → 1. Auch Mörser, UU-Materie (10) und "
                        + "Schrottbox.",
                "Macerator: copper ore or raw copper → 2, ingot/plate → 1. Industrial grinder: copper ore → 2 (+ tiny "
                        + "gold and tiny nickel dust). 4 tiny copper dust → 1. Also mortar, UU-matter (10) and scrap box.",
                "copper_dust");
        add("Macerator: Zinnerz → 2, Barren/Platte → 1, leere Brennstoffdose → 7. Industrial Grinder: Zinnerz → 2 "
                        + "(+ kl. Eisen- und kl. Zinkstaub). 4 kl. Zinnstaub → 1. Auch UU-Materie (10) und Schrottbox.",
                "Macerator: tin ore → 2, ingot/plate → 1, empty fuel can → 7. Industrial grinder: tin ore → 2 (+ tiny "
                        + "iron and tiny zinc dust). 4 tiny tin dust → 1. Also UU-matter (10) and scrap box.",
                "tin_dust");
        add("Macerator: Eisenerz oder Roheisen → 2, Barren → 1, Eimer → 3, Maschinengehäuse → 8. Industrial Grinder: "
                        + "Eisenerz → 2 (+ kl. Zinn- und kl. Nickelstaub). Elektrolyseur: Pyrit-, Olivin-, Almandin-, "
                        + "Andraditstaub. 2 kl. Eisenstaub → 1. Auch UU-Materie (4) und Schrottbox.",
                "Macerator: iron ore or raw iron → 2, ingot → 1, bucket → 3, machine casing → 8. Industrial grinder: "
                        + "iron ore → 2 (+ tiny tin and tiny nickel dust). Electrolyzer: pyrite, olivine, almandine, "
                        + "andradite dust. 2 tiny iron dust → 1. Also UU-matter (4) and scrap box.",
                "iron_dust");
        add("Macerator: Golderz oder Rohgold → 2, Barren → 1, Goldwerkzeug/-rüstung. Industrial Grinder: Golderz → 2 "
                        + "(Quecksilberzelle: 3), Kupfererz mit Quecksilberzelle → 1. Zentrifuge: 16 Glowstonestaub + "
                        + "Zelle → 8. 4 kl. Goldstaub → 1. Auch UU-Materie (2) und Schrottbox.",
                "Macerator: gold ore or raw gold → 2, ingot → 1, gold tools/armour. Industrial grinder: gold ore → 2 "
                        + "(mercury cell: 3), copper ore with mercury cell → 1. Centrifuge: 16 glowstone dust + cell → 8. "
                        + "4 tiny gold dust → 1. Also UU-matter (2) and scrap box.",
                "gold_dust");
        add("Werkbank: 3 Kupfer- + 1 Zinnstaub → 2 Bronzestaub. Macerator: Bronzebarren/-platte → 1, Bronzeblock → 9, "
                        + "Schraubenschlüssel → 6. Mörser: Bronzebarren → 1. Rotations-Macerator: Kupfer mit Zinn im "
                        + "Zusatzslot → Bronzestaub statt Kupferstaub.",
                "Crafting: 3 copper + 1 tin dust → 2 bronze dust. Macerator: bronze ingot/plate → 1, bronze block → 9, "
                        + "wrench → 6. Mortar: bronze ingot → 1. Rotary macerator: copper with tin in the supplement "
                        + "slot → bronze dust instead of copper dust.",
                "bronze_dust");
        add("Werkbank: 3 Kupfer- + 1 Zinkstaub → 4 Messingstaub (oder 3 kl. Kupfer- + 1 kl. Zinkstaub → 1). "
                        + "Macerator: Messingbarren/-platte → 1. Auch Schrottbox.",
                "Crafting: 3 copper + 1 zinc dust → 4 brass dust (or 3 tiny copper + 1 tiny zinc dust → 1). Macerator: "
                        + "brass ingot/plate → 1. Also scrap box.",
                "brass_dust");
        add("Werkbank: 1 Silber- + 1 Goldstaub → 2 Elektrumstaub (oder je 2 kl. Staub → 1). Macerator: "
                        + "Elektrumbarren/-platte → 1 (Barren auch aus der Zentrifuge: 16 Lava). Auch Schrottbox.",
                "Crafting: 1 silver + 1 gold dust → 2 electrum dust (or 2 tiny of each → 1). Macerator: electrum "
                        + "ingot/plate → 1 (ingots also from the centrifuge: 16 lava). Also scrap box.",
                "electrum_dust");
        add("Macerator: Stahlbarren/-platte → 1, Stahlblock → 9. Stahl selbst kommt aus dem Industriellen Hochofen "
                        + "(raff. Eisen + 2 Kohlestaub). Auch Schrottbox (selten).",
                "Macerator: steel ingot/plate → 1, steel block → 9. Steel itself comes from the industrial blast "
                        + "furnace (refined iron + 2 coal dust). Also scrap box (rare).",
                "steel_dust");
        add("Silbererz (Thermal Expansion): Macerator → 2, Industrial Grinder → 2 (Quecksilber 3) + 2 kl. Bleistaub. Industrial Grinder: Galenaerz + Quecksilberzelle → 1 (+ Galena- und Schwefelstaub). Elektrolyseur: 2 "
                        + "Galenastaub → 3 kl. Silberstaub (+ kl. Blei-, kl. Schwefelstaub). 4 kl. Silberstaub → 1. Auch "
                        + "UU-Materie (14), Macerator aus Barren und Schrottbox.",
                "Silver ore (Thermal Expansion): macerator → 2, industrial grinder → 2 (mercury 3) + 2 tiny lead dust. Industrial grinder: galena ore + mercury cell → 1 (+ galena and sulfur dust). Electrolyzer: 2 galena "
                        + "dust → 3 tiny silver dust (+ tiny lead, tiny sulfur dust). 4 tiny silver dust → 1. Also "
                        + "UU-matter (14), macerator from ingots and scrap box.",
                "silver_dust");
        add("Bleierz (Thermal Expansion): Macerator → 2, Industrial Grinder → 2 + kl. Silberstaub. Elektrolyseur: 2 Galenastaub → 3 kl. Bleistaub (+ kl. Silber-, kl. Schwefelstaub); Zentrifuge: 2 "
                        + "Silberstaub → 1 kl. Bleistaub. 4 kl. Bleistaub → 1. Auch UU-Materie (14), Macerator aus "
                        + "Barren und Schrottbox.",
                "Lead ore (Thermal Expansion): macerator → 2, industrial grinder → 2 + tiny silver dust. Electrolyzer: 2 galena dust → 3 tiny lead dust (+ tiny silver, tiny sulfur dust); centrifuge: 2 silver "
                        + "dust → 1 tiny lead dust. 4 tiny lead dust → 1. Also UU-matter (14), macerator from ingots and "
                        + "scrap box.",
                "lead_dust");
        add("Nickelerz (Thermal Expansion, y 5–19): Macerator → 2, Industrial Grinder → 3 (+ kl. Platin-, kl. "
                        + "Kupferstaub). Sonst als Nebenprodukt: Industrial Grinder: Sheldoniterz → 1 Nickelstaub; Eisen-, Gold- oder Kupfererz → "
                        + "1 kl. Nickelstaub. Zentrifuge: 2 Eisen-, 3 Gold-, 3 Kupferstaub oder 1 Platinstaub → 1 kl. "
                        + "Nickelstaub. 4 kl. Nickelstaub → 1. Sonst UU-Materie (10) und Schrottbox.",
                "Ferrous (nickel) ore (Thermal Expansion, y 5–19): macerator → 2, industrial grinder → 3 (+ tiny "
                        + "platinum, tiny copper dust). Otherwise as a by-product: industrial grinder: sheldonite ore → 1 nickel dust; iron, gold or copper ore → "
                        + "1 tiny nickel dust. Centrifuge: 2 iron, 3 gold, 3 copper or 1 platinum dust → 1 tiny nickel "
                        + "dust. 4 tiny nickel dust → 1. Otherwise UU-matter (10) and scrap box.",
                "nickel_dust");
        add("Industrial Grinder: Sphaleriterz → 1 (+ Sphaleritstaub). Elektrolyseur: 2 Sphaleritstaub → 1 (+ "
                        + "Schwefelstaub). Sphaleriterz droppt ihn manchmal. Kl. Zinkstaub aus dem Grinder (Zinnerz) "
                        + "und der Zentrifuge (2 Zinn- oder 1 Messingstaub); 4 → 1. Auch UU-Materie (10), Schrottbox.",
                "Industrial grinder: sphalerite ore → 1 (+ sphalerite dust). Electrolyzer: 2 sphalerite dust → 1 (+ "
                        + "sulfur dust). Sphalerite ore sometimes drops it. Tiny zinc dust from the grinder (tin ore) and "
                        + "the centrifuge (2 tin or 1 brass dust); 4 → 1. Also UU-matter (10), scrap box.",
                "zinc_dust");
        add("Viele Wege: Industrial Grinder: Bauxiterz → 1, Sodalitherz → 3; Sodalitherz droppt ihn oft. "
                        + "Elektrolyseur: 12 Bauxitstaub → 8 (bester Weg), außerdem Sodalith-, Ton-, Rubin-, Saphir-, "
                        + "Smaragd-, Lazurit- und Granatstäube. Auch UU-Materie (16), Macerator aus Barren, Schrottbox.",
                "Many ways: industrial grinder: bauxite ore → 1, sodalite ore → 3; sodalite ore often drops it. "
                        + "Electrolyzer: 12 bauxite dust → 8 (best way), also sodalite, clay, ruby, sapphire, emerald, "
                        + "lazurite and garnet dusts. Also UU-matter (16), macerator from ingots, scrap box.",
                "aluminium_dust");
        add("Bauxiterz (Oberwelt: Ebenen, Wiese, Wälder; Mining-Dimension): Macerator oder Industrial Grinder → 4. "
                        + "Elektrolyseur: 12 Bauxitstaub + 8 Zellen → 8 Aluminiumstaub + 2 kl. Titanstaub + Wasserstoff. "
                        + "Auch Schrottbox.",
                "Bauxite ore (overworld: plains, meadow, forests; mining dimension): macerator or industrial grinder → "
                        + "4. Electrolyzer: 12 bauxite dust + 8 cells → 8 aluminium dust + 2 tiny titanium dust + "
                        + "hydrogen. Also scrap box.",
                "bauxite_dust");
        add("Elektrolyseur: 12 Bauxitstaub → 2 kl. Titanstaub (+ 8 Aluminiumstaub); 4 kl. → 1. Sonst nur UU-Materie "
                        + "(2), sehr selten aus der Schrottbox. Kein Erz.",
                "Electrolyzer: 12 bauxite dust → 2 tiny titanium dust (+ 8 aluminium dust); 4 tiny → 1. Otherwise only "
                        + "UU-matter (2), very rarely from the scrap box. No ore.",
                "titanium_dust");
        add("Elektrolyseur: 9 Rubinstaub → 1 (+ 2 Aluminiumstaub), 20 Uvarovitstaub → 2. 4 kl. Chromstaub → 1. Sehr "
                        + "selten aus der Schrottbox. Kein Erz.",
                "Electrolyzer: 9 ruby dust → 1 (+ 2 aluminium dust), 20 uvarovite dust → 2. 4 tiny chrome dust → 1. Very "
                        + "rarely from the scrap box. No ore.",
                "chrome_dust");
        add("Elektrolyseur: 9 Olivinstaub → 2 (+ 2 Eisenstaub), 20 Pyropstaub → 3; 4 Obsidianstaub → 2 kl. "
                        + "Magnesiumstaub. 4 kl. → 1. Sehr selten aus der Schrottbox.",
                "Electrolyzer: 9 olivine dust → 2 (+ 2 iron dust), 20 pyrope dust → 3; 4 obsidian dust → 2 tiny "
                        + "magnesium dust. 4 tiny → 1. Very rarely from the scrap box.",
                "magnesium_dust");
        add("Elektrolyseur: 20 Spessartinstaub → 3 (Spessartin aus rotem Granatstaub in der Zentrifuge). Industrial "
                        + "Grinder: Wolframiterz → 3 kl. Manganstaub. 4 kl. → 1.",
                "Electrolyzer: 20 spessartine dust → 3 (spessartine from red garnet dust in the centrifuge). Industrial "
                        + "grinder: tungstate ore → 3 tiny manganese dust. 4 tiny → 1.",
                "manganese_dust");
        add("Wolframiterz (nur End): Macerator oder Industrial Grinder → 2, Ofen → 1. Zentrifuge: 4 Uranstaub + 4 "
                        + "Zellen → 1; 16 Lava → 1 kl. Wolframstaub. 4 kl. → 1. Auch UU-Materie (6), sehr selten "
                        + "Schrottbox.",
                "Tungstate ore (End only): macerator or industrial grinder → 2, furnace → 1. Centrifuge: 4 uranium dust "
                        + "+ 4 cells → 1; 16 lava → 1 tiny tungsten dust. 4 tiny → 1. Also UU-matter (6), very rarely "
                        + "scrap box.",
                "tungsten_dust");
        add("Sheldoniterz (nur End): Macerator oder Industrial Grinder → 2 (Quecksilberzelle: 3). Industrial Grinder: "
                        + "Iridiumerz → 2 kl. Platinstaub (Quecksilberzelle: 1 Platinstaub). 4 kl. → 1. Auch Macerator "
                        + "aus Barren, UU-Materie (1), sehr selten Schrottbox.",
                "Sheldonite ore (End only): macerator or industrial grinder → 2 (mercury cell: 3). Industrial grinder: "
                        + "iridium ore → 2 tiny platinum dust (mercury cell: 1 platinum dust). 4 tiny → 1. Also macerator "
                        + "from ingots, UU-matter (1), very rarely scrap box.",
                "platinum_dust");
        add("Uranerz: Macerator oder Industrial Grinder → 2 (Grinder + 2 kl. Plutoniumstaub). Für die Zentrifuge "
                        + "(Thorium, Plutonium, Wolfram, Uranzellen).",
                "Uranium ore: macerator or industrial grinder → 2 (grinder + 2 tiny plutonium dust). For the centrifuge "
                        + "(thorium, plutonium, tungsten, uranium cells).",
                "uranium_dust");
        add("Industrial Grinder: Uranerz → 2 kl. Plutoniumstaub; Zentrifuge: 4 Uranstaub → 1 kl., 8 "
                        + "wiederangereicherte Uranzellen → 1 Plutoniumstaub. 4 kl. → 1.",
                "Industrial grinder: uranium ore → 2 tiny plutonium dust; centrifuge: 4 uranium dust → 1 tiny, 8 "
                        + "re-enriched uranium cells → 1 plutonium dust. 4 tiny → 1.",
                "plutonium_dust");
        add("Zentrifuge: 4 Uranstaub + 4 Zellen → 2, fast verbrauchte Uranzelle → 1, 8 wiederangereicherte "
                        + "Uranzellen → 4. Industrial Grinder: Kohleerz → 1 kl. Thoriumstaub. 4 kl. → 1.",
                "Centrifuge: 4 uranium dust + 4 cells → 2, near-depleted uranium cell → 1, 8 re-enriched uranium cells → "
                        + "4. Industrial grinder: coal ore → 1 tiny thorium dust. 4 tiny → 1.",
                "thorium_dust");
        add("Galenaerz (Oberwelt y 0–31, Mining-Dimension): Macerator oder Industrial Grinder → 2. Elektrolyseur: 2 "
                        + "Galenastaub → je 3 kl. Silber- und kl. Bleistaub + 2 kl. Schwefelstaub.",
                "Galena ore (overworld y 0–31, mining dimension): macerator or industrial grinder → 2. Electrolyzer: 2 "
                        + "galena dust → 3 tiny silver and 3 tiny lead dust each + 2 tiny sulfur dust.",
                "galena_dust");
        add("Viele Wege: Industrial Grinder: Galena- (1) und Pyriterz (2). Elektrolyseur: 3 Pyrit-, 2 Sphalerit-, 2 "
                        + "Zinnoberstaub oder 4 Lohenstaub. Zentrifuge: 16 Netherrackstaub → 4, Schwefelzelle → 1. "
                        + "4 kl. → 1. Auch Schrottbox.",
                "Many ways: industrial grinder: galena (1) and pyrite ore (2). Electrolyzer: 3 pyrite, 2 sphalerite, 2 "
                        + "cinnabar dust or 4 blaze powder. Centrifuge: 16 netherrack dust → 4, sulfur cell → 1. 4 tiny → "
                        + "1. Also scrap box.",
                "sulfur_dust");
        add("Zentrifuge: 16 Seelensand + 1 Zelle → 4 Salpeterstaub (+ Ölzelle, Kohlestaub, Sand). Auch Schrottbox. "
                        + "Kein Erz.",
                "Centrifuge: 16 soul sand + 1 cell → 4 saltpeter dust (+ oil cell, coal dust, sand). Also scrap box. No "
                        + "ore.",
                "saltpeter_dust");
        add("Pyriterz (nur Nether) droppt 2 (mit Glück mehr); Macerator oder Industrial Grinder: Erz → 5. "
                        + "Zentrifuge: 10 Redstone + 4 Zellen → 5. 4 kl. → 1. Auch Schrottbox.",
                "Pyrite ore (Nether only) drops 2 (more with fortune); macerator or industrial grinder: ore → 5. "
                        + "Centrifuge: 10 redstone + 4 cells → 5. 4 tiny → 1. Also scrap box.",
                "pyrite_dust");
        add("Zinnobererz (nur Nether) droppt 2 (mit Glück mehr); Erz: Macerator → 3, Industrial Grinder → 5. "
                        + "Elektrolyseur: 2 Zinnoberstaub + Zelle → Quecksilberzelle + Schwefelstaub.",
                "Cinnabar ore (Nether only) drops 2 (more with fortune); ore: macerator → 3, industrial grinder → 5. "
                        + "Electrolyzer: 2 cinnabar dust + cell → mercury cell + sulfur dust.",
                "cinnabar_dust");
        add("Sphaleriterz (nur Nether) droppt 2 (mit Glück mehr); Erz: Macerator → 4, Industrial Grinder → 5 (+ 1 "
                        + "Zinkstaub). Elektrolyseur: 2 → 1 Zinkstaub + 1 Schwefelstaub.",
                "Sphalerite ore (Nether only) drops 2 (more with fortune); ore: macerator → 4, industrial grinder → 5 (+ 1 "
                        + "zinc dust). Electrolyzer: 2 → 1 zinc dust + 1 sulfur dust.",
                "sphalerite_dust");
        add("Sodalitherz (nur End) droppt 6 (mit Glück mehr); Erz: Macerator oder Industrial Grinder → 12. "
                        + "Zentrifuge: 4 Lapislazuli → 2 kl. Sodalithstaub. 4 kl. → 1. Auch Schrottbox. Elektrolyseur → "
                        + "Natrium, Aluminium, Silizium, Chlor.",
                "Sodalite ore (End only) drops 6 (more with fortune); ore: macerator or industrial grinder → 12. "
                        + "Centrifuge: 4 lapis lazuli → 2 tiny sodalite dust. 4 tiny → 1. Also scrap box. Electrolyzer → "
                        + "sodium, aluminium, silicon, chlorine.",
                "sodalite_dust");
        add("Zentrifuge: 4 Lapislazuli → 3 (+ kl. Pyrit-, Calcit-, Sodalithstaub). Industrial Grinder: Lapiserz → 12 "
                        + "Lapislazuli + 3 Lazuritstaub. Auch Schrottbox.",
                "Centrifuge: 4 lapis lazuli → 3 (+ tiny pyrite, calcite, sodalite dust). Industrial grinder: lapis ore → "
                        + "12 lapis lazuli + 3 lazurite dust. Also scrap box.",
                "lazurite_dust");
        add("Zentrifuge: 4 Lapislazuli → 1 kl. Calcitstaub; Calciumcarbonatzelle → 1. 4 kl. → 1. Auch Schrottbox. "
                        + "Kein Erz.",
                "Centrifuge: 4 lapis lazuli → 1 tiny calcite dust; calcium carbonate cell → 1. 4 tiny → 1. Also scrap box. "
                        + "No ore.",
                "calcite_dust");
        add("Macerator: Kohle → 1, Kohleerz → 2. Industrial Grinder: Kohleerz → 1 (+ Kohle, kl. Thoriumstaub). "
                        + "Zentrifuge: 16 Netherrackstaub oder 16 Seelensand. Ofen: hydrierter Kohlestaub → 1. Auch "
                        + "Schrottbox.",
                "Macerator: coal → 1, coal ore → 2. Industrial grinder: coal ore → 1 (+ coal, tiny thorium dust). "
                        + "Centrifuge: 16 netherrack dust or 16 soul sand. Furnace: hydrated coal dust → 1. Also scrap box.",
                "coal_dust");
        add("Werkbank: 8 Kohlestaub um einen Wassereimer oder eine Wasserzelle → 8 (oder 1 + 1 → 1). Industrial "
                        + "Grinder: Diamanterz → 1. Rotations-Macerator: Kohle mit Wasserzelle im Zusatzslot.",
                "Crafting: 8 coal dust around a water bucket or water cell → 8 (or 1 + 1 → 1). Industrial grinder: "
                        + "diamond ore → 1. Rotary macerator: coal with a water cell in the supplement slot.",
                "hydrated_coal_dust");
        add("Macerator: Holzkohle → 1. Auch Schrottbox (häufig).",
                "Macerator: charcoal → 1. Also scrap box (common).",
                "charcoal_dust");
        add("Macerator: Tonblock → 2. Mörser: Tonblock → 1.",
                "Macerator: clay block → 2. Mortar: clay block → 1.",
                "clay_dust");
        add("Macerator: Feuerstein → 2 kl. Feuersteinstaub; 4 kl. → 1.",
                "Macerator: flint → 2 tiny flint dust; 4 tiny → 1.",
                "flint_dust");
        add("Macerator: Obsidian → 1. Auch Schrottbox.",
                "Macerator: obsidian → 1. Also scrap box.",
                "obsidian_dust");
        add("Macerator: Enderperle → 1. Für Kalium im Elektrolyseur.",
                "Macerator: ender pearl → 1. For potassium in the electrolyzer.",
                "ender_pearl_dust");
        add("Industrial Grinder: 16 Netherrack + Wasserzelle → 16 (+ Goldnugget), mit Quecksilberzelle aus 8. "
                        + "Zentrifuge: 16 → Redstone, 4 Schwefel-, 1 Kohlestaub, Goldnugget.",
                "Industrial grinder: 16 netherrack + water cell → 16 (+ gold nugget), with a mercury cell from 8. "
                        + "Centrifuge: 16 → redstone, 4 sulfur, 1 coal dust, gold nugget.",
                "netherrack_dust");
        add("Macerator: Diamant oder Industriediamant → 1, Diamantwerkzeug/-rüstung, Zaubertisch. Industrial Grinder: "
                        + "Diamanterz → 6 kl. Diamantstaub (+ Diamant). 4 kl. → 1.",
                "Macerator: diamond or industrial diamond → 1, diamond tools/armour, enchanting table. Industrial "
                        + "grinder: diamond ore → 6 tiny diamond dust (+ diamond). 4 tiny → 1.",
                "diamond_dust");
        add("Macerator: Smaragd → 1. Industrial Grinder: Smaragderz → 6 kl. (+ Smaragd), Olivinerz → 2 kl. "
                        + "Smaragdstaub. 4 kl. → 1. Elektrolyseur → Beryllium.",
                "Macerator: emerald → 1. Industrial grinder: emerald ore → 6 tiny (+ emerald), olivine ore → 2 tiny "
                        + "emerald dust. 4 tiny → 1. Electrolyzer → beryllium.",
                "emerald_dust");
        add("Macerator: Rubin → 1, Rubinerz → 2, Rubinwerkzeuge. Industrial Grinder: Rubinerz → 6 kl. Rubinstaub. "
                        + "Zentrifuge: 10 Redstone + 4 Zellen → 1. 4 kl. → 1. Elektrolyseur → Aluminium + Chrom.",
                "Macerator: ruby → 1, ruby ore → 2, ruby tools. Industrial grinder: ruby ore → 6 tiny ruby dust. "
                        + "Centrifuge: 10 redstone + 4 cells → 1. 4 tiny → 1. Electrolyzer → aluminium + chrome.",
                "ruby_dust");
        add("Macerator: Saphir → 1, Saphirerz → 2, Saphirwerkzeuge. Industrial Grinder: Saphirerz → 6 kl. "
                        + "Saphirstaub. Elektrolyseur: grüner Saphirstaub → 1. 4 kl. → 1.",
                "Macerator: sapphire → 1, sapphire ore → 2, sapphire tools. Industrial grinder: sapphire ore → 6 tiny "
                        + "sapphire dust. Electrolyzer: green sapphire dust → 1. 4 tiny → 1.",
                "sapphire_dust");
        add("Macerator: grüner Saphir → 1 (droppt selten aus Saphirerz), Werkzeuge daraus. Industrial Grinder: "
                        + "Saphirerz → 2 kl. grüner Saphirstaub. 4 kl. → 1.",
                "Macerator: green sapphire → 1 (rare drop of sapphire ore), tools made of it. Industrial grinder: "
                        + "sapphire ore → 2 tiny green sapphire dust. 4 tiny → 1.",
                "green_sapphire_dust");
        add("Macerator: Olivin → 1, Olivinerz → 2 (nur End). Industrial Grinder: Olivinerz → 6 kl., Smaragderz → 2 "
                        + "kl. Olivinstaub. 4 kl. → 1. Elektrolyseur → Magnesium, Eisen.",
                "Macerator: olivine → 1, olivine ore → 2 (End only). Industrial grinder: olivine ore → 6 tiny, emerald "
                        + "ore → 2 tiny olivine dust. 4 tiny → 1. Electrolyzer → magnesium, iron.",
                "olivine_dust");
        add("Macerator: roter Granat → 1 (droppt selten aus Rubinerz). Industrial Grinder: Rubinerz → 2 kl. roter "
                        + "Granatstaub. 4 kl. → 1. Zentrifuge: 16 → Pyrop-, Almandin-, Spessartinstaub.",
                "Macerator: red garnet → 1 (rare drop of ruby ore). Industrial grinder: ruby ore → 2 tiny red garnet "
                        + "dust. 4 tiny → 1. Centrifuge: 16 → pyrope, almandine, spessartine dust.",
                "red_garnet_dust");
        add("Macerator: gelber Granat → 1 (droppt selten aus Sphaleriterz). Industrial Grinder: Sphaleriterz → 1 kl. "
                        + "gelber Granatstaub. 4 kl. → 1. Zentrifuge: 16 → Andradit-, Grossular-, Uvarovitstaub.",
                "Macerator: yellow garnet → 1 (rare drop of sphalerite ore). Industrial grinder: sphalerite ore → 1 tiny "
                        + "yellow garnet dust. 4 tiny → 1. Centrifuge: 16 → andradite, grossular, uvarovite dust.",
                "yellow_garnet_dust");
        add("Zentrifuge: 16 roter Granatstaub → 3 Pyrop-, 5 Almandin-, 8 Spessartinstaub. Elektrolyseur zerlegt sie "
                        + "weiter (Magnesium, Eisen, Mangan, Aluminium).",
                "Centrifuge: 16 red garnet dust → 3 pyrope, 5 almandine, 8 spessartine dust. The electrolyzer splits "
                        + "them further (magnesium, iron, manganese, aluminium).",
                "pyrope_dust", "almandine_dust", "spessartine_dust");
        add("Zentrifuge: 16 gelber Granatstaub → 5 Andradit-, 8 Grossular-, 3 Uvarovitstaub. Elektrolyseur zerlegt "
                        + "sie weiter (Calcium, Eisen, Chrom, Aluminium).",
                "Centrifuge: 16 yellow garnet dust → 5 andradite, 8 grossular, 3 uvarovite dust. The electrolyzer splits "
                        + "them further (calcium, iron, chrome, aluminium).",
                "andradite_dust", "grossular_dust", "uvarovite_dust");
    }

    /** Tiny piles of dust: where they appear as by-products (phase 23.2). */
    private static void smallDusts() {
        small("copper", "Kupferstaub", "copper dust",
                "Zentrifuge: 1 Messingstaub → 3, 1 Bronzestaub → 6, 3 Gold- oder 3 Nickelstaub → 1; Industrial Grinder: "
                        + "Golderz → 1",
                "centrifuge: 1 brass dust → 3, 1 bronze dust → 6, 3 gold or 3 nickel dust → 1; industrial grinder: gold "
                        + "ore → 1");
        small("tin", "Zinnstaub", "tin dust",
                "Zentrifuge: 1 Bronzestaub → 2, 2 Eisenstaub → 1, 1 Zinkstaub → 1; Industrial Grinder: Eisenerz → 1",
                "centrifuge: 1 bronze dust → 2, 2 iron dust → 1, 1 zinc dust → 1; industrial grinder: iron ore → 1");
        small("gold", "Goldstaub", "gold dust",
                "Zentrifuge: 3 Kupfer- oder 3 Nickelstaub → 1, 1 Elektrumstaub → 2; Industrial Grinder: Kupfererz → 1",
                "centrifuge: 3 copper or 3 nickel dust → 1, 1 electrum dust → 2; industrial grinder: copper ore → 1");
        small("nickel", "Nickelstaub", "nickel dust",
                "Industrial Grinder: Eisen-, Gold- oder Kupfererz → 1; Zentrifuge: 2 Eisen-, 3 Gold-, 3 Kupfer- oder "
                        + "1 Platinstaub → 1",
                "industrial grinder: iron, gold or copper ore → 1; centrifuge: 2 iron, 3 gold, 3 copper or 1 platinum "
                        + "dust → 1");
        small("zinc", "Zinkstaub", "zinc dust",
                "Industrial Grinder: Zinnerz → 1; Zentrifuge: 2 Zinn- oder 1 Messingstaub → 1",
                "industrial grinder: tin ore → 1; centrifuge: 2 tin or 1 brass dust → 1");
        small("silver", "Silberstaub", "silver dust",
                "Elektrolyseur: 2 Galenastaub → 3; Zentrifuge: 1 Elektrumstaub → 2, 2 Bleistaub → 1",
                "electrolyzer: 2 galena dust → 3; centrifuge: 1 electrum dust → 2, 2 lead dust → 1");
        small("lead", "Bleistaub", "lead dust",
                "Elektrolyseur: 2 Galenastaub → 3; Zentrifuge: 2 Silberstaub → 1",
                "electrolyzer: 2 galena dust → 3; centrifuge: 2 silver dust → 1");
        small("sulfur", "Schwefelstaub", "sulfur dust",
                "Elektrolyseur: 2 Galenastaub → 2",
                "electrolyzer: 2 galena dust → 2");
        small("platinum", "Platinstaub", "platinum dust",
                "Industrial Grinder: Iridiumerz (Block) → 2",
                "industrial grinder: iridium ore (block) → 2");
        small("plutonium", "Plutoniumstaub", "plutonium dust",
                "Industrial Grinder: Uranerz → 2; Zentrifuge: 4 Uranstaub + 4 Zellen → 1",
                "industrial grinder: uranium ore → 2; centrifuge: 4 uranium dust + 4 cells → 1");
        small("thorium", "Thoriumstaub", "thorium dust",
                "Industrial Grinder: Kohleerz → 1",
                "industrial grinder: coal ore → 1");
        small("tungsten", "Wolframstaub", "tungsten dust",
                "Zentrifuge: 16 Lava (Tank oder Zellen) → 1",
                "centrifuge: 16 lava (tank or cells) → 1");
        small("titanium", "Titanstaub", "titanium dust",
                "Elektrolyseur: 12 Bauxitstaub → 2 (einziger Weg ohne UU-Materie)",
                "electrolyzer: 12 bauxite dust → 2 (the only way without UU-matter)");
        add("Werkbank: 1 Chromstaub → 4, 4 → 1. Entsteht nirgends als Nebenprodukt; Chromstaub selbst kommt aus dem "
                        + "Elektrolyseur (Rubin-, Uvarovitstaub).",
                "Crafting: 1 chrome dust → 4, 4 → 1. Never a by-product; chrome dust itself comes from the electrolyzer "
                        + "(ruby, uvarovite dust).",
                "small_chrome_dust");
        small("magnesium", "Magnesiumstaub", "magnesium dust",
                "Elektrolyseur: 4 Obsidianstaub → 2",
                "electrolyzer: 4 obsidian dust → 2");
        small("manganese", "Manganstaub", "manganese dust",
                "Industrial Grinder: Wolframiterz → 3",
                "industrial grinder: tungstate ore → 3");
        small("calcite", "Calcitstaub", "calcite dust",
                "Zentrifuge: 4 Lapislazuli → 1",
                "centrifuge: 4 lapis lazuli → 1");
        small("pyrite", "Pyritstaub", "pyrite dust",
                "Zentrifuge: 4 Lapislazuli → 1",
                "centrifuge: 4 lapis lazuli → 1");
        small("sodalite", "Sodalithstaub", "sodalite dust",
                "Zentrifuge: 4 Lapislazuli → 2",
                "centrifuge: 4 lapis lazuli → 2");
        small("diamond", "Diamantstaub", "diamond dust",
                "Industrial Grinder: Diamanterz → 6",
                "industrial grinder: diamond ore → 6");
        small("emerald", "Smaragdstaub", "emerald dust",
                "Industrial Grinder: Smaragderz → 6, Olivinerz → 2",
                "industrial grinder: emerald ore → 6, olivine ore → 2");
        small("olivine", "Olivinstaub", "olivine dust",
                "Industrial Grinder: Olivinerz → 6, Smaragderz → 2",
                "industrial grinder: olivine ore → 6, emerald ore → 2");
        small("ruby", "Rubinstaub", "ruby dust",
                "Industrial Grinder: Rubinerz → 6",
                "industrial grinder: ruby ore → 6");
        small("sapphire", "Saphirstaub", "sapphire dust",
                "Industrial Grinder: Saphirerz → 6",
                "industrial grinder: sapphire ore → 6");
        small("green_sapphire", "grünen Saphirstaub", "green sapphire dust",
                "Industrial Grinder: Saphirerz → 2",
                "industrial grinder: sapphire ore → 2");
        small("red_garnet", "roten Granatstaub", "red garnet dust",
                "Industrial Grinder: Rubinerz → 2",
                "industrial grinder: ruby ore → 2");
        small("yellow_garnet", "gelben Granatstaub", "yellow garnet dust",
                "Industrial Grinder: Sphaleriterz → 1",
                "industrial grinder: sphalerite ore → 1");
        small("flint", "Feuersteinstaub", "flint dust",
                "Macerator: Feuerstein → 2",
                "macerator: flint → 2");
        add("Nebenprodukt aus Industrial Grinder (Zinn- oder Wolframiterz), Zentrifuge (2 Zinn- oder 3 Nickelstaub) "
                        + "und Elektrolyseur (4 Obsidianstaub → 2); Macerator: 2 Eisenzäune → 3; Crop-Sorte Ferru über "
                        + "Eisenerz. 2 kl. Eisenstaub → 1 Eisenstaub.",
                "By-product of the industrial grinder (tin or tungstate ore), centrifuge (2 tin or 3 nickel dust) and "
                        + "electrolyzer (4 obsidian dust → 2); macerator: 2 iron fences → 3; Ferru crop above iron ore. "
                        + "2 tiny iron dust → 1 iron dust.",
                "small_iron_dust");
        add("Werkbank: 1 Redstone → 4. Industrial Grinder: Zinnobererz → 2. 4 kl. → 1 Redstone.",
                "Crafting: 1 redstone → 4. Industrial grinder: cinnabar ore → 2. 4 tiny → 1 redstone.",
                "small_redstone_dust");
        add("Werkbank: 1 Glowstonestaub → 4. Industrial Grinder: Redstone-Erz → 2, Zinnobererz → 1. 4 kl. → 1 "
                        + "Glowstonestaub.",
                "Crafting: 1 glowstone dust → 4. Industrial grinder: redstone ore → 2, cinnabar ore → 1. 4 tiny → 1 "
                        + "glowstone dust.",
                "small_glowstone_dust");
    }

    /** Ingots and nuggets (generated recipes; blast furnace heat spec 13; phase 23.3). */
    private static void ingots() {
        add("Ofen: Zinnerz oder Zinnstaub → 1, Platte → 1, Zinndose/leere Brennstoffdose. Zentrifuge: 16 Lava → 2 "
                        + "(16 Lavazellen → 10). Auch UU-Materie über Zinnstaub.",
                "Furnace: tin ore or tin dust → 1, plate → 1, tin can/empty fuel can. Centrifuge: 16 lava → 2 (16 lava "
                        + "cells → 10). Also UU-matter via tin dust.",
                "tin_ingot");
        add("Werkbank: 3 Kupfer- + 1 Zinnbarren → 1 (GregTech). Ofen: Bronzestaub → 1 (3 Kupfer- + 1 Zinnstaub → 2 "
                        + "Staub, also günstiger).",
                "Crafting: 3 copper + 1 tin ingot → 1 (GregTech). Furnace: bronze dust → 1 (3 copper + 1 tin dust → 2 "
                        + "dust, so cheaper).",
                "bronze_ingot");
        add("Ofen: Eisenbarren → 1. Industrieller Hochofen (1000 K): Eisenerz + Calciumcarbonatzelle → 3, Pyriterz + "
                        + "Calciumcarbonatzelle → 2 (1500 K). Werkbank/Ofen: Maschinengehäuse → 8.",
                "Furnace: iron ingot → 1. Industrial blast furnace (1000 K): iron ore + calcium carbonate cell → 3, "
                        + "pyrite ore + calcium carbonate cell → 2 (1500 K). Crafting/furnace: machine casing → 8.",
                "refined_iron_ingot");
        add("Rotations-Macerator (Advanced Machines): Eisen (Erz, Barren, Rohmetall) mahlen, mit Kohle oder Holzkohle "
                        + "im Zusatzslot – 1 Kohle reicht für 128 Vorgänge. Ofen → Raffiniertes Eisen.",
                "Rotary macerator (Advanced Machines): grind iron (ore, ingot, raw metal) with coal or charcoal in the "
                        + "supplement slot – 1 coal lasts 128 operations. Furnace → refined iron.",
                "refined_iron_dust");
        add("Werkbank: 2 Eisen- + 1 Nickelstaub → 3 (Thermal Expansion). Ofen → Invarbarren.",
                "Crafting: 2 iron + 1 nickel dust → 3 (Thermal Expansion). Furnace → invar ingot.",
                "invar_dust");
        add("Ofen: Invarstaub → 1 (Invarstaub: 2 Eisen- + 1 Nickelstaub).",
                "Furnace: invar dust → 1 (invar dust: 2 iron + 1 nickel dust).",
                "invar_ingot");
        add("Industrieller Hochofen (1000 K): raff. Eisen + 2 Kohlestaub → 1 (+ 2 dunkle Asche), Stahlstaub → 1. Ofen: "
                        + "Platte → 1, Stahlblock → 9.",
                "Industrial blast furnace (1000 K): refined iron + 2 coal dust → 1 (+ 2 dark ashes), steel dust → 1. "
                        + "Furnace: plate → 1, steel block → 9.",
                "steel_ingot");
        add("Industrieller Hochofen (1700 K, z. B. 34 verstärkte Gehäuse): Aluminiumstaub → 1. Ofen: Platte → 1.",
                "Industrial blast furnace (1700 K, e.g. 34 reinforced casings): aluminium dust → 1. Furnace: plate → 1.",
                "aluminium_ingot");
        add("Industrieller Hochofen (1700 K): Chromstaub → 1 (800 Ticks). Ofen: Platte → 1.",
                "Industrial blast furnace (1700 K): chrome dust → 1 (800 ticks). Furnace: plate → 1.",
                "chrome_ingot");
        add("Industrieller Hochofen (1500 K): Titanstaub → 1 (1000 Ticks). Ofen: Platte → 1.",
                "Industrial blast furnace (1500 K): titanium dust → 1 (1000 ticks). Furnace: plate → 1.",
                "titanium_ingot");
        add("Industrieller Hochofen (2500 K, fortgeschrittene Gehäuse + Heizspulen): Wolframstaub → 1 (2000 Ticks). "
                        + "Ofen: Platte → 1.",
                "Industrial blast furnace (2500 K, advanced casings + heating coils): tungsten dust → 1 (2000 ticks). "
                        + "Furnace: plate → 1.",
                "tungsten_ingot");
        add("Industrieller Hochofen (3000 K): Wolfram- + Stahlbarren → 2 (2000 Ticks). Muss im Vakuumgefrierer zu "
                        + "Wolframstahl abgekühlt werden.",
                "Industrial blast furnace (3000 K): tungsten + steel ingot → 2 (2000 ticks). Must be cooled into "
                        + "tungstensteel in the vacuum freezer.",
                "hot_tungstensteel_ingot");
        add("Vakuumgefrierer: heißer Wolframstahlbarren → 1 (450 Ticks, 128 EU/t). Ofen: Platte → 1.",
                "Vacuum freezer: hot tungstensteel ingot → 1 (450 ticks, 128 EU/t). Furnace: plate → 1.",
                "tungstensteel_ingot");
        add("Ofen: Silbererz → 1. Ofen: Silberstaub → 1. Industrieller Hochofen (1500 K): 2 Galenastaub → 1 Silber- + 1 Bleibarren.",
                "Furnace: silver ore → 1. Furnace: silver dust → 1. Industrial blast furnace (1500 K): 2 galena dust → 1 silver + 1 lead ingot.",
                "silver_ingot");
        add("Ofen: Bleierz → 1. Ofen: Bleistaub → 1. Industrieller Hochofen (1500 K): 2 Galenastaub → 1 Blei- + 1 Silberbarren.",
                "Furnace: lead ore → 1. Furnace: lead dust → 1. Industrial blast furnace (1500 K): 2 galena dust → 1 lead + 1 silver ingot.",
                "lead_ingot");
        add("Ofen: Nickelerz → 1. Ofen: Nickelstaub → 1 (siehe Nickelstaub). Ofen: Platte → 1.",
                "Furnace: ferrous (nickel) ore → 1. Furnace: nickel dust → 1 (see nickel dust). Furnace: plate → 1.",
                "nickel_ingot");
        add("Ofen: Zinkstaub → 1 (aus Sphalerit, Nether). Ofen: Platte → 1.",
                "Furnace: zinc dust → 1 (from sphalerite, Nether). Furnace: plate → 1.",
                "zinc_ingot");
        add("Ofen: Messingstaub → 1 (3 Kupfer- + 1 Zinkstaub → 4). Ofen: Platte → 1.",
                "Furnace: brass dust → 1 (3 copper + 1 zinc dust → 4). Furnace: plate → 1.",
                "brass_ingot");
        add("Ofen: Elektrumstaub → 1 (Silber- + Goldstaub). Zentrifuge: 16 Lava → 1. Ofen: Platte → 1.",
                "Furnace: electrum dust → 1 (silver + gold dust). Centrifuge: 16 lava → 1. Furnace: plate → 1.",
                "electrum_ingot");
        add("Ofen: Platinstaub oder Sheldoniterz (nur End) → 1. Ofen: Platte → 1.",
                "Furnace: platinum dust or sheldonite ore (End only) → 1. Furnace: plate → 1.",
                "platinum_ingot");
        add("Kompressor: Uranerz, Uranerz-Brocken oder Uranstaub → 1. Ofen: Uranblock → 9.",
                "Compressor: uranium ore, uranium drop or uranium dust → 1. Furnace: uranium block → 9.",
                "uranium_ingot");
        add("Kompressor: Plutoniumstaub → 1 (Plutonium aus Uran, siehe Plutoniumstaub).",
                "Compressor: plutonium dust → 1 (plutonium from uranium, see plutonium dust).",
                "plutonium_ingot");
        add("Kompressor: Thoriumstaub → 1 (Thorium aus der Zentrifuge, siehe Thoriumstaub).",
                "Compressor: thorium dust → 1 (thorium from the centrifuge, see thorium dust).",
                "thorium_ingot");
        add("Kompressor: Iridiumerz → 1 oder 9 Iridiumnuggets → 1 (auch Werkbank). Iridiumerz: sehr seltenes "
                        + "GT-Iridiumerz oder UU-Materie; Nuggets aus Sheldonit (End).",
                "Compressor: iridium ore → 1 or 9 iridium nuggets → 1 (also crafting). Iridium ore: very rare GT "
                        + "iridium ore or UU-matter; nuggets from sheldonite (End).",
                "iridium_ingot");
        add("Industrial Grinder: Sheldoniterz → 2. Zentrifuge: 1 Platinstaub → 1 (+ kl. Nickelstaub). 1 Iridiumbarren "
                        + "→ 9.",
                "Industrial grinder: sheldonite ore → 2. Centrifuge: 1 platinum dust → 1 (+ tiny nickel dust). 1 iridium "
                        + "ingot → 9.",
                "iridium_nugget");
        add("Werkbank: 4 Iridiumbarren, 4 fortgeschrittene Legierung, 1 Diamantstaub (IAI/ADA/IAI) → 1. "
                        + "Implosionskompressor → Iridiumplatte.",
                "Crafting: 4 iridium ingots, 4 advanced alloys, 1 diamond dust (IAI/ADA/IAI) → 1. Implosion compressor "
                        + "→ iridium plate.",
                "iridium_alloy_ingot");
        add("Werkbank, drei Platten senkrecht: oben raff. Eisen/Nickel (1), Stahl (2–3), Titan/Wolfram (3–4) oder "
                        + "Wolframstahl (5–6), Mitte Bronze oder Messing, unten Zinn/Zink oder Aluminium (mehr Barren).",
                "Crafting, three plates stacked: top refined iron/nickel (1), steel (2–3), titanium/tungsten (3–4) or "
                        + "tungstensteel (5–6), middle bronze or brass, bottom tin/zinc or aluminium (more ingots).",
                "mixed_metal_ingot");
    }

    /** Plates: plate bending machine from ingots, special plates (phase 23.3). */
    private static void plates() {
        for (String metal : List.of("copper", "tin", "bronze", "iron", "gold", "steel", "aluminium", "brass",
                "chrome", "electrum", "lead", "nickel", "platinum", "silver", "titanium", "tungsten", "zinc")) {
            add("Plattenbieger: 1 Barren → 1 Platte (50 Ticks, 20 EU/t). Zurück: Ofen → Barren, Macerator → 1 Staub.",
                    "Plate bending machine: 1 ingot → 1 plate (50 ticks, 20 EU/t). Back: furnace → ingot, macerator → "
                            + "1 dust.",
                    metal + "_plate");
        }
        add("Plattenbieger: 1 Barren → 1 Platte (50 Ticks, 20 EU/t). Zurück: Ofen → Barren.",
                "Plate bending machine: 1 ingot → 1 plate (50 ticks, 20 EU/t). Back: furnace → ingot.",
                "refined_iron_plate", "tungstensteel_plate");
        add("Kompressor: 8 Kupferplatten → 1 (Plattenbieger aus Kupferbarren). Macerator → 8 Kupferstaub.",
                "Compressor: 8 copper plates → 1 (plate bending machine from copper ingots). Macerator → 8 copper dust.",
                "dense_copper_plate");
        add("Kompressor: rohes Kohlenetz → 1 (Kohlenetz aus Kohlefasern, diese aus Kohlestaub).",
                "Compressor: raw carbon mesh → 1 (mesh from carbon fibres, these from coal dust).",
                "carbon_plate");
        add("Implosionskompressor: 1 Iridium-Legierungsbarren + 8 Industrie-TNT → 1 (+ 4 dunkle Asche).",
                "Implosion compressor: 1 iridium alloy ingot + 8 industrial TNT → 1 (+ 4 dark ashes).",
                "iridium_plate");
        add("Werkbank: 6 Aluminiumplatten + 3 Magnesiumstaub (AAA/MMM/AAA) → 3.",
                "Crafting: 6 aluminium plates + 3 magnesium dust (AAA/MMM/AAA) → 3.",
                "magnalium_plate");
        add("Industrieller Hochofen (1500 K): 2 Siliziumzellen → 1 (1000 Ticks). Siliziumzellen aus dem Elektrolyseur "
                        + "(Sand, Ton, Granatstäube …) oder der Zentrifuge (Redstone).",
                "Industrial blast furnace (1500 K): 2 silicon cells → 1 (1000 ticks). Silicon cells from the "
                        + "electrolyzer (sand, clay, garnet dusts …) or the centrifuge (redstone).",
                "silicon_plate");
        add("Kompressor: 8 Holzschliff → 1 (Holzschliff: Macerator aus Brettern, Holztüren, Kisten …).",
                "Compressor: 8 wood pulp → 1 (wood pulp: macerator from planks, wooden doors, chests …).",
                "wood_plate");
    }

    /** Gems (ore drops, implosion compressor, UU-matter; phase 23.3). */
    private static void gems() {
        add("Rubinerz (Oberwelt: Wüste, Tafelberg, Savanne; Mining-Dimension) droppt 1 (mit Glück mehr); Industrial "
                        + "Grinder → 1. Implosionskompressor: 4 Rubinstaub + 24 Industrie-TNT → 3. UU-Materie → 2.",
                "Ruby ore (overworld: desert, badlands, savanna; mining dimension) drops 1 (more with fortune); "
                        + "industrial grinder → 1. Implosion compressor: 4 ruby dust + 24 industrial TNT → 3. UU-matter → 2.",
                "ruby");
        add("Saphirerz (Oberwelt: Ozean, Strand, Mangrovensumpf; Mining-Dimension) droppt 1 (mit Glück mehr); "
                        + "Industrial Grinder → 1. Implosionskompressor: 4 Saphirstaub + 24 Industrie-TNT → 3. "
                        + "UU-Materie → 2.",
                "Sapphire ore (overworld: ocean, beach, mangrove swamp; mining dimension) drops 1 (more with fortune); "
                        + "industrial grinder → 1. Implosion compressor: 4 sapphire dust + 24 industrial TNT → 3. "
                        + "UU-matter → 2.",
                "sapphire");
        add("Seltener Zusatzdrop von Saphirerz. Implosionskompressor: 4 grüner Saphirstaub + 24 Industrie-TNT → 3. "
                        + "UU-Materie → 2.",
                "Rare extra drop of sapphire ore. Implosion compressor: 4 green sapphire dust + 24 industrial TNT → 3. "
                        + "UU-matter → 2.",
                "green_sapphire");
        add("Olivinerz (nur End) droppt 1 (mit Glück mehr); Industrial Grinder → 1. Implosionskompressor: 4 "
                        + "Olivinstaub + 24 Industrie-TNT → 3. UU-Materie → 2.",
                "Olivine ore (End only) drops 1 (more with fortune); industrial grinder → 1. Implosion compressor: 4 "
                        + "olivine dust + 24 industrial TNT → 3. UU-matter → 2.",
                "olivine");
        add("Seltener Zusatzdrop von Rubinerz. Implosionskompressor: 4 roter Granatstaub + 16 Industrie-TNT → 3.",
                "Rare extra drop of ruby ore. Implosion compressor: 4 red garnet dust + 16 industrial TNT → 3.",
                "red_garnet");
        add("Seltener Zusatzdrop von Sphaleriterz (Nether). Implosionskompressor: 4 gelber Granatstaub + 16 "
                        + "Industrie-TNT → 3.",
                "Rare extra drop of sphalerite ore (Nether). Implosion compressor: 4 yellow garnet dust + 16 industrial "
                        + "TNT → 3.",
                "yellow_garnet");
        add("Kompressor: Kohleklumpen → 1 (Kohleball → komprimierter Kohleball → 8 davon um Obsidian, Ziegel oder einen Eisenblock → Kohleklumpen). "
                        + "Implosionskompressor: 4 Diamantstaub + 32 Industrie-TNT → 3. Werkbank: → Diamant.",
                "Compressor: coal chunk → 1 (coal ball → compressed coal ball → 8 of them around obsidian, bricks or an iron block → coal chunk). "
                        + "Implosion compressor: 4 diamond dust + 32 industrial TNT → 3. Crafting: → diamond.",
                "industrial_diamond");
    }

    /** Liquid and gas cells (canning machine, electrolyzer, centrifuge, vacuum freezer; phase 23.4). */
    private static void cells() {
        add("Werkbank: 4 Zinnbarren → 4. Kommt aus fast allen GT-Maschinen zurück, sobald ihr Inhalt verarbeitet ist "
                        + "(Elektrolyseur, Zentrifuge, Industrial Grinder …); Druckluftzelle → 1 (Werkbank oder Extraktor).",
                "Crafting: 4 tin ingots → 4. Returned by almost every GT machine once the content is used up "
                        + "(electrolyzer, centrifuge, industrial grinder …); air cell → 1 (crafting or extractor).",
                "empty_cell");
        add("Leere Zelle auf eine Wasserquelle anwenden (nimmt den Block auf). Canning Machine: Wassereimer + Zelle. "
                        + "Elektrolyseur: 32 Zucker + 7 Zellen → 5 + 2 Kohlenstoffzellen.",
                "Use an empty cell on a water source (picks up the block). Canning machine: water bucket + cell. "
                        + "Electrolyzer: 32 sugar + 7 cells → 5 + 2 carbon cells.",
                "water_cell");
        add("Leere Zelle auf eine Lavaquelle anwenden (nimmt den Block auf). Canning Machine: Lavaeimer + Zelle.",
                "Use an empty cell on a lava source (picks up the block). Canning machine: lava bucket + cell.",
                "lava_cell");
        add("Kompressor: leere Zelle → 1. Nebenprodukt vieler Elektrolysen (Sand, Wasser, Granat-, Rubin-, "
                        + "Saphir-, Olivin-, Feuerstein-, Obsidianstaub …).",
                "Compressor: empty cell → 1. By-product of many electrolyses (sand, water, garnet, ruby, sapphire, "
                        + "olivine, flint, obsidian dust …).",
                "air_cell");
        add("Extraktor: Wasserzelle → 1.",
                "Extractor: water cell → 1.",
                "hydrating_cell");
        add("Elektrolyseur: Wasserzelle → 1 oder Wassereimer + leere Zelle → 1.",
                "Electrolyzer: water cell → 1, or water bucket + empty cell → 1.",
                "electrolyzed_water_cell");
        add("Elektrolyseur: 6 elektrolysierte Wasserzellen → 4 (+ Druckluftzelle); 6000 mB Wasser im Tank + "
                        + "5 Zellen → 4; 12 Bauxitstaub + 8 Zellen → 5 (+ Aluminiumstaub).",
                "Electrolyzer: 6 electrolyzed water cells → 4 (+ air cell); 6000 mB water in its tank + 5 "
                        + "cells → 4; 12 bauxite dust + 8 cells → 5 (+ aluminium dust).",
                "hydrogen_cell");
        add("Zentrifuge: 4 Wasserstoffzellen → 1 (+ 3 leere Zellen, 3000 Ticks).",
                "Centrifuge: 4 hydrogen cells → 1 (+ 3 empty cells, 3000 ticks).",
                "deuterium_cell");
        add("Zentrifuge: 4 Deuteriumzellen → 1 (+ 3 leere Zellen, 3000 Ticks).",
                "Centrifuge: 4 deuterium cells → 1 (+ 3 empty cells, 3000 ticks).",
                "tritium_cell");
        add("Zentrifuge: 16 Glowstonestaub + 1 Zelle → 1 (+ 8 Redstone, 8 Goldstaub; 25 000 Ticks).",
                "Centrifuge: 16 glowstone dust + 1 cell → 1 (+ 8 redstone, 8 gold dust; 25,000 ticks).",
                "helium_cell");
        add("Zentrifuge: 16 Heliumzellen → 1 (+ 15 leere Zellen, 10 000 Ticks).",
                "Centrifuge: 16 helium cells → 1 (+ 15 empty cells, 10,000 ticks).",
                "helium_3_cell");
        add("Elektrolyseur: Kohle- oder Holzkohlestaub, 2 Asche, Calcitstaub, 32 Zucker (je + leere Zellen). "
                        + "Zentrifuge: 16 Gummiholz + 5 Zellen → 4 (+ Harz, Pflanzenbälle, Methan).",
                "Electrolyzer: coal or charcoal dust, 2 ash, calcite dust, 32 sugar (each + empty cells). Centrifuge: "
                        + "16 rubber wood + 5 cells → 4 (+ resin, plant balls, methane).",
                "carbon_cell");
        add("Zentrifuge: fast jedes Essen (Äpfel, Fleisch, Brot, Kartoffeln, Pilze, Melonen, Kürbis, Netherwarze, "
                        + "verrottetes Fleisch …) mit leeren Zellen; auch 16 Gummiholz → 1.",
                "Centrifuge: almost any food (apples, meat, bread, potatoes, mushrooms, melons, pumpkin, nether wart, "
                        + "rotten flesh …) with empty cells; also 16 rubber wood → 1.",
                "methane_cell");
        add("Elektrolyseur: Sand, Ton-, Feuerstein-, Obsidian-, Olivin-, Smaragd-, Lasurit-, Sodalith- und Granatstäube "
                        + "(je + leere Zellen). Zentrifuge: 10 Redstone + 4 Zellen → 1 (+ Quecksilber, Pyrit, Rubin).",
                "Electrolyzer: sand, clay, flint, obsidian, olivine, emerald, lazurite, sodalite and garnet dusts (each "
                        + "+ empty cells). Centrifuge: 10 redstone + 4 cells → 1 (+ mercury, pyrite, ruby).",
                "silicon_cell");
        add("Elektrolyseur: 3 Knochenmehl + 1 Zelle → 1; Nebenprodukt von Calcit-, Lasurit-, Andradit-, Grossular- "
                        + "und Uwarowitstaub.",
                "Electrolyzer: 3 bone meal + 1 cell → 1; by-product of calcite, lazurite, andradite, grossular and "
                        + "uvarovite dust.",
                "calcium_cell");
        add("Canning Machine: Calcitstaub + leere Zelle.",
                "Canning machine: calcite dust + empty cell.",
                "calcium_carbonate_cell");
        add("Elektrolyseur: Tonstaub + 5 Zellen → 2 (+ Lithium, Silizium, Aluminium); 29 Lasuritstaub → 4; "
                        + "23 Sodalithstaub + 8 Zellen → 4 (bester Weg).",
                "Electrolyzer: clay dust + 5 cells → 2 (+ lithium, silicon, aluminium); 29 lazurite dust → 4; "
                        + "23 sodalite dust + 8 cells → 4 (best way).",
                "sodium_cell");
        add("Elektrolyseur: 10 Salpeterstaub + 7 Zellen → 2 (+ Stickstoff); 16 Enderperlenstaub + 16 Zellen → 4.",
                "Electrolyzer: 10 saltpeter dust + 7 cells → 2 (+ nitrogen); 16 ender pearl dust + 16 cells → 4.",
                "potassium_cell");
        add("Elektrolyseur: 10 Salpeterstaub + 7 Zellen → 2; 16 Enderperlenstaub + 16 Zellen → 5.",
                "Electrolyzer: 10 saltpeter dust + 7 cells → 2; 16 ender pearl dust + 16 cells → 5.",
                "nitrogen_cell");
        add("Elektrolyseur: 29 Smaragdstaub + 18 Zellen → 3; 16 Enderperlenstaub + 16 Zellen → 1.",
                "Electrolyzer: 29 emerald dust + 18 cells → 3; 16 ender pearl dust + 16 cells → 1.",
                "beryllium_cell");
        add("Elektrolyseur: 16 Enderperlenstaub + 16 Zellen → 6; 23 Sodalithstaub + 8 Zellen → 1.",
                "Electrolyzer: 16 ender pearl dust + 16 cells → 6; 23 sodalite dust + 8 cells → 1.",
                "chlorite_cell");
        add("Elektrolyseur: Tonstaub + 5 Zellen → 1 (+ Natrium, Silizium, Aluminium).",
                "Electrolyzer: clay dust + 5 cells → 1 (+ sodium, silicon, aluminium).",
                "lithium_cell");
        add("Elektrolyseur: 2 Zinnoberstaub + 1 Zelle → 1 (+ Schwefelstaub). Zentrifuge: 10 Redstone + 4 Zellen → 3.",
                "Electrolyzer: 2 cinnabar dust + 1 cell → 1 (+ sulfur dust). Centrifuge: 10 redstone + 4 cells → 3.",
                "mercury_cell");
        add("Canning Machine: Schwefelstaub + leere Zelle.",
                "Canning machine: sulfur dust + empty cell.",
                "sulfur_cell");
        add("Canning Machine: Wolframstaub + leere Zelle.",
                "Canning machine: tungsten dust + empty cell.",
                "tungsten_cell");
        add("Zentrifuge: 16 Seelensand + 1 Zelle → 1 (+ 4 Salpeterstaub, Kohlestaub, 10 Sand). Öl aus der Welt "
                        + "kommt nur in Eimer und Tanks, nicht in Zellen.",
                "Centrifuge: 16 soul sand + 1 cell → 1 (+ 4 saltpeter dust, coal dust, 10 sand). Oil from the world "
                        + "only goes into buckets and tanks, not cells.",
                "oil_cell");
        add("Vakuumgefrierer: Wasserzelle → 1 (50 Ticks).",
                "Vacuum freezer: water cell → 1 (50 ticks).",
                "ice_cell");
        add("Canning Machine: komprimierte Pflanzen + leere Zelle. Extraktor → Bio-Brennstoffzelle.",
                "Canning machine: compressed plants + empty cell. Extractor → biofuel cell.",
                "bio_cell");
        add("Extraktor: Biozelle → 1 (Biozelle: komprimierte Pflanzen in die Canning Machine).",
                "Extractor: bio cell → 1 (bio cell: compressed plants in the canning machine).",
                "biofuel_cell");
        add("Canning Machine: hydrierte Kohle + leere Zelle (hydrierte Kohle: Kompressor aus hydriertem Kohlestaub).",
                "Canning machine: hydrated coal + empty cell (hydrated coal: compressor from hydrated coal dust).",
                "hydrated_coal_cell");
        add("Extraktor: hydrierte Kohlezelle → 1.",
                "Extractor: hydrated coal cell → 1.",
                "coalfuel_cell");
    }

    /** Fuel rods and reactor cells, kept short (spec 24 §3; phase 23.4). */
    private static void fuelRods() {
        add("Canning Machine: Uranbarren + leere Zelle. Werkbank: wiederangereicherte Uranzelle + Kohlestaub. "
                        + "Zentrifuge: 4 Uranstaub + 4 Zellen → 4 (+ Plutonium, Thorium, Wolfram).",
                "Canning machine: uranium ingot + empty cell. Crafting: re-enriched uranium cell + coal dust. "
                        + "Centrifuge: 4 uranium dust + 4 cells → 4 (+ plutonium, thorium, tungsten).",
                "uranium_cell");
        add("Werkbank: Uranzelle + dichte Kupferplatte + Uranzelle → Doppel; 2 Doppel + 3 dichte Kupferplatten → "
                        + "Vierfach.",
                "Crafting: uranium cell + dense copper plate + uranium cell → dual; 2 dual + 3 dense copper plates → "
                        + "quad.",
                "dual_uranium_cell", "quad_uranium_cell");
        add("Canning Machine: Plutoniumbarren + leere Zelle (Plutonium aus Uran, siehe Plutoniumstaub). Werkbank: "
                        + "2 Zellen + dichte Kupferplatte → Doppel; 2 Doppel + 3 dichte Kupferplatten → Vierfach.",
                "Canning machine: plutonium ingot + empty cell (plutonium from uranium, see plutonium dust). Crafting: "
                        + "2 cells + dense copper plate → dual; 2 dual + 3 dense copper plates → quad.",
                "plutonium_cell", "dual_plutonium_cell", "quad_plutonium_cell");
        add("Canning Machine: Thoriumbarren + leere Zelle (Thorium aus der Zentrifuge). Werkbank: 2 Zellen + "
                        + "Kupferplatte → Doppel; 2 Doppel + 3 Kupferplatten → Vierfach.",
                "Canning machine: thorium ingot + empty cell (thorium from the centrifuge). Crafting: 2 cells + copper "
                        + "plate → dual; 2 dual + 3 copper plates → quad.",
                "thorium_cell", "dual_thorium_cell", "quad_thorium_cell");
        add("Werkbank: 8 leere Zellen um einen Uranbarren → 8. Zentrifuge: 8 wiederangereicherte Uranzellen → 3 "
                        + "(+ Plutonium-, Thoriumstaub).",
                "Crafting: 8 empty cells around an uranium ingot → 8. Centrifuge: 8 re-enriched uranium cells → 3 "
                        + "(+ plutonium, thorium dust).",
                "near_depleted_uranium_cell");
        add("Werkbank: fast erschöpfte Uranzelle + Kohlestaub. Im Reaktor neben aktiven Uranzellen wird sie zur "
                        + "wiederangereicherten Uranzelle (schneller bei hoher Hitze).",
                "Crafting: near-depleted uranium cell + coal dust. In the reactor next to active uranium cells it turns "
                        + "into a re-enriched uranium cell (faster at high heat).",
                "isotope_cell");
        add("Entsteht im Reaktor aus einer abgereicherten Isotopenzelle neben aktiven Uranzellen. Werkbank: + "
                        + "Kohlestaub → Uranzelle.",
                "Made in the reactor from a depleted isotope cell next to active uranium cells. Crafting: + coal dust → "
                        + "uranium cell.",
                "re_enriched_uranium_cell");
        add("Werkbank: 4 Zinnbarren um eine Wasserzelle (oder Wassereimer) → 10k; 3× 10k + 6 Zinn → 30k; 2× 30k + "
                        + "6 Zinn + dichte Kupferplatte → 60k. Vakuumgefrierer kühlt benutzte Zellen wieder ab.",
                "Crafting: 4 tin ingots around a water cell (or water bucket) → 10k; 3× 10k + 6 tin → 30k; 2× 30k + "
                        + "6 tin + dense copper plate → 60k. The vacuum freezer cools used cells back down.",
                "coolant_cell_10k", "coolant_cell_30k", "coolant_cell_60k");
        add("Werkbank: 4 Zinnbarren um eine Heliumzelle → 60k; 3× 60k + 6 Zinn → 180k; 2× 180k + 6 Zinn + dichte "
                        + "Kupferplatte → 360k. Vakuumgefrierer kühlt benutzte Zellen wieder ab.",
                "Crafting: 4 tin ingots around a helium cell → 60k; 3× 60k + 6 tin → 180k; 2× 180k + 6 tin + dense "
                        + "copper plate → 360k. The vacuum freezer cools used cells back down.",
                "helium_coolant_cell_60k", "helium_coolant_cell_180k", "helium_coolant_cell_360k");
        add("Werkbank: 10k-Kühlzelle + 2 Natrium- + 2 Kaliumzellen + 4 Zinn → 60k; 3× 60k + 6 Zinn → 180k; 2× 180k "
                        + "+ 6 Zinn + dichte Kupferplatte → 360k. Vakuumgefrierer kühlt benutzte Zellen wieder ab.",
                "Crafting: 10k coolant cell + 2 sodium + 2 potassium cells + 4 tin → 60k; 3× 60k + 6 tin → 180k; "
                        + "2× 180k + 6 tin + dense copper plate → 360k. The vacuum freezer cools used cells back down.",
                "nak_coolant_cell_60k", "nak_coolant_cell_180k", "nak_coolant_cell_360k");
        add("Werkbank: Schaltkreis, Lavazelle, dichte Kupferplatte (untereinander).",
                "Crafting: circuit, lava cell, dense copper plate (in a column).",
                "heating_cell");
    }

    /** Coal chain, carbon, coke, ash, plants, rubber, alloys, fuel (phase 23.4). */
    private static void misc() {
        add("Werkbank: 8 Kohlestaub um einen Feuerstein → 1.",
                "Crafting: 8 coal dust around a flint → 1.",
                "coal_ball");
        add("Kompressor: Kohleball → 1.",
                "Compressor: coal ball → 1.",
                "compressed_coal_ball");
        add("Werkbank: 8 komprimierte Kohlebälle um Obsidian, Ziegel oder einen Eisenblock → 1. Kompressor → "
                        + "Industriediamant.",
                "Crafting: 8 compressed coal balls around obsidian, bricks or an iron block → 1. Compressor → "
                        + "industrial diamond.",
                "coal_chunk");
        add("Werkbank: 4 Kohlestaub (2×2) → 1. Zwei davon → rohes Kohlenetz.",
                "Crafting: 4 coal dust (2×2) → 1. Two of them → raw carbon mesh.",
                "carbon_fiber");
        add("Werkbank: 2 rohe Kohlefasern → 1. Kompressor → Kohleplatte.",
                "Crafting: 2 raw carbon fibres → 1. Compressor → carbon plate.",
                "carbon_mesh");
        add("Koksofen (3×3×3 aus Koksofenziegeln, ohne Strom): 1 Kohle → 1 in 900 Ticks; Holzkohle geht nicht. "
                        + "Koksblock → 9.",
                "Coke oven (3×3×3 of coke oven bricks, no power): 1 coal → 1 in 900 ticks; charcoal is not accepted. "
                        + "Coke block → 9.",
                "coke");
        add("Werkbank: 9 Koks → 1.",
                "Crafting: 9 coke → 1.",
                "coke_block");
        add("Kessel des Kohlekraftwerks: je 4 Kohle (oder 2 Koks) → 1 im Asche-Slot. Zentrifuge: 2 dunkle Asche → 2.",
                "Coal power plant boiler: every 4 coal (or 2 coke) → 1 in the ash slot. Centrifuge: 2 dark ashes → 2.",
                "ash");
        add("Nebenprodukt des Implosionskompressors (4–16 je Vorgang) und des Industriellen Hochofens (Stahl 2, "
                        + "Wolframstahl 4). Elektrolyseur: 4 Lohenstaub → 1 (+ Schwefelstaub).",
                "By-product of the implosion compressor (4–16 per run) and the industrial blast furnace (steel 2, "
                        + "tungstensteel 4). Electrolyzer: 4 blaze powder → 1 (+ sulfur dust).",
                "dark_ash");
        add("Werkbank: 8 Blätter, Weizen, Samen, Gras, Zuckerrohr oder Gummisetzlinge im Ring → 1; 8 Setzlinge → 2. "
                        + "Zentrifuge: Nebenprodukt von Erde, Grasblöcken, Harz und Gummiholz.",
                "Crafting: 8 leaves, wheat, seeds, grass, sugar cane or rubber saplings in a ring → 1; 8 saplings → 2. "
                        + "Centrifuge: by-product of dirt, grass blocks, resin and rubber wood.",
                "plant_ball");
        add("Kompressor: Pflanzenball → 1; 4 Setzlinge, 8 Blätter (kein Gummibaum), Weizen, Zuckerrohr, Kakteen, "
                        + "Löwenzahn oder Mohn → 1. Zentrifuge: Nebenprodukt von Erde, Grasblöcken und Harz.",
                "Compressor: plant ball → 1; 4 saplings, 8 leaves (not rubber), wheat, sugar cane, cacti, dandelions "
                        + "or poppies → 1. Centrifuge: by-product of dirt, grass blocks and resin.",
                "compressed_plant_ball");
        add("Kompressor: hydrierter Kohlestaub → 1 (Staub: 8 Kohlestaub um eine Wasserzelle oder einen Wassereimer).",
                "Compressor: hydrated coal dust → 1 (dust: 8 coal dust around a water cell or water bucket).",
                "hydrated_coal_clump");
        add("Extraktor: Harz → 3 (bester Weg), Schleimball → 2, Gummiholz oder -setzling → 1. Ofen: Harz → 1. "
                        + "Zentrifuge: 4 Harz → 14.",
                "Extractor: resin → 3 (best way), slime ball → 2, rubber wood or sapling → 1. Furnace: resin → 1. "
                        + "Centrifuge: 4 resin → 14.",
                "rubber");
        add("Kompressor: Mischmetallbarren → 1 (Mischmetall: Werkbank aus drei Platten senkrecht, z. B. raff. Eisen, "
                        + "Bronze, Zinn).",
                "Compressor: mixed metal ingot → 1 (mixed metal: crafting from three plates in a column, e.g. refined "
                        + "iron, bronze, tin).",
                "advanced_alloy");
        add("Raffinerie (BuildCraft): Öl → Treibstoff 1:1; Treibstoff in einen Tank pumpen und dort mit einem leeren "
                        + "Eimer abfüllen.",
                "Refinery (BuildCraft): oil → fuel 1:1; pump the fuel into a tank and fill an empty bucket there.",
                "fuel_bucket");
    }

    /** Tiny pile of {@code material}: 4 make one dust, one dust makes 4; {@code from} lists the by-product sources. */
    private static void small(String material, String germanDust, String englishDust, String from, String fromEnglish) {
        add("4 kleine Haufen → 1 " + germanDust + " (Werkbank), 1 " + germanDust + " → 4. Entsteht als "
                        + "Nebenprodukt: " + from + ".",
                "4 tiny piles → 1 " + englishDust + " (crafting), 1 " + englishDust + " → 4. Comes as a by-product: "
                        + fromEnglish + ".",
                "small_" + material + "_dust");
    }
}
