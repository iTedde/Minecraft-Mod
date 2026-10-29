# 35 – Kupfergolem

Neuer Inhalt ohne Pack-Vorbild (Nutzer, 2026-10-29): „Es gibt ja Kupfergolems in Vanilla. Die würde ich gerne für
Craft nutzbar machen. Die müssen aber mehr Range haben, also z. B. 64,64,64 und auch mit den Iron Chest/Dia Chests
usw. arbeiten können.“

Den Kupfergolem gibt es erst ab Minecraft **1.21.9** („The Copper Age“), nicht in 1.21.1. Er wird deshalb als
Craft-Inhalt nachgebaut (Paket `dev.craft.coppergolem`). Vorbild ist das Verhalten aus 1.21.9 (Java Edition, Stand
minecraft.wiki 2026-10-29), mit diesen Änderungen:

1. Suchbereich **65 × 65 × 65** statt 65 × 17 × 65 (±32 in jede Richtung um den Golem).
2. Quelle ist die **Kupferkiste der Iron Chests** (`craft:copper_chest`, 45 Slots) statt der Vanilla-Kupfertruhe; sie
   bekommt dafür Oxidation wie die Kupfertruhe aus 1.21.9.
3. Ziele sind zusätzlich **alle Iron Chests** und Fässer, nicht nur Holz-, Redstone- und Kupfertruhen.
4. Keine Statue: Ein oxidierter Golem bleibt ein (langsam arbeitender) Golem.

Entschieden (Nutzer, 2026-10-29): Verhalten wie 1.21.9 · Iron-Chest-Kupferkiste als Quelle, mit Oxidation · Suchbereich
±32 · Ziele Iron Chests, Truhen, Fässer – **keine** beliebigen Item-Behälter (Maschinen o. Ä.), „da gibt's Chaos“ ·
keine Statue · Bau-Erkennung per Event ohne Mixin · alles im Kreativ-Tab der Iron Chests.

## 1. Kupferkiste mit Oxidation (Änderung an Phase 20)

Die Kupferkiste (`spec/22` §1) bleibt in Größe (45 Slots, 9 × 5), GUI, Rezept (`KKK/KCK/KKK`) und Upgrades unverändert.
Neu:

| Eigenschaft | Umsetzung |
|---|---|
| Oxidation | Blockstate `oxidation` = `unaffected` / `exposed` / `weathered` / `oxidized`, Fortschritt über Zufallsticks wie Vanilla-Kupferblöcke (`WeatheringCopper`-Regeln: Wahrscheinlichkeit, Bremse durch weniger oxidierte Nachbarn) |
| Wachs | Blockstate `waxed` (boolean); Honigwabe wachst (Partikel, Ton, Honigwabe wird verbraucht), gewachste Kiste oxidiert nicht |
| Axt | schleichend Rechtsklick: entfernt Wachs, sonst eine Stufe (Haltbarkeit −1, Partikel wie Vanilla-Kupfer); ohne Schleichen öffnet sich wie bisher die GUI |
| Blitz | wie Vanilla-Kupfer: die getroffene Kiste (oder die hinter dem Blitzableiter) wird ganz frisch, Kupfer in der Nähe verliert zufällig je eine Stufe |
| Inhalt / Funktion | unabhängig von der Stufe; Oxidation ist nur optisch und für den Golem egal |
| Item | eine einzige Kupferkiste als Item (frisch); Abbauen droppt die frische Kiste, Stufe und Wachs gehen verloren (F1, Nutzer 2026-10-29) |
| Umsetzung (35.1) | 8 Blöcke (`craft:copper_chest`, `exposed_`/`weathered_`/`oxidized_copper_chest`, je mit `waxed_`), alle `IronChestType.COPPER` mit demselben Block-Entity-Typ; Stufenwechsel behält das Block-Entity (`IronChestBlock#onRemove`); Data Maps `neoforge:oxidizables`/`waxables`, damit Vanilla-Zufallsticks, Honigwabe, Axt und Blitz greifen |
| Upgrades | Kupfer → Eisen / Silber wie bisher, Oxidation entfällt mit dem Wechsel; Holz → Kupfer ergibt eine frische Kiste |
| Darstellung | Renderer wählt die Textur nach Stufe: 4 Kupferkisten-Texturen (Python/Pillow aus `copperchest.png`, Farben der Vanilla-Kupferstufen) |
| Komparator, Trichter, Rohre | unverändert |

