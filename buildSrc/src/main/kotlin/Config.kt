import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 2

    // Tachidesk-Server version
    const val tachideskVersion = "v0.6.3"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1108
    const val preview = true
    const val previewCommit = "bdf3a7014f45138dec71ee16064755a23a2876ef"

    val desktopJvmTarget = JavaVersion.VERSION_17
    val androidJvmTarget = JavaVersion.VERSION_11

    const val androidDev = false
}