# Energiebalancing – Bestandsaufnahme und Vorschlag

Status: **Vorschlag, nicht freigegeben.** Nichts davon wird umgesetzt, bevor es bestätigt ist. Alle Originalwerte
stammen aus `docs/spec/` (IC2 1.115.231, BuildCraft 3.4.3, Forestry 2.0.0.11, Power Converters, Compact-Solars-Config).

Grundsatz laut Auftrag: Rezepte und Kosten bleiben wie im Original; gebalanct wird über **Leistung, Effizienz und
Platzbedarf**. Solar wird nicht pauschal geschwächt, die anderen Quellen werden eher effizienter.

## 1. Annahmen für Vergleiche

- 1 Minecraft-Tag = 24 000 Ticks. „Tag“ für Solar (`isDaytime`) ≈ 12 000–13 000 Ticks → Solar-Mittel ≈ **0,5 ×**
  Spitzenleistung ohne Regen; mit gelegentlichem Regen ≈ 0,45 ×.
- MJ→EU über Power Converters: **1 MJ = 2,43 EU** (4375/1800 PCU). EU→MJ über Electrical Engine: **3 EU = 1 MJ**.
- „Fläche“ = belegte Grundfläche inkl. zwingend freizuhaltender Umgebung (Windmühle: Hindernis-Würfel 9×9).
- Ressourcen: 1 Kohle, 1 Eimer (1000 mB) Lava/Öl/Treibstoff, 1 Uranbarren.

## 2. Originalwerte je Energiequelle

| Quelle | Leistung | Fläche | EU/t pro Block | Energie pro Ressource | Bedingungen / Aufwand |
|---|---:|---:|---:|---:|---|
| IC2 Generator | 10 EU/t | 1 | 10 | 4 000 EU / Kohle | Brennstoff nachfüllen |
| IC2 Geothermie | 20 EU/t | 1 | 20 | 20 000 EU / Lava-Eimer | Lava-Logistik (Pumpe) |
| IC2 Solarpanel | 1 EU/t (Tag) | 1 | 1 (≈ 0,5 Mittel) | – | Himmel, Tag, kein Regen |
| IC2 Wassermühle | ≤ 0,26 EU/t passiv; 1–2 EU/t mit Eimern/Zellen | 3×3×3 | ≈ 0,03 | 500 EU/Wassereimer, 1 000 EU/Wasserzelle | praktisch nutzlos |
| IC2 Windmühle | `wind·(y−64−Hindernisse)/750`; y 128, Wind 15: ≈ 1,3 EU/t; Gewitter ×1,5 | ≈ 9×9 (frei) | ≈ 0,016 | – | Höhe, freie Umgebung, Bruchrisiko > 5 EU/t |
| IC2 Kernreaktor | 5 EU/t je Puls; Bsp. 1 Vierfachzelle allein 60 EU/t, typische Designs 100–400+ EU/t | ≈ 3×3 (Reaktor + Kammern) | 10–50+ | Einzelzelle 1 000 000 EU (10 000 Zyklen à 5 EU/t·20 t); Vierfachzelle ≈ 12 Mio. EU | Kühlung, Explosionsrisiko, Redstone |
| Compact Solars LV | 8 EU/t (Tag) | 1 | **8** | – | Rezept ≈ 8 Panels |
| Compact Solars MV | 64 EU/t (Tag) | 1 | **64** | – | ≈ 64 Panels |
| Compact Solars HV | 512 EU/t (Tag) | 1 | **512** | – | ≈ 512 Panels |
| BC Redstone-Engine | 0,05 MJ/t | 1 | 0,12 EU-Äquiv. | – | Redstone |
| BC Stirling-Engine | 1 MJ/t (= 2,43 EU/t) | 1 (+ Konverter) | 2,4 | 1 600 MJ = 3 888 EU / Kohle | Explodiert, wenn voll und weiterbrennt |
| BC Combustion (Lava) | 1 MJ/t | 1 | 2,4 | 20 000 MJ = 48 600 EU / Eimer | Wasserkühlung |
| BC Combustion (Öl) | 3 MJ/t (= 7,3 EU/t) | 1 | 7,3 | 60 000 MJ = **145 800 EU / Eimer** | Kühlung, Pumpe, Öl suchen |
| BC Combustion (Treibstoff) | 6 MJ/t (= 14,6 EU/t) | 1 | 14,6 | 600 000 MJ = **1 458 000 EU / Eimer** | braucht Raffinerie (im Umfang, 12 MJ pro mB) |
| Forestry Electrical Engine | 6 EU → 2 MJ (Upgrades bis 36 EU → 10 MJ) | 1 | Umwandler | 3 : 1 bis 3,6 : 1 | Redstone; Rundreise EU→MJ→EU ≈ 81 % |
| Power Converters | 1 MJ ↔ 2,43 EU, Producer nur volle Pakete | 1 je Block | Umwandler | verlustfrei (Rundungsreste) | Energy Bridge nötig |

