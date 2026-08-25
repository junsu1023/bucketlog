package com.bucketlog.di

import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.app_name
import bucketlog.shared.generated.resources.due_soon_notification_body
import bucketlog.shared.generated.resources.goal_reminder_notification_body
import bucketlog.shared.generated.resources.monthly_recap_notification_body
import bucketlog.shared.generated.resources.nudge_notification_body
import bucketlog.shared.generated.resources.year_end_recap_notification_end_body
import bucketlog.shared.generated.resources.year_end_recap_notification_mid_body
import com.bucketlog.data.local.AppDatabase
import com.bucketlog.data.local.DatabaseFactory
import com.bucketlog.data.repository.EntryRepositoryImpl
import com.bucketlog.data.repository.GoalRepositoryImpl
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.usecase.AddCheckInEntryUseCase
import com.bucketlog.domain.usecase.AddGoalUseCase
import com.bucketlog.domain.usecase.AddProgressEntryUseCase
import com.bucketlog.domain.usecase.ArchiveGoalUseCase
import com.bucketlog.domain.usecase.CompleteGoalUseCase
import com.bucketlog.domain.usecase.DeleteGoalUseCase
import com.bucketlog.domain.usecase.EvaluateNotificationsUseCase
import com.bucketlog.domain.usecase.ExportBackupUseCase
import com.bucketlog.domain.usecase.ObserveGoalOverviewsUseCase
import com.bucketlog.domain.usecase.PickNudgeTargetUseCase
import com.bucketlog.domain.usecase.DeleteEntryUseCase
import com.bucketlog.domain.usecase.ResetAllDataUseCase
import com.bucketlog.domain.usecase.RestoreBackupUseCase
import com.bucketlog.domain.usecase.RestoreGoalUseCase
import com.bucketlog.domain.usecase.RolloverGoalsUseCase
import com.bucketlog.domain.usecase.ScheduleDueSoonUseCase
import com.bucketlog.domain.usecase.ScheduleGoalRemindersUseCase
import com.bucketlog.domain.usecase.ScheduleMonthlyRecapUseCase
import com.bucketlog.domain.usecase.ScheduleNudgeUseCase
import com.bucketlog.domain.usecase.ScheduleYearEndRecapUseCase
import com.bucketlog.domain.usecase.SearchGoalsUseCase
import com.bucketlog.domain.usecase.UpdateEntryUseCase
import com.bucketlog.notification.AppSettingsStore
import com.bucketlog.notification.NotificationBudget
import com.bucketlog.notification.SettingsStore
import com.bucketlog.platform.AppSettings
import com.bucketlog.platform.FileStorage
import com.bucketlog.platform.ImageProcessor
import com.bucketlog.platform.NotificationScheduler
import com.bucketlog.platform.ZipArchiver
import com.bucketlog.presentation.addgoal.AddGoalViewModel
import com.bucketlog.presentation.archive.ArchiveViewModel
import com.bucketlog.presentation.goaldetail.GoalDetailViewModel
import com.bucketlog.presentation.home.HomeViewModel
import com.bucketlog.presentation.onboarding.OnboardingViewModel
import com.bucketlog.presentation.rollover.RolloverViewModel
import com.bucketlog.presentation.search.SearchViewModel
import com.bucketlog.presentation.settings.SettingsViewModel
import com.bucketlog.presentation.theme.ThemeModeStore
import org.jetbrains.compose.resources.getString
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single { get<DatabaseFactory>().create() }
    single { get<AppDatabase>().goalDao() }
    single { get<AppDatabase>().entryDao() }
    single { get<AppDatabase>().photoDao() }

    // BucketLog: 사진 파이프라인(2주차). FileStorage는 Android/iOS 생성자가 달라
    // platformModule에서 각각 등록한다(DatabaseFactory와 동일한 패턴).
    single { ImageProcessor() }

    // BucketLog: 백업/복원(M-02). ZipArchiver는 Android/iOS 둘 다 Context가 필요 없어
    // FileStorage/AppSettings와 달리 여기(appModule)에서 바로 등록한다.
    single { ZipArchiver() }

    single<GoalRepository> { GoalRepositoryImpl(get(), get(), get()) }
    single<EntryRepository> { EntryRepositoryImpl(get(), get(), get()) }

    // BucketLog: 알림(5주차). AppSettings/NotificationScheduler는 Android/iOS 생성자가 달라
    // platformModule에서 각각 등록한다(FileStorage와 동일한 패턴).
    single<SettingsStore> { AppSettingsStore(get<AppSettings>()) }
    single { ThemeModeStore(get()) }
    single { val scheduler = get<NotificationScheduler>(); NotificationBudget(get()) { scheduler.schedule(it) } }

    factory { AddGoalUseCase(get()) }
    factory { DeleteGoalUseCase(get()) }
    factory { CompleteGoalUseCase(get(), get(), get(), get()) }
    factory { ArchiveGoalUseCase(get()) }
    factory { RolloverGoalsUseCase(get(), get()) }
    factory { RestoreGoalUseCase(get(), get()) }
    factory { AddCheckInEntryUseCase(get()) }
    factory { AddProgressEntryUseCase(get(), get(), get()) }
    factory { ObserveGoalOverviewsUseCase(get(), get()) }
    factory { PickNudgeTargetUseCase(get(), get()) }
    factory {
        ScheduleNudgeUseCase(get(), get(), get(), get(), get()) { days ->
            getString(Res.string.nudge_notification_body, days)
        }
    }
    factory {
        val scheduler = get<NotificationScheduler>()
        ScheduleMonthlyRecapUseCase(
            entryRepository = get(),
            notificationBudget = get(),
            settings = get(),
            cancelNotification = { id -> scheduler.cancel(id) },
            recapTitle = { getString(Res.string.app_name) },
            recapBody = { month -> getString(Res.string.monthly_recap_notification_body, month) },
        )
    }
    factory {
        ScheduleGoalRemindersUseCase(get(), get(), get()) {
            getString(Res.string.goal_reminder_notification_body)
        }
    }
    factory {
        ScheduleDueSoonUseCase(get(), get(), get()) { title ->
            getString(Res.string.due_soon_notification_body, title)
        }
    }
    factory {
        ScheduleYearEndRecapUseCase(
            notificationBudget = get(),
            settings = get(),
            recapTitle = { getString(Res.string.app_name) },
            midMonthBody = { year -> getString(Res.string.year_end_recap_notification_mid_body, year) },
            yearEndBody = { year -> getString(Res.string.year_end_recap_notification_end_body, year) },
        )
    }
    factory { EvaluateNotificationsUseCase(get(), get(), get(), get(), get()) }
    factory { ExportBackupUseCase(get(), get(), get(), get()) }
    factory { RestoreBackupUseCase(get(), get(), get(), get()) }
    factory { ResetAllDataUseCase(get()) }
    factory { UpdateEntryUseCase(get()) }
    factory { DeleteEntryUseCase(get()) }
    factory { SearchGoalsUseCase(get(), get()) }

    viewModelOf(::HomeViewModel)
    viewModelOf(::AddGoalViewModel)
    viewModelOf(::ArchiveViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::SearchViewModel)

    // goalId는 화면 진입 시점의 런타임 파라미터라 viewModelOf(생성자 참조)로는 주입할 수 없다.
    viewModel { params ->
        GoalDetailViewModel(
            goalId = params.get(),
            goalRepository = get(),
            entryRepository = get(),
            fileStorage = get<FileStorage>(),
            addCheckInEntry = get(),
            addProgressEntry = get(),
            completeGoal = get(),
            archiveGoal = get(),
            restoreGoal = get(),
            deleteGoal = get(),
            updateEntry = get(),
            deleteEntry = get(),
        )
    }
    // year도 GoalDetailViewModel의 goalId와 같은 이유로 런타임 파라미터.
    viewModel { params -> RolloverViewModel(year = params.get(), goalRepository = get(), rolloverGoals = get()) }
}

/** 플랫폼 진입점(Android Application / iOS MainViewController)에서 한 번만 호출한다. */
fun initKoin(platformModule: Module, config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, platformModule)
    }
}
