# 11 – Compact Solars

Quelle: Compact Solar Arrays **4.0** (cpw), `mcversion 1.4.7`. Die Jar ist nicht im Pack-Download enthalten; daher
wurde der offizielle Quellcode (GitHub `cpw/compactsolars`, Tag `v4.0`, 2013-02-04) geladen:
`reference/source/compactsolars/v4.0/`. Version 4.0 passt zu MC 1.4.7 und zur Pack-Config (`CompactSolars.cfg`:
Block-ID 650, `scaleFactor = 1`, Hut-IDs ab 19551).

**Maßgebliche Originalwerte (vom Nutzer bestätigt, 2026-10-15):** LV Solar Array 8 EU/t (8 Solarpanels um einen
LV-Transformer), MV 64 EU/t (8 LV-Arrays um einen MV-Transformer), HV 512 EU/t (8 MV-Arrays um einen
HV-Transformer). Es gibt keine Compact-Solars-Jar; nicht danach suchen.

> Hinweis: §1–3 beschreiben das Original, §4 die Umsetzung: seit 2026-10-24 nur noch Solarpanels mit 9×9-Bonus,
> die Arrays sind stillgelegter Altbestand (`docs/balancing.md` §3.1).

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

## 4. Umsetzung: nur Solarpanels mit 9×9-Bonus (Nutzer, 2026-10-24)

Ersetzt die 3×3-Felder mit Array-Mitte (2026-10-20 bis 2026-10-24) und davor „Fläche der Panels“ (3×3/9×9/27×27,
2026-10-15 bis 2026-10-20). Solarstrom kommt nur noch aus dem **IC2-Solarpanel**; die Compact-Solars-Arrays haben
keine Funktion mehr.

**Solarpanel:** wie im Original 1 EU/t bei Sonne (Himmelssicht über dem Panel, Tag, kein Regen außer in Wüsten;
Prüfung alle 128 Ticks), **jedes Panel liefert selbst** (keine Mitte, kein Ring).

**Feldbonus ×1,2:** Liegt ein Panel in mindestens einem **lückenlosen 9×9-Quadrat aus Solarpanels auf derselben
Höhe**, liefert es bei Sonne 1,2 EU/t – technisch jeden 5. besonnten Tick 2 EU statt 1 (Pakete bleiben LV). Nur eine
Stufe, kein Deckel.

- Das Quadrat darf beliebig liegen, das Panel muss nicht in der Mitte sein; ein 10×10-Feld bekommt den Bonus also
  vollständig, eine 9×20-Fläche ebenfalls. Panels außerhalb jedes vollen 9×9 (z. B. eine zehnte Spalte an einem
  9×9) liefern 1 EU/t.
- Für das Quadrat zählen alle Solarpanel-Blöcke, auch abgedeckte; den Bonus erhält aber nur, wer selbst Sonne hat.
- Blöcke in nicht geladenen Chunks zählen als fehlend.
- Prüfung zusammen mit der Sonne (alle 128 Ticks, versetzt je Panel) und einmal beim ersten Tick nach dem Laden; nicht
  gespeichert. Ein neu gebautes Feld bekommt den Bonus also spätestens nach 128 Ticks (≈ 6 s).
- Umsetzung: 17×17-Umgebung der Ebene als Präfixsummen, dann die 81 möglichen 9×9-Quadrate, die das Panel
  enthalten, in O(1) je Quadrat (`SolarPanelBlockEntity.isInField`).
- Puffer des Panels 2 EU (für den Bonus-Tick), Paketgröße 1 bzw. 2 im Feld.

| Aufbau | Leistung (Tag) |
|---|---:|
| einzelnes Panel | 1 EU/t |
| 9×9 = 81 Panels | 97,2 EU/t (statt 81) |
| 10×10 = 100 Panels | 120 EU/t |
| 9×9 mit einer Lücke (80 Panels) | 80 EU/t |

**Alte Arrays (LV/MV/HV):** bleiben registriert, damit Welten und Truhen nichts verlieren, aber
- kein Herstellungsrezept, nicht im Kreativtab (Tab „Compact Solars“ entfällt) und in EMI ausgeblendet;
- liefern nichts, kein Energienetz-Anschluss; Lade-Slot und GUI bleiben (Inhalt kann entnommen werden, droppt beim
  Abbau), Abbau droppt das Array;
- **Zerlegen** in der Werkbank: 1 Array → 8 der Stufe darunter (HV → 8 MV, MV → 8 LV, LV → 8 Solarpanels). Der Trafo
  aus dem Originalrezept geht dabei verloren.
- Texturen/Namen wie bisher (`block_textures.png`, englische Namen).

**Solar-Hüte:** entfallen (Nutzer, 2026-10-17; mit dem Controller als Zutat wären sie zu billig).