### Problembefund

1. **Compact Solars**: HV-Array = 512 EU/t auf **einem** Block, wartungsfrei. Pro Fläche 25× stärker als Geothermie,
   ohne Brennstoff. Die Kosten (≈ 512 Panels) werden einmalig bezahlt, danach dominiert Solar jede andere Quelle.
2. **Wasser und Wind** sind im Original Spielerei (< 2 EU/t) – keine sinnvolle Rolle.
3. **Kohle** hat nur den Generator (4 000 EU/Kohle, 10 EU/t) – keine Ausbaustufe.
4. **Öl** ist stark (145 800 EU/Eimer über Konverter), aber an BC-Infrastruktur und Konverter gebunden – passt zur
   gewünschten Rolle „lagerbar, braucht Infrastruktur“. Treibstoff (1,46 Mio. EU/Eimer) fehlt ohne Raffinerie.
5. **Atom** ist bereits die stärkste Quelle pro Fläche, aber durch Compact Solars entwertet.

## 3. Vorschlag

### 3.1 Solar (Rolle: wartungsfrei, pro Fläche am schwächsten)

- **IC2-Solarpanel bleibt 1:1** (1 EU/t bei Sonne, 1 Block).
- **Compact Solar Arrays werden flache Multiblocks**, die genau die Fläche ihrer Panels belegen. Leitregel:
  *Ein Array ist nur noch eine Bündelung, keine Verdichtung* – es spart Kabel und liefert ein einziges Paket der
  passenden Spannung, verdichtet aber nicht.

| Stufe | Größe (Vorschlag) | Aufbau | Leistung (Tag) | EU/t pro Block | Begründung |
|---|---|---|---:|---:|---|
| LV Solar Array | **3×3** | 8 Solarpanels + 1 LV-Array-Controller (Mitte) | 8 EU/t (ein 8-EU-Paket) | 0,89 | 8 Panels wie im Originalrezept; 2×2 hätte nur Platz für 3 Panels |
| MV Solar Array | **9×9** | 3×3 Raster aus LV-Arrays, Mitte = MV-Controller statt LV-Controller | 64 EU/t | 0,79 | 8 LV-Arrays wie im Originalrezept |
| HV Solar Array | **27×27** | 3×3 Raster aus MV-Arrays, Mitte = HV-Controller | ~~512~~ **1024 EU/t** (2 je Panel, 2 Pakete à 512) | 1,40 | 8 MV-Arrays wie im Originalrezept; Leistung verdoppelt (Nutzer, 2026-10-17) |

- Kosten = Originalrezept (die Arrays bestehen aus denselben Teilen), nur aufgeteilt auf Controller + verbaute Module.
- Bedingungen pro Modul wie Solarpanel (jedes Panel braucht Himmelssicht; abgedeckte Panels liefern nichts).
- Alternative (kompakter, näher an den Richtwerten 2×2/3×3/4×4): spezielle Array-Module mit 2×2 / 3×3 / 4×4 Blöcken
  und Leistung 4 / 9 / 16 EU/t (1 EU/t pro Block). Nachteil: die Original-Paketgrößen 8/64/512 gehen verloren und die
  Rezepte müssten angepasst werden. **Empfehlung: Variante „Fläche der Panels“.**

### 3.2 Wasserkraft (Rolle: zuverlässig, mittelstark, Tag und Nacht)

**IC2-Wassermühle (neu)**, Rezept unverändert:
- Zählt Wasserblöcke im 5×5×3-Bereich um die Mühle (max. 74).
- Ertrag pro Wasserblock: Quelle 0,02 EU/t, **fließendes Wasser 0,04 EU/t**.
- Biom-Faktor: Fluss ×1,5, Ozean ×1,25, sonst ×1,0.
- Deckel: **2 EU/t** pro Mühle (LV), mindestens 4 Blöcke Abstand zwischen Mühlen (sonst teilen sie sich das Wasser).
- Wassereimer/-zellen-Betrieb wie Original (Kompatibilität), aber unwichtig.
→ Gute Mühle im Fluss: 2 EU/t konstant = 4× ein Solarpanel im Tagesmittel, auf ≈ 25 Blöcken Wasserfläche.

