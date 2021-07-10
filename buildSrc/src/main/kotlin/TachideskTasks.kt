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
import org.gradle.api.tasks.bundling.Zip

private const val tachideskGroup = "tachidesk"
private const val downloadTask = "downloadTar"
private const val extractTask = "extractTar"
private const val androidScriptTask = "runGetAndroid"
private const val setupCITask = "setupServerCI"
private const val buildTachideskTask = "buildTachidesk"
private const val copyTachideskJarTask = "copyTachidesk"
private const val extractTachideskJar = "extractJar"
private const val signTachideskJar = "signJar"
private const val zipTachideskJar = "zipJar"
private const val deleteTmpFolderTask = "deleteTmp"
private const val runAllTachideskTasks = "setupTachideskJar"

private fun Task.onlyIfTachideskDoesntExist(rootDir: File) {
    onlyIf { !File(rootDir, "src/main/resources/Tachidesk.jar").exists() }
}
private fun Task.onlyIfSigning(project: Project) {
    with (project){
        onlyIf {
            DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
                && isSigning(properties)
                && !File(rootDir, "src/main/resources/Tachidesk.jar").exists()
        }
    }

}

private fun isSigning(properties: Map<String, Any?>) = properties["compose.desktop.mac.sign"].toString() == "true"

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
            val os = DefaultNativePlatform.getCurrentOperatingSystem()
            when {
                os.isMacOsX && isSigning(properties) -> into(File(tmpDir(), "macos/"))
                else -> into(File(rootDir, "src/main/resources/"))
            }
            rename {
                "Tachidesk.jar"
            }
        }
        register<Copy>(extractTachideskJar) {
            group = tachideskGroup
            mustRunAfter(copyTachideskJarTask)
            onlyIfSigning(project)

            from(zipTree(File(tmpDir(), "macos/Tachidesk.jar")))
            into(File(tmpDir(), "macos/jar/"))
        }
        register(signTachideskJar) {
            group = tachideskGroup
            mustRunAfter(extractTachideskJar)
            onlyIfSigning(project)

            doFirst {
                File(tmpDir(), "macos/jar/").walkTopDown()
                    .asSequence()
                    .filter { it.extension.equals("dylib", true) || it.extension.equals("jnilib", true) }
                    .forEach {
                        exec {
                            commandLine(
                                "codesign",
                                "--deep",
                                "-vvv",
                                "-f",
                                "--sign",
                                properties["compose.desktop.mac.signing.identity"],
                                it.absolutePath
                            )
                        }
                    }
            }
        }
        register<Zip>(zipTachideskJar) {
            group = tachideskGroup
            mustRunAfter(signTachideskJar)
            onlyIfSigning(project)

            from(File(tmpDir(), "macos/jar/"))
            archiveBaseName.set("Tachidesk")
            archiveVersion.set("")
            archiveExtension.set("jar")
            destinationDirectory.set(File(rootDir, "src/main/resources/"))
        }
        register<Delete>(deleteTmpFolderTask) {
            mustRunAfter(zipTachideskJar)
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
                extractTachideskJar,
                signTachideskJar,
                zipTachideskJar,
                deleteTmpFolderTask
            )
        }
    }

    named("processResources") {
        mustRunAfter(runAllTachideskTasks)
    }
}