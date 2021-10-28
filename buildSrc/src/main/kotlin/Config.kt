import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.5.4"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1013
    const val preview = true
    const val previewCommit = "1ee37da720abd8d017f3c443f0b7e2dc543ee1ef"

    val jvmTarget = JavaVersion.VERSION_15
}