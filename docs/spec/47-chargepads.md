# 47 – ChargePads

Quelle: `ftb ultimate/mods/ChargePads.zip` (**Charge Pads 2.4.1 build 66** von Myrathi, für MC 1.4.7 / IC2 1.115),
Pack-Config `ftb ultimate/config/ChargePads.cfg` (alle Schalter auf Standard: `allowEmitterUpgrade=true`,
`dischargeZap=true`, `fallingZap=true`, `upgradeAnySide=false`, `upgradeZap=true`, `upgradeZapModifier=1.0`).
Dekompiliert (2026-11-12, Vineflower ohne Remapping, Minecraft- und Mod-Namen verschleiert):
`reference/decompiled/chargepads/myrathi/ic2/chargepads/` – `e` (Mod, Config), `d` (Pad-Stufen, Pad-Rezepte),
`f` (Block), `w` (Block-Entity-Basis: Energie, Upgrades, Wrench), `ac`/`ad`/`ab` (Static/Crystalizor/Lapotronic:
Laden, Partikel), `l`/`m`/`k` (Menü, Modulslot, GUI), `o`/`q` (Ausbausätze), `r` (Effizienz-Upgrade), `s`/`u`
(Emitter-Module), `h`/`i` (Schadensarten), `j` (Partikel). Dazu `README.ChargePads.txt` (Anleitung, Rezeptbilder).
Assets im Zip: `gfx/blocks.png` (16×16-Raster mit Block- und Item-Icons), `gfx/GUIChargePad.png`.
GregTech greift in keine ChargePads-Rezepte ein (keine Treffer in `reference/decompiled/gregtech/`).

Obfuskierte Vanilla-Namen über `ftb ultimate/instmods/minecraft.jar` aufgelöst (statischer Initialisierer):
`amq.aN` Steindruckplatte, `up.aC` Redstone, `up.aT` Glowstonestaub, `up.bn` Enderperle, `ll.u` Vergiftung,
`agi.f` Material Eisen, `amq.i` Stein-Geräusch; `ke.f` = aufrunden, `ke.c` = abrunden; `md` = `EntityLiving`,
`qx` = `EntityPlayer`, `iq` = `EntityPlayerMP`.

Nutzerwunsch (2026-11-12): „ChargePads“ aus dem Pack. Paket `dev.craft.chargepads`.

## 1 Übersicht

| Registry `craft:…` | Name EN / DE | Stapel | Seltenheit |
|---|---|---|---|
| `static_charge_pad` | Static Charge Pad / Statisches Ladepad | 4 | normal |
| `crystalizor_charge_pad` | Crystalizor Charge Pad / Kristallisator-Ladepad | 4 | normal |
| `lapotronic_charge_pad` | Lapotronic Charge Pad / Lapotron-Ladepad | 4 | ungewöhnlich |
| `crystalizor_upgrade_kit` | Crystalizor Upgrade Kit / Kristallisator-Ausbausatz | 4 | normal |
| `mfs_unit_upgrade` | MFS Unit Upgrade / MFSU-Aufrüstung | 4 | normal |
| `lapotronic_upgrade_kit` | Lapotronic Upgrade Kit / Lapotron-Ausbausatz | 4 | ungewöhnlich |
| `efficiency_upgrade` | Efficiency Upgrade / Effizienz-Upgrade | 64 | normal |
| `drain_conversion_module` | Drain Conversion Module / Entladungs-Umwandlungsmodul | 64 | normal |
| `damage_conversion_module` | Damage Conversion Module / Schadens-Umwandlungsmodul | 64 | ungewöhnlich |
| `armour_priority_module` | Armour Priority Module / Rüstungsvorrang-Modul | 64 | normal |
| `proximity_booster_module` | Proximity Booster Module / Nahbereichs-Verstärkermodul | 64 | normal |
| `wide_band_booster_module` | Wide-Band Booster Module / Breitband-Verstärkermodul | 64 | ungewöhnlich |
| `field_expansion_module_alpha` | Field Expansion Module (Alpha) / Feldausdehnungsmodul (Alpha) | 64 | normal |
| `field_expansion_module_delta` | Field Expansion Module (Delta) / Feldausdehnungsmodul (Delta) | 64 | normal |
| `field_expansion_module_theta` | Field Expansion Module (Theta) / Feldausdehnungsmodul (Theta) | 64 | ungewöhnlich |

