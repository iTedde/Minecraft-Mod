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
| Rüstungssystem | Forge `ISpecialArmor` (Absorptionsanteil, ×25-Einheiten) | Schadens-Event (`LivingIncomingDamageEvent`) bildet Absorption und EU-Kosten nach (Details unten „Rüstungsrechnung“) | `ISpecialArmor` gibt es nicht mehr | **umgesetzt** |
| Trank „Radioaktivität“ | Potion-ID 24 | registrierter MobEffect `craft:radiation` | Registry | **umgesetzt** |
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
| Laser, Bau-Roboter, Bohrarm | Entities (`EntityBlock`, `EntityRobot`, `EntityEnergyLaser`, `EntityMechanicalArm`) | Block-Entity-Renderer der Landmark/Quarry mit Original-Geometrie/-Texturen; die Bau-Warteschlange des Roboters liegt in der Quarry, sein Flug wird nur im Client simuliert | keine Hilfs-Entities nötig; idiomatisch | **umgesetzt** |
| Quarry: Chunkloading | Forge-Ticket je Quarry, max. 200 Tickets je Mod; ohne Ticket Meldung „no more chunkloaders“ | NeoForge-`TicketController` (`craft:quarry`); kein Ticket-Limit, daher keine „no more chunkloaders“-Meldung; Größenprüfung weiterhin mit 25 (`maximumChunksPerTicket`) | API | **umgesetzt** |
| Quarry: Tiefe | Ebenen bis y = 0 | bis zur Weltuntergrenze (y −64); Serpentinen-Parität mit `floorMod` für negative y | 1.18-Weltgeometrie | **umgesetzt** |
| Quarry: Ausgabe in Inventare | `Transactor` mit Seite UNKNOWN | Item-Handler-Capability der Seite, die zur Quarry zeigt; erst stapeln, dann leere Slots | API | **umgesetzt** |
| Weiche Blöcke | `BlockFluid`, `IPlantable`, Schnee, Ranken | Flüssigkeitsblöcke, `BushBlock` (Blumen, Setzlinge, Feldfrüchte, Gras, Pilze, Stängel, Netherwarze …), Zuckerrohr, Kaktus, Schneeschicht, Ranken | kein `IPlantable` in 1.21 | **umgesetzt** |
| Quarry: Helligkeit | `BlockMachineRoot`: +0,5 Helligkeit, wenn eine aktive Maschine daneben steht | entfällt | rein optisch, kein Gegenstück | **umgesetzt** |
| Landmark-Modell | flache Fackel-Ebenen (`RenderingMarkers`) | 2×10-px-Stab wie das Vanilla-Fackelmodell, je Richtung gedreht | Blockmodell-JSON | **umgesetzt** |
| Rahmen-Zerfall | Random-Tick-Rate von 1.4.7 | Random-Tick-Rate von 1.21 (`randomTickSpeed`); Chance je Tick unverändert 4/10 | Engine | **umgesetzt** |
| Quarry: Rezept | Pack-Rezept mit IC2-Diamantbohrer | kommt mit dem Diamantbohrer (Phase 6/6b); bis dahin nur Creative | Zutat fehlt noch | **offen bis Phase 6** |
| Flüssigkeitsbehälter (Tank, Raffinerie, Combustion Engine) | Forge `LiquidContainerRegistry` | feste Tabelle `LiquidContainers` mit den Pack-Einträgen (Wasser-/Lava-/Öl-/Treibstoffeimer, IC2-Wasser-/Lavazelle), sonst Item-Fluid-Capability; `Utils.consumeItem` 1:1 (bei Stapel > 1 geht der leere Behälter verloren) | Registry gibt es nicht mehr | **umgesetzt** |
| Pumpe: Flood-Fill | gleiche **Block-ID** (stehendes 9 und fließendes 8 Wasser getrennt); jeder `ILiquid`-Block | gleiche Flüssigkeit (Quelle + fließend gemeinsam); nur Flüssigkeitsblöcke (`LiquidBlock`), keine wassergeloggten Blöcke | 1.21 hat einen Block je Flüssigkeit; Wasserloggen würde Blöcke zerstören | **umgesetzt** |
| Pumpe: Höhen | Schichten ab y > 0, Suche abwärts bis y 1 | jede Schicht zählt (auch y ≤ 0), Suche bis zur Weltuntergrenze | negative Welthöhen seit 1.18 | **umgesetzt** |
| Pumpenrohr | `EntityBlock` | Block-Entity-Renderer mit Textur 102 | keine Hilfs-Entities | **umgesetzt** |
| Flüssigkeitsdarstellung (Tank, Raffinerie) | Block-/Item-Icon der Flüssigkeit, GL-Licht aus | Still-Textur der Flüssigkeit mit Tönung (Wasser blau getönt), durchscheinend, gleiche Maße und 100 Höhenstufen | Fluid-Rendering von 1.21 | **umgesetzt** |
| Raffinerie: Filter | Flüssigkeits-ID + Meta, Setzen per eigenem Paket (`PacketUpdate` 50) | Flüssigkeit (Registry), Setzen über das Vanilla-Menü-Button-Paket (Server liest den Behälter am Mauszeiger); Filterfelder zeigen die Flüssigkeitstextur statt des Block-Icons | idiomatisch, kein eigenes Paket nötig | **umgesetzt** |
| Raffinerie: Rezept | `"   ", "RTR", "TGT"` – nur in den unteren zwei Reihen | 1.21 entfernt die leere Reihe: passt auch in die oberen zwei Reihen | Rezeptformat | **umgesetzt** |
| Raffinerie: Forestry-Rezept | Biomasse → Biotreibstoff (Forestry) | entfällt | Forestry-Flüssigkeiten nicht im Umfang | **umgesetzt** |
| Raffinerie/Pumpe/Mining Well: `powerRequest`, `getTank(Seite)`, Rohr-Ausgabe | für Rohre und Gates | noch nicht umgesetzt | erst mit den Rohren (Phase 9) relevant | **offen bis Phase 9** |
| Mining Well: Tiefe | bis y 0 | bis zur Weltuntergrenze | 1.18-Weltgeometrie | **umgesetzt** |
| Minenrohr | Block mit Item (nicht im Creative-Tab) | Block ohne Item | wird nie gedroppt oder gesetzt | **umgesetzt** |
| Forestry-Zeitbasis (Arbeitstakt alle 5 Ticks, Carpenter 20/40 Ticks) | `getWorldTime()` | `getGameTime()` | wie BC-Zeitbasis | **umgesetzt** |
| Forestry-Items mit Metadaten | `thermionicTubes` (Meta 0–3), `chipsets` (Meta 0–3) | je ein Item pro Röhre/Platine; Schaltungen als Data Component `craft:circuit_board` | keine Metadaten in 1.21 | **umgesetzt** |
| Röhren-Umfang | 12 Röhren (Kupfer … Lapis) | nur Kupfer, Zinn, Bronze, Eisen | andere nur für Bienen/Farmen | **umgesetzt** |
| Fabricator/Carpenter-Rezepte | Code-Registrierung (`RecipeManagers`) | Datapack-Rezepttypen `craft:fabricator`/`craft:carpenter` (Datagen); Schmelztabelle (Glas, Glasscheibe, Sand) im Code | idiomatisch | **umgesetzt** |
| Ore-Dictionary-Gleichheit beim Vorrat | `isItemOreEqual` (nur Kupferbarren-Sonderfall) | gleiches Item + Komponenten | nur Vanilla-Kupfer im Spiel | **umgesetzt** |
| Forestry-Darstellung | TESR mit `ModelRenderer` | BER mit `ModelPart`, gleiche Geometrie/Drehwinkel; Item über BEWLR | wie BC-Engines | **umgesetzt** |
| Forestry-Ledger (Energie, Hinweise, Fehlerstatus) und Zugriffsrechte | vorhanden | fehlen | Feinschliff (Phase 10) | **offen** |
| Fabricator-Ergebnisslot | virtueller Slot direkt auf der Maschine | Anzeige-Slot, vom Server bei jedem Zugriff neu berechnet | Client hat keine Maschine im Menü | **umgesetzt** |
| Carpenter: Rezeptsuche nach Laden | sofort in `readFromNBT` | im ersten Tick | Level beim Laden noch nicht verfügbar | **umgesetzt** |
| Tank-Tooltip in Forestry-GUIs | Name (+ Menge laut Config) | Name und Menge in mB | – | **umgesetzt** |
| Electrical Engine: Drehung beim Setzen | `onBlockAdded` → `rotateEngine` sofort | im ersten Server-Tick des neu gesetzten Blocks (nicht nach dem Laden) | in 1.21 existiert die BlockEntity während `onPlace` noch nicht | **umgesetzt** |
| Electrical Engine: Anmeldung im EU-Netz | im ersten `updateServerSide` | `onLoad` (wie alle EU-Tiles) | API | **umgesetzt** |
| Electrical Engine: Platinen-Sockel | `GfxSlot` mit eigenen Paketen (70/71/72) | Menü-Slot, der keine Vanilla-Klicks annimmt; Klick wird im Menü ausgewertet, Inhalt per Slot-Sync | idiomatisch, gleiches Verhalten | **umgesetzt** |
| Electrical Engine: Lötkolben ins Inventar | `stowInInventory` über alle 40 Spieler-Slots (inkl. Rüstung) | `Inventory.add` (Haupt-/Hotbar-Slots) | Platine gehört nicht in Rüstungsslots | **umgesetzt** |
| Electrical Engine: `euConfig` | Schaltungen ändern die Werte schrittweise (`changeEnergyConfig`) | Werte werden bei jeder Sockeländerung aus der Platine neu berechnet | gleiches Ergebnis | **umgesetzt** |
| Biodust-Partikel | `EntityBiodustFX` (Reddust, RGB 0/1/0) | `DustParticleOptions` grün, Größe 1 | API | **umgesetzt** |
| Power Converters: Blöcke | 3 Blöcke mit Metadaten (Common/BC/IC2) | 12 einzelne Blöcke (`energy_bridge`, `universal_charger`, `bc_consumer` … `ic2_ev_producer`) | keine Metadaten in 1.21 | **umgesetzt** |
| Power Converters: „verbundene“ Seiten | Client prüft beim Rendern die Klasse des Nachbar-TEs | Blockstate-Eigenschaften je Seite (Multipart), vom Server bei Nachbaränderung/erstem Tick gesetzt; Charger-Oberseite bei Spieler | kein TE-Zugriff im Blockmodell | **umgesetzt** |
| Power Converters: passende Nachbarn | Klassenprüfung (`IEnergyEmitter`, `IEnergyAcceptor`, `IPowerReceptor`, `IInventory`) | EU: `instanceof EnergyEmitter/EnergyAcceptor`; MJ: Capability `craft:power_receptor`; Inventar: Item-Handler-Capability der zugewandten Seite | Capabilities | **umgesetzt** |
| Power Converters: Reihenfolge | `HashMap` (zufällige Richtungsfolge) für Bridges/Producer/Nachbarn | Richtungsfolge DOWN, UP, NORTH, SOUTH, WEST, EAST | deterministisch | **umgesetzt** |
| Power Converters: EU-Netz-Anmeldung | im ersten Tick | `onLoad` (wie alle EU-Tiles) | API | **umgesetzt** |
| Power Converters: `voltageIndex` | im NBT gespeichert | aus dem Block abgeleitet | ein Block je Spannung | **umgesetzt** |
| Universal Charger: Spieler | Spieler wird auch auf dem Client gemerkt (nur für die Textur) | nur Server; zusätzlich vergessen, wenn der Spieler entfernt wurde (Ausloggen) | Textur über Blockstate | **umgesetzt** |
| Universal Charger: Spielerinventar | `InventoryPlayer` (36 + 4 Rüstung) | `PlayerInvWrapper` (zusätzlich Nebenhand) | 1.21-Inventar | **umgesetzt** |
| Energy Bridge GUI: Sync | einzelne Container-Werte (16 Bit); System-ID nur gesendet, wenn vorhanden | `SyncedInts` (volle ints), System −1 = keins | gleiches Bild | **umgesetzt** |
| IC2 Consumer: Rezepte | LV-/MV-/HV-Trafo, MFSU | LV/MV/HV mit Phase 6.1; EV-Rezept vorhanden, MFSU selbst aber erst ab Phase 6b herstellbar | Zutaten fehlten | **umgesetzt** |
| MFSU: Rezept | `LCL/LML/LAL` mit fortg. Maschinenblock | Block vorhanden (Creative), Rezept erst mit Phase 6b: der fortg. Maschinenblock braucht fortg. Legierung, deren Mischmetallbarren im Pack nur aus GT-Platten entsteht | Zutat fehlt noch | **offen bis Phase 6b** |
| IC2-Tasten | eigene `KeyBinding`s (L-Alt, L-Strg, M, C), Zustand in einer Map je Spielerobjekt | NeoForge-`KeyMapping`s (Kategorie „IndustrialCraft 2“, gleiche Standardtasten; L-Strg teilt sich die Taste mit Vanilla-Sprinten, beide wirken), Zustand als nicht gespeichertes Spieler-Attachment, Payload `craft:key_state`; der Client sendet zusätzlich neu, wenn sich das Spielerobjekt ändert (Weltwechsel/Respawn) | 1.21-API | **umgesetzt** |
| Elektrowerkzeuge: abbaubare Blöcke | Forge-Werkzeugklasse + Material (Stein/Eisen bzw. Holz) + Blockliste | ein Block-Tag je Werkzeug: `craft:mineable/mining_drill` (= `#mineable/pickaxe` + Erde/Sand/Kies/Schnee/Ton/Ackerboden/Seelensand …), `…/chainsaw` (= `#mineable/axe` + Blätter + Spinnennetz), `…/electric_hoe`; neue 1.21-Varianten (grobe Erde, Podsol, Wurzelerde, roter Sand, Seelenerde) wie der Basisblock | keine Materialien in 1.21 | **umgesetzt** |
| Elektrowerkzeuge: `onBlockStartBreak` | Item-Hook vor dem Abbau | `BlockEvent.BreakEvent` (Haupthand des Spielers) | Hook existiert nicht mehr | **umgesetzt** |
| Kettensäge: Blöcke scheren | Forge-`IShearable`-Blöcke (Blätter, Gras, Ranken, toter Busch) | Tag `craft:chainsaw_shearable`, Drop = der Block selbst | Blöcke sind in 1.21 nicht `IShearable` | **umgesetzt** |
| Kettensäge: Sound | Leerlauf-Loop/Stopp über IC2-AudioManager | Client-Tick prüft die Haupthand des lokalen Spielers, mitlaufender Loop | API | **umgesetzt** |
| Elektrische Hacke | `UseHoeEvent` vor dem Pflügen | kein Event; nur Erde und Gras werden Ackerboden (wie im Original, keine 1.21-Erdvarianten) | Event entfällt in 1.21 | **umgesetzt** |
| Nanosäbel: zwei Items | `itemNanoSaberOff`/`itemNanoSaber` | ein Item mit Data Component `craft:active`; Modell-Override mit animierter Flacker-Textur (feste Pseudozufallsfolge statt Zufall je Frame) | Data Components | **umgesetzt** |
| Nanosäbel: Schaden | `getDamageVsEntity` 20/4 | Attribut-Modifikator +19/+3 auf die Basis 1; Angriffs-Abklingzeit (1.9+) mit Standardgeschwindigkeit | Kampfsystem 1.9+ | **umgesetzt** |
| Nanosäbel: Leerlaufzähler | globaler Zähler, +1 je Welt-Tick (effektiv alle 16 Ticks) | `getGameTime() % 16`; Nebenhand zählt als „außerhalb der Hotbar“ | deterministisch, gleiche Rate | **umgesetzt** |
| Nanosäbel: Rüstungsentzug | Treffer gegen Spieler: jedes Nano-Teil −4800 EU, leer → gelöscht; Quantum nie (Originalfehler) | 1:1 (`hurtEnemy`) | – | **umgesetzt** |
| Bergbaulaser: Materialregeln | Material Holz (Super-Heat ohne Drop), `getCanBurn` (Feuer), Material TNT (`onBlockDestroyedByPlayer`) | `#mineable/axe`, `ignitedByLava()`, `TntBlock` über `onCaughtFire` | keine Materialien in 1.21 | **umgesetzt** |
| Bergbaulaser: nicht abbaubar | Ziegel, Obsidian, Wasser, Lava, Bedrock, Glas | genau diese Vanilla-Blöcke (nicht gefärbtes/getöntes Glas, weinender Obsidian …) | 1:1 | **umgesetzt** |
| Bergbaulaser: Explosionsresistenz | `getExplosionResistance(Laser, …)` | `getExplosionResistance(level, pos, Explosion mit dem Laser als Quelle)` | API | **umgesetzt** |
| Scanner: Scantiefe | bis y = 1 (`y > 0`) | bis eine Ebene über der Weltuntergrenze (`y > minBuildHeight`) | Welthöhe −64 | **umgesetzt** |
| Scanner: kein Block gescannt | `null` als `int` → Absturz | Ergebnis 0 | kein Absturz | **umgesetzt** |
| Scanner: Erzwerte | `valuableOres` (Block-ID/Meta, Ore Dictionary) | Konventions-Tags `c:ores/*` (Tiefenschiefer-/Nethervarianten wie Basiserz), sonstige `c:ores` = 1 | Tags | **umgesetzt** |
| OV-Scanner: zweites Rezept | geladene RE-Batterie statt OD-Scanner (Originalfehler) | RE-Batterie beliebiger Ladung (ein Item für leer/geladen) | RE-Batterie ist ein Item | **umgesetzt** |
| Elektrowerkzeuge: Rezepte | GT-Rezepte mit Stahl-/Titanplatten u. a. | Werkzeuge ohne Rezept (Creative) bis Phase 6b; OD-/OV-Scanner haben ihr IC2-Rezept; Quarry-Rezept (GT) mit Diamantbohrer jetzt vorhanden | Zutaten fehlen noch | **offen bis Phase 6b** |
| Errungenschaften der Werkzeuge | „Meet the Dentist“, „Laser Time“ … | entfallen (wie alle IC2-Errungenschaften bisher) | – | **entfällt** |
| Rüstungsrechnung | Forge `ISpecialArmor.ApplyArmor` in `EntityPlayer.damageEntity` (int-Schaden, Übertrag `carryoverDamage`) | trägt ein Spieler mindestens ein IC2-Rüstungsteil, ersetzt ein `DamageContainer`-Modifikator (`Reduction.ARMOR`) die Vanilla-Rüstungsformel durch `ApplyArmor` 1:1 (Vanilla-Rüstungsteile mit Punkte/25, Härte zählt dann nicht); Vanilla-Rüstungsschaden wird über `ArmorHurtEvent` unterdrückt. Ergebnis als float ohne Übertrag. Ohne IC2-Rüstung bleibt die 1.21-Formel | kein `ISpecialArmor`, float-Lebenspunkte | **umgesetzt** |
| Rüstungsanzeige | `getArmorDisplay` | Rüstungsattribut: elektrisch dynamisch (`getDefaultAttributeModifiers` je nach Ladung), Hazmat 1 über das Material | Anzeige = Attribut | **umgesetzt** |
| Rüstungs-Tick | Forge `onArmorTickUpdate` | `inventoryTick`, wenn der Stapel im passenden Rüstungsslot steckt (beide Seiten wie im Original; Server-only, wo nur Serverzustand betroffen ist: Quantenhelm/-brust, Hazmat-Helm, Nachtsicht, Solarhelm, Static Boots) | Hook entfällt | **umgesetzt** |
| Rüstungstexturen/-material | `addArmor("ic2/<name>")`, Diamant-Rendermaterial | je Textur ein `ArmorMaterial` (`craft:<name>`, `_layer_1/_2`), Verteidigung 0 (Hazmat 1), nicht verzauberbar, keine Reparatur mit Material; Anlegegeräusch generisch (1.4.7 ohne) | Registry | **umgesetzt** |
| Quantum: Sprint/Sprung | Config `enableQuantumSpeedOnSprint` (Client → Server), `isJumping` | fest `true` (Pack-Wert); Springen über die IC2-Sprungtaste (`Ic2Keyboard`) statt `isJumping` | Feld geschützt, gleiche Bedeutung | **umgesetzt** |
| Quantum: Zustände je Spieler | statische Maps, `removePlayerReferences` beim Ausloggen | `WeakHashMap` je Spielerobjekt | gleiches Verhalten | **umgesetzt** |
| Quantumhelm: Zinndosen essen | gefüllte Zinndose aus dem Inventar | kommt mit der Zinndose (Canning Machine) | Item fehlt noch | **offen** |
| Errungenschaft „starveWithQHelmet“ | Quantumhelm | entfällt (wie alle IC2-Errungenschaften) | – | **entfällt** |
| Hazmat: Feuerquellen | `inFire`, `onFire`, `lava` | zusätzlich `campfire` (Lagerfeuer gab es 1.4.7 nicht; Feuerblock-Schaden) | neuer Block | **umgesetzt** |
| Hazmat: `isInLava` | Material Lava/Feuer am Fußblock, Höhe über `BlockFluid` | Lava-Fluid mit Level-Eigenschaft, gleiche Höhenformel | Materialien entfallen | **umgesetzt** |
| Jetpack: Flug-Kick | `resetPlayerInAirTime` (`ticksForFloatKick`) | `aboveGroundTickCount = 0` (Access Transformer) | privates Feld | **umgesetzt** |
| Jetpack: Welthöhe | `getHeight()` = 256 | `getMaxBuildHeight()` (Oberwelt 320) | Welthöhe | **umgesetzt** |
| Jetpack: Sound | IC2-AudioManager „Rucksack“-Position | mitlaufender Loop am lokalen Spieler | API | **umgesetzt** |
| Jetpack: Betankung | Canning Machine | Jetpack wird leer hergestellt (Damage 18 001), Betanken mit der Canning Machine | Maschine fehlt noch | **offen** |
| CF-Pack | Pellet-Vorrat für den Bauschaumsprüher | kommt mit dem Bauschaumsprüher (Rezept braucht ihn) | Item fehlt noch | **offen** |
| Quantum-Rezepte | Iridiumplatte, verst. Glas, fortg. Schaltkreis … | Quantum ohne Rezept (Creative) bis Phase 6b | Zutaten (GT) fehlen | **offen bis Phase 6b** |
| Static Boots: Position | NBT `x`/`z` | Data Component `craft:last_position` | Data Components | **umgesetzt** |
| Jetpack: Modus/Timer | NBT `hoverMode`/`toggleTimer` | Data Components `craft:hover_mode`/`craft:toggle_timer` | Data Components | **umgesetzt** |
| UU-Materie-Rezepte: feste Position | `AdvRecipe` mit Leerzeilen/-spalten passt nur an genau dieser Stelle der Werkbank | `Ic2ShapedRecipe` mit `FixedPosition`; die Lage im Raster liefert ein Mixin auf `CraftingInput.ofPositioned` (`GridPosition`), Vanilla schneidet sie sonst ab. Eingaben ohne bekannte Lage (z. B. Autocrafter anderer Mods) werden akzeptiert | 1.21 kennt keine positionsgebundenen Rezepte | **umgesetzt** |
| UU-Materie → Wasser/Lava | je 1 Block (Wasser `   / M / M `, Lava ` M / M / M`) | entfallen | Flüssigkeitsblöcke haben in 1.21 keine Item-Form | **umgesetzt** |
| Massefabrikator: GT-Verstärker | Nikolit-/Manganstaub je 5000 | fehlen vorerst | GT-Items gibt es noch nicht (Phase 6b) | offen |
| Strahlungseffekt | Potion-ID 24, Icon Vanilla-Gift | `craft:radiation` (MobEffect), Icon = Vanilla-Gift-Textur; Damage-Type `craft:radiation` im Tag `bypasses_armor`; Todesnachricht neu (1.4.7 ohne Übersetzung) | Registry | **umgesetzt** |

