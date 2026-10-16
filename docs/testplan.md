# Testplan – manuelle Client-Tests

Gesammelt aus den „Nächste Schritte“-Abschnitten von `STATUS.md` (Phasen 2–11), nach Mod/Thema geordnet. Was die
GameTests bereits abdecken (Werte, Rezepte, Energiefluss, Multiblock-Erkennung …), steht hier nicht; hier geht es um
Darstellung, GUIs, Sounds, Tasten, Weltgen und Zusammenspiel im echten Client. Spalte „OK“ zum Abhaken (`x`),
Auffälligkeiten unter „Befunde“ am Ende notieren.

**Vorbereitung:** `gradlew runClient`, neue Welt (Kreativ, Cheats an); für Explosionen/Reaktor/Nuke eine eigene
Testwelt. Sprache einmal Deutsch, einmal Englisch prüfen. EMI und Xaero's Minimap laufen mit (`localRuntime`).

## 1. Allgemein / Start

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 1.1 | Client startet, Welt betreten, Log ohne Mod-, Modell-, Textur- oder EMI-Plugin-Fehler | 2, 11.1 | |
| 1.2 | Creative-Tabs vollständig, Item-Darstellung (Modelle/Texturen) stichprobenartig in allen Tabs | 2–10 | |
| 1.3 | Xaero's Minimap lädt und zeigt die Karte | 11.4 | |
| 1.4 | Steuerungsmenü: Kategorie „IndustrialCraft 2“ (Modus, Boost, Alt) | 6.2 | |

## 2. IC2 – Grundlagen (Phase 2/3)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 2.1 | Kabel: Verbindungen/Modelle aller 13 Sorten, Farbe, Isolieren, Detektor-/Splitterkabel mit Redstone, Schmelzen bei Überspannung (Zischen/Rauch), Stromschlag an blankem Kabel | 2 | |
| 2.2 | Bauschaum am Kabel (nass → hart), Sand beschleunigt | 2 | |
| 2.3 | Generatoren (Generator, Geothermie, Solar, Wasser-, Windmühle): GUIs, Brennstoff/Lava, Animation aktiv, Sounds | 3 | |
| 2.4 | Maschinen (Macerator, Extractor, Compressor, Elektroofen, Recycler, Eisenofen): GUIs, Fortschritt, Sounds, Upgrades (Overclocker, Speicher, Trafo), Ofen-XP | 3 | |
| 2.5 | Batterien (RE, Energie-/Lapotronkristall): Ladebalken/Ladestufen-Icon, BatBox laden/entladen, Redstone-Knopf | 3 | |
| 2.6 | Gummibaum: Weltgen, Harzstellen, Baumzapfer, Blätterfarbe; Harz auf Kolben → Klebekolben, Harzschicht | 3 | |
| 2.7 | Zinnerz/Kupfer in neuer Welt (inkl. Tiefenschiefer), Kupfererz droppt 1 Rohkupfer | 3 | |
| 2.8 | Schraubenschlüssel dreht/baut Maschinen ab; mit Alt zyklisches Drehen | 3, 6.2 | |
| 2.9 | Schrottbox per Rechtsklick und aus dem Werfer | 3 | |