Im Original ein Block mit Meta 0–2 (Bit 8 = aktiv), ein Kit-Item (Meta 0–2, Meta 3 „Unknown Upgrade“ ohne
Tab), ein Effizienz-Item und ein Modul-Item (Meta 0–7, Meta 8 „Unknown Module“). In 1.21 eigene Registry-Namen
je Variante, „Unknown …“ entfällt (Abweichung). Im Pack eigener Kreativ-Tab „Charge Pads“ (Symbol Lapotron-Pad) –
siehe §11.

## 2 Pad-Stufen

| Stufe | Pad | Speicher | Max. Eingang (= Laderate) | Fallgrenze | Fallschaden | Zap-Radius min. | Zap-Formel (r, s) | Plätze |
|---|---|---|---|---|---|---|---|---|
| 1 | Static | 40 000 EU | 32 EU/t | > 3,5 | 3 | 3 | −10, 5 | Batterie + 1 Upgrade |
| 2 | Crystalizor | 600 000 EU | 128 EU/t | > 2,5 | 6 | 6 | 0, 50 | Batterie + Projektor + Feld + 2 Upgrades |
| 3 | Lapotronic | 10 000 000 EU | 512 EU/t | > 1,5 | 9 | 10 | 0, 500 | Batterie + Projektor + Feld + 3 Upgrades |

Die Stufe ist zugleich die **Ladestufe** (Items mit höherer Stufe werden nicht geladen) und die Stufe des
Batterieslots.

### 2.1 Block

- Flach: Kollision und Auswahl 16×4×16 px (0–0,25 Höhe). Nicht undurchsichtig, kein voller Würfel.
  Nur die **Unterseite** ist eine feste Fläche (`isBlockSolidOnSide` nur DOWN).
- Härte 1,5, Material Eisen, Stein-Geräusch. `canHarvestBlock = false`: **Abbauen ohne Schraubenschlüssel
  droppt den Pad nie** (auch nicht mit Spitzhacke), §6.
- Zustand `active` (Original Meta-Bit 8). Texturen: Unterseite Icon 0/1/2, Seiten 32/33/34, Oberseite inaktiv
  16/17/18, aktiv 48/49/50 (je Static/Crystalizor/Lapotronic, aus `blocks.png`).
- Rechtsklick (nicht schleichend) öffnet das GUI. Ist ein **Entladungs- oder Schadensmodul** eingesetzt, öffnet
  sich das GUI nur beim Klick auf die **Unterseite** (sonst passiert nichts, Klick gilt als verbraucht).
- Schraubenschlüssel (IC2-Schraubenschlüssel/Elektroschlüssel, `IWrenchable`): nicht drehbar (Ausrichtung immer
  oben), Abbau immer erlaubt mit Drop-Rate 1,0 → Pad-Item (ohne gespeicherte Energie, Energie geht verloren);
  mit Entladungs-/Schadensmodul nur, wenn der Spieler auf die Unterseite zielt (Blickstrahl 5 Blöcke).
  Inventar fällt heraus (§6).

## 3 Energie

- EU-Senke, nimmt **nur von unten** an (`acceptsEnergyFrom` nur YN). Kein Ausgang.
- `demandsEnergy` = Kapazität − Speicher. Max. sicherer Eingang = Basis × 4^Transformer (32 → 128 → 512 → 2048 →
  8192 bei Static). Paket größer → **IC2-Maschinenexplosion** (`explodeMachineAt`; Fallback im Original
  Explosion Stärke 1 + Stufe-Index).
- `injectEnergy`: passt das Paket nicht ganz, wird der Rest gespeichert und `Paket − max(1, frei)` zurückgegeben
  (IC2-Eigenheit, wie andere Speicher übernehmen).
- Kapazität = Basis + Stufe × 10 000 × Speicher-Upgrades (Static +10 000, Crystalizor +20 000, Lapotronic
  +30 000 je Upgrade). Sinkt die Kapazität (Upgrade entnommen), wird der Speicher gekappt.
- **Batterieslot** (Slot 0, IC2-`SlotDischarge` mit Pad-Stufe): nimmt Redstone, Einweg-Batterie und entladbare
  Elektro-Items bis zur Pad-Stufe. Nur wenn der Pad Energie braucht und sich in diesem Tick die Upgrades **nicht** geändert
  haben (Kurzschluss-Auswertung im Original):
  - Elektro-Item mit `canProvideEnergy`: entlädt bis zur freien Kapazität, Stufe **Pad-Stufe + Transformer**,
    Transferlimit beachtet.
  - Redstone: 500 EU, Einweg-Batterie: 1 000 EU – je 1 Stück, nur wenn die volle Menge in den Speicher passt.

