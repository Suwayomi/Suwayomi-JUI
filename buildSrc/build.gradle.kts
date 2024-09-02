plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(gradleApi())
    implementation(localGroovy())
    implementation(libs.gradle.download.task)
    implementation(libs.gradle.kotlin)
    implementation(libs.gradle.android)
}
