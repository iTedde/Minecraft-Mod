"""Builds Craft-Wiki.html: a self-contained, offline wiki for the Craft mod.

Sources: Minecraft-Mod/docs (Markdown), CLAUDE.md, lang files, item models/textures,
vanilla textures from the NeoForge client-extra jar, plus hand-written pages in content.py.
Run:  python build_wiki.py
"""
import base64
import html
import io
import json
import os
import re
import subprocess
import sys
import zipfile

import markdown
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.abspath(os.path.join(HERE, '..'))
DOCS = os.path.join(MOD, 'docs')
ASSETS_MAIN = os.path.join(MOD, 'src', 'main', 'resources', 'assets', 'craft')
ASSETS_GEN = os.path.join(MOD, 'src', 'generated', 'resources', 'assets', 'craft')
JAVA = os.path.join(MOD, 'src', 'main', 'java', 'dev', 'craft')
VANILLA_JAR = os.path.join(MOD, 'build', 'moddev', 'artifacts',
                           'neoforge-21.1.259-client-extra-aka-minecraft-resources.jar')
OUT = os.path.join(HERE, 'Craft-Wiki.html')

sys.path.insert(0, HERE)


def read(path):
    with open(path, encoding='utf-8') as f:
        return f.read()


# ---------------------------------------------------------------- lang + items

LANG_DE = json.loads(read(os.path.join(ASSETS_GEN, 'lang', 'de_de.json')))
LANG_EN = json.loads(read(os.path.join(ASSETS_GEN, 'lang', 'en_us.json')))
# where the raw materials come from (spec 24): 'source.<ns>.<path>' -> text, keyed by item ref
SOURCES = {k[7:].replace('.', ':', 1): v for k, v in LANG_DE.items() if k.startswith('source.')}
# items without recipe and without a source text (no raw materials: placed blocks, holo items …); wiki only
WIKI_NOTES = {
    'craft:booze_mug': 'Steinkrug per Rechtsklick an einem Fass mit fertigem Bier oder Rum füllen.',
    'craft:crop_seed_bag': 'Gewachsene Pflanze auf einem Crop-Stab pflücken (Linksklick) → mit etwas Glück ein Saatbeutel; '
                           'im Cropnalyzer auswerten. Nicht im Kreativ-Inventar (wie IC2).',
    'craft:construction_foam_wall': 'Entsteht, wenn gesetzter Bauschaum aushärtet; lässt sich mit Farbstoff einfärben. Kein Item.',
    'craft:dynamite_stick': 'Gesetztes Dynamit (Block). Das Item heißt Dynamit und wird in der Werkbank hergestellt.',
    'craft:dynamite_stick_with_remote': 'Gesetztes Dynamit, das mit einem Fernzünder verbunden ist (Block).',
    'craft:frame': 'Rahmen der Quarry: setzt sie selbst um das Abbaufeld; droppt nichts.',
    'craft:ic2_mining_tip': 'Spitze des Bergbaurohrs, die Miner und IC2-Pumpe setzen; droppt ein Bergbaurohr. Kein Item.',
    'craft:mining_pipe': 'Rohr der Mining Well und der BuildCraft-Pumpe; wird von ihnen gesetzt und droppt nichts.',
    'craft:mining_portal': 'Portalfläche: Rahmen aus Glattem Stein bauen und mit dem Feuerzeug zünden.',
    'craft:nightvision_goggles': 'Kein Rezept – auch im Pack nicht (IC2 1.115 hat keins). Nur im Kreativmodus.',
    'craft:plan': 'Projekttisch: Rezept auslegen, leeren Bauplan einlegen und den Bauplan entnehmen.',
    'craft:resin_sheet': 'Harz per Rechtsklick auf die Oberseite eines festen Blocks setzen. Kein eigenes Item.',
    'craft:water': 'Anzeige-Item für Wasser im Tank des Elektrolyseurs; nicht erhältlich.',
    'craft:lava': 'Anzeige-Item für Lava im Tank der Zentrifuge; nicht erhältlich.',
}

