# 19 – RedPower-World-Edelsteinwerkzeuge (Phase 17.2)

Nutzerwunsch 2026-10-17: „In FTB gab es auch Emerald Spitzhacken, Ruby und Saphire, mach die auch nochmal rein.“
Entscheidung: **komplettes Set** je Edelstein. „Emerald“ ist der **Grüne Saphir** (RedPower hat keine
Smaragdwerkzeuge). Quelle: RedPower 2 World (im Pack in `ftb ultimate/mods/redpowermechanical.zip`), Handsäge aus
`redpowercore.zip`; dekompiliert nach `reference/decompiled/redpower/` (nur `RedPowerWorld`, `ItemCustom*`,
`ItemSickle`, `base/ItemHandsaw`, ohne Remapping – Minecraft-Namen obfuskiert).

## 1. Material (`RedPowerWorld` 236–238)

`EnumHelper.addToolMaterial(name, 2, 500, 8.0F, 3, 12)` für `RUBY`, `GREENSAPPHIRE`, `SAPPHIRE` – alle drei gleich:

| Wert | 1.4.7 | Umsetzung 1.21 |
|---|---|---|
| Abbaustufe | 2 (Eisen) | `#minecraft:incorrect_for_iron_tool` |
| Haltbarkeit | 500 | 500 |
| Abbautempo | 8.0 | 8.0 |
| Schadensbonus | 3 (= Diamant) | 3.0 |
| Verzauberbarkeit | 12 | 12 (Sichel 20, s. u.) |
| Reparatur | `getIsRepairable`: der eigene Edelstein | Amboss mit `#c:gems/ruby` / `#c:gems/sapphire` / `#c:gems/green_sapphire` |

Schaden/Angriffstempo: in 1.4.7 gleichen die Edelsteinwerkzeuge im Schaden den Diamantwerkzeugen (Bonus 3). 1.21 hat
neue Werkzeug-Grundwerte (Axt 9 statt 6, Angriffstempo); die Edelsteinwerkzeuge bekommen die **Grundwerte der
1.21-Diamantwerkzeuge** (Schwert 7 / 1,6, Spitzhacke 5 / 1,2, Axt 9 / 1,0, Schaufel 5,5 / 1,0, Hacke 1 / 4,0), damit
das Verhältnis zu Diamant wie im Pack bleibt (`abweichungen.md`).

## 2. Werkzeuge und Rezepte

G = Edelstein (im Pack RedPowers Rubin/Saphir/Grüner Saphir; hier die GT-Edelsteine über `#c:gems/…`), S = Stock,
I = Eisenbarren. Spiegelbar wie alle 1.4.7-Werkbankrezepte.

| Werkzeug | Rezept | Klasse | Besonderheiten |
|---|---|---|---|
| Spitzhacke | `GGG/ S / S ` | Vanilla-Spitzhacke | Werkzeugklasse `pickaxe` Stufe 2 |
| Schaufel | `G/S/S` | Vanilla-Schaufel | |
| Axt | `GG/GS/ S` | Vanilla-Axt | |
| Schwert | `G/G/S` | Vanilla-Schwert | |
| Hacke | `GG/ S/ S` | Vanilla-Hacke | `setMaxDamage(500)` (wie das Material) |
| Sichel | ` G /  G/SG ` | `ItemSickle` | §3 |
| Handsäge | `SSS/ II/ GG` | `ItemHandsaw` | §4 |

Namen (Lang RedPower): „Ruby Pickaxe“, „Green Sapphire Pickaxe“, „Sapphire Pickaxe“ usw.; „Ruby Sickle“, „Ruby
Handsaw“. Deutsch: „Rubin-Spitzhacke“, „Grünsaphir-Spitzhacke“, „Saphir-Spitzhacke“ … „Rubin-Sichel“, „Rubin-Handsäge“.

Registry-IDs: `craft:ruby_pickaxe`, `craft:green_sapphire_pickaxe`, `craft:sapphire_pickaxe`, analog `_shovel`,
`_axe`, `_sword`, `_hoe`, `_sickle`, `_handsaw`.

