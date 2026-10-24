# Testplan – manuelle Client-Tests

Gesammelt aus den „Nächste Schritte“-Abschnitten von `STATUS.md` (Phasen 2–11), nach Mod/Thema geordnet. Was die
GameTests bereits abdecken (Werte, Rezepte, Energiefluss, Multiblock-Erkennung …), steht hier nicht; hier geht es um
Darstellung, GUIs, Sounds, Tasten, Weltgen und Zusammenspiel im echten Client. Spalte „OK“ zum Abhaken (`x`),
Auffälligkeiten unter „Befunde“ am Ende notieren.

**Vorbereitung:** `gradlew runClient` (mit `-PquickPlay=<Welt>` direkt in eine vorhandene Welt), neue Welt (Kreativ, Cheats an); für Explosionen/Reaktor/Nuke eine eigene
Testwelt. Sprache einmal Deutsch, einmal Englisch prüfen. EMI und Xaero's Minimap laufen mit (`localRuntime`).

## 1. Allgemein / Start

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 1.1 | Client startet, Welt betreten, Log ohne Mod-, Modell-, Textur- oder EMI-Plugin-Fehler | 2, 11.1 | x |
| 1.2 | Creative-Tabs vollständig, Item-Darstellung (Modelle/Texturen) stichprobenartig in allen Tabs | 2–10 | x |
| 1.3 | Xaero's Minimap lädt und zeigt die Karte | 11.4 | x |
| 1.4 | Steuerungsmenü: Kategorie „IndustrialCraft 2“ (Modus, Boost, Alt) | 6.2 | x |

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
| 3.21 | Bauschaumsprüher: leer gecraftet, mit Pellets füllen (Balken), Sprühen füllt 13 Blöcke vom Spieler weg, Kabel werden nass geschäumt, Holzgerüst wird zu Schaum (droppt); mit CF-Pack ohne Sprüher-Verbrauch; CF-Pack im Canner füllen (Fortschrittspfeil je Pellet) | 12.2 | |
| 3.22 | Bauschaum härtet bei Licht (im Dunkeln nicht), Sand härtet sofort; Wand färben (Farbroller), Wand droppt nichts, Pick-Block = Bauschaum | 12.2 | |
| 3.23 | Gerüste: Säule bauen (Linksklick mit Gerüst), seitlich auskragen bis Stärke 0, verstärken (Stöcke/Eisenzaun, andere Textur), Unterbau entfernen → Gerüste fallen; Klettern mit Vorwärts, kein Fallschaden | 12.2 | |
| 3.24 | Luminator: an Wand/Decke/Boden, auf Kabel als volle Platte; leuchtet nur mit Strom vom Halte-Kabel, verlischt ohne; Laden per Akku-Rechtsklick; Monster brennen; > 32 EU/t explodiert; Abbau droppt nichts | 12.2 | |
| 3.25 | Miner: GUI (Energieanzeige, Slots nur für Batterie/Scanner/Block/Bohrer), Rohr senkt sich mit Bohrer, Drops in Truhe daneben, Loop-Sound; mit OD/OV-Scanner werden Erze der Ebene seitlich abgebaut; mit IC2-Pumpe daneben Wasser/Lava abpumpen; Bergbaubohrer bleibt an Obsidian hängen; ohne Bohrer Rohre einziehen, Block im Rohr-Slot füllt das Loch; Rohrspitze-Textur | 12.3 | |
| 3.26 | Teleporter: zwei Teleporter mit Frequenzsender verknüpfen (alle Meldungen de/en, Luft-Klick entkoppelt), BatBox/MFE daneben, Hebel → blaue Partikel + Loop, Spieler/Tier/Items springen, Sound und Partikel an beiden Enden; leerer Speicher → nichts; Ziel abbauen → Teleporter vergisst es | 12.4 | |
| 3.27 | Tesla-Spule: mit MV-Strom und Redstone werden Mobs/Spieler im Umkreis 4 alle 1,6 s geschockt (Schaden = Energie/400), danach leer; voller Hazmat-Anzug schützt; > 128 EU/t explodiert | 12.4 | |
| 3.28 | Terraformer: Blaupause per Rechtsklick einsetzen (alte fliegt raus), Schraubenschlüssel wirft aus; mit MFSU-Strom je Blaupause im Umkreis beobachten: Kultivierung (Sand → Erde → Gras + Pflanzen), Bewässerung (Bäume/Weizen wachsen, Gras), Kühlung (Eis, Schnee), Wüste (Sand, Feuer), Einebnung, Pilze (Myzel, Biomfarbe ändert sich in 4×4-Säulen); Loop-Sound; EMI-Infoseiten der Blaupausen | 12.5 | |
| 3.29 | Personal-Blöcke: Safe (Tür öffnet/schließt mit Sound, fremder Spieler bekommt die Meldung, Abbau nur leer mit Schraubenschlüssel, unzerstörbar sonst), Trade-O-Mat (Besitzer-GUI mit Mustern und Zähler, Kunden-GUI mit Vorrat rot/schwarz, Handel aus Truhe/eigenem Safe daneben, Sound), Energy-O-Mat (Knöpfe ±100/±1000, Kunde bezahlt, Strom über die Front rein, an andere Seiten raus; Upgrades) | 12.6 | |
| 3.30 | Crops: Crop-Stab nur auf Ackerboden (fällt ohne ab), Kreuzungsstab per zweitem Stab, Weizensamen pflanzen, Wachstum bei Licht, Ernte mit Rechtsklick (Größe 7 → 2), Pflücken mit Linksklick, Pflege (Eimer, Dünger, Hydrierzelle, Weed-EX), Unkraut auf leeren Stäben, Kreuzung zwischen zwei Pflanzen; Cropnalyzer-GUI scannt Saatbeutel mit Batterie (Anzeige je Stufe), Rechtsklick auf Pflanze gibt Chatmeldung; Saatbeutel pflanzt | 12.7a | |
| 3.31 | Crop-Sorten: Basissaaten (Kürbis-/Melonenkerne, Netherwarze, Terra-Warze, Kaffeebohnen, Zuckerrohr, Kakao, 4 Mohn/Löwenzahn, Karotte, Kartoffel) pflanzen; Sprites aller Wachstumsstufen; Ferru/Aurelia nur über Eisen-/Golderz reif; Rotweizen leuchtet und gibt Redstone-Signal; Venomilia vergiftet bei Größe 5 (Schleichen schützt meist); Netherwarze auf Schneeblock wird Terra-Warze und umgekehrt; Terra-Warze essen entfernt Gift/Schwäche/Übelkeit; EMI-Kategorie „Crops“ | 12.7b | |
| 3.32 | Crop-Matron: Rezept, GUI (Energiebalken links, 3 Spalten nur Dünger/Hydrierzelle/Weed-EX), an BatBox; Feld im Umkreis 4 (y −1..+1) wird gedüngt/bewässert/mit Weed-EX versorgt (Cropnalyzer), Stapel rücken nach, Trichter unten → Weed-EX, oben → Zellen, Seite → Dünger; Abbau → Maschinenblock | 12.7c | |
| 3.33 | Kaffee/Fass: Kaffee aus Bohnen (Werkbank 1, Macerator 3), Tasse + Pulver + Wasser → kalt, Ofen → schwarz, + Zucker + Milch; Trinken gibt Tempo/Eile, viel Kaffee Übelkeit; Fass auf Holzgerüst setzen, mit Wasser/Weizen/Hopfen bzw. Zuckerrohr befüllen, Baumzapfer an die Seite (Textur), mit Steinkrug zapfen (Name je Bier/Rum, Symbol je Reifestufe), Linksklick entfernt Zapfer bzw. gibt Fass mit Inhalt (Name „n L“) und lässt das Gerüst stehen; EMI-Infoseiten | 12.8 | |
| 3.34 | Dynamit: Rezepte (ITNT + Faden → 8, Klebedynamit mit Harz, zwei Fernzünder-Rezepte); werfen (Item-Sprite fliegt, raucht, prallt ab), Klebedynamit bleibt an Wand/Decke kleben und geht schneller hoch, im Wasser keine Explosion; Explosion 3×3×3 (Stein/Erde weg mit Drops, Obsidian bleibt, keine Partikel); Dynamit auf Boden/Wand setzen (Fackel-Modell), Redstone/Abbauen zündet (40 Ticks), Halt entfernen → sofort; Fernzünder verknüpft (Textur wechselt, Tooltip „n Dynamit“), trennt, zündet alle; Werfer schießt Dynamit; Industrie-TNT neben später gesetztem Redstone-Block zündet | 12.9 | |
| 3.36 | Kompositweste: beide Rezepte (Lederharnisch/Eisenbrustplatte unbeschädigt), Rüstungsanzeige 9, Textur am Spieler, Verzaubern, Amboss-Reparatur mit fortg. Legierung | 13.1 | |
| 3.37 | Ladeanzeige: Tooltip „x / y EU“ an Akkus, Werkzeugen, Rüstung; Treibstoff-Jetpack „Treibstoff: n %“; getragene elektrische Rüstung/Jetpacks links neben der Hotbar (Nutzer, B5) mit Symbol und %, gelb ≤ 25 %, rot ≤ 10 %; ausgeblendet mit F1/F3 | 20.2 | x |
| 3.35 | GT-Änderungen: Upgrades stapeln nur bis 4 (Inventar, Shift-Klick in Maschinen-Upgrade-Slots, Creative-Tab); Eis/Schnee im Recycler ergibt nie Schrott (EMI-Blacklist-Seite zeigt sie); Miner mit Scanner + IC2-Pumpe daneben fährt zu Lava-Quellen der Ebene und füllt Lavazellen | 12.10 | |

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
| 5.18 | GT-Stäube (13.3): Macerator Feuerstein → 2 kl. Feuersteinstaub, Bretter/Holzplatte → Holzschliff, Stock/Setzling/Stufe → 2 kl., Tür → 2, Truhe → 8, Holzkohle/Obsidian/Elektrumbarren → Staub, Zaubertisch → 2 Diamantstaub; 8 Holzschliff → Holzplatte (als Brett nutzbar); Elektrolyseur Feuerstein/Holzkohle/Asche/Obsidian/Salpeter/Pyrit/Lazurit; Zentrifuge Elektrum/Netherrack; Werkbank Elektrumstaub, Schießpulver (Kohle 3, Holzkohle 2), 5 ITNT aus Feuersteinstaub, Lappack mit Lazuritbrocken; Schrottbox-Seite in EMI | 13.3 | |
| 5.19 | GT-Rückgewinnung (13.4): unbeschädigte Eisen-/Gold-/Diamant-/Holzwerkzeuge und -rüstung im Macerator (z. B. Eisenbrust → 8 Eisenstaub, Holzaxt → 16 kl. Holzschliff), Eisen/Gold im Ofen → Barren; beschädigte Teile werden weder gemahlen noch geschmolzen; Kettenrüstung → Nugget; kein Eimer aus Zinn mehr | 13.4 | |
| 5.20 | Phase 14: Elektrumplatte im Plattenbieger; Assembler Basis-/fortg. Platine, Schaltkreisteile (Lapis + Glowstone), fortg. Schaltkreis, el. Schaltkreis aus Basis-Platine + 3 isol. Kupferkabel, Frequenzsender; Messingstaub (3 Kupfer + Zink), Messingbarren/-platte, Mischmetall mit Messingplatte; Eisenmörser (Haltbarkeitsbalken, nach 64 Benutzungen Eisenstaub), Feuersteinmörser verbraucht; Mehl → Brot; Compressor 4 Setzlinge / 8 Blätter → kompr. Pflanzenball (Gummiblätter nicht); Solarhelm/Static Boots nur mit unbeschädigtem Eisenhelm/-stiefeln; EMI zeigt alles | 14 | |
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
| 6.13 | IC2-Kühlmittel: kalte 10k/30k/60k-Kühlzelle in den Verbrennungsmotor → 1000/3000/6000 mB im Kühltank (Cyan), erhitzte Zelle wird nicht angenommen; Zelle am BC-Tank leeren, leere Zelle am Tank füllen → 10k-Zelle; EMI-Seite „Kühlmittel 5,0“ | 13.2 | |
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
| 8.1b | Solar seit 2026-10-24: 9×9 aus Solarpanels, BatBox unter einem Panel im Feld lädt 1,2 EU/t (60 EU in 50 Ticks), einzelnes Panel 1 EU/t; Lücke im 9×9 → kein Bonus; altes Array liefert nichts, zerfällt in der Werkbank in 8 der Stufe darunter, nicht in EMI/Kreativtab; Lade-Slot eines alten Arrays entnehmbar | Solar-Umbau 2026-10-24 | |
| 8.1 | (überholt seit 2026-10-24) Solar Arrays (3×3, 2026-10-20): Rezepte SSS/SXS/SSS; LV + 8 Panels: Sonnensymbol am Tag, BatBox lädt mit 19 EU/t, Panel abdecken → weniger; einzelnes Array liefert nichts; MV mit 8 LV-Arrays 179 EU/t (Ring-Arrays liefern selbst nichts), HV mit 8 MV-Arrays 1536 EU/t (Glasfaser/MFSU); HV-Array im MV-Ring → nicht geformt; zweites Feld, das einen Randblock mit einem ersten teilt, liefert nichts (nach Abbau des ersten schon); ein laufendes Feld wird nicht Teil eines anderen; Lade-Slot; Abbau droppt das Array; Texturen | 6.4b, Nachtrag 2026-10-20 | x |
| 8.2 | Wassermühle an fließendem Wasser/im Fluss, zwei Mühlen nebeneinander; Windmühle in großer Höhe bei Gewitter (zerfällt nie) | 9.1 | |
| 8.3 | Koksofen bauen, Kohle per Trichter einspeisen, Koks unten abziehen | 9.2 | |
| 8.4 | Kessel: Wasser per Eimer/IC2-Pumpe, Kohle/Koks nachlegen, Temperaturbalken und Tanks in der GUI, Dampf in BC-Tank daneben; Explosion (Wasser auf trockenen heißen Kessel) aus sicherer Entfernung | 9.3a | |
| 8.5 | Dampfturbine: Kraftwerk aus Kessel + Turbine, MFE/Kabel am Controller und an beliebigem Gehäuseblock, GUI (Dampftank, EU/t, Puffer), bis zu 3 Kessel an einer Turbine | 9.3b | |
| 8.6 | Wasserturbine mit fließendem Wasser rundherum und darüber (z. B. Eimer auf dem Deckel), Rechtsklick-Meldung, Kabel/BatBox am Controller und an beliebigem Gehäuseblock | 9.4 | |
| 8.7 | Hochdruckkessel (5×5×5 hohl): Rezepte, Wasser per Pumpe/Rohr, Kohle/Koks, GUI-Balken bis 1500 °C und 64 000 mB, Feuerung leuchtet; Explosion (Stärke 10) nur aus großer Entfernung | 21 | x |
| 8.8 | Große Dampfturbine (5×5×9, 7 Große Turbinenrotoren): Dampf von 1–3 Hochdruckkesseln, GUI, HV-Kabel/MFSU am Controller und an einem Gehäuseblock, bis 512 EU/t | 21 | x |
| 8.9 | Große Wasserturbine (5×5×5 massiv) im Fluss: Rechtsklick-Meldung (ab 64 fließenden Blöcken), Leistung bis 256 EU/t, Abgabe über jedes Gehäuse | 21 | x |
| 8.10 | Große Windturbine: Gondel auf 8 Masten, Rotor einsetzen/tauschen/mit Schleichen herausnehmen, Rotor dreht sich (5×5) mit richtiger Textur, Rechtsklick-Meldung, Leistung in y 128/y 200/bei Gewitter, Kabel am Mastfuß, Abbau droppt den Rotor; EMI-Info-Seiten der vier Großanlagen | 21 | x |