## 4 Aktivierung, Feld und Redstone

- Betritt ein Wesen den Block (Kollision mit dem Blockraum) und der Pad ist inaktiv, prüft er; danach prüft er alle
  **10 Ticks** (geplanter Tick) und zusätzlich bei zufälligen Ticks, solange aktiv.
- Prüfbereich: **Spieler** im Quader x+0,125 … x+0,875, y … y+0,5, z+0,125 … z+0,875; ist der Pad aktiv und
  hat Feldausdehnung n, wird der Quader horizontal um n Blöcke vergrößert.
- Spieler gefunden, Pad inaktiv → aktiv, Klickgeräusch (`random.click`, Lautstärke 0,3, Tonhöhe 0,6).
  Aktiv und kein Spieler (oder der kleine Quader leer **und** der Pad meldet „nichts zu laden“) → inaktiv,
  Klick Tonhöhe 0,5, „nichts zu laden“ zurückgesetzt.
- Bei jedem Wechsel: Nachbar-Updates unten und an den 4 Seiten, Block neu zeichnen.
- **Redstone**: aktiv → starkes und schwaches Signal 15 nach unten und an alle 4 Seiten (nicht nach oben),
  unabhängig von gespeicherter Energie. Redstone verbindet sich von allen Seiten.
- **Feld**: Bereich = Block-Kollisionsbox (16×4×16 px) um n horizontal und **0,5 vertikal** vergrößert
  (n = Feldausdehnung 0/1/2/3). Erfasst werden alle Lebewesen (`EntityLiving`); geladen werden nur Spieler.

## 5 Laden (je Tick, nur Server, nur aktiv)

1. **Grundverbrauch** jeden Tick, solange aktiv: Stufe + 2 × Feldausdehnung + max(0, Übertakter − Effizienz) EU
   (Speicher nicht unter 0).
2. Nur wenn danach **Speicher ≥ 32**: Lebewesen im Feld suchen. Keine → nichts.
3. Laderate R = min(Speicher, ⌊Basis × 1,05^Übertakter⌋) (Basis 32/128/512). Effizienzfaktor k = 0,995^Effizienz.
4. **Schadensmodul** (wenn `allowEmitterUpgrade`): alle Lebewesen im Feld erhalten 1 Schaden „emitter radiation“
   (Rüstung wirkt; Unverwundbarkeits-Ticks wie Vanilla) und, falls noch nicht vergiftet, **Vergiftung**
   Dauer Stufe × 20 Ticks, Stärke Stufe × 4 (Static Gift V 1 s, Crystalizor Gift IX 2 s, Lapotronic Gift XIII
   3 s). Verbrauch: ⌈R × k⌉ EU je Tick. Kein Laden.
5. Sonst je **Spieler** (nur echte Server-Spieler) Items sammeln, die die Pad-Stufe nicht übersteigen und sich
   laden (bzw. mit Entladungsmodul entladen) lassen:
   - Hand: das gehaltene Hotbar-Fach; mit **Nahbereich** zusätzlich die Fächer links/rechts daneben (Fach 0–8,
     am Rand abgeschnitten); mit **Breitband** alle 9 Hotbar-Fächer.
   - Rüstung: alle 4 Rüstungsplätze.
   - Reihenfolge: ohne Rüstungsvorrang zuerst Hand; **nur wenn dort nichts Ladbares ist**, Rüstung. Mit
     Rüstungsvorrang umgekehrt. (Es wird je Spieler also immer nur eine Gruppe bedient – Originalverhalten.)
   - Liste leer → Pad meldet „nichts zu laden“ (§4).
6. Verteilung über alle gesammelten Items (alle Spieler zusammen) der Reihe nach: Item i bekommt
   ⌊R_rest / (Anzahl − i)⌋ EU angeboten (Ganzzahldivision, das `ceil` im Original wirkt nicht), geladen mit
   Pad-Stufe und **ohne Transferlimit**. Verbraucht werden ⌈geladen × k⌉ EU; R_rest sinkt um denselben Wert.
   Mit **Entladungsmodul** wird stattdessen entladen; die entnommene Energie geht verloren und kostet trotzdem
   ⌈entladen × k⌉ EU aus dem Speicher (Originalverhalten).
