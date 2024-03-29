import Config.preview
import Config.previewCommit
import Config.serverCode
import Config.tachideskVersion
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Delete
import org.gradle.internal.impldep.com.google.gson.Gson
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
private const val downloadApiTask = "downloadApiJson"
private const val downloadTachidesk = "downloadTachidesk"
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

private fun tachideskExists(projectDir: File) = File(projectDir, "src/main/resources/Tachidesk.jar").exists()

private fun Task.onlyIfTachideskDoesntExist(projectDir: File) {
    onlyIf { !tachideskExists(projectDir) }
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

private fun Project.getSigningIdentity() = "${properties["compose.desktop.mac.signing.identity"].toString().trim('"','\'')} (${properties["compose.desktop.mac.notarization.teamID"].toString().trim('"','\'')})"

private fun isSigning(properties: Map<String, Any?>) = properties["compose.desktop.mac.sign"].toString() == "true"

private const val tmpPath = "tmp"
private val apiUrl = if (preview) {
    "https://api.github.com/repos/Suwayomi/Suwayomi-Server-preview/releases/tags/$previewCommit"
} else {
    "https://api.github.com/repos/Suwayomi/Suwayomi-Server/releases/tags/$tachideskVersion"
}
private const val tmpJson = "$tmpPath/Suwayomi-Server.json"
private val fileSuffix get() = if (preview) {
    previewCommit
} else {
    tachideskVersion.drop(1)
}
private val tmpServerFolder = "$tmpPath/Suwayomi-Server-$fileSuffix/"
private const val macosFolder = "$tmpPath/macos/"
private const val macosJarFolder = "$tmpPath/macos/jar/"
private const val destination = "src/main/resources/"
private const val finalJar = "src/main/resources/Tachidesk.jar"

internal class Asset(
    val name: String,
    val browser_download_url: String,
)

internal class Release(
    val assets: Array<Asset>
)

fun TaskContainerScope.registerTachideskTasks(project: Project) {
    with(project) {
        register<Delete>(deleteOldTachideskTask) {
            group = tachideskGroup
            val tachideskJar = file(finalJar)
            onlyIf {
                tachideskJar.exists() && JarFile(tachideskJar).use { jar ->
                    jar.manifest?.mainAttributes?.getValue("JUI-KEY")?.toIntOrNull() != serverCode
                }
            }
            delete(tachideskJar)
        }

        register<Download>(downloadApiTask) {
            group = tachideskGroup
            mustRunAfter(deleteOldTachideskTask)
            onlyIf { !tachideskExists(projectDir) && !file(finalJar).exists() }

            onlyIfTachideskDoesntExist(projectDir)

            src(apiUrl)
            dest(tmpJson)
        }
        register<Download>(downloadTachidesk) {
            group = tachideskGroup
            mustRunAfter(downloadApiTask)
            onlyIf { !tachideskExists(projectDir) && !file(finalJar).exists() }

            onlyIfTachideskDoesntExist(projectDir)

            doFirst {
                val gson = Gson()
                val jar = gson.fromJson(file(tmpJson).reader(), Release::class.java)
                    .assets
                    .find { it.name.endsWith("jar") }
                src(jar?.browser_download_url)
                dest(finalJar)
            }
        }
        register(signTachideskJar) {
            group = tachideskGroup
            mustRunAfter(downloadTachidesk)
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
                                    tmpFile.toAbsolutePath().toString(),
                                )
                            }

                            Files.copy(tmpFile, it, StandardCopyOption.REPLACE_EXISTING)
                            Files.delete(tmpFile)
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
                    jar.manifest?.mainAttributes?.getValue("JUI-KEY")?.toIntOrNull() != serverCode
                }
            }

            doFirst {
                val manifest = JarFile(tachideskJar).use { jar ->
                    Manifest(jar.manifest)
                }
                manifest.mainAttributes[Attributes.Name("JUI-KEY")] = serverCode.toString()
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
                downloadApiTask,
                downloadTachidesk,
                signTachideskJar,
                modifyTachideskJarManifest,
                deleteTmpFolderTask,
            )
        }
    }

    named("processResources") {
        dependsOn(runAllTachideskTasks)
    }
}
