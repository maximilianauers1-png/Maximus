package app.maximus.core.app

import app.maximus.calendar.data.CalendarRepository
import app.maximus.chat.data.ChatService
import app.maximus.core.backup.BackupManager
import app.maximus.diagnostics.DiagnosticsRepository
import app.maximus.dnd.data.DndRepository
import app.maximus.lab.data.LabRepository
import app.maximus.notes.data.NotesRepository
import app.maximus.nutrition.data.NutritionRepository
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.vault.data.VaultRepository
import app.maximus.vault.data.VaultSession
import javax.inject.Inject
import javax.inject.Singleton

/** Single injection point for the UI layer. */
@Singleton
class AppServices @Inject constructor(
    val diagnostics: DiagnosticsRepository,
    val strongman: StrongmanRepository,
    val vaultSession: VaultSession,
    val vault: VaultRepository,
    val calendar: CalendarRepository,
    val notes: NotesRepository,
    val backup: BackupManager,
    val nutrition: NutritionRepository,
    val dnd: DndRepository,
    val lab: LabRepository,
    val chat: ChatService
)
