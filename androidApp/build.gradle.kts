import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.core)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    // 홈 화면 위젯(Phase 1, docs/ROADMAP.md). Glance는 androidApp에서 직접 그린다 —
    // Compose Multiplatform 화면 트리와 별개(App Widget Host가 구동하는 별도 프로세스/트리).
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    // shared가 kotlinx-datetime을 implementation으로만 물고 있어 Goal.dueDate(LocalDate) 같은
    // 타입을 위젯 코드에서 직접 다루려면 여기서도 명시적으로 있어야 한다.
    implementation(libs.kotlinx.datetime)
}

android {
    namespace = "com.bucketlog"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bucketlog"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}