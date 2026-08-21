package com.bucketlog

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.bucketlog.platform.AppBackHandler
import com.bucketlog.presentation.addgoal.AddGoalScreen
import com.bucketlog.presentation.addgoal.AddGoalViewModel
import com.bucketlog.presentation.archive.ArchiveScreen
import com.bucketlog.presentation.goaldetail.GoalDetailScreen
import com.bucketlog.presentation.home.HomeScreen
import com.bucketlog.presentation.onboarding.OnboardingScreen
import com.bucketlog.presentation.common.MonthKey
import com.bucketlog.presentation.onboarding.OnboardingViewModel
import com.bucketlog.presentation.settings.SettingsScreen
import com.bucketlog.presentation.theme.BucketLogTheme
import com.bucketlog.presentation.theme.ThemeMode
import com.bucketlog.presentation.theme.ThemeModeStore
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private sealed interface Screen {
    /** 최초 실행 여부(목표 0개인지) 판정이 끝나기 전까지의 대기 상태 — 빈 테마 배경만 보인다. */
    data object Loading : Screen
    data object Onboarding : Screen
    data object Home : Screen
    /** [editingGoalId]가 있으면 수정 모드(G-05). [returnTo]는 저장/취소 후 돌아갈 화면. */
    data class AddGoal(val editingGoalId: String? = null, val returnTo: Screen = Home) : Screen
    /** [targetMonth]가 있으면 N-01 딥링크로 진입한 것 — "이번 달" 탭이 그 달을 보여준다. */
    data class Archive(val targetMonth: MonthKey? = null) : Screen
    data object Settings : Screen
    /** [from]으로 돌아가야 뒤로가기가 진입 경로(홈/보관함)에 맞게 동작한다.
     *  [focusCheckIn]은 스마트 넛지 딥링크(focus=checkin)로 들어왔을 때만 true. */
    data class GoalDetail(val goalId: String, val from: Screen, val focusCheckIn: Boolean = false) : Screen
}

@Composable
@Preview
fun App() {
    val themeModeStore: ThemeModeStore = koinInject()
    val themeMode by themeModeStore.mode.collectAsState()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    BucketLogTheme(darkTheme = darkTheme) {
        var screen by remember { mutableStateOf<Screen>(Screen.Loading) }
        val addGoalViewModel: AddGoalViewModel = koinViewModel()
        val onboardingViewModel: OnboardingViewModel = koinViewModel()

        val shouldShowOnboarding by onboardingViewModel.shouldShowOnboarding.collectAsState()
        LaunchedEffect(shouldShowOnboarding) {
            if (screen == Screen.Loading && shouldShowOnboarding != null) {
                screen = if (shouldShowOnboarding == true) Screen.Onboarding else Screen.Home
            }
        }

        val pendingDeepLink by DeepLinkHolder.pending.collectAsState()
        LaunchedEffect(pendingDeepLink) {
            val uri = pendingDeepLink ?: return@LaunchedEffect
            parseGoalDeepLink(uri)?.let { (goalId, focusCheckIn) ->
                screen = Screen.GoalDetail(goalId, from = Screen.Home, focusCheckIn = focusCheckIn)
            }
            parseArchiveMonthDeepLink(uri)?.let { month ->
                screen = Screen.Archive(targetMonth = month)
            }
            DeepLinkHolder.consume()
        }

        // 상세/추가/보관함/온보딩 화면에서는 시스템 뒤로가기가 앱 종료가 아니라 이전 화면으로 돌아가야 한다.
        AppBackHandler(enabled = screen != Screen.Home && screen != Screen.Loading) {
            screen = when (val s = screen) {
                is Screen.GoalDetail -> s.from
                is Screen.AddGoal -> s.returnTo
                else -> Screen.Home
            }
        }

        when (val current = screen) {
            Screen.Loading -> Unit
            Screen.Onboarding -> OnboardingScreen(
                viewModel = onboardingViewModel,
                onCustomInput = {
                    addGoalViewModel.resetForm()
                    screen = Screen.AddGoal()
                },
                onDone = { screen = Screen.Home },
            )
            Screen.Home -> HomeScreen(
                viewModel = koinViewModel(),
                onAddGoalClick = {
                    // koinViewModel()이 화면 재진입마다 같은 인스턴스를 재사용하므로
                    // 진입 직전에 폼을 초기화한다 (AddGoalViewModel.resetForm 참고).
                    addGoalViewModel.resetForm()
                    screen = Screen.AddGoal()
                },
                onGoalClick = { goalId -> screen = Screen.GoalDetail(goalId, from = Screen.Home) },
                onArchiveClick = { screen = Screen.Archive() },
                onSettingsClick = { screen = Screen.Settings },
            )
            is Screen.AddGoal -> AddGoalScreen(
                viewModel = addGoalViewModel,
                onSaved = { screen = current.returnTo },
                onCancel = { screen = current.returnTo },
            )
            is Screen.Archive -> ArchiveScreen(
                viewModel = koinViewModel(),
                onBack = { screen = Screen.Home },
                onGoalClick = { goalId -> screen = Screen.GoalDetail(goalId, from = current) },
                targetMonth = current.targetMonth,
            )
            Screen.Settings -> SettingsScreen(
                viewModel = koinViewModel(),
                onBack = { screen = Screen.Home },
            )
            is Screen.GoalDetail -> GoalDetailScreen(
                // goalId가 바뀌면(다른 목표 상세로 재진입) key로 새 ViewModel 인스턴스를 만든다.
                viewModel = koinViewModel(key = current.goalId) { parametersOf(current.goalId) },
                onBack = { screen = current.from },
                onEditClick = { goal ->
                    addGoalViewModel.loadForEdit(goal)
                    screen = Screen.AddGoal(editingGoalId = goal.id, returnTo = current)
                },
                focusCheckIn = current.focusCheckIn,
            )
        }
    }
}
