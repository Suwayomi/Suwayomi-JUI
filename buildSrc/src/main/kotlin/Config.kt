import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.4.9"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 875
    const val preview = true
    const val previewCommit = "b05b817aebdb512b73765940645b4db7763a8909"

    val jvmTarget = JavaVersion.VERSION_15
}