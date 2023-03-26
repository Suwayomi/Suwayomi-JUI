plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("de.undercouch:gradle-download-task:5.3.0")
}
