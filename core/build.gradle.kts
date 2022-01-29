plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("com.codingfeline.buildkonfig")
    id("org.jmailen.kotlinter")
}

group = "ca.gosyer"
version = "1.2.1"

repositories {
    mavenCentral()
}

kotlin {
    android()
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
            kotlin.srcDir("build/generated/ksp/commonMain/kotlin")
            dependencies {
                api(kotlin("stdlib-common"))
                api(libs.coroutinesCore)
                api(libs.json)
                api(libs.kotlinInjectRuntime)
                api(libs.ktorCore)
                api(libs.ktorSerialization)
                api(libs.okio)
                api(libs.ktlogging)
                api(libs.multiplatformSettingsCore)
                api(libs.multiplatformSettingsCoroutines)
                api(libs.multiplatformSettingsSerialization)
            }
        }
        val commonTest by getting {
            kotlin.srcDir("build/generated/ksp/commonTest/kotlin")
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val desktopMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            kotlin.srcDir("build/generated/ksp/desktopMain/kotlin")
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(libs.appDirs)
            }
        }
        val desktopTest by getting {
            kotlin.srcDir("src/jvmTest/kotlin")
            kotlin.srcDir("build/generated/ksp/desktopTest/kotlin")
        }

        val androidMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            kotlin.srcDir("build/generated/ksp/androidMain/kotlin")
            dependencies {
                api(kotlin("stdlib-jdk8"))
            }
        }
        val androidTest by getting {
            kotlin.srcDir("src/jvmTest/kotlin")
            kotlin.srcDir("build/generated/ksp/androidTest/kotlin")
        }
    }
}

dependencies {
    add("kspDesktop", libs.kotlinInjectCompiler)
    add("kspAndroid", libs.kotlinInjectCompiler)
}

buildkonfig {
    packageName = "ca.gosyer.core.build"
}
