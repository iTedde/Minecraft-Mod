# 05 – Items, Werkzeuge, Rüstung, Sonstiges

Quelle: `ic2.core.IC2` (Registrierung), `ic2.core.item.**`, `ic2.core.block.crop.*`, `ic2.core.block.Entity*`.
Englische Originalnamen: `ic2/core/init/Localization.java` (310 Einträge) bzw. `reference/config/IC2.lang` – dient
später als Basis für die Lang-Datagen.

Haltbarkeit = `maxDamage`. Elektrische Items: `charge` im NBT, siehe `01-eu-system.md` §5.

## 1. Materialien und einfache Items

| Item (Key) | Bemerkung |
|---|---|
| Harz (`itemHarz`) | Rechtsklick oben auf Block setzt Harzschicht (`blockHarz`); Rechtsklick auf Kolben (passende Seite) → Klebekolben |
| Gummi (`itemRubber`, OreDict `itemRubber`) | |
| Uranerz-Brocken (`itemOreUran`, `dropUranium`) | Drop von Uranerz |
| Stäube: Bronze, Ton, Kohle, Kupfer, Gold, Eisen, Silber, Zinn, kleiner Eisenstaub, hydrierter Kohlestaub | OreDict `dust*` |
| Barren: raffiniertes Eisen, Kupfer, Zinn, Bronze, Mischmetall (`itemIngotAlloy`), Uran | OreDict `ingot*` (Mischmetall ohne OreDict) |
| Elektronischer Schaltkreis, Fortgeschrittener Schaltkreis (uncommon) | |
| Fortgeschrittene Legierung, Kohlefaser, Kohlenetz, Kohleplatte | |
| UU-Materie (`itemMatter`, rare), Iridiumerz (rare), Iridiumplatte (rare), Dichte Kupferplatte | |
| Kohleball, komprimierter Kohleball, Kohleklumpen, Industriediamant (kein Creative-Tab) | Industriediamant → formlos 1 Diamant |
| Schrott, Schrottbox | Brennwerte siehe Generator |
| Hydrierter Kohleklumpen, Pflanzenball, komprimierter Pflanzenball | |
| Bauschaum-Pellet, Grinpulver, Kühlmittel (`itemCoolant`, Flüssigkeit) | |
| Münze (`itemCoin`, kein Creative-Tab) | |
| Zellen: leer, Lava, hydr. Kohle, Bio, Kohlebrennstoff, Biobrennstoff, Wasser, elektrolysiertes Wasser, Luft | siehe §6 |
| Upgrade-Module: Overclocker (0), Transformer (1), Energiespeicher (2) | Stack 64 (GT: 4, siehe Rezepte/offene Punkte) |
| Terraformer-Blaupausen (leer + 6 Sorten), Stack 1 | siehe `04-maschinen.md` |
| Farbroller (`itemToolPainter`) + 16 Farb-Varianten | §4 |
| Dynamit, Klebedynamit, Fernzünder | §7 |
| Gefüllte/leere Zinndose, (leerer/gefüllter) Brennstoffkanister | §6 |
| Crop-Items: Crop-Stäbe (`blockCrop`-Item), Saatbeutel, Cropnalyzer, Dünger, Hydrierzelle, Weed-EX, Terra-Warze, Kaffee (Bohnen, Pulver, Tasse leer/gefüllt), Hopfen, Fass, Bier-/Rum-Tasse | §9 |


### 1.1 Schrottbox öffnen (`ItemScrapbox`, im Pack von GregTech ersetzt)

Rechtsklick: −1 Schrottbox (nicht im Kreativmodus), ein zufälliger Drop wird wie ein vom Spieler geworfenes Item
fallen gelassen. Werfer (`BehaviorScrapboxDispense`): öffnet eine Box und schießt den Drop mit Geschwindigkeit 6
heraus. Auswahl gewichtet: `rand · Summe` gegen die kumulierten Chancen.

**Pack-Liste:** IC2 füllt die Liste in PreInit (`ItemScrapbox.init`), GregTech **leert sie** in seiner PreInit
(`GT_Mod.java:431`), trägt Schrott (Chance 200) ein und fügt in PostInit seine Liste hinzu (`GT_Mod.java:2231–2311`,
alle Einträge in `RecipesAdvancedConfig.cfg` aktiv). Die IC2-Liste (inkl. „suddenlyHoes“) ist damit wirkungslos.
Summe aller Pack-Chancen 320,78; Schrott ≈ 62 %. Die Sonderregeln in `GT_ModHandler.getRandomScrapboxDrop`
(Minecart und Goldhelm neu würfeln) gelten nur für den GT-Scrapboxinator, nicht für den Rechtsklick.

| Chance | Drops |
|---:|---|
| 200 | Schrott |
| 9,5 | Holzhacke, Stock |
| 9 | Verrottetes Fleisch |
| 5 | Erde, Papier |
| 4 | GT-Netherrack-Staub, GT-Feuersteinstaub |
| 3,8 | Holzschliff (`dustWood`) |
| 3 | Grasblock, Kies |
| 2,7 | Single-Use-Batterie |
| 2,5 | Holzkohlestaub |
| 2 | Holzaxt/-schwert/-schaufel/-spitzhacke, Schild (→ Eichenschild), Netherrack, verdorbene Zinndose (Meta 1), leere Zelle, isoliertes Kupferkabel, Salpeterstaub, GT-Staub Lazurit/Pyrit/Calcit/Sodalith |
| 1,8 | GT Basic Circuit Board |
| 1,5 | Obsidian-, Schwefelstaub |
| 1,2 | GT Advanced Circuit Parts, Zinn-, Kupferstaub |
| 1 | Seelensand, Knochen, gefüllte Zinndose, Wasserzelle, Leder, Feder, Eisen-, Goldstaub |
| 0,9 | Redstone |
| 0,8 | GT Machine Parts, Gummi, Glowstone-Staub, Kohlestaub |
| 0,7 | Pflanzenball |
| 0,6 | Schleimball |
| 0,5 | Kürbis, Apfel, Brot, Staub Silber/Elektrum/Bauxit/Aluminium/Blei/Nickel/Zink/Messing/Stahl, Roter/Gelber Granat |
| 0,4 | gebratenes Schwein/Rind/Huhn, doppelt isoliertes Goldkabel, GT Advanced Circuit Board |
| 0,2 | Siliziumzelle (GT-Zelle 7), GT Elite Circuit Board |
| 0,1 | Kuchen |
| 0,05 | Olivin, Rubin, Saphir, Grüner Saphir, Smaragd, Diamant |
| 0,03 | GT-Staub Platin/Wolfram/Chrom/Titan/Magnesium/Endstein |

Umsetzung: Loot-Tabelle `craft:gameplay/scrap_box`, Gewicht = Chance × 100. Einträge erscheinen, sobald es das Item
gibt (GT-Items mit Phase 6b, Zinndosen mit der Canning Machine); bis dahin Summe 280 (Schrott ≈ 71 %).

Harzschicht (`blockHarz`): siehe §10; Platzieren mit Harz (Rechtsklick auf die Oberseite eines undurchsichtigen
Blocks, Luft darüber), Harz auf die Vorderseite eines eingefahrenen Kolbens → Klebekolben (gleiche Ausrichtung).

## 2. Batterien und Energiespeicher-Items

| Item | Kapazität (EU) | Transfer/t | Tier | Batterie für Maschinen | Bemerkung |
|---|---:|---:|---:|:-:|---|
| RE-Batterie (leer `itemBatREDischarged` / geladen `itemBatRE`) | 10 000 | 100 | 1 | ja | leer stapelbar bis 16; geladene Variante = eigene ID. Rechtsklick (geladen): entlädt sich mit bis zu `2·100` EU in elektrische Nicht-Batterie-Items der Hotbar (Tier des Ziels) |
| Energiekristall (`itemBatCrystal`) | 100 000 | 250 | 2 | ja | |
| Lapotronkristall (`itemBatLamaCrystal`, uncommon) | 1 000 000 | 600 | 3 | ja | |
| Single-Use-Batterie (`itemBatSU`) | 1000 (einmalig) | – | 1 | – | Rechtsklick: lädt Hotbar-Items mit insgesamt 1000 EU (Tier 1, ohne Limit), wird verbraucht wenn etwas übertragen wurde; im Batterie-Slot von Maschinen +1000 EU |

Batterien haben keine Reparatur, Haltbarkeitsbalken 27 Stufen; Icon in 5 Ladestufen.

## 3. Elektrische Werkzeuge

| Werkzeug | Kapazität | Transfer | Tier | EU/Aktion | Effizienz | Weiteres |
|---|---:|---:|---:|---:|---:|---|
| Bergbaubohrer (`itemToolDrill`) | 10 000 | 100 | 1 | 50 | 8,0 | Material Eisen (Harvest 2). Abbaubar: alles Material Stein/Eisen + Liste (Erde, Gras, Sand, Kies, Schnee, Ton, Netherrack, Glowstone, Seelensand, Amboss …) |
| Diamantbohrer (`itemToolDDrill`) | 10 000 | 100 | 1 | 80 | 16,0 | Material Diamant (Harvest 3), zusätzlich Obsidian |
| Kettensäge (`itemToolChainsaw`) | 10 000 | 100 | 1 | 100 | 12,0 | Material Holz + Bretter, Bücherregal, Holz, Truhe, Blätter, Spinnennetz, Crop, Gummiblätter. Schlag: 100 EU → 9 Schaden zusätzlich. Schert Schafe/Blätter (IShearable) für 100 EU |
| Elektrische Hacke (`itemToolHoe`) | 10 000 | 100 | 1 | 50 | 16,0 | pflügt wie Hacke (Erde/Gras) |
| Elektrischer Baumzapfer (`itemTreetapElectric`) | 10 000 | 100 | 1 | 50 | – | wie Baumzapfer, ohne Haltbarkeit |
| Elektrischer Schraubenschlüssel (`itemToolWrenchElectric`) | 12 000 | 250 | 1 | 50 pro Haltbarkeitspunkt | – | Modus-Taste + Rechtsklick: Lossless-Modus (Abbau immer verlustfrei, kostet 200 Punkte = 10 000 EU) |
| Bergbaulaser (`itemToolMiningLaser`, uncommon) | 200 000 | 120 | 2 | s. u. | – | 7 Modi |
| Nanosäbel (`itemNanoSaber`, uncommon) | 40 000 | 128 | 2 | s. u. | – | |
| OD-Scanner (`itemScanner`) | 10 000 | 50 | 1 | 50 | – | |
| OV-Scanner (`itemScannerAdv`) | 10 000 | 50 | 2 | 250 | – | |

