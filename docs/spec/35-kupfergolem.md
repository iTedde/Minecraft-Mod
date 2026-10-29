# 35 – Kupfergolem und Kupfertruhe

Neuer Inhalt ohne Pack-Vorbild (Nutzer, 2026-10-29): „Es gibt ja Kupfergolems in Vanilla. Die würde ich gerne für
Craft nutzbar machen. Die müssen aber mehr Range haben, also z. B. 64,64,64 und auch mit den Iron Chest/Dia Chests
usw. arbeiten können.“

Den Kupfergolem und die Kupfertruhe gibt es erst ab Minecraft **1.21.9** („The Copper Age“), nicht in 1.21.1. Beide
werden deshalb als Craft-Inhalt nachgebaut (Paket `dev.craft.coppergolem`). Vorbild ist das Verhalten aus 1.21.9
(Java Edition, Stand minecraft.wiki 2026-10-29), mit diesen drei Änderungen:

1. Suchbereich **65 × 65 × 65** statt 65 × 17 × 65 (±32 in jede Richtung um den Golem).
2. Ziele sind **alle Item-Behälter** (Iron Chests aller Stufen, Truhen, Fässer, Behälter anderer Mods, Maschinen), nicht
   nur Holz-, Redstone- und Kupfertruhen.
3. Die Kupfertruhe ist ein eigener Block, weil `craft:copper_chest` schon die Iron-Chest-„Kupferkiste“ ist.

Entschieden (Nutzer, 2026-10-29): eigener Golem wie 1.21.9 · Kupfertruhe als Quelle nachbauen · Würfel ±32 · Ziele alle
Iron Chests, Vanilla-Truhen/-Fässer und alle Item-Behälter.

## 1. Kupfertruhe (Quelle)

| Eigenschaft | Umsetzung |
|---|---|
| ID / Name | `craft:golem_chest`, „Copper Golem Chest“ / „Kupfertruhe“ (Vorschlag, **F1**; Abgrenzung zur „Kupferkiste“ der Iron Chests) |
| Inventar | 27 Slots; zwei benachbarte gleich ausgerichtete Truhen bilden eine große Truhe mit 54 Slots (wie Vanilla-Truhe; schleichend setzen verhindert das) |
| Material / Härte | Härte 3, Explosionsfestigkeit 6, `mineable/pickaxe` + `requiresCorrectToolForDrops` (ab Steinspitzhacke) |
| Rezept | Truhe in der Mitte, 8 Kupferbarren außen (wie 1.21.9) |
| Oxidation | 4 Stufen (normal, angelaufen, verwittert, oxidiert) über Zufallsticks wie Vanilla-Kupferblöcke (`WeatheringCopper`); Honigwabe wachst, Axt (schleichend) entfernt Wachs oder eine Stufe, Blitz entfernt eine Stufe (**F2**: alle 8 Varianten oder nur eine Truhe ohne Oxidation) |
| Große Truhe aus unterschiedlichen Stufen | verbindet sich; beide Hälften nehmen die geringere Oxidation an, gewachst nur wenn beide gewachst |
| Trichter / Komparator / Rohre | wie eine Vanilla-Truhe (Item-Handler-Capability, Komparatorsignal) |
| Piglins | werden feindlich, wenn ein Spieler sie öffnet oder abbaut (wie in 1.21.9) |
| Kolben | lassen sich nicht verschieben |
| Darstellung | Truhenmodell wie die Iron Chests (`BlockEntityRenderer`), Kupfertexturen je Stufe aus den Vanilla-Kupferfarben (Python/Pillow) |
| Tab | eigener Abschnitt „Craft: Kupfergolem“ oder im Iron-Chests-Tab (**F3**) |

## 2. Kupfergolem (Kreatur)

### 2.1 Bau

- Kupferblock (jede Oxidationsstufe, gewachst oder nicht) setzen, dann geschnitzten Kürbis oder Kürbislaterne darauf.
  Der Kürbis muss zuletzt kommen (Spieler, Werfer, Enderman). Das Muster geht auch waagerecht und kopfüber.
- Der Kupferblock wird zur Kupfertruhe, ausgerichtet wie der Kürbis; der Golem erscheint an der Stelle des Kürbisses.
- Golem und Truhe übernehmen die Oxidationsstufe des Blocks, sind aber **nicht** gewachst.
- Grenzt die neue Truhe an eine passende Kupfertruhe, verbinden sich beide (siehe §1).
- Spawn-Ei: nur der Golem, ohne Truhe, unoxidiert, ungewachst.

Umsetzung: Vanilla 1.21.1 prüft Golem-Muster in `CarvedPumpkinBlock#trySpawnGolem`; das Kupfermuster kommt per
`BlockEvent.EntityPlaceEvent` (Spieler/Enderman) und eigenem Werfer-Verhalten für Kürbisse dazu, ohne Mixin (**F4**).

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

**Holen** (leere Hand): Der Golem geht zur nächsten **Kupfertruhe** im Suchbereich, öffnet sie (Deckelanimation, Ton)
und schaut **3 Sekunden** hinein. Er nimmt **bis zu 16** Stück des ersten Items (vorderster belegter Slot). Ist die
Truhe leer, geht er zur nächsten noch nicht besuchten Kupfertruhe.

