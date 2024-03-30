pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
rootProject.name = "Suwayomi-JUI"

include("core")
include("i18n")
include("data")
include("domain")
include("ui-core")
include("presentation")
include("android")
include("desktop")
include("ios")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