Allgemein elektrische Werkzeuge: Ohne Energie Abbaugeschwindigkeit 1,0. Pro abgebautem Block mit Härte ≠ 0 werden die
EU/Aktion abgezogen (`ElectricItem.use`, inkl. Nachladen aus Rüstung). Nicht verzauberbar, nicht reparierbar,
Nahkampfschaden 1 (Basis).

**Bergbaulaser** (Modus-Taste + Rechtsklick wechselt; Nachricht „Laser Mode: …“):

| Modus | EU | Reichweite | Kraft | Blöcke | Besonderheit |
|---|---:|---|---:|---|---|
| Mining | 1250 | ∞ | 5 | ∞ | |
| Low-Focus | 100 | 4 | 5 | 1 | |
| Long-Range | 5000 | ∞ | 20 | ∞ | |
| Horizontal | 3000 | ∞ | 5 | ∞ | nur Rechtsklick auf Block; horizontal auf Blockhöhe; „aiming angle too steep“ bei > 1,5 Höhenunterschied |
| Super-Heat | 2500 | ∞ | 8 | ∞ | schmilzt Drops (Ofenrezept), Holz verbrennt ohne Drop; setzt Ergebnis-Block statt Drop, wenn es ein Block ist |
| Scatter | 10 000 | ∞ | 12 | ∞ | 25 Strahlen (5×5, je ±20° Yaw/Pitch) |
| Explosive | 5000 | ∞ | 12 | ∞ | explodiert beim Aufprall: `ExplosionIC2(5, drop 0,85, dmg 0,55)` |

Laserprojektil: Geschwindigkeit 1 Block/Tick; pro Block `kraft −= (Explosionsresistenz + 0,3)/10`; Luft −0,5/Tick;
Drop-Chance 0,9; 10 % Feuer bei brennbarem Material; nicht abbaubar: Ziegel, Obsidian, Lava, Wasser, Bedrock, Glas;
stirbt in Wasser. Treffer auf Entity: Schaden `(int)kraft`, Feuer `kraft` (Super-Heat: doppelt) Sekunden.

**Nanosäbel**: Rechtsklick schaltet an (benötigt ≥ 16 EU, Sound) / aus. Aktiv: Schaden 20, nur Spinnennetz
abbaubar (Abbaugeschwindigkeit siehe §3.1). Verbrauch (`drain` = n·8 EU): pro Treffer 5×8 = 40 EU (zusätzlich gegen Spieler mit
Nano-/Quantenrüstung: je Teil 2×8, und das Rüstungsteil verliert 4800 EU (Nano) bzw. 30 000 EU (Quantum); leere
Rüstungsteile werden zerstört). Abbau: 80 EU. Leerlauf (aktiv): alle 16 Ticks 64×8 = 512 EU in der Hotbar, außerhalb
der Hotbar alle 64 Ticks 16×8 = 128 EU. Bei Energiemangel geht er aus. Inaktiv Schaden 4.
Original-Eigenheit: Der Verbrauch beim Treffen gilt nur, wenn der Angreifer kein Spieler ist **oder PvP aus ist**.

**Scanner**: Rechtsklick scannt die Säule unter dem Spieler bis y = 1 im Quadrat ±2 (OD) bzw. ±4 (OV) und meldet
`1000 · Treffer / gescannteBlöcke` („Ore density“ = Anzahl wertvoller Blöcke; „Ore value“ = Summe der Erzwerte).
Erzwerte: Tabelle in `07-weltgenerierung.md` §4. Im Miner liefert der
Scanner pro Ebene einen Layer-Scan (OD: 50 EU, Radius 2; OV: 250 EU, Radius 4).

### 3.1 Details aus dem Original (nachgetragen für Phase 6.2)

**Gemeinsam (`ItemElectricTool`)**: Stack 1, Haltbarkeitsbalken 27 Stufen, Creative-Tab zeigt geladen + leer.
Werkzeugmaterial Eisen (Bohrer, Kettensäge, Hacke, Baumzapfer, Laser, Säbel), Diamantbohrer Diamant.
- Abbaugeschwindigkeit (Forge ruft die Variante mit Metadaten): `canUse(Aktionskosten)` falsch → 1,0; sonst
  Effizienz, wenn das Werkzeug laut Forge-Werkzeugklasse effektiv ist **oder** der Block in der Abbauliste steht,
  sonst 1,0. Werkzeugklassen: Bohrer `pickaxe` 2, Diamantbohrer `pickaxe` 3, Kettensäge `axe` 2.
- `onBlockDestroyed`: Blockhärte ≠ 0 → `use(Aktionskosten)` (Nicht-Spieler: `discharge` ohne Transferlimit).
- `hitEntity` ohne Kosten; Nahkampfschaden 1; nicht verzauberbar/reparierbar, nicht im Amboss kombinierbar.
- Abbaubar (`canHarvestBlock`) = Abbauliste (+ Materialregeln unten).

**Bergbaubohrer** Abbauliste: Bruchstein, Stein-(Doppel-)Stufe, Bruchsteintreppe, Stein, Sandstein(-treppe), bemooster
Bruchstein, Eisenerz/-block, Kohleerz, Golderz/-block, Diamanterz/-block, Eis, Netherrack, Lapiserz/-block,
Redstone-Erz (beide), Ziegel(-treppe), Glowstone, Gras, Erde, Myzel, Sand, Kies, Schneedecke, Schneeblock, Ton,
Ackerboden, Steinziegel(-treppe), Netherziegel(-treppe), Seelensand, Amboss; zusätzlich **jedes** Material Stein oder
Eisen. Diamantbohrer: + Obsidian. Sound beim Abbauen (Client, jede 4. Geschwindigkeitsabfrage): `drill`/`drillOne`/
`drillTwo`/`drillThree` – **diese Sounds existieren im Pack nicht** (nicht im IC2-Jar) → effektiv lautlos.

**Kettensäge** Abbauliste: Bretter, Bücherregal, Holz, Truhe, Blätter, Spinnennetz, Crop, Gummiblätter + jedes
Material Holz. Schlag (Angreifer Spieler): `use(100)` erfolgreich → zusätzlich `attackEntityFrom(Spieler, 9)` direkt
nach dem normalen Treffer (wegen Unverwundbarkeitsfenster effektiv Gesamtschaden 9). `onBlockStartBreak` (Server):
Block ist `IShearable` und schergbar und `use(100)` → Scher-Drops (Glück aus Verzauberung) als Items mit
Aufhebe-Verzögerung 10, Statistik „Block abgebaut“; danach normaler Abbau (der dann nochmals 100 EU kostet, wenn
Härte ≠ 0; Blätter haben Härte 0,2). Entity-Interaktion (Rechtsklick, Server): `IShearable`-Entity (Schaf, Mooshroom)
→ `use(100)` **vor** der Schergbarkeitsprüfung (Originalfehler: kostet auch bei geschorenem Schaf), Drops mit
Zufallsimpuls. Client-Sound: beim Ausrüsten Loop `Tools/Chainsaw/ChainsawIdle.ogg`, beim Weglegen
`ChainsawStop.ogg` (Hit-Sounds `ChainsawUseOne/Two` über `IHitSoundOverride` – im Pack ohne Aufrufer, also stumm).

**Elektrische Hacke**: Abbauliste Erde, Gras, Myzel (Effizienz 16). `onBlockStartBreak` kostet bei **jedem** Block
50 EU (`use`, Ergebnis ignoriert), dazu `onBlockDestroyed` 50 EU bei Härte ≠ 0. Rechtsklick auf Block: Spieler darf
den Block bearbeiten → `use(50)` (**vor** der Prüfung, ob pflügbar – kostet auch auf Stein) → `UseHoeEvent` →
Gras (nur wenn Seite ≠ unten und Luft darüber) oder Erde (immer) wird Ackerboden mit Ackerboden-Schrittsound.

**Elektrischer Baumzapfer**: nur auf Gummiholz: `use(50)`, dann `ItemTreetap.attemptExtract` wie der Baumzapfer
(ohne Haltbarkeit; Kosten auch, wenn nichts gezapft wird).

**Elektrischer Schraubenschlüssel** (erbt alle Schraubenschlüssel-Funktionen): Haltbarkeit ↔ EU: `canTakeDamage(n)`
= `discharge(50·n, ohne Limit, simuliert) == 50·n`; `damage(n)` = `use(50·n)`. Drehen 50 EU, Abbau 500 EU.
Lossless (NBT `losslessMode`): wenn die Wrench-Drop-Rate < 1,0 ist, wird nach der 500-EU-Prüfung zusätzlich
`canTakeDamage(200)` geprüft – fehlt die Energie: Meldung „Not enough energy for lossless wrench operation“, kein
Abbau; sonst 10 000 EU und immer der Original-Drop. Modus-Taste + Rechtsklick (auch auf einen Block, dann ohne
Drehen/Abbau) schaltet um: „Lossless wrench mode enabled/disabled“. Kapazität 12 000, Transfer 250, Tier 1.
Schraubenschlüssel allgemein, Alt-Taste: Seite = `(facing + 1) % 6`, mit Schleichen `(facing + 5) % 6`
(ohne Alt, Schleichen: Gegenseite der geklickten Seite).

