import Config.migrationCode
import Config.serverCode
import Config.tachideskVersion
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.moko.gradle) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.ktorfit) apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.versions)
    id("com.louiscad.complete-kotlin") version "1.1.0"
}

allprojects {
    group = "ca.gosyer"
    version = "1.3.2"

    dependencies {
        modules {
            module("androidx.lifecycle:lifecycle-viewmodel-ktx") {
                replacedBy("androidx.lifecycle:lifecycle-viewmodel")
            }
        }
    }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

subprojects {
    tasks.withType<KotlinJvmCompile> {
        kotlinOptions {
            if (name.contains("android", true)) {
                jvmTarget = Config.androidJvmTarget.toString()
            }
            if (project.hasProperty("generateComposeCompilerMetrics")) {
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                            project.buildDir.absolutePath + "/compose_metrics",
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                            project.buildDir.absolutePath + "/compose_metrics"
                )
            }
        }
    }
    tasks.withType<org.jmailen.gradle.kotlinter.tasks.LintTask> {
        source(files("src"))
        exclude("ca/gosyer/jui/*/build")
    }
    tasks.withType<org.jmailen.gradle.kotlinter.tasks.FormatTask> {
        source(files("src"))
        exclude("ca/gosyer/jui/*/build")
    }
    plugins.withType<com.android.build.gradle.BasePlugin> {
        configure<com.android.build.gradle.BaseExtension> {
            compileSdkVersion(33)
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
                sourceCompatibility(Config.androidJvmTarget)
                targetCompatibility(Config.androidJvmTarget)
            }
            dependencies {
                add("coreLibraryDesugaring", libs.desugarJdkLibs)
            }
            buildFeatures.apply {
                aidl = false
                renderScript = false
                shaders = false
            }
        }
    }
    plugins.withType<com.codingfeline.buildkonfig.gradle.BuildKonfigPlugin> {
        configure<com.codingfeline.buildkonfig.gradle.BuildKonfigExtension> {
            defaultConfigs {
                buildConfigField(Type.STRING, "NAME", rootProject.name, const = true)
                buildConfigField(Type.STRING, "VERSION", project.version.toString(), const = true)
                buildConfigField(Type.INT, "MIGRATION_CODE", migrationCode.toString(), const = true)
                buildConfigField(Type.BOOLEAN, "DEBUG", project.hasProperty("debugApp").toString(), const = true)
                plugins.withType<com.android.build.gradle.BasePlugin> {
                    buildConfigField(Type.BOOLEAN, "DEBUG", gradle.startParameter.taskRequests.toString().contains("Debug").toString(), const = true)
                }
                buildConfigField(Type.BOOLEAN, "IS_PREVIEW", project.hasProperty("preview").toString(), const = true)
                buildConfigField(Type.INT, "PREVIEW_BUILD", project.properties["preview"]?.toString()?.trim('"') ?: 0.toString(), const = true)

                // Tachidesk
                buildConfigField(Type.STRING, "TACHIDESK_SP_VERSION", tachideskVersion, const = true)
                buildConfigField(Type.INT, "SERVER_CODE", serverCode.toString(), const = true)
            }
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

    plugins.withType<de.jensklingenberg.ktorfit.gradle.KtorfitGradleSubPlugin> {
        configure<de.jensklingenberg.ktorfit.gradle.KtorfitGradleConfiguration> {
            version = libs.versions.ktorfit.get()
            logging = project.hasProperty("debugApp")
        }
    }

    plugins.withType<JacocoPlugin> {
        configure<JacocoPluginExtension> {
            toolVersion = "0.8.7"
        }
    }
    plugins.withType<ComposePlugin> {
        configure<ComposeExtension> {
            kotlinCompilerPlugin.set(libs.versions.composeCompiler.get())
        }
    }
    afterEvaluate {
        extensions.findByType<KotlinMultiplatformExtension>()?.let { ext ->
            ext.sourceSets.removeAll { sourceSet ->
                setOf(
                    "androidAndroidTestRelease",
                    "androidTestFixtures",
                    "androidTestFixturesDebug",
                    "androidTestFixturesRelease",
                ).contains(sourceSet.name)
            }
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.contains(it, true) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
