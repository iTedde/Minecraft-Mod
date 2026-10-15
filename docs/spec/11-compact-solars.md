# 11 – Compact Solars

Quelle: Compact Solar Arrays **4.0** (cpw), `mcversion 1.4.7`. Die Jar ist nicht im Pack-Download enthalten; daher
wurde der offizielle Quellcode (GitHub `cpw/compactsolars`, Tag `v4.0`, 2013-02-04) geladen:
`reference/source/compactsolars/v4.0/`. Version 4.0 passt zu MC 1.4.7 und zur Pack-Config (`CompactSolars.cfg`:
Block-ID 650, `scaleFactor = 1`, Hut-IDs ab 19551).

**Maßgebliche Originalwerte (vom Nutzer bestätigt, 2026-10-15):** LV Solar Array 8 EU/t (8 Solarpanels um einen
LV-Transformer), MV 64 EU/t (8 LV-Arrays um einen MV-Transformer), HV 512 EU/t (8 MV-Arrays um einen
HV-Transformer). Es gibt keine Compact-Solars-Jar; nicht danach suchen.

> Hinweis: Laut Auftrag werden die Arrays als Multiblocks umgebaut (`docs/balancing.md`). §1–3 beschreiben das
> Original, §4 die umgesetzte Multiblock-Variante.

## 1. Blöcke

Ein Block `compactSolar` (Material Eisen, Härte 3,0), Meta 0/1/2:

| Typ | Name | Ausgabe (EU/Paket) | Lade-Tier | Rezept |
|---|---|---:|---:|---|
| LV | Low Voltage Solar Array | 8 | 1 | `SSS/SXS/SSS`, S = IC2-Solarpanel, X = LV-Trafo |
| MV | Medium Voltage Solar Array | 64 | 2 | `SSS/SXS/SSS`, S = LV Array, X = MV-Trafo |
| HV | High Voltage Solar Array | 512 | 3 | `SSS/SXS/SSS`, S = MV Array, X = HV-Trafo |

Kosten kumuliert: LV = 8 Panels + LV-Trafo; MV = 64 Panels + 8 LV- + 1 MV-Trafo; HV = 512 Panels + 64 LV- + 8 MV- +
1 HV-Trafo. GregTech ändert diese Rezepte nicht (das Solarpanel selbst schon – siehe `08-rezepte.md`).

## 2. Verhalten (`TileEntityCompactSolar`)

- IC2-Quelle, gibt in **alle Richtungen** ab, `maxEnergyOutput` = 8 / 64 / 512. Kein interner Puffer.
- 1 Inventarslot (Lade-Slot).
- Sonnenprüfung alle 64 Ticks (Zufallsversatz): `isDaytime && !(Biom kann regnen && (Regen || Gewitter)) &&
  canBlockSeeTheSky(x, y+1, z)`. Biome ohne Regen (Wüste) sind von Regen nicht betroffen. Dimension ohne Himmel →
  nie Produktion.
- Pro Tick bei Sonne (und `scaleFactor == 1` bzw. mit Chance `1/scaleFactor`): Produktion = volles Paket.
  1. Elektrisches Item im Slot wird mit Tier `typ+1` geladen (Transferlimit des Items), die geladene Menge wird
     abgezogen.
  2. Rest wird als **ein** Paket ins Netz gegeben; nicht abgenommene Energie geht verloren.
- Kein Facing, Wrench-Drop-Rate 1,0 (droppt sich selbst), mit Wrench abbaubar.

## 3. Solar-Hüte (Items)

| Item | Rezept | Ladung/Tick |
|---|---|---:|
| LV/MV/HV Solar Hat | formlos Array (gleicher Typ) + Eisenhelm | 8 / 64 / 512 EU |

- Helm-Rüstung (Material `COMPACTSOLARHAT`: Haltbarkeit 1, Schutz 1, Verzauberbarkeit 1).
- Pro Tick bei Sonne am Kopf des Spielers (gleiche Bedingungen, Position y+1 über dem Spieler): verteilt die Leistung
  auf die **anderen Rüstungsteile** mit elektrischem Item (Tier `typ+1`).
- „Strahlung“: Kann die Energie nicht vollständig abgegeben werden (`available ≤ 0`), steigt ein Zähler um `2^typ`
  pro Tick, sonst sinkt er um die vergangenen Ticks. Überschreitet er `5 · 10^typ` (5 / 50 / 500), bekommt der
  Spieler Übelkeit für `dose/4` Ticks und der Zähler sinkt um `dose`.

