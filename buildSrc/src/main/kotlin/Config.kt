import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.5.4"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 998
    const val preview = true
    const val previewCommit = "7c603258fbcd4571fba7ab56c6101f3cbf425a6b"

    val jvmTarget = JavaVersion.VERSION_15
}