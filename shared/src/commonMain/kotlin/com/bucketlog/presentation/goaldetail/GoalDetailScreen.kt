package com.bucketlog.presentation.goaldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.action_add_progress
import bucketlog.shared.generated.resources.action_archive
import bucketlog.shared.generated.resources.action_complete
import bucketlog.shared.generated.resources.action_delete
import bucketlog.shared.generated.resources.action_edit
import bucketlog.shared.generated.resources.action_restore
import bucketlog.shared.generated.resources.action_share
import bucketlog.shared.generated.resources.archive_dialog_body
import bucketlog.shared.generated.resources.archive_dialog_title
import bucketlog.shared.generated.resources.archive_reason_placeholder
import bucketlog.shared.generated.resources.back
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.check_in_placeholder
import bucketlog.shared.generated.resources.check_in_save
import bucketlog.shared.generated.resources.delete_dialog_body
import bucketlog.shared.generated.resources.delete_dialog_title
import bucketlog.shared.generated.resources.delete_entry_dialog_body
import bucketlog.shared.generated.resources.delete_entry_dialog_title
import bucketlog.shared.generated.resources.edit_entry_dialog_title
import bucketlog.shared.generated.resources.entry_count
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.photo_viewer_close
import bucketlog.shared.generated.resources.photo_viewer_count
import bucketlog.shared.generated.resources.progress_count
import bucketlog.shared.generated.resources.progress_dialog_title
import bucketlog.shared.generated.resources.progress_increment_count
import bucketlog.shared.generated.resources.progress_memo_placeholder
import bucketlog.shared.generated.resources.progress_save
import bucketlog.shared.generated.resources.progress_target_reached
import bucketlog.shared.generated.resources.progress_target_reached_action
import bucketlog.shared.generated.resources.relative_days_ago
import bucketlog.shared.generated.resources.relative_today
import bucketlog.shared.generated.resources.relative_yesterday
import bucketlog.shared.generated.resources.retrospect_another_question
import bucketlog.shared.generated.resources.retrospect_label
import bucketlog.shared.generated.resources.save
import bucketlog.shared.generated.resources.timeline_empty
import bucketlog.shared.generated.resources.timeline_goal_created
import coil3.compose.AsyncImage
import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.platform.AppBackHandler
import com.bucketlog.platform.rememberCameraCapture
import com.bucketlog.platform.rememberPhotoPicker
import com.bucketlog.presentation.common.PhotoAttachRow
import com.bucketlog.presentation.common.randomRetrospectQuestion
import com.bucketlog.presentation.theme.MonoLabel
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    viewModel: GoalDetailViewModel,
    onBack: () -> Unit,
    onEditClick: (Goal) -> Unit,
    focusCheckIn: Boolean = false,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    GoalDetailContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onEditClick = onEditClick,
        focusCheckIn = focusCheckIn,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GoalDetailContent(
    state: GoalDetailUiState,
    onIntent: (GoalDetailIntent) -> Unit,
    onBack: () -> Unit,
    onEditClick: (Goal) -> Unit,
    focusCheckIn: Boolean,
) {
    val goal = state.goal
    // D-05: 뷰어 열림 상태는 비즈니스 로직이 없는 순수 UI 네비게이션 관심사라 로컬로 둔다.
    var viewerRequest by remember { mutableStateOf<PhotoViewerRequest?>(null) }
    // S-01: 공유 카드 오버레이도 같은 이유로 로컬 상태.
    var showShareCard by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(goal?.title.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(Res.string.back)) }
                },
                actions = {
                    if (goal != null) {
                        TextButton(onClick = { onEditClick(goal) }) { Text(stringResource(Res.string.action_edit)) }
                    }
                },
            )
        },
        bottomBar = {
            if (goal != null) {
                GoalDetailBottomBar(
                    goal = goal,
                    state = state,
                    onIntent = onIntent,
                    focusCheckIn = focusCheckIn,
                    onShareClick = { showShareCard = true },
                )
            }
        },
    ) { padding ->
        if (goal == null) return@Scaffold

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // D-04: 반복형은 목표치 대비 진행량(3/12), 한 번 하기는 누적 기록 수를 보여준다.
            if (goal.type == GoalType.REPEATABLE && goal.targetCount != null) {
                Text(
                    text = stringResource(Res.string.progress_count, state.progressCount, goal.targetCount),
                    style = MaterialTheme.typography.bodyMedium.merge(MonoLabel()),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            } else if (state.timeline.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.entry_count, state.timeline.size),
                    style = MaterialTheme.typography.bodyMedium.merge(MonoLabel()),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            // MVP-SCOPE.md §1.1: 반복형이 목표치에 도달하면 자동 완료가 아니라 완료 제안만 띄운다.
            if (
                goal.status == GoalStatus.IN_PROGRESS &&
                goal.type == GoalType.REPEATABLE &&
                goal.targetCount != null &&
                state.progressCount >= goal.targetCount
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.progress_target_reached),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { onIntent(GoalDetailIntent.RequestComplete) }) {
                        Text(stringResource(Res.string.progress_target_reached_action))
                    }
                }
            }

            if (state.timeline.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.timeline_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(state.timeline, key = { it.entry.id }) { timelineEntry ->
                        TimelineNode(
                            timelineEntry = timelineEntry,
                            retrospect = goal.retrospect,
                            onPhotoClick = { index ->
                                viewerRequest = PhotoViewerRequest(
                                    displayPaths = timelineEntry.photos.map { it.displayPath },
                                    initialIndex = index,
                                )
                            },
                            onEditClick = { onIntent(GoalDetailIntent.RequestEditEntry(timelineEntry.entry.id)) },
                            onDeleteClick = { onIntent(GoalDetailIntent.RequestDeleteEntry(timelineEntry.entry.id)) },
                        )
                    }
                    item(key = "goal-created") {
                        TimelineCreatedNode(createdAt = goal.createdAt, isLast = true)
                    }
                }
            }
        }
    }

    state.pendingAction?.let { pending ->
        ActionDialog(
            pending = pending,
            goalTitle = goal?.title.orEmpty(),
            category = goal?.category ?: Category.OTHER,
            timeline = state.timeline,
            onIntent = onIntent,
        )
    }

    if (state.hasError) {
        AlertDialog(
            onDismissRequest = { onIntent(GoalDetailIntent.DismissError) },
            text = { Text(stringResource(Res.string.error_generic)) },
            confirmButton = {
                TextButton(onClick = { onIntent(GoalDetailIntent.DismissError) }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    viewerRequest?.let { request ->
        PhotoViewerOverlay(request = request, onDismiss = { viewerRequest = null })
    }

    if (showShareCard && goal != null) {
        val completionPhoto = state.timeline
            .firstOrNull { it.entry.kind == EntryKind.COMPLETION }
            ?.photos?.firstOrNull()?.displayPath
        ShareCardOverlay(
            goalTitle = goal.title,
            completedAt = goal.completedAt,
            retrospect = goal.retrospect,
            photoPath = completionPhoto,
            onDismiss = { showShareCard = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalDetailBottomBar(
    goal: Goal,
    state: GoalDetailUiState,
    onIntent: (GoalDetailIntent) -> Unit,
    focusCheckIn: Boolean,
    onShareClick: () -> Unit,
) {
    val checkInFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // N-02 스마트 넛지 딥링크(focus=checkin) — "탭 후 경험이 이 기능의 전부"(docs/NOTIFICATIONS.md §2).
    LaunchedEffect(focusCheckIn, goal.status) {
        if (focusCheckIn && goal.status == GoalStatus.IN_PROGRESS) {
            checkInFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // edge-to-edge에서는 Scaffold가 bottomBar 내부까지 시스템 내비게이션 바 인셋을 자동으로
    // 처리해주지 않는다 — 직접 안 하면 3버튼 내비게이션 모드에서 액션 버튼(특히 삭제)이 가려진다.
    Surface(tonalElevation = 3.dp, modifier = Modifier.navigationBarsPadding()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            when (goal.status) {
                GoalStatus.IN_PROGRESS -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = state.checkInDraft,
                            onValueChange = { onIntent(GoalDetailIntent.CheckInTextChanged(it)) },
                            placeholder = { Text(stringResource(Res.string.check_in_placeholder)) },
                            modifier = Modifier.weight(1f).focusRequester(checkInFocusRequester),
                            singleLine = true,
                        )
                        TextButton(onClick = { onIntent(GoalDetailIntent.SubmitCheckIn) }) {
                            Text(stringResource(Res.string.check_in_save))
                        }
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestAddProgress) }) {
                            Text(stringResource(Res.string.action_add_progress))
                        }
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestComplete) }) {
                            Text(stringResource(Res.string.action_complete))
                        }
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestArchive) }) {
                            Text(stringResource(Res.string.action_archive))
                        }
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestDelete) }) {
                            Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                GoalStatus.COMPLETED -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onShareClick) {
                            Text(stringResource(Res.string.action_share))
                        }
                        TextButton(onClick = { onIntent(GoalDetailIntent.Restore) }) {
                            Text(stringResource(Res.string.action_restore))
                        }
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestDelete) }) {
                            Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                GoalStatus.ARCHIVED -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onIntent(GoalDetailIntent.Restore) }) {
                            Text(stringResource(Res.string.action_restore))
                        }
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestDelete) }) {
                            Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

/** docs/DESIGN.md §4 타임라인: 퀵 체크인=작은 점, 진행 기록=큰 점, 완료=채워진 원+체크 */
@Composable
private fun TimelineNode(
    timelineEntry: TimelineEntry,
    retrospect: String?,
    onPhotoClick: (index: Int) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val entry = timelineEntry.entry

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        TimelineRail(
            // docs/DESIGN.md §5.2 — 노드 크기는 성과가 아니라 "기록의 중요도"를 나타낸다.
            nodeSize = when (entry.kind) {
                EntryKind.CHECK_IN -> 4.dp
                EntryKind.PROGRESS -> 8.dp
                EntryKind.COMPLETION -> 16.dp
            },
            filled = true,
            color = when (entry.kind) {
                EntryKind.COMPLETION -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            },
            showCheck = entry.kind == EntryKind.COMPLETION,
            checkColor = MaterialTheme.colorScheme.onTertiary,
        )

        Column(modifier = Modifier.padding(start = 12.dp, bottom = 20.dp)) {
            Text(
                text = relativeDayLabel(entry.recordedAt),
                style = MaterialTheme.typography.labelMedium.merge(MonoLabel()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            entry.memo?.takeIf { it.isNotBlank() }?.let { memo ->
                Text(text = memo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
            }
            if (entry.kind == EntryKind.COMPLETION && !retrospect.isNullOrBlank()) {
                Text(
                    text = "${stringResource(Res.string.retrospect_label)} $retrospect",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (timelineEntry.photos.isNotEmpty()) {
                // LazyRow는 IntrinsicSize.Min 부모(TimelineRail 세로선) 안에서 측정할 수 없어 일반 Row로 스크롤한다.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    timelineEntry.photos.forEachIndexed { index, photo ->
                        AsyncImage(
                            model = photo.thumbnailPath,
                            contentDescription = entry.memo,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onPhotoClick(index) },
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
            // E-05: 완료 인증(COMPLETION)은 별도 화면·흐름이라 여기선 체크인/진행 기록만 수정·삭제한다.
            if (entry.kind != EntryKind.COMPLETION) {
                Row(modifier = Modifier.padding(top = 2.dp)) {
                    TextButton(onClick = onEditClick, modifier = Modifier.height(32.dp)) {
                        Text(stringResource(Res.string.action_edit), style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(onClick = onDeleteClick, modifier = Modifier.height(32.dp)) {
                        Text(stringResource(Res.string.action_delete), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineCreatedNode(createdAt: Instant, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        TimelineRail(nodeSize = 10.dp, filled = false, color = MaterialTheme.colorScheme.outline, isLast = isLast)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = relativeDayLabel(createdAt),
                style = MaterialTheme.typography.labelMedium.merge(MonoLabel()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.timeline_goal_created),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimelineRail(
    nodeSize: Dp,
    filled: Boolean,
    color: Color,
    showCheck: Boolean = false,
    checkColor: Color = MaterialTheme.colorScheme.onTertiary,
    isLast: Boolean = false,
) {
    Box(modifier = Modifier.width(28.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
        if (!isLast) {
            // docs/DESIGN.md §5.2 — 세로선은 화면을 강하게 가르지 않도록 가늘고 옅게.
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)),
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(nodeSize)
                .background(if (filled) color else Color.Transparent, CircleShape)
                .then(if (!filled) Modifier.border(1.5.dp, color, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (showCheck) {
                Text(
                    text = "✓",
                    color = checkColor,
                    style = MaterialTheme.typography.labelSmall,
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionDialog(
    pending: PendingAction,
    goalTitle: String,
    category: Category,
    timeline: List<TimelineEntry>,
    onIntent: (GoalDetailIntent) -> Unit,
) {
    when (pending) {
        PendingAction.ConfirmComplete -> {
            var retrospect by remember { mutableStateOf("") }
            var photos by remember { mutableStateOf(listOf<ByteArray>()) }
            // E-08: 완료 시 카테고리에 맞는 질문을 랜덤 제시. "다른 질문 보기"로 재추첨.
            var question by remember { mutableStateOf(randomRetrospectQuestion(category)) }
            val launchCamera = rememberCameraCapture { bytes -> if (bytes != null) photos = (photos + bytes).take(5) }
            val launchGallery = rememberPhotoPicker(maxItems = (5 - photos.size).coerceAtLeast(1)) { newPhotos ->
                photos = (photos + newPhotos).take(5)
            }
            AlertDialog(
                onDismissRequest = { onIntent(GoalDetailIntent.DismissDialog) },
                title = { Text(goalTitle) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = retrospect,
                            onValueChange = { retrospect = it },
                            placeholder = { Text(stringResource(question)) },
                        )
                        TextButton(
                            onClick = { question = randomRetrospectQuestion(category, exclude = question) },
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            Text(stringResource(Res.string.retrospect_another_question))
                        }
                        PhotoAttachRow(
                            photoCount = photos.size,
                            onCameraClick = launchCamera,
                            onGalleryClick = launchGallery,
                            onClearClick = { photos = emptyList() },
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onIntent(GoalDetailIntent.ConfirmComplete(retrospect.ifBlank { null }, photos)) },
                    ) {
                        Text(stringResource(Res.string.action_complete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
        PendingAction.ConfirmArchive -> {
            var reason by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { onIntent(GoalDetailIntent.DismissDialog) },
                title = { Text(stringResource(Res.string.archive_dialog_title)) },
                text = {
                    Column {
                        Text(stringResource(Res.string.archive_dialog_body))
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            placeholder = { Text(stringResource(Res.string.archive_reason_placeholder)) },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.ConfirmArchive(reason.ifBlank { null })) }) {
                        Text(stringResource(Res.string.action_archive))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
        PendingAction.ConfirmDelete -> {
            AlertDialog(
                onDismissRequest = { onIntent(GoalDetailIntent.DismissDialog) },
                title = { Text(stringResource(Res.string.delete_dialog_title)) },
                text = { Text(stringResource(Res.string.delete_dialog_body)) },
                confirmButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.ConfirmDelete) }) {
                        Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
        PendingAction.AddProgress -> {
            var memo by remember { mutableStateOf("") }
            var photos by remember { mutableStateOf(listOf<ByteArray>()) }
            var incrementCount by remember { mutableStateOf(true) }
            val launchCamera = rememberCameraCapture { bytes -> if (bytes != null) photos = (photos + bytes).take(5) }
            val launchGallery = rememberPhotoPicker(maxItems = (5 - photos.size).coerceAtLeast(1)) { newPhotos ->
                photos = (photos + newPhotos).take(5)
            }
            AlertDialog(
                onDismissRequest = { onIntent(GoalDetailIntent.DismissDialog) },
                title = { Text(stringResource(Res.string.progress_dialog_title)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            placeholder = { Text(stringResource(Res.string.progress_memo_placeholder)) },
                        )
                        PhotoAttachRow(
                            photoCount = photos.size,
                            onCameraClick = launchCamera,
                            onGalleryClick = launchGallery,
                            onClearClick = { photos = emptyList() },
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Checkbox(checked = incrementCount, onCheckedChange = { incrementCount = it })
                            Text(stringResource(Res.string.progress_increment_count), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onIntent(GoalDetailIntent.ConfirmAddProgress(memo.ifBlank { null }, photos, incrementCount))
                        },
                        enabled = memo.isNotBlank() || photos.isNotEmpty() || incrementCount,
                    ) {
                        Text(stringResource(Res.string.progress_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
        is PendingAction.EditEntry -> {
            val entry = timeline.firstOrNull { it.entry.id == pending.entryId }?.entry ?: return
            var memo by remember(entry.id) { mutableStateOf(entry.memo.orEmpty()) }
            var recordedAt by remember(entry.id) { mutableStateOf(entry.recordedAt) }
            var showDatePicker by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { onIntent(GoalDetailIntent.DismissDialog) },
                title = { Text(stringResource(Res.string.edit_entry_dialog_title)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            placeholder = { Text(stringResource(Res.string.progress_memo_placeholder)) },
                        )
                        // E-04: 소급 기록 — 날짜를 눌러 과거 날짜로 바꿀 수 있다.
                        TextButton(onClick = { showDatePicker = true }, modifier = Modifier.padding(top = 4.dp)) {
                            Text(recordedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString())
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onIntent(GoalDetailIntent.ConfirmEditEntry(entry.id, memo.ifBlank { null }, recordedAt))
                        },
                    ) {
                        Text(stringResource(Res.string.save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )

            if (showDatePicker) {
                // E-04는 "과거 날짜로 소급 기록"이 목적이라 미래 날짜는 고를 수 없게 막는다.
                val nowMillis = remember { Clock.System.now().toEpochMilliseconds() }
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = recordedAt.toEpochMilliseconds(),
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= nowMillis
                    },
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    recordedAt = Instant.fromEpochMilliseconds(millis)
                                }
                                showDatePicker = false
                            },
                        ) { Text(stringResource(Res.string.save)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text(stringResource(Res.string.cancel)) }
                    },
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
        is PendingAction.ConfirmDeleteEntry -> {
            AlertDialog(
                onDismissRequest = { onIntent(GoalDetailIntent.DismissDialog) },
                title = { Text(stringResource(Res.string.delete_entry_dialog_title)) },
                text = { Text(stringResource(Res.string.delete_entry_dialog_body)) },
                confirmButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.ConfirmDeleteEntry(pending.entryId)) }) {
                        Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
    }
}

private data class PhotoViewerRequest(val displayPaths: List<String>, val initialIndex: Int)

/** D-05 사진 전체화면 뷰어. docs/DESIGN.md "사진이 주인공, UI는 배경으로 물러남" — 크롬 최소화. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun PhotoViewerOverlay(request: PhotoViewerRequest, onDismiss: () -> Unit) {
    AppBackHandler(enabled = true, onBack = onDismiss)
    val pagerState = rememberPagerState(initialPage = request.initialIndex) { request.displayPaths.size }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            AsyncImage(
                model = request.displayPaths[page],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (request.displayPaths.size > 1) {
                Text(
                    text = stringResource(Res.string.photo_viewer_count, pagerState.currentPage + 1, request.displayPaths.size),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            } else {
                Box(modifier = Modifier.width(1.dp))
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.photo_viewer_close), color = Color.White)
            }
        }
    }
}
