import Config.migrationCode

plugins {
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.compose.get().pluginId)
    id(libs.plugins.android.application.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.compose.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
    id(libs.plugins.aboutLibraries.get().pluginId)
}

dependencies {
    implementation(projects.core)
    implementation(projects.i18n)
    implementation(projects.domain)
    implementation(projects.data)
    implementation(projects.uiCore)
    implementation(projects.presentation)

    // UI (Compose)
    implementation(libs.bundles.compose.android)
    implementation(libs.voyager.core)
    implementation(libs.voyager.navigation)
    implementation(libs.voyager.transitions)
    implementation(libs.voyager.screenmodel)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pagerIndicators)
    implementation(libs.accompanist.flowLayout)
    implementation(libs.accompanist.systemUIController)
    implementation(libs.imageloader.core)
    implementation(libs.imageloader.moko)
    implementation(libs.materialDialogs.core)
    implementation(libs.materialDialogs.datetime)

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
    implementation(libs.serialization.json.core)
    implementation(libs.serialization.json.okio)

    // Dependency Injection
    implementation(libs.kotlinInject.runtime)
    ksp(libs.kotlinInject.compiler)

    // Http client
    implementation(libs.ktor.core)
    implementation(libs.ktor.okHttp)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.websockets)
    implementation(libs.ktor.auth)

    // Ktorfit
    implementation(libs.ktorfit.lib)
    ksp(libs.ktorfit.ksp)

    // Apollo GraphQL
    implementation(libs.apollo.runtime)
    implementation(libs.apollo.engine.ktor)

    // Logging
    implementation(libs.logging.kmlogging)

    // Storage
    implementation(libs.okio)

    // Preferences
    implementation(libs.multiplatformSettings.core)
    implementation(libs.multiplatformSettings.serialization)
    implementation(libs.multiplatformSettings.coroutines)

    // Utility
    implementation(libs.dateTime)
    implementation(libs.immutableCollections)
    implementation(libs.korge.foundation)

    // Localization
    implementation(libs.moko.core)
    implementation(libs.moko.compose)

    // Testing
    testImplementation(kotlin("test-junit"))
    //testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.coroutines.test)
}

android {
    namespace = "ca.gosyer.jui.android"
    defaultConfig {
        applicationId = "ca.gosyer.jui.android"
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

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources.excludes.addAll(listOf(
            "META-INF/DEPENDENCIES",
            "LICENSE.txt",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/LICENSE.md",
            "META-INF/LICENSE-notice.md",
            "META-INF/README.md",
            "META-INF/NOTICE",
            "META-INF/*.kotlin_module",
        ))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Config.androidJvmTarget.target))
    }
}