**Bergbaulaser** (Rarität uncommon, NBT `laserSetting`): Rechtsklick (Server): Modus-Taste → Modus `(m+1) % 7` +
Meldung; sonst `use(Kosten[m])` mit Kosten `1250/100/5000/0/2500/10000/5000`, Erfolg → Projektil(e) + Sound-Event
(alle Clients, an der Hand): Mining/Horizontal/Super-Heat `MiningLaser.ogg`, Low-Focus `MiningLaserLowFocus.ogg`,
Long-Range `MiningLaserLongRange.ogg`, Scatter `MiningLaserScatter.ogg`, Explosive `MiningLaserExplosive.ogg`.
Modus Horizontal im Rechtsklick: Kosten 0, kein Schuss. Rechtsklick auf Block (`onItemUseFirst`, vor dem normalen
Rechtsklick, der danach trotzdem ausgeführt wird): nur Modus Horizontal und ohne Modus-Taste:
`|Augenhöhe − 0,1 − (y + 0,5)| < 1,5` → `use(3000)` → Projektil mit Pitch 0 auf Höhe `y + 0,5`, Kraft 5; sonst
Meldung „Mining laser aiming angle too steep“.
Scatter: 25 Projektile mit Yaw `yaw + 20·x`, Pitch `pitch + 20·y` (x, y ∈ −2…2).

**Laserprojektil** (`EntityMiningLaser`, Größe 0,8, keine Schwerkraft, nicht gespeichert, Tracking 160 / alle 5 Ticks):
- Start: `x = Spieler.x − cos(yaw)·0,16`, `z = Spieler.z − sin(yaw)·0,16`, `y = Augenhöhe − 0,1`; Richtung aus
  Yaw/Pitch, Geschwindigkeit 1.
- Pro Tick (Server), solange `range ≥ 1`, `power > 0`, `blockBreaks > 0` (sonst: explosiv → Explosion; entfernen):
  Raytrace alter → neuer Position (Flüssigkeiten ignoriert, Blöcke ohne Kollisionsbox ignoriert); Entities im
  erweiterten Bereich (Box um 0,3 vergrößert, kollidierbar, Besitzer erst ab 5 Ticks) – nächster Treffer gewinnt.
  - Treffer + explosiv: `ExplosionIC2(Position, 5, Drop 0,85, Schaden 0,55)`, entfernen.
  - Entity: `d = (int)power`; `d > 0` → Feuer `d` (Super-Heat `2·d`) Sekunden, Schaden `d` Typ „arrow“ (Projektil,
    Besitzer); Projektil entfernen.
  - Block nicht abbaubar (Ziegel, Obsidian, Lava, Wasser, Bedrock, Glas) → entfernen. Sonst
    `power −= (Explosionsresistenz + 0,3) / 10`; wenn danach `power ≥ 0`:
    Material TNT → `onBlockDestroyedByPlayer(meta 1)` (zündet) und weiter wie normal (Block wird entfernt und kann
    gedroppt werden). Super-Heat: Material Holz → entfernen ohne Drop; sonst je Drop das Ofenergebnis: erstes
    Ergebnis, das ein (anderer) Block ist → Block an der Stelle setzen (Metadaten = Item-Schaden), kein Drop; andere
    Ergebnisse als Item-Entity (Aufhebe-Verzögerung 10); jedes Ofenergebnis setzt `power = 0` (der Strahl endet nach
    diesem Block). Ohne Ofenergebnis normaler Drop.
    Entfernen: Drop mit Chance 0,9 (Glück 0), Block → Luft, 10 % Feuer, wenn Material brennbar.
    `blockBreaks −= 1`.
  - Kein Treffer: `power −= 0,5`.
  - Bewegen um die Geschwindigkeit, `range −= 1`; im Wasser → entfernen (ohne Explosion).
- Darstellung: Pfeil-Geometrie (`RenderArrow`) mit Textur `sprites/laser.png`.

**Nanosäbel**: zwei Item-IDs (aus `itemNanoSaberOff`, an `itemNanoSaber`, nur „aus“ im Creative-Tab), Rarität
uncommon, `isFull3D`, Aktionskosten 10. Abbaugeschwindigkeit: Forge nutzt die Metadaten-Variante des Basiswerkzeugs
(die 4,0-Überschreibung ist wirkungslos) → mit ≥ 10 EU Spinnennetz 6,0 (Eisen-Effizienz), sonst 1,0 – an und aus.
Abbau: `onBlockStartBreak` (aktiv) 80 EU, plus 10 EU bei Härte ≠ 0. `drain(n)` = `use(8·n)`, misslingt → Säbel aus.
Leerlauf: globaler Zähler (+1 je Welt-Tick, effektiv alle 16 Ticks) – Hotbar `drain(64)`, sonst alle 64 `drain(16)`.
Einschalten: `canUse(16)` → an + Sound `nanosabrePower`. Treffer-/Abbau-Sounds `nanosabreSwing*`, `nanosabrePower` –
**im Pack nicht vorhanden** → lautlos (die Dateien `Tools/Nanosabre/*.ogg` werden nicht verwendet). Aktiv:
Textur flackert zufällig zwischen zwei Icons.

**Scanner** (`ItemScanner` = elektrisches Item, kein Werkzeug): Rechtsklick (Client und Server) → `use(50)` (OD) bzw.
`use(250)` (OV), Erfolg → Sound `Tools/ODScanner.ogg` an der Hand, Scan ab der Blockposition des Spielers
(abgerundet) abwärts bis y = 1 (`y > 0`), Meldung „SCAN RESULT: Ore density in this area is n“ bzw. „SCAN RESULT: Ore
value in this area is n“. Layer-Scan für den Miner: `use(50|250, ohne Spieler)` → Radius 2|4, sonst 0.

**Texturindizes** (`item_0.png`): Bohrer 48, Diamantbohrer 49, Kettensäge 50, Laser 70, OD-Scanner 59, OV-Scanner 60,
Säbel aus 77 / an 78 (+79 flackernd), el. Schraubenschlüssel 94, el. Hacke 154, el. Baumzapfer 165.

## 3a. Tastensystem (`ic2.core.util.Keyboard`)

Client-Tasten (Standard): **ALT Key** = linke Alt-Taste, **Boost Key** = linke Strg-Taste, **Mode Switch Key** = M,
**Side Inventory Key** = C. Jeden Client-Tick wird der Zustand als Bitmaske gebildet – Bit 0 Alt, 1 Boost, 2 Vorwärts
(Vanilla-Taste), 3 Modus, 4 Springen (Vanilla-Taste), 5 Seiteninventar – und bei Änderung an den Server gesendet
(und lokal übernommen). Der Server speichert den Zustand je Spieler (unbekannt = alles „nicht gedrückt“) und
vergisst ihn beim Ausloggen. Schleichen = Vanilla-Schleichzustand. Nutzer: Schraubenschlüssel (Alt), el.
Schraubenschlüssel, Bergbaulaser, Farbroller (Modus), Jetpack (Vorwärts/Springen/Modus), Quantenrüstung
(Boost/Springen), Gerüst (Vorwärts).

## 4. Nicht-elektrische Werkzeuge

| Werkzeug | Haltbarkeit | Details |
|---|---:|---|
| Bronze-Spitzhacke/-Axt/-Schaufel/-Schwert/-Hacke | wie Eisen (250) | **Effektiv Eisen-Werkzeugmaterial** (`EnumToolMaterial.IRON`: Harvest 2, Effizienz 6,0, Schaden wie Eisen, Schwert 6), Verzauberbarkeit 13 (Spitzhacke/Axt/Schaufel/Schwert), Reparatur mit Bronzebarren. Das registrierte Material `IC2_BRONZE (2, 350, 6.0, 2, 13)` wird nicht genutzt (Feld wird nur verdeckt). | **Entfällt (Nutzerentscheidung 2026-10-15).**
| Baumzapfer (`itemTreetap`) | 16 | Rechtsklick auf Gummiholz mit nasser Harzstelle (Meta 2–5, passende Seite): 1–3 Harz, Stelle wird trocken (Meta +6). Trockene Stelle: 20 % Chance → wird zu Meta 1 (verbraucht), 20 % Chance → Harz (Originalfehler: es werden **2** Harz ausgeworfen). Fass: zapft Fass an. −1 Haltbarkeit pro Nutzung auf Gummiholz |
| Schraubenschlüssel (`itemToolWrench`) | 160 | Drehen: −1; Abbau: −10. Rechtsklick dreht Facing auf die geklickte Seite (Schleichen: Gegenseite; Alt: zyklisch). Abbau mit Wrench-Drop-Rate (siehe Maschinen). Terraformer: Blaupause auswerfen. Nuke-Entity: entschärfen (−1). Server-Log beim Abbau |
| Isolierungsschneider (`itemToolCutter`) | 512 | siehe `01-eu-system.md` §3 |
| Bauschaumsprüher (`itemFoamSprayer`) | 1602 (gradual) | Füllung über Crafting mit Pellets (−100 Damage je Pellet), Rechtsklick sprüht bis zu 13 Blöcke Schaum (Flood-Fill in Blickrichtung) bzw. ersetzt Gerüste; −100 pro Sprühstoß; zieht zuerst aus getragenem CF-Pack |
| Farbroller (`itemToolPainter`) | 32 je Farbe | Färbt Kabel (siehe EU) und Wolle/Schafe. Leer → wird zum farblosen Roller (oder automatisch mit Farbstoff aus dem Inventar nachgefüllt, Modus-Taste + Rechtsklick schaltet „autoRefill“) |
| EU-Meter (`itemToolMeter`) | – | Zweimal auf dasselbe Energie-Tile klicken: „Measured power [EU/t]: x in y out z gain (avg. over n ticks)“ |
| Frequenzsender (`itemFreq`) | – | Teleporter verknüpfen (s. Maschinen); Rechtsklick in die Luft: entkoppeln |
| Werkzeugkasten (`itemToolbox`) | – | Tragbares Inventar für „boxable“ Items (Kabel, Werkzeuge, Batterien …) |
| Cropnalyzer | – | Inventar mit Batterie-Slot; Scannt Saatbeutel: Kosten nach Scanlevel 10/90/900/9000 EU |
| Fernzünder (`itemRemote`) | – | Verknüpft Dynamit-Blöcke (NBT-Liste), Rechtsklick zündet alle |
| Debug-Item | – | nur Entwickler |

