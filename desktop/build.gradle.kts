import Config.migrationCode
import Config.serverCode
import Config.tachideskVersion
import org.gradle.jvm.tasks.Jar
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import proguard.gradle.ProGuardTask

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
    id("com.github.gmazzo.buildconfig")
    id("org.jmailen.kotlinter")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":i18n"))
    implementation(project(":data"))
    implementation(project(":ui-core"))
    implementation(project(":presentation"))

    // UI (Compose)
    implementation(compose.desktop.currentOs)
    implementation(compose.uiTooling)
    implementation(compose.materialIconsExtended)
    implementation(compose("org.jetbrains.compose.ui:ui-util"))
    implementation(libs.voyagerCore)
    implementation(libs.voyagerNavigation)
    implementation(libs.voyagerTransitions)
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
    implementation(libs.kotlinInjectRuntime)
    ksp(libs.kotlinInjectCompiler)

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

    // Storage
    implementation(libs.okio)
    implementation(libs.appDirs)

    // Preferences
    implementation(libs.multiplatformSettingsCore)
    implementation(libs.multiplatformSettingsSerialization)
    implementation(libs.multiplatformSettingsCoroutines)

    // Utility
    implementation(libs.krokiCoroutines)

    // Localization
    implementation(libs.mokoCore)
    implementation(libs.mokoCompose)

    // Testing
    testImplementation(kotlin("test-junit"))
    testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
    testImplementation(libs.coroutinesTest)
}

java {
    sourceCompatibility = Config.desktopJvmTarget
    targetCompatibility = Config.desktopJvmTarget
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = Config.desktopJvmTarget.toString()
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
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

    registerTachideskTasks(project)

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

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

compose.desktop {
    application {
        mainClass = "ca.gosyer.MainKt"
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
    packageName(project.group.toString() + ".desktop.build")
    useKotlinOutput { internalVisibility = true }

    buildConfigField("String", "NAME", rootProject.name.wrap())
    buildConfigField("String", "VERSION", project.version.toString().wrap())
    buildConfigField("int", "MIGRATION_CODE", migrationCode.toString())
    buildConfigField("boolean", "DEBUG", project.hasProperty("debugApp").toString())
    buildConfigField("boolean", "IS_PREVIEW", project.hasProperty("preview").toString())
    buildConfigField("int", "PREVIEW_BUILD", project.properties["preview"]?.toString()?.trim('"') ?: 0.toString())

    // Tachidesk
    buildConfigField("String", "TACHIDESK_SP_VERSION", tachideskVersion.wrap())
    buildConfigField("int", "SERVER_CODE", serverCode.toString())
}
