package app.maximus.ui.nutrition

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.chat.domain.Focus
import app.maximus.core.app.AppServices
import app.maximus.nutrition.data.plannedMealId
import app.maximus.nutrition.data.toPoint
import app.maximus.nutrition.domain.AdaptiveTdee
import app.maximus.nutrition.domain.MacroTargets
import app.maximus.nutrition.domain.MealOverride
import app.maximus.nutrition.domain.MealPlanner
import app.maximus.nutrition.domain.NutritionTargets
import app.maximus.nutrition.domain.PlanSettings
import app.maximus.nutrition.domain.Recipe
import app.maximus.nutrition.domain.RecipeBook
import app.maximus.nutrition.domain.WeekBook
import app.maximus.nutrition.domain.WeekEntry
import app.maximus.nutrition.domain.WeekPlan
import app.maximus.ui.chat.AskMaximusButton
import app.maximus.ui.chat.AskMaximusSheet
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.components.MaximusTopBar
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun isGerman(): Boolean = LocalConfiguration.current.locales[0].language == "de"

fun Recipe.name(de: Boolean) = if (de) nameDe else nameEn
fun Recipe.steps(de: Boolean) = if (de) stepsDe else stepsEn

internal enum class NutritionPage(val label: Int, val icon: Glyph? = null) {
    TODAY(R.string.cal_today, Glyph.TODAY),
    PLAN(R.string.nu_nav_plan, Glyph.CALENDAR),
    KITCHEN(R.string.nu_tab_kitchen, Glyph.BOWL),
    JOURNAL(R.string.nu_tab_journal, Glyph.BOOK),
    SHOPPING(R.string.nu_shopping),
    PREP(R.string.nu_batch_prep),
    COACH(R.string.nu_tab_coach),
    SAVED_WEEK(R.string.nu_saved_week),
    GOALS(R.string.nu_tab_goals),
    TOOLS(R.string.nu_tab_tools)
}

private data class PlanRequest(val targets: List<MacroTargets>, val settings: PlanSettings, val recipes: List<Recipe>)
private data class PlanResult(val request: PlanRequest, val plan: WeekPlan?)

