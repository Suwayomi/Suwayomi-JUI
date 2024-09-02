import org.jetbrains.compose.compose

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotlin.compose.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.compose.get().pluginId)
    id(libs.plugins.buildkonfig.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
}

kotlin {
    androidTarget {
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget = Config.androidJvmTarget
                    }
                }
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget = Config.desktopJvmTarget
                    }
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    applyHierarchyTemplate {
        common {
            group("jvm") {
                withAndroidTarget()
                withJvm()
            }
            group("ios") {
                withIosX64()
                withIosArm64()
                withIosSimulatorArm64()
            }
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("androidx.compose.animation.ExperimentalAnimationApi")
                optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                optIn("androidx.compose.material.ExperimentalMaterialApi")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
            }
        }
        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                api(libs.imageloader.core)
                api(libs.imageloader.moko)
                api(libs.voyager.core)
                api(libs.voyager.navigation)
                api(libs.voyager.transitions)
                api(libs.voyager.screenmodel)
                api(libs.materialDialogs.core)
                api(libs.materialDialogs.datetime)
                api(libs.accompanist.pager)
                api(libs.accompanist.pagerIndicators)
                api(libs.accompanist.flowLayout)
                api(libs.dateTime)
                api(libs.immutableCollections)
                api(libs.aboutLibraries.core)
                api(libs.aboutLibraries.ui)

                api(projects.core)
                api(projects.i18n)
                api(projects.domain)
                api(projects.data)
                api(projects.uiCore)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.ui)
                api(compose.material)
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

        val jvmMain by getting {
            dependencies {
                api(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val desktopMain by getting {
            dependencies {
                api(libs.coroutines.swing)
            }
        }
        val desktopTest by getting {
        }

        val androidMain by getting {
            dependencies {
                api(libs.bundles.compose.android)
                api(libs.androidx.core)
                api(libs.androidx.appCompat)
                api(libs.androidx.activity.compose)
            }
        }
        val androidUnitTest by getting {
        }

        val iosMain by getting {
        }
        val iosTest by getting {
        }
    }
}

dependencies {
    listOf(
        "kspDesktop",
        "kspAndroid",
        "kspIosArm64",
        "kspIosSimulatorArm64",
        "kspIosX64"
    ).forEach {
        add(it, libs.kotlinInject.compiler)
    }
}

buildkonfig {
    packageName = "ca.gosyer.jui.presentation.build"
}

android {
    namespace = "ca.gosyer.jui.presentation"
}
