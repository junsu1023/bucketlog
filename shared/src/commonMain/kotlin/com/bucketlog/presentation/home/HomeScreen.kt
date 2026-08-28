package com.bucketlog.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bucketlog.presentation.common.Hairline
import com.bucketlog.presentation.common.MonoMeta
import com.bucketlog.presentation.common.MountedPhoto
import com.bucketlog.presentation.common.PillChip
import com.bucketlog.presentation.common.photoFallbackBrush
import com.bucketlog.presentation.theme.BucketLogMotion
import com.bucketlog.presentation.theme.BucketLogSpacing
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.check_in_placeholder
import bucketlog.shared.generated.resources.check_in_save
import bucketlog.shared.generated.resources.empty_in_progress
import bucketlog.shared.generated.resources.empty_state_preset_hint
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.goal_bucket_someday
import bucketlog.shared.generated.resources.home_category_filter_all
import bucketlog.shared.generated.resources.home_sort_due_soon
import bucketlog.shared.generated.resources.home_sort_recent
import bucketlog.shared.generated.resources.home_summary_someday
import bucketlog.shared.generated.resources.home_summary_year
import bucketlog.shared.generated.resources.home_notifications_button
import bucketlog.shared.generated.resources.home_rollover_banner
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
import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.presentation.common.PresetGoal
import com.bucketlog.presentation.common.labelRes
import com.bucketlog.presentation.common.presetGoals
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onGoalClick: (String) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onRolloverClick: (Int) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    HomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        onGoalClick = onGoalClick,
        onNotificationsClick = onNotificationsClick,
        onRolloverClick = onRolloverClick,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onGoalClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onRolloverClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 재설계: Material TopAppBar 대신 에디토리얼 헤더 — 큰 세리프 연도 + 모노 요약 한 줄.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 34.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                YearDropdown(
                    selected = state.yearFilter,
                    thisYear = state.thisYear,
                    availableYears = state.availableYears,
                    onSelect = { onIntent(HomeIntent.SelectYearFilter(it)) },
                )
                Spacer(Modifier.size(10.dp))
                SummaryLine(state.yearFilter, state.summaryTotal, state.summaryCompleted)
            }
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = stringResource(Res.string.home_notifications_button),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Hairline(Modifier.padding(horizontal = 20.dp))

        state.throwback?.let { banner ->
            ThrowbackBand(banner = banner, onClick = { onGoalClick(banner.goalId) })
            Hairline(Modifier.padding(horizontal = 20.dp))
        }

        val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
        if (today.monthNumber == 12) {
            RolloverBand(year = today.year, onClick = { onRolloverClick(today.year) })
            Hairline(Modifier.padding(horizontal = 20.dp))
        }

        SortAndFilterRow(
            sortOption = state.sortOption,
            categoryFilter = state.categoryFilter,
            onSortSelect = { onIntent(HomeIntent.SelectSortOption(it)) },
            onCategorySelect = { onIntent(HomeIntent.SelectCategoryFilter(it)) },
        )

        if (state.overviews.isEmpty() && !state.isLoading) {
            EmptyState(existingTitles = state.existingTitles, onIntent = onIntent, modifier = Modifier.fillMaxSize())
        } else {
            var listVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { listVisible = true }
            AnimatedVisibility(
                visible = listVisible,
                enter = fadeIn(BucketLogMotion.enter()) +
                    slideInVertically(BucketLogMotion.enter()) { it / 12 },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 4.dp,
                        bottom = 112.dp,
                    ),
                ) {
                    itemsIndexed(
                        items = state.overviews,
                        key = { _, overview -> overview.goal.id },
                    ) { index, overview ->
                        GoalRow(
                            overview = overview,
                            draftText = state.checkInDrafts[overview.goal.id].orEmpty(),
                            onIntent = onIntent,
                            onClick = { onGoalClick(overview.goal.id) },
                            modifier = Modifier.animateItem(),
                        )
                        if (index < state.overviews.lastIndex) {
                            Hairline()
                        }
                    }
                }
            }
        }
    }

    if (state.hasError) {
        ErrorDialog(onDismiss = { onIntent(HomeIntent.DismissError) })
    }
}

/** G-12 연말 이월 유도 — 강조색 없이 담담한 괘선 행. */
@Composable
private fun RolloverBand(year: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.home_rollover_banner, year),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** H-07 작년 오늘 — 감정적인 한 줄은 Playfair 이탤릭으로. 달성률 언급 없음. */
@Composable
private fun ThrowbackBand(banner: ThrowbackBanner, onClick: () -> Unit) {
    val text = when (banner.kind) {
        ThrowbackKind.YEAR_AGO -> stringResource(Res.string.throwback_year_ago, banner.goalTitle)
        ThrowbackKind.MONTH_AGO -> stringResource(Res.string.throwback_month_ago, banner.goalTitle)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** G-11 정렬·필터. 정렬은 모노 텍스트 버튼, 카테고리는 캡슐 칩 한 줄. */
@Composable
private fun SortAndFilterRow(
    sortOption: HomeSortOption,
    categoryFilter: Category?,
    onSortSelect: (HomeSortOption) -> Unit,
    onCategorySelect: (Category?) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            Row(
                modifier = Modifier.clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                MonoMeta(
                    text = stringResource(
                        if (sortOption == HomeSortOption.RECENT) Res.string.home_sort_recent else Res.string.home_sort_due_soon,
                    ),
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(stringResource(Res.string.home_sort_recent)) },
                    onClick = { onSortSelect(HomeSortOption.RECENT); expanded = false },
                )
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(stringResource(Res.string.home_sort_due_soon)) },
                    onClick = { onSortSelect(HomeSortOption.DUE_SOON); expanded = false },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PillChip(
                label = stringResource(Res.string.home_category_filter_all),
                selected = categoryFilter == null,
                onClick = { onCategorySelect(null) },
            )
            Category.entries.forEach { category ->
                PillChip(
                    label = stringResource(category.labelRes()),
                    selected = categoryFilter == category,
                    onClick = { onCategorySelect(if (categoryFilter == category) null else category) },
                )
            }
        }
    }
}

