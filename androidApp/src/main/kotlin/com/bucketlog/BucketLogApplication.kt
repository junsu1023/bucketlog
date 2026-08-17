package com.bucketlog

import android.app.Application
import com.bucketlog.data.local.DatabaseFactory
import com.bucketlog.di.initKoin
import com.bucketlog.platform.FileStorage
import org.koin.dsl.module

class BucketLogApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin(
            platformModule = module {
                single { DatabaseFactory(this@BucketLogApplication) }
                single { FileStorage(this@BucketLogApplication) }
            },
        )
    }
}
