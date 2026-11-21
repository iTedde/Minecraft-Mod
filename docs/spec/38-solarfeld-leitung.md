# Phase 38 – Solarpanels leiten untereinander (freigegeben 2026-10-29)

Nutzer (2026-10-29): „Ist das Solar 9x9 Panel mit einem Kabel schon im Mod?“ – bisher nicht: jedes Panel gab seinen
Strom nur an direkt angrenzende Kabel/Geräte ab, ein 9×9-Feld brauchte 81 Kabel darunter. Entschieden: **Variante 1,
Panels leiten untereinander** („Variante 1, mach das als neue Phase“; Variante 2 Sammelblock verworfen).

Bewusste Abweichung vom Pack (IC2-Solarpanel ist nur Quelle), ergänzt `spec/11` §4.

## 1 Regeln

- Ein Solarpanel ist zusätzlich ein **Leiter**: Es nimmt den Strom angrenzender Solarpanels an (alle sechs Seiten) und
  gibt ihn zusammen mit dem eigenen weiter. Ein Kabel oder Gerät an **irgendeinem** Panel eines zusammenhängenden
  Felds sammelt den Strom aller Panels.
- **Verlustfrei** (Leitungsverlust 0, intern wie die Turbinengehäuse das Mindestmaß 0,0001 je Block), kein Schock,
  kann nicht schmelzen.
- Panels nehmen **nur von Solarpanels** an. Kabel, Generatoren, Speicher usw. können keinen Strom durch ein Feld
  leiten; der Weg führt immer Panel → … → Panel → Kabel/Gerät.
- Feldbonus (9×9, seit Phase 40 ×1,5) unverändert; Panels müssen sich dafür ohnehin lückenlos berühren.
- Stehen mehrere Kabel/Geräte am Feld, verteilt das EU-Netz wie sonst nach Leitungsverlust auf alle Abnehmer.

| Aufbau | vorher | jetzt |
|---|---|---|
| 9×9-Feld, ein Kabel an einer Ecke | nur das Eckpanel liefert (3 EU/t) | ganzes Feld, 243 EU/t |
| Reihe aus 5 Panels, Kabel am Ende | 2 EU/t | 10 EU/t |

Werte seit Phase 40 (2 EU/t, im Feld 3 EU/t); bei Phase 38 waren es 1,2 / 97,2 bzw. 1 / 5 EU/t.

## 2 Umsetzung

`SolarPanelBlockEntity` implementiert `EnergyConductor` (`acceptsEnergyFrom` nur für `SolarPanelBlockEntity`,
Verlust 0, Isolierung/Durchbruch `Integer.MAX_VALUE`, `removeInsulation`/`removeConductor` ohne Wirkung) – wie
`ConductingPartBlockEntity` der Kraftwerke. Wiki-Liste „Gehäuse der Kraftwerke“ nimmt das Panel aus.

GameTests: `panelsConductToOneCable` (5 Panels → Zinnkabel → BatBox, 250 EU in 50 Ticks),
`otherSourcesDoNotRouteThroughPanels` (Generator erreicht die BatBox hinter einem Panel nicht),
`panelInFieldDeliversOneAndAFifth` angepasst (das ganze 9×9-Feld liefert in die eine BatBox, 81 · 60 EU).

## 3 Teilphasen

- 38.1 Spezifikation + Umsetzung + GameTests – erledigt (Client offen)
- 38.2 EMI-Text Solarpanel, Wiki (Solar-Abschnitt, neu erzeugen), `abweichungen.md`, Testplan – erledigt (EMI `solar_field`, Wiki Solar-Regel/Bauanleitung/Fehlersuche, „Alle Änderungen“, Testplan §20)