## 3. IC2 – Speicher, Werkzeuge, Rüstung (Phase 6)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 3.1 | MFE/MFSU: GUI, Laden/Entladen, Redstone-Modi, Ausgabe vorne, Drehen per Schraubenschlüssel inkl. oben/unten | 6.1 | |
| 3.2 | Trafos: HV-Trafo zwischen MFSU-Ausgang und MFE; Aufwärts mit Hebel → aktive Textur, Ausgabe vorne; falsch gedrehter LV-Trafo explodiert; Drops ohne Schraubenschlüssel | 6.1 | |
| 3.3 | Bohrer/Diamantbohrer: Geschwindigkeit, leer = langsam, Ladebalken | 6.2 | |
| 3.4 | Kettensäge: Holz, Blätter scheren, Schaf scheren, Extraschaden, Leerlauf-/Stopp-Sound | 6.2 | |
| 3.5 | El. Hacke (Erde → Ackerboden), el. Baumzapfer, el. Schraubenschlüssel (M + Rechtsklick → Lossless-Meldung, Maschinenabbau) | 6.2 | |
| 3.6 | Bergbaulaser: alle 7 Modi, M wechselt, Horizontal nur auf Block, Super-Heat Sand → Glas, Explosiv, Scatter; Projektil, Sounds | 6.2 | |
| 3.7 | Nanosäbel: an/aus, Flackern, Schaden, Leerlaufverbrauch, gegen Spieler mit Nanorüstung | 6.2, 6.3 | |
| 3.8 | OD-/OV-Scanner: Meldung, Sound | 6.2 | |
| 3.9 | Rüstung anlegen: Modelle/Texturen aller Teile, Rüstungsbalken je Ladung; Nano/Quantum gegen Mobs und Fallschaden | 6.3 | |
| 3.10 | Quantum: Sprint (Strg), Supersprung (Strg+Leertaste), Luft unter Wasser; Quantenhelm isst Dosen bei Hunger | 6.3, 6b.5a | |
| 3.11 | Hazmat in Lava/Feuer, Gummistiefel-Fall, Nachtsichtbrille Tag/Nacht | 6.3 | |
| 3.12 | Jetpack: Leertaste, M+Leertaste Schwebemodus, Vorwärts-Schub, Loop-Sound, kein Flug-Kick; Betanken in der Canning Machine | 6.3, 6b.5a | |
| 3.13 | BatPack/Lithium-BatPack laden gehaltenes Werkzeug (Bergbaubohrer zieht daraus); Solarhelm/Static Boots laden das Brust-Item | 6.3, 6b.4 | |
| 3.14 | Massefabrikator: GUI-Texte, Fortschritt, Verstärker-Anzeige, Sounds Loop/Schrott, Redstone-Stopp, Abbau → fortg. Maschinenblock | 6.4 | |
| 3.15 | UU-Rezepte in der Werkbank nur an der richtigen Stelle | 6.4, 8e.1 | |
| 3.16 | Isolierungsschneider: Linksklick auf isoliertes Kabel → blank + Gummi + Sound, Rechtsklick mit Gummi → isoliert; Haltbarkeitsbalken | 12.1 | |
| 3.17 | Farbroller: Kabel, Wolle und Schaf färben (Sound bei Blöcken), 32 Anstriche → leerer Roller; M + Rechtsklick → Nachfüllmeldung, Nachfüllen aus Farbstoff | 12.1 | |
| 3.18 | EU-Meter: erster Klick „Neue Messung“, zweiter Klick auf Kabel/Maschine/Generator zeigt rein/raus/Gewinn | 12.1 | |
| 3.19 | Werkzeugkasten: Packen (Icon gefüllt, Name des ersten Inhalts), Auspacken in Hotbar-Slots 0–7, Rest fällt | 12.1 | |
| 3.20 | Gummimatte zwischen zwei Blöcken über Luft: Sprung federt (höher mit Leertaste, schwach beim Schleichen), kein Fallschaden; Matte ohne Halt reißt; verstärkter Stein/Tür (Tür nur per Redstone) | 12.1 | |

## 4. IC2 – Canning, Pumpe, Reaktor, Nuke (Phasen 6b, 7, 8)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 4.1 | Canning Machine: GUI (Flamme, Pfeil je Modus), Kuchen → 6 Dosen, Pilzsuppe lässt Schüssel, Kanister mit 6 Kohle-Brennstoffzellen + Redstone-Booster, Seitenzugriff (links von vorne Batterie, unten Behälter, oben Eingabe) | 6b.5a | |
| 4.2 | Gefüllte Dose essen (leere Dose zurück, Effekte); Brennstoffkanister im Generator/Eisenofen | 6b.5a | |
| 4.3 | IC2-Elektrolyseur an BatBox (≥ 70 % lädt, ≤ 30 % gibt zurück, Balken); Eisenzaun-Pfahl mit Eisenstiefeln am Magnetisierer | 6b.2 | |
| 4.4 | Induktionsofen: GUI (Hitze-%, Energie-/Fortschrittsbalken), zwei Stapel parallel, Redstone heizt vor, Trichter oben Eingaben/unten Batterie/Seiten Ausgaben | 6b.3a | |
| 4.5 | IC2-Pumpe über Wasser/Lava mit Generator/BatBox: GUI, leere Zellen → Wasserzellen in Truhe, Truhe mit leeren Eimern, Geothermie daneben mit Lava, Öl + Eimer per Trichter, Loop-Sound; Compressor daneben → Schneebälle; Bergbaurohr platzieren/abbauen | 8d.1 | |
| 4.6 | Reaktor: Uranerz finden (y 0–64), Reaktor + Kammern (GUI-Breite 3–9, Kammer nur an einem Reaktor), Redstone → Loop/Geigerzähler, Energie in MFE, Kühlzellen-Balken, Kondensator mit Redstone reparieren | 7a | |
| 4.7 | Kernschmelze in Testwelt: Rauch/Flammen, Feuer/Lava ringsum, Explosion | 7a | |
| 4.8 | GT-Reaktorteile: Thorium (Output jeden 5. Zyklus), Plutonium, Iridium-Reflektor (Stapel wirkt nicht), Helium-/NaK-Kühlzelle (Tooltip „Stored Heat“) | 7b, 8a | |
| 4.9 | Nuke in Testwelt: Platzieren → Log-Eintrag, Abbau von Hand → Item, Feuerzeug/Redstone → 15 s Zündschnur, Schraubenschlüssel entschärft, Detonation (Krater, Strahlung/Hunger, Hazmat schützt) | 7c | |
| 4.10 | ITNT: Abbau/Feuerzeug/Redstone zündet, Kettenreaktion, Blitz-/Aufbläh-Animation, Sprengkraft; verstärktes Glas durchsichtig und explosionsfest | 6b.5b | |

