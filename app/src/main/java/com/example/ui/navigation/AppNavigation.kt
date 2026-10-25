package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.BackupRestoreScreen
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.ConfusableWordsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ListeningLabScreen
import com.example.ui.screens.PageAuditScreen
import com.example.ui.screens.PracticeModesScreen
import com.example.ui.screens.SentenceCoachScreen
import com.example.ui.screens.StudyPlannerScreen
import com.example.ui.screens.StudySessionScreen
import com.example.ui.screens.ToeflPracticeScreen
import com.example.ui.screens.WordDetailScreen
import com.example.ui.viewmodel.VocabViewModel

object Destinations {
    const val DASHBOARD = "dashboard"
    const val BROWSE = "browse"
    const val WORD_DETAIL = "word_detail"
    const val PAGE_AUDIT = "page_audit"
    const val PRACTICE_MODES = "practice_modes"
    const val STUDY_SESSION = "study_session"
    const val TOEFL_PRACTICE = "toefl_practice"
    const val LISTENING_LAB = "listening_lab"
    const val STUDY_PLANNER = "study_planner"
    const val SENTENCE_COACH = "sentence_coach"
    const val CONFUSABLE_WORDS = "confusable_words"
    const val BACKUP_RESTORE = "backup_restore"
}

@Composable
fun AppNavigation(
    viewModel: VocabViewModel,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val selectedEntry by viewModel.selectedEntry.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Destinations.DASHBOARD,
        modifier = modifier
    ) {
        composable(Destinations.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToBrowse = { navController.navigate(Destinations.BROWSE) },
                onNavigateToAudit = { navController.navigate(Destinations.PAGE_AUDIT) },
                onNavigateToPractice = { navController.navigate(Destinations.PRACTICE_MODES) },
                onStartDailySession = {
                    viewModel.startDailySession()
                    navController.navigate(Destinations.STUDY_SESSION)
                },
                onNavigateToToeflPractice = { navController.navigate(Destinations.TOEFL_PRACTICE) },
                onNavigateToListeningLab = { navController.navigate(Destinations.LISTENING_LAB) },
                onNavigateToStudyPlanner = { navController.navigate(Destinations.STUDY_PLANNER) },
                onNavigateToSentenceCoach = { navController.navigate(Destinations.SENTENCE_COACH) },
                onNavigateToConfusables = { navController.navigate(Destinations.CONFUSABLE_WORDS) },
                onNavigateToBackupRestore = { navController.navigate(Destinations.BACKUP_RESTORE) }
            )
        }

        composable(Destinations.BROWSE) {
            BrowseScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onWordSelected = {
                    viewModel.selectEntry(it)
                    navController.navigate(Destinations.WORD_DETAIL)
                }
            )
        }

        composable(Destinations.WORD_DETAIL) {
            selectedEntry?.let { entry ->
                WordDetailScreen(
                    entry = entry,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onStartPracticeForWord = {
                        viewModel.startSingleWordPractice(it)
                        navController.navigate(Destinations.STUDY_SESSION)
                    }
                )
            } ?: run {
                navController.popBackStack()
            }
        }

        composable(Destinations.PAGE_AUDIT) {
            PageAuditScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPageWords = {
                    navController.navigate(Destinations.BROWSE)
                },
                onWordSelected = {
                    viewModel.selectEntry(it)
                    navController.navigate(Destinations.WORD_DETAIL)
                }
            )
        }

        composable(Destinations.PRACTICE_MODES) {
            PracticeModesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onStartSession = {
                    navController.navigate(Destinations.STUDY_SESSION)
                }
            )
        }

        composable(Destinations.STUDY_SESSION) {
            StudySessionScreen(
                viewModel = viewModel,
                onFinish = {
                    navController.popBackStack()
                }
            )
        }

        composable(Destinations.TOEFL_PRACTICE) {
            ToeflPracticeScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.LISTENING_LAB) {
            ListeningLabScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.STUDY_PLANNER) {
            StudyPlannerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.SENTENCE_COACH) {
            SentenceCoachScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.CONFUSABLE_WORDS) {
            ConfusableWordsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.BACKUP_RESTORE) {
            BackupRestoreScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
