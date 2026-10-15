# Abweichungen vom Original

Hier wird jede Stelle dokumentiert, an der der neue Mod (MC 1.21.1 / NeoForge) vom Original (FTB Ultimate,
MC 1.4.7) abweicht – entweder weil 1.21 es technisch erzwingt, oder weil es bewusst entschieden wurde
(Energiebalancing, `docs/balancing.md`). Format: **Thema – Original – Neu – Grund – Status**.

Status: *geplant* (aus Phase 1 abgeleitet, noch nicht umgesetzt), *entschieden* (vom Nutzer bestätigt),
*umgesetzt*.

## Technisch bedingt (Vorschläge aus Phase 1)

| Thema | Original (1.4.7) | Neu (1.21.1) | Grund | Status |
|---|---|---|---|---|
| Block-/Item-IDs, Metadaten | numerische IDs, Meta-Varianten (z. B. ein Kabelblock mit 13 Metas) | eigene Registry-Einträge je Variante; Blockstates statt Meta | 1.13+ Flattening | geplant |
| Item-NBT (Ladung, Hitze, Modi) | NBT-Tags `charge`, `heat`, `advDmg`, `laserSetting` … | Data Components | 1.20.5+ | geplant |
| Haltbarkeitsbalken elektrischer Items | Damage-Wert 1..26 als Anzeige | Item-Bar über Data Components (`isBarVisible/getBarWidth`), Damage nicht missbraucht | 1.21 trennt Haltbarkeit/Anzeige sauber | geplant |
| OreDictionary | Strings `ingotCopper` … | Tags `c:ingots/copper` … | Forge-OreDict existiert nicht mehr | geplant |
| Kupfer | IC2-Kupfererz/-barren (im Pack aus Fremdmods) | Vanilla-Kupfer (Erz, Rohkupfer, Barren) wird verwendet, kein eigenes Kupfererz; Häufigkeit der Vanilla-Kupfergenerierung an IC2-Werte angepasst (15 Adern/Chunk, Größe 10). Macerator: Erz/Rohkupfer → 2 Kupferstaub (Details Rohkupfer in Phase 3 festlegen) | 1.17+ hat Kupfer | **entschieden** |
| Rohmetall (1.17+) | Erzblock droppte sich selbst, Macerator: Erz → 2 Staub | Eisen-/Gold-/Kupfererz droppen Vanilla-Rohmetall; (Tiefenschiefer-)Kupfererz droppt wie Eisenerz **1** Rohkupfer (Loot-Tabelle überschrieben, Vanilla: 2–5); Macerator: Rohmetall → 2 Staub **und** Erzblock (Behutsamkeit) → 2 Staub; Zinnerz droppt sich selbst wie bei IC2 | 1.17+ Erz-Drops | **entschieden** |
| Tiefenschiefer-/Netherquarz-Erze | existieren nicht | Deepslate-Varianten bekommen dieselben Macerator-/Erzwert-Regeln wie das Basiserz | neue Blöcke | geplant |
| Welthöhe | 0..255, Meeresspiegel 63 | −64..320; Erz-Höhen absolut übernommen (Uran 0..64 → −?..64 zu entscheiden); Öl siehe „Öl-Weltgen“ | 1.18-Weltgen | geplant |
| Weltgenerierung | `IWorldGenerator`, Populate-Event | `ConfiguredFeature`/`PlacedFeature`/`BiomeModifier` per Datagen; Biomlisten über Tags (`#minecraft:is_forest`, `is_taiga`, `is_jungle`, Sumpf) statt Namens-Teilstrings | API | geplant |
| Flüssigkeiten | Forge Liquid API (Blöcke 9/11 als ID) | NeoForge FluidStack/FluidHandler-Capability, mB unverändert | API | geplant |
| Energie-Capabilities | eigenes EnergyNet (IC2), PowerProvider (BC) | eigenes EU-Netz und eigenes MJ-System bleiben; **keine** Anbindung an NeoForge-FE (FE kennt keine Spannung) | 1:1-Mechanik | geplant |
| Chunkloading der Quarry | Forge-Ticket, max. 25 Chunks (Pack) | NeoForge-Ticket-Controller; Limit 25 Chunks beibehalten | API | geplant |
| Tasten (Modus, Boost, Alt, Sprung) | IC2-Keybinds über Packets | NeoForge KeyMappings + Payloads; Belegung wie Original (Modus = M, Boost = Strg …) | API | geplant |
| Explosionen | eigener `ExplosionIC2`-Algorithmus | 1:1 nachgebaut (`core.explosion.Ic2Explosion`); Drops über `Block.getDrops` ohne Explosions-Verfall (wie `getBlockDropped`), Höhengrenzen = Bauhöhe der Dimension; Whitelist als Block-Tag `craft:explosion_whitelist` (Grundgestein); Drop-Stapel zusätzlich ≤ max. Stapelgröße | API | **umgesetzt** |
| Von IC2 geänderte Vanilla-Resistenzen | global per `setResistance` (Obsidian/Zaubertisch/Endertruhe/Amboss 36, Wasser/stille Lava 18) | gelten nur in IC2-Explosionen; Vanilla-TNT/Creeper unverändert (konnten Obsidian auch mit 36 nicht sprengen, Wasser-Unterschied bei Vanilla-Explosionen entfällt) | Vanilla-Blockwerte nicht ohne Mixin änderbar | **umgesetzt** |
| Vanilla-Brennwerte | 1.4.7-Tabelle | 1.21-Tabelle für Items, die es 1.4.7 nicht gab; für vorhandene Items 1.21-Werte (identisch bis auf Details, z. B. Holzwerkzeuge) | neue Brennstoffe | geplant |
| Rüstungssystem | Forge `ISpecialArmor` (Absorptionsanteil, ×25-Einheiten) | Schadens-Event (`LivingIncomingDamageEvent`) bildet Absorption und EU-Kosten nach | `ISpecialArmor` gibt es nicht mehr | geplant |
| Trank „Radioaktivität“ | Potion-ID 24 | registrierter MobEffect | Registry | geplant |
| Schadenswerte | int halbe Herzen | float Lebenspunkte, Werte 1:1 | Typ | geplant |
| Bronze-Werkzeuge und -Rüstung | IC2-Items | werden nicht umgesetzt | Nutzerentscheidung 2026-10-15 | **entschieden** |
| Electron Tubes / Platinen / Lötkolben | Thermionic Fabricator, Carpenter | Maschinen werden 1:1 übernommen, keine Abweichung | im Umfang | **entschieden** |
| GregTech-Rezepte mit GT-Material | siehe `docs/spec/08-rezepte.md` §16 | wie im Pack (finale GT-Rezepte); GT-Materialien kommen in den Umfang, Herstellungsweg offen (`12-gregtech-materialien.md`) | Nutzerentscheidung „wie früher“ | **entschieden** (Herstellungsweg offen) |
| Silberbarren | aus Fremdmods / GregTech | GT-Silberbarren kommt mit den GT-Materialien (Silberstaub → Ofen → Silberbarren wie IC2-Original) | GT-Materialien im Umfang | geplant (abhängig vom GT-Herstellungsweg) |
| Kabel-Block | ein Block `blockCable`, Typ = Meta, Farbe/Schaum im Tile Entity | ein Block je Kabeltyp (13); Blockstate = 6 Verbindungen + Schaum (`none/wet/hard`); Farbe, Schaumfarbe, Detektor-/Splitter-Zustand im BlockEntity; Isolieren/Abisolieren = Blocktausch mit Übernahme von Farbe/Schaum | Metadaten gibt es nicht mehr | **umgesetzt** |
| Kabel-Darstellung | `RenderBlockCable` (ISBRH) mit Texturindex `Typ·16 + Farbe` | dynamisches Modell (Geometry-Loader `craft:cable`), Texturen aus dem IC2-Spritesheet geschnitten; Endflächen der Arme werden immer gezeichnet (IC2: nur bei dünnerem Nachbarkabel) – nur optisch | ISBRH existiert nicht | **umgesetzt** |
| Kabel-Kollisionsbox | eine AABB über Kern + verbundene Seiten | identisch übernommen (keine Vereinigung der Arme) | – | **umgesetzt** |
| Splitter-Kabel „bekommt Redstone“ | `isBlockGettingPowered` | `Level#getDirectSignalTo(pos) > 0` (direkte/starke Signale) | API | **umgesetzt** |
| Anmeldung am EU-Netz | `EnergyTileLoadEvent` im ersten Tile-Tick | Anmeldung in `BlockEntity#onLoad` (NeoForge ruft es in der BlockEntity-Tick-Schleife auf), Abmeldung in `setRemoved`/`onChunkUnloaded` | API | **umgesetzt** |
| Compact Solars | Jar fehlt im Pack-Download | Quellcode v4.0 (GitHub, MC 1.4.7) als Referenz, Werte verifiziert | Quelle fehlte | **entschieden** |
| RE-Batterie | zwei Items (leer `itemBatREDischarged`, geladen `itemBatRE`) | **ein** Item; ohne Ladung stapelbar bis 16 und ohne Ladebalken, mit Ladung Stapelgröße 1 (Data Component `max_stack_size`); Rezepte nehmen die Batterie mit beliebiger Ladung (IC2 registrierte sie doppelt) | Items ohne Metadaten/ID-Tausch | **umgesetzt** |
| Maschinen-Blockstates | Facing und Aktiv-Zustand im TileEntity, Textur per `sideAndFacingToSpriteOffset` | `facing` (6) und `active` im Blockstate; je Facing/Aktiv ein eigenes Modell mit genau der IC2-Zuordnung (keine Modellrotation, da IC2 bei Ost/West die Seitentexturen spiegelt) | Blockstates | **umgesetzt** |
| Gummiblätter-Zerfall | Vanilla-1.4.7-Blätterzerfall (Radius 4, `canSustainLeaves`) | Vanilla-1.21-Blätter (`distance` ≤ 7 zu `#minecraft:logs`); Gummiholz ist in `#logs_that_burn`. Bäume aus Setzling/Weltgen setzen die Distanz direkt | 1.21-Blättersystem | **umgesetzt** |
| Gummibaum-Weltgen | Biomname enthält „taiga“/„forest“/„jungle“/„swamp“, Biom bei Chunk +16/+16 | Biom-Tags `#is_taiga`, `#is_forest`, `#is_jungle`, `#c:is_swamp` (Biom bei Chunk +16/+16 auf Oberflächenhöhe); Bodenhöhe über Heightmap `WORLD_SURFACE_WG` (= erster Nicht-Luft-Block von oben). Bäume des Zufallspfads, die außerhalb der 3×3 beschreibbaren Chunks lägen, werden übersprungen | 1.21-Features dürfen nur Nachbarchunks schreiben | **umgesetzt** |
| Erzhöhen Zinn/Kupfer | y 0–39 (Zinn), y = rand(40)+rand(20)+10 (Kupfer) | absolute y-Werte unverändert übernommen (Trapezverteilung 10–68 entspricht exakt der Summe der zwei Zufallszahlen); Anzahl Adern `round(gauss·√n + n)` über `clamped_normal` (Mittelwert n + 0,5); Vanilla-Kupferadern (`ore_copper`, `ore_copper_large`) per Biome-Modifier entfernt | 1.18-Weltgen | **umgesetzt** |
| Tiefenschiefer-Zinnerz | existiert nicht | eigener Block (Härte 4,5 wie Vanilla-Tiefenschiefererze), Drops/Macerator/Ofen wie Zinnerz; Textur aus Zinnerz-Muster + Vanilla-Tiefenschiefer | Tiefenschiefer ab 1.17 | **umgesetzt** |
| Gummisetzling im Tag `#minecraft:saplings` | nicht im OreDict `treeSapling` | nur im **Block**-Tag, nicht im Item-Tag (sonst Brennwert 100 statt 80 und 2 Pflanzenbälle) | Data-Map-Reihenfolge | **umgesetzt** |
| Pflanzenball aus Gummisetzlingen | doppelt registriert (1 und 2 Stück), erstes Rezept gewinnt | 1 Stück | 1.21 erlaubt keine zwei gleichen Raster | **umgesetzt** |
| Redstone-Modus-Knopf (BatBox …) | IC2-Netzwerk-Event | Vanilla-Container-Button-Paket (`clickMenuButton`) | API | **umgesetzt** |
| GUI-Synchronisation | Fortschrittsbalken-Updates mit 16 Bit, teils nur clientseitig berechnete Anzeigen (Generator-Brennzeit, Windmühle) | alle Anzeigewerte werden serverseitig berechnet und als volle Ints synchronisiert | Anzeige korrekt | **umgesetzt** |
| Eisenofen-GUI | 1.4.7-Ofentextur | 1.21-Ofentextur und -Sprites (Flamme, Pfeil), Werte wie IC2 | Textur-Layout geändert | **umgesetzt** |
| Maschinengeräusche | IC2-AudioManager (Paul's SoundSystem) | Minecraft-Soundsystem, Original-OGGs; Start/Stopp per Block-Event, Generator-Loops solange aktiv | API | **umgesetzt** |
| GT-Recycling Vanilla-Eisenteile | Eisentür → 6 Eisenstaub bzw. 6 Eisenbarren (1.4.7: 6 Barren → 1 Tür) | Eisentür → **2** (1.21: 6 Barren → 3 Türen, sonst Vermehrung); übrige Teile (Kompass, Uhr, Schere, Lore, Eimer, Amboss 31/20/10, Steinknopf, Steindruckplatte, Hebel) wie GT; nur Hauptausgabe (Nebenprodukte gab es im IC2-Macerator nicht); GT-Material-Ausgaben (Holzstaub, kleiner Eisenstaub) erst mit den GT-Materialien | 1.21-Rezeptmenge | **umgesetzt** |
| Schrottbox-Drops | gewichtete Liste `ItemScrapbox.dropList` (Float-Chancen, kumuliert) | Loot-Tabelle `craft:gameplay/scrap_box`, Gewicht = Chance × 100 (gleiche Verteilung); „Schild“ → Eichenschild | Datapack-/JEI-tauglich; 1.21 hat Holzsorten | **umgesetzt** |
| Harzschicht als Item | implizites ItemBlock (nicht im Kreativ-Tab) | kein Block-Item; Pick-Block liefert Harz | Block wird nur mit Harz gesetzt | **umgesetzt** |
| BC-Zeitbasis (MJ-Verlust, Redstone-Engine) | `world.getWorldTime()` (Tageszeit, in 1.4.7 ohne Daylight-Gamerule immer laufend) | `getGameTime()` (Gesamt-Weltzeit) | Tageszeit steht in 1.21 bei `doDaylightCycle=false` still | **umgesetzt** |
| MJ-Empfänger | `instanceof IPowerReceptor` + Provider-Klassenprüfung | Block-Capability `craft:power_receptor` (seitenbezogen); Provider immer mit Latenz 0 wie das Pneumatic-Framework des Packs | Capabilities statt Interfaces | **umgesetzt** |
| Engine-Darstellung | TESR mit GL-Licht aus; Bruchpartikel aus Index 0 des Terrain-Sheets (Gras) | BlockEntityRenderer + Item-Renderer (BEWLR) mit Original-Geometrie und -Texturen, normale Entity-Beleuchtung; Bruchpartikel: Eichenbretter/Bruchstein/Eisenblock | API; Originalpartikel waren ein Versehen | **umgesetzt** |
| Engine: feste Seite | `isBlockSolidOnSide` nur gegenüber der Ausgabe | Block-Support-Shape (4-px-Platte auf der Rückseite) | API | **umgesetzt** |
| Öl-Weltgen: Höhen | Kugel y 20–29; Steigrohr- und Wüstensuche ab y 128 abwärts; Fontäne max. y 128 | Kugel y 20–29 unverändert; Suche ab Geländeoberkante (Heightmap `WORLD_SURFACE_WG`), Wüste weiterhin bis y 66; Fontäne bis Bauhöhe | 1.18-Berge über 128 (Nutzerentscheidung) | **umgesetzt** |
| Öl-Weltgen: Reichweite | Oberflächenseen großer Vorkommen bis Radius 39 um die Chunk-Ecke | Blöcke außerhalb der 3×3 zugreifbaren Chunks werden übersprungen (große Seen ggf. mit geraden Kanten); Lesezugriffe dort gelten als Luft | 1.21-Features dürfen nur Nachbarchunks schreiben (Nutzerentscheidung) | **umgesetzt** |
| Öl-Weltgen: Ablauf | `PopulateChunkEvent.Post`, Biom `desert` (ID-Vergleich), Blätter/Holz/Gras per Block-ID | Feature im Schritt `top_layer_modification` (nach der Dekoration) für `#is_overworld`; Biom `minecraft:desert`; `#leaves`, `#logs`, Grasblock; gesetztes Öl bekommt einen Fluid-Tick (statt Block-Update) | API | **umgesetzt** |
| Öl als „Wasser“ | Material.water: Schwimmen, Ertrinken, Löschen, kein Fallschaden, Lava erstarrt daneben, Ackerland, Zuckerrohr, Schwamm, Angeln, Wassermob-Spawns, Dispenser-Eimer → Wassereimer | FluidType mit Wasser-Eigenschaften (Schwimmen, Ertrinken, Löschen, kein Fallschaden, Boote, Ackerland) und Wasser-Nebelfarbe; Lava erstarrt neben Öl wie im Original (Quelle → Obsidian, Stufe ≤ 4 → Bruchstein); Öl ist **nicht** im Tag `#minecraft:water` (Zuckerrohr, Schwamm, Angeln, Spawns, Dispenser-Kuriosum entfallen); Mischen Öl/Wasser/Lava beim Fließen nach 1.21-Regeln | Material-System gibt es nicht mehr; Wasser-Tag hätte unüberschaubare Nebenwirkungen | **umgesetzt** |
| Öl-/Treibstoff-Texturen | prozedurale TextureFX (jede Tick neu berechnet) | mit denselben Algorithmen vorab gerenderte Animationsstreifen (64 Frames, `tools/scripts/BcLiquidTextures.java`) | kein TextureFX in 1.21 | **umgesetzt** |
| Treibstoff | Flüssigkeit über das Item `fuel`; `bucketFuel` ist ein normales Item | Flüssigkeit `craft:fuel` ohne Block; Treibstoffeimer kann nicht ausgeleert werden (nur in Tanks), kein Item `fuel` | Fluid-Registry | **umgesetzt** |

## Bewusst (Energiebalancing)

Siehe `docs/balancing.md`. Wird hier eingetragen, sobald freigegeben.
