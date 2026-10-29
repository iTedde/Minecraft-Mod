# 28 – GraviSuite

Quelle: `ftb ultimate/mods/gravisuite.zip` (Gravitation Suite **1.8** für 1.4.7, `required-after:IC2`), Pack-Config
`ftb ultimate/config/GraviSuite.cfg` (alle Rezepte aktiv, `ultimateHelmetGenDay=512`, `ultimateHelmetGenNight=64`,
`hudPosition=1`, Vajra-Präzisionsmodus aktiv, Sounds aktiv). Dekompiliert (2026-10-24, Vineflower ohne Remapping,
Minecraft-Namen verschleiert): `reference/decompiled/gravisuite/gravisuite/` – `GraviSuite` (Items, Rezepte),
`Item*` (je Item eine Klasse), `GraviChestPlateServerProxy`/`…ClientProxy` (Flug), `ServerTickHandler`/
`ClientTickHandler` (Tick, HUD), `Keyboard`/`KeyboardClient`/`ServerPacketHandler` (Tasten). Texturen im Zip:
`gravisuite/gravi_items.png` (16×16-Raster), Rüstungstexturen `graviChestPlate.png`, `advNanoChestPlate.png`,
`ultimate_lappack.png`, `advanced_lappack.png`, `advanced_jetpack.png`, `ultimateSolarHelmet.png`; Sounds
`gravisuite/audio/sounds/` (`JetpackLoop.ogg`, `toolChange.ogg`, `wrench.ogg`, `Treetap.ogg`; `JetpackFire.ogg`
ungenutzt).

Obfuskierte Vanilla-Namen über `ftb ultimate/instmods/minecraft.jar` aufgelöst: `up.n` = Diamant, `up.p` = Goldbarren,
`up.aT` = Glowstonestaub, `amq.C` = Grundgestein, `amq.bk/bl` = Redstone-Verstärker (aus/an), `amq.S` = Werfer,
`amq.ac/Y` = Kolben/klebriger Kolben, `sv.e` = Rüstungsmaterial Diamant, `uq.d`/`uq.c` = Werkzeugmaterial
Diamant/Eisen; Raritäten `vb.b/c/d` = uncommon/rare/epic.

