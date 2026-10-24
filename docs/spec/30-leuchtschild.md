# 30 – Elektrischer Leuchtschild

Neuer Inhalt ohne Pack-Vorbild (Nutzer, 2026-10-24): ein **verzauberbarer Schild**, der mit **EU** betrieben wird und
**wie eine Fackel leuchtet** (echtes Licht in der Welt, nicht nur Optik). Alle Werte unten sind ein **Vorschlag** und
gelten erst nach Freigabe durch den Nutzer; offene Punkte stehen in §8.

Werte orientieren sich an vorhandenen IC2-Inhalten, damit der Schild ins Balancing passt: Luminator
(`spec/04` §Luminator: 10 000 EU, 1 EU alle 4 Ticks, Licht 15) und die LV-Elektrowerkzeuge (`spec/05`: 10 000 EU,
Stufe 1, Transfer 100).

## 1 Item

| Eigenschaft | Wert (Vorschlag) |
|---|---|
| ID | `craft:electric_light_shield` |
| Name | DE „Elektrischer Leuchtschild“, EN „Electric Light Shield“ |
| Speicher | 10 000 EU |
| Stufe | 1 (LV) – lädt in BatBox, Ladegerät, Ladeslots der Maschinen und an Batterie-Rüstung wie andere LV-Werkzeuge |
| Transfer | 100 EU/t |
| Stapel | 1, Rarität common |
| Haltbarkeit | keine (EU statt Haltbarkeit, Ladebalken wie bei allen elektrischen Items) |
| Verzauberbarkeit | 10 (Zaubertisch möglich, s. §5) |
| Kreativ-Tab | Craft „Werkzeuge“, zusätzlich voll geladen wie die anderen Elektro-Items |

- Umsetzung als `ShieldItem` mit `ElectricItem` (`ic2/item/ElectricItems`: Ladebalken, Tooltip-Ladeanzeige und HUD aus
  Phase 20.2 gelten automatisch).
- Blocken funktioniert in beiden Händen wie beim Vanilla-Schild (`ItemAbilities.SHIELD_BLOCK`), inkl. 5 Ticks
  Anlaufzeit und Axt-Sperre (100 Ticks Abklingzeit).
- Modell/Renderer: Vanilla-`ShieldModel` mit eigener Textur (Metallplatte, Leuchtfläche in der Mitte); kein Banner-
  Muster (Rezept mit Banner entfällt). Textur per `tools/scripts/` (Python + Pillow).

## 2 Energie

| Vorgang | Kosten (Vorschlag) |
|---|---|
| Leuchten | 1 EU alle 4 Ticks (= 5 EU/s, wie der Luminator) → voll geladen ≈ 33 min Licht |
| Geblockter Treffer | 100 EU je Haltbarkeitspunkt, den ein Vanilla-Schild verloren hätte (`1 + ⌊Schaden⌋`, erst ab 3 Schaden – Vanilla-Regel) |

- Schaden am Schild entsteht nie; statt `hurtAndBreak` wird EU abgezogen (`LivingShieldBlockEvent`).
- **Leer (0 EU):** kein Licht; der Schild blockt weiter wie ein normaler Schild, aber ohne EU-Abzug (Vorschlag A in §8.2).
- Energie wird nur serverseitig abgezogen; der Client zeigt den synchronisierten Stand.

## 3 Licht

„Echtes Licht“ heißt: Blöcke und Mobs in der Umgebung werden wirklich hell (auch für andere Spieler, Mob-Spawning wird
unterdrückt). Vanilla 1.21.1 hat kein bewegliches Licht; deshalb ein unsichtbarer Lichtblock, der dem Träger folgt.

- Eigener Block `craft:shield_light`: unsichtbar (`RenderShape.INVISIBLE`), keine Kollision, kein Umriss, ersetzbar
  (`replaceable`), keine Drops, nicht in Kreativ/EMI, Lichtlevel **14** (wie eine Fackel), wasserbeständig
  (Eigenschaft `waterlogged`, damit unter Wasser geleuchtet wird).
- Bedingung: Schild in Haupt- **oder** Nebenhand, Licht eingeschaltet (§4), Ladung > 0, Spieler nicht im Zuschauermodus.
- Position: Block der Augenhöhe des Spielers; ist der belegt, der Fußblock; ist auch der belegt (z. B. im Schnee),
  kein Licht in diesem Tick. Nur Luft oder Wasser-Quellblock wird ersetzt.
