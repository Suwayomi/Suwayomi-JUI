import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.5.2"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 967
    const val preview = true
    const val previewCommit = "9b67f2c58fac5c4725414e29a360c2d3d7ed85f3"

    val jvmTarget = JavaVersion.VERSION_15
}