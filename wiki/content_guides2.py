"""Player guides for the big original systems: quarry/BuildCraft energy, reactor, GregTech machines, EU<->MJ, IC2 machines.

Values from docs/spec 04, 06, 09, 10, 13. Reactor example designs are computed with the formulas of spec 06 §3:
- dual cell + 4 heat vents: 2 pulses x 2 = 4 output (20 EU/t), heat 12 per repetition -> 3 per vent, 6 per cycle = selfVent 6
- two dual cells stacked + 6 overclocked vents: each cell 3 pulses per repetition, output 12 (60 EU/t),
  heat 24 per repetition over 3 vents -> 16 per vent and cycle < selfVent 20
"""


def faq(items):
    return ''.join(f'<details class="faq"><summary>{q}</summary><div>{a}</div></details>' for q, a in items)


def steps(items):
    return '<ol class="steps">' + ''.join(f'<li>{s}</li>' for s in items) + '</ol>'


def pages(h, stats):
    c = h.chip
    v = h.vchip
    P = []
    G = 'Anleitungen'

    def page(pid, title, body):
        P.append({'id': pid, 'title': title, 'group': G, 'html': body})

    # ================================================================== Quarry
    qk = {'L': ('craft:landmark', 'Landmarke', 'ctrl'), 'Q': ('craft:quarry', 'Quarry', 'ctrl'), 'E': ('craft:stirling_engine', 'Stirlingmotor', ''),
          'X': ('minecraft:chest', 'Truhe', ''), 'R': ('minecraft:redstone_torch', 'Redstone-Fackel', ''), '#': (None, 'Abbaufeld', 'flow'),
          '_': (None, 'frei', 'ground')}
    qplan = ['L#########_', '##########_', '##########_', '##########_', '##########_', '##########_', '##########_', '##########_',
             '##########_', 'LQ#########', 'XE_________', '_R_________']
    quarry = f'''<h1>Quarry & BuildCraft-Energie</h1>
<p class="lead">Die Quarry baut ein ganzes Gebiet bis zum Grundgestein ab und liefert alles in eine Kiste. Sie läuft mit <b>MJ</b>
(BuildCraft-Energie), nicht mit EU. Diese Seite erklärt Motoren, Landmarken, Aufbau, Strom und typische Fehler – alles 1:1 wie BuildCraft 3.4.3.</p>
<div class="tocpos"></div>
<h2 id="qu--mj">MJ und Motoren</h2>
<p>Motoren erzeugen MJ und schieben sie mit jedem Kolbenhub in den Block, auf den sie zeigen. <b>Alle Motoren laufen nur mit Redstone-Signal</b>
(Hebel, Redstone-Fackel). Der Motor dreht sich beim Setzen zum nächsten MJ-Empfänger; mit dem {c('bc_wrench')} kann man ihn weiterdrehen.</p>
{h.table(['Motor', 'Leistung', 'Brennstoff', 'Gefahr', 'für die Quarry?'], [
    [c('redstone_engine'), '0,05 MJ/t', 'nur Redstone-Signal', 'keine', '<b>nein</b> – gibt nur 1 MJ pro Hub, die Quarry braucht mind. 25–30'],
    [c('stirling_engine'), '1 MJ/t', 'feste Brennstoffe (Kohle = 1 600 MJ)', 'explodiert, wenn voll (10 000 MJ) und weiter befeuert', 'ja'],
    [c('combustion_engine'), 'Lava 1 · Öl 3 · Treibstoff 6 MJ/t', 'Flüssigkeit (10 000-mB-Tank)', 'braucht Kühlwasser, explodiert bei Überhitzung', 'ja, am stärksten'],
    [c('electrical_engine'), '2 MJ/t aus 6 EU (bis 10 MJ/t)', 'EU aus dem IC2-Netz', 'keine Explosion, kühlt zwangsweise ab', 'ja'],
])}
<p>Farbe des Motors = Zustand: Blau → Grün → Gelb → Rot. Rot heißt kurz vor der Explosion (Stirling/Combustion). Mehrere Motoren können
gleichzeitig an einer Maschine hängen; Motoren können auch Motoren dahinter versorgen (Kette, 5 % Verlust je Glied).</p>
{h.note('warn', '<b>Stirlingmotor:</b> Ist die Quarry fertig oder blockiert, nimmt sie nichts mehr ab. Der Motor läuft voll und <b>explodiert</b>, wenn er dann weiter brennt. Redstone abschalten, sobald die Quarry fertig ist, oder nicht zu viel Brennstoff einlegen.')}
<h2 id="qu--bereich">Schritt 1: Gebiet abstecken</h2>
<div class="layers">{h.layer(qplan, qk, 'Draufsicht: drei Landmarken (zwei unten links/rechts außerhalb des Bildes reichen), Quarry neben einer Landmarke, Motor mit Redstone-Fackel, Kiste an der Quarry', 22)}<div>{h.legend(qk)}</div></div>
{steps([
    f"Eine {c('landmark')} auf die Ecke des gewünschten Feldes setzen.",
    'Eine zweite Landmarke in gerader Linie entlang der X-Achse, eine dritte entlang der Z-Achse – jeweils höchstens 63 Blöcke entfernt. (Mit Redstone-Signal an einer Landmarke zeigen blaue Laser die Achsen.)',
    'Optional eine vierte über der Ecke für eine größere Höhe.',
    'Rechtsklick auf die Eck-Landmarke: Ein <b>roter Laserrahmen</b> zeigt das Feld.',
    f"Die {c('quarry')} direkt <b>neben eine Landmarke</b> setzen. Sie übernimmt den Rahmen, die Landmarken droppen.",
])}
<ul>
<li>Größe: mindestens 3×3; die Fläche muss kleiner als 6 400 Blöcke sein (z. B. 64×64 geht, 79×79 gerade noch), sonst nimmt die Quarry ihren Standardbereich.</li>
<li><b>Ohne Landmarken</b> gräbt die Quarry ein Feld von 11×11 (Innenraum 9×9) <b>hinter</b> sich – die Vorderseite zeigt beim Setzen zu dir.</li>
<li>Die Quarry hält ihre Chunks selbst geladen (max. 25 Chunks). Im Chat erscheint eine Meldung mit der Anzahl.</li>
</ul>
<h2 id="qu--strom">Schritt 2: Strom und Ausgabe</h2>
{steps([
    'Einen Stirling-, Combustion- oder Electrical-Motor an die Quarry setzen und mit Redstone aktivieren.',
    'Eine <b>Kiste direkt an die Quarry</b>: Die Quarry legt alles in ein zufälliges angrenzendes Inventar, sonst in ein angrenzendes Item-Rohr, sonst wirft sie es oben aus.',
    'Die Quarry baut zuerst den <b>Rahmen</b> (Roboter mit Laser, 25 MJ pro Rahmenblock), dann fährt der <b>Bohrarm</b> aus und gräbt Ebene für Ebene von oben nach unten.',
])}
<ul>
<li>Pro Block: 60 MJ zum Ansetzen plus Energie für die Bewegung (mehr gespeicherte Energie → schnellerer, aber teurerer Kopf). Speicher 15 000 MJ.</li>
<li>Mit einem Stirlingmotor (1 MJ/t) schafft sie grob einen Block alle 3–4 Sekunden; mehrere Motoren oder Combustion-Motoren mit Treibstoff sind deutlich schneller.</li>
<li>Übersprungen werden Luft, Flüssigkeiten, Pflanzen, Schnee, Ranken. <b>Bedrock, Lava und Öl</b> sperren die Säule darunter.</li>
<li>Drops ohne Glück/Behutsamkeit. Herumliegende Items in der Nähe des Kopfes sammelt sie mit ein.</li>
<li>Mit dem BuildCraft-Schraubenschlüssel auf die Quarry: Rahmen neu prüfen und weitergraben.</li>
</ul>
{h.note('tip', 'Ideal für Quarrys: die <a href="#/mining-dimension">Mining-Dimension</a> – flach, keine Höhlen, kein Wasser, keine Lava, alle Oberwelt-Erze.')}
<h2 id="qu--oel">Öl, Pumpe, Raffinerie</h2>
<ul>
<li><b>Öl</b> findet man als Fontänen und Pfützen (Wüsten) über unterirdischen Ölkugeln (y 20–29).</li>
<li>{c('pump')} über das Öl setzen, mit Motor betreiben (10 MJ pro 1 000 mB). Sie senkt ein Rohr ab, pumpt Quellblock für Quellblock und gibt den Inhalt an angrenzende Behälter ab.</li>
<li>{c('tank')}: 16 000 mB je Block, übereinander gestapelt bilden Tanks einen großen Tank (gefüllt von unten).</li>
<li>{c('refinery')}: 1 mB Öl → 1 mB Treibstoff für 12 MJ, höchstens 1 mB pro Tick. Öl hinein per Rohr, Treibstoff mit einem Holz-Flüssigkeitsrohr (+ Motor) heraus.</li>
<li>Treibstoff im Combustion-Motor: 6 MJ/t, 600 000 MJ pro Eimer – über <a href="#/umwandlung">Power Converters</a> ≈ 1,46 Mio. EU.</li>
<li>Combustion-Motor braucht <b>Kühlmittel</b> (Wasser oder IC2-Kühlmittel) im unteren Tank, sonst überhitzt und explodiert er. Wird ihm das Redstone weggenommen, kühlt er 600 Ticks Strafe ab.</li>
</ul>
<h2 id="qu--rohre">Rohre in Kürze</h2>
<ul>
<li><b>Item-Rohre</b> transportieren Items; ein {c('wooden_transport_pipe')} zieht mit Motor Items aus Kisten heraus. Holz verbindet sich nicht mit Holz, Bruchstein nicht mit Stein.</li>
<li><b>Flüssigkeitsrohre</b> (wasserdicht, mit Rohrdichtmittel) für Wasser, Öl, Treibstoff, Dampf; Gold-Rohre transportieren 4× so viel.</li>
<li><b>Leistungsrohre</b> leiten MJ vom Motor (am leitenden Holzrohr) zu Maschinen.</li>
<li>Trichter können nicht in Rohre schieben (Rohre haben kein Inventar), Rohre liefern aber in Trichter und Kisten.</li>
</ul>
<h2 id="qu--fehler">Fehlersuche</h2>
{faq([
    ('Die Quarry macht nichts.', 'Kein Motor mit genug Leistung (Redstone-Motor reicht nicht), kein Redstone-Signal am Motor, oder der Motor zeigt nicht auf die Quarry (mit dem BC-Schraubenschlüssel drehen).'),
    ('„Quarry size is outside of chunkloading bounds or too small“', 'Das Feld ist kleiner als 3×3 oder größer als 6 400 Blöcke Fläche. Kleiner abstecken.'),
    ('„…will not work because there are no more chunkloaders available“', 'Es sind zu viele Quarrys gleichzeitig aktiv (Chunkloading-Grenze).'),
    ('Der Rahmen wird nie fertig.', 'Im Rahmenbereich liegt Lava, Öl oder Bedrock – der Roboter kann dort nichts setzen (Original-Eigenheit). Den Bereich verschieben oder die Flüssigkeit entfernen.'),
    ('Items fliegen durch die Gegend.', 'Keine Kiste und kein Rohr an der Quarry.'),
    ('Mein Stirlingmotor ist explodiert.', 'Er war voll (Quarry fertig/blockiert) und hat weiter gebrannt.'),
])}
<p>Referenz: <a href="#/spec-09">09 – BuildCraft</a>, <a href="#/spec-15">15 – Rohre</a>.</p>
'''
    page('quarry', 'Quarry & BuildCraft-Energie', quarry)

    # ================================================================== Reaktor
    rk = {'U': ('craft:dual_uranium_cell', 'Doppel-Uranzelle', ''), 'V': ('craft:heat_vent', 'Wärmeableiter', ''),
          'O': ('craft:overclocked_heat_vent', 'Vergoldeter Wärmeableiter', ''), '_': (None, 'leer', 'ground')}
    reaktor = f'''<h1>Kernreaktor</h1>
{h.infobox('Kernreaktor', 'craft:nuclear_reactor', [('Herkunft', 'IndustrialCraft 2 (1:1)'), ('Matrix', '3 bis 9 Spalten × 6 Zeilen'),
    ('Zyklus', 'alle 20 Ticks'), ('Energie', '5 EU/t je Puls'), ('Start', 'nur mit Redstone-Signal'), ('Hülle', '10 000 Hitze (+ Platten)'),
    ('Explosion', 'bis Stärke 45')])}
<p class="lead">Der stärkste Generator – und der gefährlichste. Uranzellen erzeugen Strom <b>und</b> Hitze; die Hitze muss von Kühlkomponenten
abgeführt werden. Ist die Hülle zu heiß, brennt die Umgebung, und bei 100 % explodiert der Reaktor. Die Logik ist exakt IC2.</p>
<div class="tocpos"></div>
<h2 id="re--aufbau">Aufbau</h2>
{steps([
    f"{c('nuclear_reactor')} setzen. Er hat zunächst <b>3 Spalten × 6 Zeilen</b> für Komponenten.",
    f"Jede angrenzende {c('reactor_chamber')} (max. 6, an jeder Seite eine) erweitert die Matrix um eine Spalte – bis 9 × 6. Kammern lassen sich nur setzen, wenn sie genau einen Reaktor berühren.",
    'Komponenten in die GUI legen (Rechtsklick auf Reaktor oder Kammer).',
    '<b>Redstone-Signal</b> an den Reaktor (Hebel). Ohne Signal tun Uranzellen nichts – kein Strom, keine Hitze, kein Verschleiß. So schaltest du ihn ab.',
    'Strom abnehmen: Reaktor und Kammern geben die Energie ab, Kabel an Reaktor oder Kammer.',
])}
{h.note('warn', 'Die Paketgröße ist die ganze Leistung des Reaktors: Ein Reaktor mit 60 EU/t schickt 60er Pakete (MV), mit 200 EU/t 200er Pakete (HV). Kabel und Speicher entsprechend wählen – siehe <a href="#/strom">Strom anschließen</a>. Was niemand abnimmt, geht verloren.')}
<h2 id="re--teile">Die Komponenten</h2>
{h.table(['Komponente', 'Was sie tut'], [
    [f"{c('uranium_cell')} / {c('dual_uranium_cell')} / {c('quad_uranium_cell')}", 'erzeugen Pulse (Strom) und Hitze; halten 10 000 Zyklen (≈ 2,8 h). Nebeneinanderliegende Zellen pulsen sich gegenseitig – mehr Strom, aber überproportional mehr Hitze.'],
    [f"{c('heat_vent')} (6) · {c('advanced_heat_vent')} (12) · {c('overclocked_heat_vent')} (20, zieht 36 aus der Hülle) · {c('reactor_heat_vent')} (5 + 5 aus der Hülle)", 'kühlen sich selbst pro Zyklus um den Wert; fassen 1 000 Hitze, darüber schmelzen sie'],
    [c('component_heat_vent'), 'kühlt die 4 Nachbarn um je 4 pro Zyklus'],
    [f"{c('heat_exchanger')} · {c('reactor_heat_exchanger')} · {c('component_heat_exchanger')} · {c('advanced_heat_exchanger')}", 'verteilen Hitze zwischen Nachbarn und/oder Hülle'],
    [f"{c('coolant_cell_10k')} · {c('coolant_cell_30k')} · {c('coolant_cell_60k')}", 'speichern Hitze, kühlen selbst nicht – müssen ersetzt oder im Vakuumgefrierer gekühlt werden'],
    [f"{c('reactor_plating')} · {c('heat_capacity_reactor_plating')} · {c('containment_reactor_plating')}", 'erhöhen die maximale Hüllenhitze und schwächen Explosion/Effekte ab'],
    [f"{c('neutron_reflector')} · {c('thick_neutron_reflector')}", 'werfen Pulse zurück: mehr Strom ohne zusätzliche Uranzelle'],
    [f"{c('rsh_condensator')} · {c('lzh_condensator')}", 'schlucken Hitze, bis sie voll sind; mit Redstone/Lapis in der Werkbank wieder auffüllen'],
])}
<h2 id="re--formel">So rechnet der Reaktor</h2>
<ul>
<li>Alle 20 Ticks verarbeitet er Zeile für Zeile jede Komponente.</li>
<li>Eine Uranzelle mit <i>n</i> Zellen (1/2/4) läuft <i>n</i>-mal. Jedes Mal pulst sie <code>1 + n/2</code>-mal sich selbst und einmal jeden Nachbarn, der Pulse annimmt (Uranzellen, Reflektoren).
Jeder Puls = <b>5 EU/t</b>.</li>
<li>Hitze pro Durchlauf: <code>4 · (1 + 2 + … + Pulse)</code>. Sie wird gleichmäßig auf die Nachbarn verteilt, die Hitze speichern können; der Rest geht in die <b>Hülle</b>.</li>
<li>Eine einzelne Uranzelle allein: 5 EU/t, 4 Hitze pro Zyklus. Zwei benachbarte Einzelzellen: je 10 EU/t, je 12 Hitze.</li>
</ul>
<h3 id="re--hitze">Was bei Hitze passiert (Anteil der Hüllenhitze am Maximum)</h3>
{h.table(['ab', 'Effekt (jeden Zyklus)'], [
    ['40 %', 'Holz, Blätter, Wolle in 2 Blöcken Umkreis fangen Feuer'],
    ['50 %', 'Wasser in der Nähe verdampft'],
    ['70 %', 'Lebewesen in der Nähe bekommen Strahlungsschaden (Hazmat-Anzug schützt)'],
    ['85 %', 'Feuer und Lava entstehen rundherum'],
    ['100 %', '<b>Explosion</b>: Stärke 10 + Beitrag der Zellen, gedämpft durch Platten, höchstens 45; Reaktor und Kammern sind weg'],
])}
<p>Effekte gibt es erst ab 4 000 Hüllenhitze.</p>
<h2 id="re--designs">Sichere Einsteiger-Designs</h2>
<p>Beide passen in den Reaktor ohne Kammern (3 Spalten). Durchgerechnet mit der Formel oben – die Hitze bleibt dauerhaft bei null bzw. die Vents kühlen mehr ab, als ankommt.</p>
<div class="layers">{h.layer(['_V_', 'VUV', '_V_', '___', '___', '___'], rk, '<b>Design A – 20 EU/t (LV):</b> 1 Doppel-Uranzelle, 4 Wärmeableiter. Jeder Ableiter bekommt 6 Hitze pro Zyklus und kühlt 6. Ertrag ≈ 4 Mio. EU pro Zelle.', 38)}
{h.layer(['_O_', 'OUO', 'OUO', '_O_', '___', '___'], rk, '<b>Design B – 60 EU/t (MV!):</b> 2 Doppel-Uranzellen übereinander, 6 vergoldete Wärmeableiter. Jeder bekommt 16 Hitze und kühlt 20. Ertrag ≈ 12 Mio. EU pro Füllung.', 38)}<div>{h.legend(rk)}</div></div>
{h.note('info', 'Für Design B brauchst du MV-feste Kabel und Speicher (z. B. Goldkabel zur MFE). Die Doppelzellen halten 10 000 Zyklen; danach bleiben fast erschöpfte Uranzellen zurück (je Zelle mit 1/3 Chance).')}
<h2 id="re--fehler">Sicherheit</h2>
{faq([
    ('Wie schalte ich den Reaktor sicher ab?', 'Redstone-Signal wegnehmen. Uranzellen stoppen sofort, Vents und Tauscher kühlen weiter.'),
    ('Kann ich Designs testen?', 'Im Kreativmodus in einer Testwelt. Beobachte die Hüllenhitze und die Balken der Komponenten über mehrere Minuten.'),
    ('Was passiert beim Abbauen?', 'Mit dem Schraubenschlüssel gelingt der Abbau zu 80 %. Ohne Schraubenschlüssel droppt der Reaktor nur einen Generator, eine Kammer nur einen Maschinenblock. Inhalt vorher herausnehmen.'),
    ('Automatisierung?', 'Trichter/Rohre können über Reaktor und Kammern Komponenten einlegen und entnehmen – nur Teile, die in den Reaktor gehören.'),
])}
<p>Referenz: <a href="#/spec-06">06 – Kernreaktor</a> (alle Komponenten mit exakten Werten, GregTech-Teile wie NaK-/Helium-Kühlzellen und Iridium-Reflektor).</p>
'''
    page('reaktor', 'Kernreaktor', reaktor)

    # ================================================================== GregTech
    gk = {'C': ('craft:standard_machine_casing', 'Standard Machine Casing (30 K)', ''), '.': (None, 'Luft oder Lava (Lava: +250 K)', 'air'),
          'F': ('craft:industrial_blast_furnace', 'Industrial Blast Furnace (Controller)', 'ctrl'), '_': (None, 'davor: Controller-Ebene', 'ground')}
    gt = f'''<h1>GregTech-Maschinen & Hochofen</h1>
<p class="lead">GregTech-Maschinen machen die Materialien, die die Pack-Rezepte verlangen: Platten, Stahl, Aluminium, Silizium, Titan, Wolfram …
Sie verhalten sich in wichtigen Punkten <b>anders</b> als IC2-Maschinen – wer das nicht weiß, verliert Maschinen. Welcher Rohstoff aus welcher Maschine kommt: <a href="#/rohstoffe">Rohstoffe & Erze</a>.</p>
<div class="tocpos"></div>
<h2 id="gt--regeln">Die wichtigsten Regeln</h2>
{h.note('warn', '<b>Immer mit dem Schraubenschlüssel abbauen!</b> Ohne Schraubenschlüssel droppt eine GT-Maschine nur <b>Maschinenteile</b> (Machine Parts) statt sich selbst – und kann dabei explodieren, wenn sie viel Energie gespeichert hat. Auch eingebaute Upgrades sind dann verloren.')}
<ul>
<li><b>Eingangsspannung 32 EU (LV)</b> – größere Pakete lassen die Maschine explodieren. Der Hochofen nimmt 128 (MV). Jedes {c('transformer_upgrade')} vervierfacht die Grenze.</li>
<li><b>Feuer</b> neben einer GT-Maschine kann sie zur Explosion bringen (seltener Zufall) – keine Fackeln … nein: kein offenes Feuer/Lava daneben.</li>
<li><b>Anlaufen:</b> Eine Maschine startet ein Rezept erst, wenn sie <b>mehr als 900 EU</b> gespeichert hat (Hochofen: 100). Kurz nach dem Anschließen dauert es also einen Moment.</li>
<li><b>Upgrades</b> werden per <b>Rechtsklick mit dem Upgrade</b> auf die Maschine eingebaut (keine Slots), max. 16: {c('overclocker_upgrade')} (max. 4, je ×2 Tempo, ×4 Energie), Transformator, Energiespeicher (+10 000 EU), {c('lithium_battery_upgrade')} (+100 000 EU), {c('pneumatic_generator_upgrade')} (nimmt dann auch MJ).</li>
<li><b>Seiten:</b> Die Vorderseite hat keinen Zugriff, die <b>Rückseite</b> ist Ausgabe, alle anderen Seiten Eingabe. Steht an der Rückseite eine Kiste, schiebt die Maschine fertige Produkte <b>selbst</b> hinein.</li>
<li>Mit dem Schraubenschlüssel lässt sich die Vorderseite auf jede Seite drehen.</li>
</ul>
<h2 id="gt--maschinen">Die Maschinen</h2>
{h.table(['Maschine', 'wofür'], [
    [c('plate_bending_machine'), 'Barren → Platten (Eisen, Kupfer, Zinn, Bronze, Gold, Stahl, Aluminium …)'],
    [c('industrial_electrolyzer'), 'zerlegt Stäube und Zellen in ihre Elemente (z. B. Wasser → Wasserstoff + Sauerstoff)'],
    [c('industrial_blast_furnace'), 'Multiblock: Stahl, Aluminium, Siliziumplatte, Titan, Chrom, Wolfram …'],
    [c('assembling_machine'), 'baut Schaltkreise und Bauteile zusammen'],
    [c('automatic_canning_machine'), 'füllt Zellen und Dosen'],
    [c('implosion_compressor'), 'Multiblock: mit Industrie-TNT zu Edelsteinen und Iridiumplatten pressen'],
    [c('industrial_centrifuge'), 'trennt Stoffe nach Masse (z. B. Lava, Ruß, Erzstäube)'],
    [c('industrial_grinder'), 'Multiblock: Erze mit Wasser zu mehr Staub und Nebenprodukten mahlen'],
    [c('vacuum_freezer'), 'Multiblock: kühlt heiße Barren und Kühlzellen'],
])}
<p>Alle Rezepte jeder Maschine stehen auf ihrer Seite unter „In dieser Maschine“ (Maschine anklicken).</p>
<h2 id="gt--hochofen">Der Industrielle Hochofen</h2>
<p>Ein Turm aus 3×3×4 GT-Maschinengehäusen. Der Controller steht <b>davor</b>, vor der Mitte der untersten Lage – er gehört nicht zum Turm.</p>
<div class="layers">{h.layer(['CCC', 'CCC', 'CCC', '_F_'], gk, 'Lage 1 (unten) + Controller davor', 34)}{h.layer(['CCC', 'C.C', 'CCC'], gk, 'Lage 2', 34)}{h.layer(['CCC', 'C.C', 'CCC'], gk, 'Lage 3', 34)}{h.layer(['CCC', 'CCC', 'CCC'], gk, 'Lage 4 (oben)', 34)}<div>{h.legend(gk)}</div></div>
{steps([
    f"34 Maschinengehäuse bauen: {c('standard_machine_casing')} (30 K je Block) oder {c('reinforced_machine_casing')} (50 K).",
    'Turm aufbauen: Lage 1 voll, Lagen 2 und 3 mit freier Mitte, Lage 4 voll.',
    'Davor (auf Höhe der untersten Lage, mittig) den <b>Controller</b> setzen, mit der Vorderseite nach außen – er schaut beim Setzen zu dir.',
    'GUI öffnen: Sie zeigt „Heat Capacity: … K“ – oder „Incomplete Machine Casing!“.',
    f"Strom: <b>128 EU/t</b>, Pakete bis 128 (MV) – z. B. von einer {c('mfe')} mit Goldkabel. Der Ofen nimmt Strom von allen Seiten.",
])}
<h3 id="gt--hitze">Hitze erreichen</h3>
{h.table(['Aufbau', 'Hitze', 'reicht für'], [
    ['34 Standard-Gehäuse', '1 020 K', 'Stahl (1 000 K), raffiniertes Eisen aus Erz'],
    ['34 Standard + 2 Lava in der Mitte', '1 520 K', 'Siliziumplatte, Titan (1 500 K), Silber/Blei aus Galena'],
    ['34 verstärkte Gehäuse', '1 700 K', 'Aluminium, Chrom (1 700 K)'],
    ['34 verstärkte + 2 Lava', '2 200 K', '–'],
    ['34 verstärkte + 2 Lava + Kanthal-Spulen (Stufe 1, +500 K)', '2 700 K', 'Wolfram (2 500 K)'],
    ['34 verstärkte + 2 Lava + Nichrom-Spulen (Stufe 2, +1 000 K)', '3 200 K', 'Wolframstahl (3 000 K)'],
])}
<ul>
<li>Lava in den beiden Mittelfeldern bringt je 250 K. Achtung: Ändern der Lava/Luft in der Mitte löst keine neue Prüfung aus – danach kurz ein Gehäuse abbauen und neu setzen.</li>
<li>Heizspulen: mit <b>4 Spulen</b> in der Hand Rechtsklick auf den Controller; Stufe 1 Kanthal, danach Stufe 2 Nichrom. Kupfernickel-Spulen sind nur Zutat für den Ofen selbst.</li>
<li>Bricht die Struktur während eines Rezepts, bleibt das Rezept stehen und läuft weiter, sobald sie wieder vollständig ist.</li>
</ul>
<h2 id="gt--fehler">Fehlersuche</h2>
{faq([
    ('Meine Maschine ist explodiert.', 'Paket über 32 EU (Hochofen: 128) – Transformator-Upgrade einbauen oder einen Trafo davor. Oder Feuer daneben.'),
    ('Ich habe nur „Machine Parts“ bekommen.', 'Die Maschine wurde ohne Schraubenschlüssel abgebaut. Das lässt sich nicht rückgängig machen.'),
    ('Die Maschine startet nicht.', 'Sie braucht über 900 EU im Speicher, bevor sie anfängt. Oder es fehlt Platz in den Ausgabe-Slots.'),
    ('„Incomplete Machine Casing!“', 'Ein Gehäuse fehlt oder ist falsch, die Mitte der Lagen 2/3 ist nicht Luft/Lava, oder der Controller steht nicht mittig vor der untersten Lage.'),
    ('„Insufficient Energy Line!“', 'Die Leitung liefert zu wenig – der Hochofen braucht 128 EU/t dauerhaft.'),
    ('Heat reicht nicht.', 'Siehe Tabelle: Lava in die Mitte, verstärkte Gehäuse, Heizspulen.'),
])}
<p>Referenz: <a href="#/spec-13">13 – GregTech-Maschinen</a>, <a href="#/spec-12">12 – GregTech-Materialien</a>.</p>
'''
    gt = gt.replace(' – keine Fackeln … nein: kein offenes Feuer/Lava daneben.', ' – kein offenes Feuer direkt daneben.')
    page('gregtech-maschinen', 'GregTech-Maschinen & Hochofen', gt)

    # ================================================================== EU <-> MJ
    uk = {'B': ('craft:energy_bridge', 'Energy Bridge', 'ctrl'), 'I': ('craft:ic2_lv_consumer', 'IC2 Consumer', ''), 'P': ('craft:bc_producer', 'BC Producer', ''),
          'b': ('craft:bc_consumer', 'BC Consumer', ''), 'p': ('craft:ic2_mv_producer', 'IC2 Producer', ''), 'E': ('craft:combustion_engine', 'Motor', ''),
          'Q': ('craft:quarry', 'Quarry', ''), 'c': ('craft:insulated_copper_cable', 'Kabel', ''), '_': (None, 'frei', 'ground'),
          'F': ('craft:fe_producer', 'FE Producer', ''), 'A': (None, 'AE2 Energy Acceptor', 'ctrl')}
    um = f'''<h1>Energie umwandeln: EU ↔ MJ</h1>
<p class="lead">EU (IndustrialCraft) und MJ (BuildCraft) sind getrennte Systeme. Es gibt genau zwei Brücken: die <b>Electrical Engine</b> von Forestry
(EU → MJ) und <b>Power Converters</b> (beide Richtungen). Für Mods mit <b>FE</b> (AE2, Ad Astra, Industrial Foregoing …) gibt es dazu den <b>FE Producer</b> (nur EU/MJ → FE).</p>
<div class="tocpos"></div>
{h.table(['Weg', 'Kurs', 'Besonderheiten'], [
    [c('electrical_engine'), '3 EU → 1 MJ (6 EU → 2 MJ pro Tick)', 'braucht Redstone; mit Platinen bis 36 EU → 10 MJ; nimmt jede Spannung an, explodiert nie'],
    ['Power Converters: IC2 Consumer → Bridge → BC Producer', '1 EU → 0,41 MJ (2,43 EU = 1 MJ)', 'günstiger als die Electrical Engine; jede Spannung wird angenommen'],
    ['Power Converters: BC Consumer → Bridge → IC2 Producer', '1 MJ → 2,43 EU', 'Producer gibt nur <b>volle Pakete</b> der Nennspannung ab'],
    ['Power Converters: IC2 Consumer → Bridge → FE Producer', '1 EU → 4 FE (1 MJ ≈ 9,7 FE)', 'nur Richtung FE; Reste unter 1 FE bleiben in der Bridge'],
])}
<h2 id="um--engine">Electrical Engine (EU → MJ)</h2>
{steps([
    f"{c('electrical_engine')} an die MJ-Maschine setzen (z. B. Quarry). Wie jeder Motor zeigt sie auf den nächsten MJ-Empfänger.",
    'Ein EU-Kabel an <b>irgendeine Seite</b>. Die Engine nimmt jede Paketgröße an und explodiert nie.',
    '<b>Redstone-Signal</b> geben – ohne Signal arbeitet sie nicht.',
    'Optional eine Batterie in den Slot: Dann läuft sie auch ohne Netz.',
])}
<ul>
<li>Standard: 6 EU → 2 MJ pro Tick. Mit einer Platine aus dem {c('carpenter')} und Röhren aus dem {c('thermionic_fabricator')}, aufgelötet mit dem {c('soldering_iron')}:</li>
</ul>
{h.table(['Röhre', 'Schaltung', 'EU', 'MJ', 'max. je Platine'], [
    [c('copper_electron_tube'), 'Electric Choke I', '−2', '−1', '1'],
    [c('tin_electron_tube'), 'Electric Boost I', '+7', '+2', '2'],
    [c('bronze_electron_tube'), 'Electric Boost II', '+15', '+4', '2'],
    [c('iron_electron_tube'), 'Electric Efficiency I', '−1', '0', '1'],
])}
<p>Platinen haben 1 (Basic), 2 (Enhanced), 3 (Refined) oder 4 (Intricate) Plätze. Beispiel: Intricate mit 2× Boost II = 36 EU → 10 MJ pro Tick.
Wird der MJ-Puffer nicht abgenommen, heizt sie sich auf und kühlt dann zwangsweise ab (verliert Energie, explodiert aber nicht).</p>
<h2 id="um--pc">Power Converters</h2>
<p>Herzstück ist die {c('energy_bridge')}: Um sie herum setzt du <b>Consumer</b> (nehmen Energie auf) und <b>Producer</b> (geben sie in der anderen Form ab). Alle müssen die Bridge direkt berühren.</p>
<div class="layers">{h.layer(['_E_', 'EbE', '_B_', '_p_', '_c_'], uk, 'Öl → Strom: Combustion-Motoren am BC Consumer, darunter die Bridge, darunter ein IC2 Producer mit Kabel', 34)}
{h.layer(['_c_', '_I_', '_B_', '_P_', '_Q_'], uk, 'Strom → Quarry: Kabel an den IC2 Consumer, Bridge, BC Producer an der Quarry', 34)}<div>{h.legend(uk)}</div></div>
<ul>
<li><b>IC2 Consumer</b> (LV/MV/HV/EV): nimmt EU von allen Seiten, jede Spannung, ohne Explosion.</li>
<li><b>IC2 Producer</b> (LV 32 / MV 128 / HV 512 / EV 2048): gibt pro Tick <b>ein volles Paket</b> der Nennspannung ab – erst wenn genug für ein ganzes Paket in der Bridge ist. Ein MV-Producer liefert also 128er Pakete: passendes Kabel und Speicher!</li>
<li><b>BC Consumer</b>: nimmt MJ von Motoren (min. 2, max. 100 MJ pro Hub) ohne Verlust.</li>
<li><b>BC Producer</b>: gibt MJ direkt an angrenzende MJ-Empfänger (jeden Tick, ohne Kolben).</li>
<li>Die Bridge puffert ≈ 3 657 MJ bzw. 8 888 EU; ihre GUI zeigt je Seite Typ, Spannung und Rate.</li>
<li>{c('universal_charger')}: lädt elektrische Gegenstände von Spielern, die darauf stehen, oder in angrenzenden Kisten.</li>
<li>Consumer und Producer gleicher Stufe lassen sich in der Werkbank ineinander umwandeln.</li>
</ul>
<h2 id="um--fe">FE Producer (Craft-Strom → FE)</h2>
<p>Neues Bauteil ohne Pack-Vorbild (ab Craft 1.1.1): {c('fe_producer')} an die Bridge setzen – er gibt den Strom als <b>FE</b> (Forge Energy) an Geräte anderer Mods ab, z. B. den <b>Energy Acceptor</b> von Applied Energistics 2.</p>
<div class="layers">{h.layer(['_c_', '_I_', '_B_', '_F_', '_A_'], uk, 'Strom → AE2: Kabel an den IC2 Consumer, Bridge, FE Producer, darunter der Energy Acceptor', 34)}<div>{h.legend(uk)}</div></div>
<ul>
<li><b>Kurs 1 EU = 4 FE</b>; über einen BC Consumer auch MJ → FE (1 MJ ≈ 9,7 FE).</li>
<li>Schiebt jeden Tick an <b>alle</b> angrenzenden FE-Empfänger (sie teilen sich die Menge); FE-Kabel anderer Mods können ihn auch anzapfen. Keine Spannung, nichts explodiert.</li>
<li>Reste unter 1 FE bleiben in der Bridge, es geht nichts verloren. Ohne Empfänger füllt sich nur die Bridge.</li>
<li>Kein Rückweg: FE, Create-Drehkraft & Co. kommen <b>nicht</b> ins EU-Netz.</li>
<li>Rezept: 4 Gold in den Ecken, Redstone-Block in der Mitte (wie die anderen Konverter).</li>
<li>Beispiel: 128 EU/t (ein MV-Kabel voll) → 512 FE/t ≈ 256 AE/t in AE2.</li>
</ul>
{h.note('tip', 'Öl lohnt sich: 1 Eimer Öl im Combustion-Motor = 60 000 MJ ≈ 145 800 EU, 1 Eimer Treibstoff = 600 000 MJ ≈ 1,46 Mio. EU.')}
<p>Referenz: <a href="#/spec-10">10 – Energieumwandlung</a>, <a href="#/spec-33">33 – FE Producer</a>.</p>
'''
    page('umwandlung', 'Energie umwandeln (EU ↔ MJ ↔ FE)', um)

    # ================================================================== IC2 Maschinen
    rows = []
    for oc in range(0, 5):
        eut = int(2 * 1.6 ** oc)
        ticks = max(1, round(400 * 0.7 ** oc))
        rows.append([str(oc), f'{eut} EU/t', f'{ticks} Ticks ({ticks / 20:.1f} s)'.replace('.', ','), f'{eut * ticks} EU'])
    ic = f'''<h1>IC2-Maschinen & Upgrades</h1>
<p class="lead">Elektroofen, Macerator, Extraktor, Kompressor und Recycler funktionieren alle gleich. Wer das Prinzip kennt, kann sie
automatisieren und mit Upgrades beschleunigen.</p>
<div class="tocpos"></div>
<h2 id="ic--werte">Grundwerte</h2>
{h.table(['Maschine', 'EU/t', 'Dauer', 'EU je Vorgang', 'max. Eingang'], [
    [c('electric_furnace'), '3', '130 Ticks', '390', '32'], [c('macerator'), '2', '400 Ticks', '800', '32'],
    [c('extractor'), '2', '400 Ticks', '800', '32'], [c('compressor'), '2', '400 Ticks', '800', '32'], [c('recycler'), '1', '45 Ticks', '45', '32'],
])}
<ul>
<li>Strom kommt von <b>allen Seiten</b>; Pakete über 32 EU sprengen die Maschine (außer mit Transformator-Upgrade).</li>
<li>Ohne Netz: <b>Redstone-Staub</b> in den Energie-Slot ergibt genau einen Vorgang, ebenso Batterien.</li>
<li>Fehlt nur Strom, bleibt der Fortschritt stehen; fehlt die Eingabe oder ist die Ausgabe voll, beginnt er von vorn.</li>
</ul>
<h2 id="ic--seiten">Automatisieren</h2>
{h.table(['Seite', 'Slot'], [['oben', 'Eingabe'], ['unten', 'Energie-Slot (Batterie/Redstone)'], ['Seiten', 'Ausgabe']])}
<p>Ein Trichter von oben füllt ein, ein Trichter oder ein Holz-Item-Rohr an der Seite zieht die Ergebnisse heraus. Die Pumpe, der Miner und der Kompressor arbeiten
nebeneinander zusammen (z. B. ein leerer Kompressor neben einer geladenen IC2-Pumpe über Wasser macht Schneebälle).</p>
<h2 id="ic--upgrades">Upgrades</h2>
<p>In die vier Upgrade-Slots unten in der GUI. Upgrades stapeln sich bis 4 (Pack-Einstellung von GregTech).</p>
{h.table(['Upgrade', 'Wirkung'], [
    [c('overclocker_upgrade'), 'je Stück: Tempo ×1/0,7, Verbrauch ×1,6 – also schneller, aber ≈ 14 % mehr EU pro Vorgang'],
    [c('transformer_upgrade'), 'je Stück: maximale Eingangsspannung ×4 (32 → 128 → 512 …) und Batterien höherer Stufe im Energie-Slot'],
    [c('energy_storage_upgrade'), 'je Stück +10 000 EU Puffer'],
])}
<h3 id="ic--oc">Overclocker am Beispiel Macerator</h3>
{h.table(['Overclocker', 'Verbrauch', 'Dauer', 'EU je Vorgang'], rows)}
<p>Mit vielen Overclockern steigt der Verbrauch schnell über 32 EU/t – dann braucht die Maschine MV-Strom und ein Transformator-Upgrade.</p>
<h2 id="ic--recycler">Recycler und Schrott</h2>
<ul>
<li>Der {c('recycler')} frisst jedes Item; mit 1/8 Chance entsteht {c('scrap') if 'scrap' in h.ITEMS else 'Schrott'}. Ausnahmen (nie Schrott): Glasscheibe, Stock, Schneeball, Eis, Schneeschicht, Schneeblock, Gerüst.</li>
<li>9 Schrott in der Werkbank → {c('scrap_box')}; per Rechtsklick geöffnet gibt sie zufällige Gegenstände (GregTech-Liste wie im Pack).</li>
<li>Im {c('mass_fabricator')} dient Schrott als Verstärker für die UU-Materie-Herstellung.</li>
</ul>
<p>Referenz: <a href="#/spec-04">04 – Maschinen</a>.</p>
'''
    page('ic2-maschinen', 'IC2-Maschinen & Upgrades', ic)

    return P
