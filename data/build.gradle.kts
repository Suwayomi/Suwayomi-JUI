@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
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

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
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
                api(libs.ktor.auth)
                api(libs.ktor.logging)
                api(libs.ktor.websockets)
                api(libs.ktor.okHttp)
                api(libs.okio)
                api(libs.locale)
                api(libs.dateTime)
                api(projects.core)
                api(projects.i18n)
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

dependencies {
    add("kspDesktop", libs.kotlinInject.compiler)
    add("kspAndroid", libs.kotlinInject.compiler)
}

buildkonfig {
    packageName = "ca.gosyer.jui.data.build"
}
