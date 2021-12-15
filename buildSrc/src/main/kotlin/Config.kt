import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 1

    // Tachidesk
    const val tachideskVersion = "v0.6.0"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1049
    const val preview = true
    const val previewCommit = "63ea28a620cdf6a2fd376304ad4e54e850e4f981"

    val jvmTarget = JavaVersion.VERSION_17
}