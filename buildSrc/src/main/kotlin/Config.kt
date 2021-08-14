import org.gradle.api.JavaVersion

object Config {
    const val tachideskVersion = "v0.4.5"
    const val serverCode = 1
    const val preview = true
    const val previewCommit = "8a986383fe0550d190090f14a9ac2fa0ddace448"

    val jvmTarget = JavaVersion.VERSION_15
}