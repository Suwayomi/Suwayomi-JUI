import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("dev.icerock.mobile.multiplatform-resources")
    id("org.jetbrains.compose")
}

kotlin {
    android()
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                compileOnly(compose.runtime)
                compileOnly(compose.ui)
                api(libs.mokoCore)
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

multiplatformResources {
    multiplatformResourcesPackage = "ca.gosyer.i18n"
}

android {
    lint {
        disable += "MissingTranslation"
    }
}