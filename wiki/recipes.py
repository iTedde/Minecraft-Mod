"""Collects all datagen recipes of Craft into a compact JSON structure for the item pages."""
import json
import os
import zipfile

RECIPE_DIR = ('src', 'generated', 'resources', 'data', 'craft', 'recipe')
DATA_DIRS = (('src', 'generated', 'resources', 'data'), ('src', 'main', 'resources', 'data'))
JARS = ('neoforge-21.1.259-client-extra-aka-minecraft-resources.jar', 'neoforge-21.1.259.jar')
VANILLA_LANG_INDEX = os.path.expanduser('~/.gradle/caches/neoformruntime/assets/indexes/17.json')

# recipe type -> (label, workstation ids, extra info)
TYPES = {
    'craft:ic2_shaped': ('Werkbank', [], ''),
    'craft:ic2_shapeless': ('Werkbank', [], ''),
    'minecraft:crafting_shaped': ('Werkbank', [], ''),
    'minecraft:crafting_shapeless': ('Werkbank', [], ''),
    'craft:gradual_repair': ('Werkbank (Nachfüllen)', [], ''),
    'minecraft:smelting': ('Ofen', ['minecraft:furnace', 'craft:iron_furnace', 'craft:electric_furnace', 'craft:induction_furnace'], ''),
    'craft:macerator': ('Macerator', ['craft:macerator'], '2 EU/t · 400 Ticks · 800 EU'),
    'craft:compressor': ('Kompressor', ['craft:compressor'], '2 EU/t · 400 Ticks · 800 EU'),
    'craft:extractor': ('Extraktor', ['craft:extractor'], '2 EU/t · 400 Ticks · 800 EU'),
    'craft:gt_assembler': ('Assembler', ['craft:assembling_machine'], ''),
    'craft:gt_bender': ('Plattenbieger', ['craft:plate_bending_machine'], ''),
    'craft:gt_blast_furnace': ('Industrieller Hochofen', ['craft:industrial_blast_furnace'], ''),
    'craft:gt_canner': ('Automatische Canning Machine', ['craft:automatic_canning_machine'], ''),
    'craft:gt_centrifuge': ('Industrielle Zentrifuge', ['craft:industrial_centrifuge'], ''),
    'craft:gt_electrolyzer': ('Industrieller Elektrolyseur', ['craft:industrial_electrolyzer'], ''),
    'craft:gt_grinder': ('Industrial Grinder', ['craft:industrial_grinder'], ''),
    'craft:gt_implosion': ('Implosionskompressor', ['craft:implosion_compressor'], ''),
    'craft:gt_vacuum_freezer': ('Vakuumgefrierer', ['craft:vacuum_freezer'], ''),
    'craft:carpenter': ('Carpenter', ['craft:carpenter'], ''),
    'craft:fabricator': ('Thermionic Fabricator', ['craft:thermionic_fabricator'], ''),
}
GRID_TYPES = {'craft:ic2_shaped', 'minecraft:crafting_shaped', 'craft:carpenter', 'craft:fabricator'}
SHAPELESS = {'craft:ic2_shapeless', 'minecraft:crafting_shapeless'}


