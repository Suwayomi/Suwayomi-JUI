import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.4.6"
    // Bump this when updating the Tachidesk version or Preview commit
    const val serverCode = 2
    const val preview = true
    const val previewCommit = "a76a6d2798f2e8d7268fc66a0bb669d963613daf"

    val jvmTarget = JavaVersion.VERSION_15
}