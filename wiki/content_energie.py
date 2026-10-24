"""Wiki page "Energie-Steckbrief" (spec 24 §4, phase 23.6).

Numbers come from the code: `gradlew runData` writes src/generated/resources/reports/craft/energy_profile.json
(dev.craft.data.EnergyProfileProvider: getMaxSafeInput, getMaxEnergyOutput, storage, consumption, cables, MJ, electric
items). The texts below only explain what the code does and add values that depend on the world (docs/spec 01-04,
09, 10, 13, 14, 23).
"""
import json
import os

UNLIMITED = 2147483647
TIERS = [(32, 'LV'), (128, 'MV'), (512, 'HV'), (2048, 'EV')]

# what an overvoltage packet does (spec 01 §4, 04, 10 B.2, 13 §1.6)
BOOM_IC2 = 'Explosion (Stärke 2,5), Block und Inhalt weg'
BOOM = {
    'mass_fabricator': 'Explosion Stärke 15',
    'luminator': 'kleine Explosion (Stärke 0,5)',
    'electrical_engine': 'nimmt jede Spannung an, explodiert nie',
    'energy_o_mat': 'nimmt jede Spannung an',
    'hv_transformer': 'nimmt jede Spannung an',
}
BOOM_GT = 'Explosion nach Paketgröße (bis Stärke 8, Kabel schmelzen); Eingang ×4 je Transformer-Upgrade'
BOOM_PC = 'nimmt jede Spannung an (wie im Pack keine Prüfung)'

# consumption / output that the code does not expose as a constant
USE = {
    'canning_machine': '1 EU/t, 600 t je Vorgang',
    'induction_furnace': '15 EU/t (heizt sich langsam auf)',
    'miner': '1 EU/t im Betrieb; Bohrer 50 (Diamant 80) EU je Block',
    'ic2_pump': '1 EU/t; 200 EU je Pumpvorgang (10 s)',
    'magnetizer': '1 EU je aufgeladener Zaunstufe',
    'mass_fabricator': '1 000 000 EU je UU-Materie; mit Schrott zählt jede EU 6-fach',
    'terraformer': 'je Blaupause, z. B. Kultivierung 4 000 EU je Erfolg',
    'tesla_coil': 'mit Redstone 1 EU/t; Schock alle 32 t ab 400 EU (verbraucht alles)',
    'crop_matron': '10 EU je Dünger/Hydrierung/Weed-EX; hält mind. 31 EU',
    'luminator': '1 EU je 4 Ticks (0,25 EU/t)',
    'energy_o_mat': 'verkauft EU gegen Items (gibt 32er-Pakete ab)',
    'electrical_engine': '6 EU/t → 2 MJ/t (mit Platinen bis 36 EU/t → 10 MJ/t)',
    'recycler': None,
    'rotary_macerator': '3 EU/t; erst Hochlaufen (7 500 Ticks), dann 16 t je Vorgang (48 EU)',
    'singularity_compressor': '3 EU/t; erst Hochlaufen (7 500 Ticks), dann 16 t je Vorgang (48 EU)',
    'centrifuge_extractor': '3 EU/t; erst Hochlaufen (7 500 Ticks), dann 16 t je Vorgang (48 EU)',
}
GT_USE = 'je Rezept (EU/t steht im Rezept, EMI)'
OUT = {
    'generator': '10 EU/t (4 000 EU je Kohle)',
    'geothermal_generator': '20 EU/t (20 000 EU je Lava-Eimer)',
    'solar_panel': '1 EU/t bei Tag, kein Regen; 1,2 EU/t im 9×9-Feld',
    'water_mill': 'bis 2 EU/t (Tag und Nacht)',
    'wind_mill': '≈ 3–7 EU/t je nach Höhe, Gewitter mehr',
    'nuclear_reactor': 'je Design 60–400+ EU/t (5 EU/t je Puls), siehe Kernreaktor',
    'reactor_chamber': 'gibt den Rest der Reaktor-Ausgabe ab',
    'lv_solar_array': 'stillgelegt (liefert nichts, zerlegbar)',
    'mv_solar_array': 'stillgelegt (liefert nichts, zerlegbar)',
    'hv_solar_array': 'stillgelegt (liefert nichts, zerlegbar)',
    'steam_turbine': '20 EU/t je Kessel, bis 64',
    'large_steam_turbine': '160 EU/t je Hochdruckkessel, bis 480',
    'water_turbine': '16–32 EU/t (Wasserfluss)',
    'large_water_turbine': '64–256 EU/t (Wasserfluss)',
    'large_wind_turbine': '64–136 EU/t, Gewitter bis 384',
}
ENGINES = {
    'redstone_engine': ('0,05 MJ/t', 'nur Redstone-Signal'),
    'stirling_engine': ('1 MJ/t', 'feste Brennstoffe'),
    'combustion_engine': ('Lava 1 · Öl 3 · Treibstoff 6 MJ/t', 'Flüssigkeit + Kühlwasser'),
}
# the reactor sends its whole output as one packet per tick; getMaxEnergyOutput is only the upper bound
PACKET = {
    'nuclear_reactor': 'aktuelle Ausgabe als 1 Paket je Tick (über 512 → HV-Trafo nötig)',
    'reactor_chamber': 'wie der Reaktor',
}
NO_EU = [
    ('iron_furnace', 'Brennstoff wie ein Ofen', '–'),
    ('coke_oven', 'kein Strom: 1 Kohle → 1 Koks in 900 t', '–'),
    ('boiler_firebox', 'Kohle/Koks → Dampf für die Dampfturbine', 'Explosion (Stärke 6), wenn Wasser auf den trockenen, heißen Kessel kommt oder er trocken 1 000 °C erreicht'),
    ('high_pressure_boiler_firebox', 'Kohle/Koks → Hochdruckdampf', 'wie klein, aber Stärke 10 und Grenze 1 500 °C'),
]


