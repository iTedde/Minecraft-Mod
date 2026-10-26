# 31 – Verteiler-Transportrohr (Phase 31)

Neuer Inhalt ohne Pack-Vorbild (Nutzer, 2026-10-26): ein Item-Rohr, in dem man **je Abzweig einstellt, wie viel
Prozent** der durchlaufenden Items dorthin gehen. Entschieden (Nutzer, 2026-10-26): nur Items, GUI mit Prozentwert je
Seite, **exakte** (nicht zufällige) Aufteilung. Baut auf den BuildCraft-Rohren aus `spec/15-rohre.md` auf (Paket
`…buildcraft/transport`). Punkte zur Freigabe: §7.

## 1 Block

| Eigenschaft | Wert |
|---|---|
| ID | `craft:distribution_transport_pipe` |
| Name | DE „Verteiler-Transportrohr“, EN „Distribution Transport Pipe“ |
| Transport | Items (wie Diamant: Eingang von allen Seiten, `outputOpen` auf allen Seiten) |
| Verbindungen | wie Diamant (§3.2 Material: keine Einschränkung) |
| Geschwindigkeit | Standard (`defaultReajustSpeed`, spec 15 §4.3) |
| Energie | keine |
| Stapel | 64, Creative-Tab BuildCraft |
| Rezept | formlos **Diamant-Transportrohr + Komparator → 1** (Rezeptform wie die Zwei-Zutaten-Rohre, spec 15 §2) |
| Textur | Grundtextur des Diamantrohrs, je Seite in der Farbe der Diamant-Filterreihe (unten, oben, N, S, W, O – spec 15 §2, Kacheln 22–27), Mitte mit Komparator-Akzent; per `tools/scripts/` (Python + Pillow) |

Rechtsklick öffnet die GUI (Rohr-Item in der Hand → nichts, wie beim Smaragdrohr). Schraubenschlüssel ohne Wirkung.
Die Einstellungen bleiben beim Abbauen **nicht** erhalten (wie Diamant-/Smaragdfilter).

## 2 Einstellung

- Je Seite (unten, oben, N, S, W, O) ein Wert **0–100 %** in Schritten von 1.
- Die Werte sind **Gewichte**: Die Summe muss nicht 100 sein. Verteilt wird nur auf Seiten, die im Moment ein Ziel sind
  (§3 Schritt 1); deren Werte werden auf 100 % hochgerechnet. Beispiel: N 50, S 30, O 20 → 50/30/20; fehlt O
  (Truhe voll oder abgebaut), gehen 62,5 % nach N und 37,5 % nach S.
- **0 % = Seite gesperrt**, auch wenn sie verbunden ist.
- **Neu gesetztes Rohr: alle Seiten 0** → solange keine Seite einen Wert > 0 hat, verhält sich das Rohr wie ein
  normales Rohr (gleichverteilt zufällig, spec 15 §4.2). So funktioniert es sofort nach dem Setzen.

## 3 Zielwahl (ersetzt spec 15 §4.2 Schritt 4–5)

Für jedes Item, das die Rohrmitte erreicht:

1. Kandidaten wie beim normalen Rohr (spec 15 §4.2 Schritt 1–2): nicht die Herkunftsseite, verbunden, nimmt Items an.
   Davon nur Seiten mit Wert > 0 (sofern irgendeine Seite > 0 ist, §2).
2. Keine Kandidaten → Rückläufer zur Herkunftsseite, sofern sie Items annimmt (wie das Eisenrohr, `allowBouncing`),
   sonst Auswerfen wie beim normalen Rohr (spec 15 §4.7).
3. **Exakte Aufteilung** per Zähler je Seite (gesendete Items, nicht Stapel): Das Rohr führt `sent[s]`. Für jedes
   einzelne Item des Stapels wird unter den Kandidaten die Seite gewählt, deren `sent[s] / Wert[s]` am kleinsten ist
   (Gleichstand: Reihenfolge unten, oben, N, S, W, O), und `sent[s]` erhöht.
