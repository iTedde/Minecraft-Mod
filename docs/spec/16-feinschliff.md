# 16 – Feinschliff (Phase 11)

Umfang laut Phasenplan: Texturen, EMI-Integration (Rezeptanzeige), Xaero's Minimap mitinstallieren, Tests.
Dazu kommen die Punkte, die in `abweichungen.md`/`STATUS.md` ausdrücklich auf „Feinschliff/Phase 11“ verschoben
wurden. Spielmechanik ändert sich in Phase 11 nicht (Grundregel 1); EMI zeigt nur an, was der Mod ohnehin tut.

## 1. Bestandsaufnahme (Stand 2026-10-16)

- **Texturen:** 1487 Modelle, keine fehlende Texturreferenz (Prüfskript über `src/main` + `src/generated`).
  1196 PNGs, fast alle 16×16 aus den Original-Jars geschnitten; eigene Texturen nur für neue Inhalte
  (Kraftwerke, Tiefenschiefer-Erze, `craft:water`/`craft:lava` …).
- **Auf Phase 11 verschoben:**
  - GT-Maschinengehäuse: verbundene Texturen (`abweichungen.md`, Status **vorläufig**; `spec/13` §Gehäuse).
  - Forestry-Ledger (Energie, Hinweise, Fehlerstatus) und Zugriffsrechte (`abweichungen.md`, **offen**;
    `spec/10` §Fabricator/Carpenter/Engine).
  - Kabel-Texturen „endgültig“ (`STATUS.md`, Phase 2) – siehe Entscheidung E1.
- **Tests:** 255 GameTests grün; manuelle Client-Tests aus den Teilphasen stehen verstreut in `STATUS.md`
  („Nächste Schritte“).

## 2. EMI-Integration

Abhängigkeit: EMI für NeoForge 1.21.1 (`dev.emi:emi-neoforge:<version>+1.21.1`, Maven
`https://maven.terraformersmc.com/`), API `compileOnly`, voller Mod `localRuntime`. Optional: der Mod startet auch
ohne EMI (`neoforge.mods.toml` `type="optional"`). Code in eigenem Paket `dev.craft.compat.emi` (Plugin mit
`@EmiEntrypoint`), keine EMI-Klassen außerhalb davon.

Ohne eigene Arbeit zeigt EMI bereits: Werkbank (auch `Ic2ShapedRecipe`/`Ic2ShapelessRecipe`, die von
`ShapedRecipe`/`ShapelessRecipe` erben – Ladungsübertragung nur als Hinweis), Vanilla-Ofen (Eisenofen,
Elektroofen nutzen Vanilla-Schmelzrezepte).

### 2.1 Kategorien mit Rezepttyp (aus dem RecipeManager)

| Kategorie | Quelle | Workstations | Anzeige |
|---|---|---|---|
| Macerator / Extractor / Compressor | `MachineRecipe.Kind` | IC2-Maschine (+ GT-Pendants, falls dieselben Rezepte genutzt werden) | Ein-/Ausgabe, Dauer, EU/t |
| Plattenbieger, Elektrolyseur, Hochofen, Assembler, Canner, Implosionskompressor, Zentrifuge, Industrial Grinder, Vakuumgefrierer | `GtRecipe.Kind` | jeweilige GT-Maschine | Slots wie GUI, Dauer, EU/t, Gesamt-EU; Hochofen: Hitze; Grinder/Elektrolyseur: Flüssigkeiten/Zellen |
| Thermionic Fabricator | `FabricatorRecipe` | Fabricator | Schmelze (Glas-mB), Raster, Gussform |
| Carpenter | `CarpenterRecipe` | Carpenter | Raster, Flüssigkeit, Kiste |

### 2.2 Kategorien aus Code-Tabellen

| Kategorie | Quelle | Anzeige |
|---|---|---|
| Recycler | `RecyclerBlockEntity` (1/8, Blacklist-Tag) | „beliebiges Item → Schrott, 12,5 %“ + Blacklist als Info |
| Schrottbox | Loot-Tabelle (`CraftScrapBoxLoot`) | Drops mit Wahrscheinlichkeit |
| Massefabrikator | `MassFabricatorBlockEntity` | Verstärker (Schrott) und Energie je UU-Materie |
| IC2-Canning Machine | Canning-Logik (`spec/04`) | Brennstoff-/Lebensmitteldosen |
| Raffinerie | `RefineryRecipe` | Flüssigkeit → Flüssigkeit, MJ |
| Verbrennungsmotor | `CombustionFuels` | Treibstoffe (MJ/t, Dauer), Kühlmittel |
| Koksofen | `CokeOvenBlockEntity` | Kohle → Koks (+ Kreosot, falls vorhanden) |
| Brennstoffe Generator / Kessel | Burn-Time + Faktoren aus `spec/02`/`spec/14` | EU bzw. Dampf je Item |
| Geothermiegenerator | Lava | EU je Eimer/Zelle |

