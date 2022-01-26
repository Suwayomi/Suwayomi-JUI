import org.jetbrains.kotlin.gradle.tasks.KotlinCompile as KotlinJvmCompile

plugins {
    kotlin("multiplatform")
    kotlin("kapt")
    id("com.android.library")
}

group = "ca.gosyer"
version = "1.2.1"

repositories {
    mavenCentral()
}

kotlin {
    android()
    jvm("desktop")

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
                api(libs.coroutinesCore)
                api(libs.json)
                api(libs.toothpickKsp)
                api(libs.ktorCore)
                api(libs.ktorSerialization)
                api(libs.okio)
                api(libs.multiplatformSettingsCore)
                api(libs.multiplatformSettingsCoroutines)
                api(libs.multiplatformSettingsSerialization)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val desktopMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            dependencies {
                api(kotlin("stdlib-jdk8"))
            }
        }
        val desktopTest by getting {
            kotlin.srcDir("src/jvmTest/kotlin")
        }

        val androidMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            dependencies {
                api(kotlin("stdlib-jdk8"))
            }
        }
        val androidTest by getting {
            kotlin.srcDir("src/jvmTest/kotlin")
        }
    }
}

dependencies {
    add("kapt", libs.toothpickCompiler)
}

tasks {
    withType<KotlinJvmCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjvm-default=compatibility")
        }
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }
}