## 5. GregTech (Phasen 6b, 8)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 5.1 | Plattenbieger: Vorderseite zum Spieler, GUI (Titel links, Schalter Energieausgabe/Auto-Ausgabe/getrennte Eingänge, Pfeil), Batterie-Slot entlädt, Auto-Ausgabe in Truhe hinten, Drehen (Ausgangsseite-Textur) | 6b.1 | |
| 5.2 | GT-Maschinen allgemein: Upgrades per Rechtsklick + Tooltip nach Wrench-Abbau, Abbau ohne Schraubenschlüssel → Maschinenteile (mit Energie: Explosion), Überspannung explodiert, Feuer daneben | 6b.1 | |
| 5.3 | GT-Elektrolyseur: GUI (Slots, Holo-Wasser, Balken nach oben, Fehlertext ohne Energie), Wasser per BC-Pumpe/Tank, Sand → Siliziumzelle, Tonstaub → Aluminiumstaub, nur ein IC2-Trafo-Upgrade, keine Drehung | 6b.2 | |
| 5.4 | Hochofen 3 × 3 × 4: GUI „Heat Capacity“ bzw. „Incomplete Machine Casing!“, Lava im Kern (+500 K, danach Gehäuse neu setzen), Kanthal-/Nichrom-Spulen, fortg. Gehäuse (+70 K), Stahl/Siliziumplatte/Aluminium, Drehen, Texturen | 6b.3, 8e.3 | |
| 5.5 | Verbundene Gehäusetexturen: als Wand/Hochofen gebaut Rahmen nur an Außenkanten, Unterseite und alle vier Seiten richtig ausgerichtet, verschiedene Typen verbinden sich nicht, Item zeigt die schlichte Textur | 11.5 | |
| 5.6 | Assembler: GUI (Knopf „getrennte Eingänge“, Pfeil), Wassermühle aus Generator + Aluminiumplatten, Magnalium-Windmühle | 6b.4, 8e.2 | |
| 5.7 | MJ-Konverter-Upgrade per Rechtsklick + Engine daneben (MJ-Speicher füllt sich, Maschine läuft ohne EU); HV-Trafo-Upgrade erst nach zwei IC2-Trafos | 6b.4 | |
| 5.8 | Lithium-Batterie laden/entladen (Icon wechselt), MFE mit Lithium-Batterien, Standard-Gehäuse mit Aluminiumhülle | 6b.4 | |
| 5.9 | GT-Canner: Apfel + Dosen in vertauschten Slots, Wassereimer + Zelle | 6b.5a | |
| 5.10 | Implosionskompressor: Würfel (Ecken Standard-, Rest verstärkt, Mitte frei), Controller mittig oben → grüner Bildschirm, Legierungsbarren + 8 ITNT → Iridiumplatte + 4 dunkle Asche (zwei Knalle), Seitenzugriff | 6b.5b | |
| 5.11 | Zentrifuge: Lava per Pumpe/Rohr (16 Lava → Metalle), Redstone/Glowstone/Nahrung, Uranstaub, Granatstaub; GUI-Pfeile, Holo-Slot | 6b.5c, 7b, 8e.1 | |
| 5.12 | Grinder: Würfel hinter dem Controller, Wasser in der Mitte; Erz + Wasserzelle/-eimer/Quecksilberzelle; Tank per IC2-Pumpe/BC-Rohr (Wasserbalken), Erz ohne Zelle; Overclocker; Struktur zerstören → inaktiv | 8d.2 | |
| 5.13 | Vakuumgefrierer: Würfel bauen, heiße Helium-Zelle zurückkühlen, heißen Wolframbarren einfrieren (Hochofen ≥ 2500 K / 3000 K) | 8e.4 | |
| 5.14 | GT-Erze in neuer Welt: Galena/Iridium unter y 32 bzw. 99, Rubin in Wüste/Badlands/Savanne, Saphir an Ozean/Strand, Bauxit in Ebenen/Wäldern (y 32–79); Nether Pyrit/Zinnober/Sphalerit; End-Erze und Asteroiden; Abbau mit/ohne Glück und Behutsamkeit | 8c | |
| 5.15 | Ketten: Lapis → Sodalith → Natrium; Enderperle → Kalium; NaK-60k (nur mit unbenutzter 10k-Zelle); Silberstaub → Blei → Containment-Platte; Bauxit → Titan → Platte; Rubin → Chrom; Pyriterz + Calciumcarbonat im Hochofen | 8a, 8c, 8e.1 | |
| 5.16 | Schaltungskette im Assembler (Olivin → Datenspeicher → Elite-Platine → Datenkontrolle/Energiefluss); Nanosäbel herstellen und einschalten | 8e.3 | |
| 5.17 | Diamantbohrer → Quarry-Rezept; Bergbaulaser-Rezept (Ladung des Energiekristalls geht über); Quantum-Rüstung craften (Ladung übernommen) | 8e.2, 6b.5b | |