**Wasserturbine (neuer Multiblock)**:
- Gehäuse 3×3×3 (Turbinengehäuse-Blöcke), Front mit Turbinenrotor, muss vollständig **in fließendem Wasser** stehen
  (mindestens 16 fließende Wasserblöcke im 7×7-Ring um das Gehäuse).
- Leistung: 0,25 EU/t je fließendem Wasserblock im Ring, Deckel **32 EU/t** (LV-Paket), Fluss ×1,5 → Deckel bleibt 32.
- Kosten-Richtwert: 4 Wassermühlen + 1 Generator + Maschinenblöcke/raff. Eisen (wie IC2-Mittelklasse).
- Rolle: zuverlässige 16–32 EU/t Grundlast, benötigt einen echten Fluss.

### 3.3 Wind (Rolle: höhen- und wetterabhängig, bei Gewitter stark)

Formel bleibt, Faktoren werden erhöht:
- `sub = windStrength · (y − 64 − Hindernisse) / 750 · 2,5`
- Regen ×1,5 (statt 1,2), Gewitter ×3,0 (statt 1,5).
- ~~Bruchgefahr erst ab `sub > 12,5`~~ **Kein Zerfall mehr** (Nutzer, 2026-10-17).
- Puffer/Ausgabe auf 16 EU/t Paket (LV) angehoben.
→ y 128, Wind 15: ≈ 3,2 EU/t; Gewitter ≈ 9,6 EU/t; y 200: ≈ 6,8 EU/t (Gewitter ≈ 20).
Platzbedarf (Hindernis-Würfel) bleibt – Wind braucht Höhe und Abstand.

### 3.4 Kohle (Rolle: Early Game einfach, später effizientes Kraftwerk)

- **IC2-Generator bleibt 1:1** (10 EU/t, 4 000 EU/Kohle).
- **Koksofen (neuer Multiblock 3×3×3, Ziegel-ähnliche Blöcke)**: 1 Kohle → 1 Koks in 900 Ticks, kein Strom;
  Nebenprodukt Asche wird hier nicht erzeugt (nur im Kessel). Holzkohle wird nicht verkokt.
- **Kohlekraftwerk**:
  - *Kessel* (Multiblock 3×3×3): Brennstoff Kohle oder Koks; bei Volllast brennt Kohle 800 Ticks, Koks 1 600 Ticks.
    Temperatur 20 → 1000 °C, +1 °C/Tick beim Heizen, −1 °C/Tick sonst. Dampf erst ab 100 °C:
    `Dampf/t = 20 mB · (T − 100)/900` (max 20 mB/t). Wasserverbrauch 1 mB Wasser je 1 mB Dampf.
    **Überhitzung**: Wasser leer und T > 100 °C → beim nächsten Wasserkontakt (oder T ≥ 1000 °C ohne Wasser)
    Explosion `ExplosionIC2(Stärke 6)`.
    Asche: je 4 verbrannte Kohle (bzw. 2 Koks) 1 Asche in einen Asche-Slot; voller Slot → Kessel stoppt (muss
    abgeführt werden, z. B. per Rohr/Trichter).
  - *Dampfturbine* (Multiblock 3×3×5): 1 mB Dampf → 1 EU; ein Kessel liefert bis **20 EU/t**; bis zu 3 Kessel pro
    Turbine (Deckel 64 EU/t, MV-Paket).
  - Effizienz: 1 Kohle = 800 Ticks × 20 mB = 16 000 mB Dampf → **16 000 EU/Kohle (4× Generator)**;
    Koks (1 Kohle → 1 Koks im Koksofen) 32 000 EU – Verkoken verdoppelt also die Ausbeute; das ist der bewusste Anreiz
    für den Koksofen (Zeitaufwand 900 Ticks je Koks, Platz für den Ofen). Alternativ 1:1 neutral (Koks = 16 000 EU,
    nur höhere Lagerdichte) – siehe §5.
  - Aufheizphase ≈ 900 Ticks bis Volllast (Verlust beim Kaltstart).

### 3.5 Erdöl und Brennstoffe (Rolle: lagerbar, bedarfsgerecht, Infrastruktur)

