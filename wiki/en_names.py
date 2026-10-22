"""English item names for the wiki variant Craft-Wiki-EN.html (German text, English names of things).

Builds a German -> English term table from the Craft and vanilla lang files and replaces the terms in text
(never inside tags, <code>, <pre>, <script> or <style>).
"""
import json
import re
import zipfile

NAME_KEY = re.compile(r'(block|item|fluid_type)\.[a-z0-9_]+\.[a-z0-9_./]+')
# German names that are also everyday words in the wiki texts; they stay German
STOP = {
    'Explosion', 'Kessel', 'Fluss', 'Leiter', 'Pfeil', 'Luft', 'Licht', 'Feuer', 'Karte', 'Schild', 'Barriere',
    'Blume', 'Creeper', 'Lore', 'Angeln', 'Rot', 'Blau', 'Grün', 'Gelb', 'Grau', 'Leere',
}
# endings German adds to a name in running text (plural, genitive, dative); the first three mark a plural
PLURAL_ENDINGS = ('en', 'n', 'e')
NOUN_ENDINGS = PLURAL_ENDINGS + ('es', 's')
# English names that take no plural -s
UNCOUNTABLE = re.compile(r'(Dust|Water|Glass|Lava|Steam|Coal|Coke|Fuel|Oil|Matter|Foam|Resin|Rubber|Scrap|Slag|'
                         r'Sawdust|Coffee|Coolant|Sugar|Wheat|Hops|Powder|Armor|Redstone|Leather|String|Bamboo|'
                         r'Kelp|Mycelium|Dirt|Grass|Sand|Gravel|Clay|Ice|Snow|Stone|Bedrock|Obsidian|Wool|Fire|'
                         r'Uranium|Iridium|Bronze|Tin|Copper|Iron|Gold|Silver|Lead|Invar|Ender|Glowstone|Cobblestone|'
                         r'Deepslate|Netherrack|Farmland|Wood|Fertilizer)$|[sxz]$|\)$')
ADJ_ENDINGS = ('e', 'em', 'en', 'er', 'es')
# German words the texts use for things whose lang name is English in both languages (GregTech) or missing
ALIASES = {
    'Plattenbieger': 'Plate Bending Machine', 'Industrieller Hochofen': 'Industrial Blast Furnace',
    'Hochofen': 'Industrial Blast Furnace', 'Industrielle Zentrifuge': 'Industrial Centrifuge',
    'Zentrifuge': 'Industrial Centrifuge', 'Industrieller Elektrolyseur': 'Industrial Electrolyzer',
    'Implosionskompressor': 'Implosion Compressor', 'Vakuumgefrierer': 'Vacuum Freezer', 'Kessel': 'Boiler',
    'Hochdruckkessel': 'High-Pressure Boiler', 'Heizspule': 'Heating Coil', 'Maschinengehäuse': 'Machine Casing',
    'Reaktor': 'Reactor', 'Trafo': 'Transformer', 'Kühlzelle': 'Coolant Cell', 'Tesla-Spule': 'Tesla Coil',
}
# materials: German word -> English word; compounds with the suffixes below are used when the item exists
MATERIALS = {
    'Eisen': 'Iron', 'Kupfer': 'Copper', 'Zinn': 'Tin', 'Gold': 'Gold', 'Silber': 'Silver', 'Blei': 'Lead',
    'Nickel': 'Nickel', 'Zink': 'Zinc', 'Platin': 'Platinum', 'Aluminium': 'Aluminium', 'Titan': 'Titanium',
    'Wolfram': 'Tungsten', 'Chrom': 'Chrome', 'Messing': 'Brass', 'Bronze': 'Bronze', 'Elektrum': 'Electrum',
    'Invar': 'Invar', 'Stahl': 'Steel', 'Wolframstahl': 'Tungstensteel', 'Uran': 'Uranium',
    'Plutonium': 'Plutonium', 'Thorium': 'Thorium', 'Iridium': 'Iridium', 'Mangan': 'Manganese',
    'Magnesium': 'Magnesium', 'Lithium': 'Lithium', 'Silizium': 'Silicon', 'Diamant': 'Diamond',
    'Smaragd': 'Emerald', 'Saphir': 'Sapphire', 'Grünsaphir': 'Green Sapphire', 'Rubin': 'Ruby',
    'Olivin': 'Olivine', 'Granat': 'Garnet', 'Roter Granat': 'Red Garnet', 'Gelber Granat': 'Yellow Garnet',
    'Sodalith': 'Sodalite', 'Pyrit': 'Pyrite', 'Galena': 'Galena', 'Bauxit': 'Bauxite', 'Schwefel': 'Sulfur',
    'Glowstone': 'Glowstone', 'Kohle': 'Coal', 'Holzkohle': 'Charcoal', 'Obsidian': 'Obsidian',
    'Lazurit': 'Lazurite', 'Sphalerit': 'Sphalerite', 'Zinnober': 'Cinnabar', 'Calcit': 'Calcite',
    'Salpeter': 'Saltpeter', 'Feuerstein': 'Flint', 'Ton': 'Clay', 'Netherrack': 'Netherrack',
    'Enderperlen': 'Ender Pearl', 'Raffiniertes Eisen': 'Refined Iron', 'Raffinierter Eisen': 'Refined Iron',
    'Quecksilber': 'Mercury', 'Kalium': 'Potassium', 'Natrium': 'Sodium', 'Helium': 'Helium',
    'Wasserstoff': 'Hydrogen', 'Stickstoff': 'Nitrogen', 'Chlorit': 'Chlorite', 'Beryllium': 'Berylium',
    'Mischmetall': 'Mixed Metal', 'Magnalium': 'Magnalium', 'Kohlenstoff': 'Carbon', 'Methan': 'Methane',
    'Calcium': 'Calcium', 'Deuterium': 'Deuterium', 'Tritium': 'Tritium',
}
MATERIAL_SUFFIXES = {'staub': 'Dust', 'barren': 'Ingot', 'platte': 'Plate', 'erz': 'Ore', 'zelle': 'Cell'}
# bare material words that are also used in the texts on their own
BARE_MATERIALS = {'Eisen', 'Kupfer', 'Zinn', 'Silber', 'Blei', 'Zink', 'Platin', 'Titan', 'Wolfram', 'Chrom',
                  'Messing', 'Elektrum', 'Stahl', 'Uran', 'Mangan', 'Silizium', 'Diamant', 'Smaragd', 'Saphir',
                  'Rubin', 'Olivin', 'Sodalith', 'Pyrit', 'Bauxit', 'Schwefel', 'Quecksilber', 'Kalium',
                  'Natrium', 'Wasserstoff', 'Stickstoff', 'Mischmetall'}


