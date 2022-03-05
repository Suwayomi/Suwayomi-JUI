import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 1

    // Tachidesk
    const val tachideskVersion = "v0.6.2"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1074
    const val preview = true
    const val previewCommit = "858784857e8b4ba7d2a88a8128bb31dc53673852"

    val desktopJvmTarget = JavaVersion.VERSION_16
    val androidJvmTarget = JavaVersion.VERSION_11

    const val androidDev = false
}