## 6. BuildCraft (Phasen 4, 10)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 6.1 | Engines: Animation/Farben, Drehen per BC-Schraubenschlüssel, GUIs + Ledger, Explosion, Kette Engine → Engine | 4 | |
| 6.2 | Öl: Ölquellen in neuer Welt (Wüste/Fontänen, kein „far chunk“ im Log), fließen/aufnehmen, Schwimmen/Nebel, Öl-/Treibstoffeimer in der Combustion Engine | 4 | |
| 6.3 | Landmarks: setzen (Wand/Boden/Decke), verknüpfen, Signallaser mit Redstone, rote Box | 4 | |
| 6.4 | Quarry: Streifenlaser, Roboter mit Energielaser, Rahmenbau, Bohrarm, Abbau in Truhe/als Item-Fontäne/ins Rohr; ohne Landmarks; Welt neu laden mitten im Abbau; Abbau → Rahmen zerfallen | 4, 10.1 | |
| 6.5 | Tank stapeln, mit Eimern füllen/leeren, Flüssigkeitsstand | 4 | |
| 6.6 | BC-Pumpe über Wasser/Lava/Öl mit Engine: Rohr senkt sich, Quellen verschwinden, Tank darüber füllt sich | 4 | |
| 6.7 | Raffinerie: Öl + Engine → Magnete laufen, Treibstoff nur über Behälter/Pumpe (Eimer geht nicht), Filter in der GUI, Drehen | 4 | |
| 6.8 | Mining Well bohrt mit Engine bis Bedrock, Drops in Truhe, Abbau entfernt Rohre | 4 | |
| 6.9 | Item-Rohre: Truhe → Holzrohr (Redstone-Motor) → Strecke → Truhe; Eisenrohr per Schraubenschlüssel/Redstone; Goldrohr-Tempo; Leere-Rohr; Rohr mit Items abbauen (Drops) | 10.1 | |
| 6.10 | Diamantrohr-GUI (Filter setzen/löschen, Sortieren); Smaragdrohr an Truhe mit Motor; Obsidianrohr (Items in 4 Blöcken, Pfeile, Truhenlore, geworfene Items) | 10.2 | |
| 6.11 | Flüssigkeitsrohre: Tank → Holz-Flüssigkeitsrohr → Strecke → Tank, Anzeige waagerecht/senkrecht, Goldrohr-Tempo, Eisenrohr, Sandstein nimmt nichts aus Tanks, Leere-Rohr; Pumpe/Raffinerie/Verbrennungsmotor/Kessel-Dampf am Rohr | 10.3 | |
| 6.12 | Leistungsrohre: Redstone-/Stirling-Engine → leitendes Holzrohr → Stein-/Goldrohre → Quarry/Pumpe/Raffinerie/Fabricator; Strahlen (rot bei Überlast); Holzrohr ohne Abnehmer überhitzt; Strukturrohr an Truhe/Maschine | 10.4 | |

