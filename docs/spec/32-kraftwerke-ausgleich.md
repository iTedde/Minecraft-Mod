# 32 – Ausgleich der Großkraftwerke und Fluss-Bonus der Wasserturbinen (Phase 32)

Nutzer, 2026-10-26: „Windkraft ist zu op. Das Große Windkraftwerk ist nicht teuer genug, um 200+ EU herzustellen.“ –
„Die sollen alle auf einem gleichen Level sein, vom EU/t-Wert. Lass die große Windturbine bei HV, aber nur knapp mehr
als MV, dafür am günstigsten von der Produktion.“ Rückfrage beantwortet: gemeint ist **gleiche Preis-Leistung**
(Kosten je EU/t), die Leistungen bleiben unterschiedlich. Wasserturbine: „Das mit dem fließenden Wasser ist
obligatorisch, das müsste aber trotzdem mehr sein im Flussbiom … sogar über 32 EU“ – entschieden: **Fluss ×1,5 auch auf
den Deckel**. Ergänzt `balancing.md` §3.8 und `spec/23`; Punkte zur Freigabe §5.

## 1 Bewertungsmaßstab

Grobe Stückliste in „Metall-Einheiten“: Barren/Platte ≈ 1 (Eisen, Raffiniertes Eisen, Zinn, Kupfer, Bronze),
Gold 2, Stahl 2, Titan 8, Wolframstahl 12, Diamant 10; Kohle, Gummi, Redstone, Glas werden nicht gezählt. Leistung im
Mittel (Solar: Hälfte der Spitze). Rechnung über die generierten Rezepte (Skript außerhalb des Repos).

| Anlage | Leistung | Kosten | je EU/t |
|---|---|---:|---:|
| Große Windturbine (y 319, Wind 15) | 255 EU/t, Gewitter 384 | 190 | **0,7** |
| Große Wasserturbine | 256 EU/t | 620 | 2,4 |
| Große Dampfturbine + 3 Hochdruckkessel | 480 EU/t + ≈ 24 Kohle/min | 2 360 | 4,9 |
| altes HV-Solar-Array (512 Panels, stillgelegt) | 512 Tag / 256 Mittel | 9 700 | 38 |
| Solarfeld 9×9 | 97 Tag / 48,5 Mittel | 1 500 | 31 |

Ziel: alle drei Großanlagen bei **≈ 2–3 je EU/t**; Dampf darf am oberen Rand liegen (einzige regelbare Anlage, unabhängig
von Gelände und Wetter, Brennstoff kommt dazu). Solar bleibt bewusst teuer (wartungsfrei, keine Änderung).

## 2 Große Windturbine

- **Leistung:** `P = min(12 · sub · Faktor, 192)` EU/t (vorher Faktor 20, Deckel 384). Stufe bleibt **HV** (Pakete bis
  192, also mehr als MV 128). Beispiele Wind 15, frei, Carbon: y 128 → 38, y 200 → 82, **y 319 → 153**, Regen ab y ≈ 270
  und Gewitter → 192 (Deckel). Puffer 2 · 192.
- **Struktur:** **24** Windturbinenmasten unter der Gondel (vorher 8).
- **Rezepte:**
  - Windturbinenmast ×**4**: `S S/SSS/S S` – S **Stahlplatte** (vorher ×8 aus Raffiniertem Eisen).
  - Große Windturbine: `WAW/AHA/WAW` – W Windmühle, A Fortgeschrittenes Maschinengehäuse (**4** statt 2), H
    HV-Transformator (die 2 Generatoren entfallen).
  - Stahl-Windrotor: `PPP/PRP/PPP` – P **Stahlplatte** (8), R Eisen-Windrotor (vorher 4 Platten + 4 Stahlbarren).
  - Eisen- und Carbon-Windrotor unverändert.
- **Ergebnis:** ≈ 275 Einheiten für ≈ 150–190 EU/t → **≈ 1,7** – absolut die billigste Großanlage und je EU/t etwas
  unter Wasser (Ausgleich fürs Wetter: ohne Regen kein Bonus, volle Leistung nur ganz oben).

## 3 Wasserturbinen (klein und groß)

- **Fluss-Biom ×1,5 gilt jetzt auch für den Deckel:** klein **48 EU/t** im Fluss (sonst 32), groß **384 EU/t** im Fluss
  (sonst 256). Volllast im Fluss: klein ab 128, groß ab 256 fließenden Blöcken (wie außerhalb; der Fluss macht jeden
  Block 1,5-mal so viel wert und hebt die Obergrenze).
- Kleine Turbine: Pakete bleiben **LV (32)** – bei mehr als 32 EU/t werden pro Tick zwei Pakete abgegeben (32 + Rest),
  damit LV-Kabel und -Maschinen nicht explodieren. Große Turbine: Pakete bis 384 (HV, unverändert Stufe 3).
- Nur fließendes Wasser zählt (unverändert, Nutzer: „obligatorisch“).
- **Rezept:** Großes Wasserturbinengehäuse ×**6** statt ×8 (`SRS/RMR/SRS` unverändert).
- **Ergebnis:** ≈ 725 Einheiten → **≈ 2,8** (im Fluss 1,9).

## 4 Große Dampfturbine

- Leistung unverändert (512-Deckel, 3 Hochdruckkessel = 480 EU/t).
- **Rezepte:**
  - Großes Turbinengehäuse ×8: `PIP/IAI/PIP` – P Stahlplatte, I **Raffinierte-Eisen-Platte**, A Fortgeschrittenes
    Maschinengehäuse (vorher 8 Stahlplatten).
  - Großer Turbinenrotor: `STS/SRS/STS` – T **Titanplatte (2)**, S **Stahlplatte (6)**, R Turbinenrotor (vorher 4 Titan + 4
    Wolframstahl).
- **Ergebnis:** ≈ 1 800 Einheiten → **≈ 3,8** (vorher 4,9) plus Brennstoff.

## 5 Punkte zur Freigabe

1. Wind: Faktor 12, Deckel 192 (HV), 24 Masten aus Stahlplatten (×4), Turbine mit 4 Fortgeschrittenen
   Maschinengehäusen statt 2 + 2 Generatoren, Stahl-Windrotor aus 8 Stahlplatten.
2. Wasser: Fluss ×1,5 auch auf den Deckel (48 / 384), kleine Turbine in LV-Paketen; großes Gehäuse ×6.
3. Dampf: Gehäuse halb Stahl, halb Raffiniertes Eisen; Großer Rotor 2 Titan + 6 Stahl statt Titan + Wolframstahl.
4. Bestehende Anlagen: Windturbinen mit nur 8 Masten gelten nach dem Update als unvollständig (Masten nachbauen);
   bereits gebaute Gehäuse/Rotoren bleiben gültig.

## 6 Teilpakete

| Teil | Inhalt |
|---|---|
| 32.0 | diese Spezifikation |
| 32.1 | Umsetzung (Werte, Struktur, Rezepte, LV-Pakete), GameTests, `spec/14`/`spec/23`/`balancing.md` nachziehen |
| 32.2 | Wiki (Seiten, Rechner, Steckbriefe), EMI-Texte, Testplan, Craft-Version + Paket |
