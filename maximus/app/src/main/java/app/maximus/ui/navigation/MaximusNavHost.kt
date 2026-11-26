package app.maximus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NamedNavArgument
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
import app.maximus.ui.theme.ModulePalette
import app.maximus.ui.theme.ModuleTheme

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
        moduleScreen(Routes.SETTINGS, ModulePalette.GENERAL) {
            SettingsScreen(onBack = { navController.popBackStack() }, services = services)
        }
        moduleScreen(Routes.DIAGNOSTICS, ModulePalette.GENERAL) {
            DiagnosticsScreen(repository = services.diagnostics, onBack = { navController.popBackStack() })
        }
        moduleScreen(Routes.CORE, ModulePalette.GENERAL) {
            CoreHubScreen(
                services = services,
                onBack = { navController.popBackStack() },
                onOpenNote = { id -> navController.navigate(Routes.note(id)) }
            )
        }
        moduleScreen(Routes.NOTE, ModulePalette.GENERAL, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            NoteEditorScreen(
                services = services,
                noteId = entry.arguments?.getLong("id") ?: 0L,
                onBack = { navController.popBackStack() }
            )
        }
        moduleScreen(Routes.NUTRITION, ModulePalette.NUTRITION) {
            NutritionHubScreen(services = services, onBack = { navController.popBackStack() })
        }
        moduleScreen(Routes.DND, ModulePalette.DND) {
            DndHubScreen(
                services = services,
                onBack = { navController.popBackStack() },
                onOpenSheet = { id -> navController.navigate(Routes.dndSheet(id)) },
                onEditCharacter = { id -> navController.navigate(Routes.dndEdit(id)) }
            )
        }
        moduleScreen(Routes.DND_SHEET, ModulePalette.DND, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            CharacterSheetScreen(
                services = services,
                characterId = entry.arguments?.getLong("id") ?: 0L,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.dndEdit(id)) }
            )
        }
        moduleScreen(Routes.DND_EDIT, ModulePalette.DND, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
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
        moduleScreen(Routes.STRONGMAN, ModulePalette.STRONGMAN) {
            StrongmanHubScreen(
                repository = services.strongman,
                onBack = { navController.popBackStack() },
                onOpenProgram = { id -> navController.navigate(Routes.program(id)) }
            )
        }
        moduleScreen(Routes.PROGRAM, ModulePalette.STRONGMAN, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            ProgramEditorScreen(
                repository = services.strongman,
                programId = entry.arguments?.getLong("id") ?: 0L,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun NavGraphBuilder.moduleScreen(
    route: String,
    palette: ModulePalette,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(route, arguments = arguments) { entry ->
        ModuleTheme(palette) { content(entry) }
    }
}