### 4.2 Bauschaum-Werkzeuge (nachgetragen für Phase 12.2)

- **Bauschaum-Pellet** (`itemPartPellet`, „CF Pellet“, Sprite `item_0.png` 44): Compressor Bauschaum → Pellet.
- **Bauschaumsprüher** (`ItemSprayer`, „CF Sprayer“, Sprite 45, `ItemGradual` mit Haltbarkeit 1602, nicht
  reparierbar, nicht in den Werkzeugkasten): Rezept liefert ihn leer (Schaden 1601); Füllen über `RecipeGradual`
  (Sprüher + beliebig viele Pellets, je Pellet −100, mindestens 0). Rechtsklick (nur Server): **zuerst** wird, falls
  ein CF-Pack in der Brust steckt, 1 Pellet daraus genommen (auch wenn danach nichts gesprüht wird); ohne Pack-Pellet
  braucht der Sprüher Schaden ≤ 1501. Geklickter Block = **Holzgerüst** → Gerüstmodus an genau diesem Block, sonst
  Start im Nachbarblock an der geklickten Seite. Kosten je erfolgreichem Stoß +100 Schaden (auch im Kreativmodus),
  nichts bei Pack-Pellet.
- **Sprühen** (`sprayFoam`, Masse 13): Startblock muss passen (Schaummodus: Luft, Feuer, Flüssigkeit oder Kabel;
  Gerüstmodus: Holzgerüst), sonst Fehlschlag. Breitensuche über eine Prüfliste (jede Position nur einmal); jede
  passende Position kommt in die Ergebnisliste, kostet 1 Masse und hängt ihre sechs Nachbarn in **zufälliger**
  Reihenfolge an (16 Zufallstausche von unten/oben/N/S/W/O). Im Schaummodus nur Richtungen, die vom Spieler weg
  zeigen (`calculateDirectionsFromPlayer`: bei Nick −65…65° wird je nach Gier [ohne Normalisierung negativer Werte]
  N (300–60°), O (30–150°), S (120–240°) bzw. W (210–330°) gesperrt; Nick ≤ −40° sperrt unten, ≥ 40° oben); im
  Gerüstmodus alle sechs. Danach: Holzgerüst → droppt wie abgebaut und wird Schaum; Kabel → nasser Kabelschaum
  (auch ausgehärteter Schaum wird wieder nass); sonst Schaumblock. Eisengerüste werden nie ersetzt.
- **CF-Pack** (`ItemArmorCFPack`, „CF Backpack“, Sprite 46, Rüstungsbild des BatPacks, Haltbarkeit 260): Schaden =
  verbrauchte Pellets, Rezept liefert ihn leer (259). `getCFPellet`: Schaden < 259 → +1. Utility-Rüstung ohne
  Schutz; Forge-`isRepairable` = ja (zwei Packs im Raster kombinieren). Canner-Modus 3 siehe `04-maschinen.md`.

### 4.1 Details aus dem Original (nachgetragen für Phase 12.1)

- **Isolierungsschneider** (Sprite `item_0.png` 92): Rechtsklick auf Kabel = Isolierung hinzufügen (Übergänge
  `01-eu-system.md` §3), dann 1 Gummi aus dem Inventar verbrauchen (auch im Kreativmodus); fehlt Gummi, wird der
  Typwechsel zurückgenommen. Schaden 1. Linksklick (`onBlockClicked` des Kabels, Abbau läuft normal weiter) =
  Isolierung entfernen: 1 Gummi mit Zufallsversatz 0,15–0,85 und Aufhebeverzögerung 10, Schaden 3, Klang
  `Tools/InsulationCutters.ogg`. Schaden: `damage + n`; **erst über** 512 (nicht bei 512) zerbricht der Schneider.
- **Farbroller** (leer: Sprite 91, `ItemIC2`, Stack 64; gefärbt: Sprites 128 + Farbe, Stack 1, Haltbarkeit 32).
  Farbe = Farbstoff-Meta (0 schwarz, 1 rot, 2 grün, 3 braun, 4 blau, 5 lila, 6 türkis, 7 hellgrau, 8 grau, 9 rosa,
  10 hellgrün, 11 gelb, 12 hellblau, 13 magenta, 14 orange, 15 weiß); Tooltip = Farbstoffname. Rechtsklick auf
  `IPaintableBlock` (Kabel; Bauschaumwand ab 12.2) mit Erfolg oder auf Wolle anderer Farbe → färben, abnutzen,
  Klang `Tools/Painter.ogg`. Rechtsklick auf ein Schaf anderer Farbe → färben, abnutzen (kein Klang).
  Abnutzen: Ist der Schaden schon ≥ 31, wird bei `autoRefill` der **letzte** Hauptinventar-Slot mit passendem
  Farbstoff (`dye<Farbe>`) gesucht, 1 Farbstoff verbraucht und der Schaden auf 0 gesetzt, sonst wird der Roller zum
  leeren Farbroller; andernfalls Schaden +1 (also 32 Anstriche). Modus-Taste + Rechtsklick in die Luft schaltet
  `autoRefill` („Painter automatic refill mode enabled/disabled“).
- **EU-Meter** (Sprite 90, Stack 1): `onItemUseFirst` auf einem Energie-Tile (Quelle, Leiter oder Senke), nur
  serverseitig. Anderes Tile als zuletzt (nur x/y/z verglichen) → „Starting new measurement“ und Position merken;
  dasselbe Tile → Zeitraum = Spielzeit − letzte Messzeit (mindestens 1), `in` = Δ`getTotalEnergySunken`/Zeitraum,
  `out` = Δ`getTotalEnergyEmitted`/Zeitraum, `gain` = in − out, Format `0.##`. In beiden Fällen werden die
  Summen und die Zeit neu gespeichert (jede weitere Messung bezieht sich auf die vorige).
- **Werkzeugkasten** (Sprite 162 gefüllt, 163 leer, Stack 1; Schaden 0 = leer, 1 = gefüllt). Rechtsklick in die
  Luft (Server): leer → **packen**: Hotbar-Slots 0–8 außer dem Kasten selbst, die „boxable“ sind, wandern in
  `box0…`; nur wenn mindestens eines gepackt wurde, wird der Kasten gefüllt. Gefüllt → **auspacken**: Inhalt
  der Reihe nach in freie Hotbar-Slots **0–7** (Originaleigenheit: Slot 8 wird nie belegt), Rest wird an der
  Spielerposition fallen gelassen; Kasten wird leer. Gefüllt zeigt der Kasten den Namen des ersten Inhalts.
  Boxable: `IBoxable` entscheidet (ja: Kabel, Dynamit, leere RE-Batterie, SU-Batterie, Dünger, Hydrierzelle; nein:
  Werkzeugkasten, Brennstoffkanister, alle `ItemGradual` wie Bauschaumsprüher und Reaktor-Zellen), sonst alles mit
  maximaler Stapelgröße ≤ 1 sowie Gerüst und Bergbaurohr. Gepackt werden höchstens 8 Einträge (`box0`–`box7`).
- **Münze** (Sprite 101): ohne Funktion, kein Creative-Tab.

## 5. Rüstung

### 5.1 Klassische Rüstung

| Set | Material | Schutz (Helm/Brust/Hose/Stiefel) | Haltbarkeitsfaktor | Verzauberbarkeit | Reparatur |
|---|---|---|---:|---:|---|
| Bronze | `IC2_BRONZE` | 3 / 8 / 6 / 3 | 15 (wie Eisen: 165/240/225/195) | 9 | Bronzebarren – **entfällt (Nutzerentscheidung 2026-10-15)** |
| Kompositweste (nur Brust) | `IC2_ALLOY` | 4 / 9 / 7 / 4 → Brust 9 | 50 (Brust 800) | 12 | Fortg. Legierung |

Beide sind `IMetalArmor` (Magnetzaun).

### 5.2 Elektrische Rüstung (`ItemArmorElectric`, Forge `ISpecialArmor`)

Absorption: `ratio = basis[slot] · faktor`, Basis Helm 0,15 / Brust 0,40 / Hose 0,30 / Stiefel 0,15.
Max. absorbierbarer Schaden = `25 · ladung / EU_pro_Schaden` (Forge-Einheit ×25), abgezogene Energie =
`absorbierter Schaden · EU_pro_Schaden`. Kein Schutz bei `isUnblockable`-Schaden. Anzeige der Rüstungspunkte nur,
wenn ≥ EU_pro_Schaden geladen ist.

| Set | Kapazität | Transfer | Tier | Faktor | EU pro Schaden | Gesamtabsorption |
|---|---:|---:|---:|---:|---:|---:|
| Nano | 100 000 | 160 | 2 | 0,9 | 800 | 90 % |
| Quantum | 1 000 000 | 1000 | 3 | 1,0 (Brust 1,1) | 900 | 104 % → voller Schutz |

**Fallschaden** (Stiefel): Nano: bei Fallschaden < 8 wird er ganz abgefangen (Event abgebrochen, 800 EU pro Punkt),
sonst `ISpecialArmor`-Priorität 10 mit Ratio 0,875. Quantum: jeder Fallschaden wird abgefangen, wenn Energie reicht
(900 EU pro Punkt), sonst Ratio 1,0.

**Quantum-Spezialfunktionen** (pro Rüstungs-Tick):
- Helm: Luft < 100 → +200 Luft für 1000 EU. Hunger → isst gefüllte Zinndose aus dem Inventar (1000 EU).
  Entfernt Vergiftung (10 000 EU), Strahlung (10 000) und Wither (25 000), jeweils × (Stufe+1).