Nutzerwunsch (2026-10-24): „Advanced Jetpack“ → GraviSuite aus dem Pack, **kompletter Umfang**. Entscheidungen
(Nutzer, 2026-10-24): **Ultimativer Solarhelm wie im Pack** (kein Rezept, nur Kreativ-Tab, 512/64 EU/t);
**HUD oben rechts** (Pack-Option 2, weil oben links Xaero's Minimap sitzt). Paket `dev.craft.gravisuite`.

## 1 Übersicht

Alle Items im IC2-Kreativ-Tab, elektrische Items dort geladen + leer. Elektrisch = Ladung wie IC2 (`spec/01`),
Haltbarkeit 27 als Ladeanzeige, Stapel 1, nicht verzauberbar (Verzauberbarkeit 0), nicht reparierbar.

| Item (Registry `craft:…`) | Name EN / DE | Slot | Kapazität EU | Transfer | Tier | liefert Energie | Rarität | Textur |
|---|---|---|---|---|---|---|---|---|
| `gravi_chestplate` | GraviChestPlate / Gravitations-Brustplatte | Brust | 10 000 000 | 20 000 | 2 | ja | epic | 0 |
| `ultimate_lappack` | Ultimate Lappack / Ultimatives LapPack | Brust | 10 000 000 | 20 000 | 2 | ja | rare | 1 |
| `ultimate_solar_helmet` | Ultimate Solar Helmet / Ultimativer Solarhelm | Kopf | 1 000 000 | 20 000 | 2 | nein | epic | 6 |
| `vajra` | Vajra / Vajra | Werkzeug | 1 000 000 | 10 000 | 2 | nein | epic | 7 |
| `advanced_diamond_drill` | Advanced Diamond Drill / Fortgeschrittener Diamantbohrer | Werkzeug | 15 000 | 500 | 2 | nein | uncommon | 8 |
| `advanced_chainsaw` | Advanced Chainsaw / Fortgeschrittene Kettensäge | Werkzeug | 15 000 | 500 | 2 | nein | uncommon | 9 |
| `advanced_lappack` | Advanced Lappack / Fortgeschrittenes LapPack | Brust | 1 000 000 | 1 000 | 2 | ja | uncommon | 10 |
| `advanced_jetpack` | Advanced Electric Jetpack / Fortgeschrittenes elektrisches Jetpack | Brust | 1 000 000 | 1 000 | 2 | ja | uncommon | 13 |
| `advanced_nano_chestplate` | Advanced NanoChestPlate / Fortgeschrittene Nano-Brustplatte | Brust | 1 000 000 | 1 000 | 2 | ja | uncommon | 15 |
| `gravitool` | GraviTool / GraviTool | Werkzeug | 100 000 | 10 000 | 2 | nein | uncommon | 16–19 (Modus) |

Bauteile (`ItemSimpleItems`, Stapel 64, Kreativ-Tab IC2):

| Item | Name EN / DE | Textur |
|---|---|---|
| `superconductor_cover` | Superconductor Cover / Supraleiter-Hülle | 2 |
| `superconductor` | Superconductor / Supraleiter (Ore-Dict `itemSuperconductor`) | 3 |
| `cooling_core` | Cooling Core / Kühlkern | 4 |
| `gravitation_engine` | Gravitation Engine / Gravitationsantrieb | 5 |
| `magnetron` | Magnetron / Magnetron | 11 |
| `vajra_core` | Vajra Core / Vajra-Kern | 12 |
| `engine_booster` | Engine Booster / Triebwerksverstärker | 14 |

„liefert Energie“ (`canProvideEnergy = true`) bedeutet wie beim LapPack: das Teil lädt beim Benutzen elektrischer
Werkzeuge über `chargeFromArmor` (`spec/01`), sofern Tier ≥ Werkzeug-Tier. Das gilt hier auch für Jetpack,
Nano- und Gravitations-Brustplatte (alle implementieren `canProvideEnergy` mit `true`).

## 2 Rüstung allgemein (`ISpecialArmor`, Material Diamant)

Wie die elektrische IC2-Rüstung (`spec/05` „Elektrische Rüstung“), mit diesen Werten (`basis·faktor`, AbsorbMax,
Rüstungsanzeige):

| Teil | basis × faktor | EU pro Schaden | AbsorbMax | Rüstungsanzeige |
|---|---|---|---|---|
| Gravitations-Brustplatte | 0,4 × 1,1 = 0,44 | 900 | `25·ladung/900` | immer 9 (auch leer) |
| Fortg. Nano-Brustplatte | 0,4 × 0,9 = 0,36 | 800 | `ladung/800` (**Originalfehler:** ohne ×25) | 7, wenn Ladung ≥ 800, sonst 0 |
| Ultimativer Solarhelm | 0,15 × 1,0 = 0,15 | 900 | `ladung/900` (**Originalfehler:** ohne ×25) | immer 3 |
| Ultimatives/Fortg. LapPack, Fortg. Jetpack | 0 | 0 | 0 | 0 |

`damageArmor`: entlädt `schaden·EU_pro_Schaden` ohne Transferlimit. Unblockbarer Schaden: wie IC2 (Forge ruft
`getProperties` dafür nicht auf). Alle Teile sind „Metallrüstung“ (`IMetalArmor`, nur für Tesla-Spule/Metallprüfung).

## 3 LapPacks

- **Fortgeschrittenes LapPack**: 1 000 000 EU, Transfer 1 000, Tier 2, liefert Energie, kein Schutz.
- **Ultimatives LapPack**: 10 000 000 EU, Transfer 20 000, Tier 2, liefert Energie, kein Schutz.
- HUD (§7) zeigt beim Tragen „Energy level: n%“.

## 4 Fortgeschrittenes Jetpack und Fortg. Nano-Brustplatte (`ItemAdvancedJetPack`)

Die Nano-Brustplatte erbt das Jetpack komplett (gleiche statische Werte: 1 000 000 EU, 12 EU/Tick, Boost ×5) und
schützt zusätzlich (§2). NBT: `isFlyActive` (Triebwerk an), `isHoverActive` (Schwebemodus), `toggleTimer`.

- **Triebwerk an/aus**: Flug-Taste (§6) → Server schaltet `isFlyActive` und meldet „§eJetpack §aengine ON“ /
  „§eJetpack §cengine OFF“. Standard aus; ohne Triebwerk passiert nichts.
- **Schwebemodus**: Modus-Taste (IC2) **gedrückt (Flanke) während Sprung gehalten** → Server schaltet
  `isHoverActive`, setzt `toggleTimer = 10` (wird nur heruntergezählt, sonst nicht ausgewertet), meldet „§eHover Mode
  §aenabled.“ / „§cdisabled.“.
- **Tick** (Server **und** Client, nur Brustslot): `(Sprung oder (Schweben und motionY < −0,35)) und Triebwerk an` →
  `useJetpack(schweben)`; Server: `toggleTimer > 0` → −1.
- `useJetpack`:
  - Ladung < 12 und nicht Kreativ → nichts (false).
  - `leistung = 1,0`; `ladung/1 000 000 ≤ 0,001` (≤ 1 000 EU) → `leistung ×= ladung/50 000` (**Originalfehler:**
    Sprung von 1,0 auf ≤ 0,02). Kreativ → 1,0.
  - Vorwärts-Taste: `schub = 0,3` (Schweben 0,65), `s = leistung·schub·2`. Boost-Taste (IC2) und (Ladung > 60 oder
    Kreativ) → `boost = 0,09` (Schweben 0,07). `s > 0` → `moveFlying(0, 0,4·s + boost, 0,02 + boost)`; mit Boost
    und nicht Kreativ zusätzlich `use(60)`.
  - Höhe: `max = Welthöhe` (1.4.7: 256); `y > max − 25` → `leistung ×= (max − min(y, max))/25`.
  - `motionY = min(motionY + leistung·0,2, 0,6)`.
  - Schweben: Grenze −0,1 (mit Sprung +0,1); `motionY > Grenze` → `motionY = Grenze`, außer der Wert vor dieser
    Tick-Änderung war größer (dann der alte Wert).
  - Nicht Kreativ → `use(12)`. Fallhöhe 0, Schrittweg 0, Flug-Kick-Zähler des Servers 0. → true.
  - `use(n)` = Entladen ohne Transferlimit (nicht `ElectricItem.use`, also **ohne** Nachladen aus der Rüstung).
- Sound (nur Client, eigener Spieler): solange `useJetpack` true liefert, Loop `JetpackLoop.ogg` am Rücken; endet beim
  Wechsel auf false.
- Jetpack und Nano-Brustplatte haben keinen Fallschutz außer „Fallhöhe 0“ während des Schubs.

## 5 Gravitations-Brustplatte (`ItemGraviChestPlate`, `GraviChestPlate*Proxy`)

Werte: Mindestladung zum Einschalten 80 000, Verbrauch **416 EU/Tick**, Boost-Faktor 3 (1 248 EU), Boost-Tempo 0,11.
NBT `isFlyActive`. Schutz wie Quantum-Brustplatte (§2), aber keine sonstigen Quantum-Effekte.

- **Flug-Taste** (§6): an → Ladung ≥ 80 000 oder Kreativ: „§aGravitation engine ON“, Fähigkeiten `mayfly = true`,
  `flying = true`; sonst „Not enough energy to run Gravitation engine !“. Aus → „§cGravitation engine OFF“, außer im
  Kreativmodus `mayfly = flying = false`. Server merkt sich je Spieler „Flug durch Mod aktiv“.
- **Tick** (Brustslot, Server + Client):
  - Wurde die Platte zuvor abgelegt („zuletzt ausgezogen“), wird `isFlyActive` auf false gesetzt.
  - `isFlyActive`: nicht Kreativ → Ladung < 416: „§cWarning ! Your's energy cell is depleted ! Gravitation engine
    shutdown !“ und ausschalten; sonst 416 EU entladen (Tier 3, ohne Transferlimit). Das gilt im Pack auch am Boden.
    **Abweichung (Nutzer, 2026-10-29):** am Boden kein Verbrauch und keine Abschaltung; beides nur in der Luft.
  - Fallhöhe 0.
  - **Boost**: nicht am Boden, fliegend, Boost-Taste: Ladung ≤ 1 248 und nicht Kreativ → „Not enough energy to
    boost !“ (jeden Tick); sonst `moveFlying(seitwärts, vorwärts, 0,11)` (Client, Eingaben ±1, mit Schleichen ×0,3;
    nicht im Wasser) **und** 1 248 EU entladen, dazu Sprung → `motionY += 0,14`, Schleichen → `motionY −= 0,14`,
    nochmals 1 248 EU (**Originalfehler:** Boost kostet 2 × 1 248, also 2 912 EU/Tick mit Grundverbrauch).
  - Fähigkeiten an den Client senden.
  - Höhe > 262 und nicht Kreativ → Position auf y = 262.
- **Kreativ-Wechsel**: Platte getragen, Kreativ an → gemerkt; danach Überleben und Flug durch Mod aktiv →
  `mayfly = flying = true`.
- **Ablegen**: Platte nicht mehr im Brustslot und Flug durch Mod aktiv → `mayfly = flying = false`, „zuletzt
  ausgezogen“.
- **Welt laden**: Client meldet „worldLoad“ beim ersten Tick mit Platte; Server: `isFlyActive` → ausschalten und
  sofort wieder einschalten (Prüfung Mindestladung), dann Paket „setFlyStatus“ an den Client, der dasselbe tut.
  **Originalfehler:** scheitert das Wiedereinschalten (< 80 000 EU), bleibt das mit dem Spieler gespeicherte `mayfly`
  stehen (der Mod-Flugstatus ist nach dem Laden leer, Ablegen nimmt es also nicht weg).
- Umsetzung (27.3): Server maßgeblich, Client nur Boost-Bewegung (`moveRelative` mit `xxa`/`zza`, ±0,14 vertikal)
  und Höhenklemme; Welt-Laden über `PlayerLoggedInEvent` (Abweichungen in `abweichungen.md`).

## 6 Tasten und Netzwerk

- **Flug-Taste** „Gravi Fly Key“ (Pack: F): sendet „keyFLY“; Server schaltet bei Gravitations-Brustplatte den Flug
  (§5), bei Fortg. Jetpack/Nano-Brustplatte das Triebwerk (§4). Client schaltet bei der Gravitations-Brustplatte
  zusätzlich lokal (Vorhersage).
- Genutzte IC2-Tasten (vorhandenes `Ic2Keyboard`): Boost, Alt, Modus (Flanke), Vorwärts, Springen, Schleichen.
  GraviSuite hält eine eigene Bitmaske (Bit 0 Boost, 1 Alt, 2 Modus, 3 Vorwärts, 4 Springen, 5 Schleichen); im Mod
  reicht der vorhandene IC2-Tastenzustand, Schleichen kommt aus `player.isShiftKeyDown()`.
- „keyModePressed“ (Modus-Flanke) → Schwebemodus des Jetpacks (§4).
- **Originalfehler** `Keyboard.isForwardKeyDown` prüft `jumpKeyState.containsKey` – ohne Wirkung, da alle Zustände
  gemeinsam gesetzt werden.

## 7 HUD (`ClientTickHandler.tickEnd`, Render-Tick)

Nur im Spiel, ohne offenen Bildschirm, `Display hud = true`. Je getragener Brust:

- Gravitations-Brustplatte: Zeile 1 „§aGravitation engine ON“ (nur wenn Flug aktiv), Zeile 2 „Energy level: n%“.
- Fortg. Jetpack/Nano-Brustplatte: Zeile 1 „§eJetpack §aengine ON“ + „ §e(Hover mode)“ (nur Triebwerk an), Zeile 2
  „Energy level: n%“.
- Ultimatives/Fortg. LapPack: nur „Energy level: n%“.
- `n = ladung·100/max`; Farbe: ≤ 5 rot (§c), ≤ 10 gold (§6), sonst weiß. Ohne Statuszeile steht die Energiezeile in
  Zeile 1.
- Position **oben rechts** (Pack-Option 2, Entscheidung Nutzer): rechtsbündig 2 px vom Rand, y = 2, Zeile 2 bei
  `2 + 3 + Schrifthöhe`. Schatten-Text, weiß.
- `GetUSHGenerateStatus` (Solarhelm „day/night/no sky“) wird im Pack nicht angezeigt (ungenutzt).

## 8 Ultimativer Solarhelm (`ItemUltimateSolarHelmet`)

Kein Rezept im Pack (Rezeptblock leer) → zunächst nur Kreativ-Tab (Entscheidung Nutzer: wie im Pack). **Seit Phase 36
(Nutzer, 2026-10-29) mit Rezept** (§10), Leistung unverändert.

- Tick (Server, Spieler-Tick, Kopfslot): alle 128 Aufrufe Sichtprüfung: `nassesBiom = Regen im Biom möglich`,
  `sonne = Tag und nicht (nassesBiom und (Regen oder Gewitter))`, `himmel = Himmel über (x, y+1, z) sichtbar und
  Dimension hat Himmel`. Erzeugung je Tick: Sonne + Himmel → **512**, nur Himmel → **64**, sonst 0.
- Verteilung: zuerst Rüstungsslots (Füße → Kopf, also auch der Helm selbst), dann Hauptinventar 0–35:
  `charge(stack, rest, Tier 3, mit Transferlimit)` je elektrischem Item, bis der Rest 0 ist; Rest verfällt.
- **Originalfehler:** Zähler und Sichtstatus sind statisch (für alle Spieler gemeinsam) → im Mod je Spieler (Abweichung).

## 9 Werkzeuge

Allgemein wie IC2-Elektrowerkzeuge (`spec/05`): `canUse`/`use` mit Nachladen aus Rüstung (`ElectricItem.use`), bei
Nicht-Spielern `discharge` ohne Transferlimit. Nahkampf-Grundschaden 1.

### 9.1 Fortgeschrittener Diamantbohrer (`ItemAdvDDrill`)

Wie der Diamantbohrer (`spec/05`: gleiche Abbauliste inkl. Obsidian und Material Stein/Eisen, Werkzeugklasse
`pickaxe` 3, Material Diamant), aber: 15 000 EU, Transfer 500, Tier 2, **Effizienz 35**, **160 EU** je Block (Härte
≠ 0). Ohne 160 EU Ladung Geschwindigkeit 1,0. Treffer ohne Kosten. Keine Sounds (die Bohrer-Sound-Methode wird
nicht aufgerufen).

### 9.2 Fortgeschrittene Kettensäge (`ItemAdvChainsaw`)

Wie die Kettensäge (`spec/05`), aber: 15 000 EU, Transfer 500, Tier 2, **Effizienz 35**, 100 EU je Block (Härte
≠ 0), Werkzeugklasse `axe` 2, Material Eisen. Abbauliste: Bretter, Bücherregal, Holz, Truhe, Blätter, Spinnennetz,
**Wolle, Kürbis, Melone, Kaktus, Schneedecke**, Gummiblätter + jedes Material Holz (kein Crop).

- Schlag: `use(100)` **zweimal** erfolgreich → zusätzlicher Treffer 13 (effektiv Gesamtschaden 13), sonst 1.
- Block scheren (`onBlockStartBreak`, Server, `IShearable`): schergbar und zweimal `use(100)` ohne Spieler (also ohne
  Nachladen aus der Rüstung) → Scher-Drops (Glück aus Verzauberung), Aufhebe-Verzögerung 10, Statistik.
- Entity scheren (Rechtsklick, Server): `use(200)` **vor** der Schergbarkeitsprüfung (**Originalfehler** wie IC2),
  Drops mit Zufallsimpuls.
- Keine Sounds (kein Leerlauf-Loop).

### 9.3 Vajra (`ItemVajra`)

1 000 000 EU, Transfer 10 000, Tier 2, Material Diamant, **Effizienz 20 000** (ohne 3 333 EU Ladung 0,5), **baut
jeden Block ab** (`canHarvestBlock` immer true, also mit Drops), 3 333 EU je abgebautem Block (auch bei Härte 0).

- Schlag: `use(6 666)` erfolgreich → zusätzlicher Treffer 25, sonst 1.
- **Präzisionsmodus** (Rechtsklick auf Block, Config „Disable Vajra accurate mode = false“): Block ≠ Grundgestein und
  `canUse(3 333)` → Server: Drops des Blocks (Glück 0) mit Aufhebe-Verzögerung 10 an der Blockposition, Block → Luft,
  Schrittgeräusch des Blocks (Lautstärke `(v+1)/2`, Tonhöhe `p·0,8`), `use(3 333)`. Ohne Ladung → nichts. Wirkt auch
  auf Endportalrahmen/Portale (wie im Pack, nur Grundgestein ist ausgenommen).

### 9.4 GraviTool (`ItemGraviTool`)

100 000 EU, Transfer 10 000, Tier 2, Effizienz 16 ohne Abbauliste (also normales Abbautempo), keine Treffer-Kosten.
NBT `toolMode` 1–4 (ungültig → 1), `textureIndex` 16–19. Name des Stacks: „GraviTool (Hoe)“, „(TreeTap)“,
„(Wrench)“, „(Screwdriver)“ (wird beim Moduswechsel und bei jedem `onItemUseFirst` gesetzt).

- **Moduswechsel**: Rechtsklick mit gehaltener Modus-Taste (Server) → Modus +1 (4 → 1), Meldung „§2Hoe §ftool
  §aactivated“, „§6TreeTrap …“ (sic), „§bWrench …“, „§dScrewdriver …“; Client spielt `toolChange.ogg`. Ein Klick
  auf einen Block führt danach trotzdem die Moduswirkung aus.
- **Hacke** (Modus 1, `onItemUse`): Spieler darf den Block bearbeiten; `canDischarge(50)` sonst „Not enough energy
  for this operation !“; Forge-Hacken-Event (ALLOW → 50 EU, fertig). Gras mit Luft darüber und Seite ≠ unten → Acker;
  **Erde immer** (**Originalfehler:** auch von unten und mit Block darüber). Ackerboden-Schrittgeräusch, 50 EU.
- **Baumzapfer** (Modus 2, `onItemUse`): Fass → per Reflexion `useTreetapOn` statisch aufgerufen, scheitert immer
  (**Originalfehler:** Fass wird nicht angezapft, danach keine weitere Wirkung). Gummiholz → wie Baumzapfer
  (`spec/05`, inkl. 2-Harz-Fehler), je Erfolg 50 EU, vorher `canDischarge(50)` sonst Meldung; Sound `Treetap.ogg`.
- **Schraubenschlüssel** (Modus 3, `onItemUseFirst`): wie Elektrischer Schraubenschlüssel (`spec/05`, Alt-/Schleich-
  Regeln für die Seite), aber Drehen **50 EU**, Abbau **500 EU**, und **immer verlustfrei**: Drop-Rate < 1 → 10 000 EU
  (fehlen sie: „Not enough energy for lossless wrench operation“, kein Abbau); sonst Zufall mit Drop-Rate und 500 EU.
  Drops mit Aufhebe-Verzögerung 10. Terraformer-Bauplan-Auswurf scheitert (**Originalfehler:** Klassennamen-Vergleich
  mit `==`). Sound `wrench.ogg`. Zugleich BuildCraft-Schraubenschlüssel (`IToolWrench`): nur in Modus 3, braucht 500 EU
  (sonst „Not enough energy for this operation“), jede Nutzung 500 EU; Schleich-Klick geht an den Block.
- **Schraubendreher** (Modus 4, `onItemUseFirst`), je 500 EU (sonst „Not enough energy for this operation“), Sound
  `wrench.ogg`:
  - Redstone-Verstärker: Richtung im Uhrzeigersinn weiter, Verzögerung bleibt.
  - Werfer: Richtung `((m & 3) ^ (m >> 2)) + 2` → Nord → West → Süd → Ost → Nord.
  - Kolben/klebriger Kolben: Richtung +1 (unten → oben → Nord → Süd → West → Ost → unten).
  - RedPower-`IRotatable`: entfällt (kein RedPower-Block im Mod).

## 10 Rezepte (`GraviSuite.postInit`)

IC2-`AdvRecipe` (nicht gespiegelt; elektrische Zutaten übertragen ihre **gesamte Ladung** aufs Ergebnis, `spec/01`;
nicht-elektrische IC2-Zutaten nur mit Schaden 0 – Kühlzelle/Wärmetauscher/-ableiter also nur unbenutzt), außer
Supraleiter-Hülle und Supraleiter (Vanilla-Rezept) und Magnetron (Ore-Rezept).

| Ergebnis | Muster | Zutaten |
|---|---|---|
| 3 Supraleiter-Hülle | `RBR/CCC/RBR` | R Fortgeschrittene Legierung, B Iridiumplatte, C Kohlenstoffplatte |
| 3 Supraleiter | `RRR/CBC/RRR` | R Supraleiter-Hülle, B Goldbarren, C Glasfaserkabel |
| Kühlkern | `RBR/CDC/RBR` | R 60k-Kühlzelle, B Fortg. Wärmetauscher, C Wärmekapazitive Reaktorplatte, D Iridiumplatte |
| Gravitationsantrieb | `RBR/CDC/RBR` | R Tesla-Spule, B Supraleiter, C Kühlkern, D HV-Transformator |
| Ultimatives LapPack | `RBR/RDR/RAR` | R Lapotronkristall, B Iridiumplatte, D LapPack **oder** Fortg. LapPack, A Supraleiter |
| Gravitations-Brustplatte | `RAR/DBD/RCR` | R Supraleiter, A Quanten-Brustplatte, D Gravitationsantrieb, B HV-Transformator, C Ultimatives LapPack |
| Fortg. LapPack | `A/B/C` (eine Spalte) | A LapPack, B Fortgeschrittener Schaltkreis, C Lapotronkristall |
| Fortg. Diamantbohrer | `ABA/CAC` (oberste Zeile leer) | A Übertakter-Upgrade, B Diamantbohrer, C Fortg. Schaltkreis |
| Fortg. Kettensäge | ` F /ABA/CAC` | F Diamant, A Übertakter-Upgrade, B Kettensäge, C Fortg. Schaltkreis |
| Magnetron | `ABA/BCB/ABA` | A Raffinierter Eisenbarren, B Kupferbarren, C Supraleiter |
| Vajra-Kern | ` A /BCB/FDF` | A Magnetron, B Iridiumplatte, C Tesla-Spule, F Supraleiter, D HV-Transformator |
| Vajra | `ABA/CDC/FGF` | A Raffinierter Eisenbarren, B Energiekristall, C Kohlenstoffplatte, D Vajra-Kern, F Fortg. Legierung, G Lapotronkristall |
| GraviTool | `ABA/CDC/EFG` | A Kohlenstoffplatte, B Elektrische Hacke, C Fortg. Legierung, D Energiekristall, E Elektr. Schraubenschlüssel, F Fortg. Schaltkreis, G Elektr. Baumzapfer |
| Triebwerksverstärker | `ABA/CDC/BFB` | A Glowstonestaub, B Fortg. Legierung, C Fortg. Schaltkreis, D Übertakter-Upgrade, F Fortg. Wärmeableiter |
| Fortg. Jetpack | `ABA/CDC/EFE` | A Kohlenstoffplatte, B Elektrisches Jetpack, C Triebwerksverstärker, D Fortg. LapPack, E Glasfaserkabel, F Fortg. Schaltkreis |
| Fortg. Nano-Brustplatte | `ABA/ACA/DFD` | A Kohlenstoffplatte, B Fortg. Jetpack, C Nano-Brustplatte, D Glasfaserkabel, F Fortg. Schaltkreis |
| Ultimativer Solarhelm | `PSP/CQC/PTP` | **nicht im Pack** (Phase 36, Nutzer 2026-10-29): P Iridiumplatte, S IC2-Solarhelm, C Supraleiter, Q Quantenhelm, T HV-Transformator |

Alle Zutaten existieren im Mod (Iridiumplatte/Kohlenstoffplatte/Legierung aus IC2/GT, Reaktorteile aus Phase 7,
Tesla-Spule aus Phase 12). Ladungsübertrag z. B. Ultimatives LapPack = Summe aus 6 Lapotronkristallen + (Fortg.)
LapPack, begrenzt auf 10 000 000.

## 11 Darstellung und Sounds

- Item-Texturen aus `gravi_items.png` (Index wie §1), GraviTool je Modus eigene Textur (16 Hacke, 17 Baumzapfer,
  18 Schraubenschlüssel, 19 Schraubendreher) – im Mod über eine Item-Property/Modelüberschreibung nach `toolMode`.
- Rüstungsmodelle aus den 64×32-Rüstungstexturen (Schicht 1).
- Sounds: `JetpackLoop.ogg` (Loop), `toolChange.ogg`, `wrench.ogg`, `Treetap.ogg` (je einmal, an der Hand); Config
  „Disable all sounds = false“.

## 12 Abweichungen 1.21 (→ `abweichungen.md` bei der Umsetzung)

1. **Flug-Taste**: Pack-Standard F ist in 1.21 „Hand wechseln“ → Standard **G** (frei belegbar, Kategorie Craft).
2. **Höhengrenzen**: Gravitations-Brustplatte `y > 262` → `y > maxBuildHeight + 6` (Oberwelt 326); Jetpack `Welthöhe`
   → `maxBuildHeight` (wie beim IC2-Jetpack im Mod).
3. **Statischer Zustand** (Solarhelm-Sicht, Jetpack-Sound, Gravi-Kreativstatus) je Spieler statt global.
4. **Meldungen** als Systemnachricht wie im Pack; nur „Not enough energy to boost !“ (jeden Tick) in die Aktionsleiste,
  damit der Chat nicht überläuft.
5. **HUD oben rechts** statt oben links (Entscheidung Nutzer, Xaero).
6. **Vajra-Präzisionsmodus**: zusätzlich ausgenommen sind Blöcke, die es 1.4.7 nicht gab und die unzerstörbar sind
  (Härte < 0: Barriere, Licht, Befehls-/Struktur-/Verbundblöcke, Endtransitportal); Abbau feuert das NeoForge-
  `BlockEvent.BreakEvent` (Schutz-Mods greifen).
7. **Schraubendreher**: ausgefahrene Kolben werden nicht gedreht (1.4.7 hätte das Ausgefahren-Bit verworfen und
  einen losen Kolbenkopf hinterlassen); Werfer, die nach oben/unten zeigen (in 1.4.7 nicht möglich): unten → Nord,
  oben → Süd (ergibt sich aus der Formel; korrigiert 27.5).
8. **Hacke**: nur Gras und Erde wie im Pack (keine 1.21-Erweiterungen wie Grobe Erde/Wurzelerde/Trampelpfad).
9. **Fortg. Diamantbohrer**: die leere obere Zeile entfällt (1.21 kürzt Muster), das Rezept ist also verschiebbar.
10. Ladung als Data Component wie bei allen elektrischen Items im Mod.

## 13 Tests (GameTest)

- Rezepte inkl. Ladungsübertrag (Ultimatives LapPack aus geladenem LapPack + Kristallen; Gravitations-Brustplatte
  aus Quanten-Brustplatte + Ultimativem LapPack); Solarhelm ohne Rezept.
- `chargeFromArmor`: Werkzeug lädt aus Fortg. LapPack, Fortg. Jetpack, Gravitations-Brustplatte; nicht aus Solarhelm.
- Jetpack: ohne Triebwerk kein Schub; mit Triebwerk und Sprung `motionY`-Anstieg und −12 EU/Tick; Boost −60 extra;
  Schwebemodus begrenzt Sinken; Leistungsabfall ≤ 1 000 EU.
- Gravitations-Brustplatte: Einschalten erst ab 80 000 EU; −416 EU/Tick in der Luft, am Boden 0 (Abweichung); Abschalten bei < 416 EU;
  Ablegen nimmt `mayfly`; Höhengrenze.
- Schutzwerte (AbsorbMax-Formeln inkl. der zwei ×25-Fehler, Rüstungsanzeige).
- Solarhelm: Erzeugung 512/64/0 nach Sonne/Himmel; Verteilreihenfolge (Rüstung vor Inventar).
- Werkzeuge: Bohrer 160 EU/Block, Effizienz 35; Kettensäge 100 EU/Block, Wolle abbaubar, Treffer 200 EU; Vajra
  3 333 EU/Block, Präzisionsmodus entfernt Block mit Drops, nicht Grundgestein; GraviTool Moduswechsel, Hacke auf
  Erde mit Block darüber, Schraubenschlüssel verlustfrei (10 000 EU), Schraubendreher (Verstärker, Werfer, Kolben).

## 14 Teilpakete (Phase 27)

| Teil | Inhalt |
|---|---|
| 27.1 | Paket, Bauteile, Fortg./Ultimatives LapPack, alle Rezepte der vorhandenen Items, `chargeFromArmor` für alle „liefert Energie“-Teile, Texturen, Lang, HUD-Grundgerüst (Energiezeile) |
| 27.2 | Fortg. Jetpack + Fortg. Nano-Brustplatte (Triebwerk, Schweben, Boost, Flug-Taste, Payloads, Sound, HUD-Status) |
| 27.3 | Gravitations-Brustplatte (Flug, Verbrauch, Boost, Kreativ-/Ablege-/Welt-lade-Logik, Höhengrenze) |
| 27.4 | Fortg. Diamantbohrer, Fortg. Kettensäge, Vajra (inkl. Präzisionsmodus) |
| 27.5 | GraviTool (vier Modi, Modell je Modus, BC-Schraubenschlüssel) |
| 27.6 | Ultimativer Solarhelm; EMI (Info-Seiten), Herkunft, Wiki, Version/Paket |
