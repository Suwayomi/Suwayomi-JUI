import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.4.3"
    const val preview = true
    const val previewCommit = "afabaccf1dff12936edf64e7f40070eca892fdb6"

    val jvmTarget = JavaVersion.VERSION_15
}