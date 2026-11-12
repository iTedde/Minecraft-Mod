# 43 – EU-Eingang für Open Modular Lighting

Nutzer, 2026-11-11: „Kannst du die Flutlichter auf EU dann umstellen?“ Kein Pack-Vorbild; Open Modular Lighting (OML,
Keridos, CurseForge 268178) ist seit Paket 1.2.5 mitinstalliert und nimmt von sich aus nur FE an.

## Entscheidungen (Nutzer, 2026-11-11)

| Frage | Entschieden |
|---|---|
| Umrechnung | **1 EU = 1 FE** (Flutlicht 10 EU/t, Kegel 20, UV-Flutlicht 60, Pflanzenlicht 25, Röhren 1, Würfellicht 2) |
| Spannung | **LV**: höchstens 32 EU pro Paket; darüber explodiert das Licht wie eine IC2-Maschine (`explodeMachineAt`, kein Drop) |
| FE | bleibt zusätzlich möglich (Mekanism, Crafts & Additions, FE Producer …) |

## Umfang

- Elektrische OML-Lichter: `electric_floodlight`, `uv_floodlight`, `grow_light`, `small_fluorescent_light`,
  `square_fluorescent_light`, `cube_light`. Nicht: `carbon_floodlight` (Brennstoff), `phantom_light`/`uv_light`
  (Strahlblöcke).
- Jede Seite, an der das Licht FE annimmt, nimmt auch EU an. Bedarf = freier Platz im FE-Speicher des Lichts (50 000).
- Craft-Kabel verbinden sich optisch mit den Lichtern.
- Ohne OML passiert nichts.

## Umsetzung

`compat/euinput/ForeignEuInputs` (bis Phase 45 `compat/oml/OmlEuInput`): je Licht ein `EnergySink`-Stellvertreter im EU-Netz (positionsbasiert), angemeldet bei
Chunk-Laden und bei jeder Blockänderung mit Nachbar-Benachrichtigung (Setzen, Abbauen, Befehle, Kolben,
Contraptions), abgemeldet bei Chunk-Entladen, Blockänderung und einer Prüfung jede Sekunde. EU werden per
`IEnergyStorage.receiveEnergy` 1:1 als FE übergeben. Kabel: `CableBlock.connects`/`CableBlockEntity.canInteractWith`
fragen `ForeignEuInputs.isEuInput`.

Abweichung: OML verlangt NeoForge ≥ 21.1.264 – das Paket läuft seit 1.2.5 auf 21.1.265.
