# Phase 39 – Zusatzmods und Spalte „abgebaute Blöcke“ (2026-11-02)

Nutzer (2026-11-02): „Ich gebe dir mal ein paar Mods, die noch mit rein müssen“ – Litematica, Armor Stand Arms,
Subtle Effects, Particle Rain, Pick Up Notifier, ein Statistik-Mod (entschieden: Enhanced Player List) und „das mit
den Blöcken noch reinmodden“. Litematica: entschieden **Forgematica** (NeoForge-Port, Nutzer 2026-11-02).

Kein Pack-Vorbild; die Mods werden nur mitinstalliert, Craft ändert nur Enhanced Player List (§3).

## 1 Mods im CurseForge-Paket

Alle für NeoForge 1.21.1 geprüft (Stand 2026-11-02). Auf CurseForge vorhandene Mods kommen per `manifest.json`
`files`, die übrigen als Jar nach `overrides/mods` (Lizenz MIT, kleine Dateien).

| Mod | Version | Wo | Projekt/Datei | Abhängigkeit | Seite |
|---|---|---|---|---|---|
| Forgematica (Litematica-Port) | 0.4.3+mc1.21.1 | Manifest | 912441 / 9154476 | MaFgLib | Client |
| MaFgLib (MaLiLib-Port) | 0.4.3+mc1.21.1 | Manifest | 910766 / 7493644 | – | Client |
| Armor Stand Arms (Jodek) | v2.7.1+1.21-1.21.8 | `overrides/mods` (nur Modrinth, `UanLrler`, SHA-1 `77a2a963…`) | – | – | Server (Datenpaket als Mod) |
| Subtle Effects | 1.15.1 | Manifest | 1023913 / 9150589 | Fzzy Config | Client |
| Fzzy Config | 0.7.7+fix1 | Manifest | 1005914 / 9326327 | Kotlin for Forge (schon im Paket) | beide |
| Particle Rain | 4.0.2 | Manifest | 421897 / 9113934 | – | Client |
| Pick Up Notifier | v21.1.1 | Manifest | 351441 / 6409785 | Puzzles Lib (schon im Paket) | Client (Server optional) |
| Enhanced Player List | 1.0.2 | `overrides/mods` (nur Modrinth, `5J8PXXet`, SHA-1 `d236e688…`) | – | – | beide |

Hinweise:
- Auf CurseForge heißt ein anderes Projekt „Armor Stand Arms (RPG Theme)“ (1048775) – **nicht** dieses.
- Enhanced Player List muss auch auf dem Server liegen (Statistikdateien liest nur der Server).
- Forgematica ersetzt Litematica 1:1 (Schaltpläne, Platzierungshilfe, Taste M für das Menü – wie Litematica);
  kollidiert die Taste mit der IC2-Modus-Taste M, wird das im Client-Test geklärt.

## 2 Entwicklungsumgebung

Nur Enhanced Player List wird in Craft gebraucht: `compileOnly` + `localRuntime`
(`maven.modrinth:enhanced-player-list:5J8PXXet`, `gradle.properties` `enhancedplayerlist_version`). Die übrigen Mods
laufen nur im Paket.

## 3 Spalte „abgebaute Blöcke“ in Enhanced Player List

Befund aus der EPL-Quelle (https://github.com/Decryptu/enhanced-player-list, 1.0.2): Der Server summiert schon alle
`minecraft:mined`-Werte aus `world/stats/<UUID>.json` (online und offline) und schickt die Summe an die Clients
(`PlayerStatsData.blocksMined`), zeigt sie aber nicht an (keine Spalte in `Config.StatType`/`PlayerListRenderer`).
Die geplante eigene Statistik `craft:blocks_mined` ist daher **unnötig**: Die Vanilla-Werte enthalten auch die
bisherigen Welten von Anfang an.

Umsetzung (Mixin-Konfiguration `craft.epl.mixins.json`, nur aktiv, wenn EPL geladen ist – `EplMixinPlugin`):
- `EplStatTypeMixin`: `Config.StatType.isValid` akzeptiert `blocksMined`.
- `EplConfigMixin`: Standardliste `visibleStats` mit `blocksMined` hinter `distance` (neue Config-Dateien).
- `EplRendererMixin`: Wert der Spalte = Summe der abgebauten Blöcke (`12,345`; Kompaktmodus `12.3k`/`2.5M`),
  offline grau wie die anderen Spalten.
- `EplHeaderMixin`: Spaltenkopf „Mined“ (Kompaktmodus „MIN“), englisch wie die übrigen EPL-Köpfe.
- Hilfsklasse `compat/epl/EplMinedBlocks` (JUnit `EplMinedBlocksTest`).

Gezählt wird, was Vanilla als „abgebaut“ zählt: vom Spieler abgebaute Blöcke (mit Werkzeug oder Hand). Maschinen
(Quarry, Miner, Turtles) zählen nicht. Die Tab-Liste aktualisiert sich, wenn der Server die Statistikdatei speichert
(Autosave, Abmelden).

Eine bestehende Config-Datei (`config/enhancedplayerlist-common.toml`) behält ihre Liste; dort `"blocksMined"` von Hand
eintragen. Das Paket bringt keine eigene EPL-Config mit (Standard reicht).

## 4 Teilphasen

- 39.1 Spezifikation, EPL-Spalte (Mixins), Tests – erledigt
- 39.2 Paket (Manifest + `overrides/mods`), Wiki „Create & Paket-Mods“, Testplan – erledigt: Craft 1.1.4, `releases/Craft-1.1.4-curseforge.zip` (77 Manifest-Einträge, 9,2 MB)