- Brust: löscht Feuer (`extinguish`).
- Hose: Sprint (Config `enableQuantumSpeedOnSprint=true`) bzw. Boost-Taste, am Boden mit Bewegung > 0,1 oder im
  Wasser: Beschleunigung 0,22 (Wasser 0,1, mit Springen +0,1 aufwärts), alle 10 Ticks 1000 EU.
- Stiefel: Sprungkraft: Boost + Sprung → `motionY += charge·0,3`, bei voller Ladung zusätzlich horizontale
  Geschwindigkeit ×3,5; `charge ×= 0,75` pro Tick; Aufladen am Boden für 1000 EU.

Nano- und Quantum-Teile sind `IMetalArmor`. Nano-Helm hat **keine** Nachtsicht in 1.115.

### 5.3 Utility-Rüstung (`ItemArmorUtility`, Diamant-Rendermaterial, 0 Rüstungspunkte, nicht verzauberbar)

| Item | Slot | Werte / Effekt |
|---|---|---|
| Hazmat-Helm/-Anzug/-Hose (Haltbarkeit 64) und Gummistiefel (64) | alle | Vollständiges Set: absorbiert Feuer, Lava, Ersticken in Wand, Brennen, Elektrizität, Strahlung vollständig (Lava/Feuer gibt Feuerresistenz 3 s); löscht Brand, in Lava Feuerresistenz. Helm: bei Luft ≤ 100 wird eine Luftzelle verbraucht (+150 Luft, leere Zelle zurück). Gummistiefel: Fallschaden < 8 kostet `(fall+1)/2` Haltbarkeit statt Schaden. Sonst Haltbarkeitsverlust 2× Schaden. Reparierbar. Hazmat schützt vor Tesla-Spule und Nuke-Strahlung. |
| Jetpack (Treibstoff, Haltbarkeit 18 002 = Treibstoff) | Brust | Treibstoff = `maxDamage − 2 − damage − 1`. Sprung-Taste: Schub; Modus+Sprung: Schwebemodus. Verbrauch 9/Tick (Schweben 6). Leistung 1,0; ab ≤ 20 % Treibstoff linear schwächer. Max. Höhe = Welthöhe, ab 25 Blöcke darunter schwächer. Vorwärts-Schub 0,15 (Schweben 0,5). Betankung im Canner |
| Elektrisches Jetpack | Brust | 30 000 EU, Transfer 60, Tier 1; Verbrauch 7/Tick (Schweben 4); Leistung 0,7, Abfall ab 5 %; max. Höhe `Welthöhe/1,28`; Vorwärts +0,15; im Schweben mit Sprung leichtes Steigen (0,1) |
| BatPack | Brust | 60 000 EU, Transfer 100, Tier 1, **liefert Energie** (lädt gehaltene Werkzeuge über `chargeFromArmor`), kein Schutz |
| LapPack (uncommon) | Brust | 300 000 EU, Transfer 250, Tier 2, liefert Energie |
| CF-Pack (Haltbarkeit 260) | Brust | Pellet-Vorrat für den Schaumsprüher (1 Damage je Sprühstoß); Füllen im Canner |
| Solarhelm | Helm | bei Sonne (wie Solarpanel, Position über Kopf) +1 EU/Tick in das elektrische Brust-Item |
| Static Boots | Stiefel | alle 5 gelaufenen Blöcke (Luftlinie, nicht reitend/schwimmend) `min(3, Strecke/5)` EU ins Brust-Item; `IMetalArmor` |
| Nachtsichtbrille | Helm | elektrisch 20 000 EU, Transfer 200, Tier 1; 1 EU/Tick: nachts Nachtsicht (250 Ticks), tagsüber bei Himmelslicht > 12 Blindheit `100 + (licht−13)·50` Ticks. **Kein IC2-Rezept** in 1.115 |

`chargeFromArmor` gilt nur für Rüstungsteile mit „liefert Energie“, also **BatPack und LapPack**. Nano und Quantum liefern keine Energie an Werkzeuge.

### 5.4 Details aus dem Original (nachgetragen für Phase 6.3)

**Forge-Rüstungsrechnung** (`ISpecialArmor.ArmorProperties.ApplyArmor`, Forge 6.6.2.534) – gilt für Spieler; alle
vier Rüstungsslots (Reihenfolge Stiefel, Hose, Brust, Helm = Slot 0–3) liefern je ein `ArmorProperties(Priorität,
Ratio, AbsorbMax)`:
- `ISpecialArmor`-Teile: `getProperties(spieler, stack, quelle, schaden, slot)` (auch bei `isUnblockable`-Schaden).
- Normale Rüstung nur bei blockbarem Schaden: `(0, rüstungspunkte/25, maxHaltbarkeit + 1 − damage)`.
- Rechnung in ×25-Einheiten: `d = schaden·25`. Liste absteigend nach Priorität sortieren, bei gleicher Priorität nach
  `AbsorbMax·100/Ratio` aufsteigend (Ratio 0 → 0; Vergleich `(int)(links − rechts)`). `StandardizeList` (s. u.)
  normiert die Ratios. Dann je Eintrag: Prioritätswechsel → `d −= d·summe`, `summe = 0`; `summe += ratio`;
  `absorb = d·ratio`; bei `absorb > 0` wird das Teil mit `max(1, (int)(absorb/25))` beschädigt
  (`ISpecialArmor.damageArmor` bzw. normaler Itemschaden; Stapel 0 → Slot leer). Am Ende `d −= d·summe`;
  Ergebnis `(int)((d + übertrag)/25)`, neuer Übertrag `(int)d % 25` (Feld des Entity).
- `StandardizeList(liste, d)`: je Prioritätsgruppe (`start`, `pStart`, `total`): Summe > 1 → jede Ratio durch
  `total` teilen; überschreitet `neueRatio·d` das `AbsorbMax`, wird die Ratio `AbsorbMax/d`, `total` neu ab `pStart`
  bis zu diesem Eintrag summiert, `start` dahinter gesetzt und die Gruppe ab dort erneut durchlaufen. Summe ≤ 1 → nur
  jede Ratio auf `AbsorbMax/d` begrenzen. Nach jeder Gruppe `d −= d·total`; ist `d ≤ 0`, bekommen alle folgenden
  Einträge Ratio 0. (Wortgetreu: Code in Forge `ISpecialArmor`.)
- Danach Vanilla: Trank-/Verzauberungsrechnung, Erschöpfung, Lebenspunkte.

**Elektrische Rüstung** (`ItemArmorElectric`): Haltbarkeit 27 (Ladeanzeige), Stapel 1, nicht verzauberbar, nicht
reparierbar; Creative-Tab geladen + leer. `getProperties`: unblockbar → `(0, 0, 0)`, sonst
`(0, basis·faktor, 25·ladung/EU_pro_Schaden)` (EU_pro_Schaden 0 → AbsorbMax 0). `damageArmor`: entlädt
`schaden·EU_pro_Schaden` (ohne Transferlimit). Rüstungsanzeige `round(20·basis·faktor)`, wenn Ladung ≥ EU_pro_Schaden,
sonst 0. BatPack/LapPack: Faktor 0, EU_pro_Schaden 0, liefern Energie. Raritäten: Nano und LapPack „uncommon“,
Quantum „rare“.

**Fallschaden** (nur das Stiefelteil): `LivingFallEvent` mit `fall = (int)distanz − 3`. Nano: `fall ≥ 8` → nichts;
sonst wenn `800·fall ≤ ladung`: entladen, Event abbrechen. Quantum: ohne Obergrenze, `900·fall`. Gelingt das nicht,
gilt `getProperties` für Fallschaden: Priorität 10, Ratio Nano `schaden < 8 ? 1,0 : 0,875`, Quantum 1,0, AbsorbMax
wie oben. Gummistiefel siehe Hazmat.

**Quantum-Tick** (jedes Teil, beide Seiten): Helm: Luft < 100 und ≥ 1000 EU → Luft +200, 1000 EU (sonst bei Luft ≤ 0
Erfolg „starveWithQHelmet“). Hunger (`needFood`) und ≥ 1000 EU → erste gefüllte Zinndose im Inventar essen (Nahrung
+ `onEaten`, Stapel −1), 1000 EU. Tränke: Gift 10 000, Strahlung 10 000, Wither 25 000, je `× (Stufe+1)`, nur wenn
die Energie reicht. Brust: `extinguish`. Hose: ≥ 1000 EU und (am Boden mit `|motionX| + |motionZ| > 0,1` oder im
Wasser) und Sprint (Config `enableQuantumSpeedOnSprint=true` im Pack) → Zähler je Spieler, alle 10 Ticks 1000 EU;
`moveFlying(0, 1, 0,22)`, im Wasser 0,1 und beim Springen `motionY += 0,1`. Stiefel: Sprungladung je Spieler
(Start 1,0): am Boden mit Ladung < 1 und ≥ 1000 EU → 1,0 für 1000 EU. Bei `motionY ≥ 0`, Ladung > 0, nicht im Wasser:
Sprung + Boost → bei Ladung 1,0 `motionX/Z ×3,5`; `motionY += ladung·0,3`; `ladung ×= 0,75`; sonst Ladung < 1 → 0.

**Nanosäbel gegen Rüstung**: trifft der aktive Säbel einen Spieler, wird jedes Rüstungsteil, das `ItemArmorNanoSuit`
ist, um 4800 EU entladen (Säbel-Tier 2, ohne Transferlimit); kann es danach keine 1 EU mehr liefern, wird das Teil
**gelöscht**; je Teil Säbel −2 Einheiten. **Originalfehler:** Quantum erbt nicht von `ItemArmorNanoSuit` – Quantum
wird nie entladen (der 30 000-Zweig ist toter Code).

**Utility-Rüstung** (`ItemArmorUtility`): `getProperties` `(0, 0, 0)`, Anzeige 0, `damageArmor` nichts, nicht
reparierbar (außer Hazmat/CF-Pack), nicht verzauberbar.