GROUPS = [
    ('ironchest', 'Iron Chests'),
    ('powerplants', 'Kraftwerke (neu)'),
    ('mining', 'Mining-Dimension (neu)'),
    ('compactsolars', 'Compact Solars'),
    ('powerconverters', 'Power Converters'),
    ('forestry', 'Forestry'),
    ('buildcraft', 'BuildCraft'),
    ('gregtech', 'GregTech'),
    ('ic2', 'IndustrialCraft 2'),
]
GROUP_ORDER = ['IndustrialCraft 2', 'GregTech', 'BuildCraft', 'Forestry', 'Power Converters',
               'Compact Solars', 'Kraftwerke (neu)', 'Mining-Dimension (neu)', 'Iron Chests', 'Sonstiges']

# items whose behaviour was changed on purpose or which are new content
CHANGED = {
    'lv_solar_array': 'solar-arrays', 'mv_solar_array': 'solar-arrays', 'hv_solar_array': 'solar-arrays',
    'water_mill': 'wassermuehle', 'wind_mill': 'windmuehle',
}
NEW_PAGES = {
    'coke': 'koksofen', 'coke_block': 'koksofen', 'coke_oven': 'koksofen', 'coke_oven_bricks': 'koksofen',
    'boiler_casing': 'kohlekraftwerk', 'boiler_firebox': 'kohlekraftwerk', 'steam_turbine': 'kohlekraftwerk',
    'turbine_casing': 'kohlekraftwerk', 'turbine_rotor': 'kohlekraftwerk',
    'water_turbine': 'wasserturbine', 'water_turbine_casing': 'wasserturbine',
    'mining_portal': 'mining-dimension',
}


GUIDE_PAGES = {
    'quarry': 'quarry', 'landmark': 'quarry', 'frame': 'quarry', 'redstone_engine': 'quarry', 'stirling_engine': 'quarry',
    'combustion_engine': 'quarry', 'pump': 'quarry', 'tank': 'quarry', 'refinery': 'quarry', 'mining_well': 'quarry',
    'oil': 'quarry', 'oil_bucket': 'quarry', 'fuel_bucket': 'quarry', 'bc_wrench': 'quarry',
    'nuclear_reactor': 'reaktor', 'reactor_chamber': 'reaktor',
    'electrical_engine': 'umwandlung', 'energy_bridge': 'umwandlung', 'universal_charger': 'umwandlung',
    'bc_consumer': 'umwandlung', 'bc_producer': 'umwandlung',
    'electric_furnace': 'ic2-maschinen', 'macerator': 'ic2-maschinen', 'extractor': 'ic2-maschinen',
    'compressor': 'ic2-maschinen', 'recycler': 'ic2-maschinen', 'overclocker_upgrade': 'ic2-maschinen',
    'transformer_upgrade': 'ic2-maschinen', 'energy_storage_upgrade': 'ic2-maschinen',
    'batbox': 'strom', 'mfe': 'strom', 'mfsu': 'strom', 'lv_transformer': 'strom', 'mv_transformer': 'strom', 'hv_transformer': 'strom',
}
GUIDE_PATTERNS = [
    (r'_cable$', 'strom'), (r'pipe', 'quarry'), (r'^ic2_(lv|mv|hv|ev)_', 'umwandlung'), (r'electron_tube$|circuit_board$', 'umwandlung'),
    (r'uranium_cell$|heat_vent$|heat_exchanger$|coolant_cell|reactor_plating$|neutron_reflector$|condensator$', 'reaktor'),
    (r'machine_casing$|heating_coil$|^industrial_|^plate_bending|^assembling|^automatic_canning|^implosion|^vacuum_freezer', 'gregtech-maschinen'),
]


def guide_for(iid):
    if iid in GUIDE_PAGES:
        return GUIDE_PAGES[iid]
    return next((g for pat, g in GUIDE_PATTERNS if re.search(pat, iid)), '')


def registry_texts():
    out = []
    for pkg, label in GROUPS:
        texts, alltexts = [], []
        for dirpath, _, files in os.walk(os.path.join(JAVA, pkg)):
            for fn in files:
                if fn.endswith('.java') and 'GameTests' not in fn:
                    t = read(os.path.join(dirpath, fn))
                    alltexts.append(t)
                    if re.search(r'(Blocks|Items)\.java$', fn) or 'DeferredRegister' in t:
                        texts.append(t)
        out.append((label, '\n'.join(texts), '\n'.join(alltexts)))
    return out


