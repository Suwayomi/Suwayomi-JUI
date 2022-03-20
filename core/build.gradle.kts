plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("com.codingfeline.buildkonfig")
    id("org.jmailen.kotlinter")
}

group = "ca.gosyer"
version = "1.2.1"

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
                api(libs.ktor.serialization)
                api(libs.okio)
                api(libs.logging.ktlogging)
                api(libs.multiplatformSettings.core)
                api(libs.multiplatformSettings.coroutines)
                api(libs.multiplatformSettings.serialization)
                api(libs.locale)
                api(libs.klock)
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
                api(libs.appDirs)
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

dependencies {
    add("kspDesktop", libs.kotlinInject.compiler)
    add("kspAndroid", libs.kotlinInject.compiler)
}

buildkonfig {
    packageName = "ca.gosyer.jui.core.build"
}