**Abgeben** (Item in der Hand): Er geht zum nächsten **Ziel** im Suchbereich und schaut 3 Sekunden hinein. Er gibt ab,
wenn das Ziel **leer** ist oder schon **dasselbe Item** enthält (gleiches Item, Komponenten werden wie in 1.21.9
ignoriert: Haltbarkeit, Verzauberung, Name, Inhalte von Shulkerkisten/Bündeln). Abgelegt wird in den vordersten
passenden Slot; was nicht passt, behält er und sucht weiter.

**Ziele**: jeder Block mit `Capabilities.ItemHandler.BLOCK` (abgefragt ohne Seite, sonst von der Seite zum Golem),
darunter alle Iron Chests, Holz-/Redstone-Truhen, Fässer, Kupfertruhen und Behälter/Maschinen anderer Mods. Ein Ziel
gilt als leer, wenn alle Slots leer sind; ob es ein Item annimmt, entscheidet `insertItem` (Simulation vorab).
Ausgenommen: Endertruhen (Vanilla), Kreaturen/Loren, liegende Items (**F5**: weitere Ausnahmen, z. B. Maschinen
oder EnderStorage-Truhen; Hinweis: mit „alle Item-Behälter“ füllt der Golem auch leere Öfen und Maschinen).

**Suchbereich**: 65 × 65 × 65 Blöcke um den Block des Golems (±32). Erreichbar sind Behälter, deren Block höchstens
1 über oder 2 unter den Füßen liegt, sobald er daneben steht (wie 1.21.9). Gesucht wird über die Block-Entities der
Chunks im Bereich (höchstens 5 × 5 Chunks), nicht über alle Blöcke.

**Gedächtnis**: Er merkt sich bis zu 9 besuchte Behälter. Nach 10 erfolglosen läuft er 7 Sekunden umher, vergisst
alle und sucht von vorn. Nach einer erfolgreichen Abgabe vergisst er ebenfalls alle.

**Spieler**: Rechtsklick mit leerer Hand nimmt dem Golem sein Item ab.

### 2.4 Oxidation, Wachs, Statue

- Vier Stufen wie die Kupferblöcke. Jede dauert **420–460 Minuten** (Spielzeit über `gameTime`, läuft also auch in
  entladenen Chunks weiter; beim Laden wird nachgeholt).
- Honigwabe wachst (keine Oxidation, kein Blitz, keine Statue); Axt entfernt Wachs, sonst eine Stufe. Blitz entfernt
  eine Stufe.
- Ein **oxidierter, ungewachster** Golem ganz an der Luft (nicht unter Wasser, nicht auf Stufen/Teilblöcken, innerhalb
  der Bauhöhe) wird mit **0,58 % pro Tick** (im Mittel ≈ 8,6 s) zur **Kupfergolem-Statue** in zufälliger Pose. Gehaltene
  Items fallen heraus, ein Name bleibt.
- Statue: Block `craft:copper_golem_statue` (4 Oxidationsstufen, 4 Posen). Axt entfernt eine Stufe; eine unoxidierte
  Statue wird mit der Axt wieder zum Golem (**F6**: Statue übernehmen oder den Golem einfach bei „oxidiert“ stehen lassen).

### 2.5 Darstellung

Eigenes Modell (`EntityModel`, Kopf mit Antenne, kurze Arme, Item in der Hand) und vier Texturen je Stufe, selbst
gezeichnet mit Python/Pillow im Vanilla-Stil. Animationen: Gehen, Truhe untersuchen, Item halten. Geräusche:
Vanilla-Kupfer- und Eisengolem-Klänge in höherer Tonlage (eigene Sounds gibt es in 1.21.1 nicht).

## 3. Weiteres

- Lang (DE/EN), Datagen für Rezepte, Loot (Truhe droppt sich mit Inhalt verstreut, Statue sich selbst), Tags
  (`minecraft:mineable/pickaxe`, `c:chests`).
- EMI: Info-Seite zum Bau des Golems und zum Verhalten.
- Herkunft (`spec/24`), Wiki-Abschnitt, Testplan, GameTests (Bau aus Muster, Holen/Abgeben in Iron Chest, Reichweite
  ±32, Abbruch nach 10 Fehlversuchen).
- `docs/abweichungen.md`: Kupfergolem/-truhe als Nachbau aus 1.21.9, eigene Truhen-ID, größere Reichweite, mehr Ziele.

## 4. Teilphasen

| Teil | Inhalt |
|---|---|
| 35.0 | diese Spezifikation |
| 35.1 | Kupfertruhe (Block, Oxidation, große Truhe, GUI, Renderer, Rezept) |
| 35.2 | Golem: Kreatur, Bau aus Muster, Modell/Texturen, Oxidation/Wachs |
| 35.3 | Golem: Item-Transport (Suche, Holen, Abgeben, Gedächtnis), GameTests |
| 35.4 | Statue (falls **F6** ja) |
| 35.5 | EMI, Herkunft, Wiki, Testplan |

## 5. Offene Fragen

- **F1** Name/ID der Truhe: `craft:golem_chest` „Kupfertruhe“ – passt das neben der „Kupferkiste“?
- **F2** Kupfertruhe mit allen 4 Oxidationsstufen + gewachst (8 Blöcke) wie 1.21.9 oder nur eine nicht oxidierende Truhe?
- **F3** Kreativ-Tab: eigener Tab oder Iron-Chests-Tab?
- **F4** Bau per Event ohne Mixin (Werfer nur über eigenes Verhalten) – einverstanden?
- **F5** Ausnahmen von „alle Item-Behälter“ (z. B. Maschinen, EnderStorage-Truhen, Tesserakte)?
- **F6** Statue übernehmen?
