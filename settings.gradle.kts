pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    
}
rootProject.name = "Tachidesk-JUI"

include("desktop")
include("core")
include("i18n")

enableFeaturePreview("VERSION_CATALOGS")

