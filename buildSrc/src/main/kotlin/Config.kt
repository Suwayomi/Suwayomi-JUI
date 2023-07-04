import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 4

    // Tachidesk-Server version
    const val tachideskVersion = "v0.7.0"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1304
    const val preview = true
    const val previewCommit = "9a80992aec5edfc5293f1fed79d5e34cad14cb74"

    val desktopJvmTarget = JavaVersion.VERSION_17
    val androidJvmTarget = JavaVersion.VERSION_11
}
