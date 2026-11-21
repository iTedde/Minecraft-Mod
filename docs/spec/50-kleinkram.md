# 50 – Kleinkram aus dem Pack: OmniTools, Obsidian Pressure Plates, Gravity Gun, Secret Rooms

Nutzer, 2026-11-12: „Omnitools, Obsidian Pressure Plates, Gravity Gun, Secret Rooms“ aus FTB Ultimate. Quellen:
`ftb ultimate/mods/omnitools.zip` (OmniTools 3.0.4, braucht CoFHCore), `obsidiplates-universal-1.2.0.3.zip`
(ObsidiPlates 1.2.0), `GravityGun1.4.7v2.zip` (iChun, GraviGun 1.4.7v2), `secretroomsmod.zip` (AbrarSyed,
SecretRoomsMod). Dekompiliert 2026-11-13 nach `reference/decompiled/{omnitools,obsidiplates,gravitygun,secretrooms}`
(Vineflower, ohne Remapping). Pack-Configs: `config/cofh/omnitools.cfg` + `cofh/lexicon.whitelist`,
`config/ObsidiPlates.cfg`, `config/GraviGun.cfg`, `config/SecretRoomsMod.cfg`.

Obfuskierte Vanilla-Namen über `ftb ultimate/instmods/minecraft.jar` aufgelöst (2026-11-13): `up.n` Diamant, `up.o`
Eisenbarren, `up.p` Goldbarren, `up.aC` Redstone, `up.aL` Buch, `up.bn` Enderperle, `up.bS` Netherstern, `up.av`
Holztür, `up.aB` Eisentür, `up.aI` Ton (Item), `up.bm` verrottetes Fleisch; `amq.as` Obsidian (Härte 50, Festigkeit
2000), `amq.P` Glas, `amq.ae` Wolle (Meta 9 = cyan), `amq.A` Holzbretter, `amq.y` Erde, `amq.H` Sand, `amq.at` Fackel,
`amq.bn` Falltür, `amq.aM` Hebel, `amq.aU` Steinknopf, `amq.aP` Holzdruckplatte, `amq.aK` Bruchsteintreppe, `amq.ax`
Truhe, `amq.bg` Glowstone, `amq.bj` Kuchen, `amq.ap` TNT.

Paket je Ursprungs-Mod: `…omnitools`, `…obsidiplates`, `…gravitygun`, `…secretrooms`.

## 1 OmniTools

Nur **OmniWrench, Mystischer Sockel (Plinth) und Forge Lexicon** – die übrigen Artefakte (Sands of Time, Weather
Crystal) sind im Pack nicht erreichbar (`ItemArtifact` listet nur Metadaten 0, Configs nur `lexicon.enable`).

### 1.1 Prototype OmniWrench (`craft:omniwrench`)

- Stapel 1, **unzerstörbar**, kein Strom. Rezept `D D / G / W` (D Diamant, G Goldbarren, W cyanfarbene Wolle).
- Rechtsklick auf einen Block (`onItemUseFirst`, vor dem Block):
  1. **Vanilla-drehbar** (`BlockUtils.canRotate`, wie TE-Halbmondhammer): drehen; mit Schleichen „alternativ“ drehen.
  2. **IC2-`Wrenchable`** (Craft: `ic2/…/Wrenchable`): Schleichen spiegelt die Seite. Klick auf die Front
     (`getFacing`) und `wrenchCanRemove` → Block **immer** als `getWrenchDrop` abbauen (anders als der IC2-Schlüssel ohne
     Verlustchance und ohne EU). Sonst Front auf die geklickte Seite drehen (oben/unten nur bei Energiequelle+-senke,
     z. B. Trafos/Speicher). MFFS-Rechteprüfung (`wrenchCanManipulate`) wie beim MFFS-Multiwerkzeug.
  3. TE-`IReconfigurableFacing`: `rotateBlock`.
