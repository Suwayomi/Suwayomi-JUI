import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object Config {
    const val migrationCode = 6

    // Suwayomi-Server version
    const val tachideskVersion = "v2.1.1959"
    // Match this to the Suwayomi-Server commit count
    const val serverCode = 1959
    const val preview = true

    val desktopJvmTarget = JvmTarget.JVM_17
    val androidJvmTarget = JvmTarget.JVM_17
}
