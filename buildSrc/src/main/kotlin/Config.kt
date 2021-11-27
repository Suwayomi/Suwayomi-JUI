import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.5.4"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1031
    const val preview = true
    const val previewCommit = "420d14fc37a18269a9d7232519e3f9a21c6302a2"

    val jvmTarget = JavaVersion.VERSION_15
}