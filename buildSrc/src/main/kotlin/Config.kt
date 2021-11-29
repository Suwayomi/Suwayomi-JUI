import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.5.4"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1046
    const val preview = true
    const val previewCommit = "3b73a0fd72430fcbb4f45ee2ccca0ca64f9ffb83"

    val jvmTarget = JavaVersion.VERSION_15
}