**Hazmat** (Haltbarkeit 64, Anzeige 1 je Teil, reparierbar): absorbiert Feuer (`inFire`), Brennen (`onFire`), Lava,
Ersticken (`inWall`), Elektrizität, Strahlung. `getProperties`: **Helm** bei absorbierter Quelle und vollständigem
Set (alle vier Teile `ItemArmorHazmat`, Gummistiefel zählen) → `(10, 1,0, MAX)`, bei Feuer/Lava zusätzlich
Feuerresistenz 60 Ticks Stufe 1. **Stiefel** bei Fallschaden `(10, schaden < 8 ? 1,0 : 0,875, (rest+1)·2·25)`.
Sonst `(0, 1,0, (rest+1)/2·25)` (Ganzzahldivision, `rest = maxDamage − damage`) – auch für Teile ohne vollständiges
Set und für jeden anderen Schaden. `damageArmor`: nichts bei absorbierter Quelle und vollem Set, sonst `2·schaden`,
Stiefel bei Fall `(schaden+1)/2`. Fall-Event (Stiefel): `fall < 8` und `0 ≤ (fall+1)/2 ≤ rest` → Stiefel beschädigen,
Event abbrechen. Helm-Tick: brennt der Spieler mit vollem Set → in Lava (Block an Fußhöhe +0,02 ist Lava und unter
deren Oberfläche) Feuerresistenz 20 Ticks (ambient), dann löschen. Luft ≤ 100 und Luftzelle im Inventar → eine
Luftzelle weg, leere Zelle ins Inventar (geht verloren, wenn voll), Luft +150.

**Jetpack** (Treibstoff, Brust): Haltbarkeit 18 002, Creative-Item Damage 1. `ladung = max(0, maxDamage − 2 −
damage − 1)`, `maxLadung = maxDamage − 2`. `use(n)`: `neu = max(0, ladung − n)`, `damage = 1 + maxDamage − neu`
(**Originalfehler:** dadurch sinkt die Ladung je Aufruf um `n + 4`). Elektrisches Jetpack: Ladung = EU, `use` =
Entladen ohne Transferlimit. Tick: NBT `hoverMode`, `toggleTimer`. Sprung + Modus bei Timer 0 → Timer 10, Modus
umschalten (Server speichert und meldet „Hover Mode enabled./disabled.“). Sprung oder (Schweben und `motionY < −0,35`)
→ `useJetpack`. Timer −1 je Tick (Server). `useJetpack`: Ladung 0 → nichts. Leistung 1,0/Abfall 0,2 (elektrisch
0,7/0,05); `ladung/max ≤ abfall` → `leistung ×= ladung/(max·abfall)`. **Originalfehler:** das elektrische Jetpack
überschreibt nur `getCharge`, nicht `getMaxCharge(stack)` – `max` bleibt `maxDamage − 2 = 25` (Ladeanzeige 27), der
Abfall greift also erst unter 1,25 EU. Vorwärts: Schub 0,15 (Schweben 0,5, elektrisch
+0,15), `moveFlying(0, 0,4·leistung·schub·2, 0,02)`. Max. Höhe Welthöhe (elektrisch `(int)(Welthöhe/1,28)`); `y >
max − 25` → `leistung ×= (max − min(y, max))/25`. `motionY = min(motionY + leistung·0,2, 0,6)`; Schweben: Grenze −0,1
(elektrisch mit Sprung 0,1), `motionY` über der Grenze → Grenze, außer der vorige Wert war größer. Verbrauch 9
(Schweben 6), elektrisch −2. Fallhöhe 0, Flug-Kick-Zähler zurücksetzen. Sound `Tools/Jetpack/JetpackLoop.ogg`
(Loop, nur eigener Spieler) solange benutzt.

**Nachtsichtbrille**: nur Server; `chargeFromArmor`, dann bei ≥ 1 EU (Entladen 1, Transferlimit 1): Tag → Himmelslicht
am Block des Spielers > 12 → Nachtsicht entfernen, Blindheit `100 + (licht − 13)·50` (ambient); Nacht → Blindheit
entfernen, Nachtsicht 250 (ambient). **Solarhelm**: Brust-Item elektrisch und Sonne sichtbar bei `((int)x, (int)y + 1,
(int)z)` → Brust-Item +1 EU (Tier beliebig, ohne Transferlimit); Haltbarkeit 0. **Static Boots**: Brust-Item
elektrisch → NBT `x`/`z` (`(int)`-Koordinaten) beim Reiten/im Wasser oder fehlend neu setzen; Luftlinie ≥ 5 → neu
setzen, Brust +`min(3, (int)strecke/5)` EU.

**Texturen**: Item-Icons aus `item_0.png` – Hazmat 228/229/230, Gummistiefel 67, Nano 104–107, Quantum 108–111,
Jetpack 68, el. Jetpack 69, BatPack 73, LapPack 150, CF-Pack 46, Solarhelm 164, Static Boots 67, Nachtsichtbrille 232;
Kohlefaser 74, Kohlenetz 75, Kohleplatte 76, leerer Kanister 51. Rüstungsmodelle `armor/ic2/<name>_1|2.png`: nano,
quantum, hazmat (auch Gummistiefel), jetpack (beide), batpack (auch CF-Pack), lappack, solar, rubber (Static Boots),
nightvision.

**Rezepte** (Ergänzung zu `08-rezepte.md` §11): Solarhelm `III/ISI/CCC` I = Eisenbarren, C = isol. Kupferkabel;
` H / S /CCC` H = Eisenhelm. Static Boots ebenso mit Wolle, H = Eisenstiefel. Jetpack: I = raffiniertes Eisen,
C = el. Schaltkreis, R = Redstone. El. Jetpack: I = raffiniertes Eisen, G = Glowstonestaub. BatPack: T = Zinnbarren,
C = el. Schaltkreis, B = RE-Batterie (geladen oder leer). Hazmat: R = Gummi.

## 6. Zellen, Dosen, Kanister

- **Leere Zelle** (`itemCellEmpty`): Rechtsklick auf Wasser-/Lava-Quellblock (Reichweite 5) → Wasser-/Lavazelle ins
  Inventar, Quelle entfernt. Stapelbar 64. Compressor: Zelle → Luftzelle.
- Wasserzelle: Compressor → Schneeball; Extractor → Hydrierzelle. Lavazelle: Brennstoff (Generator 5000 EU,
  Geothermie 1000 mB). Flüssigkeitsregistrierung: Wasser/Lava je 1000 mB (leer = leere Zelle).
- Bio-/hydr. Kohlezelle → Extractor → Biobrennstoff-/Kohlebrennstoffzelle (Canner-Brennstoff 868 / 2548).
- **Zinndose** (leer, stapelbar) und **gefüllte Zinndose** (Nahrung: 2 Hunger, Sättigung 0,95, Essdauer 20 Ticks,
  heilt zusätzlich 1 Herz, gibt leere Dose zurück; Meta-Effekte: 1 = 80 % Hunger, 2 = Gift, 3 = 30 % Hunger,
  4 = Regeneration, 5 = Regeneration IV + Resistenz + Feuerresistenz; Dauern wie im Code `600/(heal/2)` usw.).
- **Brennstoffkanister**: leer / gefüllt (NBT `value`), Ofen-Brennwert `min(32767, value·2)`.

Ergänzung 6b.5a (`ItemTinCan`, `ItemFuelCanFilled`, `IC2.java`):
- Gefüllte Zinndose: `ItemFood(2, 0.95F)`, immer essbar, Essdauer 20 Ticks, Stapel 64. Nach dem Essen `heal(2)` und
  leere Zinndose ins Inventar (sonst fallen lassen). Effekte (`onFoodEaten`, Dauern aus dem Nährwert des
  Vorbilds, Werte in 1.4.7 und 1.21 gleich): 1 = 80 % Hunger 300 Ticks; 2 = Gift 100 Ticks; 3 = 30 % Hunger
  600 Ticks; 4 = Regeneration 50 Ticks; 5 = Regeneration IV 300 Ticks + Resistenz 3000 + Feuerresistenz 3000.
  Tooltip bei Meta 1–3: „This looks bad...“. Icon `item_0.png` 58, leere Dose 57 (beide Meta gleiche Textur).
- Gefüllter Brennstoffkanister: Stapel 1, Behälter-Item = leerer Kanister, Icon 52. Brennwert über Forge-Fuel-Handler
  (gilt für Ofen, Eisenofen und Generator).
- Biozelle (35), hydr. Kohlezelle (34), Bio-Brennstoffzelle (35), Kohle-Brennstoffzelle (34): einfache Items, Stapel
  64 (Icons doppelt belegt wie im Original). Extractor: Biozelle → Bio-Brennstoffzelle, hydr. Kohlezelle →
  Kohle-Brennstoffzelle.
- Quantenhelm: bei Hunger und ≥ 1000 EU erste gefüllte Dose im Inventar: Nahrung (2/0,95) + Effekte + `heal(2)` +
  leere Dose, Stapel −1, 1000 EU.

## 7. Sprengstoffe

| Block/Item | Zünder | Explosion |
|---|---|---|
| Industrie-TNT (ITNT) | Redstone, Feuerzeug, Abbauen, Explosion (verkürzt), Zündschnur 60 Ticks | `ExplosionIC2(5,5, drop 0,9, dmg 0,3)` |
| Atombombe (Nuke, uncommon) | wie ITNT, aber `canExplodeByHand = false`: Abbauen von Hand droppt den Block (auch im Kreativmodus), Feuerzeug/Redstone/Explosion zünden; Zündschnur 300 Ticks (NBT `Fuse` als Byte → nach Neuladen 44); Entity per Rechtsklick mit (Elektro-)Schraubenschlüssel entschärfbar (−1); Textur `block_0.png` 61/62/63 (unten/oben/Seite); `enableCraftingNuke = true` | `ExplosionIC2(35 = explosionPowerNuke, drop 0,05, dmg 1,5, DamageSource nuke)` + Strahlung/Hunger im Umkreis 100; Platzieren/Zünden wird geloggt |
| Dynamit / Klebedynamit (Item, werfbar) | Wurf, Zündschnur 100 Ticks; Klebedynamit haftet an Blöcken; im Wasser erlischt es (+2000 Ticks) | `PointExplosion` 3×3×3: zerstört Blöcke mit Resistenz ≤ 10, Drop 100 %, Entity-Schaden ähnlich Vanilla (Stärke 1 → 2) |
| Dynamit-Block (gelegt) / mit Fernzünder | Redstone, Fernzünder | Zündschnur 40 Ticks (durch Explosion 5) |

