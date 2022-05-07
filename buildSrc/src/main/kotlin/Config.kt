import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 2

    // Tachidesk-Server version
    const val tachideskVersion = "v0.6.3"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1100
    const val preview = true
    const val previewCommit = "86f0b3f29f2ea3f986eb40a7d4a5814112097b8e"

    val desktopJvmTarget = JavaVersion.VERSION_17
    val androidJvmTarget = JavaVersion.VERSION_11

    const val androidDev = false
}