class Collector:
    def __init__(self, mod_root, icon_fn, craft_lang):
        self.root = mod_root
        self.icon_fn = icon_fn
        self.craft_lang = craft_lang
        self.jars = []
        for j in JARS:
            p = os.path.join(mod_root, 'build', 'moddev', 'artifacts', j)
            if os.path.exists(p):
                self.jars.append(zipfile.ZipFile(p))
        self.vlang = self._vanilla_lang()
        self.tag_cache = {}
        self.refs = set()  # every item id that appears anywhere
        self.tags = {}  # tag -> list of ids

    # -------------------------------------------------------------- names
    def _vanilla_lang(self):
        try:
            idx = json.load(open(VANILLA_LANG_INDEX))['objects']
            h = idx['minecraft/lang/de_de.json']['hash']
            p = os.path.join(os.path.dirname(os.path.dirname(VANILLA_LANG_INDEX)), 'objects', h[:2], h)
            return json.load(open(p, encoding='utf-8'))
        except Exception:
            return {}

    def name(self, ref):
        ns, p = ref.split(':', 1)
        lang = self.craft_lang if ns == 'craft' else self.vlang
        for k in (f'item.{ns}.{p}', f'block.{ns}.{p}'):
            if k in lang:
                return lang[k]
        return p.replace('_', ' ')

    def fluid_name(self, ref):
        ns, p = ref.split(':', 1)
        lang = self.craft_lang if ns == 'craft' else self.vlang
        return lang.get(f'fluid_type.{ns}.{p}') or lang.get(f'block.{ns}.{p}') or p

    # -------------------------------------------------------------- tags
    def _read_tag(self, ns, path):
        rel = f'{ns}/tags/item/{path}.json'
        for d in DATA_DIRS:
            p = os.path.join(self.root, *d, *rel.split('/'))
            if os.path.exists(p):
                yield json.load(open(p, encoding='utf-8'))
        for z in self.jars:
            try:
                yield json.loads(z.read('data/' + rel))
            except KeyError:
                pass

    def tag(self, tag, depth=0):
        if tag in self.tag_cache:
            return self.tag_cache[tag]
        ns, path = tag.split(':', 1)
        out = []
        if depth < 10:
            for data in self._read_tag(ns, path):
                for v in data.get('values', []):
                    v = v['id'] if isinstance(v, dict) else v
                    if v.startswith('#'):
                        out += self.tag(v[1:], depth + 1)
                    else:
                        out.append(v)
        seen = []
        for v in out:
            if v not in seen:
                seen.append(v)
        # craft items first: they are the ones the pack actually provides
        seen.sort(key=lambda r: 0 if r.startswith('craft:') else 1)
        self.tag_cache[tag] = seen
        return seen

    # -------------------------------------------------------------- ingredients
    def ing(self, j, count=None):
        """-> {'r': [ids], 't': tag or absent, 'c': count}"""
        if j is None or j == {}:
            return None
        if isinstance(j, list):
            refs = []
            for x in j:
                sub = self.ing(x)
                if sub:
                    refs += sub['r']
            res = {'r': refs}
        elif j.get('type') == 'neoforge:components':
            items = j['items']
            refs = [items] if isinstance(items, str) else list(items)
            res = {'r': refs, 'n': 'unbeschädigt'}
        elif 'tag' in j:
            refs = self.tag(j['tag'])
            self.tags[j['tag']] = refs
            res = {'r': refs, 't': j['tag']}
        elif 'item' in j:
            res = {'r': [j['item']]}
        elif 'id' in j:
            res = {'r': [j['id']]}
        else:
            return None
        c = count if count is not None else j.get('count', 1) if isinstance(j, dict) else 1
        if c and c > 1:
            res['c'] = c
        self.refs.update(res['r'])
        return res

    def out(self, j):
        if not j or 'id' not in j:
            return None
        self.refs.add(j['id'])
        o = {'r': [j['id']]}
        if j.get('count', 1) > 1:
            o['c'] = j['count']
        return o

    # -------------------------------------------------------------- recipes
    def collect(self):
        base = os.path.join(self.root, *RECIPE_DIR)
        recipes = []
        for dirpath, _, files in os.walk(base):
            for fn in sorted(files):
                if not fn.endswith('.json'):
                    continue
                rel = os.path.relpath(os.path.join(dirpath, fn), base).replace('\\', '/')[:-5]
                j = json.load(open(os.path.join(dirpath, fn), encoding='utf-8'))
                r = self.convert(j, rel)
                if r:
                    recipes.append(r)
        return recipes

    def convert(self, j, rid):
        t = j.get('type')
        if t not in TYPES:
            return None
        label, ws, info = TYPES[t]
        r = {'id': rid, 'k': t.split(':')[1], 'l': label}
        if rid.startswith('uu_matter/'):
            r['l'] = 'Werkbank (UU-Materie, versteckt)'
        meta = []
        if t in GRID_TYPES:
            key = {k: self.ing(v) for k, v in j['key'].items()}
            pat = j['pattern']
            w = max(len(p) for p in pat)
            grid = []
            for row in pat:
                grid.append([key.get(ch) if ch != ' ' else None for ch in row.ljust(w)])
            r['g'] = grid
            r['o'] = [self.out(j['result'])]
        elif t in SHAPELESS:
            r['in'] = [self.ing(x) for x in j['ingredients']]
            r['sl'] = 1
            r['o'] = [self.out(j['result'])]
        elif t == 'craft:gradual_repair':
            item = self.ing({'item': j['item']})
            r['in'] = [item, self.ing(j['material'])]
            r['sl'] = 1
            r['o'] = [{'r': item['r']}]
            meta.append(f"je Material −{j['amount']} Abnutzung")
        elif t == 'minecraft:smelting':
            r['in'] = [self.ing(j['ingredient'])]
            r['o'] = [self.out(j['result'])]
            meta.append(f"{j.get('cookingtime', 200)} Ticks im Vanilla-Ofen")
            if j.get('experience'):
                meta.append(f"{j['experience']} EP")
        elif t in ('craft:macerator', 'craft:compressor', 'craft:extractor'):
            r['in'] = [self.ing(j['ingredient'], j.get('count'))]
            r['o'] = [self.out(j['result'])]
        else:  # GT machines
            r['in'] = [self.ing(x) for x in j.get('inputs', [])]
            r['o'] = [self.out(x) for x in j.get('outputs', [])]
            d, e = j.get('duration'), j.get('eu_per_tick')
            if d and e:
                meta.append(f'{e} EU/t · {d} Ticks ({d / 20:g} s) · {d * e:,} EU'.replace(',', '.'))
            if j.get('special') and t == 'craft:gt_blast_furnace':
                meta.append(f"Hitze {j['special']} K")
        if t == 'craft:carpenter':
            liq = j.get('liquid')
            if liq:
                meta.append(f"{liq['amount']} mB {self.fluid_name(liq['id'])}")
            if 'box' in j:
                r['in'] = [self.ing(j['box'])]
            meta.append(f"{j.get('packaging_time', 0)} Arbeitsschritte")
        if t == 'craft:fabricator':
            m = j.get('molten')
            if m:
                meta.append(f"{m['amount']} mB {self.fluid_name(m['id'])}")
            if 'plan' in j:
                r['in'] = [self.ing(j['plan'])]
        if info:
            meta.append(info)
        r['o'] = [o for o in r['o'] if o]
        if 'in' in r:
            r['in'] = [x for x in r['in'] if x]
        r['m'] = ' · '.join(meta)
        r['ws'] = ws
        self.refs.update(ws)
        return r

    def ref_table(self):
        """id -> [name, icon] for every referenced id (vanilla and craft)."""
        table = {}
        for ref in sorted(self.refs):
            table[ref] = [self.name(ref), self.icon_fn(ref) or '']
        return table
