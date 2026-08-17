package com.bucketlog.di

import com.bucketlog.data.local.AppDatabase
import com.bucketlog.data.local.DatabaseFactory
import com.bucketlog.data.repository.EntryRepositoryImpl
import com.bucketlog.data.repository.GoalRepositoryImpl
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.usecase.AddCheckInEntryUseCase
import com.bucketlog.domain.usecase.AddGoalUseCase
import com.bucketlog.domain.usecase.ArchiveGoalUseCase
import com.bucketlog.domain.usecase.CompleteGoalUseCase
import com.bucketlog.domain.usecase.DeleteGoalUseCase
import com.bucketlog.domain.usecase.ObserveGoalOverviewsUseCase
import com.bucketlog.domain.usecase.RestoreGoalUseCase
import com.bucketlog.presentation.addgoal.AddGoalViewModel
import com.bucketlog.presentation.home.HomeViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single { get<DatabaseFactory>().create() }
    single { get<AppDatabase>().goalDao() }
    single { get<AppDatabase>().entryDao() }
    single { get<AppDatabase>().photoDao() }

    single<GoalRepository> { GoalRepositoryImpl(get()) }
    single<EntryRepository> { EntryRepositoryImpl(get()) }

    factory { AddGoalUseCase(get()) }
    factory { DeleteGoalUseCase(get()) }
    factory { CompleteGoalUseCase(get(), get()) }
    factory { ArchiveGoalUseCase(get()) }
    factory { RestoreGoalUseCase(get(), get()) }
    factory { AddCheckInEntryUseCase(get()) }
    factory { ObserveGoalOverviewsUseCase(get(), get()) }

    viewModelOf(::HomeViewModel)
    viewModelOf(::AddGoalViewModel)
}

/** 플랫폼 진입점(Android Application / iOS MainViewController)에서 한 번만 호출한다. */
fun initKoin(platformModule: Module, config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, platformModule)
    }
}