7. Mehrere Pads können denselben Spieler gleichzeitig laden (Felder überlappen).

### 5.1 Partikel (nur Client, Anzeige)

Bei aktivem Pad mit ≥ 4 EU Speicher: je Spalte (1 + 2n)² Positionen über der Fläche (Breite 0,9 + 2n) steigen
farbige Funken auf. Static 6 je Position, Lebensdauer ~5, Steiggeschwindigkeit 3,0 (×0,2, Dämpfung 0,99);
Crystalizor 5 je Position, Lebensdauer ~14, Steigen 7,6; Lapotronic 6, ~14, 7,6. Farbe: normal grün (R 0,
G 0,6–1,0, B 0; Lapotronic B = G → türkis); Entladungsmodul gelb/orange (R 1, G 0,6–1,0); Schadensmodul rot
(R 1, G 0). Größe 0,02, kein Kollisionstest. In 1.21 eigener Partikeltyp mit Farbe (Abweichung: Zahlenwerte sinngemäß).

## 6 Gefahren

Schadensart **„electrocution“** (Todesmeldung „%1$s died from shock“, **ignoriert Rüstung**); „emitter
radiation“ (Todesmeldung „%1$s died from emitter radiation“, Rüstung wirkt). Kreativspieler sind beim Abbau
ausgenommen.

- **Fallen** (`fallingZap`): Lebewesen mit Fallhöhe über der Fallgrenze (§2) bekommen beim Aufkommen den
  Fallschaden der Stufe (3/6/9) als electrocution, zusätzlich zum normalen Fallschaden.
- **Abbau ohne Schraubenschlüssel** durch Nicht-Kreativ-Spieler (`dischargeZap`):
  - Entladungsschaden D = ⌈max(0, ⌈Speicher / 1000⌉ − r) / s⌉ (r, s aus §2). Beispiele: Static leer 2, voll 10;
    Crystalizor voll 12; Lapotronic voll 20.
  - Radius = max(D, Mindestradius 3/6/10); alle Lebewesen in der um den Radius vergrößerten Blockbox erhalten
    D − ⌊Abstand zur Blockecke⌋ (≤ 0 → kein Schaden).
  - Der Abbauende bekommt zusätzlich 0 / 2 / 4 (Static/Crystalizor/Lapotronic).
  - Danach verliert jeder Stapel im Inventar zufällig 0 … Anzahl Stück; der Rest fällt heraus. Der Pad selbst
    droppt nicht.
- Inventar fällt beim Entfernen immer heraus (Streuung 0,7, Abholverzögerung 10 Ticks).

## 7 Upgrades und Module

Upgrade-Plätze (IC2-`SlotCustom`): IC2-Upgrades (Übertakter, Energiespeicher, Transformer) und Effizienz-Upgrade,
Stapel 16. Gezählt wird über alle Upgrade-Plätze:

| Upgrade | Max. wirksam | Wirkung |
|---|---|---|
| Übertakter | 16 | Laderate × 1,05^n; Grundverbrauch + n (abzüglich Effizienz) |
| Effizienz | 16 | hebt Übertakter-Mehrverbrauch auf; Ladekosten × 0,995^n |
| Energiespeicher | – (unbegrenzt) | + Stufe × 10 000 EU |
| Transformer | 4 | Eingang × 4^n; Batterieslot-Stufe + n |

Modulplätze (nur Crystalizor/Lapotronic, je 1 Stück, nur Module bis zur Pad-Stufe):

| Modul | Platz | Mindeststufe | Wirkung |
|---|---|---|---|
| Entladungs-Umwandlung | Feld | 2 | Feld entlädt statt zu laden |
| Schadens-Umwandlung | Feld | 2 | Feld verletzt + vergiftet (§5 Schritt 4) |
| Rüstungsvorrang | Feld | 2 | erst Rüstung, dann Hand |
| Nahbereich | Projektor | 2 | Hand ± 1 Fach |
| Breitband | Projektor | 2 | ganze Hotbar |
| Feldausdehnung Alpha / Delta / Theta | Projektor | 2 / 3 / 3 | Feld +1 / +2 / +3 Blöcke horizontal; Grundverbrauch +2/+4/+6 |

Mit `allowEmitterUpgrade = false` (nicht im Pack) entfielen Entladungs-/Schadensmodul samt Rezept – nicht umgesetzt.
Modul-Tooltip: „Restricted: Emitter Slot“, „Modulation: Projector/Field“, Feldausdehnung zusätzlich
„Strength: Alpha/Delta/Theta“ (Farbe grau/türkis/rot).

