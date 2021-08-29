import Config.serverCode
import Config.tachideskVersion
import org.gradle.jvm.tasks.Jar
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("kapt") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    id("org.jetbrains.compose") version "1.0.0-alpha4-build328"
    id("com.github.gmazzo.buildconfig") version "3.0.2"
    id("org.jmailen.kotlinter") version "3.5.0"
    id("com.github.ben-manes.versions") version "0.39.0"
}

group = "ca.gosyer"
version = "1.1.3"

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    // UI (Compose)
    implementation(compose.desktop.currentOs)
    implementation(compose.uiTooling)
    implementation(compose.materialIconsExtended)
    implementation(compose("org.jetbrains.compose.ui:ui-util"))
    implementation("ca.gosyer:compose-router:0.24.2-jetbrains-2")
    implementation("ca.gosyer:accompanist-pager:0.14.0")

    // UI (Swing)
    implementation("com.github.weisj:darklaf-core:2.7.2")

    // Threading
    val coroutinesVersion = "1.5.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")

    // Json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    // Xml
    val xmlutilVersion = "0.82.0"
    implementation("io.github.pdvrieze.xmlutil:core-jvm:$xmlutilVersion")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:$xmlutilVersion")

    // Dependency Injection
    val toothpickVersion = "3.1.0"
    implementation("com.github.stephanenicolas.toothpick:ktp:$toothpickVersion")
    kapt("com.github.stephanenicolas.toothpick:toothpick-compiler:$toothpickVersion")

    // Http client
    val ktorVersion = "1.6.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")

    // Logging
    val slf4jVersion = "1.7.32"
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:jul-to-slf4j:$slf4jVersion")
    val log4jVersion = "2.14.1"
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")

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
    sourceCompatibility = Config.jvmTarget
    targetCompatibility = Config.jvmTarget
}

tasks {
    withType<KotlinCompile> {
        dependsOn(formatKotlinMain)
        kotlinOptions {
            jvmTarget = Config.jvmTarget.toString()
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
                "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi",
                "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi"
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
        exclude("ca/gosyer/build")
    }

    withType<FormatTask> {
        source(files("src"))
        exclude("ca/gosyer/build")
    }

    registerTachideskTasks(project)

    task("generateResourceConstants") {
        val buildResources = buildConfig.forClass(project.group.toString()+ ".build", "BuildResources")

        doFirst {
            val langs = listOf("en") + sourceSets["main"].resources
                .filter { it.name == "strings.xml" }
                .drop(1)
                .map { it.absolutePath.substringAfter("values-").substringBefore(File.separatorChar) }
            buildResources.buildConfigField("Array<String>", "LANGUAGES", langs.joinToString(prefix = "arrayOf(", postfix = ")") { it.wrap() })
        }

        generateBuildConfig {
            dependsOn(this@task)
        }
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
                "java.compiler",
                "java.instrument",
                "java.management",
                "java.naming",
                "java.prefs",
                "java.rmi",
                "java.scripting",
                "java.sql",
                "jdk.crypto.ec",
                "jdk.unsupported"
            )

            packageName = "Tachidesk-JUI"
            description = "Tachidesk-JUI is a Jvm client for a Tachidesk Server"
            copyright = "Mozilla Public License v2.0"
            vendor = "Suwayomi"
            windows {
                dirChooser = true
                upgradeUuid = "B2ED947E-81E4-4258-8388-2B1EDF5E0A30"
                shortcut = true
                menu = true
                iconFile.set(project.file("resources/icon.ico"))
            }
            macOS {
                bundleID = "ca.gosyer.tachideskjui"
                packageName = rootProject.name
                iconFile.set(project.file("resources/icon.icns"))
            }
            linux {
                iconFile.set(project.file("resources/icon.png"))
            }
        }
    }
}

fun String.wrap() = """"$this""""
buildConfig {
    className("BuildConfig")
    packageName(project.group.toString() + ".build")
    useKotlinOutput { internalVisibility = true }

    buildConfigField("String", "NAME", project.name.wrap())
    buildConfigField("String", "VERSION", project.version.toString().wrap())

    buildConfigField("boolean", "DEBUG", project.hasProperty("debugApp").toString())
    buildConfigField("String", "TACHIDESK_SP_VERSION", tachideskVersion.wrap())
    buildConfigField("int", "SERVER_CODE", serverCode.toString())
}

kotlinter {
    experimentalRules = true
    disabledRules = arrayOf("experimental:argument-list-wrapping")
}

kapt {
    includeCompileClasspath = false
}