"""Hand-written wiki pages. Every value comes from Minecraft-Mod/docs (spec, balancing, abweichungen)."""


def pages(h, stats):
    c = h.chip
    v = h.vchip
    P = []

    def page(pid, title, group, body, hidden=False):
        P.append({'id': pid, 'title': title, 'group': group, 'html': body, 'hidden': hidden})

    groups = {}
    for it in h.ITEMS.values():
        groups[it['group']] = groups.get(it['group'], 0) + 1

    # ------------------------------------------------------------------ Hauptseite
    def card(href, ref, title, text, badge=''):
        src = h.icon(ref) if ref else None
        img = f'<img src="{src}" alt="">' if src else ''
        return f'<a class="card" href="#/{href}"><h4>{img}{title} {badge}</h4><p>{text}</p></a>'

    NEW = '<span class="badge new">neu</span>'
    CHG = '<span class="badge chg">geändert</span>'
    GONE = '<span class="badge gone">entfällt</span>'
    TECH = '<span class="badge tech">1.21</span>'

    home = f'''
<div class="hero"><img src="{h.icon('craft:macerator')}" alt=""><div><h1>Willkommen im Craft-Wiki</h1>
<p>Das Nachschlagewerk zu <b>Craft</b> – dem Nachbau der Technik-Mods aus <b>FTB Ultimate (Minecraft 1.4.7)</b> als ein einziger
Mod für <b>Minecraft 1.21.1 / NeoForge</b>. Mit allen Änderungen gegenüber dem Original, Bauanleitungen, Rechnern und der
kompletten technischen Spezifikation.</p></div></div>

{h.infobox('Craft', 'craft:mfsu', [
    ('Version', '0.3.0'), ('Minecraft', '1.21.1'), ('Mod-Loader', 'NeoForge 21.1.259'), ('Java', '21'),
    ('Mod-ID', '<code>craft</code>'),
    ('Vorlage', 'FTB Ultimate 1.1.2<br>(MC 1.4.7, Forge 6.6.2)'),
    ('Mitgeliefert', 'EMI 1.1.22<br>Xaero\'s Minimap 26.5.0<br>CC: Tweaked 1.120.2'),
    ('Empf. RAM', '6 GB'),
    ('Blöcke & Items', str(stats['items'])),
    ('Quellcode', f"{stats['java']} Java-Klassen<br>{stats['lines']:,} Zeilen".replace(',', '.')),
    ('Commits', f"{stats['commits']} (zuletzt {h.esc(stats['last'])})"),
])}

<p class="lead">Craft baut IndustrialCraft 2, die nötigen Teile von GregTech, BuildCraft (Quarry, Engines, Öl, Rohre), Forestrys
Electrical Engine, Power Converters und Compact Solars <b>1:1</b> nach – mit denselben Rezepten, Energiewerten, Zeiten,
Explosionen und sogar den Eigenheiten des Originals. Bewusst geändert wurde nur das <a href="#/energie">Energiebalancing</a>;
dazu kommen <a href="#/aenderungen">neue Kraftwerke und eine Mining-Dimension</a>.</p>

<h2 id="home--start">Schnellstart</h2>
<div class="cards">
{card('installation', 'craft:wrench', 'Installation & Tasten', 'Modpack importieren, mitgelieferte Mods, Tastenbelegung.')}
{card('aenderungen', 'craft:advanced_circuit', 'Alle Änderungen zum Original', 'Was ist neu, was ist anders, was fehlt – mit Gegenüberstellung.')}
{card('items', 'craft:iridium_plate', 'Gegenstände & Blöcke', f"Alle {stats['items']} Einträge mit Bild, deutschem und englischem Namen.")}
{card('fortschritt', 'craft:macerator', 'Spielfortschritt', 'Vom ersten Generator bis zum Reaktor – die typische Reihenfolge.')}
</div>

<h2 id="home--anl">Anleitungen</h2>
<div class="cards">
{card('rohstoffe', 'craft:tin_ore', 'Rohstoffe & Erze', 'Wo jedes Erz liegt und woher Nickel, Zink, Titan & Co. kommen.')}
{card('energie-steckbrief', 'craft:mfsu', 'Energie-Steckbrief', 'Spannung, Verbrauch, Leistung und Speicher jedes Geräts – und was bei Überspannung passiert.')}
{card('strom', 'craft:insulated_copper_cable', 'Strom anschließen', 'Spannung, Kabel, Speicher, Trafos – und wie man nichts sprengt.')}
{card('ic2-maschinen', 'craft:overclocker_upgrade', 'IC2-Maschinen & Upgrades', 'Seiten, Automatisierung, Overclocker-Rechnung.')}
{card('gregtech-maschinen', 'craft:industrial_blast_furnace', 'GregTech & Hochofen', 'Nur mit Schraubenschlüssel abbauen! Hochofen-Bau und Hitze.')}
{card('quarry', 'craft:quarry', 'Quarry & BuildCraft', 'Motoren, Landmarken, Öl, Raffinerie, Rohre.')}
{card('umwandlung', 'craft:energy_bridge', 'EU ↔ MJ', 'Electrical Engine und Power Converters.')}
{card('reaktor', 'craft:nuclear_reactor', 'Kernreaktor', 'Formeln, Hitzestufen und zwei sichere Designs.')}
</div>

<h2 id="home--neu">Neu und geändert</h2>
<div class="cards">
{card('solar-arrays', 'craft:solar_panel', 'Solar', 'Nur noch Solarpanels: jedes liefert 1 EU/t, im 9×9-Feld 1,2 EU/t.', CHG)}
{card('wassermuehle', 'craft:water_mill', 'Wassermühle', 'Bis 2 EU/t aus fließendem Wasser statt 0,26 EU/t.', CHG)}
{card('windmuehle', 'craft:wind_mill', 'Windmühle', 'Faktor 2,5, Gewitter ×3, geht nie kaputt.', CHG)}
{card('koksofen', 'craft:coke_oven', 'Koksofen', '3×3×3-Multiblock: Kohle → Koks ohne Strom.', NEW)}
{card('kohlekraftwerk', 'craft:boiler_firebox', 'Kohlekraftwerk', 'Kessel + Dampfturbine: 16 000 EU/Kohle, 32 000 EU/Koks.', NEW)}
{card('wasserturbine', 'craft:water_turbine', 'Wasserturbine', 'Bis 32 EU/t Grundlast aus einem Fluss.', NEW)}
{card('grosskraftwerke', 'craft:large_steam_turbine', 'Großkraftwerke', 'Hochdruckkessel, große Dampf-, Wasser- und Windturbine: 256–512 EU/t.', NEW)}
{card('mining-dimension', 'craft:mining_portal', 'Mining-Dimension', 'Flache Steinwelt voller Erze, Portal aus Glattem Stein.', NEW)}
{card('leuchtschild', 'craft:electric_light_shield', 'Elektrischer Leuchtschild', 'Schild mit EU statt Haltbarkeit, leuchtet wie eine Taschenlampe.', NEW)}
{card('zusatzmods', 'craft:cropnalyzer', 'EMI, Xaero & CC: Tweaked', 'Rezeptanzeige, Minimap und Turtles im Paket.', TECH)}
</div>

<h2 id="home--mods">Die Ursprungs-Mods</h2>
<div class="cards">
{card('mod-ic2', 'craft:generator', 'IndustrialCraft 2', f"EU-Netz, Kabel, Maschinen, Werkzeuge, Rüstung, Reaktor, Crops · {groups.get('IndustrialCraft 2', 0)} Einträge")}
{card('mod-gregtech', 'craft:industrial_blast_furnace', 'GregTech', f"Maschinen, Materialien, Erze für die Pack-Rezepte · {groups.get('GregTech', 0)} Einträge")}
{card('mod-buildcraft', 'craft:quarry', 'BuildCraft', f"MJ, Engines, Quarry, Öl, Raffinerie, Rohre · {groups.get('BuildCraft', 0)} Einträge")}
{card('mod-forestry', 'craft:electrical_engine', 'Forestry', f"Electrical Engine, Röhren, Platinen, Fabricator, Carpenter · {groups.get('Forestry', 0)} Einträge")}
{card('mod-powerconverters', 'craft:energy_bridge', 'Power Converters', f"MJ ↔ EU über die Energy Bridge · {groups.get('Power Converters', 0)} Einträge")}
{card('solar-arrays', 'craft:hv_solar_array', 'Compact Solars', f"Solar Arrays (stillgelegt, zerlegbar) · {groups.get('Compact Solars', 0)} Einträge")}
</div>

<h2 id="home--prinzip">Das Grundprinzip in drei Sätzen</h2>
<ol>
<li><b>1:1 wie im Pack.</b> Rezepte, EU-/MJ-Werte, Zeiten, Kapazitäten, Spannungsstufen, Kabelverluste, Explosionen und
Reaktorlogik entsprechen FTB Ultimate – auch dort, wo das Original seltsam ist.</li>
<li><b>Einzige bewusste Ausnahme: Energiebalancing.</b> Solar wird flächenbezogen, Wasser und Wind werden brauchbar, Kohle
bekommt ein Kraftwerk. Alles wurde vorher vorgeschlagen und freigegeben.</li>
<li><b>Wo Minecraft 1.21 es erzwingt</b> (keine Metadaten, Rohmetall, Tiefenschiefer, neue Welthöhe …), wurde die
nächstliegende Lösung gewählt und dokumentiert.</li>
</ol>
{h.note('info', 'Dieses Wiki ist eine einzelne HTML-Datei und funktioniert offline. Oben rechts kannst du zwischen hellem und dunklem Design wechseln; die Suche findet Seiten, Begriffe und Gegenstände.')}
<div class="clear"></div>
'''
    page('home', 'Hauptseite', 'Allgemein', home)

    # ------------------------------------------------------------------ Installation
    inst = f'''<h1>Installation & Tasten</h1>
<p class="lead">Craft wird als CurseForge-Modpack-Datei (<code>Craft-0.3.0-curseforge.zip</code>) weitergegeben. Darin stecken der
Mod selbst und die drei mitgelieferten Mods.</p>
<h2 id="inst--curseforge">Mit der CurseForge-App (empfohlen)</h2>
<ol>
<li>CurseForge-App öffnen → <b>Minecraft</b> → <b>Erstellen</b> (bzw. „Create Custom Profile“) → <b>Importieren</b>.</li>
<li><code>Craft-0.3.0-curseforge.zip</code> auswählen. Minecraft 1.21.1 und NeoForge 21.1.259 werden automatisch eingerichtet.</li>
<li>Profil starten. Empfohlen sind <b>6 GB Arbeitsspeicher</b> (steht so im Paket-Manifest).</li>
</ol>
<h2 id="inst--manuell">Manuell (beliebiger Launcher)</h2>
<ol>
<li>NeoForge <b>21.1.259</b> für Minecraft <b>1.21.1</b> installieren (Java 21).</li>
<li>Die Jars aus dem Ordner <code>overrides/mods/</code> der Zip in den <code>mods</code>-Ordner der Instanz kopieren.</li>
</ol>
{h.table(['Datei', 'Mod', 'Zweck'], [
    ['<code>craft-0.3.0.jar</code>', 'Craft', 'der Mod selbst'],
    ['<code>emi-neoforge-1.1.22+1.21.1.jar</code>', 'EMI', 'Rezeptanzeige mit eigenen Kategorien für alle Craft-Maschinen'],
    ['<code>xaeros-minimap-neoforge-1.21.1-26.5.0.jar</code>', "Xaero's Minimap", 'Minimap (nur mitinstalliert)'],
    ['<code>cc-tweaked-1.21.1-forge-1.120.2.jar</code>', 'CC: Tweaked', 'Computer und Turtles (trotz „forge“ im Namen der NeoForge-Build)'],
])}
{h.note('tip', 'EMI ist optional – Craft startet auch ohne. Ohne EMI gibt es aber keine Rezeptanzeige im Spiel.')}
<h2 id="inst--tasten">Tastenbelegung</h2>
<p>Die IC2-Tasten lassen sich unter <i>Optionen → Steuerung → IndustrialCraft 2</i> ändern.</p>
{h.table(['Taste (Standard)', 'Name', 'Wofür'], [
    ['<b>M</b>', 'Modus-Taste', 'Modus wechseln: Bergbaulaser, el. Schraubenschlüssel, Farbroller, Jetpack, Nanosäbel …'],
    ['<b>linke Strg</b>', 'Boost-Taste', 'Quantenrüstung: Sprint-/Sprungverstärkung'],
    ['<b>linke Alt</b>', 'ALT-Taste', 'Schraubenschlüssel: Drehrichtung umkehren u. a.'],
    ['<b>C</b>', 'Seiteninventar-Taste', 'Seiteninventar (wie im Original)'],
    ['<b>R</b> / <b>U</b>', 'EMI', 'über einem Gegenstand: Rezept / Verwendungen anzeigen'],
])}
<p>Vorwärts, Springen und Schleichen sind die normalen Minecraft-Tasten; Jetpack, Quantenstiefel und Gerüst werten sie aus.</p>
<h2 id="inst--tabs">Kreativ-Tabs</h2>
<p>Jeder Ursprungs-Mod hat einen eigenen Tab: <i>IndustrialCraft 2, GregTech, BuildCraft, Forestry, Power Converters,
Compact Solars</i> und <i>Craft: Kraftwerke</i>.</p>
'''
    page('installation', 'Installation & Tasten', 'Allgemein', inst)

    # ------------------------------------------------------------------ Änderungen
    def row(name, orig, new, link, badge):
        return [f'{badge} <a href="#/{link}">{name}</a>' if link else f'{badge} {name}', orig, new]

    chg = f'''<h1>Alle Änderungen zum Original</h1>
<p class="lead">Diese Seite fasst <b>alles</b> zusammen, was sich gegenüber FTB Ultimate (Minecraft 1.4.7) unterscheidet – geordnet
nach Art der Änderung. Jede Zeile verlinkt auf eine ausführliche Seite; die vollständige, technische Liste steht unter
<a href="#/abweichungen">Alle Abweichungen (Tabelle)</a>.</p>
<p>Legende: {NEW} komplett neuer Inhalt · {CHG} bewusst geändert (Balancing) · {GONE} weggelassen · {TECH} durch Minecraft 1.21 bedingt</p>
<div class="tocpos"></div>

<h2 id="chg--balancing">1. Bewusst geändert: Energiebalancing</h2>
<p>Ausgangslage im Original: Ein HV Solar Array lieferte 512 EU/t auf <b>einem</b> Block ohne Wartung und machte jede andere Quelle
überflüssig, Wasser- und Windmühle lieferten unter 2 EU/t, Kohle hatte nur den Generator. Leitlinie der Änderung:
<b>Rezepte und Kosten bleiben, gebalanct wird über Leistung, Effizienz und Platzbedarf.</b>
Details und Begründung: <a href="#/energie">Energiequellen im Vergleich</a>.</p>
{h.table(['Thema', 'Original', 'Craft'], [
    row('Solar', 'Solarpanel 1 EU/t; Compact Solar Arrays: ein Block je Stufe 8 / 64 / 512 EU/t', 'nur noch Solarpanels: 1 EU/t, im lückenlosen 9×9-Feld 1,2 EU/t; Arrays ohne Rezept und Leistung, in der Werkbank zerlegbar', 'solar-arrays', CHG),
    row('Wassermühle', '3×3×3 Wasserblöcke zählen, max. 0,26 EU/t', '5×5×3-Bereich, fließendes Wasser 0,04 EU/t je Block, Biom-Bonus, bis 2 EU/t', 'wassermuehle', CHG),
    row('Windmühle', 'Faktor 1, Regen ×1,2, Gewitter ×1,5, Bruch ab 5 EU/t, Puffer 4', 'Faktor 2,5, Regen ×1,5, Gewitter ×3, kein Zerfall, Puffer 16', 'windmuehle', CHG),
    row('Koks', '– (im Pack nur über Railcraft, das nicht nachgebaut wird)', 'neues Item: im Generator wie Kohle, im Kohlekraftwerk doppelt so viel', 'koksofen', NEW),
])}
<p>Unverändert 1:1 bleiben: IC2-Generator, Geothermie, alle BuildCraft-Engines, Öl und Treibstoff, Electrical
Engine, Power Converters und der Kernreaktor.</p>

<h2 id="chg--neu">2. Komplett neue Inhalte (kein Vorbild im Pack)</h2>
{h.table(['Inhalt', 'Was es ist', 'Kurz'], [
    row('Koksofen', 'Multiblock 3×3×3 aus Koksofenziegeln', '1 Kohle → 1 Koks in 900 Ticks, ohne Strom', 'koksofen', NEW),
    row('Kohlekraftwerk', 'Kessel (3×3×3) + Dampfturbine (3×3×5)', 'Kohle/Koks → Dampf → EU; 1 mB Dampf = 1 EU; bis 64 EU/t', 'kohlekraftwerk', NEW),
    row('Wasserturbine', 'Multiblock 3×3×3 in fließendem Wasser', '0,25 EU/t je fließendem Wasserblock bis 5 Blöcke um und 20 über der Turbine, bis 32 EU/t', 'wasserturbine', NEW),
    row('Großkraftwerke', 'zweite Ausbaustufe für Kohle, Wasser und Wind', 'Hochdruckkessel + Große Dampfturbine bis 512 EU/t, Große Wasserturbine bis 256 EU/t, Große Windturbine bis 384 EU/t', 'grosskraftwerke', NEW),
    row('Mining-Dimension', 'eigene Dimension, Portal aus Glattem Stein', 'flache Steinwelt bis y 64 mit allen Oberwelt-Erzen, hell, ohne Mobs', 'mining-dimension', NEW),
    row('Dampf', 'Flüssigkeit <code>craft:steam</code>', 'nur in Tanks und Rohren, kein Eimer', 'kohlekraftwerk', NEW),
    row('CC: Tweaked', 'mitinstallierter Mod', 'Computer und Turtles (Ersatz für ComputerCraft 1.5 im Pack)', 'zusatzmods', NEW),
])}

<h2 id="chg--weg">3. Weggelassen oder eingeschränkt</h2>
{h.table(['Inhalt', 'Original', 'Craft'], [
    row('Bronze-Werkzeuge und -Rüstung', 'IC2-Items', 'nicht umgesetzt (Entscheidung vom 2026-10-15)', '', GONE),
    row('Solar-Hüte (LV/MV/HV)', 'Compact Solars', 'entfallen (Nutzer, 2026-10-17); der IC2-Solarhelm existiert', 'solar-arrays', GONE),
    row('BuildCraft-Gates, Drähte, Fassaden', 'BuildCraft 3.4.3', 'nur die Rohre selbst (Item-, Flüssigkeits-, Leistungs-, Strukturrohre)', 'spec-15', GONE),
    row('GregTech', 'kompletter Mod', 'nur Maschinen und Materialien, die für die Pack-Rezepte nötig sind; dafür <b>alle</b> GT-Erze und alle damit erreichbaren Rezepte', 'mod-gregtech', GONE),
    row('Massefabrikator', 'im Pack durch GTs Matter Fabricator ersetzt', 'IC2-Massefabrikator 1:1 mit IC2-Rezept', 'spec-04', CHG),
    row('NEI (Rezeptanzeige)', 'NotEnoughItems', 'EMI mit eigener Integration (NEI gibt es für 1.21 nicht)', 'zusatzmods', TECH),
    row('Minimap', '–', "Xaero's Minimap, nur mitinstalliert (Nutzerwunsch)", 'zusatzmods', NEW),
])}

<h2 id="chg--technisch">4. Durch Minecraft 1.21 bedingt – was man beim Spielen merkt</h2>
<p>Die meisten technischen Abweichungen sind unsichtbar (Data Components statt NBT, Capabilities statt Interfaces …).
Diese hier bemerkt man im Spiel:</p>
{h.table(['Thema', 'Original (1.4.7)', 'Craft (1.21.1)'], [
    row('Kupfer', 'eigenes IC2-Kupfererz', 'Vanilla-Kupfer, aber mit IC2-Häufigkeit (15 Adern/Chunk, Größe 10); die großen Vanilla-Kupferadern sind entfernt', 'spec-07', TECH),
    row('Rohmetall', 'Erz droppt sich selbst; Macerator: Erz → 2 Staub', 'Eisen-/Gold-/Kupfererz droppen Rohmetall (Kupfer nur 1 statt 2–5); Macerator: Rohmetall → 2 Staub, Erzblock (Behutsamkeit) → 2 Staub; Zinnerz droppt sich selbst', 'spec-07', TECH),
    row('Tiefenschiefer', 'gibt es nicht', 'Tiefenschiefer-Varianten (z. B. Tiefenschiefer-Zinnerz, GT-Erze) mit denselben Regeln wie das Grunderz', 'spec-07', TECH),
    row('Welthöhe / Erzhöhen', '0–255', 'Erzhöhen absolut übernommen (z. B. Zinn y 0–39, Uran y 0–64), keine IC2-Erze unter y 0', 'spec-07', TECH),
    row('Erzwörterbuch', 'OreDictionary (<code>ingotCopper</code>)', 'Tags (<code>c:ingots/copper</code>) – andere Mods mit denselben Tags passen', '', TECH),
    row('IC2-Explosionsresistenz', 'Obsidian & Co. global auf 36 gesetzt', 'gilt nur bei IC2-Explosionen (Reaktor, Nuke, Industrie-TNT); Vanilla-TNT/Creeper unverändert', 'spec-01', TECH),
    row('RE-Batterie', 'zwei Items (leer/geladen)', 'ein Item: leer stapelbar bis 16, geladen Stapel 1', 'spec-05', TECH),
    row('Rüstung', 'Forge <code>ISpecialArmor</code>', 'über das Schadens-Event nachgebaut, gleiche Absorption und EU-Kosten', 'spec-05', TECH),
    row('GT-Recycling von Türen', 'Eisentür → 6 Eisenstaub', 'Eisen-/Holztür → 2 (in 1.21 ergeben 6 Barren 3 Türen – sonst Vermehrung)', 'abweichungen', TECH),
    row('Fass, Dynamit-Block', 'versteckte Block-Items', 'kein Block-Item; Pick-Block gibt das leere Fass bzw. Dynamit', 'abweichungen', TECH),
    row('Holzsorten', '4 Sorten', 'alle 1.21-Holzarten (Bambus, Kirsche …) zählen in GT-Rezepten (Holzschliff, Compressor)', 'abweichungen', TECH),
    row('Kupfer-/Gummibaum-Weltgen', 'Biom-Namen', 'Biom-Tags (Wald, Taiga, Dschungel, Sumpf)', 'spec-07', TECH),
    row('Mining-Portal', '–', 'beim Durchgehen erscheint das lila Nether-Portal-Overlay (fest im Vanilla-Client)', 'mining-dimension', TECH),
    row('Nachtsichtbrille', 'lila Platzhalter-Icon', 'eigenes Icon', '', TECH),
])}
{h.note('info', 'Die vollständige Liste mit allen rund 240 Einträgen (Format: Thema – Original – Neu – Grund – Status) findest du unter <a href="#/abweichungen">Alle Abweichungen (Tabelle)</a>.')}
'''
    page('aenderungen', 'Alle Änderungen zum Original', 'Änderungen', chg)

    # ------------------------------------------------------------------ Energievergleich
    bars = [
        ('IC2-Solarpanel', 1, '1 EU/t bei Sonne, unverändert'),
        ('Wassermühle (neu)', 2, 'bis 2 EU/t, Tag und Nacht'),
        ('Windmühle (neu, y 128)', 3.2, 'Gewitter bis ≈ 9,6 EU/t'),
        ('IC2-Generator', 10, '4 000 EU je Kohle'),
        ('Kessel + Dampfturbine', 20, 'je Kessel; bis 64 mit 3 Kesseln'),
        ('Geothermiegenerator', 20, '20 000 EU je Lava-Eimer'),
        ('Wasserturbine', 32, 'Deckel, braucht Fluss'),
        ('Große Windturbine (y 200)', 136, 'Carbon-Rotor; Gewitter bis 384'),
        ('Solarfeld 9×9', 97.2, '81 Solarpanels × 1,2, nur am Tag'),
        ('Große Wasserturbine', 256, 'Deckel, ab 256 fließenden Blöcken'),
        ('Hochdruckkraftwerk', 480, '3 Hochdruckkessel + Große Dampfturbine'),
        ('Kernreaktor (typisch)', 400, '60–400+ EU/t je nach Design'),
    ]
    mx = max(b[1] for b in bars)
    bar_html = ''.join(
        f'<div class="brow" data-tip="{h.esc(t)}"><span class="bl">{h.esc(n)}</span><span class="bt"><span class="bf" style="display:block;width:{v_ / mx * 100:.2f}%"></span></span>'
        f'<span class="bv">{str(v_).replace(".", ",")} EU/t</span></div>' for n, v_, t in bars)
    energie = f'''<h1>Energiequellen im Vergleich</h1>
<p class="lead">Das Ziel des Balancings: <b>Solar ist wartungsfrei, braucht aber viel Fläche und liefert nur am Tag, Wasser die zuverlässige Mittelklasse, Wind
wetterabhängig mit Gewitterspitzen, Kohle hat einen Ausbaupfad, Öl ist die lagerbare Hochenergie, Atom die Spitze.</b></p>
<h2 id="en--leistung">Typische Leistung je Anlage (Craft)</h2>
<div class="bars" role="img" aria-label="Typische Leistung je Anlage in EU pro Tick">{bar_html}</div>
<p style="font-size:12px;color:var(--muted)">Spitzenleistung; Solar liefert im Tagesmittel nur etwa die Hälfte (nur bei Tag, kein Regen).
Solar wächst nur mit der Fläche: rund 1,2 EU/t je Block im 9×9-Feld.</p>
<h2 id="en--tabelle">Vergleichstabelle</h2>
{h.table(['Quelle', 'Leistung typ.', 'EU/t pro Block Fläche', 'EU pro Ressource', 'Zuverlässigkeit'], [
    ['Solarpanel', '1 (Tag)', '1,0 Spitze / 0,5 Mittel', '– (wartungsfrei)', 'niedrig (nur Tag, kein Regen)'],
    [f'Solarfeld 9×9 {CHG}', '97 (Tag, 81 Panels)', '1,2 Spitze / 0,6 Mittel', '– (wartungsfrei)', 'niedrig (nur Tag, kein Regen)'],
    [f'Wassermühle {CHG}', 'bis 2', '≈ 0,08 (inkl. Wasserfläche)', '–', '<b>hoch</b> (Tag & Nacht)'],
    [f'Wasserturbine {NEW}', '16–32', '≈ 0,5–0,65 (inkl. Wasserring)', '–', '<b>hoch</b>'],
    [f'Windmühle {CHG}', '3–7, Gewitter bis 20', '≈ 0,04–0,25 (Freiraum 9×9)', '–', 'mittel (Wetter, Wind)'],
    ['IC2-Generator', '10', '10', '4 000 / Kohle', 'hoch, solange befüllt'],
    [f'Kohlekraftwerk {NEW}', '20 je Kessel (bis 64)', '≈ 0,7–1,5', '16 000 / Kohle, 32 000 / Koks', 'hoch (Aufheizen, Asche)'],
    ['Geothermie', '20', '20', '20 000 / Lava', 'hoch, solange befüllt'],
    ['Combustion Engine + Konverter (Öl)', '7,3 je Engine', '≈ 3', '145 800 / Öl-Eimer', 'hoch, lagerbar'],
    ['Combustion Engine + Konverter (Treibstoff)', '14,6 je Engine', '≈ 6', '1 458 000 / Eimer', 'hoch (mit Raffinerie)'],
    ['Stirling Engine + Konverter', '2,4', '≈ 1', '3 888 / Kohle', 'mittel (Explosionsgefahr)'],
    ['Kernreaktor', '60–400+', '10–50+', '≈ 1–12 Mio. / Zelle', 'hoch bei korrekter Kühlung, Risiko'],
    [f'Hochdruckkessel + Große Dampfturbine {NEW}', '160 je Kessel (bis 480)', '≈ 2,3–4', '24 000 / Kohle, 48 000 / Koks', 'hoch (Aufheizen, viel Wasser)'],
    [f'Große Wasserturbine {NEW}', '64–256', '≈ 0,2–0,7 (inkl. Wasserfläche)', '–', '<b>hoch</b>'],
    [f'Große Windturbine {NEW}', '64–136, Gewitter bis 384', '≈ 0,5–1,1 (Freiraum 11×11)', '–', 'mittel (Wetter, Wind)'],
])}
<h2 id="en--umrechnung">Umrechnung MJ ↔ EU</h2>
<ul>
<li><b>MJ → EU</b> über Power Converters: <b>1 MJ = 2,43 EU</b> (4375/1800 PCU), Producer geben nur volle Pakete ab.</li>
<li><b>EU → MJ</b> über die Forestry Electrical Engine: <b>3 EU = 1 MJ</b> (6 EU → 2 MJ, mit Upgrades bis 36 EU → 10 MJ).</li>
<li>Eine Rundreise EU → MJ → EU verliert ≈ 19 %.</li>
</ul>
<p>Herleitung, Originalwerte und der „Problembefund“ stehen im Volltext: <a href="#/balancing">Energiebalancing (Volltext)</a>.</p>
'''
    page('energie', 'Energiequellen im Vergleich', 'Änderungen', energie)

    # ------------------------------------------------------------------ Solar arrays
    # The page body lives in content_guides.py (it replaces this entry, which only keeps the position).
    page('solar-arrays', 'Solar Arrays', 'Neu & geändert', '')

    # ------------------------------------------------------------------ Wassermühle
    wm_rows = ['fffff', 'fWWWf', 'fWMWf', 'fWWWf', 'fffff']
    wm_key = {'M': ('craft:water_mill', 'Wassermühle', 'ctrl'), 'W': (None, 'Zählbereich des Originals (3×3×3)', 'water'),
              'f': (None, 'zusätzlich in Craft (5×5×3)', 'flow')}
    wasser = f'''<h1>Wassermühle</h1>
{h.infobox('Wassermühle', 'craft:water_mill', [
    ('Herkunft', 'IndustrialCraft 2'), ('Status', CHG + ' Balancing'), ('Stufe', 'LV'),
    ('Leistung', 'bis <b>2 EU/t</b> (Original ≤ 0,26)'), ('Puffer', '4 EU (Original 2)'),
    ('Bereich', '5×5×3 um die Mühle, max. 74 Blöcke'), ('Rezept', 'unverändert'),
])}
<p class="lead">Im Original war die Wassermühle praktisch nutzlos (höchstens 0,26 EU/t passiv). In Craft ist sie eine kleine,
aber zuverlässige Quelle, die <b>Tag und Nacht</b> läuft.</p>
{h.compare('<ul><li>zählt Wasser im 3×3×3-Würfel (max. 26)</li><li>Ertrag = Blöcke / 100 EU/t, max. 0,26 EU/t</li><li>Eimer: 500 EU, Wasserzelle: 1000 EU</li></ul>',
           '<ul><li>zählt Wasser im <b>5×5×3</b>-Bereich (max. 74)</li><li>Quelle 0,02, <b>fließend 0,04 EU/t</b> je Block</li><li>Fluss ×1,5, Ozean ×1,25</li><li>Deckel 2 EU/t, Nachbarn teilen sich Wasser</li></ul>')}

<h2 id="wm--funktion">So funktioniert es</h2>
<div class="layers">{h.layer(wm_rows, wm_key, 'Zählbereich von oben (3 Ebenen: y −1 … +1)', 30)}<div>{h.legend(wm_key)}</div></div>
<ol>
<li>Alle 128 Ticks zählt die Mühle die Wasserblöcke im Bereich x −2…+2, y −1…+1, z −2…+2 (ohne sich selbst, max. 74).</li>
<li>Jeder <b>Quellblock</b> bringt 0,02 EU/t, jeder <b>fließende</b> Block 0,04 EU/t.</li>
<li>Der Ertrag eines Blocks wird durch die Zahl der Mühlen geteilt, in deren Bereich er liegt – Mühlen mit weniger als
5 Blöcken Abstand teilen sich also das Wasser.</li>
<li>Biom-Faktor am Mühlenblock: Fluss ×1,5, Ozean ×1,25, sonst ×1,0.</li>
<li>Deckel <b>2 EU/t</b>. Bruchteile werden wie im Original über einen Mikrospeicher (1/100 EU) gesammelt.</li>
</ol>
<p>Unverändert: der Betrieb mit Wassereimern/-zellen, die Slots, der Drop und eine Original-Eigenheit – liegt ein
<b>Nicht-Wasser-Item</b> im Behälter-Slot, läuft der passive Betrieb nicht.</p>
<div class="calc" data-calc="watermill"><h4>Rechner: Wassermühle</h4><div class="row">
<label>Quellblöcke <input type="number" name="q" min="0" max="74" value="10"></label>
<label>fließende Blöcke <input type="number" name="f" min="0" max="74" value="30"></label>
<label>Biom <select name="bio"><option value="1">normal</option><option value="1.5">Fluss ×1,5</option><option value="1.25">Ozean ×1,25</option></select></label>
<label>Mühlen, die sich das Wasser teilen <input type="number" name="sh" min="1" max="9" value="1"></label></div><div class="out"></div><div class="msg"></div></div>
{h.note('tip', 'Eine Mühle mitten in einem Fluss mit viel fließendem Wasser erreicht die 2 EU/t leicht – das ist so viel wie vier Solarpanels im Tagesmittel. Für mehr Leistung: <a href="#/wasserturbine">Wasserturbine</a>.')}
<p>Spezifikation: <a href="#/spec-14">14 – Kraftwerke</a> §6, Original in <a href="#/spec-02">02 – Generatoren</a>.</p>
'''
    page('wassermuehle', 'Wassermühle', 'Neu & geändert', wasser)

    # ------------------------------------------------------------------ Windmühle
    wind = f'''<h1>Windmühle</h1>
{h.infobox('Windmühle', 'craft:wind_mill', [
    ('Herkunft', 'IndustrialCraft 2'), ('Status', CHG + ' Balancing'), ('Stufe', 'LV'),
    ('Leistung', 'y 128: ≈ 3,2 EU/t<br>y 200: ≈ 6,8 EU/t'), ('Gewitter', '×3 (Original ×1,5)'),
    ('Bruch', 'nie (Original ab 5 EU/t)'), ('Puffer / Ausgabe', '16 / 16 EU (Original 4 / 10)'), ('Rezept', 'unverändert'),
])}
<p class="lead">Die Formel des Originals bleibt erhalten – Höhe, freie Umgebung und Wetter entscheiden –, nur die Faktoren wurden
erhöht. Wind ist damit die <b>wetterabhängige</b> Quelle mit kräftigen Gewitterspitzen.</p>
{h.compare('<ul><li><code>sub = Wind · (y − 64 − Hindernisse) / 750</code></li><li>Regen ×1,2, Gewitter ×1,5</li><li>Bruch ab 5 EU/t</li><li>y 100, Wind 15: 0,72 EU/t</li></ul>',
           '<ul><li><code>sub = Wind · (y − 64 − Hindernisse) / 750 · <b>2,5</b></code></li><li>Regen <b>×1,5</b>, Gewitter <b>×3</b></li><li><b>zerfällt nie</b></li><li>y 128, Wind 15: 3,2 EU/t</li></ul>')}

<h2 id="wi--funktion">So funktioniert es</h2>
<ul>
<li><b>Globale Windstärke</b> (nur Oberwelt): startet bei 10–19 und wandert alle 128 Ticks zufällig um ±1, mit Tendenz in den Bereich 10–20.</li>
<li><b>Hindernisse</b>: alle 8 × 128 Ticks zählt jede Mühle die Nicht-Luft-Blöcke im Bereich x −4…+4, y −2…+4, z −4…+4 (ohne sich selbst).
Wind braucht also <b>Höhe und Abstand</b>.</li>
<li>Alle 128 Ticks: <code>sub = Wind · (y − 64 − Hindernisse) / 750 · 2,5</code>, dann ×3 bei Gewitter bzw. ×1,5 bei Regen. Unter y 65 (minus Hindernisse) erzeugt sie nichts.</li>
<li><b>Kein Zerfall</b>: Im Original zerfiel die Mühle bei zu viel Wind zu einem Generator. In Craft geht sie <b>nie von selbst kaputt</b>,
auch nicht bei Gewitter in großer Höhe; die Überhitzungsanzeige im GUI entfällt.</li>
<li>Puffer-Eigenheit (wie im Original, nur von 4 auf 16 angehoben): Würde der Puffer in einem Tick 16 EU erreichen, verfällt die
Energie dieses Ticks.</li>
</ul>
<div class="calc" data-calc="wind"><h4>Rechner: Windmühle – Original gegen Craft</h4><div class="row">
<label>Höhe y <input type="number" name="y" min="0" max="320" value="128"></label>
<label>Hindernisse <input type="number" name="ob" min="0" max="566" value="0"></label>
<label>Windstärke <input type="number" name="w" min="0" max="30" value="15"></label>
<label>Wetter <select name="we"><option value="clear">klar</option><option value="rain">Regen</option><option value="thunder">Gewitter</option></select></label>
</div><div class="out"></div><div class="msg"></div></div>
<p style="font-size:12px;color:var(--muted)">„Tatsächlich abgegeben“ ist eine Simulation über 2 000 Ticks unter der Annahme, dass das Netz jede Energie sofort abnimmt.</p>
<p>Spezifikation: <a href="#/spec-14">14 – Kraftwerke</a> §7, Original in <a href="#/spec-02">02 – Generatoren</a>.</p>
'''
    page('windmuehle', 'Windmühle', 'Neu & geändert', wind)

    # ------------------------------------------------------------------ Koksofen
    B = ('craft:coke_oven_bricks', 'Koksofenziegel', '')
    ko_key = {'B': B, 'C': ('craft:coke_oven', 'Koksofen (Controller)', 'ctrl'), '.': (None, 'Luft', 'air')}
    koks = f'''<h1>Koksofen</h1>
{h.infobox('Koksofen', 'craft:coke_oven', [
    ('Herkunft', NEW + ' Craft (Kraftwerke)'), ('Typ', 'Multiblock 3×3×3, innen hohl'), ('Strom', 'keiner'),
    ('Eingabe', 'nur Kohle (keine Holzkohle)'), ('Ausgabe', 'Koks'), ('Dauer', '900 Ticks = 45 s je Kohle'),
    ('Härte / Resistenz', '2,0 / 10, Spitzhacke'), ('Texturen/GUI', 'Railcraft-Stil'),
])}
<p class="lead">Der Koksofen verwandelt Kohle ohne Strom in <b>Koks</b>. Koks verhält sich überall wie Kohle (Ofen 1600 Ticks,
IC2-Generator 4 000 EU, Stirling Engine) – <b>nur im <a href="#/kohlekraftwerk">Kohlekraftwerk</a> bringt er das Doppelte</b>
(32 000 statt 16 000 EU). Das ist der Anreiz, Kohle zu verkoken.</p>

<h2 id="ko--aufbau">Aufbau</h2>
<p>Würfel 3×3×3, die Mitte ist <b>Luft</b>. Der Koksofen (Controller) sitzt in der Mitte einer <b>Seitenfläche</b> (nicht oben/unten),
die übrigen 25 Blöcke sind Koksofenziegel. Untere Reihe in den Bildern = Vorderseite.</p>
<div class="layers">{h.layer(['BBB', 'BBB', 'BBB'], ko_key, 'Ebene 1 (unten)')}{h.layer(['BBB', 'B.B', 'BCB'], ko_key, 'Ebene 2 (Mitte)')}{h.layer(['BBB', 'BBB', 'BBB'], ko_key, 'Ebene 3 (oben)')}<div>{h.legend(ko_key)}</div></div>
<h2 id="ko--funktion">So funktioniert es</h2>
<ul>
<li>1 Kohle → 1 Koks in <b>900 Ticks</b>, immer eine Kohle gleichzeitig. Holzkohle wird nicht angenommen.</li>
<li>Fehlt Kohle, bleibt der Fortschritt stehen (kein Zurücksetzen); ist die Ausgabe voll, wartet der Ofen.</li>
<li>Keine Asche, kein Kreosot.</li>
<li><b>Automatisierung</b>: Controller und alle Ziegel haben ein Item-Inventar – oben/Seiten führen in die Eingabe, unten in die Ausgabe.
Trichter oder Rohre können also an jedem Block des Ofens ansetzen.</li>
<li>Die Struktur wird beim Setzen/Abbauen eines Teils und spätestens alle 100 Ticks geprüft; unvollständig arbeitet der Ofen nicht
(sein Zustand bleibt erhalten).</li>
<li>9 Koks ⇄ 1 Koksblock (Brennwert 16 000 Ticks wie ein Kohleblock).</li>
</ul>
<h2 id="ko--rezepte">Rezepte</h2>
<div class="recipes">
{h.recipe(['BSB', 'SCS', 'BSB'], {'B': ('minecraft:brick', 'Ziegel'), 'S': ('minecraft:sand', 'Sand'), 'C': ('minecraft:clay_ball', 'Ton')}, ('craft:coke_oven_bricks', None), 2)}
{h.recipe(['BBB', 'BFB', 'BBB'], {'B': ('craft:coke_oven_bricks', None), 'F': ('minecraft:furnace', 'Ofen')}, ('craft:coke_oven', None))}
</div>
<p>Spezifikation: <a href="#/spec-14">14 – Kraftwerke</a> §1–2.</p>
'''
    page('koksofen', 'Koksofen', 'Neu & geändert', koks)

    # ------------------------------------------------------------------ Kohlekraftwerk
    K = ('craft:boiler_casing', 'Kesselgehäuse', '')
    kk = {'K': K, 'F': ('craft:boiler_firebox', 'Kesselfeuerung (Controller)', 'ctrl'), '.': (None, 'Luft', 'air')}
    T = ('craft:turbine_casing', 'Turbinengehäuse', '')
    tk = {'T': T, 'S': ('craft:steam_turbine', 'Dampfturbine (Controller)', 'ctrl'), 'R': ('craft:turbine_rotor', 'Turbinenrotor', '')}
    kohle = f'''<h1>Kohlekraftwerk (Kessel + Dampfturbine)</h1>
{h.infobox('Kohlekraftwerk', 'craft:boiler_firebox', [
    ('Herkunft', NEW + ' Craft (Kraftwerke)'), ('Teile', 'Kessel 3×3×3 + Dampfturbine 3×3×5'),
    ('Brennstoff', 'Kohle (800 Ticks) oder Koks (1600 Ticks)'), ('Dampf', 'bis 20 mB/t je Kessel'),
    ('Umwandlung', '1 mB Dampf = 1 EU'), ('Ausbeute', '16 000 EU / Kohle<br>32 000 EU / Koks'),
    ('Ausgabe', 'bis 64 EU/t (MV), max. 3 Kessel je Turbine'), ('Gefahr', 'Explosion Stärke 6 bei Trockenlauf'),
])}
<p class="lead">Der Ausbaupfad für Kohle: Ein IC2-Generator holt 4 000 EU aus einer Kohle, das Kohlekraftwerk <b>16 000 EU</b> – und mit
Koks aus dem <a href="#/koksofen">Koksofen</a> sogar <b>32 000 EU</b>. Dafür muss der Kessel aufheizen, mit Wasser versorgt werden
und Asche loswerden.</p>

<h2 id="kk--kessel">Der Kessel</h2>
<p>Würfel 3×3×3, Mitte <b>Luft</b>, die Kesselfeuerung (Controller) in der Mitte einer Seitenfläche, 25 Kesselgehäuse.</p>
<div class="layers">{h.layer(['KKK', 'KKK', 'KKK'], kk, 'Ebene 1 (unten)')}{h.layer(['KKK', 'K.K', 'KFK'], kk, 'Ebene 2 (Mitte)')}{h.layer(['KKK', 'KKK', 'KKK'], kk, 'Ebene 3 (oben)')}<div>{h.legend(kk)}</div></div>
<h3 id="kk--ablauf">Ablauf pro Tick</h3>
<ol>
<li><b>Nachlegen</b>: Ist die Brennzeit abgelaufen, der Dampftank nicht voll, Wasser vorhanden und der Asche-Slot nicht voll, wird
1 Brennstoff verbraucht – Kohle brennt <b>800 Ticks</b>, Koks <b>1 600 Ticks</b>. Nur Kohle und Koks, keine Blöcke, keine Holzkohle.</li>
<li><b>Temperatur</b>: Start 20 °C. Mit Feuer +1 °C pro Tick bis 1000 °C, ohne Feuer −1 °C pro Tick bis 20 °C.
Brennstoff brennt immer mit voller Rate – ein Kaltstart kostet also Energie (gewollt).</li>
<li><b>Dampf</b>: ab 100 °C entstehen <code>20 · (T − 100) / 900</code> mB Dampf pro Tick, also 20 mB/t bei 1000 °C.
Je 1 mB Dampf wird 1 mB Wasser verbraucht. Tanks: je 16 000 mB Wasser und Dampf.</li>
<li><b>Asche</b>: Kohle zählt +1, Koks +2; je 4 Punkte fällt 1 Asche in den Asche-Slot. Ist er voll (64), wird nicht nachgelegt.</li>
<li><b>Abgabe</b>: Bis zu 1 000 mB Dampf pro Tick wandern in angrenzende Flüssigkeitsempfänger (z. B. die Turbine oder ein Rohr), an allen Außenflächen.</li>
</ol>
{h.note('warn', '<b>Überhitzung:</b> Läuft der Wassertank leer, während der Kessel über 100 °C hat, wird er „trocken“. Kommt dann wieder Wasser hinein <b>oder</b> erreicht er 1000 °C, explodiert er (IC2-Explosion Stärke 6, alle Kesselblöcke werden zerstört, der Inhalt geht verloren). Erst unter 100 °C ist er wieder sicher.')}
<p><b>Wasser</b> kommt über Pumpen und Rohre (an jedem Kesselblock: hinein nur Wasser, heraus nur Dampf) oder per Eimer/Wasserzelle im Behälter-Slot.
<b>Items</b>: oben/Seiten → Brennstoff, unten → Asche heraus.</p>
<h2 id="kk--turbine">Die Dampfturbine</h2>
<p>Quader 3×3×5 mit liegender Längsachse. Innen drei Turbinenrotoren hintereinander, die Dampfturbine (Controller) in der Mitte einer
3×3-Stirnseite, 41 Turbinengehäuse. Die Bilder zeigen die fünf Querschnitte von vorne nach hinten.</p>
<div class="layers">{h.layer(['TTT', 'TST', 'TTT'], tk, 'Scheibe 1 (Stirnseite)', 30)}{h.layer(['TTT', 'TRT', 'TTT'], tk, 'Scheibe 2', 30)}{h.layer(['TTT', 'TRT', 'TTT'], tk, 'Scheibe 3', 30)}{h.layer(['TTT', 'TRT', 'TTT'], tk, 'Scheibe 4', 30)}{h.layer(['TTT', 'TTT', 'TTT'], tk, 'Scheibe 5 (hinten)', 30)}<div>{h.legend(tk)}</div></div>
<ul>
<li>Dampftank 16 000 mB, befüllbar über jeden Turbinenblock (nur Dampf).</li>
<li>Pro Tick werden bis zu <b>64 mB</b> verbraucht und 1:1 in EU umgewandelt – drei Kessel (je 20 mB/t) sind damit voll nutzbar.</li>
<li>Die EU kommen am <b>Controller und an jedem Turbinengehäuse</b> heraus (das fertige Gehäuse leitet verlustfrei): Pakete bis 64 EU, Stufe <b>MV</b>, interner Puffer 128 EU. Ist der Puffer voll, wird kein Dampf verbraucht.</li>
<li>Keine Explosion, kein Verschleiß.</li>
</ul>
<div class="calc" data-calc="boiler"><h4>Rechner: Kohlekraftwerk</h4><div class="row">
<label>Brennstoff <select name="fuel"><option value="coal">Kohle</option><option value="coke">Koks</option></select></label>
<label>Kessel an der Turbine <select name="nb"><option>1</option><option>2</option><option selected>3</option></select></label></div>
<div class="out"></div><svg viewBox="0 0 520 170" style="color:var(--ink2)"></svg></div>
<h2 id="kk--rezepte">Rezepte</h2>
<div class="recipes">
{h.recipe(['PBP', 'BFB', 'PBP'], {'P': ('craft:iron_plate', None), 'B': ('minecraft:bricks', 'Ziegelsteine'), 'F': ('minecraft:furnace', 'Ofen')}, ('craft:boiler_casing', None), 4)}
{h.recipe(['CCC', 'CIC', 'CCC'], {'C': ('craft:boiler_casing', None), 'I': ('craft:iron_furnace', None)}, ('craft:boiler_firebox', None))}
{h.recipe(['PPP', 'PMP', 'PPP'], {'P': ('craft:steel_plate', None), 'M': ('craft:machine_casing', None)}, ('craft:turbine_casing', None), 6)}
{h.recipe(['PSP', 'SIS', 'PSP'], {'P': ('craft:steel_plate', None), 'S': ('craft:steel_ingot', None), 'I': ('minecraft:iron_block', 'Eisenblock')}, ('craft:turbine_rotor', None))}
{h.recipe(['CRC', 'GMG', 'CRC'], {'C': ('craft:turbine_casing', None), 'R': ('craft:turbine_rotor', None), 'G': ('craft:generator', None), 'M': ('craft:mv_transformer', None)}, ('craft:steam_turbine', None))}
</div>
<p>Stahl- und Eisenplatten stammen aus GregTech (Plattenbieger, Hochofen) – das Kraftwerk ist also Mid-Game.
Spezifikation: <a href="#/spec-14">14 – Kraftwerke</a> §3–4.</p>
'''
    page('kohlekraftwerk', 'Kohlekraftwerk', 'Neu & geändert', kohle)

    # ------------------------------------------------------------------ Wasserturbine
    W = ('craft:water_turbine_casing', 'Wasserturbinengehäuse', '')
    wk = {'W': W, 'C': ('craft:water_turbine', 'Wasserturbine (Controller)', 'ctrl'), '~': (None, 'Messbereich: hier zählt fließendes Wasser', 'flow')}
    ring = ['~' * 13] * 5 + ['~~~~~WWW~~~~~', '~~~~~WWW~~~~~', '~~~~~WCW~~~~~'] + ['~' * 13] * 5
    wt = f'''<h1>Wasserturbine</h1>
{h.infobox('Wasserturbine', 'craft:water_turbine', [
    ('Herkunft', NEW + ' Craft (Kraftwerke)'), ('Typ', 'Multiblock 3×3×3, voll'), ('Leistung', '0,25 EU/t je fließendem Block'),
    ('Fluss-Biom', '×1,5'), ('Deckel', '<b>32 EU/t</b> (LV)'), ('Mindestens', '16 fließende Blöcke'), ('GUI', 'keine – Rechtsklick zeigt den Zustand'),
])}
<p class="lead">Die große Schwester der <a href="#/wassermuehle">Wassermühle</a>: zuverlässige <b>16–32 EU/t Grundlast</b> rund um die Uhr –
sie braucht aber einen echten, fließenden Fluss.</p>

<h2 id="wt--aufbau">Aufbau</h2>
<p>Voller Würfel 3×3×3 (auch die Mitte ist ein Gehäuseblock), die Wasserturbine (Controller mit Rotor-Textur) in der Mitte einer
Seitenfläche, 26 Wasserturbinengehäuse. Gezählt wird fließendes Wasser bis 5 Blöcke um die Turbine herum (13×13), von der unteren Ebene bis 20 Blöcke über dem Deckel.</p>
<div class="layers">{h.layer(ring, wk, 'Draufsicht mittlere Ebene: Turbine + Messbereich (13×13, zusammen 3860 Positionen)', 18)}<div>{h.legend(wk)}</div></div>
<h2 id="wt--funktion">So funktioniert es</h2>
<ul>
<li>Alle 128 Ticks zählt die Turbine die <b>fließenden</b> Wasserblöcke im Messbereich (Quellblöcke zählen nicht).</li>
<li>Unter <b>16</b> fließenden Blöcken: 0 EU/t.</li>
<li>Leistung <code>0,25 EU/t · fließend</code>, in Fluss-Biomen ×1,5, gedeckelt auf <b>32 EU/t</b>. Volllast also ab 128 fließenden Blöcken (im Fluss ab 86).</li>
<li>EU kommen am Controller <b>und an jedem Gehäuseblock</b> heraus, Pakete bis 32 (LV), Bruchteile werden gesammelt.</li>
<li>Rechtsklick: Chat-Meldung „Fließendes Wasser: n, Leistung x EU/t“ oder „Struktur unvollständig“.</li>
</ul>
<div class="calc" data-calc="waterturbine"><h4>Rechner: Wasserturbine</h4><div class="row">
<label>gezählte fließende Wasserblöcke <input type="number" name="n" min="0" max="3860" value="90"></label>
<label><input type="checkbox" name="r" checked> Fluss-Biom</label></div><div class="out"></div><div class="msg"></div></div>
<h2 id="wt--rezepte">Rezepte</h2>
<div class="recipes">
{h.recipe(['RRR', 'RMR', 'RRR'], {'R': ('craft:refined_iron_plate', None), 'M': ('craft:machine_casing', None)}, ('craft:water_turbine_casing', None), 2)}
{h.recipe(['WCW', 'CGC', 'WCW'], {'W': ('craft:water_mill', None), 'C': ('craft:water_turbine_casing', None), 'G': ('craft:generator', None)}, ('craft:water_turbine', None))}
</div>
<p>Spezifikation: <a href="#/spec-14">14 – Kraftwerke</a> §5.</p>
'''
    page('wasserturbine', 'Wasserturbine', 'Neu & geändert', wt)

    # ------------------------------------------------------------------ Großkraftwerke
    hk = {'K': ('craft:high_pressure_boiler_casing', 'Hochdruck-Kesselgehäuse', ''),
          'F': ('craft:high_pressure_boiler_firebox', 'Hochdruck-Kesselfeuerung (Controller)', 'ctrl'), '.': (None, 'Luft', 'air')}
    gk = {'T': ('craft:large_turbine_casing', 'Großes Turbinengehäuse', ''), 'S': ('craft:large_steam_turbine', 'Große Dampfturbine (Controller)', 'ctrl'),
          'R': ('craft:large_turbine_rotor', 'Großer Turbinenrotor', ''), '.': (None, 'Luft', 'air')}
    wk2 = {'W': ('craft:large_water_turbine_casing', 'Großes Wasserturbinengehäuse', ''), 'C': ('craft:large_water_turbine', 'Große Wasserturbine (Controller)', 'ctrl')}
    full5, hollow5 = ['KKKKK'] * 5, ['KKKKK', 'K...K', 'K...K', 'K...K', 'KKKKK']
    gross = f'''<h1>Großkraftwerke</h1>
{h.infobox('Großkraftwerke', 'craft:large_steam_turbine', [
    ('Herkunft', NEW + ' Craft (Kraftwerke, Phase 21)'), ('Anlagen', 'Hochdruckkessel, Große Dampf-, Wasser- und Windturbine'),
    ('Stufe', '<b>HV</b> (Pakete bis 512)'), ('Leistung', 'Dampf bis 512, Wasser bis 256, Wind bis 384 EU/t'),
])}
<p class="lead">Die zweite Ausbaustufe für Kohle, Wasser und Wind: 256 bis 512 EU/t je Anlage, <b>rund um die Uhr</b>. Zum Vergleich: Ein
9×9-Solarfeld liefert 97 EU/t, und das nur am Tag. Die kleinen Anlagen bleiben unverändert.</p>

<h2 id="gk--ueberblick">Welche Anlage wofür?</h2>
{h.table(['Anlage', 'Leistung', 'braucht dauerhaft', 'Aufwand', 'gut, wenn …'], [
    ['<a href="#/grosskraftwerke/gk--dampf">Hochdruckkessel + Große Dampfturbine</a>', 'bis <b>480 EU/t</b> (3 Kessel)', 'Kohle oder Koks, viel Wasser', 'hoch (Stahl, Titan, Wolframstahl)', 'du Kohle/Koks automatisch nachliefern kannst'],
    ['<a href="#/grosskraftwerke/gk--wasser">Große Wasserturbine</a>', 'bis <b>256 EU/t</b>', 'nichts (fließendes Wasser bleibt)', 'mittel (Stahl, raff. Eisen)', 'du einmal baust und nie wieder hinschauen willst'],
    ['<a href="#/grosskraftwerke/gk--wind">Große Windturbine</a>', 'meist 60–250, bei Gewitter bis <b>384 EU/t</b>', 'nichts', 'mittel (Carbon-Rotor)', 'du hoch bauen kannst (Berg, Turm)'],
])}
<p>Alle drei geben <b>HV</b>-Pakete ab (bis 512 EU). Das heißt für den Anschluss: {c('glass_fibre_cable')} oder
{c('insulated_hv_cable')}, als Speicher eine {c('mfsu')}, und vor normalen LV-Maschinen (32 EU) Transformatoren
(HV → MV → LV). Ein LV-Gerät direkt am Großkraftwerk explodiert. Siehe <a href="#/strom">Strom anschließen</a>.</p>

<h2 id="gk--kessel">Hochdruckkessel</h2>
<p>Der Hochdruckkessel macht aus Wasser und Kohle (oder Koks) <b>Dampf</b>. Strom erzeugt er selbst nicht – dafür ist die
<a href="#/grosskraftwerke/gk--dampf">Große Dampfturbine</a> da. Im Prinzip ist er der <a href="#/kohlekraftwerk">Kessel des Kohlekraftwerks</a>
in groß: achtmal mehr Dampf, aber auch achtmal mehr Wasserbedarf.</p>
<h3>Aufbau</h3>
<p>Ein hohler Würfel 5×5×5: außen <b>97 Hochdruck-Kesselgehäuse</b>, innen 3×3×3 <b>Luft</b> (dort darf nichts stehen, auch keine
Fackel), und genau in der Mitte einer Seitenfläche die <b>Hochdruck-Kesselfeuerung</b>. Die Feuerung ist der Controller:
Rechtsklick darauf öffnet die GUI. Die Struktur wird beim Setzen/Abbauen und alle 100 Ticks geprüft. Fehlt ein Block, steht
der Kessel still, behält aber Temperatur, Wasser und Dampf.</p>
<div class="layers">{h.layer(full5, hk, 'Ebene 1 und 5', 22)}{h.layer(hollow5, hk, 'Ebene 2 und 4', 22)}{h.layer(['KKKKK', 'K...K', 'K...K', 'K...K', 'KKFKK'], hk, 'Ebene 3 (Mitte)', 22)}<div>{h.legend(hk)}</div></div>
<h3>So läuft er</h3>
<ol>
<li><b>Wasser rein.</b> Der Kessel nimmt Wasser an <b>jedem</b> seiner Blöcke an (Rohre, Wassersammler, Tanks), oder über den
Eimer-/Zellen-Slot in der GUI. Ohne Wasser legt er keinen Brennstoff nach.</li>
<li><b>Brennstoff rein.</b> Nur <b>Kohle</b> oder <b>{c('coke')}</b> (keine Holzkohle, keine Blöcke). Ein Trichter oben oder an der Seite
füllt den Brennstoff-Slot; jedes Stück brennt sofort mit voller Rate, egal wie heiß der Kessel ist.</li>
<li><b>Aufheizen.</b> Start bei 20 °C, solange etwas brennt +1 °C pro Tick, sonst −1 °C pro Tick. Ab <b>100 °C</b> entsteht Dampf,
bei <b>1500 °C</b> volle 160 mB/t. Bis zur Volllast dauert es rund <b>70 Sekunden</b> (1 400 Ticks) – der Brennstoff in dieser
Zeit ist teilweise verschenkt, das ist gewollt. Darum: durchlaufen lassen statt oft an- und ausschalten.</li>
<li><b>Dampf raus.</b> Jeden Tick schiebt der Kessel bis zu 1 000 mB Dampf in alles, was direkt an ihm anliegt und Dampf nimmt –
am besten direkt in die Große Dampfturbine. Ist der Dampftank voll (Turbine nimmt nichts mehr ab), legt er keinen neuen
Brennstoff nach und verbraucht auch kein Wasser; das ist ungefährlich.</li>
<li><b>Asche raus.</b> Je 4 Kohle (bzw. 2 Koks) entsteht 1 Asche. Ist der Asche-Slot voll (64), legt der Kessel <b>nichts mehr nach</b>
und geht aus – bei Volllast mit Kohle nach gut einer halben Stunde. Ein Trichter <b>unter</b> dem Kessel zieht die Asche ab.</li>
</ol>
{h.table(['Wert', 'Kessel (klein)', 'Hochdruckkessel'], [
    ['Wasser-/Dampftank', '16 000 mB', '64 000 mB'], ['Höchsttemperatur', '1000 °C', '<b>1500 °C</b>'],
    ['Dampf bei Höchsttemperatur', '20 mB/t', '<b>160 mB/t</b> (<code>160 · (T − 100) / 1400</code>)'],
    ['Wasserbedarf bei Volllast', '20 mB/t', '<b>160 mB/t</b> = 3,2 Eimer pro Sekunde'],
    ['Kohle', '800 Ticks = 16 000 EU', '<b>150 Ticks = 24 000 EU</b> → 8 Kohle pro Minute'],
    ['Koks', '1 600 Ticks = 32 000 EU', '<b>300 Ticks = 48 000 EU</b> → 4 Koks pro Minute'],
    ['Explosion', 'Stärke 6', '<b>Stärke 10</b>'],
])}
<h3 id="gk--explosion">Die eine Regel, die man kennen muss</h3>
{h.note('warn', 'Läuft der Wassertank <b>leer</b>, während der Kessel über 100 °C heiß ist, wird er „trocken“. Kommt dann auch nur ein '
'Tropfen Wasser hinein, <b>explodiert er sofort</b> (Stärke 10, alle Kesselblöcke sind weg). Ebenso, wenn er trocken weiter bis 1500 °C heizt. '
'Erst unter 100 °C ist er wieder sicher.')}
<p>Das passiert nicht nur, wenn das Wasser ganz ausfällt, sondern <b>auch schleichend</b>: Liefert die Wasserversorgung z. B. nur
150 mB/t, der Kessel verdampft aber 160 mB/t, sinkt der Tank um 10 mB pro Tick. Nach gut 5 Minuten ist er leer – und die nächste
Wasserlieferung lässt ihn explodieren. Darum:</p>
<ul>
<li>Die Wasserzufuhr muss <b>mindestens 160 mB/t</b> schaffen, lieber mehr. Den Wasserstand in der GUI im Blick behalten: Er
muss bei Volllast voll bleiben oder steigen.</li>
<li>Ist der Kessel doch trocken: <b>Wasser abklemmen</b> (Rohr abbauen, Sammler weg), keinen Brennstoff mehr nachlegen und warten, bis
die GUI unter 100 °C zeigt. Danach wieder Wasser anschließen.</li>
<li>Keine Gefahr besteht, wenn nur der Dampf nicht abgenommen wird oder der Brennstoff ausgeht.</li>
</ul>
<h3 id="gk--wasserversorgung">Woher 160 mB Wasser pro Tick?</h3>
{h.table(['Quelle', 'Wasser', 'reicht für einen Hochdruckkessel'], [
    [c('aqueous_accumulator'), '<b>25 mB/t</b>, ohne Strom, endlos', '<b>7 Stück</b> (175 mB/t)'],
    ['Wasserdichtes Holz-/Stein-/Eisenrohr …', '10 mB/t pro Rohrstrang', 'nein (16 Stränge)'],
    [c('golden_fluid_pipe') + ' / Smaragdrohr', '40 mB/t pro Rohrstrang', '4 parallele Stränge'],
    [c('liquiduct'), 'bis 100 mB/t pro Ausgabestelle', '2 Ausgabestellen am Kessel'],
    ['IC2-Pumpe (gibt direkt an Kessel, Tank oder Rohr ab)', '5 mB/t', 'nein'],
])}
<p>Am einfachsten: <b>Wassersammler direkt an den Kessel</b>. Ein Wassersammler braucht <b>zwei Wasserblöcke</b> waagerecht neben sich
und gibt sein Wasser selbst an den Kessel ab. Ein Hochdruckkessel braucht <b>mindestens 7</b> davon. So passen sie an eine
5 Blöcke breite Kesselseite:</p>
<ul>
<li>Kesselebene 1, 3 und 5: <code>Wasser – Sammler – Wasser – Sammler – Wasser</code> (je 2 Sammler)</li>
<li>Kesselebene 2 und 4: eine Reihe aus 5 beliebigen festen Blöcken (z. B. Stein) als Trennschicht</li>
</ul>
<p>Das sind 6 Sammler pro Seite (150 mB/t). <b>Zwei</b> Seiten so bestückt ergeben 12 Sammler = 300 mB/t – genug Reserve. Eine Seite
allein reicht <b>nicht</b>. Die Trennschicht ist wichtig: Ohne sie stünden Sammler direkt übereinander (siehe Tipp). Das Wasser
oben darf nach hinten herauslaufen; der Sammler verbraucht es nicht, auch fließendes Wasser zählt.</p>
{h.note('tip', 'Sammler nicht direkt aneinander oder übereinander setzen: Ein Sammler beliefert reihum seine Nachbarn – grenzt er an einen anderen '
'Sammler, verschenkt er dort jeden zweiten Takt und liefert nur noch die Hälfte. Jeder Sammler soll als einzigen „Tank“ den Kessel berühren – auch nicht die Turbine.')}

<h2 id="gk--dampf">Große Dampfturbine</h2>
<p>Die Turbine macht aus Dampf Strom: <b>1 mB Dampf = 1 EU</b>, bis zu <b>512 mB pro Tick</b>. Ein Hochdruckkessel liefert 160 mB/t,
also passen <b>drei Kessel</b> an eine Turbine (480 EU/t). Die Turbine selbst braucht weder Wasser noch Brennstoff, kann nicht
explodieren und nutzt sich nicht ab.</p>
<h3>Aufbau</h3>
<p>Ein liegender Quader 5×5×9. Innen läuft eine Achse aus <b>7 Großen Turbinenrotoren</b> genau durch die Mitte, der restliche
Innenraum ist Luft. Die Hülle besteht aus <b>161 Großen Turbinengehäusen</b>, und in der Mitte einer der beiden 5×5-Stirnseiten sitzt
die <b>Große Dampfturbine</b> (Controller, Rechtsklick = GUI). Querschnitte von vorne nach hinten:</p>
<div class="layers">{h.layer(['TTTTT'] * 2 + ['TTSTT'] + ['TTTTT'] * 2, gk, 'Scheibe 1 (Stirnseite)', 22)}{h.layer(['TTTTT', 'T...T', 'T.R.T', 'T...T', 'TTTTT'], gk, 'Scheiben 2–8', 22)}{h.layer(['TTTTT'] * 5, gk, 'Scheibe 9 (hinten)', 22)}<div>{h.legend(gk)}</div></div>
<h3>Kessel anschließen</h3>
<ul>
<li>Die Kessel einfach <b>direkt an die Turbine bauen</b> – Fläche an Fläche, egal welche Blöcke sich berühren. Der Kessel schiebt den
Dampf selbst hinein (bis 1 000 mB/t), Rohre sind nicht nötig.</li>
<li>Drei Kessel passen z. B. so: einer an die linke Längsseite, einer an die rechte, einer oben drauf oder an die hintere Stirnseite.</li>
<li>Über Rohre geht es auch, aber ein wasserdichtes Goldrohr schafft nur 40 mB/t – für einen Hochdruckkessel bräuchte man vier.</li>
</ul>
<h3>Strom abnehmen</h3>
<ul>
<li>Strom kommt am Controller <b>und an jedem Großen Turbinengehäuse</b> heraus – das Kabel kann irgendwo an der Turbine sitzen.</li>
<li>Pakete bis 512 EU (<b>HV</b>), interner Puffer 1 024 EU. Nimmt niemand den Strom ab, ist der Puffer voll, die Turbine nimmt keinen
Dampf mehr, der Dampf staut sich im Kessel, und der Kessel hört auf nachzulegen. Nichts geht kaputt, es wird nur kein Brennstoff verbraucht.</li>
</ul>
<h3>Beispiel: volles Dampfkraftwerk</h3>
<ul>
<li>1 Große Dampfturbine + 3 Hochdruckkessel + 36 Wassersammler (12 je Kessel, zwei Seiten bestückt) = <b>480 EU/t</b>, Tag und Nacht.
Plane den Platz so, dass jeder Kessel eine Seite an der Turbine hat und zwei andere Seiten für die Sammler frei bleiben.</li>
<li>Verbrauch: <b>24 Kohle</b> oder <b>12 Koks pro Minute</b> (1 440 Kohle bzw. 720 Koks pro Stunde). Eine volle Truhe Kohle
(1 728 Stück) über einen Trichter reicht einem einzelnen Kessel rund 3½ Stunden.</li>
<li>Koks ist doppelt so ergiebig: Er kommt ohne Strom aus dem <a href="#/koksofen">Koksofen</a> (1 Kohle → 1 Koks).</li>
<li>Anschluss: Glasfaserkabel zur MFSU, von dort über HV-/MV-/LV-Transformatoren zu den Maschinen.</li>
</ul>

<h2 id="gk--wasser">Große Wasserturbine</h2>
<p>Die Große Wasserturbine macht Strom aus <b>fließendem Wasser</b> in ihrer Umgebung – ohne Brennstoff, ohne Wartung, ohne Gefahr.
Sie ist die große Schwester der <a href="#/wasserturbine">Wasserturbine</a>.</p>
<h3>Aufbau</h3>
<p>Ein <b>voller</b> Würfel 5×5×5 (auch innen nur Gehäuse): <b>124 Große Wasserturbinengehäuse</b> und in der Mitte einer Seitenfläche die
<b>Große Wasserturbine</b> (Controller). Eine GUI gibt es nicht.</p>
<div class="layers">{h.layer(['WWWWW'] * 4 + ['WWCWW'], wk2, 'jede Ebene (Controller nur in Ebene 3)', 22)}<div>{h.legend(wk2)}</div></div>
<h3>So läuft sie</h3>
<ul>
<li>Alle 128 Ticks (gut 6 Sekunden) zählt sie die <b>fließenden</b> Wasserblöcke um sich herum: 7 Blöcke über jede Seite hinaus
(zusammen 19×19), von der untersten Ebene des Würfels bis 24 Blöcke über seinem Deckel. <b>Quellblöcke zählen nicht</b>, nur
fließendes Wasser (auch fallendes, z. B. Wasserfälle).</li>
<li>Unter <b>64</b> fließenden Blöcken: gar nichts. Ab 64: <b>1 EU/t je fließendem Block</b>, in einem Fluss-Biom ×1,5.</li>
<li>Höchstens <b>256 EU/t</b> – erreicht ab 256 fließenden Blöcken (im Fluss-Biom ab 171).</li>
<li><b>Rechtsklick</b> auf den Controller zeigt „Fließendes Wasser: n, Leistung x EU/t“ (oder „Struktur unvollständig“). So siehst du sofort,
ob noch Wasser fehlt.</li>
<li>Strom kommt an jedem Gehäuseblock heraus, Pakete bis 256 EU (HV).</li>
</ul>
<h3>Wie bekomme ich 256 fließende Blöcke?</h3>
<p>Ein einzelner Quellblock auf einer ebenen Fläche fließt 7 Blöcke weit in alle Richtungen und erzeugt dabei bis zu gut 100
fließende Blöcke. Daraus ergibt sich der einfachste Aufbau:</p>
<ol>
<li>Rund um die Turbine eine <b>ebene Fläche</b> (z. B. Stein) auf Höhe der untersten Würfelebene bauen, 7 Blöcke breit nach jeder Seite.
Den Rand mit einer Mauer abschließen, sonst läuft das Wasser über den Rand und fehlt in der Zählung.</li>
<li>Einige <b>Quellblöcke mit Abstand</b> verteilen (z. B. vor jede Seite der Turbine einen und in die vier Ecken). Liegen Quellen zu nah
beieinander, entstehen neue Quellblöcke statt fließendem Wasser.</li>
<li>Mit Rechtsklick nachzählen. Fehlt noch etwas: eine <b>zweite Ebene</b> darüber bauen oder Wasser von oben herabfallen lassen – alles bis
24 Blöcke über dem Deckel zählt mit.</li>
</ol>
{h.note('tip', 'In einem Fluss-Biom (der Controller muss im Biom stehen) reichen schon 171 fließende Blöcke für die vollen 256 EU/t.')}

<h2 id="gk--wind">Große Windturbine</h2>
<p>Die Große Windturbine macht Strom aus <b>Wind</b>: je höher, desto mehr. Kein Brennstoff, kein Verschleiß, sie geht auch nie kaputt.</p>
<h3>Aufbau</h3>
<ol>
<li><b>8 Windturbinenmasten</b> senkrecht übereinander setzen.</li>
<li>Oben drauf die <b>Große Windturbine</b> (Gondel). Die Seite, in die sie beim Setzen schaut, ist die Rotorseite.</li>
<li>Mit einem <b>Windrotor</b> in der Hand auf die Gondel rechtsklicken – der Rotor wird eingesetzt und dreht sich als großes 5×5-Rad vor der
Gondel (er kollidiert mit nichts). Schleichen + Rechtsklick mit leerer Hand nimmt ihn wieder heraus; mit einem anderen Rotor
in der Hand wird getauscht.</li>
<li>Das Kabel kommt einfach an den <b>Mastfuß</b> – jeder Mast leitet den Strom verlustfrei. Pakete bis 384 EU (HV).</li>
</ol>
{h.table(['Rotor', 'Faktor', 'Material'], [[c('iron_wind_rotor'), '0,5', 'Eisen'], [c('steel_wind_rotor'), '0,75', 'Stahl'], [c('carbon_wind_rotor'), '1,0', 'Carbonplatten + Fortgeschrittene Legierung']])}
<h3>Was die Leistung bestimmt</h3>
<ul>
<li><b>Höhe der Gondel:</b> unter y 64 kommt nichts. Bei normalem Wind und Carbon-Rotor gilt ungefähr <b>Leistung ≈ Höhe − 64</b> EU/t.</li>
<li><b>Hindernisse:</b> Jeder Block in einem Kasten von 5 Blöcken um die Gondel (11×11, von 3 unter bis 5 über der Gondel) kostet Leistung –
die eigenen Masten und die Gondel zählen nicht. Den Bereich also komplett frei halten: keine Bäume, kein Dach, keine Plattform direkt
unter der Gondel. Mit 8 Masten ist der Boden weit genug weg.</li>
<li><b>Wind:</b> Die Windstärke schwankt langsam im ganzen Spiel (meist 10–20, Mittel 15); bei Regen ×1,5, bei Gewitter ×3.</li>
<li>Rechtsklick auf die Gondel zeigt „Hindernisse: n, Leistung x EU/t“ (oder „Kein Rotor“ / „Struktur unvollständig“). Neu gezählt
wird alle 128 Ticks, die Hindernisse alle 1 024 Ticks.</li>
</ul>
{h.table(['Gondel auf Höhe', 'Carbon, normaler Wind', 'bei Regen', 'bei Gewitter'], [
    ['y 100', '≈ 36 EU/t', '≈ 54 EU/t', '≈ 108 EU/t'],
    ['y 128', '≈ 64 EU/t', '≈ 96 EU/t', '≈ 192 EU/t'],
    ['y 200', '≈ 136 EU/t', '≈ 204 EU/t', '<b>384 EU/t</b> (Deckel)'],
    ['y 300', '≈ 236 EU/t', '≈ 354 EU/t', '<b>384 EU/t</b>'],
])}
<p>Stahlrotor: Werte × 0,75, Eisenrotor: × 0,5. Höchstwert immer 384 EU/t. Weil der Wind schwankt, lohnt sich eine
{c('mfsu')} als Puffer dahinter.</p>

<h2 id="gk--fehler">Es kommt kein Strom – woran liegt es?</h2>
{h.table(['Anlage', 'prüfen'], [
    ['alle', 'Struktur vollständig? Ein falscher oder fehlender Block (oder etwas im Luft-Innenraum) stoppt alles. Wasser-/Windturbine: Rechtsklick zeigt „Struktur unvollständig“.'],
    ['alle', 'Kabel: HV-taugliches Kabel (Glasfaser, isoliertes HV-Kabel). Ein Kupfer- oder Goldkabel schmilzt bei HV-Paketen.'],
    ['Hochdruckkessel', 'Wasser im Tank? Ohne Wasser wird kein Brennstoff nachgelegt. Asche-Slot voll? Dann geht er aus. Temperatur unter 100 °C? Dann noch kein Dampf.'],
    ['Große Dampfturbine', 'Berührt der Kessel die Turbine direkt? Dampftank in der GUI leer → kein Dampf kommt an.'],
    ['Große Wasserturbine', 'Unter 64 fließenden Blöcken gibt es 0 EU/t. Quellblöcke und stehendes Wasser zählen nicht.'],
    ['Große Windturbine', 'Rotor eingesetzt? Gondel über y 64? Hindernisse (Rechtsklick) möglichst 0.'],
])}

<h2 id="gk--rezepte">Rezepte</h2>
<div class="recipes">
{h.recipe(['SBS', 'BKB', 'SBS'], {'S': ('craft:steel_plate', None), 'B': ('craft:boiler_casing', None), 'K': ('minecraft:bricks', 'Ziegelsteine')}, ('craft:high_pressure_boiler_casing', None), 8)}
{h.recipe(['CCC', 'CFC', 'CCC'], {'C': ('craft:high_pressure_boiler_casing', None), 'F': ('craft:boiler_firebox', None)}, ('craft:high_pressure_boiler_firebox', None))}
{h.recipe(['PPP', 'PAP', 'PPP'], {'P': ('craft:steel_plate', None), 'A': ('craft:advanced_machine_casing', None)}, ('craft:large_turbine_casing', None), 8)}
{h.recipe(['TWT', 'WRW', 'TWT'], {'T': ('craft:titanium_plate', None), 'W': ('craft:tungstensteel_plate', None), 'R': ('craft:turbine_rotor', None)}, ('craft:large_turbine_rotor', None))}
{h.recipe(['CRC', 'GHG', 'CRC'], {'C': ('craft:large_turbine_casing', None), 'R': ('craft:large_turbine_rotor', None), 'G': ('craft:steam_turbine', None), 'H': ('craft:hv_transformer', None)}, ('craft:large_steam_turbine', None))}
{h.recipe(['SRS', 'RMR', 'SRS'], {'S': ('craft:steel_plate', None), 'R': ('craft:refined_iron_plate', None), 'M': ('craft:machine_casing', None)}, ('craft:large_water_turbine_casing', None), 8)}
{h.recipe(['WCW', 'CHC', 'WCW'], {'W': ('craft:water_turbine', None), 'C': ('craft:large_water_turbine_casing', None), 'H': ('craft:hv_transformer', None)}, ('craft:large_water_turbine', None))}
{h.recipe(['R R', 'RRR', 'R R'], {'R': ('craft:refined_iron_ingot', None)}, ('craft:wind_turbine_mast', None), 8)}
{h.recipe(['WAW', 'GHG', 'WAW'], {'W': ('craft:wind_mill', None), 'A': ('craft:advanced_machine_casing', None), 'G': ('craft:generator', None), 'H': ('craft:hv_transformer', None)}, ('craft:large_wind_turbine', None))}
{h.recipe(['PIP', 'IBI', 'PIP'], {'P': ('craft:iron_plate', None), 'I': ('minecraft:iron_ingot', 'Eisenbarren'), 'B': ('minecraft:iron_block', 'Eisenblock')}, ('craft:iron_wind_rotor', None))}
{h.recipe(['PSP', 'SRS', 'PSP'], {'P': ('craft:steel_plate', None), 'S': ('craft:steel_ingot', None), 'R': ('craft:iron_wind_rotor', None)}, ('craft:steel_wind_rotor', None))}
{h.recipe(['CAC', 'ARA', 'CAC'], {'C': ('craft:carbon_plate', None), 'A': ('craft:advanced_alloy', None), 'R': ('craft:steel_wind_rotor', None)}, ('craft:carbon_wind_rotor', None))}
</div>
<p>Spezifikation: <a href="#/spec-23">23 – Großkraftwerke</a>.</p>
'''
    page('grosskraftwerke', 'Großkraftwerke', 'Neu & geändert', gross)

    # ------------------------------------------------------------------ Mining-Dimension
    pk = {'S': ('minecraft:smooth_stone', 'Glatter Stein', ''), 'P': ('craft:mining_portal', 'Mining-Portal', '')}
    portal_tex = h.texture('craft:block/mining_portal')
    if portal_tex:
        pk['P'] = ('tex:craft:block/mining_portal', 'Mining-Portal', '')
    mining = f'''<h1>Mining-Dimension</h1>
{h.infobox('Mining-Welt', 'craft:mining_portal', [
    ('Herkunft', NEW + ' Craft (kein Pack-Vorbild)'), ('ID', '<code>craft:mining</code>'), ('Portal', 'Rahmen aus Glattem Stein,<br>mit Feuerzeug zünden'),
    ('Koordinaten', '1:1 zur Oberwelt'), ('Gelände', 'flacher Stein bis y 64'), ('Höhe', 'y −64 … 319 wie die Oberwelt'),
    ('Licht', 'hell, kein Tag/Nacht (immer Mittag)'), ('Mobs', 'keine'), ('Erze', 'alle Oberwelt-Erze (Vanilla, IC2, GT)'),
])}
<p class="lead">Eine eigene Dimension nur zum Abbauen: eine <b>flache, massive Steinwelt</b> mit offenem Himmel, ohne Höhlen, ohne
Monster und dauerhaft hell. Man steht oben auf der Ebene und baut mit Quarry, Miner oder Turtle nach unten ab.</p>

<h2 id="mi--portal">Das Portal</h2>
<div class="layers">{h.layer(['SSSS', 'SPPS', 'SPPS', 'SPPS', 'SSSS'], pk, 'kleinstes Portal: Innenraum 2×3', 36)}<div>{h.legend(pk)}</div></div>
<ul>
<li>Rahmen aus <b>Glattem Stein</b> (<code>minecraft:smooth_stone</code>), Form und Größe wie beim Nether-Portal: rechteckig, Innenraum 2×3 bis 21×21,
senkrecht, Ecken optional.</li>
<li>Gezündet mit <b>Feuerzeug</b> (oder Feuerkugel) an der Innenseite – nur in der Oberwelt und in der Mining-Welt. Ein Obsidianrahmen bleibt ein Nether-Portal.</li>
<li>Portalblock wie das Nether-Portal: 80 Ticks Wartezeit (Kreativ sofort), alle Entities reisen mit, Licht 11, zerfällt bei beschädigtem Rahmen,
eigene grau-blaue Textur. Es spawnen keine Zombifizierten Piglins.</li>
<li><b>Zielsuche</b>: vorhandenes Portal im Umkreis von 16 Blöcken (statt 128 wie beim Nether), damit benachbarte Oberwelt-Portale nicht
auf dasselbe Ziel fallen. Sonst wird ein neues Portal gebaut – in der Mining-Welt auf der Ebene bei y 65, notfalls mit einer Plattform aus Glattem Stein.</li>
</ul>
{h.note('info', 'Beim Durchgehen zeigt der Bildschirm das lila Nether-Overlay und lila Partikel – das ist im Vanilla-Client fest eingebaut.')}
<h2 id="mi--welt">Die Welt</h2>
{h.table(['Eigenschaft', 'Wert', 'Bedeutung'], [
    ['Oberfläche', 'massiv bis y 64, darüber Luft', 'komplett flach, Oberfläche ist Stein'],
    ['Untergrund', 'Tiefenschiefer unter y 0 (Übergang y 0–8), Bedrock ab y −64 (bis −60 ausdünnend)', 'wie die Oberwelt'],
    ['Höhlen, Wasser, Lava', 'keine', 'keine Carver, keine Aquifere, keine Lavaseen'],
    ['Füllmaterial', 'keine Erde, kein Kies, Granit, Diorit, Andesit, Tuff, Ton', 'nur Stein + Erze'],
    ['Licht', 'Umgebungslicht 1,0, feste Zeit 6000', 'Schächte sind auch ohne Fackeln hell'],
    ['Mobs', 'keine Spawns', 'sicheres Abbauen'],
    ['Betten', 'setzen den Spawnpunkt (schlafen geht nicht)', 'Ankerpunkt explodiert nicht, funktioniert aber nicht'],
    ['Wetter', 'kein Niederschlag', ''],
])}
<h2 id="mi--erze">Erze</h2>
<p>Es werden die <b>Platzierungen der Oberwelt wiederverwendet</b>, die Mengen sind also exakt gleich:</p>
<ul>
<li><b>Vanilla:</b> Kohle, Eisen, Gold, Redstone, Diamant, Lapis, Smaragd (hier überall, nicht nur im Gebirge).</li>
<li><b>IC2:</b> Kupfer (IC2-Häufigkeit), Zinn, Uran.</li>
<li><b>GregTech:</b> Bleiglanz (Galena), Iridium, Rubin, Saphir, Bauxit – Rubin, Saphir und Bauxit überall.</li>
<li>Was in der Oberwelt oberhalb von y 64 läge (obere Kohle, Teile von Eisen, Smaragd, Bauxit), entfällt ohne Ausgleich.</li>
<li><b>Nicht</b> vorhanden: Öl, Gummibäume, Nether-/End-GT-Erze, große Erzadern.</li>
</ul>
<p>Miner, Scanner und Quarry funktionieren dort ohne Sonderbehandlung.</p>
<h2 id="mi--turtles">Turtles</h2>
<p>Mit <a href="#/zusatzmods">CC: Tweaked</a> können Turtles in der Mining-Welt graben. Es gibt keine eigene Integration, aber Turtles
können über das normale Item-Inventar in Craft-Maschinen und -Kisten legen und daraus nehmen. Unzerstörbare Blöcke (Personal-Safe u. a.)
bauen sie nicht ab. Turtles brauchen Treibstoff (wie im Pack).</p>
<p>Spezifikation: <a href="#/spec-18">18 – Mining-Dimension</a>.</p>
'''
    page('mining-dimension', 'Mining-Dimension', 'Neu & geändert', mining)

    # ------------------------------------------------------------------ Leuchtschild
    shield = f'''<h1>Elektrischer Leuchtschild</h1>
{h.infobox('Elektrischer Leuchtschild', 'craft:electric_light_shield', [
    ('Herkunft', NEW + ' Craft (kein Pack-Vorbild)'), ('ID', '<code>craft:electric_light_shield</code>'),
    ('Speicher', '10 000 EU'), ('Stufe', '1 (LV), Laden max. 100 EU/t'), ('Licht', 'Kegel 16 Blöcke / 30°, Schein 7<br>(mit LambDynamicLights)'),
    ('Verbrauch', '1 EU je 4 Ticks Licht,<br>100 EU je Blockpunkt'), ('Kreativ-Tab', 'IC2 (leer und voll)'),
])}
<p class="lead">Ein Schild, der statt Haltbarkeit <b>EU</b> verbraucht und in der Hand <b>wie eine Taschenlampe leuchtet</b> – ein
Lichtkegel in Blickrichtung, den auch andere Spieler sehen. Das Licht ist rein optisch (dynamisches Licht über
LambDynamicLights), Mobs spawnen also weiter. Die Werte orientieren sich am Luminator und an den
LV-Elektrowerkzeugen.</p>

<h2 id="ls--rezept">Rezept</h2>
{h.recipe(['ALA', 'ASA', ' R '], {'A': ('craft:advanced_alloy', None), 'L': ('craft:luminator', None), 'S': ('minecraft:shield', 'Schild'), 'R': ('craft:re_battery', None)}, ('craft:electric_light_shield', None))}
<p>Der Schild kommt <b>leer</b> aus der Werkbank (die RE-Batterie darf beliebig geladen sein). Laden wie alle LV-Werkzeuge: BatBox,
Ladegerät, Batterie-Slot der Maschinen oder BatPack/LapPack auf dem Rücken.</p>

<h2 id="ls--licht">Licht</h2>
<ul>
<li>Leuchtet in <b>Haupt- oder Nebenhand</b>, solange das Licht eingeschaltet und Ladung vorhanden ist (nicht im Zuschauermodus).</li>
<li><b>Lichtkegel</b> von den Augen in Blickrichtung: 30° weit (die äußeren 5° werden weich dunkler), bis 4 Blöcke voll hell
(14), danach schwächer bis 16 Blöcke. Dazu ein <b>Schein</b> rund um den Träger (7 an den Augen, −1 je Block), damit man
die eigenen Füße sieht.</li>
<li><b>Wände</b> halten das Licht auf: die getroffene Fläche ist hell, dahinter bleibt es dunkel. Wasser lässt es durch.</li>
<li>Das Licht ist <b>dynamisches Licht</b> und braucht die Mod <b>LambDynamicLights</b> (ab Paket 1.0.12 mitinstalliert). Ohne sie
leuchtet nur die Fläche am Schild. Es ändert das Licht der Welt nicht: <b>Mobs spawnen weiter</b>, Pflanzen wachsen nicht davon.</li>
<li>Kosten: <b>1 EU je 4 Ticks</b> (5 EU/s) – voll geladen reicht das für rund 33 Minuten.</li>
<li><b>Modus-Taste + Rechtsklick</b> schaltet das Licht an/aus („Licht: an/aus“); der Tooltip zeigt den Zustand. Rechtsklick allein blockt.</li>
<li>Bis Craft 1.0.8 war das Licht ein unsichtbarer Block am Spieler. Solche Reste in alten Welten verschwinden von selbst.</li>
</ul>

<h2 id="ls--blocken">Blocken</h2>
<ul>
<li>Blockt wie ein Vanilla-Schild (5 Ticks Anlauf, Axt-Treffer sperren ihn 5 Sekunden).</li>
<li>Ein geblockter Treffer ab 3 Schaden kostet <b>100 EU je Haltbarkeitspunkt</b>, den ein normaler Schild verloren hätte
(1 + Schaden abgerundet). Der Schild selbst nimmt nie Schaden.</li>
<li>Reicht die Ladung nicht, wird der Treffer noch geblockt und der Schild ist danach leer.</li>
<li><b>Leer</b> lässt er sich nicht heben und senkt sich, sobald die Ladung ausgeht – kein Licht, kein Blocken.</li>
</ul>

<h2 id="ls--zauber">Verzauberungen</h2>
{h.table(['Verzauberung', 'Wirkung'], [
    ['Haltbarkeit I–III', 'je Blockpunkt Chance 1/(Stufe+1), dass er EU kostet; Licht kostet gleich viel'],
    ['Reparatur', 'Erfahrungskugeln laden den Schild: 1 XP = 200 EU (nimmt die XP vor anderen Items)'],
    ['Fluch des Verschwindens', 'wie Vanilla'],
])}
<p>Anders als der Vanilla-Schild auch am <b>Zaubertisch</b> verzauberbar (Verzauberbarkeit 10, dort nur Haltbarkeit); Reparatur und
Fluch kommen wie gewohnt über Bücher am Amboss.</p>
<p>Spezifikation: <a href="#/spec-30">30 – Elektrischer Leuchtschild</a>.</p>
'''
    page('leuchtschild', 'Elektrischer Leuchtschild', 'Neu & geändert', shield)

    # ------------------------------------------------------------------ Zusatzmods
    zus = f'''<h1>EMI, Xaero's Minimap & CC: Tweaked</h1>
<p class="lead">Drei Mods werden im Paket mitgeliefert. Sie ersetzen Mods aus FTB Ultimate, die es für Minecraft 1.21 nicht gibt.</p>
<h2 id="zu--emi">EMI – Rezeptanzeige {TECH}</h2>
<p>Ersatz für <b>NEI</b>. Craft bringt eine eigene EMI-Integration mit (Paket <code>dev.craft.compat.emi</code>), die alle Maschinen anzeigt:</p>
<ul>
<li><b>IC2:</b> Macerator, Extractor, Compressor, Recycler/Schrottbox, Canning Machine, Massefabrikator u. a.</li>
<li><b>GregTech:</b> Plattenbieger, Elektrolyseur, Hochofen (mit Hitze), Assembler, Canner, Implosionskompressor, Zentrifuge, Industrial Grinder, Vakuumgefrierer.</li>
<li><b>BuildCraft / Forestry / Kraftwerke:</b> Raffinerie, Thermionic Fabricator, Carpenter, Koksofen, Kessel, Turbine …</li>
</ul>
<p>Angezeigt werden Ein-/Ausgaben, Dauer, EU/t und Gesamt-EU wie in der jeweiligen GUI. Werkbank-Rezepte und Öfen zeigt EMI ohnehin.
Der <b>+</b>-Knopf füllt Werkbank-Rezepte auch in den <b>Projekttisch</b> (aus Vorrat und Inventar) und in die <b>Automatische Werkbank</b>.
Über einem Gegenstand: <b>R</b> = Rezept, <b>U</b> = Verwendungen. EMI ist optional – ohne EMI startet Craft trotzdem.</p>
<h2 id="zu--xaero">Xaero's Minimap</h2>
<p>Nur mitinstalliert, keine eigene Integration. Version neoforge-1.21.1-26.5.0.</p>
<h2 id="zu--cc">CC: Tweaked – Computer und Turtles {NEW}</h2>
<p>Ersatz für <b>ComputerCraft 1.5 + CCTurtle</b> aus dem Pack. Version 1.120.2 (NeoForge-Build, die Datei heißt trotzdem <code>…-forge-…</code>).</p>
<ul>
<li>Keine eigene Integration: keine Peripherals und keine Turtle-Upgrades (im Pack gab es MiscPeripherals).</li>
<li>Turtles nutzen trotzdem die Item-Inventare der Craft-Maschinen und -Kisten.</li>
<li>Turtles brauchen Treibstoff (<code>need_fuel = true</code>, wie <code>turtlesNeedFuel=true</code> im Pack); übrige Einstellungen sind Standard.</li>
</ul>
'''
    page('zusatzmods', 'EMI, Xaero & CC: Tweaked', 'Neu & geändert', zus)

    # ------------------------------------------------------------------ Fortschritt
    fort = f'''<h1>Spielfortschritt</h1>
<p class="lead">Eine typische Reihenfolge durch die Technik von Craft. Da die Rezepte die finalen GregTech-Varianten des Packs sind,
braucht vieles früh GT-Materialien (Platten, Stahl).</p>
<h2 id="fo--1">1. Grundlagen (IC2 Early Game)</h2>
<ul>
<li><b>Gummibäume</b> (Wald, Taiga, Dschungel, Sumpf) mit dem {c('treetap')} anzapfen → {c('resin')} → {c('rubber')}.</li>
<li><b>Zinn</b> ({c('tin_ore')}) und Vanilla-Kupfer schmelzen; {c('iron_furnace')} als erster Ofen.</li>
<li>{c('generator')} (10 EU/t aus Kohle), {c('re_battery')}, {c('batbox')} (40 000 EU, LV 32).</li>
<li>Erste Maschinen: {c('macerator')} (Erz/Rohmetall → 2 Staub = doppelte Ausbeute), {c('electric_furnace')}, {c('extractor')}, {c('compressor')}.</li>
<li>Kabel: Zinnkabel (5 EU), isoliertes Kupferkabel (32 EU, 0,2 EU Verlust je Block). Wie Spannung, Speicher und Trafos zusammenspielen: <a href="#/strom">Strom anschließen</a>.</li>
</ul>
<h2 id="fo--2">2. GregTech-Materialien</h2>
<ul>
<li>{c('plate_bending_machine')} für Platten, {c('industrial_electrolyzer')}, später der {c('industrial_blast_furnace')} (Multiblock mit Heizspulen) für Stahl & Co.</li>
<li>Damit freigeschaltet: {c('water_mill')}, {c('wind_mill')} und viele weitere Rezepte, die im Pack GT-Material verlangen.</li>
<li>Viele GT-Metalle (Nickel, Zink, Silber, Platin, Titan …) haben kein eigenes Erz und entstehen als Nebenprodukt von {c('industrial_grinder')}, Elektrolyseur und Zentrifuge – Übersicht: <a href="#/rohstoffe">Rohstoffe & Erze</a>.</li>
</ul>
<h2 id="fo--3">3. Mehr Strom</h2>
<ul>
<li>{c('geothermal_generator')} (20 EU/t aus Lava), <a href="#/wassermuehle">Wassermühlen</a>, <a href="#/windmuehle">Windmühlen</a> auf Bergen.</li>
<li><a href="#/koksofen">Koksofen</a> und <a href="#/kohlekraftwerk">Kohlekraftwerk</a> (bis 64 EU/t), <a href="#/wasserturbine">Wasserturbine</a> (bis 32 EU/t).</li>
<li>Später die <a href="#/grosskraftwerke">Großkraftwerke</a> (256–512 EU/t, HV) mit Stahl, Titan und Wolframstahl.</li>
<li>Speicher {c('mfe')} (600 000 EU, MV 128) und Transformatoren.</li>
</ul>
<h2 id="fo--4">4. BuildCraft & Umwandlung</h2>
<ul>
<li>Engines (Redstone, Stirling, Combustion), {c('quarry')} mit Landmarks, Pumpe, Tank, Raffinerie (Öl → Treibstoff), Rohre.</li>
<li>{c('electrical_engine')} (EU → MJ) und <a href="#/mod-powerconverters">Power Converters</a> (MJ ↔ EU über die {c('energy_bridge')}).</li>
<li>Ideal für Quarrys: die <a href="#/mining-dimension">Mining-Dimension</a>.</li>
</ul>
<h2 id="fo--5">5. Late Game</h2>
<ul>
<li>{c('mfsu')} (10 Mio. EU, HV 512), große <a href="#/solar-arrays">Solarfelder</a>, Nano- und Quantenrüstung, Bergbaulaser.</li>
<li>{c('nuclear_reactor')} (60–400+ EU/t), Massefabrikator, Teleporter, Terraformer, Iridium.</li>
</ul>
{h.note('tip', 'Mit EMI siehst du im Spiel jedes Rezept: über einem Gegenstand <b>R</b> drücken. Bei Rohstoffen steht die Herkunft im Tab „Info“ neben den Rezepten, im Tooltip hilft <b>Shift</b>.')}
'''
    page('fortschritt', 'Spielfortschritt', 'Allgemein', fort)

    # ------------------------------------------------------------------ Mod pages
    def modpage(pid, title, ref, intro, facts, specs, extra=''):
        links = ''.join(f'<li><a href="#/{s}">{h.esc(t)}</a></li>' for s, t in specs)
        return page(pid, title, 'Ursprungs-Mods', f'''<h1>{title}</h1>
{h.infobox(title, ref, facts)}
<p class="lead">{intro}</p>{extra}
<h2 id="{pid}--ref">Ausführliche Referenz</h2><ul>{links}</ul>''')

    modpage('mod-ic2', 'IndustrialCraft 2', 'craft:generator',
            'Das Herzstück: das <b>EU-Energienetz</b> mit Kabeln und Spannungsstufen, Generatoren, Speicher, Maschinen, elektrische Werkzeuge, '
            'Rüstung, Kernreaktor, Crops, Weltgenerierung. Nachgebaut 1:1 nach IC2 1.115.231 – inklusive Explosionsalgorithmus.',
            [('Version (Vorlage)', '1.115.231-lf'), ('Energie', 'EU, ganzzahlig, in Paketen'), ('Stufen', 'LV 32 · MV 128 · HV 512 · EV 2048'),
             ('Paket', 'Kreativ-Tab „IndustrialCraft 2“')],
            [('spec-01', '01 – EU-System (Netz, Kabel, Überspannung, Explosionen)'), ('spec-02', '02 – Generatoren'),
             ('spec-03', '03 – Speicher und Transformatoren'), ('spec-04', '04 – Maschinen'), ('spec-05', '05 – Items, Werkzeuge, Rüstung, Crops'),
             ('spec-06', '06 – Kernreaktor'), ('spec-07', '07 – Weltgenerierung'), ('spec-08', '08 – Rezepte'), ('spec-17', '17 – Restliche IC2-Inhalte')],
            f'''<h2 id="ic2--eu">So funktioniert das EU-Netz</h2>
<ul><li>Jede Quelle gibt pro Tick <b>Pakete</b> ab; die <b>Spannung</b> eines Pakets ist seine Größe in EU.</li>
<li>Ist ein Paket größer als die maximale Eingangsspannung eines Empfängers, <b>explodiert</b> der Empfänger (Überspannung).</li>
<li>Kabel verlieren je Block Energie und <b>schmelzen</b>, wenn ein Paket ihre Grenze erreicht; schlecht isolierte Kabel geben Stromschläge.</li>
<li>Transformatoren setzen zwischen den Stufen um (abwärts; mit Redstone aufwärts).</li></ul>
{h.table(['Stufe', 'Max. Paket', 'Speicher', 'Kabel'], [
    ['LV', '32 EU', 'BatBox (40 000 EU)', 'Kupfer (isoliert 0,2 EU/Block), Zinn nur 5 EU'],
    ['MV', '128 EU', 'MFE (600 000 EU)', 'Gold (0,4–0,5 EU/Block)'],
    ['HV', '512 EU', 'MFSU (10 Mio. EU)', 'Glasfaser (bis 512, 0,025 EU/Block)'],
    ['EV', '2048 EU', '–', 'HV-Kabel (0,8–1,0 EU/Block)'],
])}
<h3 id="ic2--maschinen">Standardmaschinen</h3>
{h.table(['Maschine', 'EU/t', 'Ticks', 'EU je Vorgang'], [
    [c('electric_furnace'), '3', '130', '390'], [c('macerator'), '2', '400', '800'], [c('extractor'), '2', '400', '800'],
    [c('compressor'), '2', '400', '800'], [c('recycler'), '1', '45', '45']])}
<p>Upgrades (Overclocker, Energiespeicher, Transformator) passen in die Slots 3–6 der Maschinen.</p>''')

    modpage('mod-gregtech', 'GregTech', 'craft:industrial_blast_furnace',
            'Im Pack hat GregTech viele IC2-Rezepte verschärft. Craft übernimmt diese <b>finalen GT-Rezepte</b> und baut dafür die nötigen '
            'GT-Maschinen 1:1 nach – plus <b>alle GT-Erze</b> der Weltgenerierung und alle Rezepte, die sie erreichbar machen.',
            [('Version (Vorlage)', '2.90'), ('Umfang', 'Maschinen und Materialien für die Pack-Rezepte'), ('Erze', 'alle GT-Erze wie im Pack')],
            [('spec-12', '12 – GregTech-Materialien'), ('spec-13', '13 – GregTech-Maschinen'), ('spec-07', '07 – Weltgenerierung §6 (GT-Erze)'),
             ('spec-08', '08 – Rezepte (GT-Markierungen)')],
            f'''<h2 id="gt--maschinen">Maschinen</h2><p>{c('plate_bending_machine')} {c('industrial_electrolyzer')} {c('industrial_blast_furnace')} {c('assembling_machine')}
{c('automatic_canning_machine')} {c('implosion_compressor')} {c('industrial_centrifuge')} {c('industrial_grinder')} {c('vacuum_freezer')}</p>
<p>Gemeinsame Basis wie im Original: bis zu 16 Upgrades per Rechtsklick, Explosion bei Überspannung, Seitenlogik für Ein-/Ausgabe.</p>''')

    modpage('mod-buildcraft', 'BuildCraft', 'craft:quarry',
            'Das <b>MJ-System</b> mit Engines, die Quarry mit Landmarks, Öl in der Welt, Pumpe, Tank, Raffinerie, Mining Well und die Rohre '
            '(Item-, Flüssigkeits-, Leistungs- und Strukturrohre). MJ und EU bleiben getrennt.',
            [('Version (Vorlage)', '3.4.3'), ('Energie', 'MJ'), ('Nicht enthalten', 'Gates, Drähte, Fassaden'), ('Quarry-Chunkloading', 'max. 25 Chunks')],
            [('spec-09', '09 – BuildCraft (MJ, Engines, Quarry, Öl …)'), ('spec-15', '15 – Rohre')],
            h.table(['Engine', 'Leistung', 'Brennstoff'], [
                ['Redstone-Engine', '0,05 MJ/t', 'Redstone-Signal'], ['Stirling-Engine', '1 MJ/t', 'feste Brennstoffe (1 600 MJ je Kohle)'],
                ['Combustion-Engine', '1 / 3 / 6 MJ/t', 'Lava / Öl / Treibstoff, braucht Kühlung']]) +
            '<p>Engines arbeiten nur mit Redstone-Signal, laufen heiß (Blau → Grün → Gelb → Rot) und explodieren bei Überhitzung.</p>')

    modpage('mod-forestry', 'Forestry', 'craft:electrical_engine',
            'Aus Forestry stammt nur die Technik für die Energieumwandlung: die <b>Electrical Engine</b> (EU → MJ) mit Platinen und Electron Tubes, '
            'dazu Thermionic Fabricator, Carpenter, Lötkolben und Robustes Gehäuse.',
            [('Version (Vorlage)', '2.0.0.11'), ('Umwandlung', '3 EU = 1 MJ'), ('Ledger', 'Energie, Hinweise, Fehlerstatus')],
            [('spec-10', '10 – Energieumwandlung (Teil A: Forestry)')])

    modpage('mod-powerconverters', 'Power Converters', 'craft:energy_bridge',
            'Die <b>Energy Bridge</b> verbindet MJ und EU: Consumer speisen Energie ein, Producer geben sie in der anderen Form wieder ab. '
            '1 MJ = 2,43 EU, Producer geben nur volle Pakete ab. Dazu der Universal Charger.',
            [('Umrechnung', '1 MJ = 2,43 EU'), ('IC2-Stufen', 'LV, MV, HV, EV')],
            [('spec-10', '10 – Energieumwandlung (Teil B: Power Converters)')])
    modpage('mod-ironchests', 'Iron Chests', 'craft:iron_chest',
            'Größere Kisten: <b>Kupfer</b> (45 Slots), <b>Eisen</b> (54), <b>Silber</b> (72), <b>Gold</b> (81) und <b>Diamant</b> (108). '
            'Die <b>Kristallkiste</b> ist durchsichtig und zeigt die acht häufigsten Items drehend im Inneren, die <b>Obsidiankiste</b> übersteht Explosionen. '
            'Mit <b>Upgrades</b> wird eine Kiste samt Inhalt direkt aufgewertet, auch die normale Holzkiste. Wo Eisen gebraucht wird, geht auch Raffiniertes Eisen. '
            'Zahme Katzen setzen sich auch auf diese Kisten; Komparatoren lesen den Füllstand (in 1.4.7 gab es noch keine).',
            [('Kisten', '7'), ('Upgrades', '9'), ('Paket', 'Kreativ-Tab „Iron Chests“')],
            [('spec-22', '22 – Iron Chests')])

    modpage('mod-advancedmachines', 'Advanced Machines', 'craft:rotary_macerator',
            'Das IC2-Addon aus dem Pack: <b>Rotations-Macerator</b>, <b>Singularitätskompressor</b> und <b>Zentrifugalextraktor</b> nutzen '
            'die Rezepte von Macerator, Kompressor und Extraktor, müssen aber erst <b>hochlaufen</b>: Jeder Tick mit Energie und Arbeit '
            '(oder einem Redstone-Signal) erhöht die Drehzahl um 1 bis 7 500 (3 EU/t). Der Fortschritt wächst um Drehzahl/30 je Tick, ein '
            'Vorgang braucht 4 000 – bei voller Drehzahl also nur <b>16 Ticks</b> statt 400 bei 3 EU/t. Hochlaufen dauert gut 6 Minuten '
            '(22 500 EU); ohne Arbeit sinkt die Drehzahl um 4 je Tick, ein Redstone-Signal hält sie oben. Overclocker beschleunigen das '
            'Hochlaufen und heben die Höchstdrehzahl (+500 je Stück), kosten beim Hochlaufen aber 3 · 2<sup>n</sup> EU/t; bei voller '
            'Drehzahl bleibt es bei 3 EU/t.',
            [('Version (Vorlage)', '4.7b'), ('Speicher', '5 000 EU (+5 000 je Speicher-Upgrade)'), ('Eingang', 'LV 32 (×4 je Transformer-Upgrade)'),
             ('Paket', 'Kreativ-Tab „Advanced Machines“')],
            [('spec-25', '25 – Advanced Machines')],
            f'''<h2 id="mod-advancedmachines--zusatz">Zusatzslot des Rotations-Macerators</h2>
<p>Der Slot neben der Eingabe ändert das Ergebnis:</p>
{h.table(['Normales Ergebnis', 'Zusatz', 'Ergebnis', 'reicht für'], [
    [c('iron_dust'), v('minecraft:coal', 'Kohle') + ' / ' + v('minecraft:charcoal', 'Holzkohle'), c('refined_iron_dust'), '128 Vorgänge'],
    [c('copper_dust'), 'etwas, das zu ' + c('tin_dust') + ' gemahlen wird (Zinnerz, Zinnbarren …)', c('bronze_dust'), '4 Vorgänge'],
    [c('coal_dust'), c('water_cell'), c('hydrated_coal_dust') + ' (immer 1)', '8 Vorgänge'],
])}
{h.note('warn', 'Solange ein Zusatz „angebrochen“ ist, verarbeitet der Rotations-Macerator nur passende Eingaben – andere bleiben liegen, bis der Zusatz entnommen wird (wie im Original).')}
<p>Abbau ohne Schraubenschlüssel gibt nur ein Fortgeschrittenes Maschinengehäuse zurück; mit Schraubenschlüssel immer die Maschine.
Redstone im Energieslot füllt den ganzen Speicher auf einmal.</p>''')

    modpage('mod-thermalexpansion', 'Thermal Expansion', 'craft:pulverizer',
            'Thermal Expansion 2.2.2.2 aus dem Pack, komplett mit <b>MJ</b> betrieben: neun Maschinen, zwei Motoren, die '
            '<b>Redstone-Energiezelle</b> (600 000 MJ), Energieleitungen, Liquiducts, der Tragbare Tank und die <b>Tesserakte</b>, '
            'die Energie, Flüssigkeiten und Items über beliebige Entfernungen und Dimensionen schicken. Wo TE eigene Materialien '
            'hätte (Kupfer, Zinn, Silber, Blei, Elektrum …), werden die vorhandenen Craft-Items verwendet; neu sind nur Silber-, '
            'Blei- und Nickelerz (Ferrous Ore), Invar, Holzschnitzel, Sägemehl und Schlacke.',
            [('Version (Vorlage)', '2.2.2.2'), ('Energie', 'MJ (BuildCraft)'), ('Erze', 'Silber, Blei, Nickel'),
             ('Paket', 'Kreativ-Tab „Thermal Expansion“')],
            [('spec-26', '26 – Thermal Expansion')],
            f'''<h2 id="mod-thermalexpansion--maschinen">Maschinen</h2>
<p>Alle Maschinen haben dieselbe Basis: ein MJ-Speicher (1 200 × Höchstleistung), seitlich farbig einstellbare Ein- und Ausgänge
(Reiter „Konfiguration“, Crescent Hammer dreht), Redstone-Steuerung und automatischen Auswurf. Die Rezepte zeigt EMI
(Klick auf den Fortschrittspfeil öffnet die passende Kategorie).</p>
{h.table(['Maschine', 'Macht', 'Energie'], [
    [c('powered_furnace'), 'Ofenrezepte (eigene Liste, sonst jedes Ofenrezept für 160 MJ)', 'MJ'],
    [c('pulverizer'), 'Erze → 2 Staub (+ Nebenprodukt), sonst jedes Macerator-Rezept (400 MJ)', 'MJ'],
    [c('sawmill'), 'Stämme → 6 Bretter + Sägemehl, zerlegt Holzgegenstände', 'MJ'],
    [c('induction_smelter'), 'Legierungen, Erz + Sand → 2 Barren, Erz + Reiche Schlacke → 3 Barren', 'MJ'],
    [c('magma_crucible'), 'schmilzt Stein/Bruchstein/Obsidian/Netherrack zu Lava, Schnee/Eis zu Wasser, Redstone, Enderperlen', 'MJ'],
    [c('liquid_transposer'), 'füllt und leert Behälter (Modusknopf)', 'MJ'],
    [c('glacial_precipitator'), 'Wasser → Schneebälle, Schnee, Eis', 'MJ'],
    [c('igneous_extruder'), 'Lava + Wasser → Bruchstein, Stein, Obsidian', 'kein Strom'],
    [c('aqueous_accumulator'), 'sammelt Wasser (1 000 mB je 40 Ticks bei 2 Wasser-Nachbarn)', 'kein Strom'],
])}
<h2 id="mod-thermalexpansion--energie">Energie und Transport</h2>
<ul><li>{c('steam_engine')} (0,2–2 MJ/t, feste Brennstoffe + Wasser) und {c('magmatic_engine')} (0,4–4 MJ/t, Lava): explodieren nicht,
gehen aber bei vollem Puffer kaputt – der Schraubenschlüssel repariert sie. Brennwerte zeigt EMI.</li>
<li>{c('redstone_energy_cell')}: 600 000 MJ, Senden/Empfangen je 0–100 MJ/t einstellbar; mit Schleichen + Schraubenschlüssel samt Ladung abbauen.</li>
<li>Redstone-Energieleitung (500 MJ je Knoten, 5 % Verlust), Liquiduct, {c('multimeter')} zum Messen.</li>
<li>Tesserakte (Energie, Flüssigkeit, Item): gleiche <b>Frequenz</b> verbindet sie, auch über Dimensionen; öffentlich oder privat. Energie kommt mit 25 % Verlust an.</li></ul>''')

    modpage('mod-enderstorage', 'EnderStorage', 'craft:ender_pouch',
            'EnderStorage 1.4.1.5 aus dem Pack: <b>Ender-Truhe</b>, <b>Enderbeutel</b> und <b>Ender-Tank</b> mit einer '
            '<b>Frequenz aus drei Farben</b>. Alles mit denselben drei Farben teilt sich einen Speicher – über beliebige '
            'Entfernungen und Dimensionen. Truhen und Beutel teilen 27 Plätze, Tanks 16 000 mB einer Flüssigkeit; Truhen und '
            'Tanks gleicher Farbe sind getrennt. Die Vanilla-Endertruhe bleibt daneben normal nutzbar (deutsch heißt die neue '
            '„Ender-Truhe (farbig)“).',
            [('Version (Vorlage)', '1.4.1.5'), ('Frequenzen', '16 × 16 × 16 = 4 096, je global oder persönlich'),
             ('Truhe', '27 Plätze'), ('Tank', '16 000 mB'), ('Paket', 'Kreativ-Tab „Craft: EnderStorage“')],
            [('spec-27', '27 – EnderStorage')],
            f'''<h2 id="mod-enderstorage--farben">Farben einstellen</h2>
<ul><li>Das Grundrezept gibt drei gleiche Farben (Farbe der Wolle in der Mitte oben): Truhe mit {v('minecraft:chest', 'Truhe')},
Tank mit {v('minecraft:cauldron', 'Kessel')} (jeweils Lohenruten, Obsidian, Enderperle); Beutel aus Lohenstaub, Leder, Enderperle und Wolle.</li>
<li><b>Farbstoff auf einen der drei Knöpfe</b> (auf dem Deckel bzw. oben auf dem Tank) ändert genau diese Farbe; Knopf 1 ist links,
von vorne gesehen. In der Werkbank: Farbstoffe in die Zeile über die Truhe/den Tank legen, jede Spalte setzt ihre Farbe.</li>
<li>Der <b>Enderbeutel</b> übernimmt per Schleichen + Rechtsklick die Farben einer Ender-Truhe (nicht vom Tank) und öffnet dann
deren Inhalt von überall.</li></ul>
<h2 id="mod-enderstorage--persoenlich">Persönlich</h2>
<p>Ein <b>Diamant</b> auf das Schloss der Truhe (beim Tank: Ventilknauf vorne) macht sie persönlich – eigener Speicher nur für
dich, das Schloss wird zum Diamantschloss. Schleichen + Rechtsklick mit leerer Hand macht sie wieder global und gibt den Diamanten
zurück.</p>
{h.note('warn', 'Wie im Original prüft das Entsperren nicht, wem die Truhe gehört: jeder kann eine persönliche Truhe so wieder global machen (und an den Diamanten kommen).')}
<h2 id="mod-enderstorage--tank">Ender-Tank</h2>
<ul><li>Eimer und Zellen per Rechtsklick füllen/leeren; Rohre und Maschinen an jeder Seite.</li>
<li><b>Druckventil:</b> Mit Redstone-Signal gibt der Tank je Tick bis zu <b>100 mB an jeden angrenzenden</b> Tank, jedes
Rohr und jede Maschine ab. Klick auf den Ventilknauf kehrt das um (dann Druck ohne Signal). Der Knauf zeigt den Zustand.</li>
<li>Mit Lava oder anderen leuchtenden Flüssigkeiten leuchtet der Tank. Abbauen behält Farben – der Inhalt bleibt im Speicher.</li></ul>''')

    modpage('mod-gravisuite', 'GraviSuite', 'craft:gravi_chestplate',
            'Gravitation Suite 1.8 aus dem Pack: Endgame-Ausrüstung auf IC2-Basis – <b>Gravitations-Brustplatte</b> (Flug wie '
            'im Kreativmodus), <b>Fortgeschrittenes Jetpack</b> und <b>Nano-Brustplatte</b>, <b>Fortgeschrittenes</b> und '
            '<b>Ultimatives LapPack</b>, <b>Fortgeschrittener Bohrer</b> und <b>Kettensäge</b>, <b>Vajra</b>, <b>GraviTool</b> und '
            'der <b>Ultimative Solarhelm</b> (nur Kreativ). Alles läuft mit EU; Rezepte übertragen die Ladung der Zutaten.',
            [('Version (Vorlage)', '1.8'), ('Flug-Taste', 'G (Pack: F)'), ('Gravi-Brustplatte', '10 Mio. EU, 416 EU/t'),
             ('Paket', 'Kreativ-Tab „IndustrialCraft 2“')],
            [('spec-28', '28 – GraviSuite')],
            f'''<h2 id="mod-gravisuite--flug">Fliegen</h2>
<ul><li>{c('gravi_chestplate')}: <b>G</b> schaltet den Flug ein (ab 80 000 EU), 416 EU/t auch am Boden, Boost mit Strg
(2 × 1 248 EU/t). Kein Fallschaden; schützt wie die Quanten-Brustplatte. HUD oben rechts zeigt Status und Ladung.</li>
<li>{c('advanced_jetpack')} / {c('advanced_nano_chestplate')}: <b>G</b> schaltet das Triebwerk, Springen gibt Schub (12 EU/t),
<b>M</b> beim Springen den Schwebemodus. Unter 1 000 EU bricht der Schub stark ein (wie im Original).</li>
<li>Alle Brustteile laden wie ein LapPack gehaltene Werkzeuge bis Stufe 2 nach.</li></ul>
<h2 id="mod-gravisuite--werkzeuge">Werkzeuge</h2>
{h.table(['Werkzeug', 'Wirkung', 'Kosten'], [
    [c('advanced_diamond_drill'), 'wie der Diamantbohrer, Effizienz 35', '160 EU/Block'],
    [c('advanced_chainsaw'), 'wie die Kettensäge, Effizienz 35, auch Wolle/Melone/Kaktus', '100 EU/Block'],
    [c('vajra'), 'baut alles sofort ab; Rechtsklick entfernt einen Block mit Drops (nicht Grundgestein)', '3 333 EU/Block'],
    [c('gravitool'), 'M + Rechtsklick: Hacke, Baumzapfer, Schraubenschlüssel (immer verlustfrei, auch BuildCraft), Schraubendreher', '50–10 000 EU'],
])}
<h2 id="mod-gravisuite--solarhelm">Ultimativer Solarhelm</h2>
<p>{c('ultimate_solar_helmet')} hat wie im Pack kein Rezept. Getragen erzeugt er 512 EU/t bei Sonne und 64 EU/t unter freiem
Himmel und lädt erst die Rüstung, dann das Inventar.</p>''')

    modpage('mod-mps', 'Modular Powersuits', 'craft:power_armor_torso',
            'Modular Powersuits 0.3.2 aus dem Pack („Tech-Rüstung“): eine <b>Power-Rüstung</b> (4 Teile) und ein '
            '<b>Power-Werkzeug</b>, die erst durch <b>Module</b> etwas können. Module baut man am <b>Tinker Table</b> ein und '
            'stellt sie mit Reglern ein – mehr Leistung kostet mehr Energie und Gewicht. Energie wird in Joule gerechnet '
            '(50 J = 1 EU) und kommt aus Batteriemodulen.',
            [('Version (Vorlage)', '0.3.2'), ('Energie', 'Joule, 50 J = 1 EU'), ('Tasten', 'Z runter, K Belegung, Schleichen + Mausrad Modus'),
             ('Kreativ-Tab', 'Craft: Modular Powersuits')],
            [('spec-29', '29 – Modular Powersuits')],
            f'''<h2 id="mod-mps--tisch">Tinker Table</h2>
<ul><li>{c('tinker_table')}: Rechtsklick öffnet die Übersicht aller modularen Teile im Inventar. Modul wählen,
„Einbauen“ verbraucht die angezeigten Bauteile (Kreativ: kostenlos), Regler stellen die Werte ein.</li>
<li>„Ausbauen“ gibt jedes Bauteil mit 90 % Chance zurück; NS-/MS-/HS-Kondensatoren gehen verloren. Batteriemodule mit
Kondensator bringen beim Einbau gleich etwas Ladung mit.</li></ul>
<h2 id="mod-mps--energie">Energie und Gewicht</h2>
<ul><li>Batteriemodule (Basis/Fortgeschritten/Elite) geben Kapazität; alle getragenen Teile und das Werkzeug in der Hand
bilden einen gemeinsamen Vorrat (verbraucht wird von den Stiefeln aufwärts).</li>
<li>Laden in IC2-Speichern und -Ladegeräten wie ein Stufe-1-Gegenstand; dazu Solar (Helm, alle 4 s) und Kinetik (Hose,
je 5 Blöcke).</li>
<li>Über 25 kg Gesamtgewicht wird man langsamer, Jetpack und Sprung werden schwächer.</li></ul>
<h2 id="mod-mps--module">Module</h2>
{h.table(['Teil', 'Module'], [
    ['alle Rüstungsteile', 'Eisen-/Diamantpanzerung, Energieschild (max. Schutz 6 je Teil), Batterien, Farbe, Durchsichtig, Citizen-Joe-Stil'],
    ['Helm', 'Nachtsicht, Flugsteuerung, Auto-Feeder (isst aus dem Inventar), Wasser-Elektrolyseur (Luft), Solargenerator'],
    ['Brust', 'Jetpack, Gleiter, Fallschirm, Aktive Tarnung (Unsichtbarkeit)'],
    ['Hose', 'Sprint-/Sprung-/Schwimmhilfe, Stufenhilfe, Kinetikgenerator'],
    ['Stiefel', 'Düsenstiefel, Stoßdämpfer'],
    ['Werkzeug', 'Spitzhacke, Schaufel, Axt, Diamantbohrer-Upgrade, Aqua Affinity, Nahkampfhilfe, Railgun, Blink Drive, Plasmakanone, In-Place Assembler'],
])}
{h.note('info', 'Wie im Pack baut schon das Spitzhacken-Modul Obsidian ab; das Diamantbohrer-Upgrade wirkt nur ohne Spitzhacke und ist dann fast nutzlos.')}
<h2 id="mod-mps--tasten">Tasten</h2>
<ul><li><b>Z</b>: runter mit Flugsteuerung (Jetpack/Düsenstiefel schweben ohne Tasten).</li>
<li><b>K</b>: Tastenbelegung – „Neu“, Taste drücken, Module per Ziehen auf die Taste legen; jeder Druck schaltet sie
abwechselnd aus/an (erster Druck: aus).</li>
<li><b>Schleichen + Mausrad</b> mit dem Power-Werkzeug: Modus wählen (Symbole über der Hotbar), Rechtsklick führt ihn aus.</li></ul>
<p>{c('component_carbon_myofiber')} und {c('component_myofiber_gel')} haben wie im Pack weder Rezept noch Verwendung.</p>''')

    # ------------------------------------------------------------------ Items
    page('items', 'Gegenstände & Blöcke', 'Allgemein', f'''<h1>Gegenstände & Blöcke</h1>
<p class="lead">Alle {stats['items']} registrierten Blöcke und Items von Craft mit deutschem und englischem Namen. Jeder Eintrag hat eine eigene Seite
mit allen Rezepten (Herstellung, Verwendung, Maschinenrezepte – jede Zutat ist verlinkt), Registry-ID, <code>/give</code>-Befehl und den Seiten, die ihn erwähnen. {NEW} und {CHG} markieren neue bzw. geänderte Inhalte.</p>
<div id="gallery"></div>''')

    return P