Texturen: Original-Icons aus `eloraam/world/worlditems1.png` (Spalte 0 Rubin, 1 Grüner Saphir, 2 Saphir; Zeile 2
Schwert, 3 Schaufel, 4 Spitzhacke, 5 Axt, 6 Hacke; Sicheln Zeile 1, Spalten 5–7) und `eloraam/base/items1.png`
(Handsägen Index 19–21).

## 3. Sichel (`ItemSickle`)

- `ItemTool` mit Schaden 3 (+ Material 3), Stapelgröße 1, Verzauberbarkeit **20** (`getItemEnchantability`),
  Haltbarkeit 500 (Material).
- Abbautempo: auf Laub (`BlockLeavesBase`) das Materialtempo 8, sonst wie ein Werkzeug ohne effektive Blöcke.
- Beim Abbau eines Blocks (`onBlockDestroyed`), nur durch Spieler:
  - **Laub:** alle Laubblöcke im Würfel 3×3×3 (Radius 1) um den Block werden abgebaut – mit Drops, wenn der Spieler
    sie ernten kann (`harvestBlock`) –, dann entfernt.
  - **sonst:** in der Ebene des Blocks im Quadrat 5×5 (Radius 2) jeder Block, der `BlockFlower` ist und dessen
    Material **nicht** das der Seerose (`Material.plants`) ist, mit Drops abgebaut. In 1.4.7 trifft das **nur hohes
    Gras/Farn und tote Büsche** (`Material.vine`); Blumen, Setzlinge, Feldfrüchte, Pilze, Stängel und Netherwarzen
    (alle `Material.plants`) bleiben stehen (Originalverhalten, so übernommen).
  - Wurde mindestens ein Block entfernt: **1** Haltbarkeit Schaden (einmal, nicht je Block). Andere Blöcke abbauen
    kostet keine Haltbarkeit.
- 1.21: Laub = `#minecraft:leaves`; Gras/Farn/tote Büsche = `minecraft:short_grass`, `fern`, `dead_bush` (die
  zweiblöckigen `tall_grass`/`large_fern` gab es 1.4.7 nicht und bleiben stehen). Verzauberungen wie ein
  Grabwerkzeug (Effizienz, Behutsamkeit, Glück, Haltbarkeit, Reparatur).
- RedPower hat dazu Holz-/Stein-/Eisen-/Diamant-/Goldsicheln; nicht im Umfang (Nutzerwunsch nur Edelsteine).

## 4. Handsäge (`ItemHandsaw`)

`ItemPartialCraft` (verliert beim Handwerk Haltbarkeit statt verbraucht zu werden), Haltbarkeit **640**, Schärfe 1,
Stapelgröße 1, nicht verzauberbar, 3D-Werkzeugdarstellung. Einziger Zweck in RedPower: Mikroblöcke (Abdeckungen,
Platten) zuschneiden – die gibt es im Mod nicht. Die Handsäge wird daher **ohne Funktion** umgesetzt (Item mit
Haltbarkeit, Rezept wie im Pack).

## 5. GregTech-Rückgewinnung (`applyUsagesForMaterials`, `GT_Mod` 4655–4670)

GT ruft die Rückgewinnung für Rubin, Saphir und Grünen Saphir **nur mit Macerator** (kein Ofen) auf. Die
RedPower-Rezepte passen in folgende Raster (Macerator, nur unbeschädigt, Ergebnis Edelsteinstaub):

| Werkzeug | Staub |
|---|---|
| Spitzhacke | 3 |
| Axt | 3 |
| Sichel (Raster ` M /  M/SM `) | 3 |
| Schwert | 2 |
| Hacke | 2 |
| Schaufel | 1 |
| Handsäge | – (Eisen + Edelstein passt in kein Raster) |

## 6. Teilphase

| Teilphase | Inhalt |
|---|---|
| 17.2 | diese Spezifikation; Items, Rezepte, Texturen, Lang, Tags, GT-Rückgewinnung, Tests |
