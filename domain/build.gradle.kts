import org.jetbrains.compose.compose

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.buildkonfig.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
    id(libs.plugins.ktorfit.get().pluginId)
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
                optIn("de.jensklingenberg.ktorfit.internal.InternalKtorfitApi")
            }
        }
        val commonMain by getting {
            dependencies {
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

        val jvmMain by getting {
            dependencies {
                api(libs.ktor.okHttp)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val desktopMain by getting {
        }
        val desktopTest by getting {
        }

        val androidMain by getting {
        }
        val androidUnitTest by getting {
        }

        val iosMain by getting {
            dependencies {
                api(libs.ktor.darwin)
            }
        }
        val iosTest by getting {
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
    }
}

buildkonfig {
    packageName = "ca.gosyer.jui.domain.build"
}

android {
    namespace = "ca.gosyer.jui.domain"
}
