# Phase 37 – Nichts macht mehr schneller (freigegeben 2026-10-29)

Nutzer (2026-10-29): „Wir wollen, dass sowas wie Gravi Suit und Quantum und Power Suit einen nicht mehr schneller
machen. Advanced Jetpack auch nicht. Es soll einfach nichts mehr geben, was einen schneller macht. Gib den Sachen als
Ausgleich dann etwas als Alternative.“

Bewusste Abweichung von Grundregel 1 (1:1-Mechanik) auf Nutzerwunsch; nach Umsetzung in `docs/abweichungen.md`.

## 1 Grundsatz

- Kein Craft-Inhalt bringt den Spieler **horizontal schneller als Vanilla-Sprinten** (≈ 5,6 m/s, 0,28 Blöcke/Tick) –
  am Boden, in der Luft und im Wasser.
- **Fliegen und Schweben bleiben** (Höhe gewinnen, Fallschutz); nur das Vorwärtstempo wird gedeckelt.
- Sprunghöhe bleibt (Höhe ist kein Tempo), horizontale Sprung-Schübe entfallen.
- Jedes betroffene Teil bekommt einen **Ausgleich**, der nichts mit Tempo zu tun hat.

Umsetzung zentral: `dev.craft.core.MovementCap.clampHorizontal(Vec3)` (Deckel 0,28 Blöcke/Tick) auf die Bewegung, die
ein betroffenes Teil setzt (nur wenn es getragen und aktiv ist), dazu die Einzeländerungen unten. Kein Hunger beim
Sprinten (Quantenhose): Mixins `SprintExhaustionMixin` (`ServerPlayer#checkMovementStatistics`) und
`SprintJumpExhaustionMixin` (`Player#jumpFromGround`) überspringen `causeFoodExhaustion`, solange gesprintet wird;
Rückstoß über `LivingKnockBackEvent` (`ArmorEvents`).

## 2 Bestandsaufnahme – was heute schneller macht

| Teil | Heute (Pack) | Tempo grob |
|---|---|---|
| Quantenhose (IC2) | Sprinten: Zusatzschub 0,22/Tick, im Wasser 0,1 + Auftrieb; 1 000 EU je 10 Ticks | ≈ 3× Sprint |
| Quantenstiefel (IC2) | Boost + Springen: Sprung mit horizontal ×3,5 | Weitsprung |
| Jetpack / Elektro-Jetpack (IC2) | kleiner Vorwärtsschub | ≈ 4,6 m/s, **unter** Sprint |
| Fortg. Jetpack, Fortg. Nano-Brustplatte (GraviSuite) | Vorwärtsschub (Schwebemodus stärker) + **Boost** 0,07/0,09 (3× Verbrauch) | ≈ 6–7 m/s, Boost ≈ 11–12 m/s |
| Gravitations-Brustplatte (GraviSuite) | Kreativ-Flug (Standard-Fluggeschwindigkeit, Sprint-Fliegen ×2) + **Boost** 0,11 | ≈ 11 m/s, Sprint ≈ 22 m/s, Boost mehr |
| MPS Sprint Assist (Hose) | Laufgeschwindigkeit ×(1 … 3) beim Sprinten, ×(1 … 2) beim Gehen | bis 3× |
| MPS Jump Assist (Hose) | Luftsteuerung 0,7 × Laufgeschwindigkeit statt 0,02 | schnelle Sprünge |
| MPS Swim Boost (Hose) | Schub im Wasser in Blickrichtung | schneller als Schwimmen |
| MPS Jetpack, Jet Boots, Flight Control | Schub in Blickrichtung (mit Flight Control frei lenkbar) | je nach Schub hoch |
| MPS Glider (Brust) | wandelt Fall in Vorwärtsfahrt um | über Sprint möglich |
| Kaffee (IC2) | Effekt Tempo (+ Eile), stapelbar | Tempo I–III |

Nicht betroffen: Nano-Rüstung, Static Boots, Long Fall Boots, Shock Absorber, Uphill Step Assist, Parachute, Blink
Drive (Teleport, siehe Frage F4), Werkzeug-Abbautempo.

## 3 Änderungen und Ausgleich

### 3.1 Quantenhose
- **Entfällt:** Sprint-Zusatzschub an Land und im Wasser (auch der Auftrieb beim Springen im Wasser).
- **Ausgleich „Ausdauer-Servos“:** solange geladen und getragen
  - Sprinten verbraucht **keinen Hunger** (Erschöpfung durch Sprinten/Springen wird aufgehoben),
  - **100 % Rückstoßresistenz**.
  - Verbrauch wie bisher 1 000 EU je 10 Ticks Sprinten.

### 3.2 Quantenstiefel
- **Entfällt:** horizontaler Faktor ×3,5 beim Boost-Sprung. Der hohe Sprung (Höhe) bleibt.
- **Ausgleich:** keiner nötig (Sprunghöhe und Fallschutz bleiben) – siehe Frage F3.

