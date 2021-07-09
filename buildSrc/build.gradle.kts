plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("de.undercouch:gradle-download-task:4.1.2")
}