# CLAUDE.md – Projekt „Craft“

Privater Nachbau von IndustrialCraft 2, BuildCraft-Quarry/Engines, Forestry Electrical Engine, Power Converters und
Compact Solars aus **FTB Ultimate (Minecraft 1.4.7)** als **ein** Mod für **Minecraft 1.21.1 / NeoForge**, ergänzt um
ein überarbeitetes Energiebalancing und neue Kraftwerke. Wird nicht veröffentlicht.

## Zielplattform

- Minecraft **1.21.1**, NeoForge **21.1.259** (ModDevGradle 2.0.148, Gradle 9.2.1), Java **21**.
- Build: Gradle mit NeoForge ModDevGradle. JDK 21: `C:/Program Files/Java/jdk-21` (in `gradle.properties`; Projekt liegt seit 2026-10-15 unter `X:/Craft`, `tools/` ist dabei verloren gegangen; Python 3.12 + Pillow für Texturen: `%LOCALAPPDATA%/Programs/Python/Python312/python.exe`).
- Mod-ID `craft`, Basispaket `dev.craft`. Repo: https://github.com/iTedde/Minecraft-Mod (`main` und `phase3-wip` gepusht).

## Grundregeln

1. **1:1-Mechanik**: Rezepte, EU-/MJ-Werte, Umrechnungsraten, Zeiten, Kapazitäten, Spannungsstufen, Kabelverluste,
   Explosionsverhalten, Reaktorlogik und auch Balancing-Eigenheiten/Originalfehler wie im Pack (siehe `docs/spec/`).
   Keine „Verbesserungen“.
2. **Einzige bewusste Ausnahme: Energiebalancing** nach `docs/balancing.md` – erst nach Freigabe durch den Nutzer
   umsetzen.
3. **Alten Code nicht übersetzen.** `reference/decompiled/` ist nur Spezifikation. Neuer Code ist sauber und idiomatisch
   für NeoForge 1.21.1: DeferredRegister, Data Components, Capabilities, Datagen für Rezepte/Modelle/Blockstates/Lang/
   Loot/Tags/Weltgen, Payloads für Netzwerk.
4. Wo 1.21 etwas unmöglich macht: nächstliegende Lösung wählen und in `docs/abweichungen.md` dokumentieren.
5. Nach **jeder Phase** muss der Mod bauen (`gradlew build`) und im Client starten (`gradlew runClient`).
6. Offene Entscheidungen nicht eigenmächtig treffen – nachfragen (Liste unten).
7. Dokumentation auf Deutsch; Code, Bezeichner und Commit-Messages auf Englisch.
8. `reference/decompiled/` ist vollständig dekompiliert. **Nicht erneut dekompilieren.**
9. **Niemals ganze Ordner oder große Klassen komplett lesen.** Gezielt mit Grep nach Klassen-, Methoden- oder
   Feldnamen suchen und nur die relevanten Stellen lesen.
10. Ab Phase 2 ist **`docs/spec/` die Quelle der Wahrheit**. `reference/` nur öffnen, wenn in der Spezifikation etwas
    fehlt oder unklar ist, und die Lücke dann in `docs/spec/` nachtragen.
11. Compact Solars liegt nicht als Jar vor – nicht danach suchen. Werte: LV 8 / MV 64 / HV 512 EU/t
    (`docs/spec/11-compact-solars.md`), Umbau als Multiblock laut `docs/balancing.md`.

## Paketstruktur (ein Mod, getrennte Pakete je Ursprungs-Mod)

| Paket | Inhalt |
|---|---|
| `…core` | gemeinsame Infrastruktur: Registrierungshilfen, Netzwerk, **gemeinsames Multiblock-System**, Explosions-Algorithmus, Tick-Callbacks |
| `…ic2` | IndustrialCraft 2 komplett (EU-Netz, Kabel, Generatoren, Speicher, Maschinen, Items, Rüstung, Reaktor, Crops, Weltgen) |
| `…buildcraft` | MJ-System, Engines, Landmarks, Quarry, Öl, Pumpe, Tank, Raffinerie Rohre |
| `…forestry` | Electrical Engine, Electron Tubes, Platinen, Lötkolben, Thermionic Fabricator, Carpenter |
| `…powerconverters` | Energy Bridge, Charger, BC-/IC2-Konverter |
| `…compactsolars` | Solar Arrays (als Multiblocks laut Balancing) |
| `…gregtech` | nur die für Pack-Rezepte nötigen GT-Materialien (und ggf. deren Herstellung, offen) |
| `…powerplants` (neue Inhalte) | Wasserturbine, Kohlekraftwerk (Kessel, Dampfturbine), Koksofen |

