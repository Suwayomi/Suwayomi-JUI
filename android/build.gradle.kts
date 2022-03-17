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
    implementation(libs.voyager.core)
    implementation(libs.voyager.navigation)
    implementation(libs.voyager.transitions)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pagerIndicators)
    implementation(libs.accompanist.flowLayout)
    implementation(libs.kamel)
    implementation(libs.materialDialogs.core)

    // Android
    implementation(libs.androidx.core)
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.work)

    // Android Lifecycle
    implementation(libs.lifecycle.common)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.runtime)

    // Threading
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Json
    implementation(libs.serialization.json)

    // Xml
    implementation(libs.serialization.xmlUtil.core)
    implementation(libs.serialization.xmlUtil.serialization)

    // Dependency Injection
    implementation(libs.kotlinInject.runtime)
    ksp(libs.kotlinInject.compiler)

    // Http client
    implementation(libs.ktor.core)
    implementation(libs.ktor.okHttp)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.websockets)
    implementation(libs.ktor.auth)

    // Logging
    implementation(libs.logging.slf4j.api)
    implementation(libs.logging.slf4j.android)
    implementation(libs.logging.ktlogging)

    // Storage
    implementation(libs.okio)

    // Preferences
    implementation(libs.multiplatformSettings.core)
    implementation(libs.multiplatformSettings.serialization)
    implementation(libs.multiplatformSettings.coroutines)

    // Utility
    implementation(libs.krokiCoroutines)

    // Localization
    implementation(libs.moko.core)
    implementation(libs.moko.compose)
    implementation(libs.locale)

    // Testing
    testImplementation(kotlin("test-junit"))
    testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
    testImplementation(libs.coroutines.test)
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
            isMinifyEnabled = true
            isShrinkResources = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }
}