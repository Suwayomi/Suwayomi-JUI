import org.jetbrains.compose.compose

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.compose.get().pluginId)
    id(libs.plugins.buildkonfig.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
}

kotlin {
    android {
        compilations {
            all {
                kotlinOptions.jvmTarget = Config.androidJvmTarget.toString()
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                kotlinOptions.jvmTarget = Config.desktopJvmTarget.toString()
            }
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                optIn("androidx.compose.material.ExperimentalMaterialApi")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
            }
        }
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(libs.coroutines.core)
                api(libs.kamel)
                api(libs.voyager.core)
                api(libs.dateTime)
                api(projects.core)
                api(projects.i18n)
                api(compose.desktop.currentOs)
                api(compose.materialIconsExtended)
                api(compose("org.jetbrains.compose.ui:ui-util"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by creating {
            dependsOn(commonMain)
        }

        val desktopMain by getting {
            dependsOn(jvmMain)
            dependencies {
                api(kotlin("stdlib-jdk8"))
            }
        }
        val desktopTest by getting

        val androidMain by getting {
            dependsOn(jvmMain)
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(libs.bundles.compose.android)
            }
        }
        val androidTest by getting
    }
}

buildkonfig {
    packageName = "ca.gosyer.jui.uicore.build"
}