SUFFIX_GROUPS = [
    (r'_chest$|^(iron|gold|copper|silver|wood|diamond)_[a-z]+_upgrade$', 'Iron Chests'),
    (r'_electron_tube$|circuit_board$|^soldering|^sturdy', 'Forestry'),
    (r'_pipe$|^pipe_|engine$|^landmark|_gear$', 'BuildCraft'),
    (r'_cable$|^painter|^crop|_cell$|_upgrade$|_o_mat$', 'IndustrialCraft 2'),
    (r'_ore(_block)?$|_dust$|_plate$|_ingot$|_gem$|_nugget$', 'GregTech'),
]


def collect_items():
    regs = registry_texts()
    items = {}
    for key, name in LANG_DE.items():
        m = re.fullmatch(r'(block|item)\.craft\.([a-z0-9_]+)', key)
        if not m:
            continue
        kind, iid = m.groups()
        if iid in items:
            continue
        group = 'Sonstiges'
        enum_re = re.compile(r'\b' + iid.upper() + r'\s*\(')
        for test in (lambda r: f'"{iid}"' in r[1], lambda r: enum_re.search(r[2]), lambda r: f'"{iid}"' in r[2]):
            hit = next((r[0] for r in regs if test(r)), None)
            if hit:
                group = hit
                break
        if group == 'Sonstiges':
            group = next((g for pat, g in SUFFIX_GROUPS if re.search(pat, iid)), group)
        items[iid] = {
            'id': iid, 'kind': 'Block' if kind == 'block' else 'Item',
            'de': name, 'en': LANG_EN.get(key, name), 'group': group,
        }
    return items


# ---------------------------------------------------------------- icons

VJAR = zipfile.ZipFile(VANILLA_JAR) if os.path.exists(VANILLA_JAR) else None


def load_res(ns, kind, path):
    """kind = 'models' | 'textures'; returns bytes or None."""
    ext = '.json' if kind == 'models' else '.png'
    if ns == 'craft':
        for base in (ASSETS_GEN, ASSETS_MAIN):
            p = os.path.join(base, kind, *path.split('/')) + ext
            if os.path.exists(p):
                with open(p, 'rb') as f:
                    return f.read()
        return None
    if VJAR is None:
        return None
    try:
        return VJAR.read(f'assets/{ns}/{kind}/{path}{ext}')
    except KeyError:
        return None


def split_ref(ref, default_ns='minecraft'):
    if ':' in ref:
        ns, p = ref.split(':', 1)
    else:
        ns, p = default_ns, ref
    return ns, p


TEX_PREF = ['layer0', 'front', 'north', 'all', 'side', 'top', 'texture', 'cross', 'end', 'particle']


def model_textures(ns, path, depth=0):
    if depth > 8:
        return {}
    data = load_res(ns, 'models', path)
    if data is None:
        return {}
    m = json.loads(data)
    tex = {}
    if 'parent' in m:
        pns, pp = split_ref(m['parent'])
        tex.update(model_textures(pns, pp, depth + 1))
    tex.update(m.get('textures', {}))
    return tex


def texture_for_model(ns, path):
    tex = model_textures(ns, path)
    if not tex:
        return None

    def resolve(v, n=0):
        while isinstance(v, str) and v.startswith('#') and n < 10:
            v = tex.get(v[1:])
            n += 1
        return v

    for k in TEX_PREF:
        if k in tex:
            v = resolve(tex[k])
            if v and not v.startswith('#'):
                return v
    for v in tex.values():
        v = resolve(v)
        if v and not v.startswith('#'):
            return v
    return None


ICON_CACHE = {}


def png_data_uri(ns, tpath):
    data = load_res(ns, 'textures', tpath)
    if data is None:
        return None
    img = Image.open(io.BytesIO(data)).convert('RGBA')
    w, h = img.size
    if h > w:  # animated strip: first frame
        img = img.crop((0, 0, w, w))
    if img.size[0] > 32:
        img = img.resize((32, 32), Image.NEAREST)
    buf = io.BytesIO()
    img.save(buf, 'PNG', optimize=True)
    return 'data:image/png;base64,' + base64.b64encode(buf.getvalue()).decode()


def icon(ref):
    """ref like 'craft:coke' or 'minecraft:brick' -> data uri or None."""
    if ref in ICON_CACHE:
        return ICON_CACHE[ref]
    ns, p = split_ref(ref, 'craft')
    uri = None
    t = texture_for_model(ns, 'item/' + p)
    if t is None:
        t = texture_for_model(ns, 'block/' + p)
    if t:
        tns, tp = split_ref(t)
        uri = png_data_uri(tns, tp)
    if uri is None:
        for sub in ('item/', 'block/'):
            uri = png_data_uri(ns, sub + p)
            if uri:
                break
    ICON_CACHE[ref] = uri
    return uri


