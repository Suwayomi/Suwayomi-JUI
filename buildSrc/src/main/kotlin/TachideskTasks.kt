import Config.preview
import Config.previewCommit
import Config.serverCode
import Config.tachideskVersion
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.register
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest
import kotlin.streams.asSequence

private const val tachideskGroup = "tachidesk"
private const val deleteOldTachideskTask = "deleteOldTachidesk"
private const val downloadTask = "downloadTar"
private const val extractTask = "extractTar"
private const val setupCITask = "setupServerCI"
private const val buildTachideskTask = "buildTachidesk"
private const val copyTachideskJarTask = "copyTachidesk"
private const val signTachideskJar = "signJar"
private const val modifyTachideskJarManifest = "modifyManifest"
private const val deleteTmpFolderTask = "deleteTmp"
private const val runAllTachideskTasks = "setupTachideskJar"

fun Any?.anyEquals(vararg others: Any?): Boolean {
    return others.any { this == it }
}

fun String?.anyEquals(vararg others: String?, ignoreCase: Boolean = false): Boolean {
    return others.any { this.equals(it, ignoreCase) }
}

private fun tachideskExists(rootDir: File) = File(rootDir, "src/main/resources/Tachidesk.jar").exists()

private fun Task.onlyIfTachideskDoesntExist(rootDir: File) {
    onlyIf { !tachideskExists(rootDir) }
}
private fun Task.onlyIfSigning(project: Project) {
    with(project){
        onlyIf {
            DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
                && isSigning(properties)
                && project.file(finalJar).exists()
        }
    }
}

private fun Project.getSigningIdentity() = "${properties["identity"]}".trim('"')

private fun isSigning(properties: Map<String, Any?>) = properties["compose.desktop.mac.sign"].toString() == "true"

private const val tmpPath = "tmp"
private val tarUrl = if (preview) {
    "https://github.com/Suwayomi/Tachidesk-Server/archive/$previewCommit.tar.gz"
} else {
    "https://github.com/Suwayomi/Tachidesk-Server/archive/refs/tags/$tachideskVersion.tar.gz"
}
private const val tmpTar = "$tmpPath/Tachidesk-Server.tar.gz"
private val fileSuffix get() = if (preview) {
    previewCommit
} else {
    tachideskVersion.drop(1)
}
private val tmpServerFolder = "$tmpPath/Tachidesk-Server-$fileSuffix/"
private const val macosFolder = "$tmpPath/macos/"
private const val macosJarFolder = "$tmpPath/macos/jar/"
private const val destination = "src/main/resources/"
private const val finalJar = "src/main/resources/Tachidesk.jar"