## 2. Kupfergolem (Kreatur)

### 2.1 Bau

- Kupferblock (jede Oxidationsstufe, gewachst oder nicht) setzen, dann geschnitzten Kürbis oder Kürbislaterne darauf.
  Der Kürbis muss zuletzt kommen (Spieler, Werfer, Enderman). Das Muster geht auch waagerecht und kopfüber.
- Der Kupferblock wird zur **Kupferkiste** (Iron Chests), ausgerichtet wie der Kürbis; der Golem erscheint an der
  Stelle des Kürbisses.
- Golem und Kiste übernehmen die Oxidationsstufe des Blocks, sind aber **nicht** gewachst.
- Spawn-Ei: nur der Golem, ohne Kiste, unoxidiert, ungewachst.

Umsetzung ohne Mixin: `BlockEvent.EntityPlaceEvent` (Spieler, Enderman) prüft das Muster, nachdem der Kürbis steht;
für Werfer ein eigenes `DispenseItemBehavior` für Kürbis/Kürbislaterne, das das Vanilla-Verhalten (Schneegolem,
Eisengolem, Kürbis aufsetzen) beibehält und das Kupfermuster ergänzt.

### 2.2 Werte

| Wert | 1.21.9 | Craft |
|---|---|---|
| Leben | 12 (6 Herzen) | 12 |
| Tempo | 0,2 | 0,2 |
| Hitbox | 0,49 × 0,98 | 0,49 × 0,98 |
| Fallschaden | keiner | keiner |
| Feindseligkeit | passiv | passiv |
| Drop beim Tod | 1–3 Kupferbarren (+1 Maximum je Plünderung-Stufe) | gleich |
| `FOLLOW_RANGE` | – | 48 (Wegfindung über die größere Reichweite) |

### 2.3 Item-Transport

**Holen** (leere Hand): Der Golem geht zur nächsten **Kupferkiste** im Suchbereich, öffnet sie (Deckelanimation, Ton)
und schaut **3 Sekunden** hinein. Er nimmt **bis zu 16** Stück des ersten Items (vorderster belegter Slot). Ist die
Kiste leer, geht er zur nächsten noch nicht besuchten Kupferkiste. Stufe und Wachs der Kiste sind egal.

**Abgeben** (Item in der Hand): Er geht zum nächsten **Ziel** im Suchbereich und schaut 3 Sekunden hinein. Er gibt ab,
wenn das Ziel **leer** ist oder schon **dasselbe Item** enthält (gleiches Item, Komponenten werden wie in 1.21.9
ignoriert: Haltbarkeit, Verzauberung, Name, Inhalte von Shulkerkisten/Bündeln). Abgelegt wird in den vordersten
passenden Slot; was nicht passt, behält er und sucht weiter.

**Ziele** (feste Liste, Block-Tag `craft:copper_golem_targets`):

| Behälter | Herkunft |
|---|---|
| Truhe, Redstone-Truhe | Vanilla (wie 1.21.9; große Truhen als Ganzes) |
| Fass | Vanilla (neu gegenüber 1.21.9) |
| Eisen-, Gold-, Diamant-, Kupfer-, Silber-, Kristall-, Obsidiankiste | Iron Chests (Kupferkiste ist wie in 1.21.9 die Kupfertruhe zugleich Quelle und Ziel) |

Nicht dabei: Maschinen, Öfen, Trichter, Shulkerkisten, Endertruhen, EnderStorage, Tesserakte, Behälter anderer Mods,
Loren, liegende Items. Durch den Tag lässt sich die Liste per Datenpaket erweitern. Zugegriffen wird über
`Capabilities.ItemHandler.BLOCK` (ohne Seite).

**Suchbereich**: 65 × 65 × 65 Blöcke um den Block des Golems (±32). Erreichbar sind Behälter, deren Block höchstens
1 über oder 2 unter den Füßen liegt, sobald er daneben steht (wie 1.21.9). Gesucht wird über die Block-Entities der
Chunks im Bereich (höchstens 5 × 5 Chunks), nicht über alle Blöcke.

**Gedächtnis**: Er merkt sich bis zu 9 besuchte Behälter. Nach 10 erfolglosen läuft er 7 Sekunden umher, vergisst
alle und sucht von vorn. Nach einer erfolgreichen Abgabe vergisst er ebenfalls alle.

**Spieler**: Rechtsklick mit leerer Hand nimmt dem Golem sein Item ab.

### 2.4 Oxidation und Wachs

