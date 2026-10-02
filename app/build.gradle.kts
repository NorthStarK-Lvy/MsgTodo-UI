plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.msgtodo"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.msgtodo.ui.demo"
        // 与上游一致：API 34+。Miuix 与本 UI 用到的 API 均不高于 34。
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    // LifecycleResumeEffect：demo 里回前台时刷新示例状态
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // ── UI：Miuix（HyperOS 设计语言的 Compose 实现）──
    // compose 运行时由 miuix-ui 传递引入。
    implementation("org.jetbrains.compose.foundation:foundation:1.11.1")
    // 版本必须与 foundation 一致，否则会出现两份 runtime 打架。
    implementation("org.jetbrains.compose.animation:animation:1.11.1")
    implementation("top.yukonga.miuix.kmp:miuix-ui:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-icons:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-preference:0.9.4")
}
