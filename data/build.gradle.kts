@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.buildkonfig.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
    id(libs.plugins.ktorfit.get().pluginId)
    id(libs.plugins.apollo.get().pluginId)
}

kotlin {
    androidTarget {
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
                api(kotlin("stdlib-common"))
                api(libs.coroutines.core)
                api(libs.serialization.json.core)
                api(libs.serialization.json.okio)
                api(libs.kotlinInject.runtime)
                api(libs.ktor.core)
                api(libs.ktor.websockets)
                api(libs.okio)
                api(libs.dateTime)
                api(libs.apollo.runtime)
                api(libs.apollo.engine.ktor)
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
                api(kotlin("stdlib-jdk8"))
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
        "kspIosX64"
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
        introspection {
            endpointUrl.set("http://localhost:4567/api/graphql")
            schemaFile.set(file("src/main/graphql/schema.graphqls"))
        }
    }
}
