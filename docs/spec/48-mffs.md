# 48 – Modular Force Field System (MFFS)

Quelle: `ftb ultimate/mods/modularforcefieldsystem.zip` (**Modular ForceField System V2 2.2.8.3.6** von Thunderdark,
für MC 1.4.7 / IC2), Pack-Config `ftb ultimate/config/ModularForceFieldSystem.cfg`. Dekompiliert (2026-11-12,
Vineflower ohne Remapping, Mod-Namen lesbar, Minecraft-Namen verschleiert): `reference/decompiled/mffs/chb/mods/mffs/`
– `common/ModularForceFieldSystem` (Config, Registrierung), `MFFSMaschines`/`ProjectorTyp`/`ProjectorOptions`/
`MFFSRecipes`/`RecipesFactory` (Rezepte), `tileentity/*` (Maschinen), `block/BlockForceField` + `WorldMap`/
`ForceFieldBlockStack` (Feldblöcke), `modules/*` (Feldformen), `options/*` (Projektor-Upgrades), `item/*`, `multitool/*`,
`SecurityHelper`/`SecurityRight` (Rechte), `container/*` + `client/gui/*` (GUIs), `MFFSWorldGenerator`.
Assets: `chb/mods/mffs/sprites/` – Pack nutzt `GraphicStyle=1` („UE-Thema“): Blocktexturen `*_ue.png` (32×32-Raster),
Items/Knöpfe `items.png`, Feldblöcke `blocks.png`, Erz `forciciumore.png`, GUIs `Gui*.png`.

Universal Electricity ist **nicht** im Pack → nur die IC2-Rezeptvarianten gelten; MJ-Eingang des Extraktors (BuildCraft
im Pack) gilt. Thermal Expansion ist im Pack → Pulverisierer-Rezept gilt. EE3 ist im Pack, in Craft aber nicht (§9.3).

Vanilla-Namen über `ftb ultimate/instmods/minecraft.jar` aufgelöst: `up.bn` Enderperle, `up.g` Eisenspitzhacke, `up.aw`
Eimer, `up.ay` Lavaeimer, `up.ax` Wassereimer, `up.aX` Knochen, `up.bo` Lohenrute, `up.bm` verrottetes Fleisch, `up.n`
Diamant, `up.bu` Spinnenauge, `amq.as` Obsidian, `amq.P` Glas, `up.aC` Redstone, `amq.aM` Hebel, `up.aK` Papier;
`md` Lebewesen, `qx` Spieler, `qj` Monster, `qo` Schleim, `qg` Ghast; `lh.h()` = Schaden ignoriert Rüstung.

Pack-Config (maßgeblich): `forcefieldblockcost=1`, `forcefieldblockcreatemodifier=10`, `forcefieldblockzappermodifier=2`,
`forcefieldmaxblockpeerTick=5000`, `forcefieldtransportcost=10000`, `forcefieldremoveonlywaterandlava=false`,
`ExtractorPassForceEnergyGenerate=12000`, `ForceciumWorkCylce=250`, `ForceciumCellWorkCylce=230`,
`DefenceStationKillForceEnergy=10000`, `DefenceStationSearchForceEnergy=1000`, `DefenceStationScannForceEnergy=10`,
`DefenceStationNPCScannnotification=false`, `MonazitOreWorldGen=4`, `Chunkloader=true`, `ForceFieldMaster=nobody`,
`adventuremap=false`, `uumatterForcicium=false`, `renderZapperParticles=true`, `influencedbyothermods=true`.

Nutzerwunsch (2026-11-12): „Modular Force Field System“ aus dem Pack. Paket `dev.craft.mffs`.

## 1 Übersicht

**Kraftenergie (Force Energy, „FE“)** ist die interne Energie von MFFS (nicht Forge Energy, nicht umrechenbar außer
über Extraktor/Konverter, §3/§4). Ganzzahlig; Speicher = Kondensator; Geräte holen sie drahtlos über eine
**Power-Link-Karte** (§6.2).

### 1.1 Blöcke

| Registry `craft:…` | Name EN / DE | Original |
|---|---|---|
| `mffs_projector` | MFFS Projector / MFFS-Projektor | `MFFSProjector` |
| `mffs_extractor` | MFFS Extractor / MFFS-Extraktor | `MFFSExtractor` |
| `mffs_capacitor` | MFFS Capacitor / MFFS-Kondensator | `MFFSCapacitor` |
| `mffs_converter` | MFFS Converter / MFFS-Konverter | `MFFSForceEnergyConverter` |
| `mffs_defense_station` | MFFS Defence Station / MFFS-Verteidigungsstation | `MFFSDefenceStation` |
| `mffs_security_station` | MFFS Security Station / MFFS-Sicherheitsstation | `MFFSSecurtyStation` |
| `mffs_security_storage` | MFFS Security Storage / MFFS-Sicherheitsspeicher | `MFFSSecurtyStorage` |
| `mffs_control_system` | MFFS Control System / MFFS-Steuerungssystem | `MFFSControlSystem` |
| `force_field` | Force Field / Kraftfeld (kein Item) | `MFFSFieldblock` |
| `monazit_ore`, `deepslate_monazit_ore` | Monazit Ore / Monazit-Erz (+ Tiefenschiefer) | `MFFSMonazitOre` |

### 1.2 Items

