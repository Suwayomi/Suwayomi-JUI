import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 1

    // Tachidesk
    const val tachideskVersion = "v0.6.0"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1058
    const val preview = true
    const val previewCommit = "b714abddae9f13e91bc53c5daac54aeae564cd2a"

    val desktopJvmTarget = JavaVersion.VERSION_16
    val androidJvmTarget = JavaVersion.VERSION_11

    const val androidDev = false
}