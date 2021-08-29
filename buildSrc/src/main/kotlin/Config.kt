import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.4.7"
    // Bump this when updating the Tachidesk version or Preview commit
    const val serverCode = 842
    const val preview = true
    const val previewCommit = "a8ef6cdd4f10eaf0d265fc8a422fdd9bc4a28cfd"

    val jvmTarget = JavaVersion.VERSION_15
}