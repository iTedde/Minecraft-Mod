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

**Nanosäbel**: Rechtsklick schaltet an (benötigt ≥ 16 EU, Sound) / aus. Aktiv: Schaden 20, Abbaugeschwindigkeit 4,
nur Spinnennetz abbaubar. Verbrauch (`drain` = n·8 EU): pro Treffer 5×8 = 40 EU (zusätzlich gegen Spieler mit
Nano-/Quantenrüstung: je Teil 2×8, und das Rüstungsteil verliert 4800 EU (Nano) bzw. 30 000 EU (Quantum); leere
Rüstungsteile werden zerstört). Abbau: 80 EU. Leerlauf (aktiv): alle 16 Ticks 64×8 = 512 EU in der Hotbar, außerhalb
der Hotbar alle 64 Ticks 16×8 = 128 EU. Bei Energiemangel geht er aus. Inaktiv Schaden 4.
Original-Eigenheit: Der Verbrauch beim Treffen gilt nur, wenn der Angreifer kein Spieler ist **oder PvP aus ist**.

**Scanner**: Rechtsklick scannt die Säule unter dem Spieler bis y = 1 im Quadrat ±2 (OD) bzw. ±4 (OV) und meldet
`1000 · Treffer / gescannteBlöcke` („Ore density“ = Anzahl wertvoller Blöcke; „Ore value“ = Summe der Erzwerte).
Erzwerte: Tabelle in `07-weltgenerierung.md` §4. Im Miner liefert der
Scanner pro Ebene einen Layer-Scan (OD: 50 EU, Radius 2; OV: 250 EU, Radius 4).

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

## 7. Sprengstoffe

| Block/Item | Zünder | Explosion |
|---|---|---|
| Industrie-TNT (ITNT) | Redstone, Feuerzeug, Explosion (verkürzt), Zündschnur 60 Ticks | `ExplosionIC2(5,5, drop 0,9, dmg 0,3)` |
| Atombombe (Nuke, uncommon) | wie TNT, Zündschnur 300 Ticks; mit Wrench entschärfbar (Entity) | `ExplosionIC2(35 = explosionPowerNuke, drop 0,05, dmg 1,5, DamageSource nuke)` + Strahlung/Hunger im Umkreis 100; Platzieren/Zünden wird geloggt |
| Dynamit / Klebedynamit (Item, werfbar) | Wurf, Zündschnur 100 Ticks; Klebedynamit haftet an Blöcken; im Wasser erlischt es (+2000 Ticks) | `PointExplosion` 3×3×3: zerstört Blöcke mit Resistenz ≤ 10, Drop 100 %, Entity-Schaden ähnlich Vanilla (Stärke 1 → 2) |
| Dynamit-Block (gelegt) / mit Fernzünder | Redstone, Fernzünder | Zündschnur 40 Ticks (durch Explosion 5) |

Gezündete IC2-Sprengstoffe springen wie TNT (motionY 0,2).

## 8. Strahlung, Schadensquellen

- Trank-Effekt **Radioaktivität** (`potion.radiation`, ID 24, schlecht, Farbe 0x4E9331): Schaden `stufe+1` alle
  `25 >> stufe` Ticks (DamageSource `radiation`, ignoriert Rüstung).
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
| Eisenzaun (`blockFenceIron`) | 1,5 | 5 | magnetisierbar |
| Verstärkter Stein (`blockAlloy`) | 80 | 150 | Pickaxe 2 |
| Verstärktes Glas | 5 | 150 | |
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
