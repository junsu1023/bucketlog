package com.bucketlog.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bucketlog.domain.usecase.ScheduleNudgeUseCase
import java.util.concurrent.TimeUnit
import org.koin.mp.KoinPlatformTools

private const val NUDGE_WORK_NAME = "nudge_evaluation"

/** androidApp이 WorkManager를 직접 몰라도 되게 shared(androidMain)에 등록 로직을 둔다. */
fun schedulePeriodicNudgeEvaluation(context: Context) {
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        NUDGE_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        PeriodicWorkRequestBuilder<NudgeEvaluationWorker>(7, TimeUnit.DAYS).build(),
    )
}

/**
 * WorkManager가 주 1회 실행 — docs/ARCHITECTURE.md §7 "넛지 평가".
 * Koin이 CoroutineWorker 생성자에 직접 주입하지 않으므로(koin-androidx-workmanager 미사용,
 * 최소 의존성 유지) 실행 시점에 전역 Koin 컨텍스트에서 꺼낸다 — MainActivity의
 * NotificationPermissionBridge 접근과 동일한 패턴.
 */
class NudgeEvaluationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val scheduleNudge = KoinPlatformTools.defaultContext().get().get<ScheduleNudgeUseCase>()
        return runCatching { scheduleNudge() }
            .fold(onSuccess = { Result.success() }, onFailure = { Result.retry() })
    }
}
