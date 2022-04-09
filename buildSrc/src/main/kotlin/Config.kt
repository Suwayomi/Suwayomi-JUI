import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 2

    // Tachidesk-Server version
    const val tachideskVersion = "v0.6.3"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1087
    const val preview = true
    const val previewCommit = "a26b8ecca035fe01fadc55bfb7184774b035fec1"

    val desktopJvmTarget = JavaVersion.VERSION_16
    val androidJvmTarget = JavaVersion.VERSION_11

    const val androidDev = false
}