MJ und EU bleiben getrennte Systeme; Verbindung nur über Electrical Engine (EU→MJ) und Power Converters.

## Quellen und Werkzeuge

- Pack: `ftb ultimate/` (Original-Installation inkl. Configs; `.ftbapp/version.json` = Manifest).
- `reference/jars/` – Original-Jars; `reference/config/` – Pack-Configs (maßgeblich für Werte, v. a. `IC2.cfg`,
  `GregTech/*.cfg`, `buildcraft/main.conf`, `PowerConverters.cfg`).
- `reference/remapped/` – Jars mit MCP-Namen (SpecialSource + `tools/mcp/mcp.srg`), `reference/decompiled/` –
  Vineflower-Ausgabe davon.
- Neu dekompilieren: siehe Befehle in `tools/` (SpecialSource `--live` mit `mc147.jar` im Classpath, dann
  `vineflower.jar -dgs=1 -e=<libs>`).
- Externe Quellen sind freigegeben (z. B. BuildCraft-Quellcode 3.4.3). Compact Solars 4.0: `reference/source/compactsolars/v4.0/` (nicht im Pack-Download, von GitHub).

## Spezifikation

`docs/spec/README.md` (Index), `docs/balancing.md`, `docs/abweichungen.md`. Code-Überblick: `docs/architektur.md` (statt Code als Muster lesen).

## Phasenplan

| Phase | Inhalt | Status |
|---|---|---|
| 1 | Dekompilieren, Spezifikation, Balancing-Vorschlag, CLAUDE.md | **erledigt – wartet auf OK** |
| 2 | Mod-Grundgerüst, EU-System, Kabel, gemeinsames Multiblock-System | **erledigt** (abgenommen) |
| 3 | IC2 Early Game: Generator, Geothermal, Solarpanel, Wassermühle, Windmühle, Batterien (RE, SU, Energie-/Lapotronkristall), BatBox, Macerator, Electric Furnace, Extractor, Compressor, Recycler, Upgrades **+ alle Vorprodukte** (Materialien, Eisenofen, Gummibäume, Zinnerz, Baumzapfer, Schraubenschlüssel, Zellen …). Solar/Wasser/Wind ohne Rezept bis Phase 6b | **erledigt** (abgenommen) |
| 4 | BuildCraft: MJ-System, Engines, Landmarks, Quarry, Pumpe, Tank, Raffinerie, Mining Well | **erledigt** (abgenommen) |
| 5 | Energieumwandler: Electrical Engine (inkl. Thermionic Fabricator, Carpenter, Platinen, Röhren), Power Converters | **erledigt** (abgenommen) |
| 6 | IC2 Mid/Late Game: 6.1 MFE, MFSU, Trafos · 6.2 Elektrowerkzeuge + Tastensystem · 6.3 Nano-/Quanten-/Utility-Rüstung · 6.4 Mass Fabricator, Solar Arrays als Multiblocks | **erledigt** (abgenommen) |
| 6b | GregTech-Maschinen 1:1 (Elektrolyseur, Hochofen-Multiblock + Heizspulen, Plattenbieger, Assembler, ggf. weitere) und GT-Materialien; danach Rezepte für Solarpanel, Wasser-/Windmühle u. a. [GT⚠]. Teilpakete: 6b.1 Plattenbieger (erledigt) · 6b.2 Elektrolyseur (erledigt) · 6b.3 Hochofen (erledigt) · 6b.4 Assembler (erledigt) · 6b.5a Canning Machine IC2 + GT (erledigt) · 6b.5b Implosionskompressor (erledigt) · 6b.5c Zentrifuge (erledigt, erreichbare Rezepte) | **erledigt** (abgenommen) |
| 7 | Reaktor: 7a IC2-Reaktor (erledigt) · 7b erreichbare GT-Reaktorteile (erledigt) · 7c Nuke (erledigt) | **erledigt** – wartet auf Abnahme |
| 8 | GT-Nachträge: 8a NaK-Kühlzellen (Natrium aus Sodalith, Kalium aus Enderperlenstaub) + Containment-Reaktorplatte mit Bleiplatte (erledigt) · 8b Spezifikation GT-Weltgen (erledigt) · 8c **alle GT-Erze** (Weltgen) und was sie freischalten | **in Arbeit** (8a, 8b erledigt) |
| 9 | Neue Kraftwerke: Kohlekraftwerk mit Koksofen, Wasserturbine | offen |
| 10 | BuildCraft-Rohre (fest im Umfang) | offen |
| 11 | Feinschliff: Texturen, EMI-Integration (Rezeptanzeige), Xaero's Minimap mitinstallieren, Tests | offen |