def texture(ref):
    """direct texture ref like 'craft:block/xyz'."""
    key = 'tex:' + ref
    if key not in ICON_CACHE:
        ns, p = split_ref(ref, 'craft')
        ICON_CACHE[key] = png_data_uri(ns, p)
    return ICON_CACHE[key]


# ---------------------------------------------------------------- markdown docs

DOC_PAGES = [
    # (page id, file relative to MOD, nav group, nav title override)
    ('projekt', 'CLAUDE.md', 'Projekt', 'Projektgrundlagen & Phasenplan'),
    ('status', 'docs/STATUS.md', 'Projekt', 'Entwicklungsprotokoll'),
    ('testplan', 'docs/testplan.md', 'Projekt', 'Testplan'),
    ('abweichungen', 'docs/abweichungen.md', 'Änderungen (Volltext)', 'Alle Abweichungen (Tabelle)'),
    ('balancing', 'docs/balancing.md', 'Änderungen (Volltext)', 'Energiebalancing (Volltext)'),
    ('architektur', 'docs/architektur.md', 'Technische Referenz', 'Code-Architektur'),
    ('spec-index', 'docs/spec/README.md', 'Technische Referenz', 'Spezifikation – Übersicht'),
]
for fn in sorted(os.listdir(os.path.join(DOCS, 'spec'))):
    m = re.match(r'(\d\d)-.*\.md$', fn)
    if m:
        DOC_PAGES.append((f'spec-{m.group(1)}', f'docs/spec/{fn}', 'Technische Referenz', None))

FILE_TO_PAGE = {}
for pid, rel, _, _ in DOC_PAGES:
    FILE_TO_PAGE[os.path.basename(rel)] = pid
    FILE_TO_PAGE[rel] = pid
    FILE_TO_PAGE[rel.replace('docs/', '')] = pid


def preprocess_md(text):
    lines = text.split('\n')
    out = []
    in_code = False
    for line in lines:
        s = line.lstrip()
        if s.startswith('```'):
            in_code = not in_code
        if not in_code and out:
            prev = out[-1]
            prev_s = prev.strip()
            is_list = re.match(r'([-*+]|\d+\.)\s', s) is not None and line == s
            is_table = s.startswith('|')
            prev_list = re.match(r'\s*([-*+]|\d+\.)\s', prev) is not None or prev.startswith('  ')
            if prev_s and is_list and not prev_list and not prev_s.startswith('|'):
                out.append('')
            elif prev_s and is_table and not prev_s.startswith('|'):
                out.append('')
            elif prev_s.startswith('|') and prev_s and not is_table and s and not s.startswith('#'):
                out.append('')
        out.append(line)
    return '\n'.join(out)


MD = markdown.Markdown(extensions=['tables', 'fenced_code', 'sane_lists', 'mdx_truly_sane_lists'],
                       extension_configs={'mdx_truly_sane_lists': {'nested_indent': 2}})


def slug(s):
    s = re.sub(r'<[^>]+>', '', s).lower()
    s = (s.replace('ä', 'ae').replace('ö', 'oe').replace('ü', 'ue').replace('ß', 'ss'))
    s = re.sub(r'[^a-z0-9]+', '-', s).strip('-')
    return s or 'abschnitt'


def render_md(pid, text, items):
    MD.reset()
    body = MD.convert(preprocess_md(text))
    title_m = re.search(r'<h1>(.*?)</h1>', body)
    title = re.sub(r'<[^>]+>', '', title_m.group(1)) if title_m else pid
    body = re.sub(r'<h1>.*?</h1>', '', body, count=1)
    used = set()

    def head(m):
        lvl, inner = m.group(1), m.group(2)
        base = f'{pid}--{slug(inner)}'
        hid, n = base, 2
        while hid in used:
            hid = f'{base}-{n}'
            n += 1
        used.add(hid)
        return f'<h{lvl} id="{hid}">{inner}</h{lvl}>'

    body = re.sub(r'<h([2-4])>(.*?)</h\1>', head, body)

    # doc cross links inside code spans
    def code(m):
        c = m.group(1)
        raw = html.unescape(c)
        fm = re.match(r'^((?:docs/)?(?:spec/)?[\w.-]+\.md)(.*)$', raw)
        if fm and fm.group(1) in FILE_TO_PAGE:
            return f'<a class="doclink" href="#/{FILE_TO_PAGE[fm.group(1)]}"><code>{c}</code></a>'
        im = re.fullmatch(r'craft:([a-z0-9_]+)', raw)
        if im and im.group(1) in items:
            return chip(im.group(1), items, label=c, code=True)
        return m.group(0)

    body = re.sub(r'<code>(.*?)</code>', code, body)
    body = body.replace('<table>', '<div class="tablewrap"><table class="wikitable">').replace(
        '</table>', '</table></div>')
    return title, body


