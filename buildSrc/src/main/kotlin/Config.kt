import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 2

    // Tachidesk-Server version
    const val tachideskVersion = "v0.6.5"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1143
    const val preview = true
    const val previewCommit = "2ac5c1362c0c5bb8f39d1049d6f72328102dd182"

    val desktopJvmTarget = JavaVersion.VERSION_17
    val androidJvmTarget = JavaVersion.VERSION_11
}