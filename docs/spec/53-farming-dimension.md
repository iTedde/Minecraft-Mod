# 53 – Farming-Dimension (Phase 53)

Neuer Inhalt ohne Pack-Vorbild (Nutzer, 2026-11-20), Paket `dev.craft.mining` (gemeinsam mit der Mining-Welt,
`spec/18`). Stand: **Entwurf, wartet auf Freigabe.**

## 1. Zweck und Entscheidungen

Nutzer: „Eine Dimension, wo man alle Crops finden kann. Ist sonst etwas schwierig in der Welt.“ – „Nicht zum Farmen
selbst“: Man reist hin, nimmt Saatgut, Setzlinge und Früchte mit und baut zu Hause an.

| Punkt | Entscheidung |
|---|---|
| Umfang | **Alle Pflanzen** aller geladenen Mods wachsen dort – auch die IC2-Crops, die es im Original nur durch Kreuzen gibt (Nutzer, 2026-11-20) |
| Aufbau | **Ein eigenes Biom, alles wächst überall** in kleinen Beeten; Pflanzenliste **automatisch zur Laufzeit** aus allen Mods, Sonderfälle über eine kleine Ausnahmeliste (Nutzer: „2 ist gut“, 2026-11-20) |

Erwogen und verworfen:
- *Echte Biome nachbilden* (Taiga, Dschungel …, die Mods setzen ihre Wildpflanzen selbst): man sucht wieder biomweise,
  IC2-Crops kämen nicht vor.
- *Feste, handgepflegte Pflanzenliste*: volle Kontrolle, aber jeder neue Mod (z. B. Psychedelicraft, Phase 52) müsste
  nachgetragen werden.

Ziele: jede Pflanze nach wenigen Schritten erreichbar; neue Mods ohne Codeänderung dabei; ungefährlich.
Kein Ziel: Anbauen in der Dimension (geht, ist aber nicht der Zweck), Tiere, Strukturen, Erze.
Darf nicht passieren: Pflanzen, die beim Platzieren sofort abfallen (falscher Untergrund) oder ohne Drop (kein Saatgut).

## 2. Dimension `craft:farming`

### 2.1 Dimensionstyp

Wie `craft:mining` (`spec/18` §2.1) mit diesen Unterschieden (*Vorschlag*):

| Feld | Wert | Grund |
|---|---|---|
| `min_y` / `height` / `logical_height` | 0 / 256 / 256 | nur Oberfläche nötig |
| `ambient_light` | 0.0 | normales Aussehen; Licht kommt von `fixed_time` |
| `fixed_time` | 6000 | immer Mittag: Pflanzen haben Licht ≥ 9, keine Monster |
| `bed_works` | true | wie Mining-Welt |

### 2.2 Gelände

Flach (`final_density` wie Mining-Welt, Oberfläche bei **y 64**): Grundgestein y 0, Stein bis y 60, Erde y 61–63,
Grasblock y 64, darüber Luft. Keine Höhlen, kein Wasser außer in den Beeten (§3), keine Aquifere, keine Erzadern.

### 2.3 Biom `craft:farming`

Einziges Biom (`fixed`). Temperatur 0,8, Niederschlag 0,4 (Ebenen), **kein Regen** (`has_precipitation = false`),
Gras-/Laubfarbe wie Ebenen. **Keine Mob-Spawns** (alle Kategorien leer, *Vorschlag*: auch keine Tiere). Keine Carver,
Strukturen, Bäume oder Vanilla-Pflanzen-Features – alles Pflanzliche kommt aus §3. Das Biom kommt in **keinen**
Biom-Tag, damit Biom-Modifikatoren anderer Mods (Wildpflanzen, Erze, Mobs) hier nichts hinzufügen.

**Serene Seasons:** wirkt nur in den Dimensionen seiner Whitelist (Vorgabe: Oberwelt) → hier keine Jahreszeiten.
Beim Umsetzen in der Config des Pakets prüfen und ggf. festschreiben.

## 3. Pflanzen und Gärten

### 3.1 Pflanzenliste (beim Serverstart)

`FarmingPlants` baut nach dem Laden der Registries (Server-Start, vor der ersten Chunk-Erzeugung) eine **sortierte**
Liste (nach Registry-ID, damit die Welt reproduzierbar bleibt) aus allen Blöcken, die eine dieser Bedingungen erfüllen:

1. **Wildpflanzen anderer Mods:** Blöcke, deren Pfad mit `wild_` beginnt (Farmer's Delight, Farm & Charm, Brewery:
   `wild_hops` …). Sie haben bereits Untergrund und Saatgut-Drops wie in der Oberwelt. Gibt es für eine Feldfrucht
   eine Wildform, steht **nur die Wildform** in der Liste (Paarbildung über den Namen ohne `wild_`).
2. **Feldfrüchte:** Unterklassen von `CropBlock` (auch Fackellilie/Kannenpflanze), `StemBlock` (als Frucht mit
   angewachsenem Stängel), `NetherWartBlock`, `CocoaBlock`, `SweetBerryBushBlock`, `SugarCaneBlock`, `CactusBlock`,
   `BambooStalkBlock`, Höhlenranken (Leuchtbeeren); jeweils **ausgewachsen** (höchstes `age`).
3. **Setzlinge, Blumen, Pilze:** Tags `#minecraft:saplings`, `#minecraft:small_flowers`, `#minecraft:tall_flowers`,
   Pilze (`MushroomBlock`), Nether-Pilze und -Wurzeln, Azalee, Mangroven-Keimling, Tropfblatt, Sporenblüte, Chorus-Blüte.
4. **Wasserpflanzen:** Seerose, Seetang, Seegras, Meeresgurke.
5. **IC2-Crops** (§3.4).

Dazu zwei Daten-Tags für Sonderfälle: `#craft:farming_extra` (Block zusätzlich aufnehmen) und
`#craft:farming_excluded` (weglassen, z. B. Deko-Blöcke, die zufällig die Regeln treffen). Blöcke ohne Item werden
übersprungen. Die fertige Liste wird ins Log geschrieben (Anzahl je Mod), damit Lücken auffallen.

### 3.2 Untergrund

Für jede Pflanze wird der Untergrund beim Aufbau der Liste einmal bestimmt: der erste Eintrag aus der folgenden
Reihenfolge, auf dem `canSurvive` des ausgewachsenen Zustands gilt (geprüft in einer Test-Umgebung):

Ackerboden (feucht) → Grasblock → Sand → Seelensand → Myzel/Podsol → Karmesin-/Wirr-Nezel → Endstein →
Wasserblock 1 tief (für Wasserpflanzen und Reis) → seitlich an Dschungelstamm (Kakao) → hängend unter einer Decke aus
Moosblock (Leuchtbeeren, Sporenblüte).

Für Zuckerrohr wird Wasser neben dem Untergrund gesetzt. Pflanzen, für die nichts passt, kommen ins Log und **nicht**
in die Welt.

### 3.3 Gartenraster

- Die Welt ist in **Beete von 7 × 7** eingeteilt: 5 × 5 Pflanzfläche mit umlaufendem Trampelpfad (1 Block, auf jedes
  Beet aufgeteilt). Ein Beet enthält **eine** Pflanze. Auf Ackerboden steht in der Mitte ein Wasserblock, damit der
  Boden feucht bleibt; er wird mit einer Seerose oder Glasplatte abgedeckt, damit man nicht hineinfällt (*Vorschlag*).
- **Garten = Quadrat aus k × k Beeten** mit k = ⌈√n⌉ (n = Anzahl der Pflanzen). Darin steht jede Pflanze **genau
  einmal**, in fester Reihenfolge der Liste; übrige Beete sind leeres Gras. Gärten wiederholen sich lückenlos. Bei
  ca. 200 Pflanzen ist ein Garten 15 × 15 Beete = 105 × 105 Blöcke groß, d. h. von jedem Punkt aus ist jede Pflanze
  höchstens ca. 75 Blöcke entfernt.
- Die Reihenfolge ist im ganzen Raster gleich (kein Zufall); mit Jade/EMI sieht man den Namen beim Hinschauen.
- *Vorschlag:* An jeder Beet-Ecke zum Weg hin steht ein Schild mit dem Pflanzennamen – macht das Suchen leichter, kostet
  nur ein Schild je Beet.
- Bäume werden nicht ausgewachsen gesetzt, sondern als Setzlinge (genug zum Mitnehmen).
- Bestand: Beete werden nur beim Erzeugen des Chunks gesetzt und **wachsen nicht nach**. Wer alles abgeerntet hat,
  geht zum nächsten Garten.