## 8 Ausbausätze

- **Kristallisator-Ausbausatz** macht Static → Crystalizor, **Lapotron-Ausbausatz** Crystalizor → Lapotronic.
  Die MFSU-Aufrüstung ist nur Zwischenprodukt (Rechtsklick auf den Pad tut nichts).
- Im Werkbankgitter formlos: Pad + passender Satz → nächstes Pad.
- Rechtsklick auf die **Unterseite** des passenden Pads (`upgradeAnySide=false`), falscher Pad → Klick verbraucht,
  nichts passiert. Nur Server:
  - Ist der Pad aktiv (`upgradeZap`): Meldung „The pad is active and electrocutes you!“ und electrocution
    max(2 × Stufe, ⌊D × 1,0⌋) (D aus §6 mit alter Stufe und aktuellem Speicher).
  - Block wird zum nächsten Pad, **Speicher bleibt**, Batterieslot bleibt. Hatte der alte Pad Modulplätze, bleiben
    Projektor/Feld/Upgrades auf ihren Plätzen. Beim Static → Crystalizor landet das Upgrade aus Static-Platz 1
    auf **Crystalizor-Platz 2 (Feld)** – dort wirkt es nicht, es lässt sich aber herausnehmen (Originalfehler,
    1:1). Was keinen Platz findet, fällt nach unten heraus.
  - Satz wird verbraucht (außer Kreativ).

## 9 GUI

Titel = Pad-Name; „EU  : <Speicher>“, „Max.: <Kapazität>“, klein „@ <Eingang>/t“; Energiebalken (12×26 px) links;
Batterieslot (8,58); Upgrade-Plätze (71,39), (61,58), (81,58) – nicht vorhandene ausgegraut; Modulplätze Feld
(134,29) und Projektor (134,58) mit kleiner Beschriftung „Field“/„Projector“, beim Static ausgegraut.
Spielerinventar ab (8,84). Shift-Klick wie im Original: aus dem Pad ins Inventar; aus dem Inventar zuerst in einen
passenden Pad-Platz (rückwärts gesucht), sonst Haupt-↔ Hotbar. Speicher als 2 × 16 Bit synchronisiert.

## 10 Rezepte (geformt, IC2-Rezepte)

| Ergebnis | Muster | Schlüssel |
|---|---|---|
| Statisches Ladepad | `rSr` / `c#c` | r Gummi, S Steindruckplatte, c Elektronische Schaltung, # BatBox |
| Kristallisator-Ladepad | `rSr` / `c#c` | r Gummi, S Steindruckplatte, c Elektronische Schaltung, # MFE |
| Lapotron-Ladepad | `rSr` / `c#c` | r Kohlenstoffplatte, S Steindruckplatte, c Fortgeschrittene Schaltung, # MFSU |
| Kristallisator-Ausbausatz | `W` / `*` / `#` | W Elektroschlüssel, * Elektronische Schaltung, # MFE |
| MFSU-Aufrüstung | `O*O` / `OYO` / `O#O` | O Lapotronkristall, * Fortg. Schaltung, Y Schraubenschlüssel, # Fortgeschrittenes Maschinengehäuse |
| MFSU-Aufrüstung | `Y` / `M` | Y Schraubenschlüssel, M MFSU |
| Lapotron-Ausbausatz | `*W*` / `@#@` | * Fortg. Schaltung, W Elektroschlüssel, @ Kohlenstoffplatte, # MFSU-Aufrüstung |
| Kristallisator-Ladepad (formlos) | Statisches Ladepad + Kristallisator-Ausbausatz | |
| Lapotron-Ladepad (formlos) | Kristallisator-Ladepad + Lapotron-Ausbausatz | |
| Effizienz-Upgrade | `BfB` / `g#g` | B **volles** RE-Akku, f Glasfaserkabel, g 2× isol. Goldkabel, # Fortg. Schaltung |
| Nahbereichs-Verstärkermodul | `AAA` / `^s^` / `gAg` | A Fortg. Schaltung, ^ Übertakter, s Verteilerkabel, g 2× isol. Goldkabel |
| Breitband-Verstärkermodul | `A@A` / `*s*` / `A@A` | A Fortg. Schaltung, @ Nahbereichsmodul, * Energiekristall, s Verteilerkabel |
| Feldausdehnung Alpha | `NdN` / `sWs` / `NdN` | N Neutronenreflektor, d Detektorkabel, s Verteilerkabel, W Breitbandmodul |
| Feldausdehnung Delta | `1e1` / `sAs` / `1e1` | 1 Alpha, e Effizienz-Upgrade, s Verteilerkabel, A Fortg. Schaltung |
| Feldausdehnung Theta | `T^T` / `2s2` / `eAe` | T Dicker Neutronenreflektor, ^ Übertakter, 2 Delta, s Verteilerkabel, e Effizienz-Upgrade, A Fortg. Schaltung |
| Rüstungsvorrang-Modul | `vEv` / `g@g` | v Glowstonestaub, E Enderperle, g 2× isol. Goldkabel, @ Nahbereichsmodul |
| Schadens-Umwandlungsmodul | `UUU` / `g#g` | U Fast leere Uranzelle, g 2× isol. Goldkabel, # Fortg. Schaltung |
| Entladungs-Umwandlungsmodul | `vvv` / `g#g` | v Glowstonestaub, g 2× isol. Goldkabel, # Fortg. Schaltung |

