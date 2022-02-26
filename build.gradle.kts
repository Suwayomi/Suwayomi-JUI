import Config.migrationCode
import Config.serverCode
import Config.tachideskVersion
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type

plugins {
    kotlin("multiplatform") version "1.6.10" apply false
    kotlin("plugin.serialization") version "1.6.10" apply false
    id("com.android.library") version "7.0.4" apply false
    id("com.android.application") version "7.0.4" apply false
    id("org.jetbrains.compose") version "1.1.0-alpha03" apply false
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
    id("com.github.gmazzo.buildconfig") version "3.0.3" apply false
    id("com.codingfeline.buildkonfig") version "0.11.0" apply false
    id("dev.icerock.mobile.multiplatform-resources") version "0.18.0" apply false
    id("org.jmailen.kotlinter") version "3.8.0" apply false
    id("com.github.ben-manes.versions") version "0.41.0"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.2.0-beta6") {
            exclude("com.android.tools.build")
        }
    }
}

allprojects {
    group = "ca.gosyer"
    version = "1.2.1"

    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjvm-default=compatibility",
            )
        }
    }
    tasks.withType<org.jmailen.gradle.kotlinter.tasks.LintTask> {
        source(files("src"))
        exclude("ca/gosyer/*/build")
    }
    tasks.withType<org.jmailen.gradle.kotlinter.tasks.FormatTask> {
        source(files("src"))
        exclude("ca/gosyer/*/build")
    }
    plugins.withType<com.android.build.gradle.BasePlugin> {
        configure<com.android.build.gradle.BaseExtension> {
            compileSdkVersion(31)
            defaultConfig {
                minSdk = 21
                targetSdk = 31
                /*versionCode(Config.versionCode)
                versionName(Config.versionName)
                ndk {
                    version = Config.ndk
                }*/
            }
            compileOptions {
                isCoreLibraryDesugaringEnabled = true
                sourceCompatibility(JavaVersion.VERSION_11)
                targetCompatibility(JavaVersion.VERSION_11)
            }
            sourceSets {
                named("main") {
                    val altManifest = file("src/androidMain/AndroidManifest.xml")
                    if (altManifest.exists()) {
                        manifest.srcFile(altManifest.path)
                    }
                }
            }
            dependencies {
                add("coreLibraryDesugaring", libs.desugarJdkLibs)
            }
        }
    }
    plugins.withType<com.codingfeline.buildkonfig.gradle.BuildKonfigPlugin> {
        configure<com.codingfeline.buildkonfig.gradle.BuildKonfigExtension> {
            defaultConfigs {
                buildConfigField(Type.STRING, "NAME", rootProject.name)
                buildConfigField(Type.STRING, "VERSION", project.version.toString())
                buildConfigField(Type.INT, "MIGRATION_CODE", migrationCode.toString())
                buildConfigField(Type.BOOLEAN, "DEBUG", project.hasProperty("debugApp").toString())
                buildConfigField(Type.BOOLEAN, "IS_PREVIEW", project.hasProperty("preview").toString())
                buildConfigField(Type.INT, "PREVIEW_BUILD", project.properties["preview"]?.toString()?.trim('"') ?: 0.toString())

                // Tachidesk
                buildConfigField(Type.STRING, "TACHIDESK_SP_VERSION", tachideskVersion)
                buildConfigField(Type.INT, "SERVER_CODE", serverCode.toString())
            }
        }
    }
    plugins.withType<org.jmailen.gradle.kotlinter.KotlinterPlugin> {
        configure<org.jmailen.gradle.kotlinter.KotlinterExtension> {
            experimentalRules = true
            disabledRules = arrayOf("experimental:argument-list-wrapping", "experimental:trailing-comma")
        }
    }

    plugins.withType<com.google.devtools.ksp.gradle.KspGradleSubplugin> {
        configure<com.google.devtools.ksp.gradle.KspExtension> {
            arg("me.tatarka.inject.generateCompanionExtensions", "true")
            if (project.hasProperty("debugApp")) {
                arg("me.tatarka.inject.dumpGraph", "true")
            }
        }
    }

    plugins.withType<JacocoPlugin> {
        configure<JacocoPluginExtension> {
            toolVersion = "0.8.7"
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.contains(it, true) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