fun TaskContainerScope.registerTachideskTasks(project: Project) {
    with(project) {
        register<Delete>(deleteOldTachideskTask) {
            group = tachideskGroup
            val tachideskJar = file(finalJar)
            onlyIf {
                tachideskJar.exists() && JarFile(tachideskJar).use { jar ->
                    jar.manifest?.mainAttributes?.getValue(Attributes.Name.IMPLEMENTATION_VERSION)?.toIntOrNull() != serverCode
                }
            }
            delete(tachideskJar)
        }

        register<Download>(downloadTask) {
            group = tachideskGroup
            mustRunAfter(deleteOldTachideskTask)
            onlyIf { !tachideskExists(rootDir) && !file(tmpTar).exists() }

            onlyIfTachideskDoesntExist(rootDir)

            src(tarUrl)
            dest(tmpTar)
        }
        register<Copy>(extractTask) {
            group = tachideskGroup
            mustRunAfter(downloadTask)
            onlyIf { !tachideskExists(rootDir) && !file(tmpServerFolder).exists() }

            onlyIfTachideskDoesntExist(rootDir)

            from(tarTree(tmpTar))
            into(tmpPath)
        }
        register<Copy>(setupCITask) {
            group = tachideskGroup
            mustRunAfter(extractTask)
            onlyIfTachideskDoesntExist(rootDir)

            from(file("$tmpServerFolder.github/runner-files/ci-gradle.properties"))
            into(file("$tmpServerFolder.gradle/"))
            rename {
                it.replace("ci-", "")
            }
        }
        register<Exec>(buildTachideskTask) {
            group = tachideskGroup
            mustRunAfter(setupCITask)
            onlyIfTachideskDoesntExist(rootDir)

            workingDir(tmpServerFolder)
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

            from("${tmpServerFolder}server/build/")
            include("Tachidesk-Server-$tachideskVersion-r*.jar")
            into(destination)
            rename {
                "Tachidesk.jar"
            }
        }
        register(signTachideskJar) {
            group = tachideskGroup
            mustRunAfter(copyTachideskJarTask)
            onlyIfSigning(project)

            doFirst {
                FileSystems.newFileSystem(file(finalJar).toPath()).use { fs ->
                    val macJarFolder = file(macosJarFolder).also { it.mkdirs() }.toPath()
                    Files.walk(fs.getPath("/"))
                        .asSequence()
                        .filter {
                            !Files.isDirectory(it) && it.toString()
                                .substringAfterLast('.')
                                .anyEquals("dylib", "jnilib", ignoreCase = true)
                        }
                        .forEach {
                            val tmpFile = macJarFolder.resolve(it.fileName.toString())
                            Files.copy(it, tmpFile)
                            exec {
                                commandLine(
                                    "/usr/bin/codesign",
                                    "-vvvv",
                                    "--timestamp",
                                    "--options", "runtime",
                                    "--force",
                                    "--prefix", "ca.gosyer.",
                                    "--sign", "Developer ID Application: ${getSigningIdentity()}",
                                    tmpFile.toAbsolutePath().toString()
                                )
                            }
                            Files.copy(tmpFile, it, StandardCopyOption.REPLACE_EXISTING)
                        }

                }

            }
        }
        register<Task>(modifyTachideskJarManifest) {
            group = tachideskGroup
            mustRunAfter(signTachideskJar)
            val tachideskJar = file(finalJar)
            onlyIf {
                tachideskJar.exists() && JarFile(tachideskJar).use { jar ->
                    jar.manifest?.mainAttributes?.getValue(Attributes.Name.IMPLEMENTATION_VERSION)?.toIntOrNull() != serverCode
                }
            }

            doFirst {
                val manifest = JarFile(tachideskJar).use { jar ->
                    Manifest(jar.manifest)
                }
                manifest.mainAttributes[Attributes.Name.IMPLEMENTATION_VERSION] = serverCode.toString()
                FileSystems.newFileSystem(tachideskJar.toPath()).use { fs ->
                    val manifestFile = fs.getPath("META-INF/MANIFEST.MF")
                    val manifestBak = fs.getPath("META-INF/MANIFEST.MF" + ".bak")
                    Files.deleteIfExists(manifestBak)
                    Files.move(manifestFile, manifestBak)
                    Files.newOutputStream(manifestFile).use {
                        manifest.write(it)
                    }
                }
            }
        }
        register<Delete>(deleteTmpFolderTask) {
            mustRunAfter(modifyTachideskJarManifest)
            delete(tmpPath)
            onlyIf { file(tmpPath).let { it.exists() && it.canWrite() } }

            doFirst {
                require(file(finalJar).exists()) { "Tachidesk.jar does not exist" }
            }
        }
        register(runAllTachideskTasks) {
            group = tachideskGroup

            dependsOn(
                deleteOldTachideskTask,
                downloadTask,
                extractTask,
                setupCITask,
                buildTachideskTask,
                copyTachideskJarTask,
                signTachideskJar,
                modifyTachideskJarManifest,
                deleteTmpFolderTask
            )
        }
    }

    named("processResources") {
        dependsOn(runAllTachideskTasks)
    }
}