- Nachführen: Server prüft jeden Tick; ändert sich die Position, wird der alte Lichtblock entfernt (bei Wasser
  zurück zu Wasser) und der neue gesetzt.
- Aufräumen: Lichtblock plant sich selbst alle 20 Ticks einen Tick ein und entfernt sich, wenn kein berechtigter Träger
  ihn gesetzt hat (Positionsliste je Spieler) – damit bleiben nach Absturz, Abmelden, Tod, Dimensionswechsel oder
  Weglegen keine Lichtblöcke liegen.
- Gilt auch für Zombies/Skelette, die den Schild in der Nebenhand halten? → nein, nur Spieler (Vorschlag).

## 4 Ein-/Ausschalten

- **Modus-Taste + Rechtsklick** (IC2-Tastensystem aus Phase 6.2, wie Bergbaulaser/Elektr. Schraubenschlüssel)
  schaltet das Licht um; Meldung „Licht: an/aus“. Rechtsklick ohne Modus-Taste blockt wie gewohnt.
- Zustand als Data Component `craft:light_on` (Standard: an).

## 5 Verzauberungen

Schilde sind in Vanilla nur am Amboss verzauberbar (Haltbarkeit, Reparatur, Fluch des Verschwindens). Weil der
Leuchtschild keine Haltbarkeit hat, wirken die Verzauberungen auf die Energie (Vorschlag):

| Verzauberung | Wirkung |
|---|---|
| Haltbarkeit I–III | Chance 1/(Stufe+1), die EU-Kosten eines geblockten Treffers zu zahlen (wie Haltbarkeit bei Werkzeugen); Licht unverändert |
| Reparatur | Erfahrungskugeln laden den Schild: 1 XP = 2 Punkte = 200 EU (Vanilla-Verhältnis × 100 EU/Punkt), solange nicht voll |
| Fluch des Verschwindens | wie Vanilla |

- Zusätzlich am **Zaubertisch** verzauberbar (Verzauberbarkeit 10) – dort nur die Verzauberungen oben (Tag
  `minecraft:enchantable/durability` + eigener Tag, damit der Tisch sie anbietet).

## 6 Rezept

Geformt, liefert den Schild **leer**:

```
ALA
ASA
 R
```

`A` Fortgeschrittene Legierung (`craft:advanced_alloy`), `L` Luminator (`craft:luminator`), `S` Schild (Vanilla),
`R` RE-Batterie (`craft:re_battery`, beliebige Ladung).

## 7 Einbindung

- Paket: `dev.craft.lightshield` (neuer Inhalt, wie `…powerplants`/`…mining` ohne Pack-Vorbild).
- EMI: Rezept automatisch; Info-Seite (Energie, Licht, Taste, Verzauberungen).
- Herkunft (`spec/24`): keine neuen Rohstoffe.
- Wiki: Seite „Elektrischer Leuchtschild“ in der Gruppe „Neue Inhalte“, Energie-Steckbrief.
- `abweichungen.md`: nicht nötig (kein Original), Eintrag nur in `balancing.md` §3 als neuer Inhalt.
- GameTests `LightShieldGameTests`: Licht folgt dem Spieler (Augen-/Fußblock, Wasser), Aufräumen nach Weglegen/
  Abmelden, Verbrauch 1 EU/4 Ticks, leer kein Licht, Blockkosten 100 EU/Punkt, Haltbarkeit/Reparatur, Umschalten,
  Rezept.

## 8 Zur Freigabe

1. Werte: 10 000 EU, Stufe 1, 5 EU/s Licht, 100 EU je Blockpunkt – oder anders (z. B. Stufe 2 / 100 000 EU)?
2. Leer: **A** blockt weiter wie ein normaler Schild (ohne Licht) · **B** blockt gar nicht mehr.
3. Lichtlevel 14 (Fackel) oder 15 (Luminator/Glowstone)?
4. Verzauberungen wie §5 (Energie statt Haltbarkeit, zusätzlich Zaubertisch)?
5. Rezept wie §6?
6. Umsetzung in **einer** Teilphase 30.1 (Item, Licht, Taste, Verzauberungen, Tests) + 30.2 (EMI, Wiki, Paket, Version
   Craft 1.0.8 / Paket 1.0.11).
