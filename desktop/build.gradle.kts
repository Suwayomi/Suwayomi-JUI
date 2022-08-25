import Config.migrationCode
import Config.serverCode
import Config.tachideskVersion
import org.gradle.jvm.tasks.Jar
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import proguard.gradle.ProGuardTask

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.compose.get().pluginId)
    id(libs.plugins.buildconfig.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
    id(libs.plugins.aboutLibraries.get().pluginId)
}

dependencies {
    implementation(projects.core)
    implementation(projects.i18n)
    implementation(projects.domain)
    implementation(projects.data)
    implementation(projects.uiCore)
    implementation(projects.presentation)

    // UI (Compose)
    implementation(compose.desktop.currentOs)
    implementation(compose.uiTooling)
    implementation(compose.materialIconsExtended)
    implementation(compose("org.jetbrains.compose.ui:ui-util"))
    implementation(libs.voyager.core)
    implementation(libs.voyager.navigation)
    implementation(libs.voyager.transitions)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pagerIndicators)
    implementation(libs.accompanist.flowLayout)
    implementation(libs.kamel)
    implementation(libs.imageloader)
    implementation(libs.materialDialogs.core)

    // UI (Swing)
    implementation(libs.darklaf)

    // Threading
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.swing)

    // Json
    implementation(libs.serialization.json.core)
    implementation(libs.serialization.json.okio)

    // Dependency Injection
    implementation(libs.kotlinInject.runtime)
    ksp(libs.kotlinInject.compiler)

    // Http client
    implementation(libs.ktor.core)
    implementation(libs.ktor.okHttp)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.websockets)
    implementation(libs.ktor.auth)

    // Logging
    implementation(libs.logging.slf4j.api)
    implementation(libs.logging.slf4j.jul)
    implementation(libs.logging.log4j.api)
    implementation(libs.logging.log4j.core)
    implementation(libs.logging.log4j.slf4j)
    implementation(libs.logging.kmlogging)

    // Storage
    implementation(libs.okio)
    implementation(libs.appDirs)

    // Preferences
    implementation(libs.multiplatformSettings.core)
    implementation(libs.multiplatformSettings.serialization)
    implementation(libs.multiplatformSettings.coroutines)

    // Utility
    implementation(libs.krokiCoroutines)
    implementation(libs.dateTime)

    // Localization
    implementation(libs.moko.core)
    implementation(libs.moko.compose)

    // Testing
    testImplementation(kotlin("test-junit"))
    testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
    testImplementation(libs.coroutines.test)
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
                "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
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

val isPreview: Boolean
    get() = project.hasProperty("preview")
val previewCode: String
    get() = project.properties["preview"]?.toString()?.trim('"') ?: 0.toString()

compose.desktop {
    application {
        mainClass = "ca.gosyer.jui.desktop.MainKt"
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

            packageName = if (!isPreview) {
                "Tachidesk-JUI"
            } else {
                "Tachidesk-JUI Preview"
            }
            description = "Tachidesk-JUI is a Jvm client for a Tachidesk Server"
            copyright = "Mozilla Public License v2.0"
            vendor = "Suwayomi"
            if (isPreview) {
                packageVersion = "${version.toString().substringBeforeLast('.')}.$previewCode"
            }
            windows {
                dirChooser = true
                upgradeUuid = if (!isPreview) {
                    "B2ED947E-81E4-4258-8388-2B1EDF5E0A30"
                } else {
                    "7869504A-DB4D-45E8-AC6C-60C0360EA2F0"
                }
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
    packageName(project.group.toString() + ".jui.desktop.build")
    useKotlinOutput { internalVisibility = true }

    buildConfigField("String", "NAME", rootProject.name.wrap())
    buildConfigField("String", "VERSION", project.version.toString().wrap())
    buildConfigField("int", "MIGRATION_CODE", migrationCode.toString())
    buildConfigField("boolean", "DEBUG", project.hasProperty("debugApp").toString())
    buildConfigField("boolean", "IS_PREVIEW", isPreview.toString())
    buildConfigField("int", "PREVIEW_BUILD", previewCode)

    // Tachidesk
    buildConfigField("String", "TACHIDESK_SP_VERSION", tachideskVersion.wrap())
    buildConfigField("int", "SERVER_CODE", serverCode.toString())
}