/** 홈 헤더 타이틀 — 연도 선택 드롭다운("2026 ⌄"), 큰 세리프. */
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
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 34.sp, lineHeight = 38.sp),
            )
            Icon(
                Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(stringResource(Res.string.year_chip, thisYear)) },
                onClick = { onSelect(BucketYearFilter.Year(thisYear)); expanded = false },
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(stringResource(Res.string.year_dropdown_previous)) },
                onClick = { expanded = false; showPastYearDialog = true },
            )
            androidx.compose.material3.DropdownMenuItem(
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

/** H-02 요약 — "언젠가"는 완료 비율 대신 개수만. 모노 라벨로 담담하게. */
@Composable
private fun SummaryLine(filter: BucketYearFilter, total: Int, completed: Int) {
    val text = when (filter) {
        is BucketYearFilter.Year -> stringResource(Res.string.home_summary_year, filter.year, total, completed)
        BucketYearFilter.Someday -> stringResource(Res.string.home_summary_someday, total)
    }
    MonoMeta(text = text)
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
                PresetChip(preset = preset, existingTitles = existingTitles, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun PresetChip(preset: PresetGoal, existingTitles: Set<String>, onIntent: (HomeIntent) -> Unit) {
    val title = stringResource(preset.titleRes)
    if (title in existingTitles) return
    PillChip(label = title, selected = false, onClick = { onIntent(HomeIntent.AddPresetGoal(title, preset.category)) })
}

/**
 * 재설계 목표 행 — 카드(채움+그림자)를 걷어내고, 마운트에 끼운 사진 + 세리프 제목 + 모노 메타 +
 * 접힌 퀵 체크인으로 구성. 행 사이는 괘선으로만 나눈다.
 */
@Composable
private fun GoalRow(
    overview: GoalOverview,
    draftText: String,
    onIntent: (HomeIntent) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val goal = overview.goal
    val photo = overview.recentPhotoPaths.firstOrNull()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 22.dp),
    ) {
        MountedPhoto(
            model = photo,
            contentDescription = goal.title,
            fallbackBrush = photoFallbackBrush(goal.id.hashCode()),
            overlay = {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(BucketLogSpacing.ChipRadius))
                        .padding(horizontal = BucketLogSpacing.sm, vertical = BucketLogSpacing.xs),
                ) {
                    Text(
                        text = stringResource(goal.category.labelRes()),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }
            },
        )

        Text(
            text = goal.title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp, lineHeight = 26.sp),
            modifier = Modifier.padding(top = 14.dp),
        )

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(BucketLogSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            overview.lastRecordedAt?.let {
                MonoMeta(text = stringResource(Res.string.last_recorded, relativeDayLabel(it)))
            }
            if (goal.type == GoalType.REPEATABLE && goal.targetCount != null) {
                MonoMeta(
                    text = stringResource(Res.string.progress_count, overview.progressCount, goal.targetCount),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (goal.status == GoalStatus.IN_PROGRESS) {
            InlineCheckIn(
                goalId = goal.id,
                draftText = draftText,
                onIntent = onIntent,
            )
        }
    }
}

/** 퀵 체크인 — 평소엔 "한 줄 남기기" 고스트 행, 탭하면 입력이 축을 따라 내려온다(E-01, 3초 원칙). */
@Composable
private fun InlineCheckIn(
    goalId: String,
    draftText: String,
    onIntent: (HomeIntent) -> Unit,
) {
    var expanded by remember(goalId) { mutableStateOf(false) }
    Column(modifier = Modifier.padding(top = 14.dp)) {
        AnimatedVisibility(
            visible = !expanded,
            exit = fadeOut(BucketLogMotion.exit()),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f),
                        RoundedCornerShape(12.dp),
                    )
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.Send,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.check_in_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(BucketLogMotion.enter()) + expandVertically(BucketLogMotion.enter()),
            exit = fadeOut(BucketLogMotion.exit()) + shrinkVertically(BucketLogMotion.exit()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draftText,
                    onValueChange = { onIntent(HomeIntent.CheckInTextChanged(goalId, it)) },
                    placeholder = { Text(stringResource(Res.string.check_in_placeholder)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                IconButton(onClick = {
                    onIntent(HomeIntent.SubmitCheckIn(goalId))
                    expanded = false
                }) {
                    Icon(
                        Icons.Outlined.Send,
                        contentDescription = stringResource(Res.string.check_in_save),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
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
