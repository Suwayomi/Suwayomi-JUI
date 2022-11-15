import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 2

    // Tachidesk-Server version
    const val tachideskVersion = "v0.6.5"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1156
    const val preview = true
    const val previewCommit = "67e09e2e1d452e041c46a334f1b473f38c5fc25b"

    val desktopJvmTarget = JavaVersion.VERSION_17
    val androidJvmTarget = JavaVersion.VERSION_11
}