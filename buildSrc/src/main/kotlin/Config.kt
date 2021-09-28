import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.5.3"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 976
    const val preview = true
    const val previewCommit = "2cbee62f0a8fa0a431bba290e5602970687ba3b7"

    val jvmTarget = JavaVersion.VERSION_15
}