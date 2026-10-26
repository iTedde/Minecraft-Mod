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

### 3.1 Solar (Rolle: wartungsfrei, teuer, kompakt)

- **IC2-Solarpanel bleibt 1:1** (1 EU/t bei Sonne, 1 Block).
- **Nur noch Solarpanels, Feldbonus ×1,2** (Nutzer, 2026-10-24; ersetzt die 3×3-Felder mit LV/MV/HV-Array-Mitte vom
  2026-10-20). Jedes Panel liefert selbst 1 EU/t; liegt es in einem lückenlosen **9×9-Quadrat** aus Panels auf
  derselben Höhe, 1,2 EU/t. Eine Stufe, kein Deckel. Ein 9×9-Feld liefert 97 EU/t, ein 10×10-Feld 120 EU/t.
- Die Compact-Solars-Arrays sind stillgelegt: kein Rezept, keine Leistung, in der Werkbank zerlegbar in 8 der Stufe
  darunter (`spec/11-compact-solars.md` §4).
- Folge (bewusst): Solar skaliert nur noch linear mit der Fläche (≈ 1,2 EU/t je Block) und ist damit wieder die
  pro Fläche schwache, wartungsfreie Quelle – nur am Tag, ohne Regen. Die Großkraftwerke (§3.8) waren gegen ein
  HV-Feld (1536 EU/t) gerechnet und liegen jetzt deutlich über Solar.
- Vorher (2026-10-20 bis 2026-10-24): 3×3-Felder, LV ×1,2 / MV ×1,4 / HV ×1,5, bis 1536 EU/t mit 8 MV-Arrays.

- Electrical Engine und Power Converters bleiben 1:1 (bewusste Verluste und Rundungen erhalten).

### 3.8 Großkraftwerke (Phase 21, Richtung freigegeben 2026-10-20)

Befund (Nutzer): Solar skaliert über die Array-Stufen exponentiell, alle anderen Quellen nur linear über die Anzahl
mit kleinen Deckeln – ein HV-Feld (1536 EU/t Tag) entspricht ≈ 3 Reaktoren oder ≈ 100 Kohle-Multiblöcken. Solar
bleibt; jede andere Quelle bekommt eine zweite, große Ausbaustufe (neue Blöcke, kleine Anlagen unverändert). Ziel:
eine Großanlage ≈ ⅓–⅔ eines HV-Felds, dafür Tag und Nacht. Vorschlagswerte (endgültig in `spec/23-grosskraftwerke.md`):

- **Hochdruckkessel** 5×5×5 (Stahl), bis 1500 °C, 160 mB/t Dampf; Koks 48 000 EU (statt 32 000); Explosion Stärke 10.
- **Große Dampfturbine** 5×5×9 (Titan-/Wolframstahl-Rotoren), Deckel 512 EU/t (HV), bis 3 Hochdruckkessel.
- **Große Wasserturbine** 5×5×5, Deckel 256 EU/t, ≈ 64 fließende Wasserblöcke nötig.
- **Große Windturbine** Mast + 5×5-Rotor, Rotorstufen Eisen/Stahl/Carbon; y 128 ≈ 64, y 200 ≈ 136, Gewitter bis 384 EU/t (Carbon-Rotor). **Umgesetzt in Phase 21.**
- Reaktor, Öl/Treibstoff und Umwandler bleiben 1:1.

**Ausgleich (Phase 32, freigegeben 2026-10-26, `spec/32-kraftwerke-ausgleich.md`):** gleiche Preis-Leistung aller
Großanlagen (≈ 2–4 Metall-Einheiten je EU/t statt 0,7 / 2,4 / 4,9). Große Windturbine: Faktor 12, Deckel **192 EU/t**
(HV, knapp über MV), 24 Stahl-Masten, 4 Fortg. Maschinengehäuse – absolut am billigsten (≈ 1,7). Große Wasserturbine:
Gehäuse ×6 (≈ 2,8). Große Dampfturbine: Gehäuse halb Raffiniertes Eisen, Rotor 2 Titan + 6 Stahl (≈ 3,8 + Brennstoff).
Wasserturbinen: Fluss ×1,5 auch auf den Deckel (klein 48 EU/t in LV-Paketen, groß 384 EU/t).

### 3.9 Elektrischer Leuchtschild (Phase 30, freigegeben 2026-10-24)

Neuer Inhalt ohne Pack-Vorbild (`spec/30-leuchtschild.md`): LV-Schild mit 10 000 EU (Transfer 100), Licht 14 für
1 EU je 4 Ticks wie der Luminator (≈ 33 min je Ladung), geblockter Treffer 100 EU je Haltbarkeitspunkt eines
Vanilla-Schilds; leer blockt er nicht. Ab 30.4 ist das Licht rein optisch über
LambDynamicLights, seit 30.6 wie eine Fackel in der Hand (Licht 14 ringsum statt Kegel); der Verbrauch bleibt.