| Registry `craft:…` | Name EN / DE | Stapel | Original |
|---|---|---|---|
| `forcicium` | Forcicium / Forcicium | 64 | `itemForcicium` |
| `forcicium_cell` | MFFS compact Forcicium Cell / Kompakte Forcicium-Zelle | 1 | `itemForcicumCell` |
| `force_energy_crystal` | MFFS Force Energy Crystal / Kraftenergie-Kristall | 1 | `itemForcePowerCrystal` |
| `projector_focus_matrix` | MFFS Projector Focus Matrix / Projektor-Fokusmatrix | 64 | `itemPorjectorFocusmatrix` |
| `field_modulator_distance` | MFFS Projector Field Modulator \<distance\> / Feldmodulator ‹Distanz› | 64 | `itemProjectorFFDistance` |
| `field_modulator_strength` | MFFS Projector Field Modulator \<strength\> / Feldmodulator ‹Stärke› | 64 | `itemProjectorFFStrength` |
| `projector_module_wall` | MFFS Projector Module Wall / Projektormodul Wand | 8 | `itemProjectorTypwall` |
| `projector_module_deflector` | … Deflector / Deflektor | 8 | `itemProjectorTypdeflector` |
| `projector_module_tube` | … Tube / Röhre | 8 | `itemProjectorTyptube` |
| `projector_module_cube` | … Cube / Würfel | 8 | `itemProjectorTypkube` |
| `projector_module_sphere` | … Sphere / Kugel | 8 | `itemProjectorTypsphere` |
| `projector_module_containment` | … Containment / Eindämmung | 8 | `itemProjectorModuleContainment` |
| `projector_module_adv_cube` | … Adv.Cube / Erweiterter Würfel | 8 | `itemProjectorModuleAdvCube` |
| `projector_module_diagonal_wall` | … diagonal Wall / Diagonale Wand | 8 | `itemProjectorModulediagonallyWall` |
| `projector_upgrade_touch_damage` | MFFS Projector Upgrade \<touch damage\> / Projektor-Upgrade ‹Berührungsschaden› | 8 | `itemupgradeprozapper` |
| `projector_upgrade_sponge` | … \<Sponge\> / ‹Schwamm› | 8 | `itemupgradeprosubwater` |
| `projector_upgrade_field_manipulator` | … \<Field Manipulator\> / ‹Feldmanipulator› | 8 | `itemupgradeprodome` |
| `projector_upgrade_block_breaker` | … \<Block Breaker\> / ‹Blockbrecher› | 8 | `itemUpgradeprocutter` |
| `projector_upgrade_field_jammer` | … \<Force Field Jammer\> / ‹Kraftfeld-Störsender› | 8 | `itemProjectorOptionFFJammer` |
| `projector_upgrade_camouflage` | … \<Camouflage\> / ‹Tarnung› | 8 | `itemProjectorOptionCamoflage` |
| `projector_upgrade_field_fusion` | … \<Field Fusion\> / ‹Feldfusion› | 8 | `itemProjectorOptionFieldFusion` |
| `projector_upgrade_npc_defense` | … \<NPC Defense\> / ‹NPC-Abwehr› | 8 | `itemProjectorOptionMoobEx` |
| `projector_upgrade_defense_station` | … \<Defense Station\> / ‹Verteidigungsstation› | 8 | `itemProjectorOptiondefencestation` |
| `mffs_extractor_booster` | MFFS Extractor Booster / Extraktor-Beschleuniger | 19 | `itemextractorbooster` |
| `mffs_capacitor_upgrade_capacity` | MFFS Capacitor Upgrade \<Capacity\> / Kondensator-Upgrade ‹Kapazität› | 9 | `itemupgradecapcap` |
| `mffs_capacitor_upgrade_range` | MFFS Capacitor Upgrade \<Range\> / Kondensator-Upgrade ‹Reichweite› | 9 | `itemupgradecaprange` |
| `mffs_card_blank` | MFFS Card \<blank\> / MFFS-Karte ‹leer› | 16 | `itemcardempty` |
| `mffs_card_power_link` | MFFS Card \<Power Link\> / MFFS-Karte ‹Power-Link› | 1 | `itemfc` |
| `mffs_card_personal_id` | MFFS Card \<Personal ID\> / MFFS-Karte ‹Personalausweis› | 1 | `itemIDCard` |
| `mffs_card_security_link` | MFFS Card \<Security Station Link\> / MFFS-Karte ‹Sicherheitsstations-Link› | 1 | `itemSecLinkCard` |
| `mffs_card_access` | MFFS Card \<Access license\> / MFFS-Karte ‹Zugangslizenz› | 1 | `itemAccessCard` |
| `mffs_card_data_link` | MFFS Card \<Data Link\> / MFFS-Karte ‹Datenlink› | 1 | `itemCardDataLink` |
| `mffs_card_infinite_power` | MFFS Infinite Power Card / MFFS-Unendlich-Energie-Karte | 1 | `itemInfPowerCard` |
| `mffs_multitool_wrench` | MFFS MultiTool \<Wrench\> / MFFS-Multiwerkzeug ‹Schraubenschlüssel› | 1 | `itemWrench` |
| `mffs_multitool_switch` | … \<Switch\> / ‹Schalter› | 1 | `itemSwitch` |
| `mffs_multitool_id_writer` | … \<PersonalID & Data Link Coder\> / ‹Ausweis- & Datenlink-Kodierer› | 1 | `ItemMFDIDwriter` |
| `mffs_multitool_field_teleporter` | … \<Field Teleporter\> / ‹Feld-Teleporter› | 1 | `itemForceFieldsync` |
| `mffs_multitool_guide` | … \<Guide\> / ‹Handbuch› | 1 | `itemManuelBook` |
| `mffs_multitool_debugger` | … \<Debugger\> / ‹Debugger› | 1 | `itemMFDdebugger` |

Keine Seltenheiten (alle normal). Kreativ-Tab im Pack: eigener Tab „Modular Force Field System“ (§11). Zelle, Kristall,
Multiwerkzeuge zeigen im Tab voll (+ Zelle/Kristall auch leer). `densifiedForcicium`/`depletedForcicium` sind im
Original deklariert, aber nie erzeugt → entfallen.

## 2 Monazit, Forcicium, Zelle

- **Monazit-Erz**: Material Stein, Härte 3, Explosionswiderstand 5, Stein-Geräusch, droppt sich selbst (kein
  Glück-Bonus, kein XP). Werkzeug: Spitzhacke (Stufe wie Eisenerz-Vorbild: Stein reicht, Original prüft keine Stufe →
  `mineable/pickaxe`, keine Stufenpflicht). OreDict `MonazitOre` → Tag `c:ores/monazit`.
- **Weltgen** (`MFFSWorldGenerator`): je Chunk **5** Versuche (`MonazitOreWorldGen + 1`), Ader **5** Blöcke
  (`WorldGenMinable(…, 4 + 1)`), y = rand(80) = **0–79** absolut, ersetzt nur Stein (1.21: Stein → Erz, Tiefenschiefer →
  Tiefenschiefer-Variante). Original in jeder Dimension mit Stein = Oberwelt; Craft: Oberwelt + Mining-Dimension
  (doppelte Menge, `spec 18`).
- **Verarbeitung**: Ofen → **4 Forcicium** (0,5 XP); IC2-Macerator → **8**; TE-Pulverisierer → **8** (Original-Energie
  100); GT Industrial Grinder (Wasserzelle) → **10 Forcicium + 2 Thoriumstaub + 1 leere Zelle** (Wassereimer-Variante
  laut `spec 13` §10); GT-Erzwert (`addValuableOre`) **3** für Miner/Scanner. Massefabrikator-Verstärker: Forcicium
  **5 000**. UU-Rezept aus (Pack-Config).
- **Forcicium**: OreDict `ForciciumItem` → Tag `c:gems/forcicium`; GT schließt es von der Vereinheitlichung aus (nichts
  zu tun).