### 3.4 IC2-Crops

- Je Sorte (22, `Crops`) ein Beet aus Crop-Stäben auf Ackerboden mit **ausgewachsener** Pflanze (Größe = Maximalgröße
  der Sorte), Werte Wachstum/Ertrag/Widerstand **1/1/1**, Scan-Stufe 0 (`CropBlockEntity.forcePlant`).
- Weed (Unkraut) wird **nicht** gesetzt.
- Ernte wie in IC2 (Rechtsklick/Abbauen). Damit man sicher Saatgut bekommt, lässt jede in der Farming-Welt erzeugte
  IC2-Pflanze beim **ersten Abbauen garantiert einen Saatbeutel** (1/1/1) fallen; danach gilt die normale IC2-Regel
  (*Vorschlag*; Kennzeichen im Block-Entity, z. B. `wild = true`). → `abweichungen.md`.
- Sonderbedingungen der Sorten (z. B. Metall-Crops brauchen den Erzblock darunter, Terra-/Netherwarze den Untergrund)
  gelten nur fürs Wachsen; ausgewachsen gesetzt sind sie trotzdem erntbar. Für Ferru/Aurelia wird *zusätzlich* der
  passende Erzblock unter den Ackerboden gesetzt, damit man ihr Verhalten sieht (*Vorschlag*).

## 4. Portal

Wie das Mining-Portal (`spec/18` §4: Rahmenform, Zünden mit Feuerzeug/Feuerkugel, 80 Ticks, POI-Suche im Umkreis 16,
Portalerzeugung), mit diesen Unterschieden:

- Rahmen aus **Bemoostem Bruchstein** (`minecraft:mossy_cobblestone`) (*Vorschlag*: grün, früh verfügbar, brennt nicht).
- Portalblock `craft:farming_portal`, Animation wie das Nether-Portal in **Grün** umgefärbt, Licht 11.
- Verbindet nur Oberwelt ↔ `craft:farming`. In der Mining-Welt und der Farming-Welt lässt sich das jeweils andere
  Portal nicht zünden.
- Das neue Portal in der Farming-Welt wird auf ein freies Wegstück gesetzt, nicht in ein Beet.

Umsetzung: Portal-Code der Mining-Welt verallgemeinern (Rahmenblock, Portalblock, Ziel-Dimension als Parameter), nicht
kopieren.

## 5. Sprache, Wiki, EMI

- Lang (de/en): „Farming-Portal“ / „Farming Portal“, Dimension „Farming-Welt“ / „Farming World“.
- Wiki: neue Seite „Farming-Welt“ (Zweck, Portalbau, Gartenraster, IC2-Saatbeutel, Hinweis auf Schilder) neben der
  Mining-Welt; Herkunftstexte (`spec/24`) der IC2-Saatbeutel ergänzen: „wild in der Farming-Welt“.
- EMI: Info-Seite zum Portal (wie Mining-Portal).

## 6. Teilphasen

| Teil | Inhalt |
|---|---|
| 53.0 | diese Spezifikation |
| 53.1 | Dimension, Biom, Gelände, Portal (Mining-Portal verallgemeinert), GameTests |
| 53.2 | Pflanzenliste, Untergrund-Ermittlung, Gartenraster (Feature), Schilder, Log |
| 53.3 | IC2-Crops in den Gärten, garantierter Saatbeutel |
| 53.4 | Wiki, EMI, Herkunft, Testplan, Craft-Version + Paket |

## 7. Für die Umsetzung offen (Entscheidung des Umsetzenden)

- Genaue Klassen/Feature-Aufteilung, ob das Gartenraster als eigenes `Feature` oder eigener `ChunkGenerator` läuft.
- Wie `canSurvive` vor der Welterzeugung geprüft wird (z. B. kleine Test-Ansicht mit `LevelReader`-Stub).
- Doppelt hohe Pflanzen (große Blumen, Kannenpflanze, Hopfen): beide Hälften setzen.
- Pflanzen mit Block-Entity, das beim Setzen Daten braucht (z. B. Forestry-Setzlinge mit Genom): ohne Daten nicht
  setzen → `#craft:farming_excluded`, sofern sie nicht sinnvoll belegt werden können.
- Ackerboden wird beim Draufspringen zertrampelt (Vanilla) – bleibt so.
