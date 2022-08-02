import org.jetbrains.compose.compose

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
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
    //iosSimulatorArm64()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("com.russhwolf.settings.ExperimentalSettingsApi")
                optIn("com.russhwolf.settings.ExperimentalSettingsImplementation")
            }
        }
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(libs.coroutines.core)
                api(libs.serialization.json)
                api(libs.kotlinInject.runtime)
                api(libs.ktor.core)
                api(libs.ktor.contentNegotiation)
                api(libs.ktor.serialization.json)
                api(libs.okio)
                api(libs.logging.kmlogging)
                api(libs.multiplatformSettings.core)
                api(libs.multiplatformSettings.coroutines)
                api(libs.multiplatformSettings.serialization)
                api(libs.dateTime)
                api(compose("org.jetbrains.compose.ui:ui-text"))
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
                api(libs.appDirs)
            }
        }
        val desktopTest by getting

        val androidMain by getting {
            dependsOn(jvmMain)
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(libs.compose.ui.text)
            }
        }
        val androidTest by getting

        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosTest by creating {
            dependsOn(commonTest)
        }

        listOf("iosX64Main", "iosArm64Main"/*, "iosSimulatorArm64Main"*/).forEach {
            getByName(it).dependsOn(iosMain)
        }
        listOf("iosX64Test", "iosArm64Test"/*, "iosSimulatorArm64Test"*/).forEach {
            getByName(it).dependsOn(iosTest)
        }
    }
}

dependencies {
    add("kspDesktop", libs.kotlinInject.compiler)
    add("kspAndroid", libs.kotlinInject.compiler)
}

buildkonfig {
    packageName = "ca.gosyer.jui.core.build"
}