## Bewusst (Energiebalancing)

Siehe `docs/balancing.md`. Wird hier eingetragen, sobald freigegeben.

| Thema | Original | Umsetzung | Grund | Status |
|---|---|---|---|---|
| Compact Solar Arrays | Ein Block je Stufe (8/64/512 EU/t), Rezept aus Panels/Arrays + Trafo | Flache Multiblocks 3×3/9×9/27×27 mit LV-/MV-/HV-Controller (formlos aus dem Trafo), Leistung = min(Deckel, Panels mit Sonne), Details `spec/11-compact-solars.md` §4 | Balancing §3.1 „Fläche der Panels“ | **umgesetzt** |
| Solar Arrays: Lease | – | Zugehörigkeit als Lease (`core/multiblock/MultiblockLease`, alle 20 Ticks erneuert, verfällt nach 40, nicht gespeichert); verbaute Panels verwerfen ihren 1-EU-Puffer; überlappende Arrays: ein Panel zählt nur für den Controller, der es zuerst hält | Doppelzählung vermeiden | **umgesetzt** |
| Solar Arrays: Texturen/Namen | `block_textures.png` (Zeile = Stufe, Spalten Seite/oben/unten), Namen nur englisch | wie im Original (Sheet aus GitHub v4.0 geschnitten), Namen englisch auch in de_de; eigener Creative-Tab „Compact Solars“ | – | **umgesetzt** |
| Solar-Hüte | LV/MV/HV Solar Hat | nicht umgesetzt | mit dem Controller als Zutat zu billig; Entscheidung später | **offen** |
