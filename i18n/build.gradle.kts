@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.moko.gradle.get().pluginId)
}

kotlin {
    android {
        compilations {
            all {
                kotlinOptions.jvmTarget = Config.androidJvmTarget.toString()
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                kotlinOptions.jvmTarget = Config.desktopJvmTarget.toString()
            }
        }
    }
    val configuration: org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.() -> Unit = {
        binaries {
            framework {
                baseName = "i18n"
            }
        }
    }
    iosX64(configure = configuration)
    iosArm64(configure = configuration)
    iosSimulatorArm64(configure = configuration)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(libs.moko.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

tasks {
    registerLocalizationTask(project)

    getByName("desktopProcessResources")
        .dependsOn("generateMRcommonMain", "generateMRdesktopMain")
}

multiplatformResources {
    multiplatformResourcesPackage = "ca.gosyer.jui.i18n"
}

android {
    namespace = "ca.gosyer.jui.i18n"
    lint {
        disable += "MissingTranslation"
    }

    sourceSets.getByName("main") {
        assets.srcDir(File(buildDir, "generated/moko/androidMain/assets"))
        res.srcDir(File(buildDir, "generated/moko/androidMain/res"))
    }
}