- Gilt außerdem als **BuildCraft-/TE-Schraubenschlüssel** (`ToolWrench`: Motoren, Rohre, Quarry, Raffinerie, TE-Blöcke
  abbauen/drehen), als Railcraft-Brecheisen (entfällt, kein Railcraft) und für die Nuclear-Control-Blöcke, die
  heute nur `WrenchItem` erkennen.
- Optional laut Pack-Config: `wrench.logic2=false` (kein Log) – entfällt.

### 1.2 Mystischer Sockel (`craft:mystical_plinth`)

- Block ¾ hoch (0–0,75), Rezept `GWG / OOO` (G Goldbarren, W cyanfarbene Wolle, O Obsidian), Leuchten wie im
  Original (`getLightValue`, in 50.1 nachlesen), abbaubar mit Spitzhacke.
- **Lexicon einsetzen:** Rechtsklick (ohne Schleichen) mit dem Forge Lexicon auf einen leeren Sockel → Blitz (nur
  optisch, `EntityLightningBoltFake`) + Schein-Explosion (Partikel, kein Schaden), Lexicon wird verbraucht (außer
  Kreativ), der Sockel wird zum **Sockel mit Lexicon** (Block mit Block-Entity, aufgeschlagenes Buch wie der
  Zaubertisch, dreht sich zum Spieler in 3 Blöcken Umkreis).
- Abbauen/Schraubenschlüssel: Inhalt + Lexicon fallen heraus (`dismantle`).

### 1.3 Forge Lexicon (`craft:forge_lexicon`)

Wandelt Gegenstände **zwischen gleichwertigen Varianten** um (Ore Dictionary → in 1.21 **gemeinsame `c:`-Tags**), z. B.
Craft-Zinnbarren ↔ Mekanism-Zinnbarren, Craft-Kupferstaub ↔ Create-/Mekanism-Kupferstaub.

- Öffnen: Schleichen + Rechtsklick mit dem Lexicon in der Hand (tragbar) oder Rechtsklick auf den Sockel mit Lexicon.
- GUI: Eingangsfeld, Auswahlfeld (Vorschau der Zielvariante, Pfeile ◀ ▶ blättern durch alle Varianten des Tags),
  Ausgangsfeld, Knopf „Umwandeln“: der **ganze Eingangsstapel** wird in die gewählte Variante umgewandelt, soweit
  der Ausgang Platz hat (Rest bleibt im Eingang). Die Auswahl bleibt für gleichartige Eingänge erhalten.
- Erlaubte Gruppen (Pack: Whitelist `cofh/lexicon.whitelist`, 197 Einträge: Erze, Stäube, Barren, Nuggets u. a.,
  auch IC2-Speicherzellen-Namen): Abbildung `oreX`/`dustX`/`ingotX`/`nuggetX` → `c:ores/x`, `c:dusts/x`,
  `c:ingots/x`, `c:nuggets/x`; nur Einträge der Whitelist, für die es den `c:`-Tag gibt. Liste als Item-Tag
  `craft:lexicon_groups`-Datei (Datagen), damit Packersteller ergänzen können.
- Tragbar: Inhalt fällt beim Schließen zurück ins Inventar (wie Werkbank).

## 2 Obsidian Pressure Plates

- `craft:obsidian_pressure_plate`: Druckplatte, die **nur Spieler** auslöst (`EnumMobType.players`), Textur/Klang
  wie Obsidian, Härte 12,5 (Obsidian 50 – im Original wirklich 12,5), Explosionsfestigkeit wie Obsidian (1200), mit
  **jeder Spitzhacke** abbaubar (Material Stein; die Erntestufe 3 setzt Forge nur für den Obsidianblock selbst).
- Rezept `##` (2 Obsidian waagerecht) → 1 Platte.
- Signal 15 solange ein Spieler (nicht Zuschauer) darauf steht, Abschalten nach 20 Ticks wie Stein-Druckplatten.

