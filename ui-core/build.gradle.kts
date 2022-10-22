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
    iosX64()
    iosArm64()
    iosSimulatorArm64()

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
                api(libs.imageloader)
                api(libs.voyager.core)
                api(libs.dateTime)
                api(libs.immutableCollections)
                api(projects.core)
                api(projects.i18n)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.ui)
                api(compose.material)
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
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(compose.desktop.currentOs)
            }
        }
        val jvmTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val desktopMain by getting {
            dependsOn(jvmMain)
        }
        val desktopTest by getting {
            dependsOn(jvmTest)
        }

        val androidMain by getting {
            dependsOn(jvmMain)
            dependencies {
                api(libs.bundles.compose.android)
                api(libs.androidx.core)
                api(libs.androidx.appCompat)
            }
        }
        val androidTest by getting {
            dependsOn(jvmTest)
        }

        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosTest by creating {
            dependsOn(commonTest)
        }

        listOf(
            "iosX64",
            "iosArm64",
            "iosSimulatorArm64",
        ).forEach {
            getByName(it + "Main").dependsOn(iosMain)
            getByName(it + "Test").dependsOn(iosTest)
        }
    }
}

buildkonfig {
    packageName = "ca.gosyer.jui.uicore.build"
}
