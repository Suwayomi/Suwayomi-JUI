import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 3

    // Tachidesk-Server version
    const val tachideskVersion = "v0.7.0"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1198
    const val preview = true
    const val previewCommit = "d4e71274f94a066309cb4881042cf4673075a5d0"

    val desktopJvmTarget = JavaVersion.VERSION_17
    val androidJvmTarget = JavaVersion.VERSION_11
}