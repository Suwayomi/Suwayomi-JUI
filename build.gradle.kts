plugins {
    kotlin("multiplatform") version "1.6.10" apply false
    kotlin("kapt") version "1.6.10" apply false
    kotlin("plugin.serialization") version "1.6.10" apply false
    id("org.jetbrains.compose") version "1.0.1" apply false
    id("com.github.gmazzo.buildconfig") version "3.0.3" apply false
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