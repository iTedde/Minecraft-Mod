"""Detailed build-and-connect guides. They replace the shorter pages of content.py with the same id.

Every statement was checked against docs/spec and the code in dev.craft.powerplants / compactsolars / ic2:
- controllers face the player when placed (opposite of the horizontal look direction), the structure lies behind
  them; the IC2 wrench cannot turn them (wrenchCanSetFacing = false)
- turbine/water turbine controllers emit EU to every side; turbine casings of a formed turbine conduct
  EU losslessly (only from their own turbine); every solar panel emits itself (solar arrays are leftovers since 2026-10-24)
- the boiler pushes up to 1000 mB steam per tick into every fluid handler next to its outer faces (round robin);
  all boiler blocks accept only water and give only steam; all turbine blocks accept only steam
- item access: top/sides = input, bottom = output (coke oven: coke, boiler: ash)
- steam turbine emits min(buffer, 64) per tick, buffer up to 128 -> packets up to 64 EU (MV)
- water turbine counts non-source water (falling water counts) up to 5 blocks around the cube, from its bottom layer
  up to 20 blocks above its top
"""


def faq(items):
    return ''.join(f'<details class="faq"><summary>{q}</summary><div>{a}</div></details>' for q, a in items)


def steps(items):
    return '<ol class="steps">' + ''.join(f'<li>{s}</li>' for s in items) + '</ol>'


