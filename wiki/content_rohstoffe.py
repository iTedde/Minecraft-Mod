"""Wiki page "Rohstoffe & Erze" (spec 24 §2.3, phase 23.5).

Ore values from docs/spec/07 §1 and §6 and spec 18 (mining dimension: overworld ores in double amount); the per-item
texts come from the generated 'source.*' lang keys (dev.craft.data.SourceTexts), so they are maintained in one place.
"""
import re


def pages(h, stats):
    c = h.chip
    v = h.vchip
    S = h.SOURCES

    def src(iid):
        return h.esc(S.get(iid if ':' in iid else 'craft:' + iid, ''))

    # ------------------------------------------------------------------ ores
    ore_head = ['Erz', 'Wo', 'Höhe y', 'Menge je Chunk', 'Werkzeug', 'Drop (ohne Behutsamkeit)', 'Mining-Dim.']
    ow = [
        (v('minecraft:copper_ore', 'Kupfererz'), 'Oberwelt', '10–68', '≈ 15 Adern à 10', 'Stein', 'Rohkupfer', 'ja, doppelt'),
        (c('tin_ore'), 'Oberwelt', '0–39', '≈ 25 Adern à 6', 'Stein', 'Erz', 'ja, doppelt'),
        (c('uranium_ore'), 'Oberwelt', '0–64', '≈ 20 Adern à 3', 'Eisen', c('uranium_drop'), 'ja, doppelt'),
        (c('galena_ore'), 'Oberwelt', '0–31', '1 Ader à 16', 'Stein', 'Erz', 'ja, doppelt'),
        (c('iridium_ore_block'), 'Oberwelt', '0–98', 'Einzelblock in ≈ jedem 5. Chunk', 'Diamant', c('iridium_ore'), 'ja, doppelt'),
        (c('ruby_ore'), 'Oberwelt: Wüste, Tafelberg, Savanne', '0–31', '8 Einzelblöcke', 'Eisen',
         c('ruby') + ', selten ' + c('red_garnet'), 'ja, überall'),
        (c('sapphire_ore'), 'Oberwelt: Ozean, Strand, Mangrovensumpf', '0–31', '8 Einzelblöcke', 'Eisen',
         c('sapphire') + ', selten ' + c('green_sapphire'), 'ja, überall'),
        (c('bauxite_ore'), 'Oberwelt: Ebenen, Wiese, Wälder, Kirschblütenhain', '32–79', '2 Adern à 16', 'Stein', 'Erz', 'ja, überall (bis y 64)'),
    ]
    nether = [
        (c('pyrite_ore'), 'Nether', '0–63', '1–16 Adern', 'Stein', '2+ ' + c('pyrite_dust'), 'nein'),
        (c('cinnabar_ore'), 'Nether', '64–127', '1–8 Adern', 'Eisen', '2+ ' + c('cinnabar_dust') + ', oft Redstone', 'nein'),
        (c('sphalerite_ore'), 'Nether', '32–95', '1–16 Adern', 'Stein',
         '2+ ' + c('sphalerite_dust') + ', manchmal ' + c('zinc_dust') + ' / ' + c('yellow_garnet'), 'nein'),
    ]
    end = [
        (c('tungstate_ore'), 'End (+ Asteroiden)', '0–127', '4 Adern à 16', 'Eisen', 'Erz', 'nein'),
        (c('sheldonite_ore'), 'End (+ Asteroiden)', '0–127', '1 Ader à 4', 'Diamant', 'Erz', 'nein'),
        (c('olivine_ore'), 'End (+ Asteroiden)', '0–127', '5 Adern à 8', 'Diamant', c('olivine'), 'nein'),
        (c('sodalite_ore'), 'End (+ große Asteroiden)', '0–127', '8 Adern à 16', 'Eisen',
         '6+ ' + c('sodalite_dust') + ', oft ' + c('aluminium_dust'), 'nein'),
    ]

    # ------------------------------------------------------------------ hard-to-get materials
    hard = ['nickel_dust', 'zinc_dust', 'silver_dust', 'lead_dust', 'platinum_dust', 'aluminium_dust', 'titanium_dust',
            'chrome_dust', 'manganese_dust', 'tungsten_dust', 'iridium_ore', 'plutonium_dust', 'thorium_dust',
            'sodium_cell', 'potassium_cell', 'silicon_cell', 'mercury_cell', 'helium_cell', 'industrial_diamond']
    hard_rows = [(c(i), src(i)) for i in hard if 'craft:' + i in S]

    # ------------------------------------------------------------------ all texts, grouped
    groups = [
        ('Erze', lambda i: i.endswith('_ore') or i.endswith('_ore_block') or i.endswith('copper_ore')),
        ('Kleine Stäube', lambda i: i.startswith('small_') and i.endswith('_dust')),
        ('Stäube', lambda i: i.endswith('_dust')),
        ('Barren und Nuggets', lambda i: i.endswith('_ingot') or i.endswith('_nugget')),
        ('Platten', lambda i: i.endswith('_plate')),
        ('Zellen, Brennstäbe, Kühlzellen', lambda i: '_cell' in i),
        ('Edelsteine', lambda i: i in ('ruby', 'sapphire', 'green_sapphire', 'olivine', 'red_garnet', 'yellow_garnet',
                                       'industrial_diamond')),
        ('Weltgenerierung, Drops und Sonstiges', lambda i: True),
    ]
    buckets = {g: [] for g, _ in groups}
    for ref in S:
        ns, iid = ref.split(':', 1)
        g = next(g for g, test in groups if test(iid))
        it = h.ITEMS.get(iid) if ns == 'craft' else None
        name = it['de'] if it else iid.replace('_', ' ').capitalize()
        chip = c(iid) if it else v(ref, {'copper_ore': 'Kupfererz', 'deepslate_copper_ore': 'Tiefenschiefer-Kupfererz',
                                          'raw_copper': 'Rohkupfer'}.get(iid, name))
        buckets[g].append((name.lower(), chip, h.esc(S[ref])))
    order = ['Erze', 'Weltgenerierung, Drops und Sonstiges', 'Stäube', 'Kleine Stäube', 'Barren und Nuggets', 'Platten',
             'Edelsteine', 'Zellen, Brennstäbe, Kühlzellen']
    all_html = ''
    for g in order:
        rows = sorted(buckets[g])
        if not rows:
            continue
        all_html += (f'<details class="faq"><summary>{g} ({len(rows)})</summary><div>'
                     + h.table(['Gegenstand', 'Herkunft'], [(r[1], r[2]) for r in rows]) + '</div></details>')

    body = f'''<h1>Rohstoffe & Erze</h1>
<p class="lead">Wo kommt was her? Diese Seite listet alle Erze mit Fundort und alle Rohstoffe mit ihrem besten Weg. Viele
GregTech-Metalle (Nickel, Zink, Silber, Platin, Titan …) haben <b>kein eigenes Erz</b> und entstehen nur als Nebenprodukt
von Industrial Grinder, Elektrolyseur oder Zentrifuge – oft erst als <b>kleiner Staub</b> (4 kleine → 1 Staub).</p>
{h.note('tip', 'Im Spiel: über einem Rohstoff <b>R</b> drücken – EMI zeigt zuerst die Seite <b>„Herkunft“</b>, danach die Rezepte. '
               'Im Tooltip zeigt <b>Shift</b> denselben Text. Jede Gegenstandsseite dieses Wikis hat den Abschnitt „Herkunft“.')}
<div class="tocpos"></div>

<h2 id="ro--erze">Erze</h2>
<p>„Menge je Chunk“ gilt für die Oberwelt; in der <a href="#/mining-dimension">Mining-Dimension</a> gibt es alle Oberwelt-Erze in
<b>doppelter</b> Menge, Rubin, Saphir und Bauxit dort in jedem Gebiet. Nether- und End-Erze gibt es nur in Nether und End.
Mit <b>Behutsamkeit</b> droppen alle Erze sich selbst – das lohnt sich für den {c('industrial_grinder')}, der aus dem Erzblock die
meisten Nebenprodukte holt. Die Tiefenschiefer-Varianten verhalten sich gleich.</p>
<h3 id="ro--ow">Oberwelt</h3>
{h.table(ore_head, ow)}
<h3 id="ro--nether">Nether</h3>
{h.table(ore_head, nether)}
<h3 id="ro--end">End</h3>
{h.table(ore_head, end)}
<p>Die End-Erze liegen im Endstein der Inseln und zusätzlich in seltenen, frei schwebenden Endstein-Asteroiden (etwa 1 % der
Chunks). Vanilla-Erze (Kohle, Eisen, Gold, Redstone, Lapis, Diamant, Smaragd) sind unverändert.</p>

<h2 id="ro--welt">Weitere Quellen aus der Welt</h2>
{h.table(['Rohstoff', 'Herkunft'], [(c(i), src(i)) for i in ('rubber_wood', 'resin', 'oil', 'scrap', 'uu_matter',
                                                            'coffee_beans', 'hops', 'terra_wart')])}

<h2 id="ro--maschinen">Welche Maschine schaltet was frei?</h2>
{h.table(['Maschine', 'liefert vor allem'], [
    (c('macerator'), '2 Staub je Erz oder Rohmetall (doppelte Ausbeute); Stäube aus Barren, Steinen und Edelsteinen.'),
    (c('industrial_grinder'), 'Aus Erzblöcken (mit Behutsamkeit abbauen) Staub <b>plus Nebenprodukte</b>: kleiner Nickel-, Zink-, '
                              'Gold-, Platinstaub, Schwefel; mit Quecksilberzelle Silber und mehr Platin.'),
    (c('industrial_electrolyzer'), 'Zerlegt Stäube in Elemente: Aluminium, Titan (aus Bauxit), Chrom, Mangan, Blei/Silber '
                                   '(aus Galena) und die Gaszellen (Wasserstoff, Natrium, Kalium, Silizium …).'),
    (c('industrial_centrifuge'), 'Trennt Stäube, Zellen und Essen: kleine Metallstäube aus Eisen/Gold/Kupfer/Zinn, '
                                 'Helium aus Glowstone, Deuterium/Tritium, Methan, Uran → Plutonium/Thorium.'),
    (c('industrial_blast_furnace'), 'Stahl, Aluminium-, Titan-, Chrom-, Wolfram- und Wolframstahlbarren, raffiniertes Eisen, '
                                    'Siliziumplatte (Hitze je nach Rezept bis 3000 K).'),
    (c('implosion_compressor'), 'Edelsteine aus Staub (Rubin, Saphir, Olivin, Granat, Industriediamant) und die Iridiumplatte.'),
    (c('mass_fabricator'), 'UU-Materie: daraus fast jeder Rohstoff, auch Iridium.'),
])}

<h2 id="ro--schwer">Schwer zu findende Materialien</h2>
<p>Die Materialien, nach denen am häufigsten gefragt wird – mit dem vollständigen Herkunftstext:</p>
{h.table(['Material', 'Herkunft'], hard_rows)}

<h2 id="ro--alle">Alle Herkunftstexte</h2>
<p>Alle {len(S)} Einträge, nach Art gruppiert (zum Aufklappen). Jeder Name führt zur Gegenstandsseite mit Rezepten und
Verwendungen.</p>
{all_html}
'''
    return [{'id': 'rohstoffe', 'title': 'Rohstoffe & Erze', 'group': 'Anleitungen', 'html': body}]
