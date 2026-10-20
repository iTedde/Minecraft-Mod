# 24 – Herkunft von Rohstoffen erklären (Phase 23)

Nutzerwunsch (2026-10-20): Im Spiel lässt sich kaum herausfinden, wie man an viele Rohstoffe kommt (Beispiel
Nickel: nur über Nebenprodukte von Zentrifuge/Industrial Grinder, UU-Materie oder Schrottbox), und das Wiki ist zu
vage („Mögliche Quellen: Weltgenerierung, Drops, Maschinen …“). Für **alle Rohstoffe** soll klar dastehen, woher sie
kommen – im Spiel und im Wiki. Keine Änderung an Mechanik oder Rezepten.

## 1. Bestandsaufnahme (Stand 0.4.4)

- **EMI** zeigt alle Rezepte aus dem RecipeManager und die Code-Tabellen aus `spec/16` §2.2 (Schrottbox, Recycler,
  Massefabrikator …), Info-Seiten nur für Multiblöcke und Fass/Kaffee. Es fehlt:
  - **Weltgenerierung**: Für kein Erz steht, in welcher Dimension, Höhe und Häufigkeit es vorkommt, und welches
    Werkzeug es braucht (Sheldonit nur im End, Iridium-Erz selten, Bauxit/Rubin nur in bestimmten Biomen …).
  - **Mehrstufige Wege**: Nickel- oder Zinkstaub entstehen fast nur als **kleiner Staub** (Nebenprodukt); unter dem
    Staub selbst steht nur „4 kleine Staub → 1 Staub“, der eigentliche Weg (Zentrifuge/Grinder) erst eine Ebene tiefer.
  - **Drops und Weltinteraktion**: Harz (Baumzapfer am Gummibaum), Uran (Erz), Schrott (Recycler), Öl (Quellen/Pfützen), Kaffeebohnen/Hopfen/Terra Wart (Crops) usw.
  - **Nicht erreichbare** Materialien (Titan, Chrom, Plutonium, Thorium, Mangan …) sehen aus wie normale Items.
- **Wiki**: Gegenstandsseiten zeigen nur Datenpaket-Rezepte. 47 Items haben gar kein Rezept (Erze, Gummibaum,
  Schrott, UU-Materie, Öl …) und bekommen den Sammeltext oben. Code-Tabellen (Schrottbox, Crops, Zentrifuge mit
  Zellen) und Weltgenerierung fehlen dort ganz.

## 2. Lösung

### 2.1 Eine Datenquelle: Herkunftstexte

Pro Rohstoff ein kurzer Text **„Herkunft“** (Deutsch und Englisch), datengetrieben in einer eigenen Klasse
`dev.craft.data.SourceTexts` (Eintrag = Item + DE + EN), vom `CraftLanguageProvider` als Lang-Schlüssel
`source.craft.<id>` ausgegeben. Dieselben Schlüssel nutzen EMI, Tooltip und Wiki – Text nur an einer Stelle pflegen.

Inhalt eines Textes (in dieser Reihenfolge, nur was zutrifft, Werte aus der Spezifikation):
1. **Vorkommen**: Dimension, Höhe, Häufigkeit (Adern je Chunk, Größe), Biom, Werkzeugstufe; Mining-Dimension ja/nein.
2. **Bester Weg** (z. B. „Industrial Grinder: Eisenerz → … + kl. Nickelstaub, dann 4 kl. → 1 Staub“).
3. **Weitere Wege**: Nebenprodukte, Zentrifuge, UU-Materie, Schrottbox, Recycler, Crops, Mobs.
4. **Nicht erreichbar** deutlich kennzeichnen („in Craft nicht herstellbar – nur Zutat/Rezeptreste aus dem Pack“).

Kurz halten (Ziel ≤ 4 Zeilen à ~40 Zeichen im Tooltip); ausführliche Tabellen gehören ins Wiki.

### 2.2 Anzeige im Spiel

