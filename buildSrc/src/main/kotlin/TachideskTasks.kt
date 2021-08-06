import Config.preview
import Config.previewCommit
import Config.tachideskVersion
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.KotlinClosure1
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.register
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.io.File
import java.util.jar.JarFile

private const val tachideskGroup = "tachidesk"
private const val deleteOldTachideskTask = "deleteOldTachidesk"
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
    with(project){
        onlyIf {
            DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
                && isSigning(properties)
                && !File(rootDir, "src/main/resources/Tachidesk.jar").exists()
        }
    }
}

private fun Project.getSigningIdentity() = "${properties["compose.desktop.mac.signing.identity"]}".trim('"')

private fun isSigning(properties: Map<String, Any?>) = properties["compose.desktop.mac.sign"].toString() == "true"

private fun Project.tmpDir() = File(rootDir, "tmp")

private val fileSuffix get() = if (preview) {
    previewCommit
} else {
    tachideskVersion.drop(1)
}

fun TaskContainerScope.registerTachideskTasks(project: Project) {
    with(project) {
        register<Delete>(deleteOldTachideskTask) {
            group = tachideskGroup
            val tachideskJar = File(rootDir, "src/main/resources/Tachidesk.jar")
            onlyIf {
                tachideskJar.exists() && JarFile(tachideskJar).use {
                    it.manifest?.mainAttributes?.getValue("Specification-Version") != tachideskVersion
                }
            }
            delete(tachideskJar)
        }

        register<Download>(downloadTask) {
            group = tachideskGroup
            mustRunAfter(deleteOldTachideskTask)
            onlyIfTachideskDoesntExist(rootDir)

            val tmpDir = tmpDir()
            src(
                if (preview) {
                    "https://github.com/Suwayomi/Tachidesk/archive/$previewCommit.tar.gz"
                } else {
                    "https://github.com/Suwayomi/Tachidesk/archive/refs/tags/$tachideskVersion.tar.gz"
                }
            )

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

            val workingDir = File(tmpDir(), "Tachidesk-$fileSuffix/")
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

            val tachideskDir = File(tmpDir(), "Tachidesk-$fileSuffix/")
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

            workingDir(File(tmpDir(), "Tachidesk-$fileSuffix/"))
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

            from(File(tmpDir(), "Tachidesk-$fileSuffix/server/build/"))
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
                                "-vvvv",
                                "--deep",
                                "--timestamp",
                                "--options", "runtime",
                                "--force",
                                "--prefix", "ca.gosyer.",
                                "--sign", "Developer ID Application: ${getSigningIdentity()}",
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
                deleteOldTachideskTask,
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
        dependsOn(runAllTachideskTasks)
    }
}