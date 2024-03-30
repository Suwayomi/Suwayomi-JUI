import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 4

    // Suwayomi-Server version
    const val tachideskVersion = "v1.0.0"
    // Match this to the Suwayomi-Server commit count
    const val serverCode = 1498
    const val preview = false
    const val previewCommit = "54df9d634a1e83143a6cacf6206b6504721b6ca8"

    val desktopJvmTarget = JavaVersion.VERSION_17
    val androidJvmTarget = JavaVersion.VERSION_11
}
