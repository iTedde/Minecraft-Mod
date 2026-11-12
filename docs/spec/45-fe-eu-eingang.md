# 45 – EU-Eingang für Ad Astra und AE2

Nutzer, 2026-11-12: „EU-Eingang wie bei den Flutlichtern auch für andere FE-Mods, z. B. Ad-Astra-Maschinen oder
AE2-Controller direkt am EU-Kabel“ – „AD Astra mit EU Kompatibel machen klingt gut“. Ausbau von Phase 43
(`spec/43-oml-eu.md`); kein Pack-Vorbild.

## 1. Entscheidungen (Nutzer, 2026-11-12)

| Frage | Entschieden |
|---|---|
| Umfang | **nur Ad Astra und AE2** (feste Blockliste, keine automatische Erkennung anderer Mods) |
| Kurs | **1 EU = 4 FE** wie der FE Producer; die OML-Lichter bleiben bei 1 EU = 1 FE (Phase 43) |
| Spannung | **LV**: höchstens 32 EU pro Paket, darüber explodiert der Block (`Ic2Explosion.explodeMachineAt`, kein Drop) |
| Speicher/Kabel | **ausgenommen** – nur Verbraucher nehmen EU; FE-Akkus und -Kabel bleiben reine FE-Blöcke |
| FE | bleibt zusätzlich möglich |

## 2. Blockliste (Paketversionen Ad Astra 1.16.26, AE2 19.2.18)

### Ad Astra (`ad_astra:`)

| Block | Gerät |
|---|---|
| `compressor` | Kompressor |
| `fuel_refinery` | Treibstoffraffinerie |
| `oxygen_loader` | Sauerstofflader |
| `oxygen_distributor` | Sauerstoffverteiler |
| `water_pump` | Wasserpumpe |
| `cryo_freezer` | Kryo-Gefrierer |
| `gravity_normalizer` | Schwerkraft-Normalisierer |
| `etrionic_blast_furnace` | Etrionischer Hochofen |

Nicht: `coal_generator`, `solar_panel` (Erzeuger), `energizer` (Speicher), `steel_cable`, `desh_cable`, `cable_duct`
(Kabel).

### AE2 (`ae2:`)

Ermittelt aus der Jar: FE nehmen alle Unterklassen von `AEBasePoweredBlockEntity` an.

| Block | Gerät |
|---|---|
| `controller` | ME-Controller |
| `energy_acceptor` | Energieakzeptor (Block) |
| `charger` | Auflader |
| `inscriber` | Prägepresse |
| `growth_accelerator` | Wachstumsbeschleuniger |
| `chest` | ME-Truhe |

Nicht: `cable_bus` (der Energieakzeptor als Kabel-**Part** – sonst würden sich EU-Kabel optisch an jedes AE2-Kabel
hängen; stattdessen den Energieakzeptor-Block nehmen), Energiezellen (nehmen kein FE). AE2 rechnet intern
2 FE = 1 AE, am EU-Kabel also 1 EU = 2 AE.

## 3. Verhalten

- Am Block, an jeder Seite mit FE-Eingang: EU-Senke im Craft-Netz, Bedarf = freier FE-Platz / 4 (abgerundet).
- Ein Paket von n EU (n ≤ 32) wird als bis zu 4·n FE übergeben; angenommen werden nur ganze EU (vorher per
  Simulation geprüft), der Rest geht ans Netz zurück.
- Über 32 EU: Explosion wie eine IC2-Maschine.
- Craft-Kabel verbinden sich optisch mit allen Blöcken der Liste.
- Ohne die jeweilige Mod passiert nichts.

## 4. Umsetzung

`compat/oml/OmlEuInput` wird zu einem gemeinsamen `compat/euinput/ForeignEuInputs` verallgemeinert: Regeln je Mod
(Namespace, Blockliste, FE je EU), gleiche Lebenszyklus-Logik wie Phase 43 (Chunk-Laden, Nachbar-Benachrichtigung,
Prüfung jede Sekunde). Kabelanbindung über `ForeignEuInputs.isEuInput(state)`. GameTests mit Ad Astra und AE2
(Kompressor, ME-Controller: 32 EU → 128 FE; 128 EU → Explosion), Lauf mit den Paket-Jars im eigenen Testordner.

## 5. Teilphasen

45.0 Spezifikation · 45.1 Umsetzung + GameTests · 45.2 EMI-/Wiki-Text, Craft-Version/Paket
