import org.gradle.jvm.tasks.Jar
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("kapt") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.10"
    id("org.jetbrains.compose") version "0.4.0"
    id("de.fuerstenau.buildconfig") version "1.1.8"
    id("org.jmailen.kotlinter") version "3.4.5"
    id("com.github.ben-manes.versions") version "0.39.0"
}

group = "ca.gosyer"
version = "1.1.1"

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    // UI (Compose)
    implementation(compose.desktop.currentOs)
    implementation(compose("org.jetbrains.compose.ui:ui-util"))
    implementation(compose("org.jetbrains.compose.material:material-icons-extended"))
    implementation("ca.gosyer:compose-router:0.24.2-jetbrains-2")
    implementation("ca.gosyer:accompanist-pager:0.9.1")

    // UI (Swing)
    implementation("com.github.weisj:darklaf-core:2.6.1")

    // Threading
    val coroutinesVersion = "1.5.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")

    // Json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")

    // Xml
    val xmlutilVersion = "0.82.0"
    implementation("io.github.pdvrieze.xmlutil:core-jvm:$xmlutilVersion")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:$xmlutilVersion")

    // Dependency Injection
    val toothpickVersion = "3.1.0"
    implementation("com.github.stephanenicolas.toothpick:ktp:$toothpickVersion")
    kapt("com.github.stephanenicolas.toothpick:toothpick-compiler:$toothpickVersion")

    // Http client
    val ktorVersion = "1.6.0"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

    // Logging
    val log4jVersion = "2.14.1"
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.8")

    // User storage
    implementation("net.harawata:appdirs:1.2.1")

    // Preferences
    val multiplatformSettingsVersion = "0.7.7"
    implementation("com.russhwolf:multiplatform-settings-jvm:$multiplatformSettingsVersion")
    implementation("com.russhwolf:multiplatform-settings-serialization-jvm:$multiplatformSettingsVersion")
    implementation("com.russhwolf:multiplatform-settings-coroutines-jvm:$multiplatformSettingsVersion")

    // Utility
    implementation("io.github.kerubistan.kroki:kroki-coroutines:1.21")

    // Testing
    testImplementation(kotlin("test-junit"))
    testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

tasks {
    withType<KotlinCompile> {
        dependsOn(formatKotlinMain)
        kotlinOptions {
            jvmTarget = "15"
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.time.ExperimentalTime",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-Xopt-in=com.russhwolf.settings.ExperimentalSettingsApi",
                "-Xopt-in=com.russhwolf.settings.ExperimentalSettingsImplementation",
                "-Xopt-in=com.google.accompanist.pager.ExperimentalPagerApi",
                "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi",
                "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi"
            )
        }
    }
    test {
        useJUnit()
    }

    withType<Jar> {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }

    withType<LintTask> {
        source(files("src"))
    }

    withType<FormatTask> {
        source(files("src"))
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
                TargetFormat.Dmg
            )
            modules(
                "java.instrument",
                "java.management",
                "java.naming",
                "java.prefs",
                "java.rmi",
                "java.scripting",
                "java.sql",
                "jdk.unsupported"
            )

            packageName = "TachideskJUI"
            description = "TachideskJUI is a Jvm client for a Tachidesk Server"
            copyright = "Mozilla Public License v2.0"
            vendor = "Suwayomi"
            windows {
                dirChooser = true
                upgradeUuid = "B2ED947E-81E4-4258-8388-2B1EDF5E0A30"
                shortcut = true
                menu = true
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
    buildConfigField("String", "TACHIDESK_SP_VERSION", "v0.4.3")
}

kotlinter {
    experimentalRules = true
    disabledRules = arrayOf("experimental:argument-list-wrapping")
}

kapt {
    includeCompileClasspath = false
}