### 3.3 Jetpack / Elektro-Jetpack (IC2)
- Liegt schon unter Sprinttempo; nur der gemeinsame Deckel wird angehängt. Kein Ausgleich.

### 3.4 Fortgeschrittenes Jetpack und Fortg. Nano-Brustplatte
- **Entfällt:** Boost (Boost-Taste ohne Wirkung, kein 3-facher Verbrauch). Grund-Vorwärtsschub bleibt, gedeckelt auf
  Sprinttempo.
- **Ausgleich „Sparantrieb“:** Flugverbrauch **halbiert** (doppelte Flugzeit). Die LapPack-Ladefunktion bleibt.

### 3.5 Gravitations-Brustplatte
- **Entfällt:** Boost; Sprint-Fliegen bringt nichts mehr; Fluggeschwindigkeit auf Sprinttempo gesenkt
  (Fluggeschwindigkeit des Spielers ≈ halbiert, Sprint beim Fliegen wird ignoriert). Steigen/Sinken unverändert.
- **Ausgleich „Gravitationsmagnet“:** zieht Items und Erfahrungskugeln im Umkreis von **8 Blöcken** zum Spieler
  (eigener Ein/Aus-Schalter, Boost-Taste + Modus-Taste; Zustand im HUD). Kosten: **50 EU je angezogenem
  Item-Stapel**, Erfahrung kostenlos; funktioniert auch am Boden und ohne Flug.

### 3.6 Modular Powersuits
- **Sprint Assist:** Tempo-Multiplikatoren entfallen (Gehen und Sprinten). **Ausgleich:** der Regler „Power“ erhöht
  stattdessen die **Tragkraft** (Gewichtsgrenze 25 000 g + bis zu 25 000 g, also bis zu doppelt so viel Panzerung/
  Batterien ohne Bremswirkung); „Compensation“ (weniger Hunger beim Sprinten) bleibt; Energiekosten wie bisher.
- **Jump Assist:** erhöhte Luftsteuerung entfällt, Sprunghöhe bleibt. Kein weiterer Ausgleich.
- **Swim Boost:** Schub entfällt. **Ausgleich „Taucher-Servos“:** unter Wasser **kein Abbau-Malus** (wie Wasseraffinität,
  auch nicht beim Schwimmen ohne Bodenkontakt) für jedes Werkzeug; Energie wie bisher je Tick unter Wasser.
- **Jetpack, Jet Boots, Flight Control, Glider:** bleiben, horizontal auf Sprinttempo gedeckelt (Steigen/Schweben
  unverändert). Kein weiterer Ausgleich.

### 3.7 Kaffee
- **Entfällt:** Effekt Tempo. **Ausgleich:** an seine Stelle tritt **Sättigung** (gleiche Stapel-/Überdosis-Logik:
  Stufe aus Tempo wird Stufe Sättigung; Eile bleibt).

## 4 Fragen an den Nutzer

- **F1** Deckel „nie schneller als Sprinten“ auch beim **Fliegen** (Gravi, Jetpacks, MPS)? Vorschlag: ja.
  Alternative: Fliegen nur so schnell wie **Gehen** (≈ 4,3 m/s).
- **F2** Sind die Ausgleiche so recht (Quantenhose Hunger/Rückstoß, Fortg. Jetpack halber Verbrauch, Gravi-Magnet,
  MPS Tragkraft/Taucher, Kaffee Sättigung)?
- **F3** Quantenstiefel: nur Weitsprung weg (Vorschlag) oder auch der hohe Sprung?
- **F4** Gilt das auch für Dinge **außerhalb von Craft** (Vanilla: Elytra, Tempotränke, Leuchtfeuer, Seelenschnelligkeit,
  Pferde; Pack-Mods: Create: Hypertubes, Ad Astra …) und für den MPS-**Blink Drive** (Teleport)? Vorschlag: nein, nur
  Craft-Rüstung/-Items; Blink Drive bleibt.

## 5 Teilphasen

- 37.0 Spezifikation (dieses Dokument) – **freigegeben 2026-10-29** (Nutzer: „alles nach deinem Vorschlag“, also F1 Deckel
  Sprinttempo auch beim Fliegen, F2 Ausgleiche wie §3, F3 nur Weitsprung weg, F4 nur Craft-Inhalte, Blink Drive bleibt)
- 37.1 Gemeinsamer Deckel, IC2 (Quantenhose/-stiefel, Jetpacks, Kaffee) + GameTests – erledigt
- 37.2 GraviSuite (Fortg. Jetpack, Nano-Brustplatte, Gravi-Brustplatte mit Magnet) + GameTests
- 37.3 MPS (Sprint/Jump/Swim, Deckel für Jet/Glider) + GameTests
- 37.4 EMI-Texte, Tooltips, Wiki, `spec/28`/`spec/29`/`spec/17`-Verweise, `abweichungen.md`, Testplan