- **Kompakte Forcicium-Zelle**: speichert 0–**1 000** Forcicium (NBT `Forceciumlevel`), Haltbarkeitsbalken
  = 101 − Füllung·100/1000. Rechtsklick in der Luft schaltet **aktiv/inaktiv** (Chat „[Forcicum Cell] Active/Inactive“).
  Aktiv und nicht voll: je Inventar-Tick wird **1 Forcicium** aus dem Spielerinventar (erster Fund) in die Zelle
  gezogen. Tooltip „%d / %d Forcicum“.

## 3 Extraktor (`TileEntityExtractor`)

Wandelt **EU (oder MJ) + Forcicium → FE** und schickt sie an den verlinkten Kondensator.

- Plätze: 0 Forcicium/Zelle (64, nur wenn Platz 4 leer), 1 Power-Link (1), 2 Kapazitäts-Upgrade (max 9), 3 Beschleuniger
  (max 19), 4 Forcicium-Zelle zum Füllen (1, nur wenn Platz 0 leer). Automation (alle Seiten) nur in Platz 0.
- **Arbeitsenergie** (EU-Puffer) max **4 000**; EU von **allen Seiten**, max. sichere Spannung **2 048** (darüber
  IC2-Explosion), Bedarf nur wenn aktiv. MJ (BuildCraft): 1 MJ = 2,5 EU, Provider `configure(Latenz 10, min 2, max/Aktivierung/Speicher je
  4 000/2,5 = 1 600)` (einmal im Konstruktor, auch im Zellenmodus nicht neu), jeden Tick nur wenn aktiv:
  `useEnergy(1, MaxWork − Work/2,5)` (Originalfehler: Klammer fehlt) · 2,5 → Arbeitsenergie (gedeckelt).
- Arbeitsenergie-Annahme EU: alles bis zum Puffer-Max (Rest zurück), von allen Seiten, Bedarf = Max − Arbeit
  (nur aktiv). Material wird schon geladen, wenn die Arbeitsenergie noch fehlt (Zyklus bleibt erhalten);
  Zyklus − 1 und +12 000 FE nur, wenn Platz im FE-Puffer, Material **und** Arbeitsenergie ≥ Max − 1 (dann 0).
- **FE-Puffer** 1 000 000 + 100 000 je Kapazitäts-Upgrade.
- Arbeitstakt alle **20 − Beschleuniger** Ticks (19 Beschleuniger → jeden Tick):
  - **Modus 0** (Platz 4 leer): Ist der FE-Puffer nicht voll, Material vorhanden (laufender Zyklus > 0, sonst 1
    Forcicium → 250 Zyklen bzw. 1 Forcicium aus Zelle in Platz 0 → 230 Zyklen) **und** Arbeitsenergie ≥ 3 999:
    Arbeitsenergie = 0, Zyklus − 1, FE-Puffer **+12 000** (also 4 000 EU → 12 000 FE). Danach Übertragung an den
    Kondensator: höchstens Kondensator-Max/120 je Takt, begrenzt durch freien Platz und eigenen Puffer.
  - **Modus 1** (Zelle in Platz 4): Arbeitsenergie max **200 000**; ist die Zelle nicht voll und Arbeitsenergie
    ≥ 199 999: Arbeitsenergie = 0, Zelle **+1 Forcicium** (200 000 EU je Forcicium). Keine FE-Erzeugung.
- Ergiebigkeit: 1 Forcicium = 250 × 12 000 = **3 Mio. FE** (aus 1 Mio. EU); aus der Zelle 230 Zyklen je Forcicium.
- Anzeige: Fortschritt = Arbeitsenergie % , Kapazität = FE-Puffer %.

## 4 Konverter (`TileEntityConverter`)

Wandelt **FE → EU**: je EU **3 FE** (`12000/4000`). Platz 0 Power-Link (nur `IPowerLinkItem`).
- Inventar 4 Plätze; Plätze 1–3 ohne GUI, nehmen alles; Automation (alle Seiten) nur Platz 1 (Originaleigenheit).
- Aktiv, wenn eingeschaltet (§5) und Energiequelle vorhanden. Ausgang „IC 2“ an/aus (Knopf, Standard aus).
- Paketgröße 1 / 32 / 128 / 512 / 2 048 EU (Knöpfe +/−, zyklisch), Paketanzahl 1–9 je Tick; nur änderbar, solange der
  Ausgang aus ist. Je Paket: prüft 3·Größe FE vorhanden, gibt das Paket ins EU-Netz, zieht 3 · (Größe − Rest) FE ab.
  Gibt an alle Seiten ab (Empfänger = jeder EU-Akzeptor). UE-Teil entfällt (kein UE).
- Extraktor + Konverter ergeben verlustfrei 1 EU → 1 EU (Original-Eigenheit, drahtloser EU-Transport bis 80 Blöcke).

## 5 Gemeinsame Maschinenlogik (`TileEntityMachines`, `BlockMFFSBase`)

- **Block**: Material Eisen, **unzerstörbar** (Härte −1), Explosionswiderstand 100 (aktiv 999), Stein-Geräusch.
  Abbau per MFFS-Multiwerkzeug (§8) oder – wie im Pack über `IWrenchable` – mit dem IC2-Schraubenschlüssel (nur inaktiv); dabei fallen die Plätze heraus (Karten mit Daten fallen als **leere
  Karte**: Power-Link, Sicherheits-Link, Ausweis, Datenlink; Sicherheitsstation ohne Datenlink-Ausnahme). Block droppt
  sich selbst.
- **Ausrichtung** beim Setzen nach Blick (6 Richtungen, Blick ≥ 65° hoch/runter = oben/unten). Textur: Vorderseite,
  Rückseite, Rest; aktiv eigene Texturen; Projektor zusätzlich je Modultyp (Raster `typ·16 + aktiv·4 + {0,1,2}`).
- **Name**: frei, Tastatureingabe im GUI (angehängt wird, solange der Name ≤ 20 Zeichen hat, also bis 21). **Geräte-ID**: zufällige Zahl ≠ 0, eindeutig je Typ,
  bleibt erhalten (NBT). Alle Geräte einer Welt stehen in einer Liste je Typ (Linkgrid) – darüber finden Karten ihre
  Ziele auch nach dem Verschieben.
- **Schaltmodus** (Knopf oben rechts, zyklisch zwischen min..max): 1 = **Redstone** (an solange Signal), 2 =
  **Schalter** (Multiwerkzeug ‹Schalter› oder Steuerungssystem schaltet), 3 = **Steuerungssystem** (nur dort sichtbar
  als dritter Zustand; Schaltwert wie 2). Projektor, Extraktor, Kondensator, Konverter, Verteidigungsstation: 1–3;
  Sicherheitsspeicher: 2–3; Sicherheitsstation/Steuerungssystem: kein Schalter.