- **EMI-Info-Seite** je Rohstoff (`EmiInfoRecipe`, Kategorie „Herkunft“): erscheint bei **R** (Rezepte) auf dem Item
  vor den Rezepten. Für Erze zusätzlich die Weltgenerierungs-Werte.
- **Tooltip** (Entscheidung E1): Zeile „[Shift] Herkunft“; mit gedrückter Umschalttaste der Herkunftstext,
  umgebrochen. Nur für Items mit Herkunftstext.

### 2.3 Wiki

- Gegenstandsseite: neuer Abschnitt **„Herkunft“** vor „Herstellung“ aus `source.craft.<id>`; der Sammeltext
  „Mögliche Quellen …“ entfällt für Items mit Herkunftstext.
- Neue Seite **„Rohstoffe & Erze“**: Tabelle aller Erze (Dimension, Höhe, Adern × Größe, Biom, Werkzeug, Drop,
  Mining-Dimension) und Tabelle der Materialien mit bestem Weg; Verweise auf die Gegenstandsseiten.
- Durchsicht der Anleitungsseiten (`content*.py`) auf vage Herkunftsangaben; durch konkrete Wege ersetzen.

## 3. Umfang (≈ 240 Einträge)

| Gruppe | Items |
|---|---|
| Erze (21) | Zinn, Uran, Iridium (Erz + Block), Galena, Rubin, Saphir, Bauxit, Pyrit, Zinnober, Sphalerit, Wolframit, Sheldonit, Olivin, Sodalith; Tiefenschiefer-Varianten; Vanilla-Kupfer (geänderte Häufigkeit) |
| Weltgen/Drops | Gummibaum (Holz, Blätter, Setzling), Harz, Öl (Block/Eimer), Uran-Drop, Iridium-Erz (Item), Schrott, UU-Materie, Kaffeebohnen, Hopfen, Terra Wart, Bergbaurohr-Spitze |
| Stäube (54) und kleine Stäube (31) | alle `*_dust`, `small_*_dust` |
| Barren (23), Platten (25), Edelsteine (7), Nuggets | alle; bei Platten genügt meist „Plattenbieger/Schmiedehammer aus Barren“ |
| Zellen (44) | Flüssigkeits- und Gaszellen (Elektrolyseur/Zentrifuge-Wege), Brennstäbe nur kurz |
| Sonstiges | Kohle-Kette (Kohleball → komprimiert → Kohlebrocken → Industriediamant), Kohlenstoff (Faser/Netz/Platte), Koks, Asche, Pflanzenball, Gummi, fortgeschrittene Legierung, Mischmetall |

Fertige Maschinen, Werkzeuge und Rüstung bekommen **keinen** Herkunftstext (ihr Rezept genügt).

## 4. Teilphasen

| Teilphase | Inhalt |
|---|---|
| 23.0 | diese Spezifikation |
| 23.1 | Mechanismus (`SourceTexts`, Lang, EMI-Info, Tooltip, Wiki-Abschnitt) + Erze und Weltgen-/Drop-Rohstoffe |
| 23.2 | Stäube und kleine Stäube |
| 23.3 | Barren, Platten, Edelsteine, Nuggets |
| 23.4 | Zellen und Sonstiges |
| 23.5 | Wiki-Seite „Rohstoffe & Erze“, Durchsicht der Anleitungsseiten, Client-Test, Version + CurseForge-Paket |

Jede Teilphase: Texte aus `spec/07`, `spec/12`, `spec/13`, `STATUS.md` (Rezeptlisten) belegen; GameTest, der prüft,
dass jedes Item der Gruppe einen Herkunftstext in DE und EN hat; `gradlew build`; Wiki neu erzeugen.

## 5. Entscheidungen

- **E1** Tooltip mit Shift zusätzlich zur EMI-Info-Seite? *Vorschlag: ja.*
- **E2** Nicht erreichbare Materialien als solche kennzeichnen? *Vorschlag: ja.*