## 9. EMI (Phase 11)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 9.1 | Kategorien IC2 (Macerator, Extractor, Compressor, Recycler, Schrottbox, Massefabrikator, Canning, Elektrolyseur, Generator/Geothermie): Werte, Texte de/en | 11.1 | |
| 9.2 | Kategorien GT (9): Werte, Flüssigkeiten, Hitze beim Hochofen, Wasser beim Grinder | 11.2 | |
| 9.3 | Kategorien BC/Forestry/Kraftwerke: Raffinerie, Verbrennungsmotor, Fabricator (Raster), Carpenter (Raster), Koksofen, Kessel, Dampfturbine; Info-Seiten der Multiblöcke | 11.3 | |
| 9.4 | Klick auf Fortschrittspfeile/Flammen in den GUIs öffnet die passende Kategorie | 11.1–11.3 | |
| 9.5 | `craft:water`/`craft:lava` nicht im Index | 11.2 | |
| 9.6 | EMI-„+“ im Projekttisch (Zutaten aus Vorrat und Inventar, Shift-+ für viele) und in der Automatischen Werkbank; nur Werkbank-Rezepte | 20.1 | x |

## 10. Iron Chests (Phase 20)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 10.1 | Alle 7 Kisten herstellen (auch Eisenkiste aus Raffiniertem Eisen), platzieren: Front zum Spieler, Modell und Textur richtig herum, Deckel öffnet mit Geräusch; GUI-Größe und Slots (Kupfer 45 … Diamant 108), Shift-Klick in beide Richtungen; Block darüber verhindert das Öffnen | 20.4 | x |
| 10.2 | Kristallkiste: durchsichtig, die häufigsten Items drehen sich darin (ein Item groß in der Mitte) | 20.4 | x |
| 10.3 | Upgrades: Holzkiste (auch eine Hälfte einer Doppelkiste) → Eisen/Kupfer, Eisen → Gold → Diamant → Kristall/Obsidian; Inhalt und Blickrichtung bleiben, Upgrade verbraucht; falsches Upgrade öffnet nur die Kiste | 20.4 | x |
| 10.4 | Obsidiankiste neben TNT bleibt stehen; Abbau mit Spitzhacke droppt Kiste und Inhalt; Trichter/BC-Rohr füllt und leert; Komparator zeigt den Füllstand; zahme Katze setzt sich auf eine Iron Chest; Items im Inventar/in der Hand als Kiste dargestellt | 20.4 | x |

