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
    android()
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
                api(project(":core"))
                api(project(":i18n"))
                api(project(":data"))
                api(project(":ui-core"))
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
            kotlin.srcDir("src/jvmMain/kotlin")
            kotlin.srcDir("build/generated/ksp/desktopMain/kotlin")
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(libs.coroutinesSwing)
                api(libs.accompanistPager)
                api(libs.accompanistFlowLayout)
                api(libs.krokiCoroutines)
            }
        }
        val desktopTest by getting {
            kotlin.srcDir("src/jvmTest/kotlin")
            kotlin.srcDir("build/generated/ksp/desktopTest/kotlin")
        }

        val androidMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            kotlin.srcDir("build/generated/ksp/androidMain/kotlin")
            dependencies {
                api(kotlin("stdlib-jdk8"))
            }
        }
        val androidTest by getting {
            kotlin.srcDir("src/jvmTest/kotlin")
            kotlin.srcDir("build/generated/ksp/androidTest/kotlin")
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