Gezündete IC2-Sprengstoffe springen wie TNT (motionY 0,2).

**Industrie-TNT im Detail** (`BlockITNT`/`BlockIC2Explosive`/`EntityItnt`/`EntityIC2Explosive`, Teilpaket 6b.5b):
- Block (`blockITNT`): Material TNT, Härte 0, Klang Gras, Seltenheit normal, Creative-Tab IC2. Textur `block_0.png`
  58 (unten), 59 (oben), 60 (Seiten). Droppt **nie** etwas (`quantityDropped = 0`).
- Zünden (`removeBlockByPlayer` → Block weg, Entity am Blockmittelpunkt, Sound „random.fuse“):
  - **Abbauen durch den Spieler** (auch Creative; `canExplodeByHand = true` für ITNT) – ITNT lässt sich nicht
    aufheben.
  - Rechtsklick mit **Feuerzeug** (wird dabei **nicht** abgenutzt; Feuerkugel/brennende Pfeile zünden nicht).
  - Redstone: beim Platzieren, wenn bestromt; bei Nachbaränderung, wenn der Nachbarblock Redstone liefern kann und
    der Block bestromt ist.
  - Von einer Explosion zerstört (`onBlockDestroyedByExplosion`): Entity ohne Sound, Zündschnur
    `rand(fuse/4) + fuse/8` = 7–21 Ticks.
- Entity (`EntityIC2Explosive`): 0,98 × 0,98, anklickbar, Startbewegung wie Vanilla-TNT (zufällige Richtung 0,02,
  aufwärts 0,2), je Tick Schwerkraft 0,04, Dämpfung 0,98, am Boden horizontal ×0,7 und vertikal ×−0,5; Rauchpartikel
  je Tick; NBT `Fuse` (Byte). Bei `fuse-- ≤ 0`: entfernen und `ExplosionIC2(5,5, 0,9, 0,3, DamageSource explosion)`
  am Entity-Mittelpunkt. Darstellung wie Vanilla-TNT (Block-Modell, in den letzten 10 Ticks aufblähen, Weißblitz bei
  `fuse/5 % 2 == 0`).
- Rezepte: 4 ITNT aus `FFF/TTT/FFF` und `FTF/FTF/FTF` (F = Feuerstein, T = TNT); GT-Variante mit Feuersteinstaub
  (→ 5) folgt mit dem Staub.

## 8. Strahlung, Schadensquellen

- Trank-Effekt **Radioaktivität** (`potion.radiation`, ID 24, schlecht, Farbe 0x4E9331): Schaden `stufe+1` alle
  `25 >> stufe` Ticks (bei Rate 0 jeden Tick; DamageSource `radiation`, ignoriert Rüstung). Icon wie Vanilla-Gift
  (Index 6,0).
- DamageSources: `electricity`, `nuke`, `radiation` (bypass armor). Todesnachrichten siehe Localization.
- Kein Strahlungsschaden durch Uran im Inventar (in 1.115 nicht vorhanden).

## 9. Crops (Pflanzenzucht)

**Crop-Stäbe** (`blockCrop`) werden auf Ackerboden gesetzt. Rechtsklick mit Crop-Stab auf leeren Stab → Kreuzungsstab
(„upgraded“). Basissaaten (Rechtsklick): Weizensamen, Kürbis-, Melonenkerne, Netherwarze, Terra-Warze, Kaffeebohnen
(Stats 1/1/1), Zuckerrohr (1/3/0/2 → Größe 1, Gr 3, Ga 0, Re 2), Kakaobohne (0/0/0), 4 Rosen, 4 Löwenzahn,
Karotte, Kartoffel. Saatbeutel tragen Typ, Growth/Gain/Resistance (0–31) und Scanlevel.

Ablauf alle 256 Ticks (`tick`):
- Umweltwerte (alle 1024 Ticks versetzt neu berechnet):
  - Feuchtigkeit = Biom-Bonus + 2 (Ackerboden nass, Meta ≥ 7) + 2 (Wasserspeicher ≥ 5) + `(wasser+24)/25`.
  - Nährstoffe = Biom-Bonus + Anzahl Erdblöcke 2–4 darunter + `(nährstoffe+19)/20`.
  - Luft = `clamp((y−64)/15, 0, 4)` + `frei/2` (frei = 9 − undurchsichtige Blöcke/Crops im 2×2-Bereich, siehe Code)
    + 2 bei Himmelssicht.
  - Biom-Boni (Feuchte, Nährstoffe): Fluss 2/0, Sumpf 2/2, Wald(+Hügel) 1/1, Dschungel(+Hügel) 1/2, Wüste(+Hügel) −1/0,
    Pilzinsel(+Ufer) 0/2.
- Leerer Kreuzungsstab: mit 1/3 Chance Kreuzung (s. u.); leerer Stab: 1 % Unkraut (außer Weed-EX aktiv).
- Wachstum (wenn `canGrow`): `growthPoints += calcGrowthRate()`, bei ≥ `growthDuration` → Größe + 1.
  `calcGrowthRate`: `base = 3 + rand(7) + Gr`; `need = (tier−1)·4 + Gr + Ga + Re`; `have = weightInfluences · 5`
  (Standard: Feuchte + Nährstoffe + Luft); bei `have ≥ need` → `base·(100 + have − need)/100`, sonst
  `neg = (need−have)·4`; `neg > 100` und `rand(32) > Re` → Pflanze stirbt; sonst `base·(100−neg)/100`.
- Wasser- und Nährstoffspeicher −1 je Tick-Zyklus. Unkraut (bzw. Pflanzen mit Gr ≥ 24 ab Größe 2) breitet sich aus.
- **Kreuzung**: Nachbarn (4) mit Größe ≥ 3 nehmen mit Wahrscheinlichkeit `(4 + [Gr≥16] + [Gr≥30] + min(0, 27−Re bei Re≥28))/20` teil;
  ≥ 2 Teilnehmer nötig. Neue Sorte gewichtet nach Ähnlichkeit (`calculateRatioFor`: 500 bei gleicher Sorte, sonst
  Stat-Ähnlichkeit, +5 je gemeinsamem Attribut, Tier-Abzüge). Neue Stats = Durchschnitt ± `rand(1+2n)−n`, begrenzt 0–31.
- Ernten (Rechtsklick): Anzahl Drops über `dropGainChance · 1,03^Ga` (wiederholt `−random`), +1 je Drop mit
  `Ga %`; Größe danach `getSizeAfterHarvest`. Pflücken (Linksklick): Ernte + Saatbeutel-Chance
  (`dropSeedChance · 1,1^Re` …), Stab wird geleert.
- Pflege: Wassereimer/Wasserzelle (Wasser auf 10), Samen (+25 Nährstoffe bis 50), Knochenmehl/Dünger (+100, Crop-Matron
  +90, bis 100), Hydrierzelle (Wasser bis 200, Crop-Matron bis 180), Weed-EX (+50, kann Stats senken).
- Zertrampeln: Sprinten auf Crop → 1 % × (`rand(40) > Re`) → Crop weg, Ackerboden zu Erde.

| ID | Sorte | Tier | Attribute | Wachstum / Ernte |
|---:|---|---:|---|---|
| 0 | Weed | 0 | Weed, Bad | wächst bis 3, unerntbar, Linksklick entfernt |
| 1 | Wheat | 1 | Yellow, Food, Wheat | bis 7 (Licht ≥ 9), Ernte bei 7: Weizen; danach 2 |
| 2 | Pumpkin | 1 | Orange, Decoration, Stem | 4: Kürbis; Dauer 200/600 |
| 3 | Melon | 2 | Green, Food, Stem | 4: Melone (1/3) oder 2–5 Scheiben |
| 4–8 | Dandelion, Rose, Blackthorn, Tulip, Cyazint | 2 | Farbe, Flower (+Rose/Tulip) | Licht ≥ 12; 4: Farbstoff (11/1/0/5/6) |
| 9 | Venomilia | 3 | Purple, Flower, Tulip, Poison | 4: lila Farbstoff, 5: Grinpulver; berührt bei 5 → Gift |
| 10 | Reed | 2 | Reed | bis 3, Zuckerrohr (Größe−1) |
| 11 | Stickreed | 4 | Reed, Resin | bis 4; 4: Harz |
| 12 | Cocoa | 3 | Brown, Food, Stem | Nährstoffe ≥ 3; 4: Kakao |
| 13 | Ferru | 6 | Gray, Leaves, Metal | Stufe 4 nur über Eisenerz; kleiner Eisenstaub |
| 14 | Aurelia | 8 | Gold, Leaves, Metal | über Golderz; Goldnugget |
| 15 | Redwheat | 6 | Red, Redstone, Wheat | Licht 5–10; 7: Redstone/Weizen; leuchtet (7), Redstone-Signal |
| 16 | Nether Wart | 5 | Red, Nether, Ingredient, Soulsand | auf Seelensand schneller; auf Schnee → Terra-Warze |
| 17 | Terra Wart | 5 | Blue, Aether, Consumable, Snow | auf Schneeblock schneller; auf Seelensand → Netherwarze |
| 18 | Coffee | 7 | Leaves, Ingrident, Beans | Kaffeebohnen |
| 19 | Hops | 5 | Green, Ingredient, Wheat | bis 7; Hopfen |
| 20 | Carrots | 2 | Orange, Food, Carrots | 3: Karotte |
| 21 | Potato | 2 | Yellow, Food, Potato | 3: Kartoffel (2 % giftig) |

Detail-Stats (`stat(0..4)`), Dauern und Sonderregeln je Sorte: `reference/decompiled/ic2/ic2/core/block/crop/*.java`.