## 11. GraviSuite (Phase 27)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 11.1 | Alle Items im IC2-Tab (geladen + leer), Texturen, Rüstungsmodelle (Jetpack, Nano-, Gravitations-Brustplatte, LapPacks, Solarhelm); HUD oben rechts neben Xaero | 27.1–27.3 | |
| 11.2 | Fortg. Jetpack/Nano-Brustplatte: G schaltet Triebwerk (Meldung), Springen hebt mit Loop-Sound, Strg-Boost, M beim Springen = Schwebemodus | 27.2 | |
| 11.3 | Gravitations-Brustplatte: G ab 80 000 EU Flug (Meldung), Boost mit Strg/Leertaste/Schleichen, Abschalten bei leerer Ladung, Ablegen nimmt den Flug, Welt neu laden | 27.3 | |
| 11.4 | Fortg. Bohrer/Kettensäge (Tempo, Wolle), Vajra: Sofortabbau, Rechtsklick entfernt Block mit Drops und Geräusch, Grundgestein bleibt | 27.4 | |
| 11.5 | GraviTool: M + Rechtsklick wechselt Modus (Meldung, Sound, Modell und Name wechseln); Hacke, Baumzapfer am Gummibaum, Schraubenschlüssel an IC2-Maschine (verlustfrei) und BC-Motor (dreht, 500 EU), Schraubendreher an Verstärker/Werfer/Kolben | 27.5 | |
| 11.6 | Ultimativer Solarhelm (Kreativ): lädt am Tag unter freiem Himmel schnell, nachts langsam, unter Dach nicht; EMI-Info-Seiten aller GraviSuite-Items | 27.6 | |

