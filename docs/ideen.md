# Ideen für Craft

Stand 2026-10-31. Nur Vorschläge, nichts davon ist freigegeben oder eingeplant. Sortiert nach Nutzen.

Empfehlung: zuerst die offenen Phasen 37.4 und 38.2 abschließen (fehlen nur noch EMI, Wiki, Doku), dann
CC-Peripherals und danach das Pumpspeicherkraftwerk.

## Ganz oben

1. **CC-Peripherals für Craft-Maschinen:** CC: Tweaked ist im Paket, im Craft-Code gibt es noch keine
   Peripheral-Anbindung. Computer könnten Reaktorhitze, MFSU-Ladung oder Turbinen auslesen und steuern, zum Beispiel
   den Reaktor vor der Überhitzung abschalten. Kleiner Aufwand, weil eine generische Anbindung für EU-Blöcke schon
   viele Maschinen abdeckt. `miscperipherals` lag dem Pack dafür bei.
2. **IC2 Nuclear Control (aus dem Pack):** Thermo-Monitor, Fernsensor, Infotafeln, Alarm-Sirene. Ergibt eine
   Schaltwarte für Reaktor, Speicher und Kraftwerke und ergänzt die Peripherals für alle, die nicht programmieren
   wollen.
3. **Pumpspeicherkraftwerk (neu):** Tagsüber pumpt Solarüberschuss Wasser in ein hoch gelegenes Becken, nachts läuft
   es durch eine Wasserturbine zurück. Leistung abhängig von Höhe und Beckengröße. Schließt die Nachtlücke des
   Solarfelds, nutzt die Wasserturbinen-Technik und wäre der erste Speicher, den man als Bauwerk sieht.

## Kleinere Ergänzungen

4. **ChargePads (aus dem Pack):** Rüstung und Werkzeuge laden beim Draufstellen. Täglicher Komfort bei
   Quanten-Rüstung, GraviSuite und Elektrowerkzeugen. Wenig Arbeit, ein paar Teilphasen.
5. **EU-Verteiler (neu):** Gegenstück zum Verteiler-Transportrohr, mit Prozentwerten je Seite, zum Beispiel nur 20 %
   für den Massefabrikator. Eigener Block, der EU-Algorithmus bleibt 1:1.
6. **Gravity Gun (aus dem Pack):** Vor allem Spaß, passt zu den Long Fall Boots, schnell gebaut.

## Größere Projekte

7. **Modular Force Field System (aus dem Pack):** EU-betriebene Kraftfelder um Basis oder Reaktor. Großer Stromabnehmer
   fürs Endgame und auf dem Server praktisch als Schutz, interessant zusammen mit Nuke und Create Big Cannons.
8. **Brennstoffzelle und Wasserstoff (neu):** Elektrolyseur erzeugt Wasserstoff, Tank speichert ihn, die Zelle macht
   wieder Strom daraus. Zweiter Speicherweg mit Verlusten. Nur eines von Pumpspeicher und Wasserstoff bauen, zuerst
   den Pumpspeicher.

## Atmosphäre

Vom Nutzer 2026-10-31 alle für gut befunden, aber noch nicht als Phase freigegeben. Reihenfolge: zuerst rein
optische und akustische Punkte (Die Fabrik lebt), weil sie keine Spielwerte ändern und die 1:1-Regel nicht berühren.
Danach Notbeleuchtung und Werkssirene.

### Die Fabrik lebt

- **Rauch und Dampf:** Kohlekraftwerk und Koksofen qualmen aus Schornsteinen, Turbinen lassen Dampf ab, Kühltürme
  dampfen. Von weitem sichtbar, ob das Werk läuft.
- **Maschinengeräusche nach Last:** darf nicht penetrant sein, weil viele Geräte dauerhaft unter Last laufen (Nutzer,
  2026-10-31). Daher kein Dauerton je Maschine: auffällig nur Anlaufen, Auslaufen und Störungen; im Dauerbetrieb nur
  ein leises Grundbrummen mit kurzer Reichweite. Viele Maschinen zählen zusammen als eine Klangquelle mit Lautstärke-
  Deckel. Eigener Lautstärkeregler in der Client-Config, bis auf aus.
- **Funken und Glühen:** Überlastete Kabel knistern und sprühen Funken, heiße Maschinen glühen.
- **Reaktor-Stimmung:** brummt tiefer, je heißer er wird, das Licht flackert, zuletzt heult ein Alarm.

### Arbeitsalltag

- **Werkssirene und Schichtglocke:** Signal bei Sonnenaufgang und -untergang, per Redstone auch von Hand auslösbar.
- **Stempeluhr:** Deko, zählt die Stunden je Spieler.
- **Spinde und Pausenraum:** Spinde als Lager mit Namensschild, Kaffeeautomat, Sitzbank, Kantinentisch.
- **Schilder und Beschriftung:** Werksschilder, Bodenmarkierungen, Warnhinweise, Tafeln mit frei wählbarem Text.
- **Werkskneipe:** Zapfanlage für Bier und Rum, Betrunkenheit mit schwankender Sicht, eventuell Kantinenessen.

### Licht und Oberflächen

- **Industrielicht:** Hallenstrahler, Käfiglampen, Notbeleuchtung, die bei Stromausfall rot wird.
- **Abnutzung:** Maschinen setzen optisch Rost und Ruß an, Ruß sammelt sich auf dem Boden um Kohlekraftwerke.
- **Industrie-Deko:** Warnschilder, Gitterböden, Rohre als Deko, Warnleuchten, die auf Redstone reagieren.

### Handbuch

- **Werkshandbuch im Spiel:** das Wiki als Buch, das mitwächst und Einträge freischaltet, sobald man etwas gebaut hat.

## Abgelehnt (Nutzer, 2026-10-31)

Verlassene Industrieanlagen, gefährlichere Mining-Dimension, Fallout nach der Nuke, defekte Roboter, weitere Golems,
Stromzähler und Rechnungen, Werksfunk, Ingenieur-Dorfbewohner.

## Besser weglassen

- **Advanced Solar Panels:** untergräbt den Solar-Umbau aus Phase 29.
- **Railcraft, Steve's Carts, RedPower-Rahmen:** Create und Steam 'n' Rails decken das größtenteils ab.
- **MineFactory Reloaded:** überschneidet sich stark mit Industrial Foregoing.
- **ChickenChunks:** übernimmt FTB Chunks bereits.
- **Alles, was schneller macht:** widerspricht Phase 37.
