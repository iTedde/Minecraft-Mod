# 30 – Elektrischer Leuchtschild

Neuer Inhalt ohne Pack-Vorbild (Nutzer, 2026-10-24): ein **verzauberbarer Schild**, der mit **EU** betrieben wird und
**wie eine Fackel leuchtet** (echtes Licht in der Welt, nicht nur Optik). Freigegeben vom Nutzer am 2026-10-24 (§8);
Entscheidungen dort; abweichend vom Vorschlag blockt der leere Schild nicht (§2).

Werte orientieren sich an vorhandenen IC2-Inhalten, damit der Schild ins Balancing passt: Luminator
(`spec/04` §Luminator: 10 000 EU, 1 EU alle 4 Ticks, Licht 15) und die LV-Elektrowerkzeuge (`spec/05`: 10 000 EU,
Stufe 1, Transfer 100).

## 1 Item

| Eigenschaft | Wert |
|---|---|
| ID | `craft:electric_light_shield` |
| Name | DE „Elektrischer Leuchtschild“, EN „Electric Light Shield“ |
| Speicher | 10 000 EU |
| Stufe | 1 (LV) – lädt in BatBox, Ladegerät, Ladeslots der Maschinen und an Batterie-Rüstung wie andere LV-Werkzeuge |
| Transfer | 100 EU/t |
| Stapel | 1, Rarität common |
| Haltbarkeit | keine (EU statt Haltbarkeit, Ladebalken wie bei allen elektrischen Items) |
| Verzauberbarkeit | 10 (Zaubertisch möglich, s. §5) |
| Kreativ-Tab | IC2-Tab (dort liegen alle Elektrowerkzeuge), leer und voll geladen |

- Umsetzung als `ShieldItem` mit `ElectricItem` (`ic2/item/ElectricItems`: Ladebalken, Tooltip-Ladeanzeige und HUD aus
  Phase 20.2 gelten automatisch).
- Blocken funktioniert in beiden Händen wie beim Vanilla-Schild (`ItemAbilities.SHIELD_BLOCK`), inkl. 5 Ticks
  Anlaufzeit und Axt-Sperre (100 Ticks Abklingzeit).
- Modell/Renderer: Vanilla-`ShieldModel` mit eigener Textur (Metallplatte, Leuchtfläche in der Mitte); kein Banner-
  Muster (Rezept mit Banner entfällt). Textur per `tools/scripts/` (Python + Pillow).

## 2 Energie

| Vorgang | Kosten |
|---|---|
| Leuchten | 1 EU alle 4 Ticks (= 5 EU/s, wie der Luminator) → voll geladen ≈ 33 min Licht |
| Geblockter Treffer | 100 EU je Haltbarkeitspunkt, den ein Vanilla-Schild verloren hätte (`1 + ⌊Schaden⌋`, erst ab 3 Schaden – Vanilla-Regel) |

- Schaden am Schild entsteht nie; statt `hurtAndBreak` wird EU abgezogen (`LivingShieldBlockEvent`).
- **Leer (0 EU):** kein Licht und **kein Blocken** – der Schild lässt sich nicht heben, bis er geladen ist (Nutzer: §8.2 B). Reicht die Ladung für einen Treffer nicht, wird der Treffer noch geblockt und die Ladung auf 0 gesetzt.
- Energie wird nur serverseitig abgezogen; der Client zeigt den synchronisierten Stand.

## 3 Licht

> **Ersetzt durch §3a (Nutzer, 2026-10-24, Phase 30.3).** Der folgende Abschnitt beschreibt die Umsetzung aus 30.1
> (unsichtbarer Lichtblock) und gilt nur noch als Historie; der Block `craft:shield_light` bleibt registriert, damit
> alte Welten ihn laden und er sich selbst entfernt.

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
- Gilt auch für Zombies/Skelette, die den Schild in der Nebenhand halten? → nein, nur Spieler.

## 3a Licht als Lichtkegel (dynamisches Licht, ab 30.3)

Nutzer, 2026-10-24: nur dynamisches Licht, **kegelförmig nach vorne** wie eine Taschenlampe; kein Lichtblock mehr.
Befund vorher: Jade (15.10.6) zeigt den alten Lichtblock nicht an (Strahl mit `ClipContext.Block.OUTLINE`, Umriss leer).