- **Chunkloading** (Pack: an): jede MFFS-Maschine hält ihren eigenen Chunk geladen (§11).
- GUI öffnen (Rechtsklick ohne Schleichen) nur mit Recht **EB** (Sicherheitsstation aktiv: **CSR**,
  Steuerungssystem: **UCS**, Sicherheitsspeicher: **OSS**). Mit Multiwerkzeug, leerer Karte, Projektormodul,
  Power-Link-, Sicherheits-Link-Karte oder Hebel in der Hand öffnet sich nichts (das Item wirkt selbst).

## 6 Kondensator und Karten

### 6.1 Kondensator (`TileEntityCapacitor`)

- Speicher **10 000 000 FE** + 2 000 000 je Kapazitäts-Upgrade (max 9 → 28 Mio.).
- **Sendereichweite** 8 · (Reichweiten-Upgrades + 1) Blöcke (max 80), euklidisch, gleiche Dimension.
- Plätze: 0 Kapazität (9), 1 Reichweite (9), 2 Power-Link-Karte **oder** FE-Item (Kristall, Multiwerkzeug,
  Unendlich-Karte), 4 Sicherheits-Link-Karte. Keine Automation.
- Alle 10 Ticks: Anzahl verbundener Geräte (Projektoren, Kondensatoren, Verteidigungsstationen, Extraktoren,
  Konverter mit dieser Quelle in Reichweite) und Füllstand aktualisieren; dann Platz 2:
  - **FE-Item** → Modus 3 „laden“ / 4 „entladen“ (Knopf): je 10 Ticks bis zur Item-Rate (Kristall 100 000, Multiwerkzeug
    50 000, Unendlich-Karte 1 000 000; Unendlich-Karte entladen = beliebig viel).
  - **Power-Link** zu einem anderen Kondensator (nur wenn aktiv), Rate Ziel-Max/120 je 10 Ticks: Modus 0 „bei ≥ 95 %
    weitergeben“, 1 „ausgleichen“ (solange Ziel-% < eigene %), 2 „alles weitergeben“.
- Aktiv = eingeschaltet (§5); nur aktiv wird an andere Kondensatoren weitergegeben. Geräte ziehen auch aus inaktiven
  Kondensatoren.

### 6.2 Karten

- **Leere Karte** (`mffs_card_blank`, Stapel 16): Rechtsklick auf **Kondensator** (Recht EB) → Power-Link-Karte
  (Name, Koordinaten, Welt, ID, „gültig“); auf **aktive Sicherheitsstation** (Recht CSR) → Sicherheits-Link-Karte
  (inaktiv: „must be Active“). Formlos: jede Datenkarte → leere Karte (Rezept löscht).
- **Power-Link-Karte**: Rechtsklick auf Projektor/Konverter/Verteidigungsstation (Platz 0), Extraktor (Platz 1),
  Kondensator (Platz 2) setzt sie ein (Recht EB); liegt dort eine leere Karte, werden die Daten **kopiert** (Karte bleibt
  in der Hand). Gültig, solange der Kondensator existiert (ID gleich), in derselben Dimension und **in seiner
  Sendereichweite** steht; sonst „No Link/OOR“. Kondensator verschoben → Koordinaten über die ID nachgeführt; Ziel-Chunk
  geladen und kein Kondensator mehr → Karte „ungültig“. Name wird alle 600 Ticks nachgeführt.
- **Sicherheits-Link-Karte**: Rechtsklick auf Steuerungssystem (Platz 0), Kondensator (4), Verteidigungsstation (1),
  Sicherheitsspeicher (0), Projektor (12). Ist die Station verschwunden (Chunk geladen), wird die Karte im Gerät zur
  **leeren Karte**.
- **Personalausweis** (Besitzername, Rechte als NBT) und **Zugangslizenz** (zusätzlich Gültigkeit in Minuten, Station-ID)
  – §7. **Datenlink-Karte** (Gerätetyp, Name, ID, Ort) – §7.4.
- **Unendlich-Energie-Karte** (nur Kreativ): Energiequelle mit immer 10 Mio. FE, Verbrauch kostenlos, Quelle-ID 0.
- **Kraftenergie-Kristall**: tragbare Quelle mit **5 000 000 FE**, Haltbarkeitsbalken; direkt in Projektor/Konverter/…
  als Energiequelle einsetzbar (`isPowersourceItem`, dann §7.5-Einschränkungen). Im Kondensator laden/entladen.

## 7 Sicherheit

### 7.1 Rechte (`SecurityRight`)

| Schlüssel | Name | Wirkung in Craft |
|---|---|---|
| FFB | ForceField Bypass | Feld-Teleporter / Feldberührung erlaubt (Zugriffsart 2/3) |
| EB | Edit MFFS Block | GUI öffnen, Karten/Module einsetzen, Multiwerkzeug |
| CSR | Config Security Rights | Sicherheitsstation bedienen, Link-Karte erstellen |
| SR | Stay in Area | kein Schaden durch Verteidigung, Feld-Linksklick |
| OSS | Open Secure Storage | Sicherheitsspeicher öffnen |
| RPB | Change Protected Block | im Original nur für EE3-Transmutation – in Craft ohne Wirkung (§9.3) |
| AAI | Allow have all Items | Verteidigungsstation durchsucht nicht |
| UCS | Use Control System | Steuerungssystem bedienen, Datenlink-Karte erstellen |

Recht gewährt (`isAccessGranted`), wenn: Station **inaktiv**, Spieler in der Admin-Liste (Pack „nobody“ → niemand),
Spieler = **Hauptnutzer**, ein Ausweis mit diesem Namen in den Plätzen 35…1 der Station hat das Recht (der erste
gefundene Ausweis des Namens entscheidet), oder der Spieler trägt eine gültige **Zugangslizenz** dieser Station mit dem
Recht (oder einen **Debugger**). Abgelaufene Lizenz im Inventar wird beim Prüfen zur leeren Karte („expired validity“).
Geräte ohne verlinkte Station: alles erlaubt (Sicherheitsspeicher/Steuerungssystem ohne Station: nur Server erlaubt).
Kondensator, Extraktor, Konverter, Verteidigungsstation, Steuerungssystem, Speicher prüfen gegen ihre eigene Link-Karte
bzw. Extraktor/Konverter über die Station ihres Kondensators. Projektor: Zugriffsart 2 → Station des Kondensators,
3 → eigene Link-Karte, 0/1 → immer erlaubt. Meldung bei Ablehnung „[Field Security] Fail: access denied“.

### 7.2 Sicherheitsstation (`TileEntityAdvSecurityStation`)

