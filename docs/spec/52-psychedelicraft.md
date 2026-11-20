# 52 – Psychedelicraft auf NeoForge 1.21.1 (Phase 52)

Port der Fabric-Mod **Psychedelicraft** (Ivorius, Sollace; MIT) nach NeoForge 1.21.1 als **eigene Mod** im
Craft-Repo (Nutzer, 2026-11-20). Stand: **freigegeben 2026-11-20** (Nutzer: „Passt so“ – Kater-Zahlen §6, Sodium/Iris-Rückfallebene
und §7 wie vorgeschlagen).

## 1. Entscheidungen (bereits getroffen)

| Punkt | Entscheidung |
|---|---|
| Weg | **Port des Fabric-Codes**, keine Neuentwicklung; Grundregel 3 (nicht übersetzen) gilt hier nicht (Nutzer: „Port Mod, würde ich sagen“) |
| Ort | Unterordner `psychedelicraft/` im Craft-Repo, **eigener Gradle-Build, eigene Jar**, Mod-ID `psychedelicraft`, Paket bleibt `ivorius.psychedelicraft` (nicht `dev.craft`) |
| Paket | Jar in `overrides/mods` des CurseForge-Pakets (MIT erlaubt Weitergabe; Lizenz + Urheber in der Jar und im Wiki nennen) |
| Wünsche | richtiger **Kater**, **Halluzinationen** wie im Original, **Herstellung** im Craft-Wiki erklärt |
| Kater | freigegeben 2026-11-20: leicht = Übelkeit + Abbaulähmung · mittel = + Langsamkeit, Hunger, Blendung im Sonnenlicht · schwer = + Schwäche, Übergeben; Wasser/Kaffee lindert; Dauer bis 1 MC-Tag (Zahlen §6) |

## 2. Vorlage

| Quelle | Ort (nicht im Git, `reference/` ist ignoriert) |
|---|---|
| Quellcode Branch `1.21`, Commit `678cfa5654dee22008fb7d8897a1b9d3eac8ff80` | `reference/source/psychedelicraft/src-1.21/` (`git clone --depth 1 -b 1.21 https://github.com/Sollace/Psychedelicraft.git`) |
| Release-Jar **1.8.9+1.21** (Fabric, enthält die generierten Daten) | `reference/source/psychedelicraft/jar-1.8.9/` (GitHub-Release `1.8.9+1.21.5`, Asset `psychedelicraft-1.8.9+1.21.jar`) |
| Upstream-Wiki (Herstellung, Bilder, Rezepte) | `reference/source/psychedelicraft/wiki/` (`Psychedelicraft.wiki.git`) |

Umfang der Vorlage: 416 Java-Dateien, ~40 000 Zeilen; Yarn-Namen (MC 1.21, `yarn 1.21+build.9`).

