package com.bucketlog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.bucketlog.presentation.addgoal.AddGoalScreen
import com.bucketlog.presentation.home.HomeScreen
import org.koin.compose.viewmodel.koinViewModel

private sealed interface Screen {
    data object Home : Screen
    data object AddGoal : Screen
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf<Screen>(Screen.Home) }

        when (screen) {
            Screen.Home -> HomeScreen(
                viewModel = koinViewModel(),
                onAddGoalClick = { screen = Screen.AddGoal },
            )
            Screen.AddGoal -> AddGoalScreen(
                viewModel = koinViewModel(),
                onSaved = { screen = Screen.Home },
                onCancel = { screen = Screen.Home },
            )
        }
    }
}