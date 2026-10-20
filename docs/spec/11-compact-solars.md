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

## 4. Umsetzung als Multiblock (Balancing; 3×3 mit Bonus, Nutzer 2026-10-20)

Ersetzt die Variante „Fläche der Panels“ (3×3 / 9×9 / 27×27, 2026-10-15 bis 2026-10-20): die riesigen Flächen waren
dem Nutzer zu unhandlich. Jetzt ist jede Stufe ein 3×3-Feld, und das Aufstellen in dieser Konstellation wird mit einem
Bonus belohnt.

**Blöcke:** LV-, MV-, HV-Solar-Array (Material Eisen, Härte 3,0, Metall-Sound, mit Schraubenschlüssel abbaubar,
Wrench-Drop-Rate 1,0, droppt sich selbst). Rezept **wie im Original** (§1): `SSS/SXS/SSS`, S = IC2-Solarpanel /
LV-Array / MV-Array, X = LV-/MV-/HV-Trafo.

**Aufbau** (eine Ebene, flach, jede Drehung gleich): das Array in der Mitte, darum ein Ring aus 8 Blöcken. Im Ring sind
IC2-Solarpanels und Arrays **bis zur Stufe der Mitte** erlaubt, beliebig gemischt (um ein MV also Panels, LV- und
MV-Arrays, aber kein HV-Array). Fehlt ein Ringblock oder steht dort etwas anderes, ist das Feld nicht geformt.

**Grundwert** je Block = die Panels, die in ihm stecken: Solarpanel 1, LV 8, MV 64, HV 512 EU/t.

**Leistung je Tick** = `⌊(Grundwerte aller besonnten Teile, Mitte eingeschlossen) × Bonus der Mitte⌋`, Bonus
**LV ×1,2 · MV ×1,4 · HV ×1,5**. Der Bonus ist zugleich der Ertrag je verbautem Panel. Kein Deckel.

| Mitte | Ring | Rechnung | Leistung (Tag) | Panels verbaut |
|---|---|---|---:|---:|
| LV | 8 Solarpanels | 16 × 1,2 | 19 EU/t | 16 |
| LV | 8 LV-Arrays | 72 × 1,2 | 86 EU/t | 72 |
| MV | 8 LV-Arrays | 128 × 1,4 | 179 EU/t | 128 |
| MV | 8 MV-Arrays | 576 × 1,4 | 806 EU/t | 576 |
| HV | 8 MV-Arrays | 1024 × 1,5 | 1536 EU/t | 1024 |
| HV | 8 HV-Arrays | 4608 × 1,5 | 6912 EU/t | 4608 |

- Sonne: Panels wie IC2-Solarpanel (eigene Prüfung alle 128 Ticks, Himmelssicht über dem Panel, Tag, kein Regen außer
  in Wüsten); Arrays (Mitte und Ring) mit derselben Bedingung, geprüft beim Prüfen des Aufbaus (alle 20 Ticks). Ein
  abgedeckter Block zählt 0, das Feld bleibt geformt.
- Abgabe wie im Original (`TileEntityCompactSolar`): erst Lade-Slot (Tier 1/2/3 für LV/MV/HV), Rest als Pakete von
  höchstens 8 / 64 / 512 EU (Spannung wie im Original) in alle Richtungen ins Netz – so viele Pakete wie nötig (19 EU/t
  am LV = 8 + 8 + 3) –, nicht abgenommene Energie geht verloren, kein Puffer.
- Einzeln aufgestellt oder ohne vollständigen Ring liefert ein Array **nichts**. Panels und Arrays im Ring eines
  geformten Felds erzeugen selbst nichts (Panels auch nicht für ihren Lade-Slot); nur die Mitte liefert. Die
  Zugehörigkeit wird als Lease gehalten (alle 20 Ticks erneuert, verfällt nach 40 Ticks) und nicht gespeichert.
- Felder dürfen sich Ränder teilen; ein Teil zählt nur für ein Feld (wer es zuerst hält).
- Aufbau-Prüfung nur, wenn das 3×3-Feld geladen ist; sonst ruht das Array.
- GUI wie IC2-Solarpanel (`GUISolarGenerator`): ein Lade-Slot, Sonnensymbol, solange das Array Energie liefert.

- Umsetzungsdetails: Prüfung über `MultiblockController` mit fester Ausrichtung (Muster symmetrisch, Ringsymbol `R`).
  Reihenfolge beim Prüfen: erst die Arrays im Ring (höchste Stufe zuerst), dann die Panels; ein Teil darf übernommen
  werden, wenn sein Lease frei/abgelaufen ist oder einem Array gehört, das diese Mitte gerade selbst hält. Teile eines
  fremden Felds werden übersprungen (zählen nicht). Ein verbautes Panel setzt seinen Puffer auf 0 und prüft weiter
  selbst die Sonne (aktive Textur bleibt). Abgerundet wird erst nach der Multiplikation.
- Texturen: `block_textures.png` aus Compact Solars v4.0, Zeile = Stufe (LV 0, MV 1, HV 2), Spalte 0 Seite, 1 oben,
  2 unten. Namen „Low/Medium/High Voltage Solar Array“ (nur englisch registriert).
- Bestehende Welten: alte 9×9-/27×27-Aufbauten liefern nach dem Update nur noch, wo zufällig ein gültiges 3×3-Feld
  entsteht; sie müssen umgebaut werden. Die Array-Blöcke selbst bleiben erhalten.

**Solar-Hüte:** entfallen (Nutzer, 2026-10-17; mit dem Controller als Zutat wären sie zu billig).