## 3 Gravity Gun (`craft:gravity_gun`, `craft:charged_gravity_gun`)

Pack-Config (`GraviGun.cfg`): kein Treibstoff (`graviGunFuel=0`), `enableChargedGrabbing=1`, `grabBlockMode=1`
(Blöcke fallen beim Loslassen), `grabEntityMode=1` (die meisten Entities), `grabablePlayers=1`, `creeperExplosion=2`
(geworfene Creeper explodieren nur mit der aufgeladenen Kanone), Kistentyp greifbar, keine Block-Sperrliste.

- **Normale Kanone:** Rechtsklick halten zieht das anvisierte Entity (Reichweite 10) heran und hält es ab 4 Blöcken
  Abstand 3,5 Blöcke vor dem Spieler in der Luft; Linksklick schleudert es (Stärke 10 / Abstand) bzw. stößt ohne Griff
  an. Greifbar: Mobs, Tiere, Items, Loren, Boote, andere Spieler; nicht: Gemälde, Erfahrungskugeln, Blitze u. Ä.
  (Liste in 50.1 aus `EntityHelper.canGrab`).
- **Aufgeladene Kanone:** Reichweite 15, Wurf 12/Abstand, zieht doppelt so schnell, **greift auch Blöcke**: volle
  Blöcke sowie Glas, Glowstone, Kuchen und TNT werden zu einem fliegenden Block-Entity,
  das beim Loslassen fällt und sich als Block absetzt (Kisten mit Inhalt). Grundgestein nie.
- **Aufprallschaden:** geworfene Entities verursachen beim Treffen `⌊2,5 × Geschwindigkeit⌋` Schaden (skaliert mit
  dem Größenverhältnis), auch an sich selbst bei Wand-/Bodenkontakt. Geworfene Creeper explodieren (nur aufgeladen).
- **Aufladen:** Schlägt ein Blitz in höchstens 3 Blöcken Umkreis ein, werden alle normalen Kanonen im Inventar der
  Spieler dort aufgeladen. Zweites Rezept stellt die aufgeladene direkt her.
- Rezepte: `DEI / EGE / IEO` (D Diamant, E Enderperle, I Eisenbarren, O Obsidian, G Glowstone) → normal;
  G = Netherstern → aufgeladen.
- Zombies, die eine Gravity Gun halten (nur per Befehl/Ausrüstung), greifen/werfen zufällig (0,8 % je Tick).
- **Aussehen/Klänge:** Modell, Texturen und Sounds des Originals stammen aus Half-Life 2 – **nicht übernommen** (§11.1).
  Eigene schlichte Texturen (Python), Vanilla-Klänge (Greifen/Loslassen/Schuss/Fehlschlag).

## 4 Secret Rooms (Tab „Secret Rooms“)

Grundprinzip **Tarnung:** Tarnblöcke sehen aus wie ihre Nachbarn. Beim Setzen (und bei Nachbaränderung nicht – nur
beim Setzen) wird die Vorlage gewählt: gegenüberliegende Nachbarpaare gleicher Art haben Vorrang (unten/oben,
West/Ost, Nord/Süd; bei zwei gleichen Paaren dieses), sonst der häufigste Nachbar, sonst Stein. Zählen nur volle,
undurchsichtige Würfel (und Einwegglas). Umsetzung: Block-Entity speichert den Vorlage-`BlockState`, eigenes
`IDynamicBakedModel` zeichnet dessen Modell (inkl. Farbe/Biomtönung). Lichtdurchlass, Brennbarkeit und Seiten-
Festigkeit wie die Vorlage.

