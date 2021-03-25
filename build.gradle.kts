import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.serialization") version "1.4.31"
    id("org.jetbrains.compose") version "0.4.0-build174"
}

group = "ca.gosyer"
version = "1.0.0"

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    // UI (Compose)
    implementation(compose.desktop.currentOs)

    // Threading
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

    // Json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

    // Dependency Injection
    implementation("io.insert-koin:koin-core-ext:3.0.1-beta-1")

    // Http client
    val ktorVersion = "1.5.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.2.3")
    //implementation("org.fusesource.jansi:jansi:1.18")
    implementation("io.github.microutils:kotlin-logging:2.0.5")

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
}


compose.desktop {
    application {
        mainClass = "ca.gosyer.ui.main.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TachideskJUI"
        }
    }
}