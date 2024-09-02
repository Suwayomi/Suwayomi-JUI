import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotlin.compose.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.compose.get().pluginId)
    id(libs.plugins.buildkonfig.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
    id(libs.plugins.aboutLibraries.get().pluginId)
    id(libs.plugins.ktorfit.get().pluginId)
}

kotlin {
    val configuration: KotlinNativeTarget.() -> Unit = {
        binaries.framework {
            baseName = "ios"
            isStatic = true
        }
    }
    iosX64("uikitX64", configuration)
    iosArm64("uikitArm64", configuration)
    iosSimulatorArm64("uikitSimulatorArm64", configuration)

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    applyHierarchyTemplate {
        common {
            group("uikit") {
                withIosX64()
                withIosArm64()
                withIosSimulatorArm64()
            }
        }
    }

    sourceSets {
        val uikitMain by getting {
            dependencies {
                implementation(projects.core)
                implementation(projects.i18n)
                implementation(projects.domain)
                implementation(projects.data)
                implementation(projects.uiCore)
                implementation(projects.presentation)

                // UI (Compose)
                implementation(compose.foundation)
                implementation(compose.runtime)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.animation)
                implementation(compose("org.jetbrains.compose.ui:ui-util"))
                implementation(libs.voyager.core)
                implementation(libs.voyager.navigation)
                implementation(libs.voyager.transitions)
                implementation(libs.voyager.screenmodel)
                implementation(libs.accompanist.pager)
                implementation(libs.accompanist.pagerIndicators)
                implementation(libs.accompanist.flowLayout)
                implementation(libs.imageloader.core)
                implementation(libs.imageloader.moko)
                implementation(libs.materialDialogs.core)
                implementation(libs.materialDialogs.datetime)

                // Threading
                implementation(libs.coroutines.core)

                // Json
                implementation(libs.serialization.json.core)
                implementation(libs.serialization.json.okio)

                // Dependency Injection
                implementation(libs.kotlinInject.runtime)

                // Http client
                implementation(libs.ktor.core)
                implementation(libs.ktor.darwin)
                implementation(libs.ktor.contentNegotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.logging)
                implementation(libs.ktor.websockets)
                implementation(libs.ktor.auth)

                // Ktorfit
                implementation(libs.ktorfit.lib)

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
                //implementation(libs.moko.compose)

                // Testing
                /*testImplementation(kotlin("test-junit"))
                testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
                testImplementation(libs.coroutines.test)*/
            }
        }
        val uikitTest by getting {
        }
    }
}

dependencies {
    listOf(
        "kspUikitArm64",
        "kspUikitSimulatorArm64",
        "kspUikitX64"
    ).forEach {
        add(it, libs.kotlinInject.compiler)
        add(it, libs.ktorfit.ksp)
    }
}

buildkonfig {
    packageName = "ca.gosyer.jui.ios.build"
}

tasks.register("kspCommonMainKotlinMetadata")