# ---------------------------------------------------------------- html helpers (also used by content.py)

ITEMS = {}


def esc(s):
    return html.escape(str(s), quote=True)


def chip(iid, items=None, label=None, code=False):
    items = items or ITEMS
    it = items.get(iid)
    src = icon('craft:' + iid)
    img = f'<img src="{src}" alt="">' if src else '<span class="noicon"></span>'
    text = label if label is not None else esc(it['de'] if it else iid)
    if code:
        text = f'<code>{text}</code>'
    return f'<a class="chip" href="#/item/{iid}">{img}{text}</a>'


def vchip(ref, name):
    src = icon(ref)
    img = f'<img src="{src}" alt="">' if src else '<span class="noicon"></span>'
    return f'<span class="chip vanilla">{img}{esc(name)}</span>'


def slot(ref, name, count=None, big=False):
    if ref is None:
        return '<span class="slot"></span>'
    src = icon(ref) if not ref.startswith('tex:') else texture(ref[4:])
    img = f'<img src="{src}" alt="">' if src else f'<span class="slotlabel">{esc(name[:3])}</span>'
    cnt = f'<span class="count">{count}</span>' if count and count > 1 else ''
    link_open, link_close = '', ''
    if ref.startswith('craft:'):
        link_open, link_close = f'<a href="#/item/{ref[6:]}">', '</a>'
    cls = 'slot big' if big else 'slot'
    return f'<span class="{cls}" title="{esc(name)}">{link_open}{img}{cnt}{link_close}</span>'


def name_of(ref, given=None):
    if given:
        return given
    if ref.startswith('craft:'):
        it = ITEMS.get(ref[6:])
        return it['de'] if it else ref
    return ref


def recipe(rows, key, out, count=1, title=None, shapeless=False):
    """rows: list of strings, key: char -> (ref, name|None); out: (ref, name|None)."""
    cells = []
    width = 3
    for r in range(3):
        row = rows[r] if r < len(rows) else ''
        for c in range(width):
            ch = row[c] if c < len(row) else ' '
            if ch == ' ' or ch not in key:
                cells.append(slot(None, ''))
            else:
                ref, nm = key[ch]
                cells.append(slot(ref, name_of(ref, nm)))
    oref, onm = out
    legend = ''.join(
        f'<li><b>{esc(ch)}</b> {chip(ref[6:]) if ref.startswith("craft:") else vchip(ref, nm or ref)}</li>'
        for ch, (ref, nm) in key.items())
    t = f'<div class="rtitle">{esc(title)}</div>' if title else ''
    sl = '<div class="shapeless">formlos</div>' if shapeless else ''
    return (f'<figure class="recipe">{t}<div class="craftui"><div class="grid3">{"".join(cells)}</div>'
            f'<div class="arrow">➜</div>{slot(oref, name_of(oref, onm), count, big=True)}</div>{sl}'
            f'<ul class="rkey">{legend}</ul></figure>')


def layer(rows, key, caption, cell=34, labels=None):
    """Structure layer. rows: list of strings, key: char -> (ref or None for air/color, name, css class).
    labels: optional text per row, shown left of the row (e.g. heights in a side view)."""
    cols = max(len(r) for r in rows)
    cells = []
    for i, row in enumerate(rows):
        if labels:
            cells.append(f'<span class="rlabel">{labels[i]}</span>')
        for c in range(cols):
            ch = row[c] if c < len(row) else ' '
            ref, nm, cls = key.get(ch, (None, 'Luft', 'air'))
            if ref:
                src = texture(ref[4:]) if ref.startswith('tex:') else icon(ref)
                inner = f'<img src="{src}" alt="">' if src else ''
            else:
                inner = ''
            cells.append(f'<span class="cell {cls}" title="{esc(nm)}">{inner}</span>')
    lead = 'auto ' if labels else ''
    return (f'<figure class="layer"><div class="lgrid" style="grid-template-columns:{lead}repeat({cols},{cell}px)">'
            f'{"".join(cells)}</div><figcaption>{caption}</figcaption></figure>')


