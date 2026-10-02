package app.maximus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.maximus.core.app.AppServices
import app.maximus.ui.core.CoreHubScreen
import app.maximus.ui.core.NoteEditorScreen
import app.maximus.ui.dnd.CharacterEditorScreen
import app.maximus.ui.dnd.CharacterSheetScreen
import app.maximus.ui.dnd.DndHubScreen
import app.maximus.ui.nutrition.NutritionHubScreen
import app.maximus.ui.screens.DiagnosticsScreen
import app.maximus.ui.screens.HomeScreen
import app.maximus.ui.screens.SettingsScreen
import app.maximus.ui.strongman.ProgramEditorScreen
import app.maximus.ui.strongman.StrongmanHubScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DIAGNOSTICS = "diagnostics"
    const val CORE = "core"
    const val NOTE = "core/note/{id}"
    const val NUTRITION = "nutrition"
    const val DND = "dnd"
    const val DND_SHEET = "dnd/sheet/{id}"
    const val DND_EDIT = "dnd/edit/{id}"
    const val STRONGMAN = "strongman"
    const val PROGRAM = "strongman/program/{id}"
    fun note(id: Long) = "core/note/$id"
    fun dndSheet(id: Long) = "dnd/sheet/$id"
    fun dndEdit(id: Long) = "dnd/edit/$id"
    fun program(id: Long) = "strongman/program/$id"
}

@Composable
fun MaximusNavHost(services: AppServices) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenDiagnostics = { navController.navigate(Routes.DIAGNOSTICS) },
                onOpenModule = { route -> navController.navigate(route) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() }, services = services)
        }
        composable(Routes.DIAGNOSTICS) {
            DiagnosticsScreen(repository = services.diagnostics, onBack = { navController.popBackStack() })
        }
        composable(Routes.CORE) {
            CoreHubScreen(
                services = services,
                onBack = { navController.popBackStack() },
                onOpenNote = { id -> navController.navigate(Routes.note(id)) }
            )
        }
        composable(Routes.NOTE, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            NoteEditorScreen(
                services = services,
                noteId = entry.arguments?.getLong("id") ?: 0L,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.NUTRITION) {
            NutritionHubScreen(services = services, onBack = { navController.popBackStack() })
        }
        composable(Routes.DND) {
            DndHubScreen(
                services = services,
                onBack = { navController.popBackStack() },
                onOpenSheet = { id -> navController.navigate(Routes.dndSheet(id)) },
                onEditCharacter = { id -> navController.navigate(Routes.dndEdit(id)) }
            )
        }
        composable(Routes.DND_SHEET, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            CharacterSheetScreen(
                services = services,
                characterId = entry.arguments?.getLong("id") ?: 0L,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.dndEdit(id)) }
            )
        }
        composable(Routes.DND_EDIT, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            CharacterEditorScreen(
                services = services,
                characterId = entry.arguments?.getLong("id") ?: 0L,
                onBack = { navController.popBackStack() },
                onOpenSheet = { id ->
                    navController.popBackStack()
                    navController.navigate(Routes.dndSheet(id))
                }
            )
        }
        composable(Routes.STRONGMAN) {
            StrongmanHubScreen(
                repository = services.strongman,
                onBack = { navController.popBackStack() },
                onOpenProgram = { id -> navController.navigate(Routes.program(id)) }
            )
        }
        composable(Routes.PROGRAM, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            ProgramEditorScreen(
                repository = services.strongman,
                programId = entry.arguments?.getLong("id") ?: 0L,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