Phasen-Umnummerierung (Nutzer, 2026-10-16): Phase 8 „GT-Nachträge“ eingeschoben; die bisherigen Phasen 8/9/10
(Kraftwerke, Rohre, Feinschliff) heißen jetzt 9/10/11. Ältere Einträge in `docs/STATUS.md` sind entsprechend
angepasst.

Nicht explizit in einer Phase, aber Teil von „IC2 komplett“ (Zuordnung klären): Eisenofen,
Miner, Pumpe, Terraformer, Teleporter, Tesla-Spule, Crops/Crop-Matron,
Personal-Blöcke, Luminator, Sprengstoffe (Industrie-TNT erledigt in 6b.5b, Nuke in 7c), Bauschaum, Kaffee/Fass.

## Offene Entscheidungen (Stand Phase 1)

1. ~~GT-Erze~~ **Entschieden (2026-10-16):** **alle** GT-Erze wie im Pack werden in Phase 8 generiert.
   Der Herstellungsweg selbst ist entschieden (s. u.).
2. ~~Massefabrikator~~ **Entschieden:** IC2-Massefabrikator 1:1 mit IC2-Rezept (kein GT Matter Fabricator).
3. Balancing-Vorschlag freigeben (`docs/balancing.md` §5) – **Solar freigegeben** (Fläche der Panels), Rest offen.
4. GT-Nicht-Rezept-Änderungen: Upgrade-Stack 4 und Recycler-Blacklist noch offen. **Entschieden:** Schrottbox-Drops wie im Pack – GT ersetzt die IC2-Liste (Schrott 200 + GT-Liste; GT-Items erst, wenn es sie gibt).
5. ~~Iridiumerz-Generierung (GT)~~ **Entschieden:** wie im Pack (Teil der GT-Erze, Phase 8).
6. Rezeptanzeige und Minimap (Nutzerwunsch 2026-10-15): **Entschieden:** **EMI** (statt NEI, das es für 1.21 nicht gibt) mit eigener Integration (Rezeptkategorien für IC2-/GT-/BC-/Forestry-Maschinen, Phase 11); Minimap **Xaero's Minimap**, nur mitinstalliert (keine eigene Integration).

Aktueller Stand/ToDo: `docs/STATUS.md`.

Entschieden: GregTech-Rezepte werden **wie im Pack** umgesetzt (finale GT-Varianten, kein IC2-Original-Fallback).
Die dafür nötigen GT-Materialien (Platten, Stahl, Titan, Silizium, Beryllium, Platin, Iridium-Legierung …) kommen in
den Umfang; Werte/Rezepte aus `reference/decompiled/gregtech/`.

Entschieden: externe Quellen erlaubt (Compact Solars wird nachgeladen); Kupfer = Vanilla-Kupfer mit IC2-Häufigkeit,
eigenes Zinnerz; BuildCraft Pumpe/Tank/Raffinerie und Forestry Thermionic Fabricator/Carpenter sind im Umfang
(Phasenzuordnung: Pumpe/Tank/Raffinerie → Phase 4, Fabricator/Carpenter → Phase 5).

Entschieden (Phase 3): GT-Materialien werden über **1:1 nachgebaute GT-Maschinen** hergestellt (eigene Phase 6b);
bis dahin haben die betroffenen Blöcke (Solarpanel, Wasser-/Windmühle …) kein Rezept. Rohmetall (Eisen/Gold/Kupfer)
→ Macerator → 2 Staub wie der Erzblock. Phase 3 enthält alle Vorprodukte ihrer Rezepte; Upgrades (Logik + Items)
ebenfalls, Rezept des Transformer-Upgrades erst mit dem MV-Trafo (Phase 6).