| Block | Verhalten | Rezept (Original) |
|---|---|---|
| Tarnpaste (Item) | Zutat | 9× aus `X#X / $0$ / @#@`: X roter, $ grüner, @ gelber, # schwarzer Farbstoff, 0 = Erde, Sand oder Ton (drei Rezepte) |
| Geisterblock | getarnt, **ohne Kollision** (durchlaufbar); mit Redstone-Signal am Block oder darüber fest | 4× `X0X / 0 0 / X0X`, X Paste, 0 verrottetes Fleisch oder Wolle |
| Getarnter Redstone-Block | getarnter, voller Block, leitet Redstone wie Draht | `X0X / 0@0 / X0X`, @ Redstone |
| Getarnter Hebel / Knopf | getarnte volle Blöcke, die wie Hebel/Knopf schalten | @ Hebel / Knopf |
| Getarnte Druckplatte (alle / Spieler) | getarnter Block, löst beim Betreten aus (alle Entities / nur Spieler) | `X@X / 0 0 / X0X`, @ Holzdruckplatte; Spieler-Variante Mitte Eisenbarren |
| Getarnte Treppe | getarnte Treppe | 4× mit @ Bruchsteintreppe; Original-Fehler: mit @ Holztreppe (`stairWood`) kommen 9 Tarnpaste heraus (übernommen, §11.3) |
| Getarnte Kiste | getarnte Truhe (27 Plätze) | @ Truhe |
| Getarntes Tor (+ Verlängerung) | Redstone-Signal → schiebt eine Wand aus Tarnblöcken aus / ein (Länge in 50.5 aus `BlockCamoGate`) | `#0# / 0A0 / #@#`, # Holzbretter, 0 Paste, @ Redstone, A Enderperle |
| Getarnte Holz-/Eisentür | Türen mit Tarnung beider Hälften | Paste + Holz-/Eisentür |
| Getarnte Falltür | Falltür mit Tarnung | Paste + Falltür |
| Einwegglas | von einer Seite (Front, beim Setzen zum Spieler) durchsichtig, von der anderen getarnt wie die Nachbarn | 9× aus Paste + 6 Glas (4 Lagen), 1× aus Paste + Glas |
| Fackel-Hebel | sieht aus wie eine Wandfackel, Rechtsklick schaltet wie ein Hebel | `# / X`, # Fackel, X Redstone |

- Befehl `/sr-show` (`CommandShow`): Tarnung für den Spieler kurz aufheben → in 50.1 prüfen, ggf. als Debug.
- Das Original nutzt eine „FakeWorld“ zum Zeichnen; in 1.21 übernimmt das das Modell.

## 5 Einbindung

- Tabs (§11.5): neuer Tab „Kleinkram“ für OmniTools, Gravity Gun und Obsidian-Druckplatte; eigener Tab „Secret Rooms“.
- EMI: Rezepte automatisch; Info-Seiten für OmniWrench, Lexicon, Gravity Gun, Tarnblöcke.
- Wiki, Herkunft (`SourceTexts`), Testplan, GameTests je Teilphase.

## 6 Teilphasen (Vorschlag)

50.1 Obsidian-Druckplatte + OmniWrench · 50.2 Sockel + Forge Lexicon · 50.3 Gravity Gun · 50.4 Secret Rooms: Tarn-
Grundlage (Modell, Geisterblock, Paste, Einwegglas) · 50.5 Secret Rooms: Redstone-/Tür-/Kisten-Varianten, Tor,
Fackel-Hebel · 50.6 EMI/Herkunft/Wiki/Paket.

## 11 Entscheidungen (Nutzer, 2026-11-13 – alle wie vorgeschlagen)

1. **Gravity Gun:** HL2-Modell, -Texturen und -Sounds nicht übernehmen; eigene Textur + Vanilla-Klänge.
2. **Lexicon-Gruppen:** Whitelist des Packs auf `c:`-Tags abgebildet.
3. **Secret-Rooms-Rezeptfehler** „Paste + Holztreppe → 9 Paste“ 1:1 übernommen.
4. **Umfang Secret Rooms:** alles aus §4.
5. **Tabs:** neuer Tab „Kleinkram“ (OmniTools, Gravity Gun, Obsidian-Druckplatte) + Tab „Secret Rooms“.