def tier(n):
    if n >= UNLIMITED:
        return 'beliebig'
    return next((t for limit, t in TIERS if n <= limit), 'über EV')


def num(n):
    if isinstance(n, float):
        s = f'{n:.3f}'.rstrip('0').rstrip(',.')
        return s.replace('.', ',')
    return f'{n:,}'.replace(',', '.') if n < UNLIMITED else '∞'


def pages(h, stats):
    c = h.chip
    path = os.path.join(h.MOD, 'src', 'generated', 'resources', 'reports', 'craft', 'energy_profile.json')
    with open(path, encoding='utf-8') as f:
        data = json.load(f)
    blocks = {b['id']: b for b in data['blocks']}

    def chip(iid):
        return c(iid) if iid in h.ITEMS else h.esc(iid)

    def store(b):
        val = b['values']
        for k in ('getMaxStorage', 'getMaxEnergy', 'maxEUStore', 'MAX_ENERGY'):
            if k in val:
                return num(val[k]) + ' EU'
        return '–'

    def is_gt(b):
        return 'GtMachineBlockEntity' in b['classes']

    storages = ('batbox', 'mfe', 'mfsu')
    trafos = ('lv_transformer', 'mv_transformer', 'hv_transformer')
    converters = [i for i in blocks if i.startswith('ic2_') and (i.endswith('_consumer') or i.endswith('_producer'))]

    # ---------------------------------------------------------------- consumers
    sinks = []
    for iid, b in blocks.items():
        if 'eu_sink' not in b['kinds'] or iid in storages + trafos or iid in converters:
            continue
        safe = b.get('maxSafeInput', 0)
        val = b['values']
        if is_gt(b):
            use, boom = GT_USE, BOOM_GT
        else:
            boom = BOOM.get(iid, BOOM_IC2)
            use = USE.get(iid)
            if use is None and 'getEnergyConsume' in val:
                e, t = val['getEnergyConsume'], val.get('getOperationLength', 0)
                use = f'{e} EU/t, {t} t je Vorgang (= {num(e * t)} EU)'
        sinks.append((h.ITEMS.get(iid, {}).get('de', iid).lower(),
                      [chip(iid), tier(safe), num(safe), boom, use or '–', store(b)]))
    sinks.sort()

    # ---------------------------------------------------------------- generators
    gens = []
    for iid, b in blocks.items():
        out = b.get('maxEnergyOutput', 0)
        if 'eu_source' not in b['kinds'] or out <= 0 or 'eu_sink' in b['kinds'] or iid in converters:
            continue
        gens.append((h.ITEMS.get(iid, {}).get('de', iid).lower(),
                     [chip(iid), PACKET.get(iid, f'{num(out)} ({tier(out)})'), OUT.get(iid, '–'), store(b)]))
    gens.sort()

    # ---------------------------------------------------------------- storage, transformers, converters
    st = []
    for iid in storages + trafos:
        b = blocks[iid]
        safe = b['maxSafeInput']
        boom = BOOM.get(iid, BOOM_IC2)
        note = ('Standard abwärts: Eingang Frontseite, bis 4 Pakete je Tick an den anderen Seiten; mit Redstone aufwärts (umgekehrt, 1 Paket der hohen Spannung)'
                if iid in trafos else 'Ausgabe nur an der Frontseite, Eingabe an den anderen 5 Seiten')
        st.append([chip(iid), f'{num(safe)} ({tier(safe)})', f'{num(b["maxEnergyOutput"])} ({tier(b["maxEnergyOutput"])})',
                   store(b) if iid in storages else '–', boom, note])
    pc = []
    for iid in sorted(converters, key=lambda i: (['lv', 'mv', 'hv', 'ev'].index(i.split('_')[1]), i)):
        b = blocks[iid]
        if iid.endswith('_consumer'):
            pc.append([chip(iid), 'EU → Energy Bridge', 'nimmt jede Spannung an', BOOM_PC])
        else:
            out = b['maxEnergyOutput']
            pc.append([chip(iid), 'Energy Bridge → EU', f'Pakete à {num(out)} ({tier(out)})', '–'])

    # ---------------------------------------------------------------- cables
    cables = []
    for iid, b in blocks.items():
        if 'eu_conductor' not in b['kinds'] or 'conductorBreakdown' not in b or b['conductorBreakdown'] >= UNLIMITED:
            continue
        melt = b['conductorBreakdown']
        ins = b['insulationAbsorption']
        cables.append((melt, b['conductionLoss'], [
            chip(iid), f'{num(melt - 1)} ({tier(melt - 1)})', num(b['conductionLoss']) + ' EU',
            'voll isoliert' if ins >= 9001 else (f'bis {num(ins)} EU' if ins > 0 else 'blank – schockt immer')]))
    cables.sort(key=lambda r: (r[0], r[1]))
    casings = [chip(i) for i, b in blocks.items() if 'eu_conductor' in b['kinds'] and b.get('conductorBreakdown', 0) >= UNLIMITED]

    # ---------------------------------------------------------------- MJ
    engines = [[chip(i), ENGINES[i][0], ENGINES[i][1], num(blocks[i]['values']['getMaxEnergy']) + ' MJ',
                f"Überhitzung → Explosion (Radius {blocks[i]['values']['explosionRange']})"] for i in ENGINES if i in blocks]
    if 'electrical_engine' in blocks:
        engines.append([chip('electrical_engine'), '2 MJ/t aus 6 EU (mit Platinen bis 10 MJ/t)', 'EU (jede Spannung)',
                        num(blocks['electrical_engine']['mjMaxStored']) + ' MJ', 'keine Explosion, kühlt zwangsweise ab'])
    # Thermal Expansion engines (spec 26 §6.1): no explosion, they break when the buffer is full
    for iid, power, fuel, buffer in (('steam_engine', '0,2–2 MJ/t', 'feste Brennstoffe + Wasser', 2000),
                                     ('magmatic_engine', '0,4–4 MJ/t', 'Lava (18 000 MJ je Eimer)', 4000)):
        if iid in h.ITEMS:
            engines.append([chip(iid), power, fuel, num(buffer) + ' MJ',
                            'keine Explosion; bei vollem Puffer kaputt (Schraubenschlüssel repariert)'])
    mj = []
    for iid, b in blocks.items():
        if 'mj_receiver' not in b['kinds'] or b.get('mjMaxReceived', 0) <= 0 or 'transport_pipe' in iid \
                or 'pipe' in iid or iid in ('bc_consumer', 'electrical_engine'):
            continue
        extra = 'keine Explosion'
        mj.append((h.ITEMS.get(iid, {}).get('de', iid).lower(), [
            chip(iid), f"{num(b['mjMinReceived'])}–{num(b['mjMaxReceived'])} MJ", num(b['mjActivation']) + ' MJ',
            num(b['mjMaxStored']) + ' MJ', extra]))
    mj.sort()

    # ---------------------------------------------------------------- items
    # Modular Powersuits count in joules from their battery modules; the IC2 view (max charge 1) means nothing
    items = sorted((i for i in data['items'] if not i['id'].startswith('power_')),
                   key=lambda i: (not i['canProvideEnergy'], i['tier'], i['maxCharge'], i['id']))
    item_rows = [[chip(i['id']), f"{i['tier']} ({['', 'LV', 'MV', 'HV', 'EV'][i['tier']]})", num(i['maxCharge']) + ' EU',
                  num(i['transferLimit']) + ' EU/t', 'ja' if i['canProvideEnergy'] else 'nein'] for i in items]

    body = f'''<h1>Energie-Steckbrief</h1>
<p class="lead">Für jedes Gerät: welche Energie, wie viel Spannung es verträgt, was bei mehr passiert, was es verbraucht oder
liefert und wie viel es speichert. Die Zahlen stammen direkt aus dem Code (beim Bauen des Wikis ausgelesen).</p>
<div class="tocpos"></div>
<h2 id="es--stufen">Spannungsstufen</h2>
{h.table(['Stufe', 'max. Paket', 'typische Quelle', 'Speicher / Trafo'], [
    ['LV (1)', '32 EU', 'Generator, Solar, Wasser, Wind, BatBox', c('batbox') + ', ' + c('lv_transformer')],
    ['MV (2)', '128 EU', 'MFE, LV→MV-Trafo, Dampfturbine (64)', c('mfe') + ', ' + c('mv_transformer')],
    ['HV (3)', '512 EU', 'MFSU, Großkraftwerke', c('mfsu') + ', ' + c('hv_transformer')],
    ['EV (4)', '2 048 EU', 'HV-Trafo hochgeschaltet, EV-Producer', '–'],
])}
{h.note('warn', 'Ein Paket <b>größer</b> als die maximale sichere Eingabe lässt die meisten IC2- und GT-Maschinen <b>explodieren</b>. '
               'Die Paketgröße bestimmt die Quelle (Speicher, Trafo, Generator), nicht der Verbrauch. Wie man richtig anschließt: '
               '<a href="#/strom">Strom anschließen</a>.')}

<h2 id="es--verbraucher">EU-Verbraucher</h2>
{h.table(['Gerät', 'Stufe', 'max. sichere Eingabe', 'bei mehr', 'Verbrauch', 'Speicher'], [r for _, r in sinks])}
<p style="font-size:12px;color:var(--muted)">IC2-Maschinen: Overclocker beschleunigen und erhöhen den Verbrauch, Transformer-Upgrades
heben die sichere Eingabe um eine Stufe, Energiespeicher-Upgrades vergrößern den Speicher (<a href="#/ic2-maschinen">IC2-Maschinen & Upgrades</a>).
GT-Maschinen nehmen mit MJ-Konverter-Upgrade auch MJ an.</p>

<h2 id="es--erzeuger">EU-Erzeuger</h2>
{h.table(['Gerät', 'Paketgröße (Stufe)', 'Leistung', 'interner Speicher'], [r for _, r in gens])}
<p>Vergleich aller Quellen: <a href="#/energie">Energiequellen im Vergleich</a>.</p>

<h2 id="es--speicher">Speicher und Transformatoren</h2>
{h.table(['Gerät', 'Eingang max.', 'Ausgang', 'Speicher', 'bei mehr', 'Hinweis'], st)}

<h2 id="es--pc">Power Converters</h2>
{h.table(['Gerät', 'Richtung', 'Spannung', 'Überspannung'], pc)}

<h2 id="es--kabel">Kabel</h2>
{h.table(['Kabel', 'hält Pakete bis', 'Verlust je Block', 'Isolierung'], [r[2] for r in cables])}
<p>Größere Pakete lassen das Kabel <b>schmelzen</b>. Über der Isolierung gibt das Kabel Stromschläge. Verlustfrei sind die
Gehäuse der Kraftwerke ({', '.join(casings)}): Sie leiten nur innerhalb der eigenen geformten Anlage.</p>

<h2 id="es--mj">MJ (BuildCraft)</h2>
<h3 id="es--motoren">Motoren</h3>
{h.table(['Motor', 'Leistung', 'Brennstoff', 'Speicher', 'Gefahr'], engines)}
<h3 id="es--mjmaschinen">MJ-Verbraucher</h3>
{h.table(['Gerät', 'nimmt je Tick', 'Aktivierung', 'Speicher', 'Überlast'], [r for _, r in mj])}
<p>MJ kennt keine Spannung: Zu viel MJ wird einfach nicht angenommen. Die Electrical Engine wandelt EU in MJ
(<a href="#/umwandlung">Energie umwandeln</a>).</p>

<h2 id="es--ohne">Ohne Strom</h2>
{h.table(['Gerät', 'Energie', 'Gefahr'], [[chip(i), t, g] for i, t, g in NO_EU])}

<h2 id="es--items">Elektrische Gegenstände</h2>
{h.table(['Gegenstand', 'Stufe', 'Kapazität', 'Laden max.', 'gibt Energie ab'], item_rows)}
<p>Laden geht nur in Speichern und Maschinen mindestens derselben Stufe. Gegenstände, die Energie abgeben, versorgen
Maschinen aus dem Batterie-Slot; die Akkupacks (BatPack, LapPack, Lithium-Batpack) laden auf dem Rücken getragen die
Werkzeuge in der Hand. Power-Rüstung und Power-Werkzeug (Modular Powersuits) rechnen in Joule (50 J = 1 EU); ihre
Kapazität kommt aus den eingebauten Batteriemodulen, geladen werden sie wie Stufe-1-Gegenstände
(<a href="#/mod-mps">Modular Powersuits</a>).</p>
<p>Der <a href="#/leuchtschild">Elektrische Leuchtschild</a> verbraucht 1 EU je 4 Ticks Licht und 100 EU je geblocktem
Haltbarkeitspunkt.</p>
'''
    return [{'id': 'energie-steckbrief', 'title': 'Energie-Steckbrief', 'group': 'Anleitungen', 'html': body}]
