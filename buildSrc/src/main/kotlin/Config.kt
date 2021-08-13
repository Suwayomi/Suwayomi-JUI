import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.4.5"
    const val preview = true
    const val previewCommit = "9fa17f617e497efa437f652cc7ee8139dffe2b91"

    val jvmTarget = JavaVersion.VERSION_15
}