- 40 Plätze, alle Stapel 1. Platz 0 **Hauptnutzer-Ausweis** (Station aktiv, solange dort ein Ausweis liegt; Hauptnutzer
  erhält automatisch CSR). Platz 1 **Bearbeitungsplatz**: Ausweis → Rechte-Knöpfe schalten jedes Recht; leere Karte →
  „+“ erzeugt **Zugangslizenz** (5 min, Name/ID der Station); Lizenz → „+“/„−“ je 5 min (≤ 5 → wird leere Karte).
  Plätze 2–33 Ablage (Ausweise zählen für Rechte), 38 leere Karte + 39 Ausweis → **Kopie** des Ausweises in 38.
  Erlaubt nur Ausweise und leere Karten (39: nur Ausweis, keine Lizenz).
- Wrench kann die Station nicht drehen; aktiv nicht abbaubar.
- Zugangslizenz: Gültigkeit sinkt um 1 je Minute im Inventar; Tooltip „Security Area“, „period of validity: n min“,
  Rechte (Shift).

### 7.3 Sicherheitsspeicher (`TileEntitySecStorage`)

60 Plätze: 0 Sicherheits-Link-Karte, 1–54 Ablage (Stapel 64). Aktiv = Station verlinkt **und** eingeschaltet (Schalter-
modus 2/3, Standard Wert aus). Automation (alle Seiten, Plätze 1–54) **nur wenn inaktiv**. Öffnen: Recht OSS.

### 7.4 Steuerungssystem (`TileEntityControlSystem`)

40 Plätze (Stapel 1): 0 Sicherheits-Link, 1 **Leser** (aktive Datenlink-Karte), 4–31 Ablage für Datenlink-Karten.
Aktiv, wenn Station verlinkt. Alle 20 Ticks: Ziel über die Karte (Typ + ID) suchen → Anzeige Name, Typ, aktiv, Schalt-
modus/-wert, Station verlinkt, Energiequelle, Energie %, „OK“/„OOR“ (≤ 61 Blöcke). Knöpfe: Ziel-Schaltmodus wechseln,
Schaltwert umschalten, „Open Remote Gui“ (nur ≤ 61 Blöcke). Ziel weg → Karte wird leere Karte.
Datenlink-Karte: Multiwerkzeug ‹Ausweis-Kodierer› Rechtsklick auf eine MFFS-Maschine (Recht UCS, 1 000 FE, leere Karte
im Inventar).

### 7.5 Zugriffsarten des Projektors (`accesstyp`)

0 = **kein Zutritt** (Feld schockt jeden), 1 = **freier Zutritt**, 2 = **über Station des Kondensators**,
3 = **eigene Station** (automatisch, solange Platz 12 eine Sicherheits-Link-Karte hat). Knopf schaltet 0→1→2→0, nur
wenn inaktiv und keine Item-Energiequelle. Mit Kristall/Unendlich-Karte als Quelle: Zugriffsart 0 erzwungen, keine
Sicherheits-Link-Karte, kein Störsender/Feldfusion/Verteidigungsstation-Upgrade.

## 8 Multiwerkzeug (`ItemMultitool`)

Ein Werkzeug mit Modi, je eigenes Item (Registry s. §1.2); **Schleichen + Rechtsklick** wechselt zum nächsten Modus
(Wrench → Schalter → Kodierer → Feld-Teleporter → Handbuch → Wrench; Debugger nicht im Kreis), Ladung bleibt.
Speicher **1 000 000 FE**, Laderate 50 000 (im Kondensator), Haltbarkeitsbalken. Tooltip „%d FE/%d FE“.

- **Wrench**: auf ausgebrannten Projektor → repariert (10 000 FE). Auf MFFS-Maschine (Recht EB, nicht aktiv bei
  Projektor/Sicherheitsstation/Verteidigungsstation; je 1 000 FE): andere Seite angeklickt → Ausrichtung auf diese Seite;
  Vorderseite angeklickt → Maschine abbauen (droppt Block + Plätze). Auf IC2-Wrench-Blöcke: drehen 1 000 FE, abbauen
  25 000 FE (Craft: Craft-Blöcke mit Schraubenschlüssel-Logik). Ist zugleich BC-Wrench (1 000 FE je Nutzung).
- **Schalter**: auf Maschine im Schaltmodus 2 (Recht EB) → Schaltwert umschalten, 1 000 FE; sonst „Object not in switch
  enable mode“.
- **Kodierer**: Rechtsklick Luft → Ausweis für sich selbst; Linksklick auf Spieler → Ausweis für ihn; Rechtsklick auf
  Maschine → Datenlink-Karte (§7.4). Je 1 000 FE + 1 leere Karte aus dem Inventar.
- **Feld-Teleporter**: Rechtsklick auf Kraftfeld eines **aktiven** Projektors mit Power-Link-Quelle; Zutritt nach
  Zugriffsart (0: nur Admin/Debugger, 1: jeder, 2/3: Recht FFB). Blickrichtung (6 Richtungen) bestimmt die Durchquerung;
  Feld ≤ **5 Blöcke** dick, Ziel + Block darunter frei (Luft/Flüssigkeit), nicht ins Void (y > 0 bzw. Weltboden);
  kostet **10 000 FE** → Teleport hinter das Feld. Meldungen „access granted/denied“, „detected obstacle“, „Field to
  Strong >= 5 Blocks“, „not enough FP“, „transmission complete“.
- **Handbuch**: Rechtsklick öffnet das Handbuch-GUI (§10).
- **Debugger** (nur Kreativ): gilt bei Sicherheitsprüfungen als Freibrief (§7.1, Feld-Teleporter Zugriffsart 0).

Rezept nur für den Wrench (§12); die anderen Modi entstehen durch Umschalten.

## 9 Projektor und Kraftfeld

### 9.1 Projektor (`TileEntityProjector`)

Plätze (13): 0 Energiequelle (Power-Link/Kristall/Unendlich), 1 **Modul** (Typ), 2–4 **Upgrades** (je 1), 5 Distanz-
Modulator (64), 6 Stärke-Modulator (64), 7–10 **Fokusmatrix** oben/unten/rechts/links (je 64), 11 **Tarnblock**
(nur mit Tarnung, Automation von allen Seiten), 12 Sicherheits-Link. Nicht unterstützte Teile fallen beim Modulwechsel
heraus (Modul entfernt → Plätze 2–10 fallen heraus). Upgrades nur einsetzbar, wenn inaktiv, Modul + Energiequelle
vorhanden, Modul unterstützt es, und nicht Tarnung **und** Berührungsschaden zugleich.

- **Einschalten**: Schaltwert an, ≥ 40 Ticks seit dem letzten Wechsel, Modul + Quelle, verfügbare FE > Bedarf(5) →
  Feld berechnen und erzeugen (Erstkosten). **Ausschalten**: Schaltwert aus (nach 40 Ticks), kein Modul, keine Quelle,
  **ausgebrannt**, oder verfügbare FE ≤ Bedarf(1) → Feld entfernen.