**Grundlage: LambDynamicLights (LDL) 4.8.11+1.21.1** (NeoForge), optional. Craft meldet sich als LDL-Initializer an
(`neoforge.mods.toml`: `[[modproperties.craft."yumi:entrypoints"."lambdynlights:initializer"]]`, Klasse in
`lightshield/client/compat`) und registriert eigene `DynamicLightBehavior`-Objekte. Ohne LDL wird der Eintrag ignoriert;
Craft hat keine harte Abhängigkeit (API nur `compileOnly`). LDL steht schon seit Paket 1.0.2 im CurseForge-Manifest
(CurseForge 393442, Datei 8780227 = 4.8.11+1.21.1; Befund 30.5).

- **Rein clientseitig und optisch:** Blöcke, Mobs und Items im Kegel werden hell gezeichnet, das Licht der Welt ändert
  sich nicht – **Mobs spawnen weiter** (bewusst, Nutzer). Ohne LDL leuchtet nur die Leuchtfläche am Schild.
- **Für alle Spieler:** Jeder Client zeigt die Kegel aller Spieler in Sichtweite, die einen Leuchtschild mit Licht
  an und Ladung > 0 in Haupt- oder Nebenhand halten (Item-Daten werden ohnehin synchronisiert). Nicht im
  Zuschauermodus, nicht für Mobs.

**Form (Taschenlampe):**

| Größe | Wert |
|---|---|
| Spitze | Augenposition des Trägers |
| Achse | Blickrichtung (Gier + Neigung) zum Zeitpunkt der Rechnung (je Tick, LDL baut Chunks ohnehin nur je Tick neu) |
| Öffnung | 30° (halber Winkel 15°); in den äußersten 5° fällt die Helligkeit linear auf 0 |
| Reichweite | 16 Blöcke; bis 4 Blöcke Helligkeit 14, danach linear auf 0 bei 16 |
| Schein um den Träger | Helligkeit 7 am Augenpunkt, −1 je Block (Radius 7), damit die Füße nicht im Dunkeln stehen |
| Ergebnis je Block | Maximum aus Kegel und Schein; LDL mischt es wie üblich mit dem Weltlicht |

- **Wände blockieren:** Ein Block erhält Kegellicht nur, wenn die Sichtlinie vom Augenpunkt zur Blockmitte frei ist
  (`ClipContext.Block.COLLIDER`, Flüssigkeiten durchlässig); der getroffene Block selbst wird beleuchtet (Wandfläche
  hell, dahinter dunkel). Der Schein um den Träger breitet sich vom Augenblock aus nur durch Blöcke ohne Kollision
  aus (Flutfüllung, Wert nach Luftlinie); kollidierende Blöcke werden beschienen, halten ihn aber auf (umgesetzt in
  30.4 statt einzelner Sichtlinien – gleiche Wirkung, weit weniger Rechenaufwand).
- **Rechnung:** nicht in `lightAtPos` je Block, sondern einmal bei jeder Änderung: Strahlenfächer über den Kegel
  (Abstand ≤ 0,5 Block an der Kegelbasis), getroffene und durchlaufene Blöcke mit ihrer Helligkeit in einer Tabelle;
  `lightAtPos` liest nur nach. `getBoundingBox` = Hülle aus Kegel und Schein.
- **Neu berechnen** (`hasChanged`), wenn sich der Augenpunkt um ≥ 0,25 Block oder die Blickrichtung um ≥ 2° ändert,
  das Licht umgeschaltet wird oder Ladung/Halten wechselt; sonst höchstens alle 10 Ticks (Blockänderungen im Kegel).
- Unter Wasser leuchtet der Kegel genauso (Wasser ist durchlässig).

**Energie und Serverseite:** unverändert 1 EU je 4 Ticks, solange Licht an, Ladung > 0 und in der Hand (Server, auch ohne
LDL – die Energie misst den eingeschalteten Zustand, nicht das sichtbare Licht). Der Server setzt keine Blöcke mehr;
`ShieldLights` und das Setzen/Nachführen entfallen. `craft:shield_light` bleibt als Altlast registriert und entfernt
sich wie bisher selbst (≤ 20 Ticks), kein Code setzt ihn mehr.

**Tests:** Kegelrechnung als reine Funktion (`LightCone`: Winkel, Reichweite, Randabfall, Schein) in GameTests; Wand-
Prüfung mit einer gebauten Wand in der Testwelt; Verbrauch weiterhin serverseitig; Altlast-Block entfernt sich.
Der LDL-Teil selbst nur im Client-Test (Testplan 13).

