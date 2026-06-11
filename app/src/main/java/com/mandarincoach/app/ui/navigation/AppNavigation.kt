package com.mandarincoach.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mandarincoach.app.data.model.ProficiencyLevel
import com.mandarincoach.app.ui.conversation.ConversationScreen
import com.mandarincoach.app.ui.home.HomeScreen
import com.mandarincoach.app.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val CONVERSATION = "conversation/{level}?scenario={scenario}"
    const val SETTINGS = "settings"
    fun conversation(level: ProficiencyLevel, scenarioId: String? = null) =
        "conversation/${level.name}" + (scenarioId?.let { "?scenario=$it" } ?: "")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onLevelSelected = { level ->
                    navController.navigate(Routes.conversation(level))
                },
                onScenarioSelected = { level, scenarioId ->
                    navController.navigate(Routes.conversation(level, scenarioId))
                },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = Routes.CONVERSATION,
            arguments = listOf(
                navArgument("level") { type = NavType.StringType },
                navArgument("scenario") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStack ->
            val levelName = backStack.arguments?.getString("level") ?: ProficiencyLevel.BEGINNER.name
            val level = runCatching { ProficiencyLevel.valueOf(levelName) }
                .getOrDefault(ProficiencyLevel.BEGINNER)
            ConversationScreen(
                level = level,
                scenarioId = backStack.arguments?.getString("scenario"),
                onNavigateBack = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
