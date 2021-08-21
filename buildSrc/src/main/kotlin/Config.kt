import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.4.7"
    // Bump this when updating the Tachidesk version or Preview commit
    const val serverCode = 3
    const val preview = true
    const val previewCommit = "da44d3b2b48357d10e4c28e3b01fed4d2465115c"

    val jvmTarget = JavaVersion.VERSION_15
}