## 12. Modular Powersuits (Phase 28)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 12.1 | Kreativ-Tab „Craft: Modular Powersuits“, Texturen der Bauteile/Teile, Tinker Table (Modell, leuchtender Würfel, Item in der Hand); Rezepte in EMI inkl. der 4 GT-Rezepte, Info-Seiten | 28.1, 28.5 | |
| 12.2 | Tinker Table: Teil wählen, Batterie einbauen (Bauteile weg, Ladebonus), Regler (Werte im Info-Rahmen), Ausbauen (90 %-Rückgabe, Kondensator weg); Power-Rüstung am IC2-Speicher laden, Tooltip „Energie“ | 28.1 | |
| 12.3 | Bewegung: Sprint-/Sprunghilfe, Stufenhilfe, Jetpack + Düsenstiefel mit/ohne Flugsteuerung (Schweben, Z runter), Gleiter/Fallschirm beim Schleichen, Schwimmhilfe, Stoßdämpfer, Gewicht > 25 kg bremst | 28.2 | |
| 12.4 | Umwelt: Nachtsicht, Tarnung (Effekte ohne Partikel), Auto-Feeder isst bei Hunger (Meldung), Elektrolyseur unter Wasser, Solar/Kinetik laden; K-GUI: Taste anlegen, Modul binden, Taste schaltet aus/an (Meldung), Datei bleibt nach Neustart | 28.3 | |
| 12.5 | Kosmetik: Farbmodul färbt Icon und getragene Rüstung, Durchsichtig blendet aus, Citizen-Joe-Textur | 28.3 | |
| 12.6 | Power-Werkzeug: Stein/Erde/Holz/Obsidian abbauen (Tempo, Energie je Block), Nahkampfhilfe; Schleichen + Mausrad wechselt Modus (Icons über der Hotbar, Hotbar bleibt), Railgun (Partikelspur, Rückstoß), Blink Drive, Plasmakanone (aufladen, Kugel, Explosion), In-Place Assembler (Werkbank bleibt offen) | 28.4 | |

