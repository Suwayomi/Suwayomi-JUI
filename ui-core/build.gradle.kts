plugins {
    kotlin("multiplatform")
    id("com.android.library")
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
                optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                optIn("androidx.compose.material.ExperimentalMaterialApi")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
            }
        }
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(libs.coroutinesCore)
                api(libs.kamel)
                api(libs.voyagerCore)
                api(projects.core)
                api(projects.i18n)
                api(compose.desktop.currentOs)
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
            }
        }
        val desktopTest by getting {
        }

        val androidMain by getting {
            dependencies {
                api(kotlin("stdlib-jdk8"))
            }
        }
        val androidTest by getting {
        }
    }
}

buildkonfig {
    packageName = "ca.gosyer.uicore.build"
}
