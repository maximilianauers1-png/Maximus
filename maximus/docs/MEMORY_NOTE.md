# Speichernotiz – Phase P0

Status: **NICHT GEMESSEN.** Diese Notiz enthält das Messprotokoll und eine analytische
Obergrenze. Die Messwerte werden nach dem ersten Lauf auf dem Zielgerät (8 GB RAM) eingetragen.

## Messprotokoll

1. Release-Build installieren (`./gradlew :app:installRelease`, R8 aktiv), App kalt starten.
2. Übersicht öffnen, 10 s warten, dann Diagnose öffnen (erzwingt Keystore-Unwrap + SQLCipher-Open).
3. `adb shell dumpsys meminfo app.maximus` dreimal im Abstand von 5 s; Median von `TOTAL PSS` notieren.
4. Zusätzlich den In-App-Wert „App-PSS gesamt“ (Debug.MemoryInfo.totalPss) notieren.
5. App in den Hintergrund, `adb shell am send-trim-memory app.maximus BACKGROUND`,
   erneut messen (prüft den Freigabepfad von C4; in P0 ist noch keine Schwerkomponente resident).

| Zustand                        | TOTAL PSS (dumpsys) | In-App-PSS | Java-Heap | Native Heap |
|--------------------------------|---------------------|------------|-----------|-------------|
| Kaltstart, Übersicht           | –                   | –          | –         | –           |
| Nach Diagnose (DB offen)       | –                   | –          | –         | –           |
| Nach TRIM_MEMORY_BACKGROUND    | –                   | –          | –         | –           |

## Analytische Erwartung (Größenordnung, keine Messung)

Eine Compose-App mit Material 3 liegt nach Kaltstart typischerweise im Bereich 60–150 MB PSS;
SQLCipher (libsqlcipher.so + Seitencache, Standard 2000 Seiten × 4 KiB ≈ 8 MiB) addiert < 20 MB.
Erwartung P0: ≪ 1,5 GB (typisches Budget C1). Die kritische Größe entsteht erst in P7:

  M_LLM ≈ M_weights(Q4_K_M) + M_KV + M_scratch,
  M_KV  = 2 · n_layer · n_kv_head · d_head · n_ctx · 2 Byte (fp16).

Beispiel n_ctx = 2048: 16 · 8 · 64 (Llama-3.2-1B-Architektur) → 64 MiB;
28 · 2 · 128 (Qwen2.5-1.5B-Architektur) → 56 MiB. Architekturparameter aus dem Gedächtnis,
vor P7 aus den GGUF-Metadaten verifizieren.

---

# Speichernotiz – Phase P2 (Strongman)

Status: **NICHT GEMESSEN.** P2 fügt keine Schwerkomponente hinzu (keine neue Bibliothek; Diagramme
sind eigener Compose-Canvas-Code). Protokoll wie P0, zusätzlich:

6. Verlauf-Tab mit „Alles“ öffnen (alle Diagramme aktiv), dreimal `dumpsys meminfo` wie oben.
7. PNG-Export eines Diagramms auslösen (erzeugt eine Bitmap in Canvas-Größe) und erneut messen.

| Zustand                                  | TOTAL PSS (dumpsys) | In-App-PSS | Java-Heap | Native Heap |
|------------------------------------------|---------------------|------------|-----------|-------------|
| Verlauf-Tab, Bereich „Alles“             | –                   | –          | –         | –           |
| Direkt nach PNG-Export                   | –                   | –          | –         | –           |

## Analytische Obergrenze

Datenmenge: 10 Jahre × 4 Einheiten/Woche × 25 Sätze ≈ 5,2·10⁴ Zeilen `set_log`.
Pro Zeile ≈ 120 B in SQLite → ≈ 6 MB Datenbank. Der Verlauf lädt alle Zeilen als `AnalyticsSet`
(≈ 100 B inkl. Objekt-Header und Boxing) → ≈ 5 MB Java-Heap pro Emission; die Aggregationen
(Wochenvolumen, Heatmap, Histogramm) sind O(n) bzw. O(n log n) mit Hilfsstrukturen ≪ n.
PNG-Export: Breite × Höhe × 4 B, z. B. 1080 × 660 px ≈ 2,9 MB, kurzlebig.
Erwartung P2: + < 20 MB gegenüber P0, weit unter C1.

