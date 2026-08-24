package com.bucketlog.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bucketlog.presentation.theme.BucketLogSpacing
import com.bucketlog.presentation.theme.LocalExtraColors
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.check_in_placeholder
import bucketlog.shared.generated.resources.check_in_save
import bucketlog.shared.generated.resources.empty_in_progress
import bucketlog.shared.generated.resources.empty_state_preset_hint
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.goal_bucket_someday
import bucketlog.shared.generated.resources.home_summary_someday
import bucketlog.shared.generated.resources.home_summary_year
import bucketlog.shared.generated.resources.home_notifications_button
import bucketlog.shared.generated.resources.last_recorded
import bucketlog.shared.generated.resources.progress_count
import bucketlog.shared.generated.resources.relative_days_ago
import bucketlog.shared.generated.resources.relative_today
import bucketlog.shared.generated.resources.relative_yesterday
import bucketlog.shared.generated.resources.throwback_month_ago
import bucketlog.shared.generated.resources.throwback_year_ago
import bucketlog.shared.generated.resources.year_chip
import bucketlog.shared.generated.resources.year_dropdown_previous
import bucketlog.shared.generated.resources.year_picker_dialog_title
import coil3.compose.AsyncImage
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.presentation.common.PresetGoal
import com.bucketlog.presentation.common.labelRes
import com.bucketlog.presentation.common.presetGoals
import com.bucketlog.presentation.theme.MonoLabel
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onGoalClick: (String) -> Unit,
    onNotificationsClick: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    HomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        onGoalClick = onGoalClick,
        onNotificationsClick = onNotificationsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onGoalClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
) {
    Scaffold(
        // 목표 추가는 하단 내비게이션의 가운데 "+"가 맡는다(App.kt의 BottomNav 참고).
        topBar = {
            TopAppBar(
                title = {
                    YearDropdown(
                        selected = state.yearFilter,
                        thisYear = state.thisYear,
                        availableYears = state.availableYears,
                        onSelect = { onIntent(HomeIntent.SelectYearFilter(it)) },
                    )
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Outlined.Notifications, contentDescription = stringResource(Res.string.home_notifications_button))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            state.throwback?.let { banner ->
                ThrowbackBannerCard(banner = banner, onClick = { onGoalClick(banner.goalId) })
            }
            SummaryHeader(state.yearFilter, state.summaryTotal, state.summaryCompleted)

            if (state.overviews.isEmpty() && !state.isLoading) {
                EmptyState(existingTitles = state.existingTitles, onIntent = onIntent, modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    // 마지막 카드가 하단 내비게이션 바로 아래 바짝 붙지 않도록 아래쪽만 더 넉넉히 둔다.
                    contentPadding = PaddingValues(
                        start = BucketLogSpacing.lg,
                        end = BucketLogSpacing.lg,
                        top = BucketLogSpacing.lg,
                        bottom = BucketLogSpacing.xxl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(BucketLogSpacing.md),
                ) {
                    items(state.overviews, key = { it.goal.id }) { overview ->
                        GoalCard(
                            overview = overview,
                            draftText = state.checkInDrafts[overview.goal.id].orEmpty(),
                            onIntent = onIntent,
                            onClick = { onGoalClick(overview.goal.id) },
                        )
                    }
                }
            }
        }
    }

    if (state.hasError) {
        ErrorDialog(onDismiss = { onIntent(HomeIntent.DismissError) })
    }
}

/** 홈 상단바 타이틀 자리 — 연도 선택을 드롭다운 하나로 압축한다("2026년 ⌄"). */
@Composable
private fun YearDropdown(
    selected: BucketYearFilter,
    thisYear: Int,
    availableYears: List<Int>,
    onSelect: (BucketYearFilter) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showPastYearDialog by remember { mutableStateOf(false) }
    val label = when (selected) {
        is BucketYearFilter.Year -> stringResource(Res.string.year_chip, selected.year)
        BucketYearFilter.Someday -> stringResource(Res.string.goal_bucket_someday)
    }
    Box {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.headlineSmall)
            Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
        }
        // 드롭다운엔 현재 연도/이전/언젠가 3개만 두고, "이전"을 고르면 다이얼로그에서 특정 연도를
        // 고르게 한다 — 연도가 늘어날수록 드롭다운이 한없이 길어지는 걸 막는다.
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.year_chip, thisYear)) },
                onClick = { onSelect(BucketYearFilter.Year(thisYear)); expanded = false },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.year_dropdown_previous)) },
                onClick = { expanded = false; showPastYearDialog = true },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.goal_bucket_someday)) },
                onClick = { onSelect(BucketYearFilter.Someday); expanded = false },
            )
        }
    }

    if (showPastYearDialog) {
        AlertDialog(
            onDismissRequest = { showPastYearDialog = false },
            title = { Text(stringResource(Res.string.year_picker_dialog_title)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    availableYears.filter { it != thisYear }.forEach { year ->
                        TextButton(
                            onClick = {
                                onSelect(BucketYearFilter.Year(year))
                                showPastYearDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = stringResource(Res.string.year_chip, year),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPastYearDialog = false }) { Text(stringResource(Res.string.cancel)) }
            },
        )
    }
}

/** H-07 작년 오늘 — 유저가 아무것도 안 해도 앱이 먼저 과거 기록을 꺼내 보여준다. 달성률 언급 없음. */
@Composable
private fun ThrowbackBannerCard(banner: ThrowbackBanner, onClick: () -> Unit) {
    val text = when (banner.kind) {
        ThrowbackKind.YEAR_AGO -> stringResource(Res.string.throwback_year_ago, banner.goalTitle)
        ThrowbackKind.MONTH_AGO -> stringResource(Res.string.throwback_month_ago, banner.goalTitle)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(BucketLogSpacing.CardRadius),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BucketLogSpacing.lg, vertical = BucketLogSpacing.sm)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(BucketLogSpacing.lg),
        )
    }
}

