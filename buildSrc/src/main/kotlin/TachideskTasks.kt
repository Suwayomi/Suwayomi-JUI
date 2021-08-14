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
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.register
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest

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
                && !File(rootDir, "src/main/resources/Tachidesk.jar").exists()
        }
    }
}

private fun Project.getSigningIdentity() = "${properties["compose.desktop.mac.signing.identity"]}".trim('"')

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
        register<Exec>(androidScriptTask) {
            group = tachideskGroup
            mustRunAfter(extractTask)
            onlyIf { !tachideskExists(rootDir) && !file("${tmpServerFolder}AndroidCompat/lib/android.jar").exists() }

            val workingDir = file(tmpServerFolder)
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
            val os = DefaultNativePlatform.getCurrentOperatingSystem()
            when {
                os.isMacOsX && isSigning(properties) -> into(macosFolder)
                else -> into(destination)
            }
            rename {
                "Tachidesk.jar"
            }
        }
        register<Copy>(extractTachideskJar) {
            group = tachideskGroup
            mustRunAfter(copyTachideskJarTask)
            onlyIfSigning(project)

            from(zipTree("${macosFolder}Tachidesk.jar"))
            into(macosJarFolder)
        }
        register(signTachideskJar) {
            group = tachideskGroup
            mustRunAfter(extractTachideskJar)
            onlyIfSigning(project)

            doFirst {
                file(macosJarFolder).walkTopDown()
                    .asSequence()
                    .filter { it.extension.anyEquals("dylib", "jnilib", ignoreCase = true) }
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

            from(macosJarFolder)
            archiveBaseName.set("Tachidesk")
            archiveVersion.set("")
            archiveExtension.set("jar")
            destinationDirectory.set(file(destination))
        }
        register<Task>(modifyTachideskJarManifest) {
            group = tachideskGroup
            mustRunAfter(zipTachideskJar)
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
                androidScriptTask,
                setupCITask,
                buildTachideskTask,
                copyTachideskJarTask,
                extractTachideskJar,
                signTachideskJar,
                zipTachideskJar,
                modifyTachideskJarManifest,
                deleteTmpFolderTask
            )
        }
    }

    named("processResources") {
        dependsOn(runAllTachideskTasks)
    }
}