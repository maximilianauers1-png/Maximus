package app.maximus.core.app;

import app.maximus.calendar.data.CalendarRepository;
import app.maximus.core.backup.BackupManager;
import app.maximus.diagnostics.DiagnosticsRepository;
import app.maximus.dnd.data.DndRepository;
import app.maximus.notes.data.NotesRepository;
import app.maximus.nutrition.data.NutritionRepository;
import app.maximus.strongman.data.StrongmanRepository;
import app.maximus.vault.data.VaultRepository;
import app.maximus.vault.data.VaultSession;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AppServices_Factory implements Factory<AppServices> {
  private final Provider<DiagnosticsRepository> diagnosticsProvider;

  private final Provider<StrongmanRepository> strongmanProvider;

  private final Provider<VaultSession> vaultSessionProvider;

  private final Provider<VaultRepository> vaultProvider;

  private final Provider<CalendarRepository> calendarProvider;

  private final Provider<NotesRepository> notesProvider;

  private final Provider<BackupManager> backupProvider;

  private final Provider<NutritionRepository> nutritionProvider;

  private final Provider<DndRepository> dndProvider;

  private AppServices_Factory(Provider<DiagnosticsRepository> diagnosticsProvider,
      Provider<StrongmanRepository> strongmanProvider, Provider<VaultSession> vaultSessionProvider,
      Provider<VaultRepository> vaultProvider, Provider<CalendarRepository> calendarProvider,
      Provider<NotesRepository> notesProvider, Provider<BackupManager> backupProvider,
      Provider<NutritionRepository> nutritionProvider, Provider<DndRepository> dndProvider) {
    this.diagnosticsProvider = diagnosticsProvider;
    this.strongmanProvider = strongmanProvider;
    this.vaultSessionProvider = vaultSessionProvider;
    this.vaultProvider = vaultProvider;
    this.calendarProvider = calendarProvider;
    this.notesProvider = notesProvider;
    this.backupProvider = backupProvider;
    this.nutritionProvider = nutritionProvider;
    this.dndProvider = dndProvider;
  }

  @Override
  public AppServices get() {
    return newInstance(diagnosticsProvider.get(), strongmanProvider.get(), vaultSessionProvider.get(), vaultProvider.get(), calendarProvider.get(), notesProvider.get(), backupProvider.get(), nutritionProvider.get(), dndProvider.get());
  }

  public static AppServices_Factory create(Provider<DiagnosticsRepository> diagnosticsProvider,
      Provider<StrongmanRepository> strongmanProvider, Provider<VaultSession> vaultSessionProvider,
      Provider<VaultRepository> vaultProvider, Provider<CalendarRepository> calendarProvider,
      Provider<NotesRepository> notesProvider, Provider<BackupManager> backupProvider,
      Provider<NutritionRepository> nutritionProvider, Provider<DndRepository> dndProvider) {
    return new AppServices_Factory(diagnosticsProvider, strongmanProvider, vaultSessionProvider, vaultProvider, calendarProvider, notesProvider, backupProvider, nutritionProvider, dndProvider);
  }

  public static AppServices newInstance(DiagnosticsRepository diagnostics,
      StrongmanRepository strongman, VaultSession vaultSession, VaultRepository vault,
      CalendarRepository calendar, NotesRepository notes, BackupManager backup,
      NutritionRepository nutrition, DndRepository dnd) {
    return new AppServices(diagnostics, strongman, vaultSession, vault, calendar, notes, backup, nutrition, dnd);
  }
}
