import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.KotlinClosure1
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.register
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.io.File
import Config.tachideskVersion
import org.gradle.api.Project
import org.gradle.api.Task

private const val tachideskGroup = "tachidesk"
private const val downloadTask = "downloadTar"
private const val extractTask = "extractTar"
private const val androidScriptTask = "runGetAndroid"
private const val setupCITask = "setupServerCI"
private const val buildTachideskTask = "buildTachidesk"
private const val copyTachideskJarTask = "copyTachidesk"
private const val deleteTmpFolderTask = "deleteTmp"
private const val runAllTachideskTasks = "setupTachideskJar"

private fun Task.onlyIfTachideskDoesntExist(rootDir: File) {
    onlyIf { !File(rootDir, "src/main/resources/Tachidesk.jar").exists() }
}

private fun Project.tmpDir() = File(rootDir, "tmp")

fun TaskContainerScope.registerTachideskTasks(project: Project) {
    with(project) {
        register<Download>(downloadTask) {
            group = tachideskGroup
            onlyIfTachideskDoesntExist(rootDir)

            val tmpDir = tmpDir()
            src("https://github.com/Suwayomi/Tachidesk/archive/refs/tags/$tachideskVersion.tar.gz")

            dest(
                KotlinClosure1<Any?, File>(
                    {
                        File(tmpDir.also { it.mkdir() }, "Tachidesk.tar.gz")
                    },
                    this,
                    this
                )
            )
        }
        register<Copy>(extractTask) {
            group = tachideskGroup
            mustRunAfter(downloadTask)
            onlyIfTachideskDoesntExist(rootDir)

            from(tarTree(File(rootDir, "tmp/Tachidesk.tar.gz")))
            into(tmpDir())
        }
        register<Exec>(androidScriptTask) {
            group = tachideskGroup
            mustRunAfter(extractTask)
            onlyIfTachideskDoesntExist(rootDir)

            val workingDir = File(tmpDir(), "Tachidesk-${tachideskVersion.drop(1)}/")
            val getAndroidScript = File(workingDir, "AndroidCompat/getAndroid").absolutePath
            workingDir(workingDir)
            val os = DefaultNativePlatform.getCurrentOperatingSystem()
            when {
                os.isWindows -> commandLine("cmd", "/c", """Powershell -File "$getAndroidScript.ps1"""")
                os.isLinux || os.isMacOsX -> commandLine("$getAndroidScript.sh")
            }
        }
        register<Copy>(setupCITask) {
            group = tachideskGroup
            mustRunAfter(androidScriptTask)
            onlyIfTachideskDoesntExist(rootDir)

            val tachideskDir = File(tmpDir(), "Tachidesk-${tachideskVersion.drop(1)}/")
            from(File(tachideskDir, ".github/runner-files/ci-gradle.properties"))
            into(File(tachideskDir, ".gradle/"))
            rename {
                it.replace("ci-", "")
            }
        }
        register<Exec>(buildTachideskTask) {
            group = tachideskGroup
            mustRunAfter(setupCITask)
            onlyIfTachideskDoesntExist(rootDir)

            workingDir(File(tmpDir(), "Tachidesk-${tachideskVersion.drop(1)}/"))
            val os = DefaultNativePlatform.getCurrentOperatingSystem()
            when {
                os.isWindows -> commandLine("cmd", "/c", "gradlew", ":server:shadowJar")
                os.isLinux || os.isMacOsX -> commandLine("./gradlew", ":server:shadowJar")
            }
        }
        register<Copy>(copyTachideskJarTask) {
            group = tachideskGroup
            mustRunAfter(buildTachideskTask)
            onlyIfTachideskDoesntExist(rootDir)

            from(File(tmpDir(), "Tachidesk-${tachideskVersion.drop(1)}/server/build/"))
            include("Tachidesk-$tachideskVersion-r*.jar")
            into(File(rootDir, "src/main/resources/"))
            rename {
                "Tachidesk.jar"
            }
        }
        register<Delete>(deleteTmpFolderTask) {
            mustRunAfter(copyTachideskJarTask)
            delete(tmpDir())
        }
        register(runAllTachideskTasks) {
            group = tachideskGroup

            dependsOn(
                downloadTask,
                extractTask,
                androidScriptTask,
                setupCITask,
                buildTachideskTask,
                copyTachideskJarTask,
                deleteTmpFolderTask
            )
        }
    }

    named("processResources") {
        mustRunAfter(runAllTachideskTasks)
    }
}