## 7. Forestry und Power Converters (Phase 5)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 7.1 | Fabricator: Glas/Sand schmelzen mit Engine, Hitzebalken, Röhren per Klick/Shift-Klick, Erstarren ohne Energie | 5a | |
| 7.2 | Carpenter: Wassereimer, Muster per Phantom-Klick, Fortschrittsbalken, Tankstufen-Textur | 5a | |
| 7.3 | Lötkolben: Röhren auf Platine löten, Layout-Knöpfe, Drop beim Schließen | 5a | |
| 7.4 | Electrical Engine: Ausrichtung neben Carpenter/Fabricator, Redstone + Kabel/BatBox → Kolben, Partikel, Stammfarbe nach Hitze, Zwangsabkühlung; GUI: EU-Anzeige, Batterie-Slot, Platine einsetzen/mit Lötkolben entnehmen, Tooltip leerer Sockel | 5b | |
| 7.5 | Ledger an Engine/Carpenter/Fabricator: Auf-/Zuklappen, Texte de/en, Fehler-Ledger nur bei Fehler, offener Ledger bleibt fürs nächste GUI offen, EMI überdeckt sie nicht | 11.4 | |
| 7.6 | Energy Bridge + IC2 Consumer an Kabel/Generator → GUI „IN“ und EU/t, Ladebalken; IC2 Producer → nur volle Pakete; BC Consumer mit Engine, BC Producer an Quarry/Pumpe; verbundene Seitentexturen; Rechtsklick auf Bauteil öffnet Bridge-GUI | 5c | |
| 7.7 | Charger: draufstellen lädt Batterien im Inventar, Truhe daneben; formlose Umwandlungsrezepte | 5c | |

## 8. Compact Solars und Kraftwerke (Phasen 6.4, 9)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 8.1 | LV-Array (8 Panels + Controller): Sonnensymbol am Tag, BatBox lädt mit 8 EU/t, Panel abdecken → weniger; MV 9 × 9 (verbaute LV-Controller liefern nichts, 64 EU/t), HV 27 × 27; Lade-Slot; Abbau droppt Controller; Texturen | 6.4b | |
| 8.2 | Wassermühle an fließendem Wasser/im Fluss, zwei Mühlen nebeneinander; Windmühle in großer Höhe bei Gewitter | 9.1 | |
| 8.3 | Koksofen bauen, Kohle per Trichter einspeisen, Koks unten abziehen | 9.2 | |
| 8.4 | Kessel: Wasser per Eimer/IC2-Pumpe, Kohle/Koks nachlegen, Temperaturbalken und Tanks in der GUI, Dampf in BC-Tank daneben; Explosion (Wasser auf trockenen heißen Kessel) aus sicherer Entfernung | 9.3a | |
| 8.5 | Dampfturbine: Kraftwerk aus Kessel + Turbine, MFE/Kabel am Controller, GUI (Dampftank, EU/t, Puffer), bis zu 3 Kessel an einer Turbine | 9.3b | |
| 8.6 | Wasserturbine in einen Fluss (fließendes Wasser rundherum), Rechtsklick-Meldung, Kabel/BatBox am Controller | 9.4 | |

## 9. EMI (Phase 11)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 9.1 | Kategorien IC2 (Macerator, Extractor, Compressor, Recycler, Schrottbox, Massefabrikator, Canning, Elektrolyseur, Generator/Geothermie): Werte, Texte de/en | 11.1 | |
| 9.2 | Kategorien GT (9): Werte, Flüssigkeiten, Hitze beim Hochofen, Wasser beim Grinder | 11.2 | |
| 9.3 | Kategorien BC/Forestry/Kraftwerke: Raffinerie, Verbrennungsmotor, Fabricator (Raster), Carpenter (Raster), Koksofen, Kessel, Dampfturbine; Info-Seiten der Multiblöcke | 11.3 | |
| 9.4 | Klick auf Fortschrittspfeile/Flammen in den GUIs öffnet die passende Kategorie | 11.1–11.3 | |
| 9.5 | `craft:water`/`craft:lava` nicht im Index | 11.2 | |

## Befunde

| Nr | Datum | Befund | Status |
|---|---|---|---|