## 13. Elektrischer Leuchtschild (Phase 30)

| Nr | Test | Herkunft | OK |
|---|---|---|---|
| 13.1 | IC2-Tab (leer + voll), Rezept und Info-Seite in EMI; Modell im Inventar, in Haupt-/Nebenhand (1./3. Person), Blocken-Pose, Leuchtfläche bei Licht an hell, bei aus normal; Ladebalken, Tooltip „Licht an/aus“ | 30.1, 30.2 | |
| 13.2 | Lichtkegel (mit LambDynamicLights): nachts/in einer Höhle Kegel in Blickrichtung (~16 Blöcke, weicher Rand), Schein um die Füße, Wand hell und dahinter dunkel, unter Wasser; folgt beim Laufen und Umsehen flüssig, kein Ruckeln; zweiter Spieler sieht den Kegel; Schild weglegen/leer/Modus-Taste + Rechtsklick (Meldung) → Kegel weg; Mobs spawnen weiter; alte Welt mit Lichtblock aus 1.0.8 → Rest verschwindet | 30.1, 30.4 | |
| 13.3 | Blocken: Zombie-/Skelett-Treffer kosten EU, leer lässt er sich nicht heben, beim Leerlaufen senkt er sich; Axt sperrt ihn | 30.1 | |
| 13.4 | Zaubertisch bietet Haltbarkeit an; Amboss mit Reparatur-Buch, Erfahrung lädt den Schild; BatBox/Ladegerät lädt | 30.1 | |

