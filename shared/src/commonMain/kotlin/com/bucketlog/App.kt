package com.bucketlog

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bucketlog.presentation.theme.BucketLogMotion
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.add_goal_fab
import bucketlog.shared.generated.resources.archive_nav_button
import bucketlog.shared.generated.resources.exit_double_back_message
import bucketlog.shared.generated.resources.home_nav_button
import bucketlog.shared.generated.resources.search_nav_button
import bucketlog.shared.generated.resources.settings_nav_button
import com.bucketlog.platform.AppBackHandler
import com.bucketlog.platform.ExitOnDoubleBackHandler
import com.bucketlog.presentation.addgoal.AddGoalScreen
import com.bucketlog.presentation.addgoal.AddGoalViewModel
import com.bucketlog.presentation.archive.ArchiveScreen
import com.bucketlog.presentation.common.MonthKey
import com.bucketlog.presentation.goaldetail.GoalDetailScreen
import com.bucketlog.presentation.home.HomeScreen
import com.bucketlog.presentation.onboarding.OnboardingScreen
import com.bucketlog.presentation.onboarding.OnboardingViewModel
import com.bucketlog.presentation.rollover.RolloverScreen
import com.bucketlog.presentation.search.SearchScreen
import com.bucketlog.presentation.settings.SettingsScreen
import com.bucketlog.presentation.theme.BucketLogTheme
import com.bucketlog.presentation.theme.ThemeMode
import com.bucketlog.presentation.theme.ThemeModeStore
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private sealed interface Screen {
    /** 최초 실행 여부(목표 0개인지) 판정이 끝나기 전까지의 대기 상태 — 빈 테마 배경만 보인다. */
    data object Loading : Screen
    data object Onboarding : Screen
    data object Home : Screen
    /** 하단 탭 중 하나 — MVP-SCOPE.md엔 없던 기능, 디자인 리뉴얼 때 추가(목표 제목 검색). */
    data object Search : Screen
    /** [editingGoalId]가 있으면 수정 모드(G-05). [returnTo]는 저장/취소 후 돌아갈 화면. */
    data class AddGoal(val editingGoalId: String? = null, val returnTo: Screen = Home) : Screen
    /** [targetMonth]가 있으면 N-01 딥링크로 진입한 것 — "이번 달" 탭이 그 달을 보여준다. */
    data class Archive(val targetMonth: MonthKey? = null) : Screen
    data object Settings : Screen
    /** [from]으로 돌아가야 뒤로가기가 진입 경로(홈/보관함/검색)에 맞게 동작한다.
     *  [focusCheckIn]은 스마트 넛지 딥링크(focus=checkin)로 들어왔을 때만 true. */
    data class GoalDetail(val goalId: String, val from: Screen, val focusCheckIn: Boolean = false) : Screen
    /** G-12 연말 이월 — 설정 메뉴 / 12월 홈 배너 / N-05 알림 딥링크(bucketlog://retrospect/{year}) 세 경로로 들어온다. */
    data class Rollover(val year: Int, val from: Screen = Home) : Screen
}