def legend(key):
    seen = []
    for ch, (ref, nm, cls) in key.items():
        if nm in [s[1] for s in seen]:
            continue
        seen.append((ref, nm, cls))
    lis = []
    for ref, nm, cls in seen:
        src = (texture(ref[4:]) if ref and ref.startswith('tex:') else icon(ref)) if ref else None
        inner = f'<img src="{src}" alt="">' if src else ''
        lis.append(f'<li><span class="cell {cls}">{inner}</span>{esc(nm)}</li>')
    return f'<ul class="slegend">{"".join(lis)}</ul>'


def compare(orig, new, otitle='Original (FTB Ultimate, 1.4.7)', ntitle='In Craft (1.21.1)'):
    return (f'<div class="compare"><div class="cmp orig"><div class="cmph">{otitle}</div>{orig}</div>'
            f'<div class="cmp new"><div class="cmph">{ntitle}</div>{new}</div></div>')


def infobox(title, img_ref, rows):
    src = icon(img_ref) if img_ref else None
    img = f'<div class="ibimg"><img src="{src}" alt=""></div>' if src else ''
    trs = ''.join(f'<tr><th>{k}</th><td>{v}</td></tr>' for k, v in rows)
    return f'<aside class="infobox"><div class="ibtitle">{title}</div>{img}<table>{trs}</table></aside>'


def note(kind, text):
    labels = {'info': 'Hinweis', 'warn': 'Achtung', 'tip': 'Tipp', 'new': 'Neu in Craft', 'chg': 'Geändert'}
    return f'<div class="note {kind}"><b>{labels.get(kind, "")}</b> {text}</div>'


def table(head, rows, cls='wikitable'):
    th = ''.join(f'<th>{h}</th>' for h in head)
    trs = ''.join('<tr>' + ''.join(f'<td>{c}</td>' for c in r) + '</tr>' for r in rows)
    return f'<div class="tablewrap"><table class="{cls}"><thead><tr>{th}</tr></thead><tbody>{trs}</tbody></table></div>'


# ---------------------------------------------------------------- assemble

def git_info():
    try:
        n = subprocess.run(['git', '-C', MOD, 'rev-list', '--count', 'HEAD'], capture_output=True,
                           text=True).stdout.strip()
        last = subprocess.run(['git', '-C', MOD, 'log', '-1', '--format=%h %ad', '--date=short'],
                              capture_output=True, text=True).stdout.strip()
        return n, last
    except Exception:
        return '?', '?'


def count_java():
    n = 0
    lines = 0
    for dirpath, _, files in os.walk(os.path.join(MOD, 'src', 'main', 'java')):
        for fn in files:
            if fn.endswith('.java'):
                n += 1
                with open(os.path.join(dirpath, fn), encoding='utf-8', errors='ignore') as f:
                    lines += sum(1 for _ in f)
    return n, lines


