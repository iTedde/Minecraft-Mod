"""Psychedelicraft pages (phase 52.6, docs/spec/52-psychedelicraft.md).

Psychedelicraft is a separate port mod in the same repo (psychedelicraft/, mod id 'psychedelicraft'), not part of
dev.craft. Its items are not in the Craft item index; the pages use their own chips with icons from the port's assets.
Facts come from the port's data (recipes, worldgen, loot) and code (drug types, fluids); the hangover is spec §6.
"""

G = 'Psychedelicraft'


def pages(h, stats):
    P = []

    def page(pid, title, body):
        P.append({'id': pid, 'title': title, 'group': G, 'html': body})

    def c(path, name, ns='psychedelicraft'):
        return h.vchip(f'{ns}:{path}', name)

    def v(path, name):
        return h.vchip(f'minecraft:{path}', name)

    T = h.table
    N = h.note

    # ================================================================== overview + plants
    overview = f'''<h1>Psychedelicraft – Überblick</h1>
<p class="lead"><b>Psychedelicraft</b> (Ivorius/Sollace, MIT-Lizenz) bringt Pflanzen, Brauen, Brennen und Drogen mit
Rausch, Halluzinationen und Folgen. In Craft ist es ein <b>eigener Port</b> der Fabric-Version 1.8.9 für NeoForge 1.21.1:
eigene Mod (<code>psychedelicraft</code>), eigene Jar, im Craft-Repo unter <code>psychedelicraft/</code>. Spielinhalte wie
in der Vorlage; neu ist nur der <b>Kater</b>.</p>
<div class="tocpos"></div>
{N('info', 'Die englischen Item-Namen stammen aus der Mod (eine deutsche Übersetzung hat die Vorlage nicht). Alle Rezepte zeigt <b>EMI</b> mit eigenen Kategorien: Trocknen, Einweichen (Maischbottich), Fass/Destille/Kolben, Backen (Tablett), Flüssigkeits-Zubereitung, Welt-Interaktion.')}
<h2 id="ps--weg">Der Weg in Kürze</h2>
<ol class="steps">
<li><b>Pflanzen finden</b> (Wildwuchs in der Oberwelt, Truhen, Farming-Welt) und auf Ackerboden anbauen.</li>
<li><b>Trocknen</b> auf dem {c('drying_table', 'Drying Table')}: Blätter/Blüten → getrocknete Ware für Joints, Zigaretten, Pfeife, Bong.</li>
<li><b>Brauen</b>: Wasser + 8 Früchte/Getreide in den {c('mash_tub', 'Wooden Vat')} → Most; gärt, reift im {c('oak_barrel', 'Fass')}, wird in der {c('distillery', 'Distillery')} gebrannt.</li>
<li><b>Chemie</b>: Pflanzen im {c('flask', 'Flask')} mit Wasser auskochen (Extrakte), auf dem {c('tray', 'Tray')} über dem {c('bunsen_burner', 'Bunsen Burner')} zu Pulver/Tabletten härten.</li>
<li><b>Konsumieren</b> – und mit den Folgen leben: Halluzinationen, Übelkeit, Erbrechen, Kater.</li>
</ol>
<h2 id="ps--pflanzen">Pflanzen und Fundorte</h2>
{T(['Pflanze', 'Wild zu finden', 'Ernte / Verwendung'], [
    [c('cannabis_seeds', 'Cannabis'), 'Angelegte Ackerfelder in kalten Biomen, Hügeln, Wäldern, Ebenen', 'Blätter, Blüten (wächst 3 Blöcke hoch) → trocknen → Joint, Blunt, Haschmuffin, Tee'],
    [c('hop_seeds', 'Hops'), 'wie Cannabis', 'Hopfendolden → Weizen-Hopfen-Most (Bier)'],
    [c('tobacco_seeds', 'Tobacco'), 'wie Cannabis', 'Tabakblätter → trocknen → Zigarette, Zigarre, Blunt, Harmonium'],
    [c('coca_seeds', 'Coca'), 'wie Cannabis', 'Kokablätter → Tee oder trocknen → Kokainpulver'],
    [c('coffea_cherries', 'Coffea'), 'wie Cannabis', 'Kaffeekirschen → im Ofen rösten → Kaffeebohnen → Kaffee, Koffein'],
    [c('juniper_sapling', 'Juniper'), 'Bäume in Wäldern (dicht) und auf Hügeln (vereinzelt)', 'fruchtende Blätter mit der Schere → Wacholderbeeren (Gin); Holz, Fass, Boot'],
    [c('morning_glory', 'Morning Glory'), 'Blumenwald, Sonnenblumenebene, Wiese, üppige Höhlen; am Holzspalier', 'Blüten/Samen → Extrakt → LSA-Papier, LSD, Meth'],
    [c('belladonna_berries', 'Belladonna'), 'Dunkelwald, Blumenwald', 'Beeren, Blätter, Samen → Atropin'],
    [c('jimsonweed_seed_pod', 'Jimsonweed'), 'Dschungel (alle Arten)', 'Samenkapseln, Blätter → Atropin'],
    [c('tomato', 'Tomato'), 'Wälder', 'Tomaten → Tomatensaft, Ketchup, Tomatenschnaps'],
    [c('peyote', 'Peyote'), 'Savanne, Tafelberge, Wüste (mit Wüstentempeln)', 'essen, trocknen → Peyote-Joint, Saft'],
    [c('agave_leaf', 'Agave'), 'wie Peyote', 'Agavenblätter → Mezcal, Tequila'],
    [c('wine_grapes', 'Wine Grapes'), 'nur in Truhen (Mine, Verlies, Dorftempel)', 'auf ein Holzspalier setzen → wächst, mit der Schere ernten → Wein'],
])}
<p>Vanilla-Zutaten: {v('brown_mushroom', 'Pilze')} → getrocknet <b>Zauberpilze</b>, {v('poppy', 'Mohn')} → getrocknet für die Bong
bzw. ausgekocht <b>Morphin</b>, dazu Weizen, Kartoffel, Zuckerrohr, Honig, Äpfel und – über <code>c:</code>-Tags – Mais, Reis,
Bananen, Ananas, Trauben anderer Mods (z. B. Farmer's Delight).</p>
{N('tip', 'In der <a href="#/farming-dimension">Farming-Welt</a> wachsen alle Psychedelicraft-Pflanzen wild (Hanf, Hopfen, Tabak, Koka, Kaffee, Wacholder automatisch, Agave, Peyote, Tollkirsche, Stechapfel, Tomate, Prunkwinde und das Weinreben-Spalier über <code>#craft:farming_extra</code>).')}
<h3 id="ps--truhen">Truhen und Dörfer</h3>
{T(['Fundort', 'Psychedelicraft-Beute'], [
    ['Verlassene Mine', 'Weintrauben, Zigaretten, Pfeife, Holzkrug, Wacholderbeeren, getrockneter Tabak'],
    ['Verlies', 'Weintrauben, Glaskelch, Holzkrug, Wacholderbeeren, getrockneter Tabak'],
    ['Wüstentempel', 'Peyote-Joint, getrockneter Peyote, Agavenblätter'],
    ['Plünderer-Außenposten', 'Bong, Pfeife, Steinbecher, getrocknete Kokablätter, getrockneter Tabak'],
    ['Schiffswrack (Vorrat)', 'Holzkrüge, Fässer, Flaschen'],
    ['Dorf (Tempel, Werkzeug-/Waffenschmied, Schäfer, Gerber)', 'Krüge, Zigaretten, Zigarren, Joints, Steinbecher, Spritze, Haschmuffins'],
])}
<p>Dazu eigene Dorfgebäude (Ebene, Wüste, Taiga) und der Dorfbewohner <b>Süchtiger</b> als Händler.</p>
<h2 id="ps--craft">Zusammenspiel mit Craft</h2>
<ul>
<li>Keine Abhängigkeit in beide Richtungen; beide Mods laufen auch allein.</li>
<li>Doppelte Pflanzen (Hopfen, Kaffee, Tabak, Tomate in IC2/Farmer's Delight/Brewery) bleiben <b>getrennt</b>: jede Mod hat ihre eigene Kette.</li>
<li>Craft-Kaffee (IC2) lindert den Psychedelicraft-Kater (Tag <code>psychedelicraft:hangover_relief</code>).</li>
<li>Den Kater gibt es nur von Psychedelicraft-Alkohol – Craft-Bier/-Rum aus dem IC2-Fass und Brewery-Getränke lösen ihn nicht aus.</li>
</ul>'''
    page('ps-uebersicht', 'Überblick & Pflanzen', overview)

    # ================================================================== processing
    proc = f'''<h1>Psychedelicraft – Verarbeitung</h1>
<div class="tocpos"></div>
<h2 id="ps--trocknen">Trocknen</h2>
<p>Der {c('drying_table', 'Drying Table')} (oder {c('iron_drying_table', 'Iron Drying Table')}) trocknet mit
<b>Sonne</b>: je heller und je wärmer das Biom, desto schneller; im Regen unter freiem Himmel steht er still. Ein
<b>befeuerter Ofen</b> zwei Blöcke daneben (dazwischen Luft) heizt zusätzlich. Aus 9 Stück werden 3 getrocknete.</p>
{T(['Frisch', 'Getrocknet', 'Trockenzeit (Faktor)'], [
    [c('cannabis_buds', 'Cannabis Buds'), c('dried_cannabis_buds', 'Dried Cannabis Buds'), '1,0'],
    [c('cannabis_leaf', 'Cannabis Leaf'), c('dried_cannabis_leaf', 'Dried Cannabis Leaf'), '0,6'],
    [c('tobacco', 'Tobacco Leaf'), c('dried_tobacco', 'Dried Tobacco'), '0,6'],
    [c('coca_leaves', 'Coca Leaves'), c('dried_coca_leaves', 'Dried Coca Leaves'), '1,0'],
    [c('peyote', 'Peyote'), c('dried_peyote', 'Dried Peyote'), '1,5'],
    [c('belladonna_leaf', 'Belladonna Leaf'), c('dried_belladonna_leaf', 'Dried Belladonna Leaf'), '0,3'],
    [c('jimsonweed_leaf', 'Jimsonweed Leaf'), c('dried_jimsonweed_leaf', 'Dried Jimsonweed Leaf'), '1,1'],
    [v('poppy', 'Mohn'), c('dried_poppy', 'Dried Poppy'), '0,4'],
    [v('brown_mushroom', 'Brauner Pilz') + ' ' + v('red_mushroom', 'Roter Pilz'), c('brown_magic_mushrooms', 'Brown Magic Mushrooms') + ' ' + c('red_magic_mushrooms', 'Red Magic Mushrooms'), '0,4'],
])}
<h3 id="ps--rauchwaren">Rauchwaren und Esswaren (Werkbank)</h3>
{T(['Item', 'Rezept', 'Droge'], [
    [c('joint', 'Joint'), 'Papier – getrocknete Cannabisblüten – Papier (senkrecht)', 'Cannabis'],
    [c('cigarette', 'Cigarette'), 'Papier – getrockneter Tabak – Papier', 'Tabak'],
    [c('cigar', 'Cigar'), '6 getrockneter Tabak über 3 Papier', 'Tabak'],
    [c('blunt', 'Blunt'), '6 getrockneter Tabak über 3 getrockneten Cannabisblättern', 'Tabak + Cannabis'],
    [c('peyote_joint', 'Peyote Joint'), 'Papier – getrockneter Peyote – Papier', 'Peyote + etwas Tabak'],
    [c('hash_muffin', 'Hash Muffin'), '3 Cannabisblätter (getr.) / 3 Weizen / 3 Cannabisblätter (getr.)', 'Cannabis (verzögert)'],
    [c('harmonium', 'Harmonium'), 'Glowstone-Staub + getrockneter Tabak (formlos), färbbar', 'Harmonium (nur Bong/Pfeife)'],
    [c('cocaine_powder', 'Cocaine Powder'), '1 getrocknete Kokablätter', 'Kokain'],
])}
<p>{c('smoking_pipe', 'Pipe')} und {c('bong', 'Bong')} rauchen getrocknete Cannabisblüten, Tabak, Mohn, Tollkirschen-/Stechapfelblätter und Harmonium direkt aus dem Inventar.</p>

<h2 id="ps--brauen">Brauen, Reifen, Brennen</h2>
<ol class="steps">
<li><b>Maischen:</b> Den {c('mash_tub', 'Wooden Vat')} (3×3, fasst 9 Eimer) mit mindestens 2¼ Eimern Wasser füllen und
<b>8 Zutaten hineinwerfen</b> (Item fallen lassen). Es entsteht der Most der Zutat.</li>
<li><b>Gären:</b> Der Most gärt im Bottich in zwei Stufen (je Stufe meist 40 min); bleibt er danach zu lange stehen, wird er zu <b>Essig</b>.</li>
<li><b>Reifen:</b> Vergorenes im {c('oak_barrel', 'Fass')} (jede Holzart, fasst 8 Eimer) lagern – nur Vergorenes reift; je länger, desto „älter“ das Getränk (Wein: jung → leicht gereift → gereift → gut gereift).</li>
<li><b>Brennen:</b> Vergorenes, noch nicht Gereiftes in die {c('distillery', 'Distillery')} füllen; sie steht auf Feuer oder einem brennenden Lagerfeuer und gibt in einen angeschlossenen {c('flask', 'Flask')} ab. Jede Destillation hebt den Alkohol; die Schlempe landet im Kolben. Unvergorenes wird nicht gebrannt.</li>
<li><b>Abfüllen:</b> Mit {c('wooden_mug', 'Mug')}, {c('bottle', 'Bottle')}, {c('glass_chalice', 'Glass Chalice')}, {c('shot_glass', 'Shot Glass')} oder {c('stone_cup', 'Cup')} aus Fass, Bottich oder Kolben schöpfen; Flaschen passen ins {c('bottle_rack', 'Bottle Rack')}. Getränke lassen sich in der Werkbank umfüllen.</li>
</ol>
{T(['Zutat (8 ×, sofern nicht anders)', 'vergoren', 'gereift', 'gebrannt (+ gereift)'], [
    ['6 Weizen + 2 Hopfendolden', 'Bier (grün)', 'Bier', 'klares Bier'],
    ['Weizen', 'Maische', 'Brandy', 'Wodka (+ gereift: Weizenwhiskey)'],
    ['Kartoffeln', 'Bier', 'dunkles Bier', 'Wodka (+ gereift: Poitín)'],
    ['Weintrauben', 'Maische (Wein)', 'Wein jung … gut gereift', 'Brandy'],
    ['Reis', 'junger Reiswein', 'Reiswein', 'Brandy'],
    ['4 Wacholderbeeren + 2 Trauben + Zucker + Weizen', 'junge Maische', 'Maische', 'Gin'],
    ['Honig', 'junger Met', 'Met', 'Brandy'],
    ['Zuckerrohr', 'junger Basi', 'Basi', 'junger Rum (+ gereift: Rum)'],
    ['Mais', 'grünes Bier', 'Bier', 'Wodka (+ gereift: Whiskey)'],
    ['Äpfel', 'Cidre halbsüß → herb', 'Cidre', 'Brandy'],
    ['Ananas / Bananen', 'junger Wein / Bier', 'Wein / gereiftes Bier', 'Brandy'],
    ['Tomaten', 'Bier', 'gereiftes Bier', 'Wodka, Met, Whiskey (je nach Reife/Brennen); Essig = Ketchup'],
    ['Agavenblätter', 'Maische', '–', 'Mezcal (1×), Tequila Blanco (2×), Reposado (2× + gereift)'],
])}
{N('info', 'Wein und Bier haben Nährwert und lassen sich – wie im Original – nur trinken, wenn man Hunger hat. Milch lässt sich ebenfalls vergären (Blaand, gebrannt Arkhi).')}

<h2 id="ps--chemie">Chemie: Kolben, Tablett, Bunsenbrenner</h2>
<p>Im {c('flask', 'Flask')} mit Wasser kochen Pflanzen zu Extrakten aus (Nebenprodukt Kohle); Zusätze (Lapis, Zucker,
Kohle, Glasscherben) gehen als „Verunreinigung“ in die Flüssigkeit ein. Der {c('tray', 'Tray')} auf dem
{c('bunsen_burner', 'Bunsen Burner')} härtet Flüssigkeiten zu 3–6 Stück Pulver/Tabletten.</p>
{T(['Ausgangsstoff', 'Zwischenschritt', 'Ergebnis'], [
    ['Prunkwinden-Blüten/-Samen + Wasser (Kolben)', 'Prunkwinden-Extrakt', '2× gebrannt auf Papier → ' + c('lsd_square', 'LSA Square') + '; im Kolben zu Säure gebrannt → Tablett → ' + c('lsd_pill', 'LSD Tablet')],
    ['Prunkwinden-Extrakt (Tablett)', 'ohne Lapis / mit Lapis-Wasser', c('methamphetamine_powder', 'Crystal Meth') + ' / ' + c('blue_methamphetamine_powder', 'Blue Crystal Meth') + '; 4 Meth → 2 ' + c('extacy', 'Extacy')],
    ['Mohn + Wasser (Kolben)', 'Morphin', 'Tablett → ' + c('heroine_powder', 'Heroine') + '; 4 Heroin → ' + c('morphine_tablet', 'Morphine')],
    ['Tollkirschen-/Stechapfelsamen + Wasser', 'Extrakt → Atropin', 'trinken bzw. Spritze'],
    ['2 Kokainpulver + Wasser (Gefäß)', 'Kokainlösung', 'mit Ethanol auf dem Tablett → ' + c('crack_cocaine', 'Crack Cocaine')],
    ['Kohle/Holzkohle + Wasser', 'Petroleum', 'brennbar (Molotowcocktail)'],
    [c('obsidian_bottle', 'Obsidian Bottle') + ' → ' + c('obsidian_dust', 'Obsidian Dust'), '+ Lava im Gefäß', 'Badesalz'],
])}
<h3 id="ps--getraenke">Tee und Kaffee</h3>
<p>Je 2 Blätter/Bohnen in einem Gefäß mit Wasser: Cannabis-Tee, Koka-Tee, Peyote-Saft, Agavensaft; 2 Kaffeebohnen →
Kaffee (im Ofen erhitzt <b>heißer Kaffee</b> mit Wärme), in Drogen-Gefäßen → Koffein. {c('syringe', 'Syringe')} (Eisen
über Glas) spritzt Lösungen direkt.</p>
{N('tip', 'Die genauen Mengen, Zeiten und Verarbeitungsstufen jeder Flüssigkeit zeigt EMI (Kategorie „Flüssigkeits-Zubereitung“, Mengen in mB).')}'''
    page('ps-verarbeitung', 'Verarbeitung', proc)

    # ================================================================== drugs, effects, hangover
    drugs = f'''<h1>Psychedelicraft – Drogen, Wirkung, Kater</h1>
<div class="tocpos"></div>
<p class="lead">Jede Droge hat einen Pegel (0–1), der beim Konsum steigt – je nach Weg sofort (Rauchen, Spritze),
verzögert (Essen, Trinken) oder schleichend – und danach langsam abklingt. Was man sieht und spürt, hängt vom Pegel ab.</p>
<h2 id="ps--drogen">Drogen und ihre Wirkung</h2>
{T(['Droge', 'Quelle', 'Wirkung'], [
    ['Alkohol', 'alle vergorenen/gebrannten Getränke', 'Doppelbilder, Bewegungsunschärfe, Schwanken; danach Kater (unten)'],
    ['Cannabis', 'Joint, Blunt, Bong, Pfeife, Haschmuffin, Tee', 'langsamer laufen und abbauen (bis −50 %), träger Blick, Sättigungs-, Farb-, Bewegungs- und Kontext-Halluzinationen, Hunger'],
    ['Tabak', 'Zigarette, Zigarre, Blunt, Bong', 'blassere Farben'],
    ['Zauberpilze (braun/rot)', 'getrocknete Pilze essen', 'Farb-, Bewegungs- und Kontext-Halluzinationen, Schwanken, weniger Hunger'],
    ['Peyote', 'Peyote, Peyote-Joint, Saft', 'Farb- und Kontext-Halluzinationen'],
    ['LSD', 'LSA-Papier, LSD-Tablette', 'Regenbogen-, Blasen- und Splitterwellen, Bloom, Farben, Schwerelosigkeit, Zittern, schneller'],
    ['Kokain', 'Pulver, Crack, Koka-Tee', 'schneller laufen und abbauen, Herzschlag und Atem hörbar, Zittern, Bloom, Halluzinationen, ungewolltes Springen/Schlagen'],
    ['Koffein / Zucker', 'Kaffee, Koffeinlösung, süße Items', 'etwas schneller (bis +20 %), Herzschlag (Koffein), gelegentlich Springen/Schlagen'],
    ['Meth', 'Crystal Meth, Extacy', 'Zittern, Herzschlag, weniger Schmerz'],
    ['Morphin', 'Heroin, Morphintablette, Mohn in der Bong', 'Schmerzunterdrückung (weniger Schaden spürbar), Herzschlag, Zittern'],
    ['Atropin', 'Tollkirsche, Stechapfel', 'Herzrasen, Zittern, Farb-, Bewegungs- und Kontext-Halluzinationen'],
    ['Badesalz', 'Spritze', 'Bloom, Farben, Farbumkehr, Bewegungs-Halluzinationen, Springen/Schlagen'],
    ['Kava', 'Kava-Getränk', 'schläfrig, langsamer, leiser, blasser'],
    ['Harmonium', 'Bong/Pfeife', 'färbt die Welt in der Farbe des Harmoniums'],
    ['Wärme', 'heißer Kaffee', 'wärmt (Kälte)'],
    ['Schlafmangel', 'lange nicht geschlafen, Kater nach dem Aufwachen', 'müde, verschwommen'],
])}
{N('warn', 'Zu viel Essen und Trinken auf einmal überfüllt den Magen: Übelkeit und <b>Erbrechen</b> ({c("vomit", "Vomit")}). Mit einer {c("paper_bag", "Empty Paper Bag")} in der Nebenhand landet es in der Tüte. Wer sich zu oft übergibt, kann daran sterben.')}

<h2 id="ps--halluzinationen">Halluzinationen</h2>
<ul>
<li><b>Farbe/Sättigung/Bloom:</b> die Welt wird bunter, blasser, leuchtet oder kehrt die Farben um (Post-Shader).</li>
<li><b>Bewegung/Wellen:</b> Blöcke atmen, Wellen laufen durch die Landschaft (Regenbogen, Blasen, Splitter), die Sicht schwankt oder sieht doppelt.</li>
<li><b>Kontext:</b> Dinge, die nicht da sind – Fantasie-Mobs, falsche Blöcke, verzerrter Chat („Chat Distortion“), Hintergrundmusik je Droge.</li>
<li><b>Körper:</b> Herzschlag und Atem werden hörbar, Hände zittern, man springt oder schlägt ungewollt.</li>
</ul>
{N('info', 'Die Halluzinationen laufen nur im Client (Shader). Der Client-Teil des Ports (Phase 52.3, Sodium/Iris-Anpassung) ist noch nicht abgenommen; vorgesehen ist: bei aktivem <b>Iris-Shaderpack</b> schaltet Psychedelicraft seine eigenen Shader ab, Overlays und Kontext-Halluzinationen bleiben. Stärke und einzelne Effekte lassen sich in den Mod-Einstellungen (Modliste → Psychedelicraft) abschwächen.')}

<h2 id="ps--kater">Kater (neu in Craft)</h2>
{N('new', 'Im Original gibt es nach dem Rausch nur eine Aufwach-Animation und Schlafmangel – beides bleibt. Der Kater ist eine Ergänzung dieses Ports (freigegeben 2026-11-20, <a href="#/spec-52">Spezifikation §6</a>).')}
<p>Gemerkt wird der <b>höchste Alkoholwert</b> seit dem letzten Nüchternsein. Fällt der Alkohol unter 0,05 (oder man
schläft – das setzt den Alkohol auf 0), beginnt der Kater. Er ist als Effekt <b>Kater I–III</b> mit Restzeit im Inventar
zu sehen und dauert höchster Wert × 1 MC-Tag (höchstens 20 min). <b>Milch heilt ihn nicht.</b></p>
{T(['Stufe', 'ab Höchstwert', 'Wirkungen'], [
    ['I – leicht', '0,2', 'Übelkeit in Schüben (alle 60–120 s für 6 s), Abbaulähmung I'],
    ['II – mittel', '0,5', '+ Langsamkeit I, Hunger I, <b>Blendung</b>: am Tag unter freiem Himmel weiße Überblendung (bei Regen schwächer) – Schatten hilft'],
    ['III – schwer', '0,8', '+ Schwäche I, <b>Übergeben</b> alle 2–5 min: 4 Hunger weg, 8 s Übelkeit, Erbrochenes fällt zu Boden (oder in die Papiertüte in der Nebenhand)'],
])}
<p>Unter 0,2 gibt es keinen Kater. Wer während des Katers weitertrinkt, schiebt ihn auf: Anzeige und Zeit ruhen, danach
geht es mit dem neuen, höheren Wert weiter. Im Kreativ- und Zuschauermodus gibt es keinen Kater.</p>
{T(['Mittel', 'Wirkung'], [
    [v('potion', 'Wasserflasche') + ', Wasser aus Psychedelicraft-Gefäßen', '−2 min Restzeit (Wasser in Psychedelicraft-Gefäßen ist nur mit Kater trinkbar)'],
    ['Psychedelicraft-Kaffee, ' + h.chip('coffee') + ' (IC2-Kaffee, auch dunkel/kalt)', '−5 min Restzeit und eine Stufe leichter (nicht unter I)'],
])}
{N('info', 'Nur Psychedelicraft-Alkohol löst den Kater aus. Craft-Bier/-Rum (IC2-Fass) und Getränke aus [Let\'s Do] Brewery (eigener Rausch) nicht.')}'''
    page('ps-drogen', 'Drogen, Wirkung, Kater', drugs)
    return P
