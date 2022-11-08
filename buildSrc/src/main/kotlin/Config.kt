import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 2

    // Tachidesk-Server version
    const val tachideskVersion = "v0.6.5"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1148
    const val preview = true
    const val previewCommit = "2195c3df765c3e1e435595d9edbec8ad3590bf46"

    val desktopJvmTarget = JavaVersion.VERSION_17
    val androidJvmTarget = JavaVersion.VERSION_11
}