| Bereich | Inhalt |
|---|---|
| Drogen (`entity/drug/type`) | Alkohol, Cannabis, braune/rote Zauberpilze, Tabak, Kokain, Koffein, Zucker, Badesalz, Schlafmangel, LSD, Atropin, Morphin, Methamphetamin, Kava, Wärme, Peyote, Harmonium, „Zero“/„Power“ (Riss-Glas) |
| Halluzinationen | Wesen, Identitätstausch, Rasta-Kopf, Kamera-Drift, Farben, Chat-Verzerrung (Chatbot), Hintergrundmusik je Droge |
| Bildeffekte | 10 Post-Shader (Bloom, Unschärfe, Doppelsicht, Tiefenschärfe, Hitzeflimmern, Digital …), Geometrie-Shader, Overlays (Alkohol, Müdigkeit, Wärme, Linsenreflex, Bewegungsunschärfe) |
| Pflanzen/Weltgen | Tabak, Koka, Cannabis, Hopfen, Kaffee (Coffea), Agave, Peyote, Prunkwinde, Tollkirsche, Stechapfel, Tomate, Weinreben (Spalier), **Wacholder** (Baum + komplettes Holzset inkl. Boot); Felder/Flecken per Weltgen, Dorf-Strukturen (15) |
| Verarbeitung | Trockentisch (Holz/Eisen), Maischbottich, Fässer (alle Holzarten), Destille, Flasche/Kolben, Bunsenbrenner, Tablett, Glasrohr/-ventil, Pumpe, Flaschenregal, Riss-Glas; Rezepttypen `mashing`, `drying`, `reacting`, `hardening`, `fill/change/smelting_receptical`, `crafting_pouring`, `crafting_shapeless_fluid` |
| Flüssigkeiten | eigenes Flüssigkeitssystem mit Reife/Stärke/Geschmack (Wein, Bier, Wodka, Rum, Tequila, Kaffee, Koka-Tee …), Gefäße (Becher, Flasche, Kelch, Schnapsglas, Krug, Obsidianflasche) |
| Sonstiges | Konsum (Rauchen, Schnupfen, Spritze, Bong, Pfeife), Sucht/Entzug, Erbrechen + Tüte, Molotow-Cocktail, Dorfbewohner (Händler/Drogendealer), 72 Fortschritte, Befehle, Einstellungsbildschirm, EMI über TLA-API |

## 3. Abhängigkeiten und Ersatz

| Fabric-Teil | Verwendung | Ersatz in NeoForge 1.21.1 |
|---|---|---|
| Yarn-Namen | ganzer Code + 33 Mixins | **Loom `migrateMappings` auf Mojang-Namen** (einmal im Referenz-Klon, Mercury remappt auch Mixin-Ziele); was nicht klappt, von Hand |
| Fabric API Registry/Events/ItemGroup/Particles/Command/Loot/Biome | Registrierung, Weltgen-Einbau, Loot-Ergänzungen | `DeferredRegister`, NeoForge-Events, `BiomeModifier`/`GlobalLootModifier` als JSON |
| Fabric **Transfer API** (`FluidVariant` …, 12 Dateien) | eigenes Flüssigkeitssystem, Eimer, Gefäß-Komponenten | NeoForge `FluidStack`/`IFluidHandler`(-Item) als Capability; interne Datenhaltung bleibt Psychedelicraft-eigen |
| Fabric Networking + **Fabwork** | 2 Pakete (Drogenwerte, Halluzination) | `CustomPacketPayload` + `RegisterPayloadHandlersEvent` |
| **Kirin** (MineLittlePony) | Einstellungen + Einstellungsbildschirm | `ModConfigSpec` (Client/Server) + NeoForge-Konfigurationsbildschirm |
| **Terraform Wood API** | Wacholder-Boot | eigene Boot-Entität (Vanilla-`Boat`-Unterklasse) |
| **TLA-API** (EMI/REI-Abstraktion) | 12 Dateien Rezeptanzeige | direkt **EMI-Plugin** (`@EmiEntrypoint`), REI entfällt |
| Fabric Datagen (14 Provider, ~4 600 Zeilen) | Daten | **nicht portiert**: generierte JSONs aus dem 1.8.9-Jar (bzw. eigenem `runDatagen`-Lauf der Vorlage) als feste Ressourcen; 9 `fabric:`-Bedingungen/-Zutaten auf `neoforge:` umstellen |
| Mod Menu | Einstellungsknopf | NeoForge-Modliste (`IConfigScreenFactory`) |
| Sodium 0.6.9 / Iris 1.8.8 (Kompat-Mixins, 5 Dateien) | Shader mit Sodium/Iris | Paket hat **Sodium 0.8.12 + Iris** (NeoForge) → Mixins auf diese Versionen anpassen (§8 Frage 2) |
| Access Widener | private Felder | NeoForge **Access Transformer** |

Mixins bleiben (NeoForge kann Mixins); wo NeoForge ein Event hat (HUD, Kamera, Maus, Render-Stufen), wird das Event
bevorzugt.