## 4 Ein-/Ausschalten

- **Modus-Taste + Rechtsklick** (IC2-Tastensystem aus Phase 6.2, wie Bergbaulaser/Elektr. Schraubenschlüssel)
  schaltet das Licht um; Meldung „Licht: an/aus“. Rechtsklick ohne Modus-Taste blockt wie gewohnt.
- Zustand als Data Component `craft:light_on` (Standard: an).

## 5 Verzauberungen

Schilde sind in Vanilla nur am Amboss verzauberbar (Haltbarkeit, Reparatur, Fluch des Verschwindens). Weil der
Leuchtschild keine Haltbarkeit hat, wirken die Verzauberungen auf die Energie:

| Verzauberung | Wirkung |
|---|---|
| Haltbarkeit I–III | je Blockpunkt Chance 1/(Stufe+1), dass er EU kostet (Vanilla-Regel `processDurabilityChange` wie bei Werkzeugen); Licht unverändert |
| Reparatur | Erfahrungskugeln laden den Schild: 1 XP = 2 Punkte = 200 EU (Vanilla-Verhältnis × 100 EU/Punkt), solange nicht voll |
| Fluch des Verschwindens | wie Vanilla |

- Zusätzlich am **Zaubertisch** verzauberbar (Verzauberbarkeit 10) – dort nur die Verzauberungen oben (Tag
  `minecraft:enchantable/durability`, dazu `minecraft:enchantable/vanishing` für den Fluch; Reparatur und Fluch
  sind Schatz-Verzauberungen und kommen wie in Vanilla nur über Bücher am Amboss).
- Umsetzung Reparatur: Mixin `ExperienceOrbMixin` vor `ExperienceOrb.repairPlayerItems` (Vanilla repariert nur
  beschädigte Items); ein Schild mit Reparatur in Haupt- oder Nebenhand nimmt die XP zuerst, der Rest geht an Vanilla.

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
- GameTests `LightShieldGameTests`: ab 30.4 Kegelwerte, Kegel vorn/Schein hinten, Wand blockiert, Wasser durchlässig,
  Leuchten nur in der Hand mit Ladung (nicht als Zuschauer), Altlast-Block entfernt sich, Verbrauch 1 EU/4 Ticks, leer
  kein Licht, Blockkosten 100 EU/Punkt, Haltbarkeit/Reparatur, Umschalten, Rezept (bis 30.3: Lichtblock folgt dem
  Spieler, Aufräumen).

## 8 Entscheidungen (Nutzer, 2026-10-24)

Freigegeben: 1 LV-Werte wie §1/§2 · 2 **B** (leer kein Blocken) · 3 Licht 14 · 4 Verzauberungen wie §5 · 5 Rezept wie §6 ·
6 Teilphasen 30.1 + 30.2.

Nachtrag (Nutzer, 2026-10-24): Licht nur dynamisch als Taschenlampen-Kegel über LambDynamicLights (16 Blöcke, 30°,
Schein 7, Wände blockieren), Mobs spawnen weiter, LDL ins CurseForge-Paket – §3a. Teilphasen: 30.3 Spezifikation ·
30.4 Umsetzung (Kegel, LDL-Anbindung, Lichtblock-Code entfernen, Tests) · 30.5 Wiki/EMI-Texte, Paket mit LDL,
Craft 1.0.9 / Paket 1.0.12.

Ursprüngliche Fragen:

1. Werte: 10 000 EU, Stufe 1, 5 EU/s Licht, 100 EU je Blockpunkt – oder anders (z. B. Stufe 2 / 100 000 EU)?
2. Leer: **A** blockt weiter wie ein normaler Schild (ohne Licht) · **B** blockt gar nicht mehr.
3. Lichtlevel 14 (Fackel) oder 15 (Luminator/Glowstone)?
4. Verzauberungen wie §5 (Energie statt Haltbarkeit, zusätzlich Zaubertisch)?
5. Rezept wie §6?
6. Umsetzung in **einer** Teilphase 30.1 (Item, Licht, Taste, Verzauberungen, Tests) + 30.2 (EMI, Wiki, Paket, Version
   Craft 1.0.8 / Paket 1.0.11).