Rechenzeit PR-Erkennung: chronologischer Durchlauf mit Pareto-Front F; jede Prüfung kostet O(|F|),
und |F| ≤ Anzahl verschiedener Wiederholungszahlen (≤ ≈ 20) → O(n) pro geloggtem Satz.

# P1 – Tresor, Kalender, Notizbuch (+ Design „Porphyr & Messing“)

Messprotokoll wie oben; zusätzlich: Tresor entsperren, 50 Einträge anlegen, Kalender über 24 Monate
blättern, Notiz mit 10 000 Wörtern in der Vorschau öffnen.

| Zustand                                  | TOTAL PSS (dumpsys) | In-App-PSS | Java-Heap | Native Heap |
|------------------------------------------|---------------------|------------|-----------|-------------|
| Tresor entsperrt, Liste sichtbar         | –                   | –          | –         | –           |
| Kalender, Monatsansicht                  | –                   | –          | –         | –           |
| Notiz-Vorschau (10 000 Wörter)           | –                   | –          | –         | –           |

## Analytische Obergrenze

Schriften: sechs statische, auf Latin-1 + Typografie reduzierte TTF-Schnitte ≈ 1,4 MB im APK,
zur Laufzeit gemappt (Font-Cache < 2 MB). Tresor: Klartext-VDK 32 B; ein Eintrag ≈ 1 KB,
1 000 Einträge ≈ 1 MB. Kalender: Expansion für ein 6-Wochen-Fenster ist O(E · k) mit k ≤ 42
Vorkommen pro Serie; 500 Serien → ≤ 2,1·10⁴ Objekte ≈ 2 MB, kurzlebig. Markdown: O(n) Zeilen,
10 000 Wörter ≈ 60 KB Text → < 1 MB AnnotatedStrings.
Erwartung P1: + < 10 MB gegenüber P2, weit unter C1 (3,0 GB).

# P3 – Ernährung (+ Design „Eisen & Schwarz“, Ritter-Maskottchen, Konjugat-Vorlagen)

| Zustand                                  | TOTAL PSS (dumpsys) | In-App-PSS | Java-Heap | Native Heap |
|------------------------------------------|---------------------|------------|-----------|-------------|
| Ernährung, Wochenplan (6 Mahlzeiten)     | –                   | –          | –         | –           |
| Ziele mit Prognosediagramm               | –                   | –          | –         | –           |

## Analytische Obergrenze

Rezept- und Lebensmitteldaten: 41 Rezepte, 70 Lebensmittel als Kotlin-Konstanten ≈ 60 KB Heap.
Wochenplan: 400 Stichproben × 7 Tage × ≤ 6 Mahlzeiten ≈ 1,7·10⁴ Rezeptzugriffe, kurzlebig, < 1 MB.
Hall-Modell: Löser = 60 Bisektionsschritte × Simulation über D Tage, bei D = 365 ≈ 2,2·10⁴ Euler-Schritte
pro Neuberechnung (< 5 ms), Trajektorie 365 Doubles. Ritter-Vektorgrafik: 73 Pfade, einmal geparst.
Schriften: Grenze Gotisch 3 × 54 KB ersetzt Cormorant. Erwartung P3: + < 10 MB gegenüber P1.

# P4 – D&D, Strongman-Rekorde, erweiterte Datenbanken

| Zustand                                      | TOTAL PSS (dumpsys) | In-App-PSS | Java-Heap | Native Heap |
|----------------------------------------------|---------------------|------------|-----------|-------------|
| Würfler mit Verteilung von 8d6               | –                   | –          | –         | –           |
| Würfler mit 10d10kh5 (große Verteilung)      | –                   | –          | –         | –           |
| Charakterbogen Stufe 20                      | –                   | –          | –         | –           |
| Rekord-Reiter mit 5 000 geloggten Sätzen     | –                   | –          | –         | –           |

## Analytische Obergrenze

