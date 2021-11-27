import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.5.4"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1043
    const val preview = true
    const val previewCommit = "5e47b7ae6b37931ce3a8eee33cafb9475d7a77bb"

    val jvmTarget = JavaVersion.VERSION_15
}