„Volles RE-Akku“: nur ein voll geladenes RE-Akku passt (Original: `chargedReBattery` mit Schaden = max.,
1.4.7-Darstellung „voll“). Das Ergebnis-Pad aus formlosen Rezepten ist leer (Pad-Items tragen keine Energie).
Alle Bauteile gibt es in Craft bereits (IC2-Phasen 3/6/7).

## 11 Entscheidungen (Nutzer, 2026-11-12: alle Vorschläge übernommen)

1. **Kreativ-Tab**: IC2-Tab (wie Nuclear Control), kein eigener Tab.
2. **Welche Items werden geladen**: alle Craft-Elektro-Items (`ElectricItem`, also IC2, GraviSuite, MPS,
   Leuchtschild …) bis zur Pad-Stufe – keine FE-Items anderer Mods.
3. **Gefährliche Teile** (Fall-/Abbau-/Ausbau-Zap, Schadens- und Entladungsmodul): wie im Pack an
   (Schalter als Konstanten in `ChargePadsConfig`).

## 12 Abweichungen (1.21)

- Ein Block/Item je Variante statt Metadaten; „Unknown Upgrade/Module“ entfallen; aktiv als Blockzustand.
- Texturen einzeln aus `blocks.png` ausgeschnitten, GUI aus `GUIChargePad.png`.
- Schadensarten als Datapack-Damage-Types (`craft:electrocution` mit `bypasses_armor`, `craft:emitter_radiation`).
- Vergiftung Stärke Stufe × 4 als Effekt-Amplifier wie im Original.
- Partikel als eigener farbiger Partikeltyp, Zahlenwerte sinngemäß.
- Nur Spieler werden geladen (Original nur `EntityPlayerMP`, in 1.21 jeder `ServerPlayer` – gleich).
- Inventar einheitlich 6 Plätze (Batterie, Projektor, Feld, 3 Upgrades); der Static nutzt Platz 0 und 1 (Upgrade),
  Crystalizor/Lapotronic die Upgrade-Plätze 3–4 bzw. 3–5. Die Zuordnung beim Ausbau (§8) folgt in 47.2.
- Klickgeräusch `random.click` → Steinknopf-Klick (`block.stone_button.click_on`), Lautstärke/Tonhöhe wie im Original.
- Abbauen dauert immer wie ohne passendes Werkzeug (`requiresCorrectToolForDrops`, in keinem `mineable`-Tag) –
  wie `canHarvestBlock = false` im Original; Drop nie (Loot-Tabelle leer), nur der Schraubenschlüssel gibt den Pad.

## 13 Teilphasen

- **47.1** Pads: Block (flach, aktiv, Redstone), Block-Entity (Energie von unten, Speicher, Batterieslot,
  Explosion), Feld + Laden mit IC2-Upgrades, GUI/Menü, Gefahren (Fall-/Abbau-Zap, Schadensarten), Wrench,
  Pad-Rezepte, Texturen, GameTests.
- **47.2** Effizienz-Upgrade, alle Emitter-Module (Projektor/Feld, Schadens-/Entladungsfeld, Feldausdehnung),
  Ausbausätze (Werkbank + Rechtsklick, Ausbau-Zap), Partikel, Rezepte, GameTests.
- **47.3** EMI (Info-Seiten), Herkunft, Wiki, Testplan, Craft-Version + Paket.