@Composable
fun NutritionHubScreen(services: AppServices, onBack: () -> Unit, onOpenChat: () -> Unit = {}) {
    val repo = services.nutrition
    val profile by repo.profile.collectAsState(initial = null)
    val log by repo.log.collectAsState(initial = emptyList())
    val meals by repo.meals.collectAsState(initial = emptyList())
    val customRecipes by repo.customRecipes.collectAsState(initial = emptyList())
    val book = remember(customRecipes) { RecipeBook(customRecipes) }
    var ask by remember { mutableStateOf(false) }
    val today by produceState(LocalDate.now()) {
        while (true) {
            value = LocalDate.now()
            delay(30_000)
        }
    }
    val epochDay = today.toEpochDay()
    val dayIndex = today.dayOfWeek.value - 1
    val monday = today.minusDays(dayIndex.toLong())
    val trainingLogged by remember(epochDay) { services.strongman.trainingLogged(epochDay) }.collectAsState(initial = false)
    var page by rememberSaveable { mutableStateOf(NutritionPage.TODAY) }
    var previous by rememberSaveable { mutableStateOf(NutritionPage.TODAY) }
    var main by rememberSaveable { mutableStateOf(NutritionPage.TODAY) }
    var weekOffset by rememberSaveable { mutableIntStateOf(0) }
    var selectedDay by rememberSaveable { mutableIntStateOf(0) }
    var selectedSlot by rememberSaveable { mutableIntStateOf(-1) }
    var swapping by rememberSaveable { mutableStateOf(false) }
    val week = monday.plusWeeks(weekOffset.toLong())
    val weekId = week.toEpochDay()
    val tabs = rememberSaveableStateHolder()
    val scope = rememberCoroutineScope()
    val snacks = remember { SnackbarHostState() }
    val failedMessage = stringResource(R.string.nu_save_failed)
    val savedMessage = stringResource(R.string.nu_saved)

    fun write(action: suspend () -> Unit) {
        scope.launch {
            try {
                action()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                snacks.showSnackbar(failedMessage)
            }
        }
    }

    fun navigate(next: NutritionPage) {
        if (next == page) return
        selectedSlot = -1
        swapping = false
        if (next.icon != null) main = next else if (next != NutritionPage.TOOLS) previous = main
        if (next == NutritionPage.TODAY) weekOffset = 0
        page = next
    }

    fun back() {
        when (page) {
            NutritionPage.TODAY -> onBack()
            NutritionPage.TOOLS -> page = NutritionPage.GOALS
            NutritionPage.GOALS, NutritionPage.SHOPPING, NutritionPage.PREP,
            NutritionPage.COACH, NutritionPage.SAVED_WEEK -> navigate(previous)
            else -> navigate(NutritionPage.TODAY)
        }
    }

    fun openMeal(day: Int, slot: Int) {
        selectedDay = day
        selectedSlot = slot
        swapping = false
    }

    BackHandler(enabled = page != NutritionPage.TODAY) { back() }
    val selectedTab = when (page) {
        NutritionPage.SHOPPING -> NutritionPage.PLAN
        NutritionPage.PREP -> NutritionPage.KITCHEN
        else -> main
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snacks) },
        topBar = {
            val title = when (page) {
                NutritionPage.GOALS, NutritionPage.TOOLS, NutritionPage.COACH, NutritionPage.SAVED_WEEK -> page.label
                else -> R.string.module_nutrition
            }
            MaximusTopBar(stringResource(title), { back() }) {
                if (page != NutritionPage.GOALS && page != NutritionPage.TOOLS) {
                    TextButton(onClick = { navigate(NutritionPage.GOALS) }) { Text(stringResource(R.string.nu_tab_goals)) }
                }
                AskMaximusButton(onClick = { ask = true })
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp
            ) {
                NutritionPage.entries.forEach { destination ->
                    val icon = destination.icon ?: return@forEach
                    val label = stringResource(destination.label)
                    NavigationBarItem(
                        selected = selectedTab == destination,
                        onClick = { navigate(destination) },
                        icon = { GlyphIcon(icon, tint = LocalContentColor.current, size = 21.dp) },
                        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        val p = profile
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (p == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                return@Box
            }

            val adaptive = remember(log, epochDay) { AdaptiveTdee.estimate(log.map { it.toPoint() }, epochDay) }
            val derived = remember(p, epochDay, adaptive) { NutritionTargets.derive(p, epochDay, adaptive) }
            val goals = remember(derived, p.plan) { MealPlanner.weeklyTargets(derived.macros, p.plan, derived.bmr) }
            val (activeSeed, overrides, checked) = key(weekId) {
                val seed by remember { repo.weekSeed(weekId) }.collectAsState(initial = null)
                val edits by remember { repo.planOverrides(weekId) }.collectAsState(initial = emptyList())
                val groceries by remember { repo.checkedShopping(weekId) }.collectAsState(initial = emptyMap())
                Triple(seed, edits, groceries)
            }
            val request = remember(goals, p.plan, activeSeed, book) {
                PlanRequest(goals, p.plan.copy(seed = activeSeed ?: p.plan.seed), book.all)
            }
            val solved by produceState<PlanResult?>(null, request) {
                value = withContext(Dispatchers.Default) {
                    PlanResult(request, MealPlanner.plan(request.targets, request.settings, request.recipes))
                }
            }
            val result = solved?.takeIf { it.request == request }
            val plan = remember(result, overrides, book) {
                result?.plan?.let { MealPlanner.applyOverrides(it, goals, p.plan, overrides, book.all) }
            }
            val list = remember(plan) { plan?.let(MealPlanner::shoppingList).orEmpty() }
            val latestWeight = remember(log, epochDay) { log.lastOrNull { it.epochDay <= epochDay && it.weightKg != null }?.weightKg }
            val loading = result == null
            val editPlan: ((PlanSettings) -> PlanSettings) -> Unit = { edit ->
                write { repo.updateProfile { current -> current.copy(plan = edit(current.plan)) } }
            }
            val logMeal: (Recipe, Double) -> Unit = { recipe, portions -> write { repo.logMeal(epochDay, recipe, portions) } }

            tabs.SaveableStateProvider(page.name) {
                when (page) {
                    NutritionPage.TODAY -> TodayTab(
                        day = today,
                        training = p.plan.trainingDays.takeIf { it.isNotEmpty() }?.contains(today.dayOfWeek.value),
                        workoutLogged = trainingLogged,
                        target = goals[dayIndex], planned = plan?.days?.get(dayIndex), loading = loading,
                        checkIn = log.firstOrNull { it.epochDay == epochDay }, meals = meals.filter { it.day == epochDay },
                        checkout = list.count { checked[it.food.key] == it.roundedGrams() } to list.size,
                        goalWeight = p.goalWeightKg, latestWeight = latestWeight, book = book,
                        onMeal = { openMeal(dayIndex, it) }, navigate = ::navigate
                    )
                    NutritionPage.PLAN -> PlanTab(
                        week, weekOffset, today, { weekOffset = it }, p.plan, goals, plan, loading, meals, book,
                        editPlan, { write { repo.rerollWeek(weekId, p.plan.seed) } }, { navigate(NutritionPage.SHOPPING) },
                        { navigate(NutritionPage.SAVED_WEEK) }, ::openMeal
                    )
                    NutritionPage.SHOPPING -> ShoppingTab(week, list, checked, loading) { item, done ->
                        write { repo.setShoppingChecked(weekId, item.food.key, item.roundedGrams(), done) }
                    }
                    NutritionPage.KITCHEN -> RecipesTab(
                        p.plan.excluded, book, epochDay,
                        onExclude = { recipe, exclude -> editPlan { settings ->
                            settings.copy(excluded = if (exclude) settings.excluded + recipe else settings.excluded - recipe)
                        } },
                        onLog = logMeal, onPrep = { navigate(NutritionPage.PREP) },
                        onSaveRecipe = { recipe -> write { repo.saveCustomRecipe(recipe) } },
                        onDeleteRecipe = { recipe -> write { repo.deleteCustomRecipe(recipe.key) } },
                        onAddToWeek = { recipe, day, slot, portions ->
                            write { repo.addToWeek(WeekBook.weekId(epochDay), day, slot, WeekEntry(recipe.key, portions)) }
                        }
                    )
                    NutritionPage.PREP -> PrepTab(week, today, plan, loading)
                    NutritionPage.JOURNAL -> JournalTab(log, meals, adaptive, epochDay, book, repo, ::write)
                    NutritionPage.COACH -> {
                        val id = WeekBook.weekId(epochDay)
                        val saved by remember(id) { repo.week(id) }.collectAsState(initial = WeekBook(id))
                        CoachTab(p, derived, log, epochDay, saved, book, repo) { edit ->
                            write { repo.updateProfile(edit) }
                        }
                    }
                    NutritionPage.SAVED_WEEK -> WeekTab(derived, epochDay, book, repo, initialMonday = weekId)
                    NutritionPage.GOALS -> GoalsTab(p, derived, adaptive, epochDay, latestWeight, { navigate(NutritionPage.TOOLS) }) { edited ->
                        write {
                            repo.updateProfile { current ->
                                edited.copy(
                                    plan = current.plan, coachMode = current.coachMode, phase = current.phase,
                                    phaseStartDay = current.phaseStartDay, coachAdjustKcal = current.coachAdjustKcal,
                                    lastCheckInDay = current.lastCheckInDay, targetBodyFatPercent = current.targetBodyFatPercent
                                )
                            }
                            snacks.showSnackbar(savedMessage)
                        }
                    }
                    NutritionPage.TOOLS -> ToolsTab(p)
                }
            }

            val slot = selectedSlot
            val day = selectedDay
            val meal = plan?.days?.getOrNull(day)?.meals?.getOrNull(slot)
            if (meal != null) {
                val date = weekId + day
                val logged = meals.firstOrNull { it.id == plannedMealId(date, slot) }
                val close = { selectedSlot = -1; swapping = false }
                fun saveMeal(recipe: Recipe, portions: Double) {
                    val edit = MealOverride(day, slot, p.plan.mealsPerDay, recipe.key, portions)
                    close()
                    write { repo.savePlanMeal(weekId, edit) }
                }

                if (swapping) {
                    val choices = remember(p.plan, meal.recipe.key, book) {
                        MealPlanner.eligible(book.all, p.plan).filter { it.type == meal.type && it.key != meal.recipe.key }
                    }
                    SwapRecipeDialog(choices, { saveMeal(it, meal.portions) }, { swapping = false })
                } else {
                    RecipeDialog(
                        recipe = logged?.let { book.resolve(it.itemKey) } ?: meal.recipe,
                        initialPortions = logged?.amount ?: meal.portions,
                        readOnly = logged != null,
                        logLabel = stringResource(R.string.nu_log_planned_meal),
                        onLog = if (logged == null && date <= epochDay) { portions ->
                            close()
                            write { repo.logMeal(date, meal.recipe, portions, slot) }
                        } else null,
                        onApplyPortions = if (logged == null) { portions -> saveMeal(meal.recipe, portions) } else null,
                        onSwap = if (logged == null) { { swapping = true } } else null,
                        onReset = if (logged == null && overrides.any { it.day == day && it.slot == slot && it.mealCount == p.plan.mealsPerDay }) {
                            {
                                close()
                                write { repo.resetPlanMeal(weekId, day, slot) }
                            }
                        } else null,
                        onDismiss = close
                    )
                }
            }
        }
    }
    if (ask) {
        AskMaximusSheet(
            services.chat,
            AskRequest(
                "Ernährung", Focus.NUTRITION,
                listOf("Bewerte meinen Check-in dieser Woche", "Wie verliere ich Fett, ohne Kraft zu verlieren?",
                    "Passt meine Proteinmenge zu meinen Zielen?", "Was esse ich vor und nach dem Training?",
                    "Wie esse ich am Wettkampftag?", "Lohnt sich Kreatin für mich?"),
                context = { services.chat.nutritionContext() }
            ),
            onDismiss = { ask = false },
            onOpenChat = { ask = false; onOpenChat() }
        )
    }
}
