import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.4.9"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 878
    const val preview = true
    const val previewCommit = "90be30bddbcb27eaf9a925bb6e8e025f43459ebb"

    val jvmTarget = JavaVersion.VERSION_15
}