def pages(h, stats):
    c = h.chip
    v = h.vchip
    NEW = '<span class="badge new">neu</span>'
    CHG = '<span class="badge chg">geändert</span>'
    P = []

    def page(pid, title, group, body):
        P.append({'id': pid, 'title': title, 'group': group, 'html': body})

    def mats(rows):
        return h.table(['Anzahl', 'Gegenstand', 'wofür'], rows)

    controller_rule = h.note('warn', '<b>Controller richtig setzen:</b> Ein Controller schaut beim Setzen immer '
        '<b>zu dir</b>, und die Struktur muss <b>hinter</b> ihm liegen. Stell dich also <b>außen</b> vor die Lücke, schau auf die '
        'Struktur und setze den Controller dann. Der Schraubenschlüssel kann Kraftwerks-Controller <b>nicht</b> drehen – steht er '
        'falsch, abbauen (er droppt sich selbst) und neu setzen.')

    # ================================================================== Strom anschließen
    strom = f'''<h1>Strom anschließen – Grundlagen</h1>
<p class="lead">Bevor du ein Kraftwerk baust: So fließt Strom (EU) in Craft. Wer das einmal verstanden hat, sprengt keine
Maschinen mehr. Alles hier ist 1:1 IndustrialCraft 2.</p>
<div class="tocpos"></div>
<h2 id="st--pakete">Pakete und Spannung</h2>
<ul>
<li>Jede Stromquelle schickt pro Tick <b>Pakete</b> ins Netz. Die Größe eines Pakets in EU ist seine <b>Spannung</b>.</li>
<li>Jeder Empfänger hat eine <b>maximale Eingangsspannung</b>. Kommt ein größeres Paket an, <b>explodiert</b> er sofort
(Block und Inventar sind weg).</li>
<li>Kabel haben eine Grenze, ab der sie <b>schmelzen</b>, und verlieren pro Block etwas Energie. Pakete werden auf dem Weg nicht
aufgeteilt – das Paket kommt um den Kabelverlust kleiner an.</li>
<li>Die Energie sucht sich automatisch den Weg zu allen Verbrauchern, die Strom wollen. Verbraucher, die nichts brauchen, bekommen nichts.</li>
</ul>
{h.table(['Stufe', 'Paket bis', 'typische Quellen', 'sichere Empfänger', 'passende Kabel'], [
    ['<b>LV</b>', '32 EU', 'Generator, Geothermie, Wasser-/Windmühle, Solarpanel (1, im 9×9-Feld 2er Pakete), Wasserturbine (32)', f"{c('batbox')}, alle Standardmaschinen", f"{c('insulated_copper_cable')} (0,2 EU Verlust/Block), {c('tin_cable')} nur bis 5 EU"],
    ['<b>MV</b>', '128 EU', 'MFE, <b>Dampfturbine (bis 64)</b>, MV-Array (64)', f"{c('mfe')}, LV-Transformator (Eingang)", f"{c('insulated_gold_cable')}, {c('double_insulated_gold_cable')}"],
    ['<b>HV</b>', '512 EU', 'MFSU, HV-Array (2 × 512 je Tick)', f"{c('mfsu')}, MV-Transformator (Eingang)", f"{c('glass_fibre_cable')} (0,025 EU/Block)"],
    ['<b>EV</b>', '2048 EU', 'HV-Transformator (hohe Seite)', 'HV-Transformator (Eingang)', f"{c('hv_cable')} und isolierte Varianten"],
])}
{h.note('warn', 'Ein Kupferkabel schmilzt bei einem Paket ab 33 EU, ein Goldkabel ab 129 EU, Glasfaser ab 513 EU. Unisolierte und schwach isolierte Kabel geben außerdem Stromschläge, wenn große Pakete durchlaufen.')}
<h2 id="st--speicher">Speicherblöcke (BatBox, MFE, MFSU)</h2>
<ul>
<li>Strom geht auf <b>5 Seiten hinein</b> und kommt nur auf der <b>Vorderseite</b> (dem Gesicht mit dem Punkt) wieder heraus – ein Paket pro Tick in Höhe der Stufe (32 / 128 / 512).</li>
<li>Die Vorderseite zeigt beim Setzen zu dir (auch nach oben/unten, wenn du steil schaust). Mit dem {c('wrench')} kannst du sie auf jede Seite drehen.</li>
<li>Oberer Slot lädt Werkzeuge/Batterien, unterer Slot entlädt sie ins Gerät. Ein Knopf in der GUI schaltet das Redstone-Verhalten durch
(z. B. „Signal, wenn voll“ oder „keine Abgabe bei Redstone“).</li>
</ul>
<h2 id="st--trafo">Transformatoren</h2>
<p>Ein Trafo verbindet zwei benachbarte Stufen. <b>Ohne Redstone</b> setzt er herunter: hinein auf der <b>Vorderseite</b>
(bis zur hohen Spannung), heraus auf den <b>anderen 5 Seiten</b> in Paketen der niedrigen Spannung (bis zu 4 Pakete pro Tick).
<b>Mit Redstone-Signal</b> arbeitet er umgekehrt (5 Seiten hinein, vorne ein großes Paket heraus).</p>
{h.table(['Trafo', 'niedrige Seite', 'hohe Seite'], [[c('lv_transformer'), '32', '128'], [c('mv_transformer'), '128', '512'], [c('hv_transformer'), '512', '2048']])}
<h2 id="st--beispiel">Beispiel: großes Paket für kleine Maschinen</h2>
<p>Die Dampfturbine schickt Pakete bis 64 EU. So kommst du sicher auf LV herunter:</p>
{steps([f"Dampfturbine → {c('insulated_gold_cable')} → <b>Vorderseite</b> eines {c('lv_transformer')} (Vorderseite zeigt zur Turbine).",
        f"Von einer der anderen 5 Seiten des Trafos mit {c('insulated_copper_cable')} weiter zu {c('batbox')} und Maschinen (32er Pakete).",
        f"Alternative: Turbine direkt an eine {c('mfe')} (nimmt bis 128) und von der MFE-Vorderseite über einen LV-Trafo weiter."])}
<h2 id="st--fehler">Typische Fehler</h2>
{faq([
    ('Meine Maschine ist explodiert.', 'Ein Paket war größer als ihre Eingangsspannung (Standardmaschinen: 32). Prüfe, welche Quelle MV/HV liefert (Dampfturbine, MV-/HV-Array, MFE/MFSU-Ausgang, Trafo im Aufwärts-Modus) und setze einen Trafo davor.'),
    ('Mein Kabel ist verschwunden.', 'Es ist geschmolzen – das Paket war zu groß für den Kabeltyp. Kupfer hält 32, Gold 128, Glasfaser 512, HV-Kabel 2048.'),
    ('Es kommt kein Strom an.', 'Speicher geben nur vorne ab (und nehmen dort nichts an), Trafos nehmen ohne Redstone nur vorne auf. Dampf- und Wasserturbine geben an jedem Block ab, aber nur fertig gebaut; Solar-Arrays nur in der Mitte eines vollständigen 3×3-Felds.'),
    ('Ich verliere viel Energie.', 'Jeder Kabelblock zieht Verlust ab (Kupfer 0,2, Gold 0,4–0,5, Glasfaser 0,025 EU je Block und Paket). Für lange Strecken Glasfaser oder höhere Spannung nutzen.'),
])}
<p>Vollständige Regeln: <a href="#/spec-01">01 – EU-System</a>, <a href="#/spec-03">03 – Speicher und Transformatoren</a>.</p>
'''
    page('strom', 'Strom anschließen (Grundlagen)', 'Allgemein', strom)

    # ================================================================== Kohlekraftwerk
    K = ('craft:boiler_casing', 'Kesselgehäuse', '')
    kk = {'K': K, 'F': ('craft:boiler_firebox', 'Kesselfeuerung (Controller)', 'ctrl'), '.': (None, 'Luft', 'air')}
    T = ('craft:turbine_casing', 'Turbinengehäuse', '')
    tk = {'T': T, 'S': ('craft:steam_turbine', 'Dampfturbine (Controller)', 'ctrl'), 'R': ('craft:turbine_rotor', 'Turbinenrotor', '')}
    plan_key = {'K': K, 'F': ('craft:boiler_firebox', 'Kesselfeuerung', 'ctrl'), '.': (None, 'Luft (Kesselinneres)', 'air'),
                'T': T, 'R': ('craft:turbine_rotor', 'Turbinenrotor', ''), 'S': ('craft:steam_turbine', 'Dampfturbine', 'ctrl'),
                'c': ('craft:insulated_gold_cable', 'Isoliertes Goldkabel', ''), 'M': ('craft:mfe', 'MFE', ''),
                '_': (None, 'Boden / frei', 'ground')}
    plan = ['___TTT___', 'KKKTRTKKK', 'F.KTRTK.F', 'KKKTRTKKK', '___TST___', '____c____', '____M____']
    kohle = f'''<h1>Kohlekraftwerk (Kessel + Dampfturbine)</h1>
{h.infobox('Kohlekraftwerk', 'craft:boiler_firebox', [
    ('Herkunft', NEW + ' Craft (Kraftwerke)'), ('Teile', 'Kessel 3×3×3 + Dampfturbine 3×3×5'),
    ('Brennstoff', 'Kohle (800 Ticks) oder Koks (1600 Ticks)'), ('Wasser', '20 mB/t je Kessel bei Volllast'),
    ('Dampf', 'bis 20 mB/t je Kessel'), ('Umwandlung', '1 mB Dampf = 1 EU'), ('Ausbeute', '16 000 EU / Kohle<br>32 000 EU / Koks'),
    ('Ausgabe', 'bis 64 EU/t, Pakete bis 64 EU (<b>MV</b>)'), ('Kessel je Turbine', 'bis 3'), ('Gefahr', 'Explosion Stärke 6 bei Trockenlauf'),
])}
<p class="lead">Das Kohlekraftwerk besteht aus zwei Multiblocks: Der <b>Kessel</b> verbrennt Kohle oder Koks und macht aus Wasser
<b>Dampf</b>, die <b>Dampfturbine</b> macht aus dem Dampf Strom. Ein IC2-Generator holt 4 000 EU aus einer Kohle, das Kraftwerk
<b>16 000 EU</b> – mit Koks aus dem <a href="#/koksofen">Koksofen</a> sogar <b>32 000 EU</b>.</p>
<div class="tocpos"></div>

<h2 id="kk--prinzip">So hängt alles zusammen</h2>
<div class="flow"><span>Wasser<br><small>Pumpe / Rohr / Eimer</small></span><b>→</b><span>Kessel<br><small>+ Kohle/Koks</small></span><b>→</b><span>Dampf<br><small>direkt oder per Rohr</small></span><b>→</b><span>Dampfturbine</span><b>→</b><span>Strom (MV)<br><small>an jedem Turbinenblock</small></span></div>
<ul>
<li><b>Dampf:</b> Der Kessel schiebt seinen Dampf <b>selbst</b> in jeden angrenzenden Flüssigkeitsbehälter – bis zu 1 000 mB pro Tick, reihum über
alle Außenflächen. Es reicht also, wenn <b>irgendein Kesselblock irgendeinen Turbinenblock berührt</b>. Ein Holzrohr oder eine Pumpe ist dafür nicht nötig.</li>
<li><b>Turbine:</b> Jeder ihrer 45 Blöcke nimmt Dampf an (nur Dampf). Sie verbraucht bis zu 64 mB pro Tick – genug für drei Kessel.</li>
<li><b>Strom:</b> kommt am Turbinen-Controller <b>und an jedem Turbinengehäuse</b> heraus – das ganze Konstrukt liefert. Kabel oder Speicher
dürfen an <b>jede freie Seite</b> der Turbine. Das Gehäuse leitet verlustfrei, aber nur Strom der eigenen Turbine und nur, solange sie fertig gebaut ist.</li>
<li><b>Wasser:</b> geht in <b>jeden</b> Kesselblock hinein (nur Wasser). Der Kessel gibt nie Wasser ab.</li>
</ul>

<h2 id="kk--material">Was du brauchst</h2>
{mats([
    ['33 (9 Rezepte à 4)', c('boiler_casing'), '25 für das Gehäuse + 8 für die Kesselfeuerung'],
    ['36 / 36 / 9', f"{c('iron_plate')}, {v('minecraft:bricks', 'Ziegelsteine')}, {v('minecraft:furnace', 'Ofen')}", 'für die 9 Rezepte Kesselgehäuse'],
    ['1', c('iron_furnace'), 'Kesselfeuerung'],
    ['45 (12 Rezepte à 4)', c('turbine_casing'), '41 für das Gehäuse + 4 für den Controller'],
    ['96 / 12', f"{c('steel_plate')}, {c('machine_casing')}", 'für die 12 Rezepte Turbinengehäuse'],
    ['5', c('turbine_rotor'), '3 in der Achse + 2 für den Controller (je 4 Stahlplatten, 4 Stahlbarren, 1 Eisenblock)'],
    ['2 / 1', f"{c('generator')}, {c('mv_transformer')}", 'Dampfturbine'],
])}
<p>Pro Kessel zusätzlich: 33 Kesselgehäuse + 1 Eisenofen. Stahlplatten kommen aus dem <a href="#/item/plate_bending_machine">Plattenbieger</a>,
Stahl aus dem <a href="#/item/industrial_blast_furnace">Industriellen Hochofen</a> – das Kraftwerk ist also Mid-Game.</p>

<h2 id="kk--kessel">Schritt 1: Den Kessel bauen</h2>
<div class="layers">{h.layer(['KKK', 'KKK', 'KKK'], kk, 'Ebene 1 (unten)')}{h.layer(['KKK', 'K.K', 'KFK'], kk, 'Ebene 2 (Mitte)')}{h.layer(['KKK', 'KKK', 'KKK'], kk, 'Ebene 3 (oben)')}<div>{h.legend(kk)}</div></div>
{steps([
    'Untere Ebene: 3×3 Kesselgehäuse legen.',
    'Mittlere Ebene: den Ring aus Kesselgehäuse setzen, die <b>Mitte frei lassen</b> (Luft!) und auf einer Seite in der Mitte eine Lücke für die Feuerung lassen.',
    'Obere Ebene: 3×3 Kesselgehäuse als Deckel.',
    'Außen vor die Lücke stellen, auf den Kessel schauen und die <b>Kesselfeuerung</b> in die Lücke setzen. Sie muss in der Mitte einer <b>Seitenfläche</b> sitzen – oben oder unten geht nicht.',
    'Rechtsklick auf die Feuerung öffnet die GUI. Sie zeigt Wasser- und Dampftank, Temperatur und Flamme – wenn das klappt, ist die Struktur fertig.',
])}
{controller_rule}

<h2 id="kk--turbine">Schritt 2: Die Dampfturbine bauen</h2>
<p>Ein liegender Quader 3 breit, 3 hoch und 5 lang. Die Längsachse zeigt von der Stirnseite mit dem Controller nach hinten.</p>
<div class="layers">{h.layer(['TTT', 'TST', 'TTT'], tk, 'Scheibe 1 (vorne)', 30)}{h.layer(['TTT', 'TRT', 'TTT'], tk, 'Scheibe 2', 30)}{h.layer(['TTT', 'TRT', 'TTT'], tk, 'Scheibe 3', 30)}{h.layer(['TTT', 'TRT', 'TTT'], tk, 'Scheibe 4', 30)}{h.layer(['TTT', 'TTT', 'TTT'], tk, 'Scheibe 5 (hinten)', 30)}<div>{h.legend(tk)}</div></div>
{steps([
    'Hinterste Scheibe: 3×3 Turbinengehäuse (senkrecht wie eine Wand).',
    'Scheiben 2–4: jeweils ein Ring aus 8 Turbinengehäusen mit einem <b>Turbinenrotor in der Mitte</b> – die drei Rotoren bilden die Achse.',
    'Vordere Scheibe: Ring aus 8 Turbinengehäusen, die Mitte frei lassen.',
    'Vor die Lücke stellen, auf die Turbine schauen und die <b>Dampfturbine</b> (Controller) einsetzen.',
    'Rechtsklick öffnet die GUI mit Dampftank, aktueller Leistung (EU/t) und Pufferanzeige.',
])}

<h2 id="kk--verbinden">Schritt 3: Kessel und Turbine verbinden</h2>
<p><b>Möglichkeit A – direkt:</b> Den Kessel so bauen, dass er die Turbine <b>berührt</b> (eine Seitenfläche liegt an der Turbine an). Fertig – der
Dampf fließt von selbst. Bis zu drei Kessel passen an eine Turbine: links, rechts und oben auf der langen Seite (oder einer an der hinteren Stirnseite).</p>
<div class="layers">{h.layer(plan, plan_key, 'Draufsicht (mittlere Ebene): zwei Kessel links und rechts an der Turbine, vorne Goldkabel zur MFE. Ein dritter Kessel kann oben auf die Turbine.', 30)}<div>{h.legend(plan_key)}</div></div>
<p><b>Möglichkeit B – per Rohr:</b> Steht der Kessel woanders, verbinde ihn mit <b>wasserdichten BuildCraft-Rohren</b>, z. B.
{c('stone_fluid_pipe')} oder {c('golden_fluid_pipe')}. Der Kessel füllt den Dampf selbst ins angrenzende Rohr, das Rohr leitet ihn zur Turbine
weiter – ein Holzrohr mit Motor ist <b>nicht</b> nötig.</p>
<ul>
<li>Durchsatz: Normale Flüssigkeitsrohre schaffen 10 mB je Takt, Gold- und Smaragdrohre 40 mB. Für mehr als einen Kessel an einer Leitung lieber Goldrohre nehmen oder direkt anbauen.</li>
<li>Bruchsteinrohre und Steinrohre verbinden sich nicht miteinander; Sandsteinrohre verbinden sich nur mit anderen Rohren.</li>
<li>Ein {c('tank')} dazwischen geht auch: Der Kessel füllt ihn, er muss aber selbst zur Turbine weitergeben (Tanks schieben nur nach unten in den Tank darunter).</li>
</ul>
{h.note('warn', 'Der Kessel schiebt Dampf in <b>jeden</b> angrenzenden Flüssigkeitsbehälter – auch in Tanks oder Rohre, die eigentlich Wasser bringen sollen. Wasser- und Dampfleitung deshalb nicht am selben Rohrstrang zusammenführen.')}

<h2 id="kk--wasser">Schritt 4: Wasser zuführen</h2>
<p>Ein Kessel braucht bei Volllast <b>20 mB Wasser pro Tick</b> (ein Eimer alle 2,5 Sekunden), drei Kessel 60 mB/t. Möglichkeiten:</p>
<ul>
<li><b>Von Hand:</b> Wassereimer oder {c('water_cell')} in den Behälter-Slot der GUI legen; der leere Behälter kommt in den Ausgabe-Slot daneben. Gut zum Testen, nicht für den Dauerbetrieb.</li>
<li><b>BuildCraft-Pumpe:</b> {c('pump')} über ein großes Gewässer setzen und mit einem Motor betreiben (10 MJ je 1 000 mB). Die Pumpe gibt ihr Wasser
jeden Tick an angrenzende Behälter ab – entweder direkt an einen Kesselblock oder über Flüssigkeitsrohre. Ein {c('redstone_engine')} liefert nur
0,05 MJ/t (≈ 5 mB Wasser/t) – zu wenig für einen Kessel. Nimm einen {c('stirling_engine')} (1 MJ/t ≈ 100 mB/t) oder einen {c('electrical_engine')}.</li>
<li><b>Aus einem Tank:</b> Mit einem {c('wooden_fluid_pipe')} (braucht einen Motor) Wasser aus einem Tank ziehen und per Rohr zum Kessel leiten.</li>
</ul>
{h.note('info', 'Die BC-Pumpe entfernt die Quellblöcke, die sie abpumpt (auch Wasser). Eine kleine 2×2-Unendlichquelle reicht deshalb nicht dauerhaft – pumpe aus einem See oder Ozean.')}

<h2 id="kk--brennstoff">Schritt 5: Brennstoff und Asche</h2>
<ul>
<li>Nur <b>Kohle</b> und <b>Koks</b> (keine Holzkohle, keine Kohleblöcke). Per Hand in den Brennstoff-Slot oder automatisch: Ein Trichter oder Item-Rohr an
<b>Oberseite oder Seiten irgendeines Kesselblocks</b> füllt den Brennstoff-Slot.</li>
<li><b>Asche:</b> je 4 verbrannte Kohle (bzw. 2 Koks) entsteht 1 Asche im Asche-Slot. Ist er voll (64), legt der Kessel nichts mehr nach – das Kraftwerk geht aus.
Ein Trichter <b>unter</b> einem Kesselblock zieht die Asche automatisch ab (Unterseite = Ausgabe).</li>
</ul>

<h2 id="kk--strom">Schritt 6: Strom abnehmen</h2>
<p>Der Strom kommt an <b>jedem Block der Turbine</b> heraus (Controller und Gehäuse), in Paketen <b>bis 64 EU – das ist MV</b>. Lege das Kabel dahin, wo es am besten passt.</p>
{h.note('warn', 'Mit einem Kessel sind die Pakete meist nur 20 EU groß. Sobald aber der Puffer (128 EU) volläuft – etwa weil kurz niemand Strom braucht –, kommen <b>64er Pakete</b>. Ein Kupferkabel schmilzt dann, eine BatBox oder LV-Maschine explodiert. Schließe die Turbine deshalb immer MV-fest an.')}
{steps([
    f"Direkt vor den Controller ein {c('insulated_gold_cable')} legen (hält bis 128 EU) – oder gleich eine {c('mfe')} davorstellen.",
    f"Zur MFE führen oder an die <b>Vorderseite</b> eines {c('lv_transformer')}. Der Trafo gibt auf seinen anderen Seiten sichere 32er Pakete für BatBox und Maschinen aus.",
    'Mehr dazu: <a href="#/strom">Strom anschließen (Grundlagen)</a>.',
])}

<h2 id="kk--betrieb">Betrieb: Was im Kessel passiert</h2>
<ol>
<li><b>Nachlegen:</b> Ist die Brennzeit abgelaufen, der Dampftank nicht voll, Wasser vorhanden und der Asche-Slot nicht voll, wird 1 Brennstoff verbraucht – Kohle brennt <b>800 Ticks</b>, Koks <b>1 600 Ticks</b>.</li>
<li><b>Temperatur:</b> Start 20 °C. Mit Feuer +1 °C pro Tick bis 1000 °C, ohne Feuer −1 °C pro Tick bis 20 °C. Bis zur Volllast dauert es <b>980 Ticks (≈ 49 s)</b>; der Brennstoff brennt währenddessen mit voller Rate weiter – ein Kaltstart kostet also Energie (gewollt).</li>
<li><b>Dampf:</b> ab 100 °C entstehen <code>20 · (T − 100) / 900</code> mB pro Tick, also 20 mB/t bei 1000 °C. Für jedes mB Dampf wird 1 mB Wasser verbraucht. Beide Tanks fassen 16 000 mB.</li>
<li><b>Wenn kein Strom gebraucht wird:</b> Der Turbinenpuffer läuft voll, die Turbine nimmt keinen Dampf mehr an, ihr Dampftank und dann der des Kessels füllen sich. Bei vollem Dampftank legt der Kessel nichts nach – es geht kein Brennstoff verloren, nur die Hitze sinkt langsam.</li>
</ol>
<div class="calc" data-calc="boiler"><h4>Rechner: Kohlekraftwerk</h4><div class="row">
<label>Brennstoff <select name="fuel"><option value="coal">Kohle</option><option value="coke">Koks</option></select></label>
<label>Kessel an der Turbine <select name="nb"><option>1</option><option>2</option><option selected>3</option></select></label></div>
<div class="out"></div><svg viewBox="0 0 520 170" style="color:var(--ink2)"></svg></div>

<h2 id="kk--gefahr">Gefahr: Trockenlauf und Explosion</h2>
<p>Läuft der Wassertank leer, während der Kessel über 100 °C heiß ist, wird er <b>„trocken“</b>. Dann gilt:</p>
<ul>
<li>Kommt <b>wieder Wasser</b> hinein, solange er trocken ist → <b>Explosion</b>.</li>
<li>Erreicht er trocken <b>1000 °C</b> → <b>Explosion</b>.</li>
<li>Kühlt er auf 100 °C oder weniger ab → wieder sicher.</li>
</ul>
<p>Die Explosion ist eine IC2-Explosion der Stärke 6 in der Kesselmitte; alle Kesselblöcke und der Inhalt sind weg.</p>
{h.note('tip', '<b>Wasser ausgefallen?</b> Wasserzufuhr <b>nicht</b> sofort wieder anschalten. Brennstoff aus dem Slot nehmen und in der GUI warten, bis die Temperatur unter 100 °C gefallen ist (bis zu 900 Ticks = 45 s). Erst dann wieder Wasser zuführen.')}

<h2 id="kk--fehler">Fehlersuche</h2>
{faq([
    ('Die GUI öffnet sich nicht / es passiert nichts.', 'Die Struktur ist nicht vollständig: Mitte des Kessels muss Luft sein, alle 25 Gehäuse müssen da sein, die Feuerung muss in der Mitte einer Seitenfläche sitzen und nach außen schauen. Die Struktur wird beim Setzen/Abbauen eines Teils und spätestens alle 100 Ticks (5 s) geprüft.'),
    ('Der Kessel heizt nicht.', 'Er legt nur nach, wenn Wasser im Tank ist, der Dampftank nicht voll ist und der Asche-Slot Platz hat. Holzkohle und Kohleblöcke werden nicht angenommen.'),
    ('Die Turbine bekommt keinen Dampf.', 'Berührt der Kessel die Turbine wirklich (nicht nur über Eck)? Sind beide Strukturen fertig? Läuft der Dampf über Rohre: wasserdichte Rohre verwenden und darauf achten, dass Bruchstein- und Steinrohre sich nicht verbinden.'),
    ('Die Turbine läuft, aber es kommt kein Strom an.', 'Ist die Turbine fertig gebaut (GUI zeigt Leistung)? Nur dann leitet das Gehäuse. Ein Speicher nimmt auf seiner <b>Ausgabeseite</b> (Gesicht mit dem Punkt) nichts an – dreh ihn so, dass sie von der Turbine weg zeigt. Ist kein Verbraucher angeschlossen oder alle voll, verbraucht die Turbine keinen Dampf.'),
    ('Mein Kabel ist geschmolzen / die BatBox ist explodiert.', 'Die Turbine liefert MV-Pakete bis 64 EU. Goldkabel und MFE oder einen LV-Trafo verwenden (siehe Schritt 6).'),
    ('Teile zwei Kessel ein Gehäuse?', 'Nein – jeder Gehäuseblock gehört genau einem Controller. Zwei Kessel brauchen zwei vollständige Würfel, die sich höchstens berühren.'),
    ('Der Kessel ist explodiert, obwohl Wasser drin war.', 'Er war vorher kurz trocken (Wasser leer bei über 100 °C) und dann kam wieder Wasser – siehe „Gefahr“.'),
])}

<h2 id="kk--rezepte">Rezepte</h2>
<div class="recipes">
{h.recipe(['PBP', 'BFB', 'PBP'], {'P': ('craft:iron_plate', None), 'B': ('minecraft:bricks', 'Ziegelsteine'), 'F': ('minecraft:furnace', 'Ofen')}, ('craft:boiler_casing', None), 4)}
{h.recipe(['CCC', 'CIC', 'CCC'], {'C': ('craft:boiler_casing', None), 'I': ('craft:iron_furnace', None)}, ('craft:boiler_firebox', None))}
{h.recipe(['PPP', 'PMP', 'PPP'], {'P': ('craft:steel_plate', None), 'M': ('craft:machine_casing', None)}, ('craft:turbine_casing', None), 4)}
{h.recipe(['PSP', 'SIS', 'PSP'], {'P': ('craft:steel_plate', None), 'S': ('craft:steel_ingot', None), 'I': ('minecraft:iron_block', 'Eisenblock')}, ('craft:turbine_rotor', None))}
{h.recipe(['CRC', 'GMG', 'CRC'], {'C': ('craft:turbine_casing', None), 'R': ('craft:turbine_rotor', None), 'G': ('craft:generator', None), 'M': ('craft:mv_transformer', None)}, ('craft:steam_turbine', None))}
</div>
<p>Blockwerte: Gehäuse und Controller Härte 2,0, Resistenz 10, mit der Spitzhacke abbauen; ein abgebautes Teil droppt sich selbst, der Controller
zusätzlich seinen Inhalt (Flüssigkeiten gehen verloren). Spezifikation: <a href="#/spec-14">14 – Kraftwerke</a> §3–4.</p>
'''
    page('kohlekraftwerk', 'Kohlekraftwerk', 'Neu & geändert', kohle)

    # ================================================================== Koksofen
    B = ('craft:coke_oven_bricks', 'Koksofenziegel', '')
    ko_key = {'B': B, 'C': ('craft:coke_oven', 'Koksofen (Controller)', 'ctrl'), '.': (None, 'Luft', 'air')}
    side_key = {'B': B, 'C': ('craft:coke_oven', 'Koksofen', 'ctrl'), 'H': ('minecraft:hopper', 'Trichter', ''),
                'X': ('minecraft:chest', 'Truhe', ''), '_': (None, 'frei', 'ground')}
    koks = f'''<h1>Koksofen</h1>
{h.infobox('Koksofen', 'craft:coke_oven', [
    ('Herkunft', NEW + ' Craft (Kraftwerke)'), ('Typ', 'Multiblock 3×3×3, innen hohl'), ('Strom', 'keiner'),
    ('Eingabe', 'nur Kohle (keine Holzkohle)'), ('Ausgabe', 'Koks'), ('Dauer', '900 Ticks = 45 s je Kohle'),
    ('Durchsatz', '80 Koks pro Stunde und Ofen'), ('Härte / Resistenz', '2,0 / 10, Spitzhacke'),
])}
<p class="lead">Der Koksofen verwandelt Kohle ohne Strom in <b>Koks</b>. Koks verhält sich fast überall wie Kohle – im Ofen
1600 Ticks, im IC2-Generator 4 000 EU, in der Stirling Engine wie Kohle. <b>Nur im <a href="#/kohlekraftwerk">Kohlekraftwerk</a> bringt er das
Doppelte</b> (32 000 statt 16 000 EU), weil er im Kessel doppelt so lange brennt. Das ist der Grund, Kohle zu verkoken.</p>
<div class="tocpos"></div>
<h2 id="ko--material">Was du brauchst</h2>
{mats([
    ['33 (17 Rezepte à 2)', c('coke_oven_bricks'), '25 für den Würfel + 8 für den Controller'],
    ['68 / 68 / 17', f"{v('minecraft:brick', 'Ziegel')}, {v('minecraft:sand', 'Sand')}, {v('minecraft:clay_ball', 'Ton')}", 'für die 17 Ziegel-Rezepte (je 4 Ziegel, 4 Sand, 1 Ton)'],
    ['1', v('minecraft:furnace', 'Ofen'), 'Controller'],
])}
<h2 id="ko--aufbau">Aufbau Schritt für Schritt</h2>
<div class="layers">{h.layer(['BBB', 'BBB', 'BBB'], ko_key, 'Ebene 1 (unten)')}{h.layer(['BBB', 'B.B', 'BCB'], ko_key, 'Ebene 2 (Mitte)')}{h.layer(['BBB', 'BBB', 'BBB'], ko_key, 'Ebene 3 (oben)')}<div>{h.legend(ko_key)}</div></div>
{steps([
    'Untere Ebene: 3×3 Koksofenziegel.',
    'Mittlere Ebene: Ring aus Ziegeln, <b>Mitte frei</b> lassen und auf einer Seite die mittlere Position frei lassen.',
    'Obere Ebene: 3×3 Ziegel als Deckel.',
    'Außen vor die Lücke stellen, auf den Ofen schauen und den <b>Koksofen</b> einsetzen (nur an einer Seitenfläche, nicht oben/unten).',
    'Rechtsklick öffnet die GUI: links Kohle hinein, rechts kommt der Koks heraus, dazwischen Flamme und Fortschritt.',
])}
{controller_rule}
<h2 id="ko--auto">Automatisieren</h2>
<p>Jeder Block des Ofens – Controller und alle 25 Ziegel – ist ein Zugang zum Inventar:</p>
<ul>
<li><b>Oben und an den Seiten</b> geht es in den <b>Eingabe-Slot</b> (nur Kohle wird angenommen).</li>
<li><b>Unten</b> kommt der <b>Koks</b> heraus.</li>
</ul>
<div class="layers">{h.layer(['_H_', 'BBB', 'BBB', 'BBB', '_H_', '_X_'], side_key, 'Seitenansicht: Trichter oben füllt Kohle ein, Trichter unten zieht Koks in eine Truhe', 30)}<div>{h.legend(side_key)}</div></div>
<p>Statt Trichtern gehen auch BuildCraft-Item-Rohre: Ein {c('wooden_transport_pipe')} mit Motor an der Unterseite zieht den Koks heraus, ein Rohr von oben liefert Kohle an.
Mehrere Öfen nebeneinander lassen sich über eine gemeinsame Rohrleitung versorgen.</p>
<h2 id="ko--betrieb">So arbeitet er</h2>
<ul>
<li>1 Kohle → 1 Koks in <b>900 Ticks</b>, immer eine Kohle gleichzeitig. Holzkohle wird nicht angenommen.</li>
<li>Fehlt Kohle, bleibt der Fortschritt stehen (kein Zurücksetzen); ist die Ausgabe voll, wartet der Ofen.</li>
<li>Keine Asche, kein Kreosot, kein Strom.</li>
<li>Die Struktur wird beim Setzen/Abbauen eines Teils und spätestens alle 100 Ticks geprüft. Unvollständig arbeitet der Ofen nicht, sein Inhalt bleibt aber erhalten.</li>
<li>9 Koks ⇄ 1 Koksblock zum Lagern. Der Koksblock brennt 16 000 Ticks im Ofen – im Kessel wird er aber <b>nicht</b> angenommen, dort nur einzelne Kohle/Koks.</li>
</ul>
<h2 id="ko--rechnung">Lohnt sich das?</h2>
<p>Pro Kohle bringt das Verkoken im Kraftwerk 16 000 EU mehr. Ein Ofen schafft 80 Koks pro Stunde; ein Kessel bei Volllast verbraucht alle
1 600 Ticks einen Koks, also 45 Koks pro Stunde. <b>Zwei Koksöfen versorgen etwa drei Kessel</b> (135 Koks/h Bedarf bei 160 Koks/h Produktion).</p>
<h2 id="ko--fehler">Fehlersuche</h2>
{faq([
    ('Der Ofen nimmt keine Kohle an.', 'Holzkohle wird abgelehnt. Prüfe außerdem, ob die Struktur fertig ist (Mitte Luft, Controller außen an einer Seitenmitte).'),
    ('Der Trichter unten zieht nichts.', 'Er muss unter einem Ofenblock hängen und in eine Truhe oder einen weiteren Trichter zeigen. Unten kommt nur der Koks heraus.'),
    ('Der Fortschritt geht nicht weiter.', 'Ist der Ausgabe-Slot voll (64 Koks)? Dann wartet der Ofen.'),
])}
<h2 id="ko--rezepte">Rezepte</h2>
<div class="recipes">
{h.recipe(['BSB', 'SCS', 'BSB'], {'B': ('minecraft:brick', 'Ziegel'), 'S': ('minecraft:sand', 'Sand'), 'C': ('minecraft:clay_ball', 'Ton')}, ('craft:coke_oven_bricks', None), 2)}
{h.recipe(['BBB', 'BFB', 'BBB'], {'B': ('craft:coke_oven_bricks', None), 'F': ('minecraft:furnace', 'Ofen')}, ('craft:coke_oven', None))}
</div>
<p>Spezifikation: <a href="#/spec-14">14 – Kraftwerke</a> §1–2.</p>
'''
    page('koksofen', 'Koksofen', 'Neu & geändert', koks)

    # ================================================================== Wasserturbine
    W = ('craft:water_turbine_casing', 'Wasserturbinengehäuse', '')
    wk = {'W': W, 'C': ('craft:water_turbine', 'Wasserturbine (Controller)', 'ctrl'),
          '~': (None, 'Messbereich: hier zählt fließendes Wasser', 'flow'),
          'x': (None, 'außerhalb – zählt nicht mehr', 'nocount')}
    area = ['x' * 15] + ['x' + '~' * 13 + 'x'] * 5 + ['x~~~~~WWW~~~~~x', 'x~~~~~WWW~~~~~x', 'x~~~~~WCW~~~~~x'] \
        + ['x' + '~' * 13 + 'x'] * 5 + ['x' * 15]
    # side view: cut through the controller (left) along its facing axis
    hk = {'~': (None, 'Messbereich: fließendes/fallendes Wasser zählt', 'flow'),
          'x': (None, 'Höhe/Abstand außerhalb – zählt nicht', 'nocount'),
          'W': W, 'C': ('craft:water_turbine', 'Wasserturbine (Controller)', 'ctrl'), '_': (None, 'fester Boden', 'ground')}
    heights = ['ab 21 über dem Deckel', '1–20 über dem Deckel', 'obere Ebene', 'mittlere Ebene', 'untere Ebene', 'unter der Turbine']
    zones = ['x' * 15, 'x' + '~' * 13 + 'x', 'x~~~~~WWW~~~~~x', 'x~~~~~CWW~~~~~x', 'x~~~~~WWW~~~~~x', '_' * 15]
    sk = {'Q': (None, 'Wasserquelle (Eimer) – zählt selbst nicht', 'water'), '~': (None, 'fallendes / fließendes Wasser – zählt', 'flow'),
          'W': W, '_': (None, 'fester Boden', 'ground'), ' ': (None, 'Luft', 'air'), 'C': ('craft:water_turbine', 'Wasserturbine (Controller)', 'ctrl')}
    deck = ['             ', '     ~Q~     ', '    ~WWW~    ', '    ~CWW~    ', '~~~~~WWW~~~~~', '_____________']
    deck_heights = ['darüber', '1 über dem Deckel', 'obere Ebene', 'mittlere Ebene', 'untere Ebene', 'unter der Turbine']
    wt = f'''<h1>Wasserturbine</h1>
{h.infobox('Wasserturbine', 'craft:water_turbine', [
    ('Herkunft', NEW + ' Craft (Kraftwerke)'), ('Typ', 'Multiblock 3×3×3, voll'), ('Leistung', '0,25 EU/t je fließendem Wasserblock'),
    ('Fluss-Biom', '×1,5'), ('Deckel', '<b>32 EU/t</b>'), ('Pakete', 'bis 32 EU (LV)'), ('Mindestens', '16 fließende Blöcke'),
    ('Messung', 'alle 128 Ticks'), ('GUI', 'keine – Rechtsklick zeigt den Zustand im Chat'),
])}
<p class="lead">Die große Schwester der <a href="#/wassermuehle">Wassermühle</a>: zuverlässige <b>bis 32 EU/t</b> rund um die Uhr, ohne Brennstoff.
Entscheidend ist <b>fließendes</b> Wasser rund um und über der Turbine – stilles Wasser (Quellblöcke) zählt nicht.</p>
<div class="tocpos"></div>
<h2 id="wt--material">Was du brauchst</h2>
{mats([
    ['30 (15 Rezepte à 2)', c('water_turbine_casing'), '26 für den Würfel + 4 für den Controller'],
    ['120 / 15', f"{c('refined_iron_plate')}, {c('machine_casing')}", 'für die 15 Gehäuse-Rezepte'],
    ['4 / 1', f"{c('water_mill')}, {c('generator')}", 'Controller'],
    ['1+', v('minecraft:water_bucket', 'Wassereimer'), 'für Wasser auf dem Deckel (siehe unten)'],
])}
<h2 id="wt--aufbau">Aufbau</h2>
<p>Ein <b>voller</b> Würfel 3×3×3 (auch die Mitte ist ein Gehäuseblock). Der Controller sitzt in der Mitte einer Seitenfläche.</p>
{steps([
    'Untere und obere Ebene: je 3×3 Wasserturbinengehäuse.',
    'Mittlere Ebene: 3×3 Gehäuse, aber auf einer Seite die mittlere Position frei lassen.',
    'Außen vor die Lücke stellen, auf den Würfel schauen und die <b>Wasserturbine</b> einsetzen.',
    'Rechtsklick auf den Controller: „Fließendes Wasser: n, Leistung x EU/t“ – oder „Struktur unvollständig“.',
])}
{controller_rule}
<h2 id="wt--wasser">Das Wasser: worauf es ankommt</h2>
<p>Die Turbine zählt fließendes Wasser in einem großen <b>Messbereich</b> rund um und über dem Würfel: 5 Blöcke Abstand nach allen Seiten,
von der unteren Turbinenebene bis 20 Blöcke über den Deckel. Der Würfel selbst zählt nicht.</p>
<div class="layers">{h.layer(area, wk, 'Draufsicht (gilt für jede gezählte Höhe): Turbine 3×3 in der Mitte, Messbereich 13×13 = 5 Blöcke rund um die Turbine, auch die Ecken. Über dem Deckel zählt auch die Fläche direkt über der Turbine.', 18)}
{h.layer(zones, hk, 'Seitenansicht (Schnitt durch den Controller): gezählt wird von der unteren Turbinenebene bis 20 Blöcke über dem Deckel.', 18, labels=heights)}<div>{h.legend(hk)}</div></div>
<ul>
<li><b>Wo genau:</b> horizontal bis 5 Blöcke neben der Turbine (an allen vier Seiten und in den Ecken), vertikal von der unteren Turbinenebene
bis 20 Blöcke über dem Deckel. Auf Höhe der Turbine sind das je Ebene 160 Positionen, darüber je 169 – zusammen <b>3860</b>.</li>
<li><b>Was zählt:</b> Wasser, das <b>kein Quellblock</b> ist – fließendes Wasser und <b>herabfallendes</b> Wasser (Wasserfall). Die Fließrichtung ist egal.</li>
<li><b>Was nicht zählt:</b> Quellblöcke (stilles Wasser, Ozean, die meisten natürlichen Flüsse), Wasser unter dem Boden der Turbine,
Wasser ab 6 Blöcken Abstand oder ab 21 Blöcken über dem Deckel, Seegras/Seetang/Blasensäulen und Blöcke mit Wasser darin (z. B. wassergefüllte Stufen).</li>
<li>Gezählt wird alle 128 Ticks (≈ 6 s). Das Fluss-Biom (×1,5) wird am Controller-Block geprüft.</li>
<li>Unter 16 solchen Blöcken: 0 EU/t. Darüber 0,25 EU/t je Block, im Fluss-Biom ×1,5, höchstens 32 EU/t.
Volllast also ab <b>128</b> fließenden Blöcken, im Fluss-Biom ab 86.</li>
</ul>
{h.note('warn', '<b>Natürliche Flüsse bestehen fast nur aus Quellblöcken</b> – die zählen nicht! Eine Turbine einfach in den Fluss zu stellen bringt deshalb oft wenig. Das Fluss-Biom bringt aber den Faktor 1,5, egal woher das fließende Wasser kommt.')}
<h3 id="wt--varianten">Was welcher Aufbau bringt</h3>
{h.table(['Aufbau', 'gezählte Blöcke', 'Leistung', 'Fluss-Biom'], [
    ['Turbine in stillem Wasser / Ozean / natürlichem Fluss (Quellblöcke)', 'meist 0', '0 EU/t', '0 EU/t'],
    ['Bach am Boden nur an <b>einer</b> Seite, 2 breit (13 lang)', '26', '6,5 EU/t', '9,75 EU/t'],
    ['Wasserfall: eine Säule fallendes Wasser von 20 über dem Deckel bis zum Boden neben der Turbine', '23', '5,75 EU/t', '8,6 EU/t'],
    ['Sechs solcher Wasserfall-Säulen', '138', '<b>32 EU/t</b> (Deckel)', '<b>32 EU/t</b>'],
    ['<b>Bach am Boden rundherum</b>: die ganze untere Ebene im Messbereich fließt', 'bis 160', '<b>32 EU/t</b> (Deckel)', '<b>32 EU/t</b>'],
])}
<h3 id="wt--deckel">Einfachster Aufbau: Wasser auf den Deckel</h3>
<p>Eine Wasserquelle mitten auf dem Turbinendeckel reicht oft schon: Das Wasser läuft über den Deckel, fällt an allen Kanten herunter und
breitet sich am Boden rund um die Turbine aus – alles davon liegt im Messbereich.</p>
<div class="layers">{h.layer(deck, sk, 'Seitenansicht (Schnitt durch den Controller): Quelle auf dem Deckel, das Wasser fällt an den Kanten herunter und läuft am Boden auseinander.', 26, labels=deck_heights)}<div>{h.legend(sk)}</div></div>
{steps([
    'Turbine auf festem, ebenem Boden bauen (z. B. Stein). Rund um die Turbine muss auf Höhe des Bodens ebenfalls fester Boden sein, damit sich das Wasser dort ausbreitet.',
    'Einen Eimer Wasser mitten auf den Deckel setzen.',
    'Rechtsklick auf den Controller zeigt die gezählten Blöcke. Die Messung erfolgt alle 128 Ticks – kurz warten.',
    'Reicht es nicht für 128 Blöcke, weitere Quellen auf den Deckel oder höher setzen: Fallendes Wasser bis 20 Blöcke über dem Deckel zählt mit.',
])}
<p>Blöcke im Messbereich nehmen dem Wasser Platz weg – jede belegte Position fehlt. Bei 3860 Positionen ist meist aber reichlich Luft.</p>
<h2 id="wt--strom">Strom abnehmen</h2>
<p>Der Strom kommt am <b>Controller und an jedem Gehäuseblock</b> heraus – am praktischsten ist ein Kabel an der <b>Unterseite</b> oder an einer Seite, wo kein Wasser läuft. Die Pakete sind höchstens
<b>32 EU (LV)</b>: {c('insulated_copper_cable')}, {c('batbox')} und alle Standardmaschinen vertragen das.</p>
<div class="calc" data-calc="waterturbine"><h4>Rechner: Wasserturbine</h4><div class="row">
<label>gezählte fließende Wasserblöcke <input type="number" name="n" min="0" max="3860" value="90"></label>
<label><input type="checkbox" name="r" checked> Fluss-Biom</label></div><div class="out"></div><div class="msg"></div></div>
<h2 id="wt--fehler">Fehlersuche</h2>
{faq([
    ('„Struktur unvollständig“', 'Der Würfel muss komplett voll sein (auch die Mitte), 26 Gehäuse + Controller in einer Seitenmitte, nach außen zeigend.'),
    ('„Fließendes Wasser: 0“ obwohl die Turbine im Wasser steht', 'Das Wasser besteht aus Quellblöcken. Nur fließendes oder fallendes Wasser zählt – siehe Wasserfall-Aufbau.'),
    ('Unter 16 Blöcke – 0 EU/t', 'Mehr fließendes Wasser in den Messbereich bringen; die Schwelle ist 16.'),
    ('Der Wert ändert sich nicht sofort', 'Gemessen wird nur alle 128 Ticks (≈ 6 s).'),
])}
<h2 id="wt--rezepte">Rezepte</h2>
<div class="recipes">
{h.recipe(['RRR', 'RMR', 'RRR'], {'R': ('craft:refined_iron_plate', None), 'M': ('craft:machine_casing', None)}, ('craft:water_turbine_casing', None), 2)}
{h.recipe(['WCW', 'CGC', 'WCW'], {'W': ('craft:water_mill', None), 'C': ('craft:water_turbine_casing', None), 'G': ('craft:generator', None)}, ('craft:water_turbine', None))}
</div>
<p>Spezifikation: <a href="#/spec-14">14 – Kraftwerke</a> §5.</p>
'''
    page('wasserturbine', 'Wasserturbine', 'Neu & geändert', wt)

    # ================================================================== Solar (since 2026-10-24: panels only)
    P_ = ('craft:solar_panel', 'Solarpanel mit Bonus', 'pan')
    p_ = ('craft:solar_panel', 'Solarpanel ohne Bonus', 'flow')
    key_field = {'P': P_, 'p': p_}
    solar = f'''<h1>Solar: Solarpanels und Solarfelder</h1>
{h.infobox('Solarpanel', 'craft:solar_panel', [
    ('Herkunft', 'IndustrialCraft 2'), ('Status', CHG + ' Balancing (2026-10-24)'), ('Leistung', '1 EU/t bei Sonne'),
    ('Feldbonus', '<b>×1,2</b> im lückenlosen 9×9-Quadrat'), ('Paket', 'LV (1 bzw. 2 EU)'),
    ('Solar Arrays', 'stillgelegt, nur noch zerlegbar'),
])}
<p class="lead">Solarstrom kommt in Craft nur noch aus dem <b>IC2-Solarpanel</b>. Jedes Panel liefert selbst 1 EU/t, solange es
die Sonne sieht. Wer viele Panels zu einem großen Feld zusammenstellt, bekommt einen kleinen <b>Bonus von ×1,2</b>.</p>
{h.compare('<ul><li>Solarpanel 1 EU/t</li><li>Compact Solar Arrays: 1 Block = 8 / 64 / 512 EU/t</li><li>dazu Solar-Hüte als Rüstung</li></ul>',
           '<ul><li>Solarpanel 1 EU/t, im 9×9-Feld <b>1,2 EU/t</b></li><li>Solar Arrays ohne Funktion, zerlegbar</li><li>Solar-Hüte entfallen</li></ul>')}
<div class="tocpos"></div>
<h2 id="sol--regel">Die Regel</h2>
<ul>
<li>Jedes Solarpanel liefert bei Sonne <b>1 EU/t</b>: Himmelssicht direkt darüber, Tag, kein Regen (in Wüsten auch bei Regen).</li>
<li>Liegt ein Panel in einem <b>lückenlosen Quadrat aus 9×9 Solarpanels auf derselben Höhe</b>, liefert es <b>1,2 EU/t</b>
(jeden 5. Tick 2 EU statt 1).</li>
<li>Das Quadrat darf beliebig liegen. Ein 10×10-Feld oder eine 9×20-Fläche bekommt den Bonus also komplett. Nur Panels, die in
keinem vollen 9×9 liegen (z. B. eine einzelne zehnte Reihe), bleiben bei 1 EU/t.</li>
<li>Für das Quadrat zählen alle Panels, auch abgedeckte. Den Bonus selbst bekommt aber nur ein Panel, das Sonne hat.</li>
<li>Es gibt nur diese eine Stufe, keinen Deckel: Je größer das Feld, desto mehr Strom.</li>
</ul>
{h.table(['Aufbau', 'Panels', 'Leistung (Tag)'], [
    ['einzelnes Panel', '1', '1 EU/t'],
    ['8×8', '64', '64 EU/t'],
    ['<b>9×9</b>', '81', '<b>97,2 EU/t</b>'],
    ['9×9 mit einer Lücke', '80', '80 EU/t'],
    ['10×10', '100', '120 EU/t'],
    ['18×18', '324', '388,8 EU/t'],
])}
<h2 id="sol--bau">Schritt für Schritt: Solarfeld</h2>
<div class="layers">{h.layer(['PPPPPPPPPp', 'PPPPPPPPPp', 'PPPPPPPPPp', 'PPPPPPPPPp', 'PPPPPPPPPp', 'PPPPPPPPPp', 'PPPPPPPPPp', 'PPPPPPPPPp', 'PPPPPPPPPp'], key_field, '9×9 von oben, dazu eine zehnte Spalte', 22)}<div>{h.legend(key_field)}</div></div>
{steps([
    'Eine ebene Fläche mit freiem Himmel suchen. Glas über den Panels stört nicht, Blätter, Wasser und feste Blöcke schon.',
    'Mindestens <b>9×9 = 81</b> {0} lückenlos auf <b>einer Höhe</b> verlegen.'.format(c('solar_panel')),
    '<b>Kabel unter den Panels</b> verlegen (oben würden sie die Sonne verdecken). Jedes Panel gibt seinen Strom selbst ab, also braucht jedes Panel Kontakt zum Kabelnetz, etwa über ein Kabelraster in der Ebene darunter.',
    'Den Bonus erkennt ein Panel spätestens nach 128 Ticks (≈ 6 s) nach dem Bauen.',
])}
{h.note('tip', 'Ein Speicher (BatBox, MFE, MFSU) gehört dazu: Solar liefert nur am Tag. Im Tagesmittel bleibt etwa die Hälfte der Spitzenleistung übrig.')}
<div class="calc" data-calc="solar"><h4>Rechner: Solarfeld</h4><div class="row">
<label>Breite <input type="range" name="w" min="1" max="32" value="9"></label>
<label>Länge <input type="range" name="l" min="1" max="32" value="9"></label></div><div class="out"></div><div class="msg"></div></div>
<h2 id="sol--alt">Alte Solar Arrays (LV/MV/HV)</h2>
<p>Die Compact Solar Arrays gibt es nur noch als Altbestand: Sie haben <b>kein Rezept</b> mehr, <b>liefern nichts</b> und tauchen nicht
mehr in EMI oder im Kreativmenü auf. Bereits gebaute Arrays bleiben erhalten; Batterien im Lade-Slot lassen sich herausnehmen.
In der Werkbank zerfällt jedes Array in <b>8 Teile der Stufe darunter</b>. Der Transformator aus dem alten Rezept geht dabei verloren.</p>
<div class="recipes">
{h.recipe(['A'], {'A': ('craft:lv_solar_array', None)}, ('craft:solar_panel', None), 8, shapeless=True)}
{h.recipe(['A'], {'A': ('craft:mv_solar_array', None)}, ('craft:lv_solar_array', None), 8, shapeless=True)}
{h.recipe(['A'], {'A': ('craft:hv_solar_array', None)}, ('craft:mv_solar_array', None), 8, shapeless=True)}
</div>
<h2 id="sol--fehler">Fehlersuche</h2>
{faq([
    ('Kein Bonus', 'Liegt das Panel in einem vollen 9×9 auf derselben Höhe? Eine einzige Lücke oder ein Panel eine Ebene höher bricht das Quadrat. Nach dem Bauen bis zu 6 Sekunden warten.'),
    ('Ein Panel liefert nichts', 'Nacht, Regen oder kein freier Himmel direkt darüber? Hat es Kontakt zu einem Kabel oder Speicher?'),
    ('Mein altes Solar Array liefert nichts mehr', 'Richtig so: Seit dem Solar-Umbau liefern nur noch Solarpanels. Das Array in der Werkbank zerlegen und die Panels als Feld verlegen.'),
])}
<p>Spezifikation: <a href="#/spec-11">11 – Compact Solars</a> §4, <a href="#/balancing">Energiebalancing</a> §3.1.</p>
'''
    page('solar-arrays', 'Solar', 'Neu & geändert', solar)

    # ================================================================== Wassermühle
    wm_rows = ['fffff', 'fWWWf', 'fWMWf', 'fWWWf', 'fffff']
    wm_key = {'M': ('craft:water_mill', 'Wassermühle', 'ctrl'), 'W': (None, 'Zählbereich des Originals (3×3×3)', 'water'), 'f': (None, 'zusätzlich in Craft (5×5×3)', 'flow')}
    wasser = f'''<h1>Wassermühle</h1>
{h.infobox('Wassermühle', 'craft:water_mill', [
    ('Herkunft', 'IndustrialCraft 2'), ('Status', CHG + ' Balancing'), ('Stufe', 'LV'), ('Leistung', 'bis <b>2 EU/t</b> (Original ≤ 0,26)'),
    ('Puffer', '4 EU'), ('Bereich', '5×5×3 um die Mühle, max. 74 Blöcke'), ('Messung', 'alle 128 Ticks'), ('Rezept', 'unverändert (IC2/GT)'),
])}
<p class="lead">Im Original war die Wassermühle praktisch nutzlos. In Craft ist sie eine kleine, aber zuverlässige Quelle, die <b>Tag und Nacht</b> läuft – ein einzelner Block,
den man einfach ins Wasser stellt.</p>
{h.compare('<ul><li>zählt Wasser im 3×3×3-Würfel (max. 26)</li><li>Ertrag = Blöcke / 100 EU/t, max. 0,26 EU/t</li><li>Eimer: 500 EU, Wasserzelle: 1000 EU</li></ul>',
           '<ul><li>zählt Wasser im <b>5×5×3</b>-Bereich (max. 74)</li><li>Quelle 0,02, <b>fließend 0,04 EU/t</b> je Block</li><li>Fluss ×1,5, Ozean ×1,25</li><li>Deckel 2 EU/t, Nachbarn teilen sich Wasser</li></ul>')}
<div class="tocpos"></div>
<h2 id="wm--aufstellen">Aufstellen und anschließen</h2>
<div class="layers">{h.layer(wm_rows, wm_key, 'Zählbereich von oben (gilt auf 3 Ebenen: eine unter, auf und über der Mühle)', 30)}<div>{h.legend(wm_key)}</div></div>
{steps([
    'Die Mühle in oder an ein Gewässer setzen – am besten so, dass in den 2 Blöcken rundherum, eine Ebene darunter bis eine darüber, möglichst viel Wasser ist.',
    '<b>Fließendes</b> Wasser zählt doppelt so viel wie Quellblöcke (0,04 statt 0,02 EU/t). Fallendes Wasser gilt als fließend – ein Wasserfall direkt an der Mühle ist ideal.',
    f"Kabel an eine beliebige Seite. Die Mühle liefert kleine LV-Pakete – {c('insulated_copper_cable')} und {c('batbox')} passen.",
    'Für mehrere Mühlen: mindestens <b>5 Blöcke Abstand</b>, sonst teilen sie sich das Wasser in den überlappenden Bereichen.',
])}
<h2 id="wm--rechnung">So rechnet sie</h2>
<ul>
<li>Alle 128 Ticks zählt die Mühle die Wasserblöcke im Bereich x −2…+2, y −1…+1, z −2…+2 (ohne sich selbst, max. 74).</li>
<li>Quellblock 0,02 EU/t, fließender Block 0,04 EU/t. Liegt ein Block im Bereich mehrerer Mühlen, wird sein Ertrag durch deren Zahl geteilt.</li>
<li>Biom am Mühlenblock: Fluss ×1,5, Ozean ×1,25, sonst ×1,0. Deckel 2 EU/t.</li>
<li>Beispiel: 10 Quellen + 30 fließende Blöcke = 0,2 + 1,2 = 1,4 EU/t, im Fluss 2,1 → gedeckelt 2 EU/t.</li>
</ul>
<div class="calc" data-calc="watermill"><h4>Rechner: Wassermühle</h4><div class="row">
<label>Quellblöcke <input type="number" name="q" min="0" max="74" value="10"></label>
<label>fließende Blöcke <input type="number" name="f" min="0" max="74" value="30"></label>
<label>Biom <select name="bio"><option value="1">normal</option><option value="1.5">Fluss ×1,5</option><option value="1.25">Ozean ×1,25</option></select></label>
<label>Mühlen, die sich das Wasser teilen <input type="number" name="sh" min="1" max="9" value="1"></label></div><div class="out"></div><div class="msg"></div></div>
<h2 id="wm--behaelter">Betrieb mit Eimern (wie im Original)</h2>
<p>In den Behälter-Slot der GUI kann man weiterhin Wassereimer (500 EU) oder Wasserzellen (1000 EU) legen. Das ist für Craft unwichtig, aber erhalten.
<b>Achtung, Original-Eigenheit:</b> Liegt ein <b>Nicht-Wasser-Gegenstand</b> im Behälter-Slot, läuft der passive Wasserbetrieb nicht – den Slot leer lassen.</p>
<h2 id="wm--fehler">Fehlersuche</h2>
{faq([
    ('Weniger als 2 EU/t', 'Zu wenig Wasser oder nur Quellblöcke. Fließendes Wasser bringt doppelt so viel; Fluss-Biom ×1,5. Nah stehende Mühlen teilen sich Wasser.'),
    ('Gar keine Leistung', 'Liegt etwas im Behälter-Slot, das kein Wasser ist? Dann ist der passive Betrieb blockiert.'),
    ('Der Wert springt erst nach einer Weile', 'Gezählt wird nur alle 128 Ticks.'),
])}
{h.note('tip', 'Mehr Leistung aus Wasser: die <a href="#/wasserturbine">Wasserturbine</a> (bis 32 EU/t).')}
<p>Spezifikation: <a href="#/spec-14">14 – Kraftwerke</a> §6, Original in <a href="#/spec-02">02 – Generatoren</a>.</p>
'''
    page('wassermuehle', 'Wassermühle', 'Neu & geändert', wasser)

    # ================================================================== Windmühle
    wi_side = ['___O___', '_______', '_______', '___M___', '___S___', '___S___', '_______']
    wik = {'M': ('craft:wind_mill', 'Windmühle', 'ctrl'), 'S': ('minecraft:stone', 'Mast (zählt als Hindernis!)', ''),
           'O': (None, 'Prüfbereich: 4 Blöcke über …', 'flow'), '_': (None, 'Prüfbereich 9×7×9 – sollte Luft sein', 'flow')}
    wind = f'''<h1>Windmühle</h1>
{h.infobox('Windmühle', 'craft:wind_mill', [
    ('Herkunft', 'IndustrialCraft 2'), ('Status', CHG + ' Balancing'), ('Stufe', 'LV'), ('Leistung', 'y 128: ≈ 3,2 EU/t<br>y 200: ≈ 6,8 EU/t'),
    ('Gewitter', '×3 (Original ×1,5)'), ('Regen', '×1,5 (Original ×1,2)'), ('Bruch', 'nie (Original ab 5 EU/t)'), ('Puffer / Ausgabe', '16 EU'),
])}
<p class="lead">Die Windmühle braucht <b>Höhe</b> und <b>freie Umgebung</b> und liefert bei Gewitter am meisten. Die Formel des Originals bleibt, nur die
Faktoren sind höher. Wind ist damit die wetterabhängige Quelle mit kräftigen Spitzen – und dem Risiko, dass die Mühle bei zu viel Leistung zerbricht.</p>
{h.compare('<ul><li><code>Wind · (y − 64 − Hindernisse) / 750</code></li><li>Regen ×1,2, Gewitter ×1,5</li><li>Bruch ab 5 EU/t</li><li>y 100, Wind 15: 0,72 EU/t</li></ul>',
           '<ul><li><code>Wind · (y − 64 − Hindernisse) / 750 · <b>2,5</b></code></li><li>Regen <b>×1,5</b>, Gewitter <b>×3</b></li><li><b>zerfällt nie</b></li><li>y 128, Wind 15: 3,2 EU/t</li></ul>')}
<div class="tocpos"></div>
<h2 id="wi--aufstellen">Aufstellen und anschließen</h2>
<div class="layers">{h.layer(wi_side, wik, 'Seitenansicht: Prüfbereich 9 breit, von 2 unter bis 4 über der Mühle', 26)}<div>{h.legend(wik)}</div></div>
{steps([
    '<b>Hoch</b> bauen: Die Leistung wächst mit der Höhe über y 64. Unter etwa y 65 (plus Hindernisse) gibt es gar nichts. Gute Plätze: Berggipfel, ein hoher Turm (y 150–250).',
    '<b>Frei</b> halten: Jeder Block, der nicht Luft ist, im Bereich 4 Blöcke zur Seite, 2 nach unten und 4 nach oben, zieht 1 von der Höhe ab. Dazu zählen auch der eigene Mast, Kabel und andere Mühlen.',
    'Kabel am besten von unten durch den Mast führen – das sind nur 2 Blöcke im Prüfbereich (direkt unter der Mühle).',
    'Mehrere Mühlen mit <b>mindestens 5 Blöcken Abstand</b> stellen, damit sie nicht gegenseitig als Hindernis zählen.',
    f"Die Pakete sind klein (LV): {c('insulated_copper_cable')} zu einer {c('batbox')}.",
])}
<h2 id="wi--rechnung">So rechnet sie</h2>
<ul>
<li><b>Windstärke</b> (ein Wert für alle Dimensionen, verändert nur durch die Oberwelt): startet bei 10–19 und wandert alle 128 Ticks zufällig um ±1, meist zwischen 10 und 20.</li>
<li>Alle 128 Ticks: <code>Leistung = Wind · (y − 64 − Hindernisse) / 750 · 2,5</code>, bei Gewitter ×3, bei Regen ×1,5.</li>
<li>Hindernisse werden nur alle 8 × 128 Ticks neu gezählt – nach dem Umbauen etwas warten.</li>
<li><b>Kein Bruch:</b> Die Mühle geht nie von selbst kaputt – auch bei Gewitter in großer Höhe nicht (im Original zerfiel sie über 5 EU/t zu einem Generator). Eine Überhitzungsanzeige gibt es deshalb nicht.</li>
<li><b>16er-Grenze</b> (Original-Eigenheit, nur von 4 auf 16 angehoben): Würde die Mühle in einem Tick 16 EU erreichen, verfällt die Energie dieses Ticks. Sehr hohe Mühlen liefern bei Gewitter deshalb nicht mehr, sondern eher weniger.</li>
</ul>
<div class="calc" data-calc="wind"><h4>Rechner: Windmühle – Original gegen Craft</h4><div class="row">
<label>Höhe y <input type="number" name="y" min="0" max="320" value="128"></label>
<label>Hindernisse <input type="number" name="ob" min="0" max="566" value="2"></label>
<label>Windstärke <input type="number" name="w" min="0" max="30" value="15"></label>
<label>Wetter <select name="we"><option value="clear">klar</option><option value="rain">Regen</option><option value="thunder">Gewitter</option></select></label>
</div><div class="out"></div><div class="msg"></div></div>
<p style="font-size:12px;color:var(--muted)">„Tatsächlich abgegeben“ ist eine Simulation über 2 000 Ticks unter der Annahme, dass das Netz jede Energie sofort abnimmt.</p>
<h2 id="wi--hoehe">Die richtige Höhe</h2>
<p>Mit Wind 15 und ohne Hindernisse: y 100 ≈ 1,8 · y 128 ≈ 3,2 · y 160 ≈ 4,8 · y 200 ≈ 6,8 · y 250 ≈ 9,3 EU/t. Bei Gewitter dreifach – ab etwa y 155 kann
ein Gewitter die Mühle über 12,5 EU/t treiben und sie riskiert zu zerbrechen. <b>y 130–150</b> ist ein guter Kompromiss.</p>
<h2 id="wi--fehler">Fehlersuche</h2>
{faq([
    ('Die Mühle liefert nichts.', 'Zu niedrig (unter etwa y 65 + Hindernisse) oder zu viele Blöcke im Prüfbereich.'),
    ('Meine Mühle ist ein Generator geworden.', 'Sie ist durch Überlast zerbrochen (über 12,5 EU/t, meist bei Gewitter). Etwas niedriger bauen.'),
    ('Nach dem Aufräumen nicht mehr Leistung.', 'Hindernisse werden nur alle 8 × 128 Ticks (≈ 51 s) neu gezählt.'),
])}
<p>Spezifikation: <a href="#/spec-14">14 – Kraftwerke</a> §7, Original in <a href="#/spec-02">02 – Generatoren</a>.</p>
'''
    page('windmuehle', 'Windmühle', 'Neu & geändert', wind)

    # ================================================================== Mining
    pk = {'S': ('minecraft:smooth_stone', 'Glatter Stein', ''), 'P': ('tex:craft:block/mining_portal', 'Mining-Portal', '')}
    if not h.texture('craft:block/mining_portal'):
        pk['P'] = ('craft:mining_portal', 'Mining-Portal', '')
    mining = f'''<h1>Mining-Dimension</h1>
{h.infobox('Mining-Welt', 'craft:mining_portal', [
    ('Herkunft', NEW + ' Craft (kein Pack-Vorbild)'), ('ID', '<code>craft:mining</code>'), ('Portal', 'Rahmen aus Glattem Stein,<br>mit Feuerzeug zünden'),
    ('Koordinaten', '1:1 zur Oberwelt'), ('Gelände', 'flacher Stein bis y 64'), ('Höhe', 'y −64 … 319'),
    ('Licht', 'hell, immer Mittag'), ('Mobs', 'keine'), ('Erze', 'alle Oberwelt-Erze (Vanilla, IC2, GT)'),
])}
<p class="lead">Eine eigene Dimension nur zum Abbauen: eine <b>flache, massive Steinwelt</b> mit offenem Himmel, ohne Höhlen, ohne Monster und
dauerhaft hell. Man steht oben auf der Ebene (y 65) und baut mit Quarry, Miner oder Turtle nach unten ab.</p>
<div class="tocpos"></div>
<h2 id="mi--portal">Portal bauen und zünden</h2>
<div class="layers">{h.layer(['SSSS', 'SPPS', 'SPPS', 'SPPS', 'SSSS'], pk, 'kleinstes Portal: Rahmen 4×5, Innenraum 2×3', 36)}<div>{h.legend(pk)}</div></div>
{steps([
    f"{v('minecraft:smooth_stone', 'Glatten Stein')} herstellen: Stein (nicht Bruchstein) im Ofen noch einmal schmelzen. Für das kleinste Portal brauchst du 10 Blöcke (ohne Ecken) bzw. 14 (mit Ecken).",
    'Einen <b>senkrechten</b> Rahmen bauen wie beim Nether-Portal: Innenraum mindestens 2 breit und 3 hoch, höchstens 21×21. Die Ecken dürfen fehlen.',
    f"Mit einem {v('minecraft:flint_and_steel', 'Feuerzeug')} (oder einer Feuerkugel) an die Innenseite des Rahmens klicken. Das geht in der Oberwelt und in der Mining-Welt.",
    'Hineinstellen und 4 Sekunden warten (im Kreativmodus sofort).',
])}
<ul>
<li>Ein Obsidian-Rahmen bleibt ein Nether-Portal. Nether-Portale lassen sich in der Mining-Welt nicht zünden.</li>
<li>Das Portal zerfällt, wenn der Rahmen beschädigt wird. Es lässt sich nicht als Item abbauen.</li>
<li>Alle Entities reisen mit: Spieler, Tiere, Items, Loren.</li>
<li>Beim Durchgehen erscheint das lila Nether-Overlay – das ist im Vanilla-Client fest eingebaut.</li>
</ul>
<h2 id="mi--ziel">Wo komme ich raus?</h2>
<ul>
<li>Koordinaten sind <b>1:1</b>: Ein Portal bei x 500 / z 300 führt in der Mining-Welt nach x 500 / z 300.</li>
<li>Gibt es im Umkreis von <b>16 Blöcken</b> schon ein Mining-Portal, wird das nächste genommen. Sonst entsteht ein neues: in der Mining-Welt auf der
Ebene (y 65), notfalls mit einer kleinen Plattform aus Glattem Stein.</li>
<li>Weil der Suchradius nur 16 ist (beim Nether 128), können mehrere Portale in der Oberwelt, die mehr als 16 Blöcke auseinander liegen, zu
unterschiedlichen Zielen führen – praktisch für mehrere Abbaugebiete.</li>
</ul>
<h2 id="mi--welt">Die Welt</h2>
{h.table(['Eigenschaft', 'Wert', 'Was das für dich heißt'], [
    ['Oberfläche', 'massiv bis y 64, darüber Luft', 'komplett flach, auch die Oberfläche ist Stein – ideal zum Aufstellen von Quarrys'],
    ['Untergrund', 'Tiefenschiefer unter y 0 (Übergang y 0–8), Bedrock ab y −64 (bis −60 ausdünnend)', 'wie in der Oberwelt'],
    ['Höhlen, Wasser, Lava', 'keine', 'nichts läuft in deinen Schacht, nichts fällt herunter'],
    ['Füllmaterial', 'keine Erde, kein Kies, Granit, Diorit, Andesit, Tuff, Ton', 'nur Stein, Tiefenschiefer und Erze'],
    ['Licht', 'Umgebungslicht 1,0, feste Tageszeit', 'auch tiefe Schächte sind ohne Fackeln hell'],
    ['Mobs', 'keine Spawns', 'sicheres Arbeiten, keine Monster in dunklen Ecken'],
    ['Betten', 'setzen den Spawnpunkt, schlafen geht nicht', 'Respawn-Anker funktionieren hier nicht'],
    ['Wetter', 'kein Niederschlag', 'Solarpanels laufen immer – es ist immer Tag'],
])}
<h2 id="mi--erze">Erze</h2>
<p>Die Erz-Platzierungen der Oberwelt werden wiederverwendet – Mengen und Höhen sind exakt gleich:</p>
<ul>
<li><b>Vanilla:</b> Kohle, Eisen, Gold, Redstone, Diamant, Lapis, Smaragd (hier überall, nicht nur im Gebirge).</li>
<li><b>IC2:</b> Kupfer (IC2-Häufigkeit), {c('tin_ore')}, {c('uranium_ore') if 'uranium_ore' in h.ITEMS else 'Uran'}.</li>
<li><b>GregTech:</b> Bleiglanz (Galena), Iridium, Rubin, Saphir, Bauxit – Rubin, Saphir und Bauxit überall.</li>
<li>Was in der Oberwelt oberhalb von y 64 läge (obere Kohle, Teile von Eisen, Smaragd, Bauxit), entfällt ohne Ausgleich.</li>
<li><b>Nicht</b> vorhanden: Öl, Gummibäume, Nether-/End-GT-Erze, die großen Erzadern.</li>
</ul>
<h2 id="mi--abbau">Abbauen: Quarry, Miner, Turtle</h2>
<ul>
<li><b>Quarry:</b> mit {c('landmark')} das Feld abstecken, {c('quarry')} daneben, Strom per Motor (MJ). Die flache Oberfläche erspart das Planieren.
Die Quarry hält ihre Chunks selbst geladen (max. 25 Chunks).</li>
<li><b>IC2-Miner:</b> funktioniert ohne Sonderbehandlung; Scanner zeigen die Erze wie gewohnt.</li>
<li><b>Turtles</b> (<a href="#/zusatzmods">CC: Tweaked</a>): graben mit Treibstoff, können über das Item-Inventar direkt in Craft-Maschinen und Kisten abladen. Unzerstörbare Blöcke (z. B. Personal-Safe) bauen sie nicht ab.</li>
<li>Strom dort: Solar läuft rund um die Uhr (es ist immer Tag) – ein Solarfeld neben der Quarry ist eine gute Idee. Windmühlen funktionieren dort auch – die Windstärke gilt überall und ändert sich nur durch die Oberwelt; es gibt dort aber kein Gewitter.</li>
</ul>
<p>Spezifikation: <a href="#/spec-18">18 – Mining-Dimension</a>.</p>
'''
    page('mining-dimension', 'Mining-Dimension', 'Neu & geändert', mining)

    return P