Würfelverteilungen sind exakte Brüche über BigInteger. Der Träger einer Summe von n Würfeln mit s
Seiten hat n(s−1)+1 Werte; das „k aus n“-Verfahren hat O(n·s·k) Zustände. Die Obergrenze
MAX_OUTCOME_STATES = 4·10⁵ begrenzt den Speicher auf etwa 40 MB im schlimmsten Fall und löst sonst
TooComplexException aus. Typische Ausdrücke (≤ 20 Würfel) bleiben unter 1 MB.
SRD-Inhalte: 14 Völker, 12 Klassen, 13 Hintergründe als Kotlin-Konstanten ≈ 90 KB.
Übungen 147 × ≈ 150 B ≈ 22 KB; Lebensmittel 135 und Rezepte 82 ≈ 130 KB.
Trainingslast: ein Punkt je Kalendertag seit dem ersten Eintrag, 5 Jahre ≈ 1 825 Objekte, < 200 KB.
Erwartung P4: + < 15 MB gegenüber P3, weit unter C1 (3,0 GB).

# P4b – D&D-Überarbeitung (Charakterbogen und Editor)

| Zustand                                        | TOTAL PSS | In-App-PSS | Java-Heap | Native Heap |
|------------------------------------------------|-----------|------------|-----------|-------------|
| Editor, Zauberliste ungefiltert (177 Einträge) | –         | –          | –         | –           |
| Charakterbogen Stufe 20, Multiclass            | –         | –          | –         | –           |

## Analytische Obergrenze

Inhalte als Kotlin-Konstanten: 27 Völker, 24 Unterklassen, 177 Zauber, 12 Talente ≈ 320 KB im
Konstantenpool, einmalig geladen. Der Zauberfilter arbeitet auf der Liste ohne Kopie des Textes;
eine Filterung erzeugt höchstens 177 Referenzen. Ein Charakter als JSON ≈ 2–6 KB. Der Bogen hält
nur eine CharacterSheet-Instanz; deren Listen umfassen 18 Fertigkeiten, ≤ 10 Angriffe und ≤ 80
Klassenmerkmale. Erwartung: + < 3 MB gegenüber P4.

# P5 – Physik- und Mathe-Labor (+ D&D-Builds, Würfel-Bubble, Diagramm-Ränder)

| Zustand                                         | TOTAL PSS | In-App-PSS | Java-Heap | Native Heap |
|-------------------------------------------------|-----------|------------|-----------|-------------|
| Labor, Übersicht                                | –         | –          | –         | –           |
| Rechner „Magnetokalorik“ mit drei Kurven        | –         | –          | –         | –           |
| Funktionsplotter mit drei Funktionen            | –         | –          | –         | –           |
| D&D Builds-Tab (60 DPR-Charaktere berechnet)    | –         | –          | –         | –           |

## Analytische Obergrenze

Inhalte als Kotlin-Konstanten, erst beim ersten Zugriff geladen (`by lazy`): Kompendium 75 Kapitel
(8 Gebiete, Mathe in 11 Vorlesungen bis Analysis III/Funktionalanalysis) ≈ 200 KB Text (UTF-16) + 718 Formelkarten,
144 Konzeptfragen, 90 Aufgabengeneratoren, 69 Rechner → < 1 MB Heap.
Fortschritt: ein Text-Datensatz in app_meta (Zeile pro Karteikarte), bei 718 Karten ≈ 22 KB.
Rechnerkurven: ≤ 6 Reihen × ≤ 600 Punkte × 2 Doubles ≈ 60 KB pro Ergebnis, kurzlebig; Rechnung auf
Dispatchers.Default. Debye-Entropietabelle 2001 Doubles ≈ 16 KB pro Θ_D (einmalig, gecacht).
Teuerste Rechnung: Magnetokalorik-Kurven 3 × 71 Temperaturen × Brent (≈ 40 Iterationen) mit
Molekularfeld-Lösung je Auswertung → ≈ 100 ms (JVM-Messung), kein Speicherproblem.
Builds-Tab: 3 × 20 Charakterbögen (je < 10 KB) für die DPR-Kurven. Erwartung: + < 10 MB gegenüber P4b.