def main():
    global ITEMS
    ITEMS.update(collect_items())
    import content

    pages = []  # dicts: id, title, group, html
    doc_meta = {}
    for pid, rel, group, nav in DOC_PAGES:
        title, body = render_md(pid, read(os.path.join(MOD, rel)), ITEMS)
        src = f'<p class="srcnote">Quelle: <code>{esc(rel)}</code> aus dem Projekt (automatisch übernommen).</p>'
        doc_meta[pid] = (nav or title, group, src + body, title)

    # item mentions
    mentions = {iid: [] for iid in ITEMS}
    for pid, (nav, group, body, title) in doc_meta.items():
        for iid in set(re.findall(r'#/item/([a-z0-9_]+)', body)):
            if iid in mentions:
                mentions[iid].append(pid)

    jn, jl = count_java()
    gn, glast = git_info()
    stats = {'items': len(ITEMS), 'java': jn, 'lines': jl, 'commits': gn, 'last': glast,
             'docs': len(DOC_PAGES)}
    hand = content.pages(sys.modules[__name__], stats)
    import content_guides
    guides = {p['id']: p for p in content_guides.pages(sys.modules[__name__], stats)}
    hand = [guides.pop(p['id'], p) for p in hand]
    pos = next(i for i, p in enumerate(hand) if p['id'] == 'fortschritt') + 1
    hand[pos:pos] = list(guides.values())
    import content_guides2
    hand += content_guides2.pages(sys.modules[__name__], stats)
    import content_rohstoffe
    hand += content_rohstoffe.pages(sys.modules[__name__], stats)
    strom = next(p for p in hand if p['id'] == 'strom')
    hand.remove(strom)
    strom['group'] = 'Anleitungen'
    hand.insert(next(i for i, p in enumerate(hand) if p['group'] == 'Anleitungen'), strom)
    for p in hand:
        pages.append(p)
    for pid, (nav, group, body, title) in doc_meta.items():
        pages.append({'id': pid, 'title': nav, 'group': group, 'html': f'<h1>{esc(title)}</h1>' + body})

    # mentions from hand pages
    for p in hand:
        for iid in set(re.findall(r'#/item/([a-z0-9_]+)', p['html'])):
            if iid in mentions and p['id'] not in mentions[iid] and p['id'] != 'items':
                mentions[iid].insert(0, p['id'])

    items_js = []
    for iid, it in sorted(ITEMS.items(), key=lambda kv: (GROUP_ORDER.index(kv[1]['group']), kv[1]['de'].lower())):
        badge = 'chg' if iid in CHANGED else ('new' if iid in NEW_PAGES or it['group'].endswith('(neu)') else '')
        items_js.append({
            'id': iid, 'de': it['de'], 'en': it['en'], 'g': it['group'], 'k': it['kind'],
            'i': icon('craft:' + iid) or '', 'b': badge,
            'p': CHANGED.get(iid) or NEW_PAGES.get(iid) or guide_for(iid), 'm': mentions[iid][:12],
        })

    import recipes as recipes_mod
    col = recipes_mod.Collector(MOD, icon, LANG_DE)
    recipe_list = col.collect()
    refs = {r: v for r, v in col.ref_table().items() if not (r.startswith('craft:') and r[6:] in ITEMS)}
    tags = {t: v for t, v in col.tags.items()}
    print(f'recipes: {len(recipe_list)}, foreign refs: {len(refs)}, tags: {len(tags)}')

    nav_groups = []
    for p in pages:
        if p['group'] not in nav_groups:
            nav_groups.append(p['group'])
    nav = []
    for g in nav_groups:
        links = ''.join(f'<a href="#/{p["id"]}" data-nav="{p["id"]}">{esc(p["title"])}</a>'
                        for p in pages if p['group'] == g and not p.get('hidden'))
        nav.append(f'<div class="navgroup"><div class="navh">{esc(g)}</div>{links}</div>')

    sections = ''.join(f'<section class="page" id="page-{p["id"]}" data-title="{esc(p["title"])}" '
                       f'data-group="{esc(p["group"])}" hidden>{p["html"]}</section>' for p in pages)

    tpl = read(os.path.join(HERE, 'template.html')).replace('{{ITEMJS}}', read(os.path.join(HERE, 'item_page.js')))
    logo = icon('craft:macerator') or ''
    out = (tpl.replace('{{NAV}}', ''.join(nav))
              .replace('{{PAGES}}', sections)
              .replace('{{ITEMS}}', json.dumps(items_js, ensure_ascii=False).replace('</', '<\\/'))
              .replace('{{GROUPS}}', json.dumps(GROUP_ORDER, ensure_ascii=False))
              .replace('{{RECIPES}}', json.dumps(recipe_list, ensure_ascii=False, separators=(',', ':')).replace('</', '<\\/'))
              .replace('{{REFS}}', json.dumps(refs, ensure_ascii=False, separators=(',', ':')).replace('</', '<\\/'))
              .replace('{{SOURCES}}', json.dumps({**SOURCES, **WIKI_NOTES}, ensure_ascii=False, separators=(',', ':')).replace('</', '<\\/'))
              .replace('{{LOGO}}', logo)
              .replace('{{BUILD}}', esc(f'Stand {glast} · Craft 0.3.0')))
    with open(OUT, 'w', encoding='utf-8') as f:
        f.write(out)
    print(f'written {OUT}: {len(out) / 1024 / 1024:.2f} MB, {len(pages)} pages, {len(ITEMS)} items, '
          f'{sum(1 for v in ICON_CACHE.values() if v)} icons')
    missing = [i for i in ITEMS if not icon('craft:' + i)]
    print('items without icon:', len(missing), missing[:40])


if __name__ == '__main__':
    main()
