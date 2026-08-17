package com.bucketlog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.bucketlog.platform.AppBackHandler
import com.bucketlog.presentation.addgoal.AddGoalScreen
import com.bucketlog.presentation.addgoal.AddGoalViewModel
import com.bucketlog.presentation.goaldetail.GoalDetailScreen
import com.bucketlog.presentation.home.HomeScreen
import com.bucketlog.presentation.theme.BucketLogTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private sealed interface Screen {
    data object Home : Screen
    data object AddGoal : Screen
    data class GoalDetail(val goalId: String) : Screen
}

@Composable
@Preview
fun App() {
    BucketLogTheme {
        var screen by remember { mutableStateOf<Screen>(Screen.Home) }
        val addGoalViewModel: AddGoalViewModel = koinViewModel()

        // 상세/추가 화면에서는 시스템 뒤로가기가 앱 종료가 아니라 홈으로 돌아가야 한다.
        AppBackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

        when (val current = screen) {
            Screen.Home -> HomeScreen(
                viewModel = koinViewModel(),
                onAddGoalClick = {
                    // koinViewModel()이 화면 재진입마다 같은 인스턴스를 재사용하므로
                    // 진입 직전에 폼을 초기화한다 (AddGoalViewModel.resetForm 참고).
                    addGoalViewModel.resetForm()
                    screen = Screen.AddGoal
                },
                onGoalClick = { goalId -> screen = Screen.GoalDetail(goalId) },
            )
            Screen.AddGoal -> AddGoalScreen(
                viewModel = addGoalViewModel,
                onSaved = { screen = Screen.Home },
                onCancel = { screen = Screen.Home },
            )
            is Screen.GoalDetail -> GoalDetailScreen(
                // goalId가 바뀌면(다른 목표 상세로 재진입) key로 새 ViewModel 인스턴스를 만든다.
                viewModel = koinViewModel(key = current.goalId) { parametersOf(current.goalId) },
                onBack = { screen = Screen.Home },
            )
        }
    }
}