- **Bedarf(f)**: hat das Feld schon Blöcke → Anzahl · 1 FE. Sonst Schätzung je Typ (Wand: (Σ Fokus + 1) · max(Stärke,1);
  Deflektor: (oben+unten+1)·(links+rechts+1); Röhre: ((D+2)·2·4 + 4)·(S+1); Würfel/Kugel: D²·6; sonst 0) · 1 FE,
  bei f ≠ 1 zusätzlich ·10 + ·5 (also ·15).
- Alle 20 Ticks (aktiv): **Unterhalt** = Feldblöcke · 1 FE (Erstkosten beim Einschalten: · 10). **Originalfehler**:
  der Faktor ×2 (`zappermodifier`) trifft Felder vom Typ **Standard** (Wand/Deflektor/diagonal ohne Option), nicht den
  Berührungsschaden-Typ. Dann fehlende Feldblöcke setzen (max. **5 000** je Durchlauf), NPC-/Spielerabwehr (§9.4).
- Berechnung: Feldpunkte relativ zum Projektor, nur y < Welthöhe; jeder Punkt wird in der **Feldkarte** eingetragen
  (Stapel je Position: Kondensator-ID, Projektor-ID, Feldtyp; erster Eintrag gilt). Mehrere Projektoren dürfen sich
  überlagern – der erste besitzt den Block, beim Abschalten übernimmt der nächste.
- Feldblock gesetzt, wenn die Position **Luft oder Flüssigkeit** ist (Flüssigkeiten werden ersetzt) oder schon ein
  Feldblock; mit Blockbrecher werden andere Blöcke (nicht Grundgestein, ohne Block-Entity) vorher entfernt.
- Feldtyp: 2D-Module **Standard**, 3D-Module **Bereich**, Eindämmung **Eindämmung**; Tarnung → **Tarnung**,
  Berührungsschaden → **Zapper**.

### 9.2 Module (Feldformen)

D = Distanz-Modulatoren, S = Stärke-Modulatoren, F↑/F↓/F→/F← = Fokusmatrizen; „vorn“ = Ausrichtung des Projektors.

| Modul | D | S | Fokus | Upgrades | Form |
|---|---|---|---|---|---|
| Wand | ✓ | ✓ | ✓ | Blockbrecher, Tarnung, Berührung | Kreuz aus zwei Balken in der Ebene senkrecht zur Blickachse (F←…F→ quer, F↓…F↑ hoch), nur Punkte auf den Achsen; S+1 Schichten tief, ab D+1 vor dem Projektor |
| Diagonale Wand | ✓ | ✓ | ✓ | wie Wand | wie Wand, aber Diagonalen (|a| = |b|) je Quadrant mit Fokusanzahl als Länge |
| Deflektor | ✓ | – | ✓ | wie Wand | Fläche (F←+F→+1)·(F↑+F↓+1) im Abstand D+1 vor dem Projektor, 1 dick |
| Röhre | ✓ | ✓ | – | Tarnung, Fusion, Manipulator, Störsender, Schwamm, Blockbrecher, Berührung | Hohlquader um den Projektor, Querschnitt ±(D+2), Länge ±S entlang der Achse, an den Enden offen; Manipulator kappt eine Seite |
| Würfel | ✓ | – | – | Tarnung, Verteidigung, Fusion, Manipulator, Störsender, NPC-Abwehr, Schwamm, Blockbrecher | Hohlwürfel Radius D+4 (Oberkante ≤ Welthöhe); Manipulator: keine untere Hälfte (Kuppel) |
| Kugel | ✓ | ✓ | – | wie Würfel | Kugelschale Radius D+4, Dicke S+1 (gerundeter Abstand); Manipulator: Halbkugel |
| Eindämmung | ✓ | ✓ | ✓ | wie Würfel ohne Manipulator | Hohlquader vor dem Projektor: Abstand D+1, Tiefe S+1+1, Breite F←..F→, Höhe F↑..F↓ |
| Erw. Würfel | – | ✓ | ✓ | wie Würfel | Hohlquader um den Projektor: von −1 bis S+2 entlang der Achse, quer F←..F→ / F↑..F↓; Manipulator: nur die vordere Hälfte |

Die genauen Schleifen (Achsenvertauschung je Seite, Vorzeichen) folgen 1:1 `modules/*.calculateField`. Bei 3D-Modulen
gehören alle nicht-Rand-Punkte zum **Innenraum** (für Schwamm, Fusion, Störsender).

### 9.3 Upgrades (`options/*`)

- **Berührungsschaden** (Zapper): Feldblock-Kollision 1/16 kleiner; jedes Lebewesen, das ihn berührt, erhält **10**
  Schaden (`fieldShock`, ignoriert Rüstung). Rauchpartikel („townaura“ ≈ Myzel-Partikel) am Block.
- **Schwamm**: beim Berechnen werden Flüssigkeiten im **Innenraum** entfernt (Pack: alle Flüssigkeiten).
- **Feldmanipulator**: Kuppel/Halbform (s. Tabelle).
- **Blockbrecher**: entfernt Blöcke auf Feldpositionen; bei Wand/Deflektor/diagonaler Wand geht **ein** Item der Drops
  ins größte angrenzende Inventar (Sicherheitsspeicher ab Platz 1), der Rest ist verloren (**Originalfehler**,
  `addStacksToInventory` bricht nach einem Item ab); bei anderen Formen alles verloren.
- **Kraftfeld-Störsender**: jeder **andere** Projektor (anderer Kondensator), dessen Feldpunkt im **Innenraum** eines
  aktiven Störsender-Projektors liegt, **brennt aus**: alle Plätze fallen heraus, Projektor aus, bis Reparatur mit
  Multiwerkzeug ‹Wrench›. Wirkt bei jedem Projektor, auch ohne eigenes Upgrade.
- **Tarnung**: Feldblöcke zeigen die Textur des Blocks in Platz 11 (Wasser-/Lavaeimer → Wasser/Lava); ohne Block
  Standard-Tarntextur.
- **Feldfusion**: Projektoren am selben Kondensator mit Fusion: Punkte im Innenraum eines anderen aktiven Fusions-
  Projektors werden nicht gesetzt; eigene Innenpunkte entfernen dort stehende Feldblöcke anderer Fusions-Projektoren
  (verschmolzene Räume).
- **NPC-Abwehr**: alle 20 Ticks Monster/Schleime/Ghasts im Feld-Quader (Kugel: im Radius D+4) → je **10** Schaden
  (`fieldDefense`), je 10 000 FE; nur wenn verfügbare FE ≥ 10 000.
- **Verteidigungsstation**: wie NPC-Abwehr für **Spieler ohne Recht SR** (nur Zugriffsart 2/3), je 10 Schaden + Chat
  „!!! [Area Defence] leave or die !!!“, 10 000 FE; nur wenn verfügbare FE > 10 000.

