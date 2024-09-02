plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.moko.gradle.get().pluginId)
}

kotlin {
    androidTarget {
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget = Config.androidJvmTarget
                    }
                }
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget = Config.desktopJvmTarget
                    }
                }
            }
        }
    }
    val configuration: org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.() -> Unit = {
        binaries {
            framework {
                baseName = "i18n"
                isStatic = true
            }
        }
    }
    iosX64(configure = configuration)
    iosArm64(configure = configuration)
    iosSimulatorArm64(configure = configuration)

    applyDefaultHierarchyTemplate()

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(libs.moko.core)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        getByName("iosMain") {
            dependencies {
                implementation(libs.moko.parcelize)
            }
        }
    }
}

tasks {
    registerLocalizationTask(project)
}

multiplatformResources {
    resourcesPackage = "ca.gosyer.jui.i18n"
}

android {
    namespace = "ca.gosyer.jui.i18n"
    lint {
        disable += "MissingTranslation"
    }

    sourceSets.getByName("main") {
        assets.srcDir(File(layout.buildDirectory.asFile.get(), "generated/moko/androidMain/assets"))
        res.srcDir(File(layout.buildDirectory.asFile.get(), "generated/moko/androidMain/res"))
    }
}
