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
| Tiefenschiefer-/Netherquarz-Erze | existieren nicht | Deepslate-Varianten bekommen dieselben Macerator-/Erzwert-Regeln wie das Basiserz | neue Blöcke | geplant |
| Welthöhe | 0..255, Meeresspiegel 63 | −64..320; Erz-/Öl-Höhen relativ zum Meeresspiegel übertragen (Uran 0..64 → −?..64 zu entscheiden) | 1.18-Weltgen | geplant |
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
| Bronze-Werkzeuge | intern Eisen-Material + eigene Verzauberbarkeit 13 | Tier wie Eisen, Reparatur Bronze, Enchantability 13 | API | geplant |
| Electron Tubes / Platinen / Lötkolben | Thermionic Fabricator, Carpenter | Maschinen werden 1:1 übernommen, keine Abweichung | im Umfang | **entschieden** |
| GregTech-Rezepte mit GT-Material | siehe `docs/spec/08-rezepte.md` §16 | wie im Pack (finale GT-Rezepte); GT-Materialien kommen in den Umfang, Herstellungsweg offen (`12-gregtech-materialien.md`) | Nutzerentscheidung „wie früher“ | **entschieden** (Herstellungsweg offen) |
| Silberbarren | aus Fremdmods / GregTech | GT-Silberbarren kommt mit den GT-Materialien (Silberstaub → Ofen → Silberbarren wie IC2-Original) | GT-Materialien im Umfang | geplant (abhängig vom GT-Herstellungsweg) |
| Kabel-Block | ein Block `blockCable`, Typ = Meta, Farbe/Schaum im Tile Entity | ein Block je Kabeltyp (13); Blockstate = 6 Verbindungen + Schaum (`none/wet/hard`); Farbe, Schaumfarbe, Detektor-/Splitter-Zustand im BlockEntity; Isolieren/Abisolieren = Blocktausch mit Übernahme von Farbe/Schaum | Metadaten gibt es nicht mehr | **umgesetzt** |
| Kabel-Darstellung | `RenderBlockCable` (ISBRH) mit Texturindex `Typ·16 + Farbe` | dynamisches Modell (Geometry-Loader `craft:cable`), Texturen aus dem IC2-Spritesheet geschnitten; Endflächen der Arme werden immer gezeichnet (IC2: nur bei dünnerem Nachbarkabel) – nur optisch | ISBRH existiert nicht | **umgesetzt** |
| Kabel-Kollisionsbox | eine AABB über Kern + verbundene Seiten | identisch übernommen (keine Vereinigung der Arme) | – | **umgesetzt** |
| Splitter-Kabel „bekommt Redstone“ | `isBlockGettingPowered` | `Level#getDirectSignalTo(pos) > 0` (direkte/starke Signale) | API | **umgesetzt** |
| Anmeldung am EU-Netz | `EnergyTileLoadEvent` im ersten Tile-Tick | Anmeldung in `BlockEntity#onLoad` (NeoForge ruft es in der BlockEntity-Tick-Schleife auf), Abmeldung in `setRemoved`/`onChunkUnloaded` | API | **umgesetzt** |
| Compact Solars | Jar fehlt im Pack-Download | Quellcode v4.0 (GitHub, MC 1.4.7) als Referenz, Werte verifiziert | Quelle fehlte | **entschieden** |

## Bewusst (Energiebalancing)

Siehe `docs/balancing.md`. Wird hier eingetragen, sobald freigegeben.
