# 11 – Compact Solars

Quelle: Compact Solar Arrays **4.0** (cpw), `mcversion 1.4.7`. Die Jar ist nicht im Pack-Download enthalten; daher
wurde der offizielle Quellcode (GitHub `cpw/compactsolars`, Tag `v4.0`, 2013-02-04) geladen:
`reference/source/compactsolars/v4.0/`. Version 4.0 passt zu MC 1.4.7 und zur Pack-Config (`CompactSolars.cfg`:
Block-ID 650, `scaleFactor = 1`, Hut-IDs ab 19551).

> Hinweis: Laut Auftrag werden die Arrays später als Multiblocks umgebaut (`docs/balancing.md`). Diese Datei
> beschreibt das Original.

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
