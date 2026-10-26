"""Overview pages for Create and the other mods bundled with the CurseForge pack (user, 2026-10-23).

Only installed, not integrated (docs/STATUS.md 'Create & Co. im CurseForge-Paket'): these pages explain what each mod is
for, how to get started and how it relates to Craft. Details live in the mods themselves (Ponder, EMI).
"""

G = 'Create & Paket-Mods'


def steps(items):
    return '<ol class="steps">' + ''.join(f'<li>{s}</li>' for s in items) + '</ol>'


def faq(items):
    return ''.join(f'<details class="faq"><summary>{q}</summary><div>{a}</div></details>' for q, a in items)


def pages(h, stats):
    P = []

    def page(pid, title, body):
        P.append({'id': pid, 'title': title, 'group': G, 'html': body})

    # ================================================================== overview
    overview = f'''<h1>Create im Paket – Überblick</h1>
<p class="lead">Seit Paket 1.0.2 sind <b>Create 6</b> und eine Reihe von Addons mitinstalliert. Sie laufen <b>neben</b> Craft:
eigene Energie (Drehkraft statt EU/MJ), eigene Maschinen, eigene Rezepte. Diese Seiten erklären, wofür die Mods da sind, wie
man anfängt und wo sie mit Craft zusammenspielen.</p>
<div class="tocpos"></div>
<h2 id="cr--ingame">Die beste Anleitung steckt im Spiel: Ponder</h2>
<p>Create bringt zu fast jedem Block eine animierte 3D-Anleitung mit. <b>Maus über ein Create-Item im Inventar oder in EMI halten
und <kbd>W</kbd> gedrückt halten</b> – es öffnet sich eine Szene, die Aufbau und Funktion zeigt. Viele Addons haben ebenfalls Ponder-Szenen.
Rezepte zeigt wie gewohnt EMI (<kbd>R</kbd> Rezept, <kbd>U</kbd> Verwendung) – auch die der Create-Maschinen (Pressen, Mixen,
Mahlen, Waschen, Fertigungsketten …).</p>
{h.note('info', 'Create und mehrere Addons bringen ihre Rezepte nur für <b>JEI</b> mit. Ab Paket <b>1.0.3</b> ist JEI deshalb mitinstalliert: EMI liest die JEI-Rezepte mit und blendet JEI selbst aus. Fehlen bei Create-Items die Rezepte, ist noch Paket 1.0.2 installiert.')}
{h.note('tip', 'Die <b>Ingenieursbrille</b> (Engineer\'s Goggles) zeigt beim Anschauen von Maschinen Drehzahl, Belastung und Füllstände an. Eines der ersten Dinge, die man bauen sollte.')}
<h2 id="cr--mods">Was ist dabei?</h2>
{h.table(['Mod', 'Wofür'], [
    ['<b>Create</b> 6.0.10', 'Mechanik mit Drehkraft: Wellen, Zahnräder, Förderbänder, Pressen, Mixer, Mahlwerke, Züge, Logistik mit Paketen'],
    ['<b>Create Aeronautics</b> 1.3.2 (mit Simulated, Offroad) + <b>Sable</b>', 'Physik-Fahrzeuge: Luftschiffe, Boote und Fahrzeuge aus beliebigen Blöcken; Sable ist die Physik-Engine dahinter'],
    ['Aeronautics: Automated Logistics, Propulsion: Simulated, Aeroworks', 'Zusatzteile für Luftfahrzeuge: Antriebe, Steuerung, Logistik an Bord'],
    ['<b>Steam \'n\' Rails</b> (NeoForge-Port 0.2.1)', 'Mehr für Create-Züge: Gleisarten, Spurweiten, Drehgestelle, Signale, Kupplungen'],
    ['<b>Create Big Cannons</b>', 'Kanonen aus Blöcken bauen, Munition herstellen und mit Drehkraft ausrichten'],
    ['<b>Create Crafts & Additions</b>', 'Strom (FE) aus Drehkraft (Lichtmaschine) und Drehkraft aus Strom (Elektromotor), Kabel, Akkus, Walzwerk'],
    ['<b>Create Diesel Generators</b>', 'Motoren, die mit Treibstoff (Pflanzenöl, Ethanol, Biodiesel, Diesel) Drehkraft erzeugen; Pumpjack und Destillation'],
    ['Create: Connected, Create Encased', 'Zusätzliche kinetische Bauteile und verkleidete Varianten von Wellen/Zahnrädern'],
    ['Copycats+', 'Kopierblöcke in vielen Formen (Stufen, Zäune, Platten …), die das Aussehen eines anderen Blocks übernehmen'],
    ['Mechanical Extruder', 'Erzeugt Blöcke (z. B. Bruchstein, Basalt) automatisch aus Flüssigkeiten – wie ein Bruchsteingenerator als Maschine'],
    ['Create Ore Excavation', 'Bohrt unerschöpfliche Erzvorkommen mit Drehkraft ab'],
    ['Applied Kinetics (+ AE2)', 'Applied Energistics 2: digitales Lager; Applied Kinetics betreibt AE2-Teile mit Drehkraft'],
    ['New Furnitures', 'Möbel im Create-Stil'],
    ['Create Better FPS (+ Sodium)', 'Leistung: schnellere Darstellung von Create-Anlagen'],
    ['<b>Ad Astra</b>', 'Raumfahrt: Raketen, Mond, Mars und weitere Planeten, Sauerstoff, Raumanzüge (läuft mit FE-Strom)'],
    ['Industrial Foregoing (+ Titanium)', 'Automatisierung mit FE: Farmen, Mob-Verarbeitung, Laserbohrer'],
    ['<b>Create: Integrated Farming</b> (+ Create: Dragons Plus)', 'Farm-Automatisierung mit Create: Pflanzen, Tiere und Fischerei in Maschinen'],
    ['<b>Create: Protection Pixel</b>', 'Rüstungen im Steampunk-/Create-Stil aus Messing und Legierungen, einige mit Sonderfunktionen'],
    ['GraveStone Mod', 'Unabhängig von Create: beim Tod landet das Inventar in einem Grabstein statt am Boden'],
    ['LambDynamicLights', 'Unabhängig von Create: dynamisches Licht (Fackel in der Hand, brennende Mobs …) und das Licht des Elektrischen Leuchtschilds'],
])}
<h2 id="cr--craft">Zusammenspiel mit Craft</h2>
<ul>
<li><b>Energie ist getrennt.</b> Craft arbeitet mit <b>EU</b> (IC2) und <b>MJ</b> (BuildCraft), Create mit <b>Drehkraft</b> (Stress-Einheiten SU),
Crafts & Additions, Ad Astra, Industrial Foregoing und AE2 mit <b>FE</b>. <b>Craft-Strom → FE</b> geht über den <a href="#/umwandlung">FE Producer</a> (ab Craft 1.1.1): IC2 Consumer → Energy Bridge → FE Producer, <b>1 EU = 4 FE</b>. Nur diese Richtung – FE und Drehkraft kommen nicht ins EU-/MJ-Netz, und zwischen EU/MJ und Drehkraft gibt es keinen Wandler.</li>
<li><b>Items fließen frei.</b> Create-Trichter (Funnels), Rutschen (Chutes), Förderbänder und Trichter arbeiten mit allen Craft-Maschinen
und -Kisten zusammen. Es gelten die Seiten der Maschine (siehe unten), wie bei Rohren und Trichtern.</li>
<li><b>Craft-Maschinen nehmen Items seitenrichtig an:</b> Ein Create-Trichter an der Seite eines Macerators füllt den Eingabe- bzw.
Batterie-Slot, nie die Ausgabe; von der Seite entnommen wird nur die Ausgabe. Oben = Eingabe, unten = Batterie wie in IC2.</li>
<li><b>Erzverarbeitung:</b> Create (Mahlwerk, Brechräder, Waschen) und IC2/GregTech/TE bieten verschiedene Wege. Welche Craft-Erze
Create verarbeiten kann, zeigt EMI: Erz anwählen, <kbd>U</kbd> drücken.</li>
<li><b>Mining-Dimension:</b> Create-Bohrer und Züge funktionieren auch dort.</li>
</ul>
<h2 id="cr--leistung">Leistung und Grafik</h2>
<ul>
<li>Empfohlen: <b>8 GB RAM</b> für das Paket (CurseForge-Profil → Einstellungen → Java-Speicher).</li>
<li>Create zeichnet drehende Teile mit <b>Flywheel</b>. <b>Mit aktivem Shader (Iris)</b> schaltet Flywheel auf seinen langsamen
Ersatzmodus („backend fell back … to off“ im Log). Das funktioniert, kostet bei großen Anlagen aber viele FPS –
für große Fabriken oder Luftschiffe die Shader ausschalten (<kbd>K</kbd> bzw. Videoeinstellungen → Shaderpakete → Aus).</li>
<li>Create Better FPS braucht Sodium; beides ist im Paket.</li>
</ul>
{faq([
    ('Create-Maschine dreht sich nicht / „überlastet“', 'Die Quelle liefert zu wenig Belastbarkeit (SU). Mit der Ingenieursbrille nachsehen; mehr Wasserräder/Windmühlenflügel oder eine stärkere Quelle anschließen, oder die Drehzahl senken.'),
    ('Kann ich Craft-Maschinen mit Create betreiben?', 'Nicht direkt – EU/MJ und Drehkraft sind getrennt. Create kann aber die Items liefern und abholen.'),
    ('Create-Item hat in EMI kein Rezept', 'Paket 1.0.3 oder neuer installieren (bringt JEI mit, dessen Create-Rezepte EMI anzeigt). Manche Items haben wirklich kein Rezept, z. B. Kreativ-Teile.'),
    ('Fehlermeldungen im Log zu Copycats/EMI', 'Harmlos: Copycats+ meldet beim Umschalten seiner Blöcke einen Fehler in seiner EMI-Anbindung. Das Spiel läuft normal weiter.'),
])}
'''
    page('create-ueberblick', 'Überblick', overview)

    # ================================================================== first steps
    first = f'''<h1>Create – erste Schritte</h1>
<p class="lead">Ein Weg vom ersten Wasserrad bis zur automatischen Erzverdopplung. Für jeden Block zeigt Ponder (<kbd>W</kbd> halten) den Aufbau.</p>
<div class="tocpos"></div>
<h2 id="cs--grundlagen">Grundbegriffe</h2>
{h.table(['Begriff', 'Bedeutung'], [
    ['Drehzahl (RPM)', 'Wie schnell sich eine Welle dreht. Höher = Maschinen arbeiten schneller, brauchen aber mehr Belastbarkeit.'],
    ['Belastung (SU, Stress Units)', 'Jede Maschine verbraucht SU × Drehzahl-Faktor. Quellen liefern eine feste Belastbarkeit. Ist das Netz überlastet, bleibt alles stehen.'],
    ['Welle / Zahnrad', 'Leiten die Drehung weiter. Kleines ↔ großes Zahnrad verdoppelt bzw. halbiert die Drehzahl.'],
    ['Andesitlegierung', 'Grundmaterial fast aller frühen Create-Teile (Andesit + Eisen- oder Zinknugget).'],
    ['Messing', 'Material für die mittlere Stufe: Kupfer + Zink im beheizten Mixer.'],
])}
<h2 id="cs--start">Schritt für Schritt</h2>
{steps([
    '<b>Andesitlegierung</b> herstellen (Werkbank), daraus Wellen, Zahnräder und ein <b>Wasserrad</b>.',
    'Wasserrad an fließendes Wasser setzen – oder eine <b>Windmühle</b> (Lager + Segel) bauen. Ponder zeigt, wie viel Wasser bzw. wie viele Segel nötig sind.',
    'Eine <b>Ingenieursbrille</b> bauen, um Drehzahl und Belastung zu sehen.',
    '<b>Mechanische Presse</b> über ein <b>Depot</b> oder Förderband: Barren zu Platten.',
    '<b>Mahlwerk</b> (Millstone) oder später <b>Brechräder</b> (Crushing Wheels): Erze zu zerkleinertem Erz – mit Chance auf Extra-Ausbeute.',
    '<b>Verkleideter Lüfter</b> (Encased Fan) durch Wasser: zerkleinertes Erz <b>waschen</b> → Nuggets. Durch Lava/Feuer: Items im Vorbeiflug schmelzen.',
    '<b>Mixer</b> über einem <b>Becken</b> (Basin), darunter ein <b>Lohenbrenner</b> (Blaze Burner) für Hitze: Messing.',
    'Mit Messing: <b>Dampfmaschine</b> (Kessel aus Flüssigkeitstanks + Lohenbrenner) für viel Belastbarkeit, mechanische Werkbänke, Züge.',
])}
<h2 id="cs--auto">Automatisieren</h2>
<ul>
<li><b>Förderbänder</b> verbinden zwei Wellen und transportieren Items; <b>Trichter</b> (Funnels) holen Items aus Kisten/Maschinen und legen sie ab.</li>
<li><b>Greifarm</b> (Mechanical Arm): verteilt Items zwischen Ablagen, Maschinen und Bändern.</li>
<li><b>Einsetzer</b> (Deployer): benutzt Items wie ein Spieler – Blöcke setzen, Werkzeuge anwenden.</li>
<li><b>Create 6 Logistik:</b> Packer verpacken Items in Pakete, Frogports und Kettenförderer verschicken sie, Lagerverwalter (Stock Keeper) bestellen aus einem Netz.</li>
</ul>
{h.note('tip', 'Craft-Maschinen lassen sich direkt einbinden: Ein Create-Trichter an der Seite einer Craft-Maschine füllt die Eingabe (Batterien in den Batterie-Slot); die fertigen Items holt ein zweiter Trichter ebenfalls von einer Seite ab.')}
'''
    page('create-einstieg', 'Erste Schritte', first)

    # ================================================================== addons
    addons = f'''<h1>Addons im Paket</h1>
<p class="lead">Kurz zu jedem größeren Addon: wofür es da ist und womit man anfängt. Rezepte in EMI, Aufbau in Ponder.</p>
<div class="tocpos"></div>
<h2 id="ca--aero">Create Aeronautics & Sable – Luftschiffe und Fahrzeuge</h2>
<p>Mit Aeronautics werden gebaute Strukturen zu <b>echten Physik-Objekten</b>: Luftschiffe mit Ballons oder Propellern, Boote, Fahrzeuge
(Offroad). Sable berechnet Auftrieb, Masse und Zusammenstöße. Propulsion: Simulated und Aeroworks liefern Antriebe und Steuerteile,
Automated Logistics den Warentransport mit Luftschiffen.</p>
{h.note('warn', 'Physik-Fahrzeuge sind rechenintensiv. Große Schiffe mit Shadern können ruckeln – Shader aus, Sichtweite runter.')}
<h2 id="ca--rails">Steam 'n' Rails – mehr für Züge</h2>
<p>Erweitert die Züge aus Create um weitere Gleisarten (auch andere Spurweiten), Drehgestelle, Kupplungen, Signale und Bahnhofsteile.
Im Paket ist die inoffizielle NeoForge-Portierung (offiziell gibt es Steam 'n' Rails nur für 1.20.1).</p>
<h2 id="ca--cannons">Create Big Cannons</h2>
<p>Kanonen werden aus Rohr- und Verschlussblöcken zusammengesetzt, auf einer drehbaren Lafette montiert und mit Drehkraft gerichtet.
Munition (Kugeln, Granaten, Pulverladungen) wird hergestellt und geladen. Achtung: Explosionen beschädigen auch eigene Bauten.</p>
<h2 id="ca--strom">Crafts & Additions und Diesel Generators – Strom und Treibstoff</h2>
<ul>
<li><b>Lichtmaschine</b> (Alternator): Drehkraft → FE. <b>Elektromotor</b>: FE → Drehkraft. Dazu Kabel/Verbinder, Akkus und ein Walzwerk für Drähte.</li>
<li>FE ist <b>nicht</b> EU – Craft-Maschinen lassen sich damit nicht betreiben. FE nutzen Ad Astra, Industrial Foregoing und AE2-Teile. Umgekehrt liefert der <a href="#/umwandlung">FE Producer</a> Craft-Strom als FE (1 EU = 4 FE).</li>
<li><b>Diesel Generators:</b> Motoren verbrennen Treibstoff zu Drehkraft. Pflanzenöl aus Samen pressen, weiter zu Biodiesel verarbeiten; Erdöl über Pumpjack und Destillation.</li>
</ul>
<h2 id="ca--bau">Bauteile und Deko</h2>
<ul>
<li><b>Create: Connected</b> – zusätzliche Getriebe-, Kupplungs- und Steuerblöcke. <b>Create Encased</b> – Wellen und Zahnräder in vielen Verkleidungen.</li>
<li><b>Holz-Wellen und Holz-Zahnräder von Create Encased</b> (ab Paket 1.0.4): <b>8 Wellen bzw. 8 Zahnräder um 1 Brett</b> der Holzart →
8 Stück. Das Original-Rezept (Welle + Brett) überschnitt sich mit Create (Welle + Brett = Zahnrad, Zahnrad + Brett = großes Zahnrad);
Craft ersetzt es deshalb. Mit dem Einsetzer (Brett auf die Welle) geht es weiterhin einzeln.</li>
<li><b>Trichter (Vanilla)</b> (ab Paket 1.0.14): lassen sich <b>nicht mehr per Redstone abschalten</b> – sie arbeiten immer, auch neben Redstone-Blöcken, Fackeln oder Create-Signalen. Früher abgeschaltete Trichter laufen sofort wieder. Trichterloren (Aktivierungsschiene) sind nicht betroffen.</li>
<li><b>Copycats+</b> – Kopierblöcke: Block in der Hand auf den Kopierblock → er sieht aus wie dieser Block.</li>
<li><b>New Furnitures</b> – Möbel im Create-Stil.</li>
</ul>
<h2 id="ca--rohstoffe">Rohstoffe: Mechanical Extruder und Ore Excavation</h2>
<ul>
<li><b>Mechanical Extruder</b> erzeugt Blöcke aus zwei angrenzenden Flüssigkeiten (z. B. Wasser + Lava → Bruchstein) und schiebt sie aus – ohne Spielerhand.</li>
<li><b>Andesit, Granit und Tuff</b> (ab Paket 1.0.13): Wasser + Lava an den Seiten und <b>derselbe Block als Katalysator unter dem Extruder</b> – in jeder Höhe (die Mod-Vorgabe Y 0–60 entfällt für Andesit und Granit; Diorit bleibt so). Der Katalysator wird nicht verbraucht. Am besten das gewünschte Ergebnis im Filter des Extruders einstellen, sonst kann er auch Bruchstein liefern. Ochrum: Goldblock + Lava (unverändert).</li>
<li><b>Ore Excavation</b>: In der Welt gibt es unterirdische Erzvorkommen, die ein Bohrgerät mit Drehkraft dauerhaft abbaut. Vorkommen findet man mit einem Suchgerät aus dem Mod.</li>
</ul>
<h2 id="ca--ae2">Applied Energistics 2 und Applied Kinetics</h2>
<p>AE2 speichert Items digital in Laufwerken und kann automatisch herstellen. Es braucht normalerweise FE; <b>Applied Kinetics</b> erlaubt,
AE2-Teile mit Create-Drehkraft zu versorgen. Das Ingame-Handbuch von AE2 (GuideME) erklärt den Einstieg.</p>
<p><b>Mit Craft-Strom:</b> EU-Kabel → IC2 Consumer → Energy Bridge → <a href="#/umwandlung">FE Producer</a> → AE2 <b>Energy Acceptor</b> (oder Controller) direkt daneben. 128 EU/t ergeben 512 FE/t ≈ 256 AE/t (AE2: 1 AE = 2 FE).</p>
<h2 id="ca--astra">Ad Astra – Raumfahrt</h2>
<p>Raketen bauen, betanken und zu Mond, Mars, Venus, Merkur und Glacio fliegen. Auf den Planeten braucht man <b>Raumanzug und Sauerstoff</b>.
Maschinen laufen mit FE. Neue Erze und Metalle (z. B. Desh, Ostrum, Calorite) gibt es nur dort.</p>
<h2 id="ca--if">Industrial Foregoing</h2>
<p>Automatisierung mit FE: Pflanzen- und Baumfarmen, Mob-Verarbeitung, Laserbohrer für Erze, Flüssigkeitsverarbeitung. Unabhängig von Create.</p>
<h2 id="ca--farming">Create: Integrated Farming</h2>
<p>Farmen mit Create-Mitteln automatisieren: Pflanzen, Tierhaltung und Fischerei in Maschinen. Ab Paket <b>1.0.7</b>; braucht die
Bibliothek Create: Dragons Plus (mitinstalliert). Aufbau zeigen Ponder und EMI.</p>
<h2 id="ca--pixel">Create: Protection Pixel</h2>
<p>Rüstungsteile im Steampunk-Stil (Helme, Brustplatten mit Sonderfunktionen wie Jetpack oder Schild), hergestellt aus Messing und
Legierungen. Ab Paket <b>1.0.7</b>. Rezepte in EMI.</p>
<h2 id="ca--grave">GraveStone Mod</h2>
<p>Stirbt man, erscheint an der Stelle ein Grabstein mit dem gesamten Inventar; Rechtsklick holt die Sachen zurück. Ab Paket <b>1.0.7</b>.</p>
<h2 id="ca--ldl">LambDynamicLights</h2>
<p>Dynamisches Licht: Fackeln und andere leuchtende Items in der Hand, brennende Mobs und Ähnliches erhellen die Umgebung, ohne
das Licht der Welt zu ändern. Craft nutzt es für das Licht des <a href="#/leuchtschild">Elektrischen Leuchtschilds</a>.
Ab Paket <b>1.0.12</b>; Einstellungen unter Optionen → Grafik (mit Sodium in dessen Menü).</p>
'''
    page('create-addons', 'Addons', addons)
    return P
