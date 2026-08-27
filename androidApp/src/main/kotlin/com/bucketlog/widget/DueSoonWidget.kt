package com.bucketlog.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.bucketlog.R
import com.bucketlog.domain.usecase.PickDueSoonGoalUseCase
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import org.koin.mp.KoinPlatformTools

/**
 * 위젯 "마감 임박"(N-04/G-09의 위젯 버전). 알림과 달리 [PickDueSoonGoalUseCase]의 순수 조회만
 * 쓴다 — NotificationBudget 슬롯을 소비하는 [com.bucketlog.domain.usecase.ScheduleDueSoonUseCase]와
 * 달리 위젯은 그냥 화면에 그리기만 하므로 부수효과가 없어야 한다.
 */
class DueSoonWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val pickDueSoonGoal = KoinPlatformTools.defaultContext().get().get<PickDueSoonGoalUseCase>()
        val target = runCatching { pickDueSoonGoal() }.getOrNull()

        provideContent {
            if (target == null) {
                WidgetEmptyState(context.getString(R.string.widget_due_soon_empty))
            } else {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val daysLeft = today.daysUntil(target.dueDate!!)
                val body = if (daysLeft <= 0) {
                    context.getString(R.string.widget_due_soon_body_today, target.title)
                } else {
                    context.getString(R.string.widget_due_soon_body, target.title, daysLeft)
                }
                WidgetCard(
                    label = context.getString(R.string.widget_due_soon_label),
                    body = body,
                    deepLink = goalCheckInDeepLink(target.id),
                )
            }
        }
    }
}

class DueSoonWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DueSoonWidget()
}
