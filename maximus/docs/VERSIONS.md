# Abhängigkeiten – Verifikationsstand 2026-09-28

[V] = gegen offizielle Release-Seite oder GitHub-Release-Tag geprüft; [U] = nicht geprüft.

| Artefakt | Version | Status | Quelle |
|---|---|---|---|
| com.android.application (AGP) | 9.4.0 | [V] Minor-Version; Patchstand prüfen | AGP-9.4.0-Release-Notes (Sept. 2026) |
| Gradle Wrapper | 9.8.0 | [V] existiert (27.09.2026); Kompatibilität mit AGP 9.4 [U] | Gradle-Release-Notes |
| Kotlin (Compose-Compiler-Plugin) | 2.4.20 | [V] | github.com/JetBrains/kotlin |
| com.google.devtools.ksp | 2.3.12 | [V] | github.com/google/ksp |
| Hilt / Dagger | 2.60.1 | [V]; AGP-9-Kompatibilität [U] | github.com/google/dagger |
| androidx.core:core-ktx | 1.19.1 | [V] | AndroidX Stable Channel |
| androidx.appcompat:appcompat | 1.8.0 | [V] | AndroidX Stable Channel |
| androidx.activity:activity-compose | 1.11.0 | [U] | – |
| androidx.compose.ui/foundation | 1.12.1 | [V] | AndroidX Compose-Tabelle |
| androidx.compose.material3 | 1.4.0 | [V] | AndroidX Compose-Tabelle |
| androidx.navigation:navigation-compose | 2.10.2 | [V] | AndroidX Stable Channel |
| androidx.room (2.x-Linie) | 2.8.5 | [V] | AndroidX Stable Channel |
| androidx.sqlite:sqlite | 2.7.1 | [V] | AndroidX Stable Channel |
| net.zetetic:sqlcipher-android | 4.19.0 | [V] | github.com/sqlcipher/sqlcipher-android |
| kotlinx-coroutines-android | 1.10.2 | [U] | – |
| junit:junit | 4.13.2 | [U] | – |
| androidx.test.ext:junit | 1.3.0 | [U] | – |
| androidx.test:runner | 1.7.0 | [U] | – |

Später (nicht in P0 eingebunden): Vico 3.3.1 [V], ONNX Runtime 1.30.0 [V], KaTeX 0.18.9 [V];
llama.cpp wird als Git-Submodul auf einen festen Commit gepinnt (keine SemVer-Releases).

Bewusste Entscheidungen:
- Room 2.8.x statt Room3 3.0.x: Room3 ist auf SQLiteDriver umgestellt; der dokumentierte
  SQLCipher-Weg ist SupportOpenHelperFactory (SupportSQLite-API).
- Kein Compose-BOM: die BOM-Version konnte nicht verifiziert werden; stattdessen explizit
  gepinnte, verifizierte Einzelversionen.
- Kein hilt-navigation-compose: Artefaktname und -version für hiltViewModel() unverifiziert;
  P0 kommt ohne ViewModel-Injektion aus.
- compileSdk/targetSdk 36: Android 17 war laut developer.android.com zuletzt im Beta-Status.