## 4. Projektaufbau

```
psychedelicraft/
  build.gradle, settings.gradle, gradle.properties   (ModDevGradle 2.0.148, NeoForge 21.1.259, Java 21)
  gradlew -> nutzt den Wrapper aus dem Craft-Wurzelordner (eigener Wrapper, gleiche Gradle-Version 9.2.1)
  src/main/java/ivorius/psychedelicraft/...
  src/main/resources/META-INF/neoforge.mods.toml, psychedelicraft.mixins.json, accesstransformer.cfg
  src/main/resources/assets|data/...                (aus der Vorlage)
  LICENSE (MIT, Original-Urheber) + NOTICE (Port-Hinweis)
```

- Eigenständiger Build (`cd psychedelicraft && gradlew build`), **kein** Teil des Craft-Builds; Craft hängt nicht davon ab.
- Version `1.8.9-neoforge.1` (Vorlagenversion + Port-Zähler), Jar nach `releases/psychedelicraft-<Version>.jar`.
- Abhängigkeiten im Paket: keine (alle Fabric-Bibliotheken ersetzt); EMI optional.
- GameTests wie in Craft (`-PgameTestDir=run-gametest`).

## 5. Teilpakete

| Paket | Inhalt | Prüfung |
|---|---|---|
| 52.1 | Quellimport: `migrateMappings` → Mojang-Namen, Gradle-Gerüst, `neoforge.mods.toml`, Access Transformer, Ressourcen aus dem Jar; Registrierung, Netzwerk, Konfiguration, Flüssigkeiten/Transfer, Datenkomponenten, Weltgen, Loot, Dorf, Befehle auf NeoForge umstellen – **Ziel: kompiliert, Server startet** | `gradlew build`, GameTest-Server startet, Registrierungen vollständig (GameTest „alle Blöcke/Items/Rezepte geladen“) |
| 52.2 | Spielmechanik: Pflanzen/Anbau, Trockentisch, Maischbottich, Fass, Destille, Gefäße, Konsum, Drogenwerte + Sucht, Erbrechen; GameTests je Verarbeitungsschritt | GameTests grün |
| 52.3 | Client: Renderer, Block-Entity-Renderer, Modelle, Partikel, Sounds, Halluzinationen, Post-Shader + Geometrie-Shader, Overlays, Einstellungsbildschirm; Sodium/Iris-Kompat | `runClient` bis Welt, Effekte sichtbar (Client-Test) |
| 52.4 | **Kater** (§6) + GameTests | GameTests grün |
| 52.5 | EMI-Plugin (alle Rezepttypen), Zusammenspiel mit Craft (§7) | `runClient` mit EMI |
| 52.6 | Craft-Wiki: Abschnitt „Psychedelicraft“ (Pflanzen + Fundorte, Verarbeitungsketten, Getränke/Reife, Drogen + Wirkung, Kater, Halluzinationen), Testplan, Paket | Wiki erzeugt, Paket gebaut |

## 6. Kater (freigegeben 2026-11-20)

Im Original nur: beim Aufwachen mit Alkoholwert > 0 Schadensanimation, Schlafmangel +0,25, Fortschritt „Hangover“.
Neu: Statuseffekt `psychedelicraft:hangover` (Stufe I–III, im Inventar sichtbar, **Milch heilt ihn nicht**).

**Auslöser:** Der höchste Alkoholwert seit dem letzten Nüchternsein (`peak`) wird mitgeführt. Fällt der Alkoholwert
danach unter 0,05 **oder** schläft der Spieler mit Alkohol im Blut, beginnt der Kater (Original-Aufwachen bleibt
zusätzlich erhalten). Alkohol baut sich mit 0,0002/Tick ab (1,0 → 0 in ~4 min); ein Getränk hebt den Wert je nach Stärke.