Liste beim Umsetzen gegen alle Rezepttyp-Registrierungen und Maschinen prüfen; Fehlendes nachtragen.

### 2.3 Sonstiges

- GUI-Klickbereiche (Fortschrittspfeil → Kategorie) für alle Maschinen-Screens.
- Nicht erhältliche Hilfs-Items (`craft:water`, `craft:lava`, Holo-Slots) in EMI ausblenden.
- Info-Seiten (EMI `EmiInfoRecipe`) für Multiblöcke (Solar Arrays, Hochofen, Kraftwerke, Wasserturbine,
  Koksofen): Bauplan in Textform.

## 3. Xaero's Minimap

Nur mitinstalliert, keine Integration: `localRuntime` über den Modrinth-Maven
(`https://api.modrinth.com/maven`, `maven.modrinth:xaeros-minimap:<NeoForge-1.21.1-Version>`), damit `runClient`
sie lädt. Für die Spielinstallation: Hinweis in `docs/` (Version, Download).

## 4. Texturen

1. **Verbundene GT-Gehäusetexturen:** `blocks.png` ab Index 96/112/224, 12 Varianten je Seite, Auswahl je nach
   gleichartigen Nachbarn (Logik aus `reference/decompiled/gregtech` des Gehäuseblocks in `spec/13` nachtragen).
   Umsetzung idiomatisch über ein eigenes Baked-Model mit `ModelData` oder Blockstate-Properties je Seite.
2. **Kabel und übrige Original-Texturen:** siehe E1.

## 5. Forestry-Ledger und Zugriffsrechte

- Ledger (seitliche ausklappbare Reiter) an Electrical Engine, Thermionic Fabricator, Carpenter: Energie
  (MJ/t, gespeichert, Hitze), Hinweise, Fehlerstatus – Inhalte aus den Forestry-Klassen in `spec/10` nachtragen.
- Zugriffsrechte (Besitzer/Zugriffsmodus): siehe E2.

## 6. Tests

- `docs/testplan.md`: alle manuellen Client-Tests aus `STATUS.md` gesammelt, nach Mod/Thema geordnet, mit
  Abhakspalte; veraltete Einträge entfernt.
- GameTests: Lücken der EMI-Anbindung prüfen (jede registrierte Rezeptart hat eine Kategorie – als Unit-artiger
  GameTest ohne Client-Klassen nur, wenn sinnvoll möglich).
- Abschluss: `gradlew build`, alle GameTests, `runClient` mit EMI + Xaero, Rezeptanzeige stichprobenartig.

## 7. Teilphasen (Vorschlag)

| Teilphase | Inhalt |
|---|---|
| 11.0 | diese Spezifikation |
| 11.1 | EMI-Grundgerüst + IC2-Kategorien (2.1 IC2, 2.2 Recycler/Schrottbox/Massefabrikator/Canning/Brennstoffe/Geothermie), Ausblenden, Klickbereiche IC2 |
| 11.2 | EMI GregTech (9 Kategorien) + Klickbereiche |
| 11.3 | EMI BuildCraft/Forestry/Kraftwerke (Raffinerie, Verbrennungsmotor, Fabricator, Carpenter, Koksofen, Kessel) + Multiblock-Infoseiten |
| 11.4 | Xaero's Minimap + Forestry-Ledger (+ Zugriffsrechte je nach E2) |
| 11.5 | verbundene GT-Gehäusetexturen (+ Texturarbeiten je nach E1) |
| 11.6 | Testplan, Abschlusstests |

## 8. Entscheidungen (Nutzer, 2026-10-16)

- **E1 Texturen:** geschnittene Originaltexturen bleiben; neu nur die verbundenen GT-Gehäusetexturen (11.5).
- **E2 Forestry-Zugriffsrechte:** entfallen; nur die Ledger (11.4). In `abweichungen.md` vermerkt.
- **E3 Xaero:** nur Xaero's Minimap, keine World Map.
- **E4 Nicht zugeordnete IC2-Inhalte** (Miner, Terraformer, Teleporter, Tesla-Spule, Crops/Crop-Matron,
  Personal-Blöcke, Luminator, Bauschaum + CF-Pack, Kaffee/Fass; Solar-Hüte, Upgrade-Stack 4, GT-Recycler-Blacklist):
  eigene **Phase 12** nach dem Feinschliff; deren EMI-Kategorien kommen dann in Phase 12 dazu.