Getränke: Kaffee (Bohnen → Pulver; Tasse + Pulver + Wasser → kalter Kaffee, Ofen → Kaffee, + Zucker + Milch →
Milchkaffee; Effekte Eile/Sprungkraft, Überdosis Übelkeit/Schaden) und Fass-Brauen (Weizen/Hopfen → Bier, Zuckerrohr →
Rum; Fass mit Baumzapfer anzapfen) – Details `ItemMugCoffee`, `ItemBooze`, `TileEntityBarrel`.

## 10. Blöcke (Übersicht)

| Block | Härte | Resistenz | Bemerkung |
|---|---:|---:|---|
| Kupfer-/Zinnerz | 3,0 | 5 | Pickaxe Harvest 1; Ofen → Barren (0,5 XP) |
| Uranerz | 4,0 | 6 | Harvest 2; droppt Uranbrocken + 1–3 XP |
| Gummiholz | 1,0 | – | Axt 0; Meta: 0 platziert, 1 natürlich, 2–5 nasse Harzstelle (Seite), 6–11 trocken; regeneriert (Tick alle 100, 1/200) → nass. Droppt 1/6 Harz bei Meta ≠ 0. Brennbar (4/20). Ofen → Tropenholz (Jungle-Stamm) 0,1 XP |
| Gummiblätter | 0,2 | – | Birkenfarbe, Drop 1/35 Setzling, scherbar, brennbar (30/20) |
| Gummisetzling | 0 | – | Wachstum bei Licht ≥ 9 mit 1/30 pro Random-Tick; Knochenmehl sofort |
| Harzschicht | 1,6 | 0,5 | 80 % Drop Harz, bremst Entities (Fallhöhe ×0,75, Bewegung ×0,6/0,85) |
| Gummimatte (`blockRubber`, Trampolin) | 0,8 | 2 | federt |
| Eisenzaun (`blockFenceIron`) | 1,5 | 5 | magnetisierbar, Details unten |
| Verstärkter Stein (`blockAlloy`) | 80 | 150 | Pickaxe 2 |
| Verstärktes Glas (`blockAlloyGlass`) | 5 | 150 (wirksam 90) | Glas-Material (ohne Werkzeug abbaubar, droppt sich selbst), Klang Glas, durchsichtig wie Glas (Flächen zu gleichen Nachbarn ausgeblendet), Textur `block_0.png` 13; 7 Stück aus `GAG/GGG/GAG` oder `GGG/AGA/GGG` (A = fortg. Legierung) |
| Verstärkte Tür | 50 | 150 | Pickaxe 2 |
| Bauschaum (`blockFoam`) / Bauschaumwand (`blockWall`) | 0,01 / 3,0 | 10 / 30 | Schaum härtet zu Wand (Licht abhängig), Wand färbbar |
| Holzgerüst / Eisengerüst | 0,5 / 0,8 | 0,2 / 10 | kletterbar, stapelbar |
| Metallblöcke Kupfer/Zinn/Bronze/Uran (`blockMetal`) | 4,0 | – | |
| Kabel | 0,2 | 6 | siehe EU-System |
| Maschinen/Generatoren/Speicher | 2,0 / 3,0 / 1,5 | – | |
| Luminator | 0,3 | 0,5 | |
| Bergbaurohr / -spitze | 6,0 | 10 | |
| Personal-Blöcke | – | 6 000 000 | |
| ITNT/Nuke/Dynamit | 0 | – | |
| Crop | – | – | siehe §9 |

**Gummimatte** (`BlockRubberSheet`, nachgetragen für Phase 12.1; Sprite `block_0.png` 40, Material Stoff, Klang
Wolle, Höhe 0,125, Item `uncommon`): platzierbar/haltbar nur, wenn ein waagerechter Nachbar (N/S/W/O) ein
Normalwürfel oder eine Gummimatte ist; sonst droppt sie bei Nachbaränderung. Meta 1 = „trägt“ (Zwischenspeicher, bei
jeder Nachbaränderung auf 0). Wirkung (`onEntityCollidedWithBlock`, also wenn ein Wesen in den Blockraum eintritt)
**nur bei Luft darunter**: Lebewesen auf einer nicht tragenden Matte zerstören sie (ohne Drop). Tragend =
Meta 1 oder in x- **oder** z-Richtung beidseitig über eine Kette von Matten ein Normalwürfel in gleicher Höhe oder
unter einem Kettenglied (dann Meta 1). Sonst bei `motionY ≤ −0,4`: Fallhöhe 0, `motionX/Z × 1,1`, `motionY ×`
−1,3 (Lebewesen springt), −0,1 (Spieler schleicht), sonst −0,8. Rezept 3 Stück `RRR/RRR` (R = Gummi).
`RubberTrampoline.ogg` wird nirgends abgespielt.

**Verstärkter Stein** (`blockAlloy`, Sprite 12, Material Eisen, Klang Metall, Härte 80, `setResistance(150)` = 90):
Rezept 8 Stück `SSS/SAS/SSS` (S = Stein, A = fortgeschrittene Legierung). **Verstärkte Tür** (`BlockIC2Door`,
Sprites 14 oben/15 unten, Item-Sprite `item_0.png` 102, Material Eisen → nur per Redstone zu öffnen, Härte 50,
Resistenz 90, Klang Metall): Rezept `SS/SS/SS` aus verstärktem Stein; untere Hälfte droppt die Tür. GT nutzt
verstärkten Stein nur für iridium-/wolframstahlverstärkten Stein (nicht im Umfang).

**Bauschaum** (`BlockFoam`, nachgetragen für Phase 12.2; Sprite `block_0.png` 37, Material Feuer → ersetzbar,
keine Kollision, beim Schieben zerstört; Klang Stoff; Härte 0,01, `setResistance(10)` = 6; kein Drop, auch nicht mit
Behutsamkeit; zählt als Normalwürfel): härtet bei Zufallsticks und – nach einem misslungenen Versuch – 500 Ticks später
erneut: `Lichtwert(Block) · 6 ≥ random(1000)` → Bauschaumwand Farbe 7 (hellgrau). Rechtsklick mit Sand härtet sofort
(1 Sand, im Kreativmodus keiner). Rezept 3 Stück formlos Tonstaub + Wasser + Redstone + Kohlestaub.
**Bauschaumwand** (`BlockWall`, Sprites 96–111 = Farbe 0–15, Material Stein, Klang Stein, Härte 3,
`setResistance(30)` = 18): Meta = IC2-Farbe, färbbar mit dem Farbroller (`IPaintableBlock`), kein Drop,
Pick-Block = Bauschaum, kein Creative-Eintrag.

**Gerüste** (`BlockScaffold`, nachgetragen für Phase 12.2): Holz (Sprites 116 Seite/117 oben-unten/118 Seite
verstärkt, Holz, Härte 0,5, `setResistance(0.2)` = 0,12, brennbar 8/20, im Werkzeugkasten, Recycler-Blacklist) und
Eisen (Sprites 132–134, Eisen, Härte 0,8, Resistenz 6, Klang Metall). Meta = **Stärke**: Standard 2 (Eisen 5),
verstärkt 5 (Eisen 12). `getStrengthFrom` = max(Standard + 1, falls der Block darunter eine **Säule** ist [Gerüstsäule
gleicher Art, die auf einem Normalwürfel steht, oder der Normalwürfel selbst]; Stärke des Gerüsts darunter und der vier
waagerechten Nachbarn gleicher Art, je höchstens „verstärkt“) − 1. Platzierbar nur bei Ergebnis ≥ 0; Nachbaränderung
→ Tick nach 1: verstärktes Gerüst ohne Säule droppt die Verstärkung und rechnet neu, sonst neu rechnen; < 0 → Block
weg, droppt sich selbst. Rechtsklick (nicht schleichend) auf eine nicht verstärkte Säule mit ≥ 2 Stöcken (Holz) bzw.
1 Eisenzaun (Eisen) → verstärkt (verbraucht auch im Kreativmodus). Linksklick mit dem Gerüst-Item → oben auf die
Säule ein weiteres Gerüst (unter Welthöhe, ersetzbarer Platz, Stärke ≥ 0). Drop: sich selbst, verstärkt zusätzlich
2 Stöcke bzw. 1 Eisenzaun. Kollision waagerecht 1/16 eingerückt; Spieler darin: Fallhöhe 0, `motionY ≥ −0,15`,
Vorwärtstaste → `motionY = 0,2` (Klettern). Rezepte: Holz **4** (GT) `PPP/ s /s s`, Eisen 16 (raff. Eisen + Eisenzaun).

**Eisenzaun** (`BlockPoleFence`, Sprite `block_0.png` 1, Material Eisen, kein Vollblock): verbindet sich **nur mit
anderen Eisenzäunen** (Nord/Süd/West/Ost). Pfosten 0,25 × 0,25 (Mitte), Kollision 1,5 hoch, Auswahl 1 hoch; je
verbundener Seite verlängert sich die Box bis zum Blockrand; Strahltreffer: Pfosten + je Seite zwei Latten
(Höhe 0,375–0,5625 und 0,75–0,9375, Breite 0,125). Meta 0–15 = Magnetisierung (Magnetisierer, `04-maschinen.md`).
Wirkung nur auf **Spieler** an einem **freistehenden Pfahl** (kein Nachbar-Eisenzaun) und nur bei Meta > 0 *und*
Metallstiefeln (Vanilla Eisen/Gold/Kette, IC2 Bronze/Nano/Quantum/Static Boots): Meta −1 je Tick, `motionY += 0,075`,
bei `motionY > 0` → `×1,03` und `fallDistance = 0`; Obergrenze 1,5 (schleichend 0,3). Sonst schleichend: `motionY <
−0,25` → `×0,9`, sonst `fallDistance = 0`. Rezept: 12 aus `III/III` (raff. Eisen). GT: Macerator 2 Zäune → 3 kleine
Eisenstaubhaufen *(GT-Kleinstaub nicht im Umfang)*.
