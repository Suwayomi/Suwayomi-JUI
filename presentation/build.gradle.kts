import org.jetbrains.compose.compose

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
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
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("com.google.accompanist.pager.ExperimentalPagerApi")
                optIn("androidx.compose.animation.ExperimentalAnimationApi")
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
                api(libs.voyager.navigation)
                api(libs.voyager.transitions)
                api(libs.materialDialogs.core)
                api(libs.accompanist.pager)
                api(libs.accompanist.pagerIndicators)
                api(libs.accompanist.flowLayout)
                api(libs.krokiCoroutines)
                api(libs.locale)
                api(libs.dateTime)
                api(libs.aboutLibraries.core)
                api(libs.aboutLibraries.ui)
                api(projects.core)
                api(projects.i18n)
                api(projects.data)
                api(projects.uiCore)
                api(compose.desktop.currentOs)
                api(compose("org.jetbrains.compose.ui:ui-util"))
                api(compose.materialIconsExtended)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val desktopMain by getting {
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(libs.coroutines.swing)
            }
        }
        val desktopTest by getting {
        }

        val androidMain by getting {
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(libs.androidx.core)
                api(libs.androidx.appCompat)
                api(libs.androidx.activity.compose)
            }
        }
        val androidTest by getting {
        }
    }
}

dependencies {
    add("kspDesktop", libs.kotlinInject.compiler)
    add("kspAndroid", libs.kotlinInject.compiler)
}

buildkonfig {
    packageName = "ca.gosyer.jui.presentation.build"
}
