import Config.migrationCode
import Config.serverCode
import Config.tachideskVersion
import org.gradle.jvm.tasks.Jar
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask
import proguard.gradle.ProGuardTask

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("com.github.gmazzo.buildconfig")
    id("org.jmailen.kotlinter")
}

dependencies {
    // UI (Compose)
    implementation(compose.desktop.currentOs)
    implementation(compose.uiTooling)
    implementation(compose.materialIconsExtended)
    implementation(compose("org.jetbrains.compose.ui:ui-util"))
    implementation(libs.composeRouter)
    implementation(libs.accompanistPager)
    implementation(libs.accompanistFlowLayout)
    implementation(libs.kamel)

    // UI (Swing)
    implementation(libs.darklaf)

    // Threading
    implementation(libs.coroutinesCore)
    implementation(libs.coroutinesSwing)

    // Json
    implementation(libs.json)

    // Xml
    implementation(libs.xmlUtilCore)
    implementation(libs.xmlUtilSerialization)

    // Dependency Injection
    implementation(libs.toothpickKsp)
    kapt(libs.toothpickCompiler)

    // Http client
    implementation(libs.ktorCore)
    implementation(libs.ktorOkHttp)
    implementation(libs.ktorSerialization)
    implementation(libs.ktorLogging)
    implementation(libs.ktorWebsockets)
    implementation(libs.ktorAuth)

    // Logging
    implementation(libs.slf4jApi)
    implementation(libs.slf4jJul)
    implementation(libs.log4jApi)
    implementation(libs.log4jCore)
    implementation(libs.log4jSlf4j)
    implementation(libs.ktlogging)

    // User storage
    implementation(libs.appDirs)

    // Preferences
    implementation(libs.multiplatformSettingsCore)
    implementation(libs.multiplatformSettingsSerialization)
    implementation(libs.multiplatformSettingsCoroutines)

    // Utility
    implementation(libs.krokiCoroutines)

    // Testing
    testImplementation(kotlin("test-junit"))
    testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
    testImplementation(libs.coroutinesTest)
}

java {
    sourceCompatibility = Config.jvmTarget
    targetCompatibility = Config.jvmTarget
}

tasks {
    withType<KotlinCompile> {
        //dependsOn(formatKotlinMain)
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
    withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
        rejectVersionIf {
            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }
    }
    register<ProGuardTask>("optimizeUberJar") {
        group = "compose desktop"
        val packageUberJarForCurrentOS = getByName("packageUberJarForCurrentOS")
        dependsOn(packageUberJarForCurrentOS)
        val uberJar = packageUberJarForCurrentOS.outputs.files.first()
        injars(uberJar)
        outjars(File(uberJar.parentFile, "min/" + uberJar.name))
        val javaHome = System.getProperty("java.home")
        if (JavaVersion.current().isJava9Compatible) {
            libraryjars("$javaHome/jmods")
        } else {
            libraryjars("$javaHome/lib/rt.jar")
            libraryjars("$javaHome/lib/jce.jar")
        }
        configuration("proguard-rules.pro")
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.contains(it, true) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
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
                iconFile.set(rootProject.file("resources/icon.ico"))
                menuGroup = "Suwayomi"
            }
            macOS {
                bundleID = "ca.gosyer.tachideskjui"
                packageName = rootProject.name
                iconFile.set(rootProject.file("resources/icon.icns"))
            }
            linux {
                iconFile.set(rootProject.file("resources/icon.png"))
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
    buildConfigField("int", "MIGRATION_CODE", migrationCode.toString())
    buildConfigField("boolean", "DEBUG", project.hasProperty("debugApp").toString())
    buildConfigField("boolean", "IS_PREVIEW", project.hasProperty("preview").toString())
    buildConfigField("int", "PREVIEW_BUILD", project.properties["preview"]?.toString()?.trim('"') ?: 0.toString())

    // Tachidesk
    buildConfigField("String", "TACHIDESK_SP_VERSION", tachideskVersion.wrap())
    buildConfigField("int", "SERVER_CODE", serverCode.toString())
}

kotlinter {
    experimentalRules = true
    disabledRules = arrayOf("experimental:argument-list-wrapping", "experimental:trailing-comma")
}

kapt {
    includeCompileClasspath = false
}