## Befunde

| Nr | Datum | Befund | Status |
|---|---|---|---|
| B1 | 2026-10-17 | EMI meldete 82 unübersetzte Item-Tags (`c:plates/*`, `c:ingots/*`, `craft:*` …) | behoben (Tag-Namen en/de im Sprach-Datagen) |
| B2 | 2026-10-17 | Gummibaumblätter mit lila Platzhaltertextur (IC2 nutzt Index 52 aus Vanilla-`terrain.png` = Eichenlaub) | behoben (Vanilla-`oak_leaves`, Birkenfarbe wie bisher) |
| B4 | 2026-10-20 | Zahme Katzen setzten sich nicht auf Iron Chests, sondern blieben davor stehen: `IronChestBlock` meldete der Wegfindung „begehbar“ (Standard für nicht volle Blöcke), die Katze plante den Weg durch die Kiste. Vanilla-Truhe: `isPathfindable` = false | behoben (0.4.2, `isPathfindable` = false, GameTest `catsSitOnIronChests` prüft die Wegfindung) |
| B5 | 2026-10-20 | Ladeanzeige (HUD oben links) liegt über der Xaero-Minimap, die ebenfalls oben links sitzt | behoben 2026-10-20: HUD links neben die Hotbar verschoben (Nutzer); Client-Prüfung offen |
| B6 | 2026-10-20 | Shift-„+“ in EMI leert ein Raster, in dem schon ein Rezept liegt, statt es aufzufüllen (Projekttisch, Automatische Werkbank) | kein Mod-Fehler: Vanilla-Werkbank verhält sich in EMI 1.1.22 genauso; bei leerem Raster füllt Shift-„+“ korrekt |
| B3 | 2026-10-17 | Nachtsichtbrille: Icon ist eine lila Fläche – im Pack identisch (`item_0.png` Index 232 ist dort Platzhalter) | behoben: eigenes Icon (Nutzerwunsch 2026-10-17, `abweichungen.md`) |

## Durchlauf 2026-10-17 (automatisiert im echten Client)

Client per `gradlew runClient -PquickPlay=<Welt>` gestartet, Eingaben per Skript (Tasten, Klicks, Chat-Befehle),
Auswertung über Bildschirmfotos und Log; Sprache en und de.

- 1.1–1.4 vollständig (siehe Tabelle).
- GUIs: alle 72 Blöcke mit Menü einzeln gesetzt und geöffnet – jede GUI zeichnet korrekt (Titel, Slots, Balken,
  Statustexte wie „Structure incomplete“/„Incomplete Machine Casing!“), keine Fehler im Log. Blöcke ohne GUI
  (Trafos, Konverter, Tank, Pumpe, Quarry, Teleporter, Tesla-Spule, Terraformer, Magnetisierer …) reagieren nicht.
