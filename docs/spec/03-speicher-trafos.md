# 03 – Energiespeicher und Transformatoren

Quelle: `ic2.core.block.wiring.TileEntityElectricBlock`, `TileEntityElectric{BatBox,MFE,MFSU}`,
`TileEntityTransformer{,LV,MV,HV}`, `BlockElectric`.

Block `blockElectric` (Material Eisen, Härte 1,5, Metall-Sound). Meta: 0 BatBox, 1 MFE, 2 MFSU, 3 LV-Trafo,
4 MV-Trafo, 5 HV-Trafo. Drop ohne Wrench: BatBox und LV-Trafo droppen sich selbst; **MFE, MFSU, MV-, HV-Trafo droppen
einen Maschinenblock** (Machine Casing). Wrench-Drop-Rate 1,0 (immer erfolgreich). Rarity: MFSU und HV-Trafo
„uncommon“.

Platzierung (alle sechs): Facing zeigt zum Spieler, **inkl. oben/unten**: Pitch ≥ 65° (Spieler schaut nach unten)
→ Facing 1 (oben), Pitch ≤ −65° → Facing 0 (unten), sonst horizontal nach Yaw-Quadrant
(`floor(yaw·4/360 + 0,5) & 3`: 0→2, 1→5, 2→3, 3→4). Ohne Platzierer (z. B. Deployer) Facing 1.
Wrench kann das Facing auf jede andere Seite drehen. Facing-Änderung meldet das Tile im Netz ab/an.

## Speicherblöcke

| | BatBox | MFE | MFSU |
|---|---:|---:|---:|
| Tier | 1 | 2 | 3 |
| Kapazität (EU) | 40 000 | 600 000 | 10 000 000 |
| Ausgabe (EU/p, 1 Paket/Tick) | 32 | 128 | 512 |
| Max. sichere Eingabe | 32 | 128 | 512 |

- **Ausgabe** nur auf der **Frontseite** (Facing); **Eingabe** auf den anderen 5 Seiten.
- `injectEnergy`: Paket > Ausgabe → Explosion (`explodeMachineAt`). Sonst nimmt der Block an, bis
  `energy = maxStorage + output − 1` (kann also leicht über die Nennkapazität füllen).
- `demandsEnergy = maxStorage − energy`.
- Pro Tick:
  1. Slot 0 (oben, „Laden“): elektrisches Item mit eigenem Tier laden (`charge(stack, energy, tier, false, false)`,
     Transferlimit des Items gilt).
  2. Slot 1 (unten, „Entladen“): wenn `demandsEnergy > 0`: elektrisches Item mit `canProvideEnergy` entladen
     (`discharge(stack, maxStorage − energy, tier, false, false)`); sonst Redstone (+500) oder SU-Battery (+1000),
     wenn es vollständig passt.
  3. Wenn `energy ≥ output` und Redstone-Modus es erlaubt: ein Paket `output`; zurückgegebener Rest wird nicht
     abgezogen.
  4. Redstone-Ausgabe aktualisieren (mit 5-Tick-Entprellung).
- Sided Inventory: Unterseite → Slot 1 (Entladen), alle anderen Seiten → Slot 0 (Laden).

### Redstone-Modi (Knopf in der GUI, zyklisch 0–5)

| Modus | Bedeutung |
|---:|---|
| 0 | Nichts |
| 1 | Redstone-Signal, wenn voll (`energy ≥ maxStorage`) |
| 2 | Signal, wenn teilweise gefüllt (`output < energy < maxStorage`) |
| 3 | Signal, wenn leer (`energy < output`) |
| 4 | Keine Energieabgabe, solange Redstone anliegt |
| 5 | Keine Energieabgabe bei Redstone, außer wenn voll |

Chat-Meldungen: „Redstone Behavior: Nothing / Emit if full / Emit if partially filled / Emit if empty /
Do not output energy / Do not output energy unless full“.

Signal: schwaches Redstone auf allen Seiten (`isProvidingWeakPower`), Block wird „aktiv“ (Textur).
Änderung des Ausgangszustands erst nach 5 aufeinanderfolgenden Ticks mit abweichendem Sollwert.

NBT: `energy` (int), `active`, `redstoneMode` (byte), Inventar. Die Energie bleibt beim Abbau **nicht** im Item
erhalten (in 1.115 kein Energie-NBT im Item).

### Teleporter-Schnittstelle

Speicherblöcke implementieren `IEnergyStorage` (`getStored/setStored/addEnergy/getCapacity/getOutput`,
`isTeleporterCompatible = true` für alle Seiten). Der Teleporter zieht Energie direkt aus angrenzenden Speichern
(siehe `04-maschinen.md`). Der Elektrolyseur greift ebenfalls direkt auf `energy` zu.

## Transformatoren

| | LV-Trafo | MV-Trafo | HV-Trafo |
|---|---:|---:|---:|
| Niedrige Spannung (lowOutput) | 32 | 128 | 512 |
| Hohe Spannung (highOutput) | 128 | 512 | 2048 |
| Interner Puffer (maxStorage) | 256 | 1024 | 4096 |

### Modus „Abwärts“ (Standard, kein Redstone)

- **Eingang**: nur die **Frontseite** (Facing). Max. sichere Eingabe = `highOutput`; **Ausnahme HV-Trafo:
  unbegrenzt** (`Integer.MAX_VALUE`, da highOutput = 2048).
- **Ausgang**: die 5 anderen Seiten, pro Tick bis zu **4 Pakete** à `lowOutput` (solange `energy ≥ lowOutput`).
- `getMaxEnergyOutput = lowOutput`.

### Modus „Aufwärts“ (Redstone an `isBlockIndirectlyGettingPowered`)

- **Eingang**: die 5 Nicht-Front-Seiten, max. sichere Eingabe = `lowOutput`.
- **Ausgang**: Frontseite, pro Tick **1 Paket** à `highOutput` (wenn `energy ≥ highOutput`).
- `getMaxEnergyOutput = highOutput`. Block ist „aktiv“ (Textur).

Allgemein:
- `injectEnergy`: Paket > maxSafeInput → Explosion. Sonst Annahme bis `energy = maxStorage + highOutput − 1`.
- `demandsEnergy = maxStorage − energy`.
- Redstone-Wechsel meldet den Trafo im Netz ab und wieder an (Pfad-Caches werden neu berechnet), Pufferinhalt bleibt.
- Nicht verteilte Energie bleibt im Puffer.
- NBT: `energy`. Der Redstone-Zustand wird **nicht** gespeichert: nach dem Laden ist er `false`, der erste Tick liest
  ihn neu (`updateRedstone` zu Beginn jedes Ticks) und meldet den Trafo bei Bedarf um.
- Kein Inventar, keine GUI (Rechtsklick ohne Wirkung).

Rezepte: siehe `08-rezepte.md`.
