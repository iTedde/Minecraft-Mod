# 51 – Leuchtschild als Taschenlampe

Ausbau von Phase 30 (`30-leuchtschild.md`). Nutzer, 2026-11-12: „Leuchtschild viel heller, wie so ne Blendgranate“;
präzisiert 2026-11-13: „Einfach viel heller, wie so eine große anpassbare Taschenlampe“, **kein Blendeffekt**.

Entscheidungen (Nutzer, 2026-11-13):

| Frage | Entscheidung |
|---|---|
| Technik | **Echtes Licht** über unsichtbare Lichtblöcke (Server), ohne LambDynamicLights |
| Einstellbar | **Reichweite + Kegelbreite** in Stufen, mehr kostet mehr EU/t |
| Speicher | **100 000 EU**, dazu höherer Verbrauch |
| Bedienung | **kleines GUI** (Modus-Taste + Schleichen + Rechtsklick); Modus-Taste + Rechtsklick schaltet weiter an/aus |
| Rundum-Schein | **bleibt** (Kegel + Schein) |

**Ersetzt §3b** von Spec 30 (Fackel-Licht über LDL). Die LDL-Anbindung entfällt.

## 1 Item (Änderungen gegenüber Spec 30 §1/§2)

| Eigenschaft | alt | neu |
|---|---|---|
| Speicher | 10 000 EU | **100 000 EU** |
| Stufe / Transfer | 1 (LV) / 100 | unverändert (lädt weiter in BatBox & Co.; voll laden ≈ 50 s am Ladeslot) |
| Leuchten | 1 EU / 4 Ticks | **R × B EU alle 2 Ticks** (R, B = Stufe 1–4 von Reichweite und Breite), also 0,5–8 EU/t |
| Geblockter Treffer | 100 EU je Punkt | unverändert |

Laufzeit mit 100 000 EU: kleinste Einstellung (16 Blöcke, 15°) 0,5 EU/t ≈ 2 h 47 min; Standard (32, 30°) 2 EU/t
≈ 42 min; größte (64, 60°) 8 EU/t ≈ 10 min 25 s.

## 2 Einstellungen

Data Component `craft:flashlight` (Reichweitenstufe, Breitenstufe), je Stufe 1–4; fehlt sie, gilt Stufe 2/2.

| Stufe | 1 | 2 | 3 | 4 |
|---|---|---|---|---|
| Reichweite | 16 | 32 | 48 | 64 Blöcke |
| Kegelbreite (voller Öffnungswinkel) | 15° | 30° | 45° | 60° |

- GUI „Taschenlampe“: zwei Schieberegler (Reichweite, Kegel) mit Rastung auf die Stufen, darunter der Verbrauch in
  EU/t und die Laufzeit mit der aktuellen Ladung. Änderungen gelten sofort (Client → Server-Payload
  `craft:flashlight`; der Server prüft, dass der Spieler einen Leuchtschild in der Hand hält, und begrenzt auf 1–4).
- Öffnen: Modus-Taste + **Schleichen** + Rechtsklick. Modus-Taste + Rechtsklick ohne Schleichen schaltet wie bisher
  an/aus.
- Tooltip: zusätzlich „Reichweite 32 · Kegel 30° · 2 EU/t“.

## 3 Licht

Bedingung wie bisher (`ShieldLights.litShield`): Schild in Haupt- oder Nebenhand, Licht an, Ladung > 0, kein
Zuschauer. Nur Spieler.

**Lichtblock** `craft:shield_light` (wie Spec 30 §3, alt): unsichtbar, keine Kollision, ersetzbar, keine Drops,
wasserbeständig; neu mit Eigenschaft `level` (1–15) für die Lichtstärke. Gesetzt wird er nur in **Luft** oder einen
**Wasser-Quellblock**.

**Kegel:** Vom Augenpunkt aus ein Strahlenfächer in Blickrichtung über den Kegel (Öffnungswinkel aus §2), Strahlabstand
so, dass am Strahlende höchstens ~6 Blöcke zwischen Nachbarstrahlen liegen (Licht 15 reicht 14 Blöcke weit).
Jeder Strahl läuft bis zur Reichweite oder bis zum ersten festen Block (`COLLIDER`, Flüssigkeiten durchlässig).

- Trifft er einen Block, kommt ein Lichtblock in den letzten freien Block **vor** der Trefferfläche → die Fläche wird
  hell. Ohne Treffer kommt ein Lichtblock ans Strahlende (Licht in der Luft, z. B. über Schluchten).
- Helligkeit: **15** bis zur halben Reichweite, danach linear bis **11** am Ende.
- Doppelte Positionen werden zusammengefasst (Maximum). Höchstens **160** Lichtblöcke je Spieler.

**Schein:** Lichtblock Stärke **14** im Augenblock (belegt → Fußblock), wie das alte Schildlicht und eine Fackel.

**Nachführen:** Server, alle **5 Ticks** neu rechnen, außer der Spieler hat sich seit der letzten Rechnung weniger als
0,25 Block bewegt und weniger als 2° gedreht und die Einstellung ist gleich (dann nur alle 20 Ticks wegen
Blockänderungen). Nur der Unterschied wird gesetzt bzw. entfernt (Lichtupdates sparen).

**Aufräumen:** Jeder Lichtblock plant sich alle 20 Ticks einen Tick ein und entfernt sich, wenn er in keiner
aktuellen Lichtliste eines Spielers steht (Liste je Spieler und Dimension). Damit bleibt nach Absturz, Abmelden, Tod,
Dimensionswechsel, Weglegen oder Ausschalten nichts liegen. Beim Abmelden und Serverende entfernt der Server die
Lichter des Spielers sofort.

Folgen von echtem Licht: Monster spawnen im beleuchteten Bereich nicht, andere Spieler sehen das Licht ohne Zusatzmod,
Pflanzen wachsen im Licht. Bei schnellem Drehen folgt der Kegel mit bis zu ¼ s Verzögerung.

## 4 Entfällt

- LambDynamicLights-Initializer `LightShieldDynamicLights` und der `yumi:entrypoints`-Eintrag in `neoforge.mods.toml`
  (LDL bleibt als Paket-Mod für Fackeln usw. im Paket).

## 5 Tests (GameTests)

- Stufen/Verbrauch: R × B EU alle 2 Ticks; Payload begrenzt auf 1–4, nur mit Schild in der Hand.
- Kegel: Wand in 10 Blöcken Entfernung → Lichtblock direkt vor der Wand mit Stärke 15; Wand hinter Reichweite → kein
  Licht an der Wand; Strahl durch Wasser; Schein im Augenblock.
- Aufräumen: Ausschalten / Weglegen → alle Lichtblöcke weg; fremder Lichtblock ohne Liste entfernt sich.

## 6 Teilphasen

51.0 Spezifikation (diese Datei) · 51.1 Umsetzung (Licht, Verbrauch, GUI, Payload, Tests) · 51.2 EMI/Wiki/Testplan/
Doku.