- Vier Stufen wie die Kupferblöcke. Jede dauert **420–460 Minuten** (Spielzeit über `gameTime`, läuft also auch in
  entladenen Chunks weiter; beim Laden wird nachgeholt).
- Honigwabe wachst (keine Oxidation, Blitz wirkungslos); Axt entfernt Wachs, sonst eine Stufe. Blitz entfernt eine
  Stufe.
- **Keine Statue** (Nutzer): Der oxidierte Golem bleibt bei Stufe 4 stehen und arbeitet normal weiter.

### 2.5 Darstellung

Eigenes Modell (`EntityModel`, Kopf mit Antenne, kurze Arme, Item in der Hand) und vier Texturen je Stufe, selbst
gezeichnet mit Python/Pillow im Vanilla-Stil. Animationen: Gehen, Kiste untersuchen, Item halten. Geräusche:
Vanilla-Kupfer- und Eisengolem-Klänge in höherer Tonlage (eigene Sounds gibt es in 1.21.1 nicht).

### 2.6 Umsetzung (35.2)

- `CopperGolem` (`AbstractGolem`), Stufe als synchronisierter Wert, Wachs und nächster Oxidationszeitpunkt
  (`gameTime`) im NBT; beim Laden holt der erste Tick alle verstrichenen Stufen nach.
- Blitz: eine Stufe weniger (ein Treffer je Blitz, obwohl er mehrere Ticks trifft); der Golem fängt kein Feuer und
  nimmt keinen Blitzschaden. Honigwabe/Axt am Golem auch ohne Schleichen.
- Bau: `BlockEvent.EntityPlaceEvent` merkt den Kürbis vor, geprüft wird nach dem Tick (der Enderman meldet vor dem
  Setzen und für den Block darunter). Kupferblock unter dem Kürbis hat Vorrang vor seitlich/oben. Werfer: eigenes
  Verhalten für geschnitzten Kürbis und Kürbislaterne, das an das Vanilla-Verhalten weitergibt, wenn ein
  Schnee-/Eisengolem entsteht oder kein Kupferblock angrenzt; der Kürbis schaut dann in Werferrichtung.
- Modell 64 × 32 (`CopperGolemModel`), Texturen aus den Vanilla-Kupferblöcken je Stufe
  (`tools/scripts/copper_golem_textures.py <client-extra-Jar>`); Item zwischen beiden Händen. Geräusche:
  Eisengolem-Treffer/-Tod mit 1,6-facher Tonhöhe, Schritte Kupfer.
- Drop über Loot-Tabelle `craft:entities/copper_golem` (Datagen `CraftEntityLoot`).

## 3. Weiteres

- Spawn-Ei im Kreativ-Tab der Iron Chests, direkt hinter den Kisten.
- Lang (DE/EN): „Copper Golem“ / „Kupfergolem“; Tags `craft:copper_golem_targets`.
- EMI: Info-Seite zum Bau des Golems und zum Verhalten; Info an der Kupferkiste zur Oxidation.
- Herkunft (`spec/24`), Wiki-Abschnitt (Iron Chests + Kupfergolem), Testplan, GameTests (Bau aus Muster wird
  Kupferkiste, Holen aus Kupferkiste, Abgeben in Diamantkiste/Fass, Reichweite ±32, kein Abgeben in einen Ofen,
  Abbruch nach 10 Fehlversuchen, Oxidation/Wachs/Axt an Kiste und Golem).
- `docs/abweichungen.md`: Kupfergolem als Nachbau aus 1.21.9 mit Iron-Chest-Kupferkiste als Quelle, größerer
  Reichweite, mehr Zielen, ohne Statue; Oxidation der Kupferkiste als Abweichung von Iron Chests (`spec/22`).

## 4. Teilphasen

| Teil | Inhalt |
|---|---|
| 35.0 | diese Spezifikation |
| 35.1 | Kupferkiste: Oxidation, Wachs, Axt, Blitz, Texturen |
| 35.2 | Golem: Kreatur, Bau aus Muster (Event + Werfer), Modell/Texturen, Oxidation/Wachs |
| 35.3 | Golem: Item-Transport (Suche, Holen, Abgeben, Gedächtnis, Ziel-Tag), GameTests |
| 35.4 | EMI, Herkunft, Wiki, Testplan |

## 5. Offene Fragen

Keine. F1 entschieden (Nutzer, 2026-10-29): abgebaute Kupferkiste droppt frisch.