- BuildCraft-Werte bleiben 1:1 (Öl 3 MJ/t, 60 000 MJ/Eimer; Lava 1 MJ/t).
- Über Power Converters: 145 800 EU/Ölfass – sinnvoll für Mid-/Late-Game, weil Öl gefunden, gepumpt, gelagert und
  über Engines + Konverter verstromt werden muss.
- Treibstoff: BC-Raffinerie, Pumpe und Tank sind im Umfang (Phase 4, 1:1 aus BC 3.4.3). Raffinieren kostet 12 MJ
  pro mB (12 000 MJ pro Eimer), der Treibstoff liefert 600 000 MJ pro Eimer – bleibt 1:1.
- Geothermie bleibt 1:1 (20 000 EU/Lava-Eimer) – lagerbar, mittlere Leistung.

### 3.6 Atomkraft (Rolle: höchste Leistung, Aufwand und Risiko)

- **Unverändert 1:1** (Reaktorlogik ist explizit 1:1 gefordert). Durch die Solar-Änderung wird der Reaktor wieder die
  stärkste Quelle pro Fläche und pro Ressource.

### 3.7 Umwandler

- Electrical Engine und Power Converters bleiben 1:1 (bewusste Verluste und Rundungen erhalten).

## 4. Vergleichstabelle (Vorschlag, gerundet)

| Quelle | Leistung typ. | EU/t pro Block Fläche | EU pro Ressource | Zuverlässigkeit |
|---|---:|---:|---:|---|
| Solarpanel | 1 (Tag) | 1,0 Spitze / 0,5 Mittel | – (wartungsfrei) | niedrig (nur Tag, kein Regen) |
| LV/MV/HV Solar Array | 8 / 64 / 1024 (Tag) | 0,9 / 0,8 / 1,4 Spitze | – | niedrig |
| Wassermühle (neu) | bis 2 | ≈ 0,08 (inkl. Wasserfläche) | – | **hoch** (Tag & Nacht) |
| Wasserturbine | 16–32 | ≈ 0,5–0,65 (inkl. Wasserring) | – | **hoch** |
| Windmühle (neu) | 3–7, Gewitter bis 20 | ≈ 0,04–0,25 (Freiraum 9×9) | – | mittel (Wetter, Wind) |
| IC2-Generator | 10 | 10 | 4 000 / Kohle | hoch, solange befüllt |
| Kohlekraftwerk | 20 je Kessel (bis 64) | ≈ 0,7–1,5 (Kessel+Turbine+Ofen) | 16 000 / Kohle | hoch (Aufheizen, Asche) |
| Geothermie | 20 | 20 | 20 000 / Lava | hoch, solange befüllt |
| Combustion + PC (Öl) | 7,3 je Engine | ≈ 3 (Engine + Konverter) | 145 800 / Öl-Eimer | hoch, lagerbar |
| Combustion + PC (Treibstoff) | 14,6 je Engine | ≈ 6 | 1 458 000 / Eimer | hoch (wenn Raffinerie) |
| Stirling + PC | 2,4 | ≈ 1 | 3 888 / Kohle | mittel (Explosionsgefahr) |
| Kernreaktor | 60–400+ | 10–50+ | ≈ 1–12 Mio. / Zelle | hoch bei korrekter Kühlung, Risiko |

Ergebnis: Solar ist pro Fläche am schwächsten, Wasser die zuverlässige Mittelklasse, Wind wetterabhängig mit
Gewitterspitzen, Kohle hat einen sinnvollen Ausbaupfad, Öl ist die lagerbare Hochenergie, Atom die Spitze.

## 5. Zu bestätigen

1. ~~Solar-Variante~~ **Freigegeben (2026-10-15):** „Fläche der Panels“ (3×3 / 9×9 / 27×27), Mitte rekursiv mit
   Deckel, Controller-Rezept = Trafo 1:1 – Details `spec/11-compact-solars.md` §4.
2. ~~Wassermühlen-/Turbinen-Werte~~ **Freigegeben (2026-10-16)** wie §3.2 – Details `spec/14-kraftwerke.md` §5–6.
3. ~~Wind-Faktoren~~ **Freigegeben (2026-10-16)** wie §3.3 – Details `spec/14-kraftwerke.md` §7.
4. ~~Kohlekraftwerk-Werte~~ **Freigegeben (2026-10-16): Koks 32 000 EU** (Brennwert sonst wie Kohle) – Details `spec/14-kraftwerke.md` §1–4. Vorschlag war: (16 000 EU/Kohle, 20 mB/t Dampf = 20 EU/t pro Kessel, Koks 32 000 EU oder neutral,
   Asche-Regel, Explosion).