/** H-02 요약 헤더. "언젠가"는 완료 비율 대신 개수만 보여준다(연도처럼 마감 개념이 없어서). */
@Composable
private fun SummaryHeader(filter: BucketYearFilter, total: Int, completed: Int) {
    val text = when (filter) {
        is BucketYearFilter.Year -> stringResource(Res.string.home_summary_year, filter.year, total, completed)
        BucketYearFilter.Someday -> stringResource(Res.string.home_summary_someday, total)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.merge(MonoLabel()),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = BucketLogSpacing.lg, vertical = BucketLogSpacing.sm),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyState(existingTitles: Set<String>, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(32.dp)) {
        Text(
            text = stringResource(Res.string.empty_in_progress),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.empty_state_preset_hint),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presetGoals.distinctBy { it.category }.forEach { preset ->
                // 이미 추가한 프리셋은 다시 제안하지 않는다 — 중복 목표 생성 방지.
                PresetChip(preset = preset, existingTitles = existingTitles, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun PresetChip(preset: PresetGoal, existingTitles: Set<String>, onIntent: (HomeIntent) -> Unit) {
    val title = stringResource(preset.titleRes)
    if (title in existingTitles) return
    AssistChip(
        onClick = { onIntent(HomeIntent.AddPresetGoal(title, preset.category)) },
        label = { Text(title) },
        shape = RoundedCornerShape(BucketLogSpacing.ChipRadius),
        border = null,
        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalCard(
    overview: GoalOverview,
    draftText: String,
    onIntent: (HomeIntent) -> Unit,
    onClick: () -> Unit,
) {
    val goal = overview.goal
    val hasPhoto = overview.recentPhotoPaths.isNotEmpty()
    Card(
        colors = CardDefaults.cardColors(containerColor = LocalExtraColors.current.goalCard),
        shape = RoundedCornerShape(BucketLogSpacing.CardRadius),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        if (hasPhoto) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (overview.recentPhotoPaths.size == 1) {
                    AsyncImage(
                        model = overview.recentPhotoPaths.first(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(overview.recentPhotoPaths) { path ->
                            AsyncImage(
                                model = path,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
                // 사진 위에 얹는 카테고리 배지 — 사진이 없을 땐 아래(제목 위)에 인라인으로 대신 보여준다.
                CategoryBadge(
                    goal = goal,
                    modifier = Modifier.align(Alignment.TopStart).padding(BucketLogSpacing.sm),
                )
            }
        }
        Column(modifier = Modifier.padding(BucketLogSpacing.lg)) {
            if (!hasPhoto) {
                CategoryBadge(goal = goal, modifier = Modifier.padding(bottom = BucketLogSpacing.xs))
            }
            Text(text = goal.title, style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.padding(top = BucketLogSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(BucketLogSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                overview.lastRecordedAt?.let {
                    Text(
                        text = stringResource(Res.string.last_recorded, relativeDayLabel(it)),
                        style = MaterialTheme.typography.bodySmall.merge(MonoLabel()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (goal.type == GoalType.REPEATABLE && goal.targetCount != null) {
                    Text(
                        text = stringResource(Res.string.progress_count, overview.progressCount, goal.targetCount),
                        style = MaterialTheme.typography.bodySmall.merge(MonoLabel()),
                    )
                }
            }

            if (goal.status == GoalStatus.IN_PROGRESS) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { onIntent(HomeIntent.CheckInTextChanged(goal.id, it)) },
                        placeholder = { Text(stringResource(Res.string.check_in_placeholder)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    IconButton(onClick = { onIntent(HomeIntent.SubmitCheckIn(goal.id)) }) {
                        Icon(Icons.Outlined.Edit, contentDescription = stringResource(Res.string.check_in_save))
                    }
                }
            }
        }
    }
}

/** 카테고리 배지 — 사진이 있으면 사진 위에 얹는 작은 캡슐, 없으면 제목 위 인라인 텍스트로 대신 쓴다. */
@Composable
private fun CategoryBadge(goal: Goal, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(BucketLogSpacing.ChipRadius))
            .padding(horizontal = BucketLogSpacing.sm, vertical = BucketLogSpacing.xs),
    ) {
        Text(
            text = stringResource(goal.category.labelRes()),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun relativeDayLabel(instant: Instant): String {
    val zone = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(zone).date
    val recordedDay = instant.toLocalDateTime(zone).date
    val days = (today.toEpochDays() - recordedDay.toEpochDays()).toInt()
    return when {
        days <= 0 -> stringResource(Res.string.relative_today)
        days == 1 -> stringResource(Res.string.relative_yesterday)
        else -> stringResource(Res.string.relative_days_ago, days)
    }
}

@Composable
private fun ErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(Res.string.error_generic)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}
