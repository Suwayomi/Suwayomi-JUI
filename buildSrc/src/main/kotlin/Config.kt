import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.4.7"
    // Bump this when updating the Tachidesk version or Preview commit
    const val serverCode = 809
    const val preview = true
    const val previewCommit = "b31f2d50f6ed03aa900986536d90b4aa9a9417d4"

    val jvmTarget = JavaVersion.VERSION_15
}