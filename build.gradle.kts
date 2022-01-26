plugins {
    kotlin("multiplatform") version "1.6.10" apply false
    kotlin("kapt") version "1.6.10" apply false
    kotlin("plugin.serialization") version "1.6.10" apply false
    id("com.android.library") version "7.0.4" apply false
    id("com.android.application") version "7.0.4" apply false
    id("org.jetbrains.compose") version "1.0.1" apply false
    id("com.github.gmazzo.buildconfig") version "3.0.3" apply false
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

subprojects {
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
                //isCoreLibraryDesugaringEnabled = true
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
                //add("coreLibraryDesugaring", Deps.desugarJdkLibs)
            }
        }
    }
}