### 9.4 Kraftfeld-Block (`BlockForceField`)

- Unzerstörbar, Explosionswiderstand 999 (jede Explosionsabfrage kostet den Projektor 10 FE), nicht undurchsichtig,
  kein Licht, keine Drops, nicht Redstone-leitend, Auswahlrahmen leer, Kollision voller Block (Zapper 1/16 kleiner).
  Gleiche Nachbarn mit gleichem Typ verbergen die Fläche (glasartig). Keine Block-Entity außer bei Tarnung.
- **Linksklick** (Spieler): Zugriffsart 0 → 10 Schaden + „[Force Field] Attention High Energy Field“; 2/3 ohne Recht SR
  → 10 Schaden + Meldung und danach **noch einmal** 10 (**Originalfehler**, doppelte Prüfung).
- **Berührung** (nur Zapper-Typ praktisch erreichbar, da sonst voller Block): Lebewesen 10 Schaden; Nicht-Zapper-Spieler:
  mit Zutritt 1, ohne 20 Schaden.
- Wird ein Nachbar entfernt, setzt das Feld leere Feldpositionen im 3×3×3 sofort wieder (Erstkosten 10 FE, Standard-Typ
  ×2 wie oben). Zufallstick: kein aktiver Projektor → Block verschwindet.
- Verschwindet beim Abschalten/Ausbrennen/Abbau des Projektors.

## 10 Verteidigungsstation (`TileEntityAreaDefenseStation`)

- Plätze (36): 0 Power-Link, 1 Sicherheits-Link, 2 Distanz-Modulator „Warnbereich“, 3 Distanz-Modulator „Aktions-
  bereich“, 5–14 **Gegenstandsliste** (je 1), 15–34 **Beschlagnahme-Lager** (Automation alle Seiten ab 15, 20 Plätze).
- Aktionsradius A = Modulatoren in Platz 3; Warnradius W = A + Modulatoren in Platz 2 + 3.
- Aktiv: eingeschaltet, Quelle, FE > 0, Station verlinkt; aus, wenn FE < 10·W. Jeden Tick (aktiv) **10 · W FE** und Scan:
  - Spieler neu im Warnbereich (Scanmodus 1 = Kugel, 0 = Würfel) ohne SR: Chat „Warning you now in me Scanning range“
    + **1** Schaden (`areaDefense`).
  - Spieler/NPC neu im Aktionsbereich → Aktion; danach alle 100 Ticks erneut für Spieler im Bereich.
- **Aktionsmodi** (Knopf, nur inaktiv änderbar): 0 **informieren** (Spieler ohne SR: „get out immediately“),
  1 **töten** (Spieler ohne SR: gesamtes Inventar + Rüstung ins Lager bzw. angrenzendes Inventar, dann getötet,
  10 000 FE), 2 **durchsuchen** (Spieler ohne AAI: Gegenstände **auf** (Liste = Verbot, Kontratyp 1) bzw. **nicht auf**
  der Liste (Erlaubtliste, Kontratyp 0) werden beschlagnahmt, je 1 000 FE), 3 **alle NPCs töten**, 4 **feindliche NPCs
  töten**, 5 **freundliche NPCs töten** (je 10 000 FE, sofort Leben 0).
- Lager voll → Rest ins größte angrenzende Inventar; passt es nicht, geht es verloren.

## 11 Entscheidungen (alle wie vorgeschlagen freigegeben, Nutzer 2026-11-12)

1. **Kreativ-Tab**: eigener Tab „Modular Force Field System“ wie im Pack (wie TE/MPS).
2. **Chunkloading**: wie im Pack an – jede MFFS-Maschine hält ihren Chunk geladen (sonst fallen Felder ohne Spieler
   in der Nähe weg).
3. **Admin-Liste**: keine (Pack „nobody“); Debugger und Unendlich-Energie-Karte nur im Kreativ-Tab.
4. **Gefährliche Teile** (Verteidigungsstation „töten“/„durchsuchen“ mit Inventar-Beschlagnahme, Feldschock,
   Störsender-Ausbrennen): wie im Pack an.
5. **Energiename**: EN „Force Energy (FE)“ wie im Pack; DE „Kraftenergie (KE)“, damit es nicht mit Forge Energy
   (FE Producer, Phase 33) verwechselt wird.
6. **Handbuch**: Seiten Inhalt, Monazit/Forcicium/Zelle, Kartenübersicht wie im Pack; „Changelog“ und „Version Check“
   (Online-Abfrage) entfallen.

## 12 Rezepte (geformt; IC2-Varianten)

Buchstaben: A raff. Eisenbarren, B Übertakter-Upgrade, C Elektronischer Schaltkreis, D Fortgeschrittener Schaltkreis,
E Kohlenstoffplatte, F Fortgeschrittener Maschinenblock, G Extraktor (IC2), H Kupferkabel (unisoliert), I isoliertes
Kupferkabel, J Frequenzsender, K Fortgeschrittene Legierung, M Glasfaserkabel, N/O/P LV-/MV-/HV-Transformator,
Q Tesla-Spule, R UU-Materie, S Schraubenschlüssel (IC2), u Forcicium, v/y Fokusmatrix, x Kraftenergie-Kristall (jede
Ladung), z Personalausweis, a Enderperle, b Eisenspitzhacke, c Eimer, f Knochen, g Lohenrute, h verrottetes Fleisch,
i Diamant, j Spinnenauge, k Obsidian, l Glas, m Redstone, n Hebel, o Papier.

