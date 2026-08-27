package com.bucketlog.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.bucketlog.R
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.usecase.PickNudgeTargetUseCase
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import org.koin.mp.KoinPlatformTools

/**
 * 위젯 "지금 어때요"(N-02 스마트 넛지의 위젯 버전). 알림을 꺼둔 유저에게도 같은 가치를
 * 전달할 수 있는 무-권한 대체 경로 — docs/NOTIFICATIONS.md §2의 "목표 이름을 직접 부르는 것"
 * 원칙을 그대로 따른다.
 */
class NudgeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val koin = KoinPlatformTools.defaultContext().get()
        val pickNudgeTarget = koin.get<PickNudgeTargetUseCase>()
        val entryRepository = koin.get<EntryRepository>()

        val now = Clock.System.now()
        val target = runCatching { pickNudgeTarget(now) }.getOrNull()
        val daysSince = target?.let { goal ->
            val lastActivity = entryRepository.observeLastRecordedAt().first()[goal.id] ?: goal.createdAt
            (now - lastActivity).inWholeDays
        }

        provideContent {
            if (target == null || daysSince == null) {
                WidgetEmptyState(context.getString(R.string.widget_nudge_empty))
            } else {
                WidgetCard(
                    label = context.getString(R.string.widget_nudge_label),
                    body = context.getString(R.string.widget_nudge_body, target.title, daysSince.toInt()),
                    deepLink = goalCheckInDeepLink(target.id),
                )
            }
        }
    }
}

class NudgeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NudgeWidget()
}
