import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    kotlin("jvm") version "1.4.32"
    kotlin("kapt") version "1.4.32"
    kotlin("plugin.serialization") version "1.4.32"
    id("org.jetbrains.compose") version "0.4.0-build185"
    id("de.fuerstenau.buildconfig") version "1.1.8"
    id("org.jmailen.kotlinter") version "3.4.0"
}

group = "ca.gosyer"
version = "1.0.0"

repositories {
    mavenCentral()

    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // UI (Compose)
    implementation(compose.desktop.currentOs)
    implementation("br.com.devsrsouza.compose.icons.jetbrains:font-awesome:0.2.0")
    implementation("com.github.Syer10:compose-router:45a8c4fe83")

    // UI (Swing)
    implementation("com.github.weisj:darklaf-core:2.5.5")

    // Threading
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

    // Json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

    // Dependency Injection
    implementation("com.github.stephanenicolas.toothpick:ktp:3.1.0")
    kapt("com.github.stephanenicolas.toothpick:toothpick-compiler:3.1.0")

    // Http client
    val ktorVersion = "1.5.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    // Logging
    val log4jVersion = "2.14.1"
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.5")

    // User storage
    implementation("net.harawata:appdirs:1.2.1")

    // Preferences
    val multiplatformSettingsVersion = "0.7.4"
    implementation("com.russhwolf:multiplatform-settings-jvm:$multiplatformSettingsVersion")
    implementation("com.russhwolf:multiplatform-settings-serialization-jvm:$multiplatformSettingsVersion")
    implementation("com.russhwolf:multiplatform-settings-coroutines-jvm:$multiplatformSettingsVersion")

    // Testing
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.3")
}

tasks {
    withType<KotlinCompile> {
        dependsOn(formatKotlin)
        kotlinOptions {
            jvmTarget = "15"
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.time.ExperimentalTime",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-Xopt-in=com.russhwolf.settings.ExperimentalSettingsApi",
                "-Xopt-in=com.russhwolf.settings.ExperimentalSettingsImplementation"
            )
        }
    }
    test {
        useJUnit()
    }

    withType<LintTask> {
        source(files("src"))
        reports.set(mapOf(
            "plain" to file("build/lint-report.txt"),
            "json" to file("build/lint-report.json")
        ))
    }

    withType<FormatTask> {
        source(files("src"))
        report.set(file("build/format-report.txt"))
    }
}


compose.desktop {
    application {
        mainClass = "ca.gosyer.ui.main.MainKt"
        nativeDistributions {
            targetFormats(
                // Windows
                TargetFormat.Msi,
                TargetFormat.Exe,
                // Linux
                TargetFormat.Deb,
                TargetFormat.Rpm,
                // MacOS
                TargetFormat.Pkg
            )
            modules(
                "java.instrument",
                "java.management",
                "java.naming",
                "java.prefs",
                "java.sql",
                "jdk.unsupported"
            )

            packageName = "TachideskJUI"
            description = "TachideskJUI is a Jvm client for a Tachidesk Server"
            copyright = "Mozilla Public License v2.0"
            windows {
                dirChooser = true
                upgradeUuid = "B2ED947E-81E4-4258-8388-2B1EDF5E0A30"
            }
            macOS {
                bundleID = "ca.gosyer.tachideskjui"
                packageName = rootProject.name
            }
        }
    }
}

buildConfig {
    appName = project.name
    version = project.version.toString()

    clsName = "BuildConfig"
    packageName = project.group.toString()

    buildConfigField("boolean", "DEBUG", project.hasProperty("debugApp").toString())
    buildConfigField("String", "TACHIDESK_SP_VERSION", "0.2.7")
    buildConfigField("String", "TACHIDESK_IM_VERSION", "0")
}

kotlinter {
    experimentalRules = true
    disabledRules = arrayOf("experimental:argument-list-wrapping")
}

kapt {
    includeCompileClasspath = false
}