| Stufe | `peak` | Wirkungen | Dauer |
|---|---|---|---|
| leicht (I) | 0,2 – < 0,5 | Übelkeit I in Schüben (alle 60–120 s für 6 s), Abbaulähmung I | `peak` × 24 000 Ticks (max. 1 MC-Tag = 20 min) |
| mittel (II) | 0,5 – < 0,8 | + Langsamkeit I, Hunger I, **Blendung**: unter freiem Himmel am Tag weiße Überblendung + Bloom (Client-Shader aus dem Original), Gegenmittel Schatten | wie oben |
| schwer (III) | ≥ 0,8 | + Schwäche I, **Übergeben**: alle 2–5 min (Zufall) 4 Hunger weg, Übelkeit 8 s, Erbrochenes (Original-Item `vomit`) fällt auf den Boden | wie oben |

Neuer Alkohol während des Katers: Kater ruht (Effekte aus, Zeit läuft nicht), danach weiter mit dem höheren `peak`.

**Linderung:** Item-Tag `psychedelicraft:hangover_relief`.

| Mittel | Wirkung |
|---|---|
| Wasserflasche, Wasser in Psychedelicraft-Gefäßen | −2 min Restdauer |
| Kaffee (Psychedelicraft, Craft/IC2-Kaffee über den Tag) | −5 min Restdauer, Stufe −1 (nicht unter I) |
| Schlafen | Original-Verhalten (Schlafmangel) bleibt, Kater läuft weiter |

Umsetzung (52.4): Der Tag `psychedelicraft:hangover_relief` enthält die Mittel mit Kaffee-Wirkung (Craft trägt
`craft:coffee`, `dark_coffee`, `cold_coffee` ein); Wasserflaschen erkennt der Code, weil Tags keine Trank-Inhalte
unterscheiden. Wasser in Psychedelicraft-Gefäßen ist nur mit Kater trinkbar. Die Einzelwirkungen laufen als kurze
Effekte ohne Icon; sichtbar ist nur der Kater-Effekt mit Stufe und Restzeit. Übergeben füllt eine leere Papiertüte in
der Nebenhand statt Erbrochenes fallen zu lassen.

## 7. Zusammenspiel mit Craft und dem Paket (freigegeben 2026-11-20)

- **Farming-Welt** (Phase 53): Psychedelicraft-Pflanzen kommen über die automatische Pflanzenliste von selbst dazu;
  Sonderfälle (Spalier, Wacholder, Peyote auf Sand) per `#craft:farming_extra`/`#craft:farming_excluded` prüfen.
- **Doppelte Pflanzen** (Hopfen, Kaffee, Tabak, Tomate gibt es auch in IC2/Farmer's Delight/Brewery): **nicht
  zusammenlegen**, jede Mod behält ihre Kette; gemeinsame `c:`-Tags, wo die Vorlage sie schon nutzt.
- **Kater nur von Psychedelicraft-Alkohol.** Brewery hat einen eigenen Rausch; Craft-Bier/-Rum (IC2-Fass) bleiben wie sie sind.
- Keine Abhängigkeit in beide Richtungen; Craft-Kaffee kommt per Tag-Datei in den Linderungs-Tag (Datenpaket in Craft).

## 8. Fragen an den Nutzer (alle mit „ja“ beantwortet, 2026-11-20)

1. Kater-Zahlen §6 (Schwellen 0,2/0,5/0,8, Dauer `peak` × 1 MC-Tag, Linderung −2/−5 min, Milch heilt nicht) – so?
2. Sodium/Iris: Original-Kompat auf Sodium 0.8.12/Iris anpassen; **falls das nicht geht**, Psychedelicraft-Shader bei
   aktivem Iris-Shaderpack abschalten (Overlays/Halluzinationen bleiben) und in `abweichungen.md` notieren – einverstanden?
3. §7: doppelte Pflanzen getrennt lassen, Kater nur von Psychedelicraft-Alkohol – einverstanden?
