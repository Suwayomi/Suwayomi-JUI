import org.jetbrains.compose.compose

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
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("de.jensklingenberg.ktorfit.internal.InternalKtorfitApi")
            }
        }
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(kotlin("stdlib-common"))
                api(libs.coroutines.core)
                api(libs.serialization.json.core)
                api(libs.serialization.json.okio)
                api(libs.kotlinInject.runtime)
                api(libs.ktor.core)
                api(libs.ktor.contentNegotiation)
                api(libs.ktor.serialization.json)
                api(libs.ktor.auth)
                api(libs.ktor.logging)
                api(libs.ktor.websockets)
                api(libs.ktorfit.lib)
                api(libs.okio)
                api(libs.dateTime)
                api(compose("org.jetbrains.compose.runtime:runtime"))
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

        val jvmMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(libs.ktor.okHttp)
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
        }
        val androidTest by getting {
            dependsOn(jvmTest)
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.ktor.darwin)
            }
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

dependencies {
    listOf(
        "kspCommonMainMetadata",
        "kspDesktop",
        "kspAndroid",
        "kspIosArm64",
        "kspIosSimulatorArm64",
        "kspIosX64"
    ).forEach {
        add(it, libs.kotlinInject.compiler)
        add(it, libs.ktorfit.ksp)
    }
}

buildkonfig {
    packageName = "ca.gosyer.jui.domain.build"
}