## 4. Vergleichstabelle (Vorschlag, gerundet)

| Quelle | Leistung typ. | EU/t pro Block Fläche | EU pro Ressource | Zuverlässigkeit |
|---|---:|---:|---:|---|
| Solarpanel | 1 (Tag) | 1,0 Spitze / 0,5 Mittel | – (wartungsfrei) | niedrig (nur Tag, kein Regen) |
| Solarfeld 9×9 (seit 2026-10-24) | 97 (Tag, 81 Panels) | 1,2 Spitze / 0,6 Mittel | – (wartungsfrei) | niedrig (nur Tag, kein Regen) |
| Wassermühle (neu) | bis 2 | ≈ 0,08 (inkl. Wasserfläche) | – | **hoch** (Tag & Nacht) |
| Wasserturbine | 16–32 | ≈ 0,2–0,65 (inkl. Wasserfläche) | – | **hoch** |
| Windmühle (neu) | 3–7, Gewitter bis 20 | ≈ 0,04–0,25 (Freiraum 9×9) | – | mittel (Wetter, Wind) |
| IC2-Generator | 10 | 10 | 4 000 / Kohle | hoch, solange befüllt |
| Kohlekraftwerk | 20 je Kessel (bis 64) | ≈ 0,7–1,5 (Kessel+Turbine+Ofen) | 16 000 / Kohle | hoch (Aufheizen, Asche) |
| Geothermie | 20 | 20 | 20 000 / Lava | hoch, solange befüllt |
| Combustion + PC (Öl) | 7,3 je Engine | ≈ 3 (Engine + Konverter) | 145 800 / Öl-Eimer | hoch, lagerbar |
| Combustion + PC (Treibstoff) | 14,6 je Engine | ≈ 6 | 1 458 000 / Eimer | hoch (wenn Raffinerie) |
| Stirling + PC | 2,4 | ≈ 1 | 3 888 / Kohle | mittel (Explosionsgefahr) |
| Kernreaktor | 60–400+ | 10–50+ | ≈ 1–12 Mio. / Zelle | hoch bei korrekter Kühlung, Risiko |
| Hochdruckkessel + Große Dampfturbine (Phase 21) | 160 je Kessel (bis 480, Turbine bis 512) | ≈ 2,3–4 (Grundfläche Kessel + Turbine) | 24 000 / Kohle, 48 000 / Koks | hoch (Aufheizen ≈ 70 s, Asche, 160 mB/t Wasser je Kessel) |
| Große Wasserturbine (Phase 21/32) | 64–256, Fluss bis 384 | ≈ 0,2–0,7 (inkl. Wasserfläche 19×19) | – | **hoch** |
| Große Windturbine (Phase 32, Carbon) | y 128 ≈ 38, y 200 ≈ 82, y 319 ≈ 153, Regen/Gewitter bis 192 | ≈ 0,5–1,1 (Freiraum 11×11) | – | mittel (Wetter, Wind) |

Ergebnis: Solar ist wartungsfrei und kompakt, aber teuer und nur am Tag (bis 2026-10-20: pro Fläche am schwächsten), Wasser die zuverlässige Mittelklasse, Wind wetterabhängig mit
Gewitterspitzen, Kohle hat einen sinnvollen Ausbaupfad, Öl ist die lagerbare Hochenergie, Atom die Spitze. Seit Phase 21
erreicht jede Nicht-Solar-Quelle mit ihrer großen Ausbaustufe ⅙–⅓ eines HV-Solarfelds je Anlage, dafür Tag und Nacht.

## 5. Zu bestätigen

1. ~~Solar-Variante~~ **Freigegeben (2026-10-15):** „Fläche der Panels“ (3×3 / 9×9 / 27×27), Mitte rekursiv mit
   Deckel, Controller-Rezept = Trafo 1:1. **Ersetzt (2026-10-20):** 3×3-Felder mit Bonus LV ×1,2 / MV ×1,4 / HV ×1,5,
   Originalrezepte (§3.1) – Details `spec/11-compact-solars.md` §4.
2. ~~Wassermühlen-/Turbinen-Werte~~ **Freigegeben (2026-10-16)** wie §3.2 – Details `spec/14-kraftwerke.md` §5–6.
3. ~~Wind-Faktoren~~ **Freigegeben (2026-10-16)** wie §3.3 – Details `spec/14-kraftwerke.md` §7.
4. ~~Kohlekraftwerk-Werte~~ **Freigegeben (2026-10-16): Koks 32 000 EU** (Brennwert sonst wie Kohle) – Details `spec/14-kraftwerke.md` §1–4. Vorschlag war: (16 000 EU/Kohle, 20 mB/t Dampf = 20 EU/t pro Kessel, Koks 32 000 EU oder neutral,
   Asche-Regel, Explosion).
