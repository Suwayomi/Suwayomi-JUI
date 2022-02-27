import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("org.jetbrains.compose")
    id("com.codingfeline.buildkonfig")
    id("org.jmailen.kotlinter")
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
            kotlin.srcDir("build/generated/ksp/commonMain/kotlin")
            dependencies {
                api(kotlin("stdlib-common"))
                api(libs.coroutinesCore)
                api(libs.kamel)
                api(libs.voyagerCore)
                api(libs.voyagerNavigation)
                api(libs.voyagerTransitions)
                api(libs.materialDialogsCore)
                api(libs.accompanistPager)
                api(libs.accompanistFlowLayout)
                api(libs.krokiCoroutines)
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
            kotlin.srcDir("build/generated/ksp/commonTest/kotlin")
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val desktopMain by getting {
            kotlin.srcDir("build/generated/ksp/desktopMain/kotlin")
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(libs.coroutinesSwing)
            }
        }
        val desktopTest by getting {
            kotlin.srcDir("build/generated/ksp/desktopTest/kotlin")
        }

        val androidMain by getting {
            kotlin.srcDir("build/generated/ksp/androidRelease/kotlin")
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(libs.activityCompose)
            }
        }
        val androidTest by getting {
            kotlin.srcDir("build/generated/ksp/androidReleaseTest/kotlin")
        }
    }
}

dependencies {
    add("kspDesktop", libs.kotlinInjectCompiler)
    add("kspAndroid", libs.kotlinInjectCompiler)
}

buildkonfig {
    packageName = "ca.gosyer.presentation.build"
}
