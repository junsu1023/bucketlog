package com.bucketlog.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools

/**
 * 목표/기록이 바뀔 때마다 위젯 3종을 갱신한다. shared 모듈은 위젯의 존재를 몰라도 되도록(위젯은
 * Android 전용 UI 관심사이지 비즈니스 로직이 아니다) 개별 usecase 호출부마다 갱신 코드를 심는
 * 대신, 이미 있는 Repository Flow를 여기서 구독하는 방식으로 처리한다 — add/update/delete
 * 전부 자동으로 반영되고, 30분 주기 자동 갱신(*_widget_info.xml)이 있어도 실제 변경 시점에는
 * 훨씬 빠르게 반영된다.
 */
fun observeAndRefreshWidgets(context: Context, scope: CoroutineScope) {
    val koin = KoinPlatformTools.defaultContext().get()
    val goalRepository = koin.get<GoalRepository>()
    val entryRepository = koin.get<EntryRepository>()

    scope.launch {
        combine(
            goalRepository.observeAll(),
            entryRepository.observeLastRecordedAt(),
        ) { _, _ -> Unit }
            .debounce(500)
            .collect {
                SmallStepWidget().updateAll(context)
                YearProgressWidget().updateAll(context)
                TodayMemoryWidget().updateAll(context)
            }
    }
}
