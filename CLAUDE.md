# CLAUDE.md – Projekt „Craft“

Privater Nachbau von IndustrialCraft 2, BuildCraft-Quarry/Engines, Forestry Electrical Engine, Power Converters und
Compact Solars aus **FTB Ultimate (Minecraft 1.4.7)** als **ein** Mod für **Minecraft 1.21.1 / NeoForge**, ergänzt um
ein überarbeitetes Energiebalancing und neue Kraftwerke. Wird nicht veröffentlicht.

## Zielplattform

- Minecraft **1.21.1**, NeoForge **21.1.259** (ModDevGradle 2.0.148, Gradle 9.2.1), Java **21**.
- Build: Gradle mit NeoForge ModDevGradle. Lokales JDK 21: `tools/jdk-21.0.12.1+1/` (System hat nur Java 8).
- Mod-ID `craft`, Basispaket `dev.craft`. Repo: https://github.com/iTedde/Minecraft-Mod (lokal, noch nicht gepusht).

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

## Paketstruktur (ein Mod, getrennte Pakete je Ursprungs-Mod)

| Paket | Inhalt |
|---|---|
| `…core` | gemeinsame Infrastruktur: Registrierungshilfen, Netzwerk, **gemeinsames Multiblock-System**, Explosions-Algorithmus, Tick-Callbacks |
| `…ic2` | IndustrialCraft 2 komplett (EU-Netz, Kabel, Generatoren, Speicher, Maschinen, Items, Rüstung, Reaktor, Crops, Weltgen) |
| `…buildcraft` | MJ-System, Engines, Landmarks, Quarry, Öl, Pumpe, Tank, Raffinerie (optional Rohre) |
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

`docs/spec/README.md` (Index), `docs/balancing.md`, `docs/abweichungen.md`.

## Phasenplan

| Phase | Inhalt | Status |
|---|---|---|
| 1 | Dekompilieren, Spezifikation, Balancing-Vorschlag, CLAUDE.md | **erledigt – wartet auf OK** |
| 2 | Mod-Grundgerüst, EU-System, Kabel, gemeinsames Multiblock-System | **erledigt** (abgenommen) |
| 3 | IC2 Early Game: Generator, Geothermal, Solarpanel, Wassermühle, Windmühle, Batterien (RE, SU, Energie-/Lapotronkristall), BatBox, Macerator, Electric Furnace, Extractor, Compressor, Recycler, Upgrades **+ alle Vorprodukte** (Materialien, Eisenofen, Gummibäume, Zinnerz, Baumzapfer, Schraubenschlüssel, Zellen …). Solar/Wasser/Wind ohne Rezept bis Phase 6b | **in Arbeit** |
| 4 | BuildCraft: MJ-System, Engines, Landmarks, Quarry, Pumpe, Tank, Raffinerie, Mining Well | offen |
| 5 | Energieumwandler: Electrical Engine (inkl. Thermionic Fabricator, Carpenter, Platinen, Röhren), Power Converters | offen |
| 6 | IC2 Mid/Late Game: MFE, MFSU, Trafos, Mass Fabricator, Werkzeuge, Nano-/Quantenrüstung, Solar Arrays als Multiblocks | offen |
| 6b | GregTech-Maschinen 1:1 (Elektrolyseur, Hochofen-Multiblock + Heizspulen, Plattenbieger, Assembler, ggf. weitere) und GT-Materialien; danach Rezepte für Solarpanel, Wasser-/Windmühle u. a. [GT⚠] | offen |
| 7 | Reaktor | offen |
| 8 | Neue Kraftwerke: Kohlekraftwerk mit Koksofen, Wasserturbine | offen |
| 9 | Optional: BuildCraft-Rohre | offen |
| 10 | Feinschliff: Texturen, JEI-Integration, Tests | offen |

Nicht explizit in einer Phase, aber Teil von „IC2 komplett“ (Zuordnung klären): Eisenofen, Canning Machine,
Induktionsofen, Miner, Pumpe, Magnetisierer, Elektrolyseur, Terraformer, Teleporter, Tesla-Spule, Crops/Crop-Matron,
Personal-Blöcke, Luminator, Sprengstoffe, Bauschaum, Kaffee/Fass.

## Offene Entscheidungen (Stand Phase 1)

1. GT-Erze (Bauxit, Sheldonit, Rubin …) – für Silizium/Aluminium nicht nötig (Elektrolyse von Sand/Tonstaub). Der
   Herstellungsweg selbst ist entschieden (s. u.).
2. Massefabrikator (im Pack entfernt, dort GT Matter Fabricator).
3. Balancing-Vorschlag freigeben (`docs/balancing.md` §5).
4. GT-Nicht-Rezept-Änderungen (Upgrade-Stack 4, Schrottbox-Drops) übernehmen?
5. Iridiumerz-Generierung (GT).

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
