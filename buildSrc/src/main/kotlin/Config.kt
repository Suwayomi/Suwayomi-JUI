import org.gradle.api.JavaVersion

object Config {
    const val migrationCode = 2

    // Tachidesk-Server version
    const val tachideskVersion = "v0.6.4"
    // Match this to the Tachidesk-Server commit count
    const val serverCode = 1118
    const val preview = true
    const val previewCommit = "d989940a4dcdf8d5cbdc2fdfdfc40849117dc85c"

    val desktopJvmTarget = JavaVersion.VERSION_17
    val androidJvmTarget = JavaVersion.VERSION_11
}