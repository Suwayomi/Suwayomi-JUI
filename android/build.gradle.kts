import Config.migrationCode
import org.jetbrains.compose.compose

plugins {
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
    id("org.jmailen.kotlinter")
}

dependencies {
    modules {
        module("androidx.lifecycle:lifecycle-viewmodel-ktx") {
            replacedBy("androidx.lifecycle:lifecycle-viewmodel")
        }
    }
    implementation(projects.core)
    implementation(projects.i18n)
    implementation(projects.data)
    implementation(projects.uiCore)
    implementation(projects.presentation)

    // UI (Compose)
    implementation(libs.voyagerCore)
    implementation(libs.voyagerNavigation)
    implementation(libs.voyagerTransitions)
    implementation(libs.accompanistPager)
    implementation(libs.accompanistPagerIndicators)
    implementation(libs.accompanistFlowLayout)
    implementation(libs.kamel)
    implementation(libs.materialDialogsCore)

    // Android
    implementation(libs.core)
    implementation(libs.appCompat)
    implementation(libs.activityCompose)
    implementation(libs.work)

    // Android Lifecycle
    implementation(libs.lifecycleCommon)
    implementation(libs.lifecycleProcess)
    implementation(libs.lifecycleRuntime)

    // Threading
    implementation(libs.coroutinesCore)
    implementation(libs.coroutinesAndroid)

    // Json
    implementation(libs.json)

    // Xml
    implementation(libs.xmlUtilCore)
    implementation(libs.xmlUtilSerialization)

    // Dependency Injection
    implementation(libs.kotlinInjectRuntime)
    ksp(libs.kotlinInjectCompiler)

    // Http client
    implementation(libs.ktorCore)
    implementation(libs.ktorOkHttp)
    implementation(libs.ktorSerialization)
    implementation(libs.ktorLogging)
    implementation(libs.ktorWebsockets)
    implementation(libs.ktorAuth)

    // Logging
    implementation(libs.slf4jApi)
    implementation(libs.slf4jAndroid)
    implementation(libs.ktlogging)

    // Storage
    implementation(libs.okio)

    // Preferences
    implementation(libs.multiplatformSettingsCore)
    implementation(libs.multiplatformSettingsSerialization)
    implementation(libs.multiplatformSettingsCoroutines)

    // Utility
    implementation(libs.krokiCoroutines)

    // Localization
    implementation(libs.mokoCore)
    implementation(libs.mokoCompose)

    // Testing
    testImplementation(kotlin("test-junit"))
    testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
    testImplementation(libs.coroutinesTest)
}

android {
    defaultConfig {
        applicationId = "ca.gosyer.tachidesk.jui.android"
        versionCode = migrationCode
        versionName = version.toString()

        buildConfigField("boolean", "IS_PREVIEW", project.hasProperty("preview").toString())
        buildConfigField("int", "PREVIEW_BUILD", project.properties["preview"]?.toString()?.trim('"') ?: 0.toString())
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}