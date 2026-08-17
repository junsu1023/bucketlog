package com.bucketlog.di

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
import com.bucketlog.domain.usecase.ObserveGoalOverviewsUseCase
import com.bucketlog.domain.usecase.RestoreGoalUseCase
import com.bucketlog.platform.FileStorage
import com.bucketlog.platform.ImageProcessor
import com.bucketlog.presentation.addgoal.AddGoalViewModel
import com.bucketlog.presentation.goaldetail.GoalDetailViewModel
import com.bucketlog.presentation.home.HomeViewModel
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

    single<GoalRepository> { GoalRepositoryImpl(get(), get(), get()) }
    single<EntryRepository> { EntryRepositoryImpl(get(), get(), get()) }

    factory { AddGoalUseCase(get()) }
    factory { DeleteGoalUseCase(get()) }
    factory { CompleteGoalUseCase(get(), get(), get(), get()) }
    factory { ArchiveGoalUseCase(get()) }
    factory { RestoreGoalUseCase(get(), get()) }
    factory { AddCheckInEntryUseCase(get()) }
    factory { AddProgressEntryUseCase(get(), get(), get()) }
    factory { ObserveGoalOverviewsUseCase(get(), get()) }

    viewModelOf(::HomeViewModel)
    viewModelOf(::AddGoalViewModel)

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
        )
    }
}

/** 플랫폼 진입점(Android Application / iOS MainViewController)에서 한 번만 호출한다. */
fun initKoin(platformModule: Module, config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, platformModule)
    }
}