| Ergebnis | Muster | Anz. |
|---|---|---|
| Projektor | `KyK/yFy/KJK` | 1 |
| Extraktor | ` C /xFx/ G ` | 1 |
| Kondensator | `xJx/CFC/xJx` | 1 |
| Konverter | `ANA/JOM/APA` | 1 |
| Verteidigungsstation | ` J /aFa/ E ` | 1 |
| Sicherheitsstation | `KCK/CFC/KJK` | 1 |
| Sicherheitsspeicher | `AAA/ACA/AAA` | 1 |
| Steuerungssystem | `aCa/AFA/ACA` | 1 |
| Kraftenergie-Kristall | `uuu/uiu/uuu` | 1 |
| Kompakte Forcicium-Zelle | `AAA/AxA/ADA` | 1 |
| Fokusmatrix | `AlA/lil/AlA` | 64 |
| Feldmodulator ‹Stärke› | `vvv/vvv/vvv` | 1 |
| Feldmodulator ‹Distanz› | `vvv/   /vvv` | 1 |
| Extraktor-Beschleuniger | ` E /EBE/ E ` | 4 |
| Kondensator-Upgrade ‹Kapazität› | ` E /ExE/ E ` | 1 |
| Kondensator-Upgrade ‹Reichweite› | `HHH/EIE/EDE` | 1 |
| Leere Karte | `ooo/oCo/ooo` | 1 |
| Multiwerkzeug ‹Wrench› | `mSn/ExE/EDE` | 1 |
| Modul Wand | `AA /AA /BB ` (A Fokusmatrix, B Obsidian) | 1 |
| Modul Deflektor | `AAA/ABA/AAA` | 1 |
| Modul Röhre | `AAA/ B /AAA` | 1 |
| Modul Würfel | `B B/ A /B B` | 1 |
| Modul Kugel | ` B /BAB/ B ` | 1 |
| Modul Eindämmung | `BBB/BAB/BBB` | 1 |
| Modul Erw. Würfel | `AAA/ACA/AAA` (C Würfel-Modul) | 1 |
| Modul Diagonale Wand | `A A/ B /A A` | 1 |
| Upgrade Berührungsschaden | ` K /KQK/ K ` | 1 |
| Upgrade Schwamm | ` K /KcK/ K ` | 1 |
| Upgrade Feldmanipulator | ` K /KCK/ K ` | 1 |
| Upgrade Blockbrecher | ` K /KbK/ K ` | 1 |
| Upgrade Störsender | ` J /JvJ/ J ` | 1 |
| Upgrade Tarnung | ` K /KRK/ K ` | 1 |
| Upgrade Feldfusion | ` K /KDK/ K ` | 1 |
| Upgrade NPC-Abwehr | `fgf/hQh/jgj` | 1 |
| Upgrade Verteidigungsstation | ` z /CQC/ z ` | 1 |

Formlos: Power-Link-/Personalausweis-/Sicherheits-Link-/Zugangslizenz-/Datenlink-Karte → leere Karte.
Schmelzen/Maschinen: §2. Modulrezepte (Vanilla-Zutaten + MFFS) gelten auch ohne IC2 – hier ohnehin.
GregTech greift in keine MFFS-Rezepte ein (nur Grinder-Rezept und Erzwert, §2).

## 13 GUIs

Nach den Original-Hintergründen (`Gui*.png`), Spielerinventar wie im Original: Projektor (Plätze §9.1, Zugriffsart-
Knopf, Schaltmodus-Knopf, Name, „PowerLink“ %/„No Link/OOR“), Extraktor (Fortschritt, Kapazität, Upgrades), Kondensator
(FE, Power-Uplink %, Sendereichweite, verbundene Geräte, Modus-Knopf), Konverter (IC2-Ausgang an/aus, EU-Paketgröße,
Pakete, FE, ONLINE/OFFLINE), Verteidigungsstation (Kontratyp, Aktionsmodus mit Beschreibung, Scanmodus), Sicherheits-
station (Master, Rechtevergabe mit 8 Rechte-Knöpfen aus `AdvSecStationButtons.png`, „Copy->“, Gültigkeit +/−),
Sicherheitsspeicher (6×9), Steuerungssystem (Datenlink-Leser, Ablage 7×4, Ferninfo, 3 Knöpfe), Handbuch (Seiten,
„-->“/„<--“). Knöpfe zeigen Symbole aus `items.png` (Raster ab y 80/96/112/128). Namenseingabe per Tastatur im GUI
(Backspace löscht, max. 20 Zeichen).

## 14 Abweichungen (1.21)

- Ein Block/Item je Variante statt IDs/Metadaten; Feldtypen als Blockzustand `type` (camouflage/default/zapper/area/
  containment). Ausrichtung als Blockzustand `facing` (6), aktiv als `active`.
- Kraftfeld: Feldkarte (Stapel je Position) im Speicher je Level wie im Original (nicht gespeichert; Projektoren bauen
  sie beim Laden neu auf). Welthöhe 1.21 statt 255; Weltboden (y −64) statt y 0 bei Teleporter/Röhre.
- Tarnung: Feldblock rendert das Modell des Tarnblocks (Block-Entity mit Blockzustand) statt Texturindex-Strings.
- **Forcicium-Zelle aktiv/inaktiv je Item** (Data Component) statt eines globalen Schalters am Item-Objekt (Original:
  ein Schalter für alle Zellen aller Spieler). Zugangslizenz zählt Minuten je Item über die Spielzeit statt eines
  gemeinsamen Tick-Zählers aller Karten. Kartennamen-Nachführung je Item alle 600 Ticks.
- Spielernamen: Ausweise speichern Name (Anzeige) und UUID; Vergleich über UUID (Namen können sich ändern).
- Schadensarten als Datapack-Damage-Types `craft:field_shock`, `craft:area_defense`, `craft:field_defense`
  (`bypasses_armor`), Todesmeldungen wie im Original.
- Chunkloading über NeoForge-`TicketController` (wie Quarry).
- MultiTool-Modi als eigene Items (wie im Original), Umschalten ersetzt das Item in der Hand mit gleicher Ladung.
- IC2-Wrench-Schnittstelle → Craft-Schraubenschlüssel-Logik; BC-Wrench → Craft-BC-Wrench-Tag.
- Kein UE-Teil, kein ICBM-EMP (`weakenForceField`), kein EE3-Schutz (RPB ohne Wirkung), keine Versionsprüfung.
- Netzwerk über Payloads (Knöpfe, Namenseingabe) und Menüdaten statt Feld-Updates per Name.

## 15 Teilphasen

- **48.1** Grundlagen + Energie: Monazit-Erz (Weltgen, Mining-Dimension, Verarbeitung inkl. TE/GT/Massefab),
  Forcicium, Zelle, Kristall, Fokusmatrix, Modulatoren, Karten (leer, Power-Link, Unendlich), Extraktor (EU/MJ),
  Kondensator (Upgrades, Item-Laden, Weitergabe), Konverter, Multiwerkzeug ‹Wrench›/‹Schalter›, Schaltmodi, Chunk-
  loading, Tab, Texturen, Rezepte, GameTests.
- **48.2** Projektor + Kraftfeld: Block, Feldkarte, 8 Module, Kosten/Unterhalt, Feldblock-Verhalten, GUI, GameTests.
- **48.3** Projektor-Upgrades (9) inkl. Tarnungs-Rendering, Störsender/Ausbrennen, Fusion, Feld-Teleporter, GameTests.
- **48.4** Sicherheit: Sicherheitsstation, Ausweis/Lizenz/Link-/Datenlink-Karten, Rechte, Kodierer, Sicherheitsspeicher,
  Steuerungssystem, Verteidigungsstation, Handbuch, Debugger, GameTests.
- **48.5** EMI (Info-Seiten), Herkunft, Wiki, Testplan, Craft-Version + Paket.
