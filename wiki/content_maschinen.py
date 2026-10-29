"""What every machine does and what it is for, shown on its item page (section "Was macht das?").

Keys are item refs, values (Funktion, Wofür). Numbers only where they help to choose; the exact energy values are on
the page "Energie-Steckbrief", the details in docs/spec.
"""

MACHINES = {
    # ------------------------------------------------------------------ IC2: generators
    'craft:generator': (
        'Verbrennt Brennstoffe wie ein Ofen (Kohle, Holz, Koks …) und macht daraus Strom: 10 EU/t, eine Kohle ergibt '
        '4000 EU.',
        'Der erste Stromerzeuger am Anfang. Später nur noch Notstrom oder Bauteil (Rezepte von Windmühle, '
        'Wassermühle, Solarpanel, Dampfturbine).'),
    'craft:geothermal_generator': (
        'Verbraucht Lava (Eimer, Lavazellen oder per Rohr) und gibt 20 EU/t ab; ein Eimer Lava = 20 000 EU.',
        'Strom aus Lava, z. B. aus dem Nether oder mit einer Pumpe aus einem Lavasee. Deutlich stärker als der '
        'Generator.'),
    'craft:solar_panel': (
        'Erzeugt bei Tag mit freier Sicht zum Himmel 1 EU/t, nachts und bei Regen nichts. In Craft: Panels in einem '
        'lückenlosen 9×9-Quadrat liefern je ×1,2.',
        'Wartungsfreier Strom ohne Brennstoff. In Craft die einzige Solarquelle – viele Panels im 9×9-Feld statt '
        'Solar Arrays.'),
    'craft:water_mill': (
        'Zählt alle 6 Sekunden das Wasser im Bereich 5×5×3 um sich (fließend zählt doppelt) und erzeugt daraus '
        'bis zu 2 EU/t; Fluss-Biome ×1,5, Ozean ×1,25. Mühlen nah beieinander teilen sich das Wasser.',
        'Kleiner Dauerstrom am Wasser. Größer geht es mit Wasserturbine und Großer Wasserturbine.'),
    'craft:wind_mill': (
        'Erzeugt Strom aus Wind: je höher und freier (keine Blöcke ringsum), desto mehr; bei Regen ×1,5, Gewitter '
        '×3, höchstens 16 EU/t. Geht in Craft nie kaputt.',
        'Strom ohne Brennstoff, hoch oben auf Türmen oder Bergen. Die große Variante ist die Große Windturbine.'),
    'craft:nuclear_reactor': (
        'Uranzellen erzeugen Energie und Hitze; Kühlelemente, Wärmetauscher und Platten im Raster führen die Hitze '
        'ab. Zu viel Hitze lässt den Reaktor schmelzen oder explodieren.',
        'Sehr viel Strom, aber nur mit durchdachtem Aufbau (Reaktorplaner, Anleitung „Reaktor“).'),
    'craft:reactor_chamber': (
        'Erweitert einen angrenzenden Kernreaktor um eine Spalte Plätze (bis 6 Kammern) und kühlt etwas mit.',
        'Mehr Platz im Reaktor für größere, stärkere Aufbauten.'),
    # ------------------------------------------------------------------ IC2: storage, transformers, cables
    'craft:batbox': (
        'Speichert 40 000 EU und gibt sie mit bis zu 32 EU/t (LV) an der Vorderseite ab; nimmt von allen anderen '
        'Seiten an. Lädt und entlädt Werkzeuge in seinen Slots.',
        'Erster Stromspeicher: Puffer zwischen Erzeuger und Maschinen, Ladestation.'),
    'craft:mfe': (
        'Speichert 600 000 EU, Abgabe bis 128 EU/t (MV) an der Vorderseite.',
        'Mittlerer Speicher und MV-Netz, z. B. für Massefabrikator oder viele Maschinen.'),
    'craft:mfsu': (
        'Speichert 10 000 000 EU, Abgabe bis 512 EU/t (HV) an der Vorderseite.',
        'Großer Speicher für Kraftwerke und Hochspannungsnetze; lädt auch Quanten-Rüstung und Lapotronkristalle.'),
    'craft:lv_transformer': (
        'Setzt zwischen 128 EU (MV) und 32 EU (LV) um: normal kommt die hohe Spannung an der Vorderseite hinein und '
        'die niedrige an den anderen fünf Seiten heraus; mit Redstone-Signal umgekehrt (hochsetzen).',
        'LV-Maschinen an einem MV-Netz betreiben, ohne dass sie explodieren.'),
    'craft:mv_transformer': (
        'Setzt zwischen 512 EU (HV) und 128 EU (MV) um, Seiten und Redstone wie beim LV-Transformator.',
        'MV-Geräte an HV-Netzen (z. B. MFSU, Großkraftwerke).'),
    'craft:hv_transformer': (
        'Setzt zwischen 2048 EU (EV) und 512 EU (HV) um, Seiten und Redstone wie beim LV-Transformator.',
        'EV-Leistung herunter auf HV bringen (oder umgekehrt).'),
    'craft:splitter_cable': (
        'Kabel, das den Strom sperrt, solange es ein Redstone-Signal bekommt.',
        'Netzteile per Hebel oder Schaltung abschalten.'),
    'craft:detector_cable': (
        'Kabel, das ein Redstone-Signal ausgibt, solange Strom hindurchfließt.',
        'Anzeigen oder Schaltungen, die auf Stromfluss reagieren (z. B. Speicher voll/leer).'),
    # ------------------------------------------------------------------ IC2: machines
    'craft:iron_furnace': (
        'Ofen mit Brennstoff wie der Vanilla-Ofen, aber schneller (160 statt 200 Ticks) und sparsamer.',
        'Besserer Ofen vor dem ersten Strom; Bauteil des Elektroofens.'),
    'craft:electric_furnace': (
        'Schmilzt mit Strom wie ein Ofen, schneller als der Eisenofen. Upgrades (Übertakter, Speicher, Transformator) '
        'passen hinein.',
        'Standard-Ofen für alles, sobald Strom da ist; Grundlage des Induktionsofens.'),
    'craft:induction_furnace': (
        'Elektroofen mit zwei Eingängen, die gleichzeitig schmelzen. Wärmt sich unter Strom langsam auf und wird '
        'dabei immer schneller.',
        'Massenhaft Erze/Staub schmelzen, sobald er warm ist.'),
    'craft:macerator': (
        'Zermahlt Erze zu 2 Staub (doppelte Ausbeute), dazu Bruchstein zu Kies, Kies zu Feuerstein u. v. m.',
        'Erzverdopplung – eine der ersten Maschinen, die man bauen sollte.'),
    'craft:extractor': (
        'Presst Stoffe aus: Harz → 3 Gummi, Gummiholz → Gummi, Erdzellen → Wasser usw.',
        'Gummi für Kabelisolierung und viele Bauteile.'),
    'craft:compressor': (
        'Verdichtet: Staub/Platten zu Blöcken, Schneebälle zu Eis, Kohlefaser zu Kohlenstoffplatten, Mischmetall zu '
        'Fortgeschrittener Legierung u. v. m.',
        'Bauteile für Rüstung und fortgeschrittene Maschinen.'),
    'craft:recycler': (
        'Zerstört jedes eingelegte Item und gibt mit 1/8 Chance Schrott aus.',
        'Müll (Erde, Bruchstein) in Schrott für Schrottboxen oder als Verstärker im Massefabrikator verwandeln.'),
    'craft:canning_machine': (
        'Füllt Dosen mit Essen (Kaffee, Nahrung) und Brennstoffkanister mit Kraftstoff.',
        'Essen haltbar machen und Brennstoff für den Jetpack.'),
    'craft:mass_fabricator': (
        'Wandelt sehr viel Strom (1 000 000 EU je Stück) in UU-Materie um. Schrott als Verstärker lässt jede EU '
        'sechsfach zählen, bis sein Wert aufgebraucht ist. Redstone-Signal schaltet ihn ab.',
        'UU-Materie für seltene Rohstoffe (Diamanten, Iridium …) aus Strom.'),
    'craft:electrolyzer': (
        'Steht neben einem Speicher (BatBox/MFE/MFSU): ist er über 70 % voll, macht er aus Wasserzellen '
        'elektrolysierte Wasserzellen; unter 30 % gibt er die Energie daraus zurück.',
        'Energiepuffer für schwankende Stromquellen; Teil der Energiekristall-Herstellung.'),
    'craft:magnetizer': (
        'Magnetisiert senkrechte Eisenzaun-Säulen über und unter sich (je bis 20 Blöcke).',
        'Aufzug: mit Metallstiefeln am magnetisierten Zaun hochschweben, schleichend langsam herunter.'),
    'craft:miner': (
        'Bohrt mit Bergbaurohren senkrecht nach unten und baut Erze im Umkreis ab, wenn ein OD- oder OV-Scanner '
        'eingelegt ist; braucht einen Bohrer.',
        'Automatischer Erzabbau ohne Löcher in der Landschaft (die Rohre holt er beim Abbau wieder zurück).'),
    'craft:ic2_pump': (
        'Pumpt Flüssigkeit unter oder neben sich ab und füllt sie in Zellen/Eimer oder gibt sie an Rohre ab; mit '
        'Bergbaurohren auch aus der Tiefe.',
        'Lava für Geothermiegeneratoren, Wasser für Kessel.'),
    'craft:teleporter': (
        'Teleportiert Spieler und Tiere, die auf ihm stehen, zu einem verbundenen Teleporter (mit dem '
        'Frequenzsender verbinden). Kostet je nach Entfernung und Gewicht des Inventars viel Strom.',
        'Schnelles Reisen zwischen Basen.'),
    'craft:tesla_coil': (
        'Schießt mit Redstone-Signal Blitze auf Monster (und Spieler ohne Schutz) in der Nähe.',
        'Basisverteidigung.'),
    'craft:terraformer': (
        'Verändert die Landschaft im Umkreis nach eingelegtem Bauplan (Kultivierung, Bewässerung, Abkühlung, '
        'Wüste, Einebnen, Pilze).',
        'Große Flächen umgestalten, z. B. Wüste begrünen oder Gelände einebnen.'),
    'craft:crop_matron': (
        'Versorgt die IC2-Pflanzen (Crop-Stäbe) im Bereich 9×3×9 mit Dünger, Wasser aus Hydrierzellen und '
        'Unkrautvernichter (Weed-EX) aus seinen Slots.',
        'Crop-Farmen automatisch pflegen.'),
    'craft:luminator': (
        'Elektrische Lampe, an jede Fläche setzbar; leuchtet hell, solange sie über das Kabel dahinter Strom bekommt '
        '(1 EU alle 4 Ticks). Monster, die sie berühren, fangen Feuer.',
        'Licht ohne Fackeln, z. B. in Maschinenräumen.'),
    'craft:personal_safe': (
        'Kiste, die nur ihr Besitzer öffnen und abbauen kann; nahezu unzerstörbar.',
        'Wertsachen auf Servern schützen.'),
    'craft:trade_o_mat': (
        'Verkaufsautomat: der Besitzer legt Ware und Preis fest, andere Spieler tauschen ohne ihn.',
        'Handel auf Servern, auch wenn man nicht online ist.'),
    'craft:energy_o_mat': (
        'Verkauft Strom: Käufer legen den Preis hinein und bekommen EU an der Ausgabe.',
        'Strom an andere Spieler verkaufen.'),
    'craft:booze_barrel': (
        'Braut aus Weizen, Hopfen und Wasser Bier oder aus Zuckerrohr Rum; je länger es reift, desto stärker.',
        'Getränke mit Effekten (Bier, Rum).'),
    # ------------------------------------------------------------------ Advanced Machines
    'craft:rotary_macerator': (
        'Macerator, der unter Strom hochdreht und dann viel schneller arbeitet; macht aus Eisen raffinierten '
        'Eisenstaub.',
        'Erze in Massen zermahlen.'),
    'craft:singularity_compressor': (
        'Kompressor, der unter Strom hochdreht und dann viel schneller arbeitet.',
        'Kompressor-Rezepte in Massen.'),
    'craft:centrifuge_extractor': (
        'Extraktor, der unter Strom hochdreht und dann viel schneller arbeitet.',
        'Gummi und andere Extraktor-Rezepte in Massen.'),
    # ------------------------------------------------------------------ GregTech
    'craft:industrial_blast_furnace': (
        'Multiblock (Gehäuse + Heizspulen um einen Lavakern) mit Strom; erreicht hohe Temperaturen und schmilzt, '
        'was ein Ofen nicht kann.',
        'Stahl, Titan, Wolframstahl, Iridium und andere GT-Metalle.'),
    'craft:industrial_electrolyzer': (
        'Zerlegt Stoffe (Staub, Zellen) mit Strom in ihre Bestandteile, z. B. Wasser in Wasserstoff und Sauerstoff.',
        'Seltene Elemente (Natrium, Aluminium, Titan …) aus Staub gewinnen.'),
    'craft:industrial_centrifuge': (
        'Trennt Stoffe mit Strom auf, z. B. Lava in Metallstäube, Staubmischungen in ihre Bestandteile.',
        'Nebenprodukte und Metalle aus Lava und Staub.'),
    'craft:industrial_grinder': (
        'Multiblock-Mühle, mahlt Erze mit Wasser (oder anderen Flüssigkeiten) und gibt mehr Staub plus '
        'Nebenprodukte.',
        'Bessere Erzausbeute als der Macerator.'),
    'craft:plate_bending_machine': (
        'Walzt Barren zu Platten (und Platten weiter zu dünneren Teilen).',
        'Platten für GT-Rezepte, Gehäuse und Maschinen.'),
    'craft:assembling_machine': (
        'Setzt Bauteile mit Strom zusammen, z. B. Schaltkreise aus Platine, Draht und Teilen.',
        'Schaltkreise und Bauteile günstiger als in der Werkbank.'),
    'craft:automatic_canning_machine': (
        'GT-Konservierungsmaschine: füllt und leert Zellen und Dosen mit Strom.',
        'Zellen befüllen (z. B. für Elektrolyseur und Zentrifuge).'),
    'craft:implosion_compressor': (
        'Multiblock, der Stoffe mit Industrie-TNT zusammendrückt.',
        'Iridiumplatten und künstliche Edelsteine.'),
    'craft:vacuum_freezer': (
        'Multiblock, der mit Strom extrem kühlt.',
        'Heiße Barren (z. B. Wolframstahl) abkühlen und Kühlzellen herstellen.'),
    # ------------------------------------------------------------------ BuildCraft
    'craft:quarry': (
        'Baut mit MJ ein ganzes Feld bis zum Grundgestein ab (Größe mit Landmarken festlegen, sonst 9×9) und gibt '
        'alle Blöcke in eine Kiste oder ein Rohr daneben.',
        'Riesige Mengen Erz und Stein ohne eigenes Graben.'),
    'craft:landmark': (
        'Markierung: drei Landmarken im Rechteck aufstellen und eine mit Redstone aktivieren – sie zeigen das Feld '
        'mit Lasern an.',
        'Abbaufeld der Quarry festlegen.'),
    'craft:mining_well': (
        'Bohrt mit MJ senkrecht nach unten und holt jeden Block der 1×1-Säule heraus.',
        'Einfacher automatischer Abbau oder Schacht graben.'),
    'craft:pump': (
        'Pumpt mit MJ Flüssigkeiten (Wasser, Lava, Öl) aus der Welt, auch ganze Seen.',
        'Öl für die Raffinerie, Lava für Generatoren.'),
    'craft:tank': (
        'Speichert 16 Eimer einer Flüssigkeit; übereinander gestapelte Tanks bilden einen großen Tank.',
        'Flüssigkeiten lagern und puffern.'),
    'craft:refinery': (
        'Macht mit MJ aus Öl Kraftstoff.',
        'Kraftstoff für den Verbrennungsmotor (bringt viel mehr als Öl).'),
    'craft:auto_workbench': (
        'Werkbank, die ihr Rezept automatisch wiederholt, sobald die Zutaten im Vorrat liegen; Ergebnis per Rohr '
        'oder Trichter entnehmen.',
        'Rezepte automatisch herstellen, z. B. Kabel oder Platten in Massen.'),
    'craft:redstone_engine': (
        'Motor, der mit Redstone-Signal sehr wenig MJ liefert, ohne Brennstoff; überhitzt nie.',
        'Holzrohre antreiben (Items aus Kisten ziehen).'),
    'craft:stirling_engine': (
        'Verbrennt Brennstoffe und liefert MJ; kann überhitzen, wenn die Energie nicht abgenommen wird.',
        'Erste MJ-Quelle für Pumpe, Quarry oder Mining Well.'),
    'craft:combustion_engine': (
        'Verbrennt Öl oder Kraftstoff und liefert viel MJ; braucht Wasser zur Kühlung, sonst überhitzt und '
        'explodiert er.',
        'Starke MJ-Quelle für Quarry und Raffinerie.'),
    'craft:wooden_transport_pipe': (
        'Zieht mit MJ (z. B. Redstone-Motor) Items aus der angrenzenden Kiste oder Maschine ins Rohrnetz. Verbindet '
        'sich nicht mit anderen Holzrohren.',
        'Anfang jeder Item-Leitung.'),
    'craft:cobblestone_transport_pipe': (
        'Einfaches Item-Rohr; verbindet sich nicht mit Steinrohren.',
        'Billige Leitungen; mit Steinrohr nebeneinander verlegen, ohne dass sie sich verbinden.'),
    'craft:stone_transport_pipe': (
        'Einfaches Item-Rohr, etwas schneller als Bruchstein; verbindet sich nicht mit Bruchsteinrohren.',
        'Standard-Leitung.'),
    'craft:sandstone_transport_pipe': (
        'Item-Rohr, das sich nur mit anderen Rohren verbindet, nie mit Kisten oder Maschinen.',
        'An Maschinen vorbeiführen, ohne dass Items hineinlaufen.'),
    'craft:iron_transport_pipe': (
        'Gibt Items nur in eine Richtung aus; umstellen mit Schraubenschlüssel oder Redstone-Wechsel.',
        'Items gezielt in eine Richtung lenken, Rückfluss verhindern.'),
    'craft:golden_transport_pipe': (
        'Beschleunigt die Items, die hindurchlaufen.',
        'Lange Leitungen schnell halten.'),
    'craft:diamond_transport_pipe': (
        'Sortiert: je Seite legt man im Menü Filter fest, passende Items gehen dorthin, andere über freie Seiten.',
        'Sortieranlagen.'),
    'craft:obsidian_transport_pipe': (
        'Saugt herumliegende Items in der Nähe ein (mit MJ weiter weg) und gibt sie ins Rohrnetz.',
        'Drops einsammeln, z. B. an Farmen.'),
    'craft:emerald_transport_pipe': (
        'Wie das Holzrohr, zieht aber nur die im Filter eingestellten Items aus der Kiste.',
        'Gezielt bestimmte Items aus einem Lager holen.'),
    'craft:void_transport_pipe': (
        'Vernichtet jedes Item, das hineinkommt.',
        'Überschuss (z. B. Bruchstein aus der Quarry) entsorgen.'),
    'craft:distribution_transport_pipe': (
        'Verteilt Items nach eingestellten Prozentwerten exakt auf die Seiten; Stapel werden dafür geteilt.',
        'Gleichmäßig auf mehrere Maschinen verteilen (z. B. 50/50 auf zwei Macerator).'),
    'craft:wooden_fluid_pipe': (
        'Zieht mit MJ Flüssigkeit aus dem angrenzenden Tank oder der Maschine.',
        'Anfang jeder Flüssigkeitsleitung.'),
    'craft:cobblestone_fluid_pipe': (
        'Einfaches Flüssigkeitsrohr; verbindet sich nicht mit Steinrohren.',
        'Billige Flüssigkeitsleitungen.'),
    'craft:stone_fluid_pipe': (
        'Einfaches Flüssigkeitsrohr; verbindet sich nicht mit Bruchsteinrohren.',
        'Standard-Flüssigkeitsleitung.'),
    'craft:sandstone_fluid_pipe': (
        'Flüssigkeitsrohr, das sich nur mit Rohren verbindet.',
        'An Tanks und Maschinen vorbeiführen.'),
    'craft:iron_fluid_pipe': (
        'Gibt Flüssigkeit nur in eine Richtung aus (Schraubenschlüssel/Redstone).',
        'Flüssigkeit gezielt lenken.'),
    'craft:golden_fluid_pipe': (
        'Flüssigkeitsrohr mit viermal so viel Durchfluss.',
        'Viel Flüssigkeit (Dampf, Lava) schnell bewegen.'),
    'craft:emerald_fluid_pipe': (
        'Wie das Holz-Flüssigkeitsrohr, aber mit dem Durchfluss des Goldrohrs.',
        'Viel Flüssigkeit aus einem Tank ziehen.'),
    'craft:void_fluid_pipe': (
        'Vernichtet jede Flüssigkeit, die hineinkommt.',
        'Überschüssige Flüssigkeit entsorgen.'),
    'craft:wooden_power_pipe': (
        'Nimmt MJ von Motoren auf und gibt sie an angeschlossene Leistungsrohre weiter.',
        'Anfang einer MJ-Leitung (Motor → Holz → Stein/Gold → Maschine).'),
    'craft:stone_power_pipe': (
        'Leitet MJ mit etwas Verlust.',
        'MJ über kurze Strecken.'),
    'craft:golden_power_pipe': (
        'Leitet MJ mit wenig Verlust.',
        'MJ über längere Strecken.'),
    'craft:cobblestone_structure_pipe': (
        'Transportiert nichts, verbindet sich nur optisch.',
        'Deko und Stützen.'),
    # ------------------------------------------------------------------ Forestry, Power Converters
    'craft:electrical_engine': (
        'Wandelt EU in MJ um; Elektronenröhren erhöhen die Leistung.',
        'IC2-Strom für BuildCraft-Maschinen (Quarry, Pumpe).'),
    'craft:thermionic_fabricator': (
        'Schmilzt Sand/Glas zu flüssigem Glas (mit MJ) und formt daraus Elektronenröhren.',
        'Elektronenröhren für den Elektrischen Motor.'),
    'craft:carpenter': (
        'Werkbank mit MJ und Flüssigkeitstank, z. B. für Platinen aus Wasser und Bauteilen.',
        'Leiterplatten und Forestry-Bauteile.'),
    'craft:energy_bridge': (
        'Herzstück der Power Converters: sammelt Energie von angrenzenden Consumern und verteilt sie an angrenzende '
        'Producer, egal in welchem System.',
        'EU, MJ und FE ineinander umwandeln.'),
    'craft:universal_charger': (
        'Lädt elektrische Items mit Energie aus der Energy Bridge, im eigenen Slot oder das Inventar des Spielers '
        'darauf.',
        'IC2-Werkzeuge an einem fremden Energienetz laden.'),
    'craft:bc_consumer': (
        'Nimmt MJ aus Rohren/Motoren an und gibt sie an die Energy Bridge.',
        'BuildCraft-Energie in andere Systeme umwandeln.'),
    'craft:bc_producer': (
        'Gibt Energie der Energy Bridge als MJ ab.',
        'BuildCraft-Maschinen mit IC2-Strom betreiben.'),
    'craft:ic2_lv_consumer': ('Nimmt EU bis 32 EU/Paket an und gibt sie an die Energy Bridge.', 'IC2-Strom (LV) umwandeln.'),
    'craft:ic2_mv_consumer': ('Nimmt EU bis 128 EU/Paket an und gibt sie an die Energy Bridge.', 'IC2-Strom (MV) umwandeln.'),
    'craft:ic2_hv_consumer': ('Nimmt EU bis 512 EU/Paket an und gibt sie an die Energy Bridge.', 'IC2-Strom (HV) umwandeln.'),
    'craft:ic2_ev_consumer': ('Nimmt EU bis 2048 EU/Paket an und gibt sie an die Energy Bridge.', 'IC2-Strom (EV) umwandeln.'),
    'craft:ic2_lv_producer': ('Gibt Energie der Energy Bridge als EU-Pakete bis 32 aus.', 'LV-Maschinen aus anderem Strom speisen.'),
    'craft:ic2_mv_producer': ('Gibt Energie der Energy Bridge als EU-Pakete bis 128 aus.', 'MV-Maschinen aus anderem Strom speisen.'),
    'craft:ic2_hv_producer': ('Gibt Energie der Energy Bridge als EU-Pakete bis 512 aus.', 'HV-Geräte aus anderem Strom speisen.'),
    'craft:ic2_ev_producer': ('Gibt Energie der Energy Bridge als EU-Pakete bis 2048 aus.', 'EV-Geräte aus anderem Strom speisen.'),
    'craft:fe_producer': (
        'Wandelt EU in FE um (1 EU = 4 FE) und gibt sie an angrenzende FE-Geräte ab.',
        'Andere Mods (AE2, Create-Addons & Co.) mit Craft-Strom betreiben.'),
    # ------------------------------------------------------------------ power plants (new)
    'craft:coke_oven': (
        'Steuerblock des Koksofens (3×3×3 aus Koksofenziegeln, innen hohl): macht ohne Strom aus Kohle Koks.',
        'Koks für Kessel und Hochdruckkessel – dort bringt er doppelt so viel wie Kohle.'),
    'craft:coke_oven_bricks': ('Wandblock des Koksofens.', 'Mit dem Koksofen zusammen einen 3×3×3-Ofen bauen.'),
    'craft:boiler_firebox': (
        'Steuerblock des Kessels (3×3×3 aus Kesselgehäuse, innen hohl): verbrennt Kohle/Koks, heizt bis 1000 °C auf '
        'und macht aus Wasser bis zu 20 mB/t Dampf. Wasser auf einen trockenen, heißen Kessel lässt ihn explodieren.',
        'Dampf für die Dampfturbine (Kohlekraftwerk).'),
    'craft:boiler_casing': ('Wandblock des Kessels; Wasser hinein und Dampf heraus geht an jedem Block.', 'Kessel bauen.'),
    'craft:steam_turbine': (
        'Steuerblock der Dampfturbine (3×3×5 mit 3 Rotoren innen): macht aus Dampf Strom, 1 mB = 1 EU, bis 64 EU/t.',
        'Strom aus bis zu 3 Kesseln; Kabel an jedem Gehäuseblock.'),
    'craft:turbine_casing': ('Gehäuse der Dampfturbine; gibt den Strom an angeschlossene Kabel ab.', 'Dampfturbine bauen.'),
    'craft:turbine_rotor': ('Innere Achse der Dampfturbine (3 Stück).', 'Dampfturbine bauen.'),
    'craft:water_turbine': (
        'Steuerblock der Wasserturbine (3×3×3 voll): zählt fließendes Wasser im großen Umkreis und darüber; '
        '0,25 EU/t je Block ab 16 Blöcken, höchstens 32 EU/t (Fluss-Biom 48).',
        'Dauerstrom an Wasserfällen und Flüssen.'),
    'craft:water_turbine_casing': ('Gehäuse der Wasserturbine; Kabel an jedem Block.', 'Wasserturbine bauen.'),
    'craft:high_pressure_boiler_firebox': (
        'Steuerblock des Hochdruckkessels (5×5×5 hohl): heizt bis 1500 °C und macht bis 160 mB/t Dampf. Explodiert '
        'stärker als der kleine Kessel.',
        'Dampf für die Große Dampfturbine.'),
    'craft:high_pressure_boiler_casing': ('Wandblock des Hochdruckkessels.', 'Hochdruckkessel bauen.'),
    'craft:large_steam_turbine': (
        'Steuerblock der Großen Dampfturbine (5×5×9 mit 7 Rotoren): bis 512 mB/t Dampf → 512 EU/t (HV).',
        'Großkraftwerk mit drei Hochdruckkesseln, Tag und Nacht regelbar.'),
    'craft:large_turbine_casing': ('Gehäuse der Großen Dampfturbine; gibt Strom ab.', 'Große Dampfturbine bauen.'),
    'craft:large_turbine_rotor': ('Innere Achse der Großen Dampfturbine (7 Stück).', 'Große Dampfturbine bauen.'),
    'craft:large_water_turbine': (
        'Steuerblock der Großen Wasserturbine (5×5×5 voll): 1 EU/t je fließendem Wasserblock, höchstens 256 EU/t '
        '(Fluss-Biom 384).',
        'Großkraftwerk an großen Wasserfällen.'),
    'craft:large_water_turbine_casing': ('Gehäuse der Großen Wasserturbine; Kabel an jedem Block.', 'Große Wasserturbine bauen.'),
    'craft:large_wind_turbine': (
        'Gondel der Großen Windturbine auf 24 Mastblöcken, mit Rotor-Item darin: Strom aus Wind, je höher und '
        'freier desto mehr, bis 192 EU/t (HV).',
        'Billigstes Großkraftwerk, am besten ganz oben in der Welt.'),
    'craft:wind_turbine_mast': ('Mast der Großen Windturbine (24 Blöcke senkrecht).', 'Große Windturbine bauen.'),
    'craft:lv_solar_array': (
        'Stillgelegt: erzeugt nichts mehr und hat kein Rezept. In der Werkbank zerlegbar in 8 Solarpanels.',
        'Nur alte Arrays zurückgewinnen.'),
    'craft:mv_solar_array': (
        'Stillgelegt: erzeugt nichts mehr. In der Werkbank zerlegbar in 8 LV Solar Arrays.',
        'Nur alte Arrays zurückgewinnen.'),
    'craft:hv_solar_array': (
        'Stillgelegt: erzeugt nichts mehr. In der Werkbank zerlegbar in 8 MV Solar Arrays.',
        'Nur alte Arrays zurückgewinnen.'),
    # ------------------------------------------------------------------ Thermal Expansion
    'craft:powered_furnace': (
        'Ofen mit MJ; macht aus Sand Glas und kocht Essen.',
        'Schmelzen mit BuildCraft-Energie.'),
    'craft:pulverizer': (
        'Zermahlt mit MJ Erze zu 2 Staub mit Chance auf ein Nebenprodukt (z. B. Nickel aus Eisen).',
        'Erzverdopplung mit MJ statt EU.'),
    'craft:sawmill': (
        'Sägt mit MJ Holz zu mehr Brettern als von Hand und zerlegt Holzgegenstände, Nebenprodukt Sägespäne.',
        'Mehr Bretter, Holzreste verwerten.'),
    'craft:induction_smelter': (
        'Schmilzt mit MJ zwei Zutaten zusammen: Legierungen (Bronze, Invar, Elektrum), Erz mit Sand für mehr '
        'Barren.',
        'Legierungen und bessere Erzausbeute.'),
    'craft:magma_crucible': (
        'Schmilzt mit MJ Stein zu Lava, Redstone zu flüssigem Redstone, Glowstone, Ender usw.',
        'Lava für Generatoren, flüssiger Redstone für Energiezellen und Leitungen.'),
    'craft:liquid_transposer': (
        'Füllt Flüssigkeit mit MJ in Items (Zellen, Eimer, Rohlinge) oder holt sie heraus.',
        'Bauteile mit Flüssigkeit herstellen (z. B. Redstone-Energiezellen).'),
    'craft:glacial_precipitator': (
        'Macht mit MJ aus Wasser Schnee, Schneebälle und Eis.',
        'Eis und Schnee ohne Biom.'),
    'craft:igneous_extruder': (
        'Erzeugt aus angrenzender Lava und Wasser Bruchstein, Stein oder Obsidian, ohne sie zu verbrauchen.',
        'Unbegrenzt Bruchstein/Obsidian.'),
    'craft:aqueous_accumulator': (
        'Sammelt Wasser aus zwei oder mehr angrenzenden Wasserquellen (ohne sie zu verbrauchen).',
        'Wasser für Kessel, Extruder und Maschinen.'),
    'craft:portable_tank': (
        'Kleiner Tank, der seine Flüssigkeit beim Abbauen behält.',
        'Flüssigkeit mitnehmen oder zwischenlagern.'),
    'craft:steam_engine': (
        'TE-Motor: verbrennt Brennstoff und Wasser zu MJ.',
        'MJ-Quelle für TE- und BC-Maschinen.'),
    'craft:magmatic_engine': (
        'TE-Motor: liefert MJ aus Lava und anderen heißen Flüssigkeiten.',
        'MJ aus Lava, z. B. mit Magmatiegel oder Pumpe.'),
    'craft:redstone_energy_cell': (
        'Speichert MJ; Ein- und Ausgabe je Seite einstellbar.',
        'MJ-Puffer zwischen Motoren und Maschinen.'),
    'craft:redstone_energy_conduit': (
        'Leitet MJ ohne Verlust.',
        'MJ-Leitung für TE-Maschinen und Energiezellen.'),
    'craft:liquiduct': (
        'Leitet Flüssigkeiten zwischen Tanks und Maschinen.',
        'Flüssigkeitsleitung ohne Holzrohr-Motor.'),
    'craft:energy_tesseract': (
        'Überträgt MJ drahtlos zu anderen Energie-Tesserakten mit derselben Frequenz.',
        'Energie über weite Strecken ohne Leitung.'),
    'craft:liquid_tesseract': (
        'Überträgt Flüssigkeit drahtlos zu Flüssigkeits-Tesserakten mit derselben Frequenz.',
        'Flüssigkeit über weite Strecken.'),
    'craft:item_tesseract': (
        'Überträgt Items drahtlos zu Item-Tesserakten mit derselben Frequenz.',
        'Items über weite Strecken, z. B. von der Quarry nach Hause.'),
    # ------------------------------------------------------------------ others
    'craft:ender_storage_chest': (
        'Kiste mit 3-Farben-Code: alle Ender-Truhen mit demselben Code teilen sich ihren Inhalt, überall; mit '
        'Diamant persönlich.',
        'Items zwischen Orten austauschen, z. B. Mining-Dimension ↔ Basis.'),
    'craft:ender_storage_tank': (
        'Tank mit 3-Farben-Code: alle Ender-Tanks mit demselben Code teilen sich ihre Flüssigkeit.',
        'Flüssigkeit zwischen Orten austauschen.'),
    'craft:project_table': (
        'Werkbank mit 18 Vorratsplätzen, die das Raster aus dem Vorrat nachfüllt; legt Baupläne an.',
        'Oft gebrauchte Rezepte bequem herstellen.'),
    'craft:large_project_table': (
        'Wie der Projekttisch, aber mit 27 Vorratsplätzen.',
        'Mehr Vorrat für große Rezepte.'),
    'craft:tinker_table': (
        'Werkbank der Modular Powersuits: Module in Power-Rüstung und Power Tool einbauen und einstellen.',
        'Power-Rüstung zusammenstellen.'),
    'craft:mining_portal': (
        'Portal zur Mining-Dimension (Rahmen aus Glattem Stein, mit Feuerzeug zünden).',
        'Erze abbauen, ohne die Oberwelt zu durchlöchern.'),
}
