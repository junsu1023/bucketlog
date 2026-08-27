package com.bucketlog.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.bucketlog.R
import com.bucketlog.domain.usecase.GetThrowbackUseCase
import org.koin.mp.KoinPlatformTools

/**
 * 위젯 "오늘의 기억"(H-07 작년 오늘의 위젯 버전). 앱을 열지 않아도 홈 화면에서 과거 기록이
 * 스스로 돌아오게 한다 — CLAUDE.md §2의 "공백을 메우는 장치" 중 하나를 앱 밖으로 확장한 것.
 */
class ThrowbackWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val getThrowback = KoinPlatformTools.defaultContext().get().get<GetThrowbackUseCase>()
        val pick = runCatching { getThrowback() }.getOrNull()

        provideContent {
            if (pick == null) {
                WidgetEmptyState(context.getString(R.string.widget_throwback_empty))
            } else {
                val body = if (pick.isYearAgo) {
                    context.getString(R.string.widget_throwback_year_ago, pick.goalTitle)
                } else {
                    context.getString(R.string.widget_throwback_month_ago, pick.goalTitle)
                }
                WidgetCard(
                    label = context.getString(R.string.widget_throwback_label),
                    body = body,
                    deepLink = goalCheckInDeepLink(pick.goalId),
                )
            }
        }
    }
}

class ThrowbackWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ThrowbackWidget()
}
