import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.5.4"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1045
    const val preview = true
    const val previewCommit = "2478aa77cd4a71b0ae7c895fce0358ad7c30614b"

    val jvmTarget = JavaVersion.VERSION_15
}