/** 홈/보관함/검색/설정 — 하단 탭으로 묶이는 화면들. 이 화면들 사이에는 "뒤로가기"가 없다. */
private fun isBottomTab(screen: Screen) =
    screen is Screen.Home || screen is Screen.Archive || screen is Screen.Search || screen is Screen.Settings

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
        // G-12 연말 이월 진입점(설정 메뉴/12월 홈 배너)에서 대상 연도로 쓴다.
        val thisYear = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).year }

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
            parseRolloverDeepLink(uri)?.let { year ->
                screen = Screen.Rollover(year)
            }
            DeepLinkHolder.consume()
        }

        // 하단 탭 화면(홈/보관함/검색/설정)에서는 시스템 뒤로가기가 앱 종료로 이어져야 한다 —
        // 서로 형제 화면이라 "돌아갈 곳"이 없다. 상세/추가/온보딩에서만 이전 화면으로 되돌아간다.
        AppBackHandler(enabled = !isBottomTab(screen) && screen != Screen.Loading) {
            screen = when (val s = screen) {
                is Screen.GoalDetail -> s.from
                is Screen.AddGoal -> s.returnTo
                is Screen.Rollover -> s.from
                else -> Screen.Home
            }
        }
        // 실수로 앱이 바로 꺼지는 걸 막기 위해 하단 탭에서는 뒤로가기를 두 번 눌러야 종료된다.
        ExitOnDoubleBackHandler(enabled = isBottomTab(screen), message = stringResource(Res.string.exit_double_back_message))

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
            is Screen.AddGoal -> AddGoalScreen(
                viewModel = addGoalViewModel,
                onSaved = { screen = current.returnTo },
                onCancel = { screen = current.returnTo },
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
            is Screen.Rollover -> RolloverScreen(
                viewModel = koinViewModel(key = "rollover_${current.year}") { parametersOf(current.year) },
                onBack = { screen = current.from },
            )
            else -> Scaffold(
                bottomBar = {
                    BottomNav(
                        current = current,
                        onSelect = { tab -> screen = tab },
                        onAddGoal = {
                            // koinViewModel()이 화면 재진입마다 같은 인스턴스를 재사용하므로
                            // 진입 직전에 폼을 초기화한다 (AddGoalViewModel.resetForm 참고).
                            addGoalViewModel.resetForm()
                            screen = Screen.AddGoal(returnTo = current)
                        },
                    )
                },
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (current) {
                        Screen.Home -> HomeScreen(
                            viewModel = koinViewModel(),
                            onGoalClick = { goalId -> screen = Screen.GoalDetail(goalId, from = Screen.Home) },
                            onNotificationsClick = { screen = Screen.Settings },
                            onRolloverClick = { year -> screen = Screen.Rollover(year, from = Screen.Home) },
                        )
                        is Screen.Archive -> ArchiveScreen(
                            viewModel = koinViewModel(),
                            onGoalClick = { goalId -> screen = Screen.GoalDetail(goalId, from = current) },
                            targetMonth = current.targetMonth,
                        )
                        Screen.Search -> SearchScreen(
                            viewModel = koinViewModel(),
                            onGoalClick = { goalId -> screen = Screen.GoalDetail(goalId, from = Screen.Search) },
                        )
                        Screen.Settings -> SettingsScreen(
                            viewModel = koinViewModel(),
                            onRolloverClick = { screen = Screen.Rollover(thisYear, from = Screen.Settings) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * 재설계 하단 내비 — Material NavigationBar 대신 화면 위에 떠 있는 알약형 바.
 * 선택 탭 뒤로 gooey 블롭이 스프링으로 늘어졌다 뭉치며 이동하고, 가운데 "+"는 살짝 띄운 강조색 원.
 * 이 앱은 탭이 서로 형제라(뒤로가기 없음) 인디케이터가 위치를 알려주는 유일한 단서다.
 */
@Composable
private fun BottomNav(current: Screen, onSelect: (Screen) -> Unit, onAddGoal: () -> Unit) {
    // 슬롯 순서: 홈 · 보관함 · (+) · 검색 · 설정 → 블롭은 탭 슬롯(0,1,3,4)에만 뜬다.
    val blobSlot = when {
        current is Screen.Home -> 0
        current is Screen.Archive -> 1
        current is Screen.Search -> 3
        current is Screen.Settings -> 4
        else -> 0
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .height(62.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)),
                    RoundedCornerShape(50),
                ),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val slotWidth: Dp = maxWidth / 5
                val blobSize = 44.dp
                val blobX by animateDpAsState(
                    targetValue = slotWidth * blobSlot + (slotWidth - blobSize) / 2,
                    animationSpec = BucketLogMotion.indicator(),
                    label = "navBlob",
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = blobX)
                        .size(blobSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    NavSlot(slotWidth, Icons.Outlined.Home, current is Screen.Home, Res.string.home_nav_button) { onSelect(Screen.Home) }
                    NavSlot(slotWidth, Icons.Outlined.Folder, current is Screen.Archive, Res.string.archive_nav_button) { onSelect(Screen.Archive()) }
                    AddSlot(slotWidth, onAddGoal)
                    NavSlot(slotWidth, Icons.Outlined.Search, current is Screen.Search, Res.string.search_nav_button) { onSelect(Screen.Search) }
                    NavSlot(slotWidth, Icons.Outlined.Settings, current is Screen.Settings, Res.string.settings_nav_button) { onSelect(Screen.Settings) }
                }
            }
        }
    }
}

@Composable
private fun NavSlot(
    width: Dp,
    icon: ImageVector,
    selected: Boolean,
    labelRes: StringResource,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "navTint",
    )
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = stringResource(labelRes), tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun AddSlot(width: Dp, onClick: () -> Unit) {
    Box(modifier = Modifier.width(width).fillMaxHeight(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = stringResource(Res.string.add_goal_fab),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
