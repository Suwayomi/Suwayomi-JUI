plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
    id("com.android.library")
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
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(libs.coroutinesCore)
                api(libs.json)
                api(libs.kotlinInjectRuntime)
                api(libs.ktorCore)
                api(libs.ktorSerialization)
                api(libs.ktorAuth)
                api(libs.ktorLogging)
                api(libs.ktorWebsockets)
                api(libs.ktorOkHttp)
                api(libs.okio)
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
    add("kspDesktop", libs.kotlinInjectCompiler)
    add("kspAndroid", libs.kotlinInjectCompiler)
}

buildkonfig {
    packageName = "ca.gosyer.data.build"
}
