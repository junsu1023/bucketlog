package com.bucketlog.domain.usecase

import com.bucketlog.notification.NotificationBudget
import com.bucketlog.platform.LocalNotification
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.time.Clock
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * 12월에만 실제로 예약하는 usecase라 "12월이 아니면 아무것도 안 한다"만 결정적으로 검증한다 —
 * 실제 발송 로직(중순/31일 분기)은 리터럴 12월 날짜에 의존하므로 여기선 다루지 않는다
 * (테스트가 실행되는 실제 날짜에 따라 픽스처가 깨지는 걸 피하기 위함, test.md 참고).
 */
class ScheduleYearEndRecapUseCaseTest {

    @Test
    fun `12월이 아니면 아무것도 예약하지 않는다`() = runBlocking {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (today.monthNumber == 12) return@runBlocking // 실제로 12월에 돌아가면 이 케이스는 검증 대상이 아니다.

        val captured = mutableListOf<LocalNotification>()
        val settings = FakeSettingsStore()
        val budget = NotificationBudget(settings) { captured += it }
        val useCase = ScheduleYearEndRecapUseCase(
            notificationBudget = budget,
            settings = settings,
            recapTitle = { "버킷로그" },
            midMonthBody = { "" },
            yearEndBody = { "" },
        )

        useCase()

        assertFalse(captured.isNotEmpty())
    }
}
