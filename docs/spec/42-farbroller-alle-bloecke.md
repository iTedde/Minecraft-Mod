# 42 – Farbroller färbt alle Baublöcke

Nutzerwunsch 2026-11-06: „Kann man das so machen, das der Roller auch alle anderen Baublöcke einfärben kann? z.B.
Smooth Stone?“ Entschieden (Nutzer, 2026-11-06): **beides kombiniert** (echte Farbvariante, wo es eine gibt, sonst
tönen) und **Drop = Originalblock** (Farbe geht beim Abbauen verloren). Kein Pack-Vorbild, neuer Inhalt; erweitert
den IC2-Farbroller (`spec/05` §4.1). Bisheriges Verhalten (Kabel, Bauschaumwand, Wolle, Schafe) bleibt unverändert.

Status: **Spezifikation, Freigabe offen.**

## 1. Reihenfolge beim Rechtsklick auf einen Block

1. `PaintableBlock` (IC2-Kabel, Bauschaumwand) – wie bisher, inkl. Originalfehler „schwarz entfernt die Kabelfarbe“.
2. **Farbvarianten-Familie** (§2) – Block gegen die Variante in Roller-Farbe tauschen.
3. **Tönbarer Vollblock** (§3) – durch den bemalten Block ersetzen bzw. dessen Farbe ändern.
4. Sonst: nichts (PASS).

Jeder erfolgreiche Anstrich: 1 Anstrich verbraucht (32 je Roller, Nachfüllen wie bisher), Klang `Painter.ogg`.
Hat der Block die Zielfarbe schon, passiert nichts und es wird nichts verbraucht. Schwarz färbt hier **schwarz**
(der Farbwert-0-Fehler betrifft nur Kabel).

## 2. Echte Farbvarianten (Vanilla)

Der Block wird gegen dieselbe Familie in der Roller-Farbe getauscht; Blockzustand-Eigenschaften, die beide haben,
werden übernommen (Ausrichtung, Wasser, `lit`, Kerzenanzahl, Bett-Hälfte …).

| Familie | Ungefärbt gehört dazu |
|---|---|
| Wolle | – (wie bisher) |
| Teppich | – |
| Glas, Glasscheibe | Glas, Glasscheibe |
| Terrakotta | Terrakotta |
| Glasierte Terrakotta | – |
| Beton, Betonpulver | – |
| Kerzen (auch auf Kuchen) | Kerze, Kerzenkuchen |
| Shulkerkiste | Shulkerkiste (Inhalt bleibt, Block-Entity wird mitgenommen) |
| Bett | – (beide Hälften gemeinsam) |
| Banner | – nur Grundfarbe, Muster bleiben |

Getauschte Blöcke sind echte Vanilla-Blöcke (eigener Drop, Rezepte, Tags).

## 3. Getönte Blöcke

**Welche Blöcke:** jeder Block, der
- ein voller, undurchsichtiger Würfel ist (`isCollisionShapeFullBlock` und `canOcclude`),
- keine Block-Entity hat,
- nicht in §2 fällt, kein Craft-`PaintableBlock` ist und nicht im Tag `craft:unpaintable` steht
  (Standard: Grundgestein, Portalrahmen, Spawner, Befehlsblöcke, Barriere, Strukturblöcke).

Damit z. B. Glattstein, Stein, Bruchstein, Ziegel, Steinziegel, Bretter, Stämme, Quarz, Sandstein, Erze und volle
Blöcke anderer Mods. Nicht: Stufen, Treppen, Zäune, Glas (§2), Maschinen, Truhen.

**Block `craft:painted_block`** (ein Block für alles) mit Block-Entity: gespeichert der Originalzustand
(`BlockState`) und die Farbe (0–15).
- **Aussehen:** Modell des Originalblocks, alle Flächen mit der Farbe getönt (Textur × Farbstoff-Farbe, wie Leder-
  rüstung). Dunkle Blöcke werden dadurch entsprechend dunkler. Partikel beim Abbauen wie das Original.
- **Verhalten wie das Original:** Härte/Abbaugeschwindigkeit, nötiges Werkzeug und Werkzeugstufe,
  Explosionswiderstand, Klang, Licht, Rotstein-Leitfähigkeit, Pick-Block (mittlere Maustaste) = Originalblock.
- **Abbauen:** droppt, was der Originalblock droppen würde (inkl. Behutsamkeit/Glück); die Farbe ist weg.
- **Erneut anmalen:** ändert nur die Farbe.
- **Kolben:** schieben den bemalten Block wie einen normalen (die Block-Entity zieht mit, falls 1.21 das
  zulässt; sonst unbeweglich – wird bei der Umsetzung geprüft und in `abweichungen.md` festgehalten).
- **Bekannte Grenzen:** In der Welt ist der Block für andere Mods und Rezepte nicht mehr das Original (z. B. Create-
  Presse, Bohrer-Rezepte auf dem Block, Blocktags wie `minecraft:base_stone_overworld`, Endermen nehmen ihn nicht
  auf). Weltgen-Blöcke ändern sich nur dort, wo man sie bemalt.

## 4. Sonstiges

- **Tooltip** des Farbrollers: „Färbt Kabel, Wolle, Schafe, Farbblöcke und volle Baublöcke“.
- **EMI:** Infoseite am Farbroller mit dem Verhalten; kein eigenes Rezept.
- **Wiki/Testplan:** Abschnitt Farbroller erweitern, Testfälle Glattstein, Beton, Glasscheibe (Ausrichtung), Bett,
  Shulkerkiste (Inhalt), Abbau-Drop, erneutes Anmalen, Kolben.
- **Server/Client:** Craft ist auf beiden Seiten installiert; keine neue Konfiguration.

## 5. Umsetzung

| Teil | Inhalt |
|---|---|
| 42.0 | diese Spezifikation |
| 42.1 | Farbvarianten-Familien (§2) im `PainterItem` |
| 42.2 | `painted_block` + Block-Entity + getöntes Modell, Drops, Verhalten (§3), GameTests |
| 42.3 | EMI-Info, Tooltip, Wiki, Testplan, `abweichungen.md` |