## 4. Umsetzung als Multiblock (Balancing, entschieden 2026-10-15)

Variante „Fläche der Panels“ aus `balancing.md` §3.1. Leitregel: ein Array bündelt, verdichtet aber nicht.

**Blöcke:** LV-, MV-, HV-Solar-Array-Controller (Material Eisen, Härte 3,0, Metall-Sound, mit Schraubenschlüssel
abbaubar, Wrench-Drop-Rate 1,0, droppt sich selbst). Rezept **formlos**: LV-/MV-/HV-Trafo → Controller gleicher Stufe
(die Kosten der Panels werden beim Aufstellen bezahlt; Gesamtkosten = Originalrezept, s. u.).

**Aufbau** (eine Ebene, flach, jede Drehung gleich – das Muster ist symmetrisch):

| Stufe | Fläche | Aufbau | Panels | Deckel |
|---|---|---|---:|---:|
| LV | 3×3 | 8 IC2-Solarpanels um den LV-Controller | 8 | 8 EU/t |
| MV | 9×9 | 3×3-Raster aus 3×3-Feldern: 8 LV-Arrays, das mittlere Feld = 8 Panels um den MV-Controller | 72 | 64 EU/t |
| HV | 27×27 | 3×3-Raster aus 9×9-Feldern: 8 MV-Arrays, das mittlere Feld = MV-Aufbau, dessen Mitte der HV-Controller ist | 648 | 512 EU/t |

- Rekursiv: das mittlere Teilfeld einer Stufe ist ein vollständiges Feld der Stufe darunter, dessen Controller durch
  den höheren ersetzt ist. Mehrkosten gegenüber dem Original: MV +8 Panels, HV +136 Panels und +8 LV-Controller; dafür
  puffern die überzähligen Panels Abdeckung.
- **Leistung je Tick** = `min(Deckel, Anzahl Panels mit Sonne)`; Sonne je Panel wie IC2-Solarpanel (eigene Prüfung
  alle 128 Ticks, Himmelssicht über dem Panel, Tag, kein Regen außer in Wüsten). Gezählt wird beim Prüfen des Aufbaus
  (alle 20 Ticks).
- Abgabe wie im Original (`TileEntityCompactSolar`): erst Lade-Slot (Tier 1/2/3 für LV/MV/HV), Rest als **ein**
  Paket in alle Richtungen ins Netz, nicht abgenommene Energie geht verloren, kein Puffer. Spannung (maxOutput)
  8 / 64 / 512.
- Panels in einem geformten Array erzeugen selbst nichts (auch nicht für ihren eigenen Lade-Slot). Controller, die in
  einem höheren Array verbaut sind, geben nichts ab; nur der oberste geformte Controller liefert. Die Zugehörigkeit
  wird als Lease gehalten (alle 20 Ticks erneuert, verfällt nach 40 Ticks) und nicht gespeichert.
- Nicht geformter Controller: liefert nichts. Teilweise abgedeckte Arrays liefern entsprechend weniger.
- Aufbau-Prüfung nur, wenn der ganze Bereich geladen ist; sonst ruht der Controller.
- GUI wie IC2-Solarpanel (`GUISolarGenerator`): ein Lade-Slot, Sonnensymbol, solange das Array Energie liefert.

- Umsetzungsdetails (6.4b): Prüfung über `MultiblockController` mit fester Ausrichtung (Muster symmetrisch).
  Reihenfolge beim Prüfen: erst die unteren Controller (höchste Stufe zuerst), dann die Panels; ein Teil darf
  übernommen werden, wenn sein Lease frei/abgelaufen ist oder einem Controller gehört, den dieser Controller gerade
  selbst hält. Teile eines fremden Arrays werden übersprungen (zählen nicht). Ein verbautes Panel setzt seinen
  Puffer auf 0 und prüft weiter selbst die Sonne (aktive Textur bleibt).
- Texturen: `block_textures.png` aus Compact Solars v4.0, Zeile = Stufe (LV 0, MV 1, HV 2), Spalte 0 Seite, 1 oben,
  2 unten. Namen „Low/Medium/High Voltage Solar Array“ (nur englisch registriert).

**Solar-Hüte:** vorerst nicht umgesetzt (Entscheidung später; mit dem Controller als Zutat wären sie zu billig).