4. **Stapel werden geteilt**: Ein Stapel von n Items wird nach Schritt 3 Item für Item zugeordnet und in bis zu 6
   `TravelingItem`s aufgeteilt, die gleichzeitig in ihre Richtungen weiterlaufen. Beispiel 64 Items bei 50/30/20 →
   32/19/13.
5. **Kein Nachholen:** Ist eine Seite gerade kein Kandidat, zählt sie nicht mit. Wird sie wieder Kandidat, wird ihr
   Zähler auf den Stand der anderen gehoben (`sent[s] = min(sent[k] / Wert[k]) · Wert[s]` über die übrigen
   Kandidaten k, falls kleiner) – sie bekommt ab dann ihren Anteil, aber nicht nachträglich das Verpasste. Die Zähler
   werden gemeinsam um den kleinsten normierten Stand verringert, damit sie nicht überlaufen.
6. Rückläufer (Ziel-Inventar nimmt den Rest nicht an, spec 15 §4.4) kommen zur Mitte zurück und werden neu nach
   Schritt 1–5 verteilt; die abweisende Seite wird für dieses Item gesperrt (Sperrliste wie im Original).
- Zähler und Werte werden mit dem Rohr gespeichert; Werte an Clients synchronisiert (für die GUI), Zähler nicht.

## 4 GUI

- Eigener Screen ohne Spielerinventar (176 × 166, Hintergrund im Stil der BC-Filter-GUIs), Titel „Verteiler-Transportrohr“.
- 6 Zeilen, je Zeile: Farbmarke der Seite + Name (Unten, Oben, Norden, Süden, Westen, Osten), Knöpfe **−10 −1**,
  Wert, **+1 +10**, dahinter der **tatsächliche Anteil** in Klammern unter den gerade verbundenen Seiten
  (z. B. „30 % (37,5 %)“); nicht verbundene Seiten grau mit „–“.
- Umschalt-Klick auf einen Knopf: Wert auf 0 bzw. 100.
- Änderung per Menü-Knopf (`clickMenuButton`, Seite·4 + Knopf), Server prüft und speichert; kein eigenes Paket nötig.
- Unter den Zeilen: „Summe: x“ und Hinweis „Alle 0 = gleichmäßig“.

## 5 Texte, EMI, Wiki

- Lang DE/EN: Blockname, GUI-Texte, Tooltip „Teilt Items nach eingestellten Anteilen auf (Rechtsklick)“.
- EMI-Info-Seite (DE/EN) mit Kurzerklärung (Gewichte, 0 = gesperrt, Stapel werden geteilt).
- Wiki: Abschnitt bei den BuildCraft-Rohren, Steckbrief-Zeile; Herkunftstext (spec 24) nicht nötig (Block, kein Rohstoff).

## 6 Tests (GameTests)

- 50/30/20 auf drei Truhen, 100 Items einzeln → 50/30/20 exakt.
- Ein Stapel 64 bei 50/30/20 → 32/19/13.
- 0 % sperrt eine verbundene Seite.
- Alle 0 → Items kommen an (normales Verhalten).
- Volle Truhe → Anteil auf die übrigen Seiten hochgerechnet; nach dem Leeren kein Schwall (Kein Nachholen).
- Rezept vorhanden.

## 7 Punkte zur Freigabe

1. **Rezept:** Diamant-Transportrohr + Komparator → 1.
2. **Startwert:** alle Seiten 0 = verhält sich wie ein normales Rohr (Alternative: alle 100 = gleichmäßig; Ergebnis gleich,
   aber 0 % müsste man dann aktiv setzen).
3. **Stapel teilen** (statt ganze Stapel zu verteilen – dann wäre die Aufteilung erst über viele Stapel genau).
4. **Kein Nachholen** nach vollen Zielen (statt späterem Ausgleich).
5. **Ohne Ziel:** zurück zur Herkunft, sonst Auswerfen (wie Eisenrohr).

## 8 Teilpakete

| Teil | Inhalt |
|---|---|
| 31.0 | diese Spezifikation |
| 31.1 | Block, Logik, Speicherung, GUI, Rezept, Textur, GameTests |
| 31.2 | EMI-Info, Wiki, Testplan, Craft-Version + CurseForge-Paket |