- Item-Darstellung: alle Seiten der Tabs IC2/GT/BC/Forestry/Power Converters/Compact Solars/Kraftwerke gesichtet;
  Platzhalter-Suche über alle Texturen (B2, B3).
- EMI: Rezept (R), Verwendung (U), eigene Kategorie (Extractor mit EU/t · Dauer · EU), Suche, neue Schwefelzelle in der
  Automatic Canning Machine; deutsche Namen.
- Gummibaum: Blätter mit Birkenfarbe gerendert (2.6 nur Blätterfarbe, Weltgen/Harz offen).

Nicht automatisiert prüfbar und weiter offen: Sounds, Animationen im Betrieb, Explosionen, Rüstungs-/Werkzeugverhalten,
Weltgen-Begehung, Multiblock-Aufbau im Spiel – alle übrigen Zeilen ohne `x`.

## Durchlauf 2026-10-20 (automatisiert im echten Client, Version 0.4.1)

Testwelt `ClaudeTest`, Aufbauten per Datapack-Funktionen (`datapacks/abnahme`), Auswertung über Bildschirmfotos,
Blick-Overlay, Chat-Meldungen und `/data get`; Sprache en.

- 8.1 Solar-Felder: LV 19, MV 179, HV 1536 EU/t; einzelnes Array 0; HV-Array im MV-Ring 0; zweites Feld mit geteiltem
  Rand 0, nach Abbau des ersten 19; abgedecktes Panel 18; Sonnensymbol und Lade-Slot in der GUI; Abbau droppt das
  Array; Rezept in EMI. Oberseiten der drei Stufen gleich, Seiten verschieden (wie das Original-Texturblatt).
- 8.7–8.10 Großkraftwerke: Hochdruckkessel 1500 °C, Feuerung leuchtet, Asche; Große Dampfturbine 160 EU/t mit einem
  Kessel, 512 EU/t (Deckel) mit vollem Dampftank; Große Wasserturbine „Flowing water: 334, output 256.00 EU/t“; Große
  Windturbine in y 159: Carbon 119 EU/t, Eisen 59,5 EU/t, Gewitter 357 EU/t, Rotor dreht sich (≈ 5 Blöcke), Tausch und
  Herausnehmen mit Schleichen, Abbau droppt Gondel und Rotor; MFSUs laden am Mastfuß und an allen Gehäusen; Kessel
  bei 1500 °C trockengelaufen → Explosion Stärke 10. EMI: Rezepte und Info-Seiten aller vier Anlagen, Rotor-Tooltip.
- 3.37 Ladeanzeige: Tooltips „100 000 / 100 000 EU“, „Fuel: 20 %“; HUD 100 % weiß, 20 % gelb, 5 % rot, bei F3 aus (B5).
- 9.6 / Phase 18: EMI-„+“ füllt den Projekttisch aus dem Vorrat und die Automatische Werkbank aus dem Inventar;
  Shift-Klick aufs Ergebnis stellt her, bis die letzte Nutzung erreicht ist (Raster bleibt); Bauplan beschreiben
  („Chest Plan – 8 × Oak Planks“, cyan), Geister-Zutaten rot bei leerem Vorrat (B6).
- Phase 19: Long Fall Boots – 25 Blöcke Fall ohne Schaden, Stiefel unbeschädigt (Diamantstiefel 15 Blöcke: 8,8 Schaden).
- 10.1–10.4 Iron Chests: alle 7 GUIs (45/54/72/81/108/108/108), Block darüber verhindert Öffnen, Front zum Spieler,
  Kristallkiste mit drehendem Inhalt (8 Plätze bzw. ein Item mittig), Upgrades inkl. Doppelkisten-Hälfte, Inhalt und
  Richtung bleiben, falsches Upgrade öffnet nur, Trichter füllt und leert, Komparator, Obsidiankiste übersteht TNT,
  Abbau droppt Kiste + Inhalt, Kiste in Hand/Hotbar als Modell; Katzen (B4, behoben).

Nicht geprüft: Geräusche, deutsche Texte im Spiel, Windturbine in y 128/200 (Formel durch GameTests abgedeckt).
Hinweis: Bildschirmfotos auf diesem Rechner wirken aufgehellt (auch Vanilla-GUIs); das liegt an der Aufnahme, nicht
am Spiel.

