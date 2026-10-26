# 33 – FE Producer (EU → FE)

Neuer Inhalt ohne Pack-Vorbild (Nutzer, 2026-10-26): Craft-Strom soll Mods mit NeoForge-Energie (**FE**) versorgen,
vor allem **Applied Energistics 2** (Energy Acceptor, Controller), außerdem Ad Astra, Industrial Foregoing,
Create Crafts & Additions.

Entschieden (Nutzer, 2026-10-26):

| Frage | Entscheidung |
|---|---|
| Bauform | **Bauteil der Power Converters** (kein Einzelblock): IC2 Consumer → Energy Bridge → **FE Producer** |
| Richtung | **nur EU → FE** (kein FE Consumer; Create-/FE-Strom kommt nicht ins EU-Netz) |
| Kurs | **1 EU = 4 FE** |

Damit bleibt `abweichungen.md` Z. 24 gültig: das EU-Netz selbst wird **nicht** an FE angebunden (keine
FE-Capability an Kabeln, Maschinen oder Speichern). FE gibt es nur am Ausgang des FE Producers.

## 1. Neues Leistungssystem `FE`

`PowerSystem` bekommt einen dritten Eintrag (ID 2, nach BC und IC2):

| Feld | Wert |
|---|---|
| Abkürzung | `FE` |
| PCU je Ein-/Ausgabe | **450** / **450** |
| Spannungen | keine (wie BC) |
| Einheit | `FE/t` |

Herleitung: 1 EU = 1800 PCU, 1 EU = 4 FE → 1 FE = 450 PCU. Nebenwirkung: auch **MJ → FE** geht über die Bridge
(BC Consumer → Bridge → FE Producer), 1 MJ = 4375 PCU ≈ 9,72 FE.

## 2. FE Producer (Block)

- Block wie die übrigen Konverter (`PowerConverterBlock`): Material Ton, Härte 1,0, kein Werkzeug nötig, Stein-Klang,
  droppt sich selbst, keine Ausrichtung, kein Schraubenschlüssel. Rechtsklick öffnet die GUI der ersten angrenzenden
  Bridge.
- `ConverterType.FE_PRODUCER` („fe_producer“, System FE, Spannungsindex 0, Producer). Kein Consumer-Gegenstück, also
  **kein** formloses Umwandlungsrezept (`counterpart()` gilt nur für BC/IC2).
- „Verbunden“-Seiten (Blockstate je Seite, wie bei den anderen Bauteilen): Nachbar hat die NeoForge-Capability
  `Capabilities.EnergyStorage.BLOCK` auf der zugewandten Seite und `canReceive()`.

### 2.1 Abgabe (Push)

`produceEnergy(pcu)` wird von der Bridge jeden Tick aufgerufen (§B.8 in `10-energieumwandlung.md`):

1. `fe = pcu / 450` (ganzzahlig); `fe == 0` → alles zurück.
2. Für jeden verbundenen FE-Empfänger (Richtungsfolge DOWN, UP, NORTH, SOUTH, WEST, EAST):
   `angenommen = receiveEnergy(fe, false)`, `fe −= angenommen`; `fe == 0` → Schleife beenden.
3. Rückgabe an die Bridge: `pcu − (abgegebene FE · 450)`. Bruchteile unter 450 PCU **bleiben in der Bridge** (kein
   Verlust, anders als beim IC2 Producer), und mehrere Empfänger teilen sich die Menge korrekt (anders als der
   BC Producer, §B.8).
4. Abgabe pro Tick nicht gedeckelt – Grenzen sind Bridge-Puffer (16 000 000 PCU ≈ 35 555 FE) und Aufnahme der
   Empfänger. Die EU-Seite deckelt ohnehin: ein EV Consumer nimmt so viel an, wie das EU-Netz liefert.

### 2.2 Entnahme (Pull)

Der FE Producer stellt selbst `Capabilities.EnergyStorage.BLOCK` (alle Seiten) bereit, damit Kabel anderer Mods
(z. B. Crafts & Additions) andocken und ziehen können:

| Methode | Verhalten |
|---|---|
| `canReceive()` | `false`; `receiveEnergy` → 0 |
| `canExtract()` | `true` |
| `extractEnergy(max, simulate)` | entnimmt bis zu `max · 450` PCU aus den angrenzenden Bridges (Richtungsfolge), nur volle FE; gibt die FE zurück |
| `getEnergyStored()` | Summe der Bridge-Inhalte / 450 (gedeckelt auf `Integer.MAX_VALUE`) |
| `getMaxEnergyStored()` | Summe der Bridge-Kapazitäten / 450 |

### 2.3 Anzeige

- Bridge-GUI: Zeile „FE“ ohne Spannung, „OUT“, Rate in `FE/t` (bzw. „x.x kFE/t“ über 1000) – wie BC.
- Ausgabe-Messung (`OutputReadout`, z. B. für Jade/Multimeter-Anzeige) in FE/t.
- Tooltip (de/en): „Gibt Strom aus einer angrenzenden Energy Bridge als FE ab (1 EU = 4 FE)“.

## 3. Rezept

`G G` / ` R ` / `G G`, G = Goldbarren, R = **Redstone-Block** → 1 FE Producer (gleiches Muster wie die anderen
Konverter, Mitte = kennzeichnendes Bauteil).

## 4. Texturen

Zwei Texturen wie bei den übrigen Bauteilen (nicht verbunden / verbunden), Grundmuster der IC2-Producer-Texturen,
Akzentfarbe **Rot** (Redstone/FE), damit der Block nicht mit IC2/BC verwechselt wird.

## 5. Spieler-Anleitung (für Wiki/EMI)

1. EU-Kabel → **IC2 Consumer** (Stufe passend zur Kabelspannung; jede Stufe nimmt jede Spannung an, §B.2).
2. Consumer neben eine **Energy Bridge**.
3. **FE Producer** an die Bridge, AE2-**Energy Acceptor** (oder anderes FE-Gerät) direkt an den FE Producer.

Beispiel: 128 EU/t aus dem Netz → 512 FE/t → reicht für rund 256 AE/t in AE2 (AE2: 1 AE = 2 FE).

## 6. Teilphasen

- **33.0** Spezifikation (diese Datei) – zur Freigabe.
- **33.1** Umsetzung: `PowerSystem.FE`, `ConverterType.FE_PRODUCER`, `FeProducerBlockEntity`, Capability-Registrierung,
  Bridge-GUI, Rezept, Lang, Texturen, GameTests (Push in einen FE-Speicher, Bruchteile bleiben in der Bridge, zwei
  Empfänger teilen korrekt, Pull über die Capability).
- **33.2** EMI-Info, Wiki (Power Converters + Create/AE2-Seite: „kein Wandler“ ersetzen), Testplan, Paket.