def lang_pairs(de, en):
    return {de[k]: en[k] for k in de if NAME_KEY.fullmatch(k) and k in en and de[k] != en[k]}


def vanilla_en(jar_path):
    try:
        with zipfile.ZipFile(jar_path) as z:
            return json.loads(z.read('assets/minecraft/lang/en_us.json').decode('utf-8'))
    except Exception:
        return {}


def plural(en):
    if UNCOUNTABLE.search(en):
        return en
    if re.search(r'[^aeiou]y$', en):
        return en[:-1] + 'ies'
    return en + 's'


def variants(name):
    """German name and its inflected forms in running text: form -> is plural."""
    out = {name: False}
    words = name.split(' ')
    if len(words) > 1 and re.fullmatch(r'[A-ZÄÖÜ][a-zäöüß]+(e|er|es)', words[0]):
        stem = re.sub(r'(er|es|e)$', '', words[0])
        for end in ADJ_ENDINGS:
            out.setdefault(' '.join([stem + end] + words[1:]), False)
    for form in list(out):
        if len(form) >= 5:
            for end in NOUN_ENDINGS:
                out.setdefault(form + end, end in PLURAL_ENDINGS)
    return out


class Terms:
    def __init__(self, craft_de, craft_en, vanilla_de, vanilla_en_lang):
        pairs = lang_pairs(vanilla_de, vanilla_en_lang)
        pairs.update(lang_pairs(craft_de, craft_en))  # Craft wins on clashes
        known = set(craft_en.values()) | set(vanilla_en_lang.values())
        extra = dict(ALIASES)
        for de_mat, en_mat in MATERIALS.items():
            if de_mat in BARE_MATERIALS:
                extra[de_mat] = en_mat
            for de_suf, en_suf in MATERIAL_SUFFIXES.items():
                de_name = de_mat + (' ' + de_suf.capitalize() if ' ' in de_mat else de_suf)
                if de_name not in pairs and f'{en_mat} {en_suf}' in known:
                    extra[de_name] = f'{en_mat} {en_suf}'
        for de, en in extra.items():
            pairs.setdefault(de, en)
        stop = STOP - set(ALIASES)
        table = {}
        for de, en in pairs.items():
            if de in stop or len(de) < 3:
                continue
            for form, is_plural in variants(de).items():
                table.setdefault(form, plural(en) if is_plural else en)
        for de in pairs:  # an exact name beats an inflected form of a shorter one
            if de in table and de not in stop:
                table[de] = pairs[de]
        self.table = table
        keys = sorted(table, key=len, reverse=True)
        self.pattern = re.compile(r'(?<![\wÄÖÜäöüß])(' + '|'.join(map(re.escape, keys)) + r')(?![\wÄÖÜäöüß])')

    def text(self, s):
        return self.pattern.sub(lambda m: self.table[m.group(1)], s) if s else s

    def html(self, s):
        out, skip = [], 0
        for part in re.split(r'(<[^>]*>)', s):
            if part.startswith('<'):
                tag = re.match(r'</?([a-zA-Z0-9]+)', part)
                if tag and tag.group(1).lower() in ('code', 'pre', 'script', 'style'):
                    skip += -1 if part.startswith('</') else 1
                out.append(part)
            else:
                out.append(part if skip > 0 else self.text(part))
        return ''.join(out)

    def deep(self, value):
        """Strings inside JSON-like data; ids, refs and data URIs stay untouched."""
        if isinstance(value, str):
            if value.startswith('data:') or re.fullmatch(r'[#a-z0-9_]+:[a-z0-9_/.]+', value):
                return value
            return self.text(value)
        if isinstance(value, list):
            return [self.deep(v) for v in value]
        if isinstance(value, dict):
            return {k: self.deep(v) for k, v in value.items()}
        return value
