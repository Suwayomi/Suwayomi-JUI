plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.buildkonfig.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
    id(libs.plugins.apollo.get().pluginId)
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
            }
        }
        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                api(libs.serialization.json.core)
                api(libs.serialization.json.okio)
                api(libs.kotlinInject.runtime)
                api(libs.ktor.core)
                api(libs.ktor.websockets)
                api(libs.okio)
                api(libs.dateTime)
                implementation(libs.apollo.runtime)
                implementation(libs.apollo.engine.ktor)
                implementation(libs.ktorfit.lib)
                api(projects.core)
                api(projects.i18n)
                api(projects.domain)
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
        "kspIosX64",
        "kspCommonMainMetadata"
    ).forEach {
        add(it, libs.kotlinInject.compiler)
    }
}

buildkonfig {
    packageName = "ca.gosyer.jui.data.build"
}

android {
    namespace = "ca.gosyer.jui.data"
}

apollo {
    service("service") {
        packageName.set("ca.gosyer.jui.data.graphql")
        generateMethods.set(listOf("equalsHashCode"))
        mapScalar("LongString","kotlin.Long", "ca.gosyer.jui.data.scalars.LongStringScalar")
        mapScalarToUpload("Upload")
        introspection {
            endpointUrl.set("http://localhost:4567/api/graphql")
            schemaFile.set(file("src/main/graphql/schema.graphqls"))
        }
    }
}
