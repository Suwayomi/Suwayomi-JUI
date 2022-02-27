import org.jetbrains.compose.compose

plugins {
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
    id("org.jmailen.kotlinter")
}

dependencies {
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
    implementation(libs.accompanistFlowLayout)
    implementation(libs.kamel)
    implementation(libs.materialDialogsCore)

    // Android
    implementation(libs.appCompat)
    implementation(libs.activityCompose)

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
        versionCode = 1
        versionName = version.toString()
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}