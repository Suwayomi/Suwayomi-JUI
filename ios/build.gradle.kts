import org.jetbrains.compose.compose
import org.jetbrains.compose.experimental.uikit.tasks.ExperimentalPackComposeApplicationForXCodeTask
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File
import kotlin.reflect.full.declaredMemberProperties

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.compose.get().pluginId)
    id(libs.plugins.buildkonfig.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
    id(libs.plugins.aboutLibraries.get().pluginId)
}

kotlin {
    val configuration: KotlinNativeTarget.() -> Unit = {
        binaries {
            executable {
                entryPoint = "ca.gosyer.jui.ios.main"
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics"
                )
                // TODO: the current compose binary surprises LLVM, so disable checks for now.
                freeCompilerArgs = freeCompilerArgs + "-Xdisable-phases=VerifyBitcode"
            }
        }
    }
    iosX64("uikitX64", configuration)
    iosArm64("uikitArm64", configuration)
    iosSimulatorArm64("uikitSimulatorArm64", configuration)

    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val uikitMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(projects.core)
                implementation(projects.i18n)
                implementation(projects.domain)
                implementation(projects.data)
                implementation(projects.uiCore)
                implementation(projects.presentation)

                // UI (Compose)
                implementation(compose.foundation)
                implementation(compose.runtime)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.animation)
                implementation(compose("org.jetbrains.compose.ui:ui-util"))
                implementation(libs.voyager.core)
                implementation(libs.voyager.navigation)
                implementation(libs.voyager.transitions)
                implementation(libs.accompanist.pager)
                implementation(libs.accompanist.pagerIndicators)
                implementation(libs.accompanist.flowLayout)
                implementation(libs.imageloader)
                implementation(libs.materialDialogs.core)

                // Threading
                implementation(libs.coroutines.core)

                // Json
                implementation(libs.serialization.json.core)
                implementation(libs.serialization.json.okio)

                // Dependency Injection
                implementation(libs.kotlinInject.runtime)

                // Http client
                implementation(libs.ktor.core)
                implementation(libs.ktor.darwin)
                implementation(libs.ktor.contentNegotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.logging)
                implementation(libs.ktor.websockets)
                implementation(libs.ktor.auth)

                // Ktorfit
                implementation(libs.ktorfit.lib)

                // Logging
                implementation(libs.logging.kmlogging)

                // Storage
                implementation(libs.okio)

                // Preferences
                implementation(libs.multiplatformSettings.core)
                implementation(libs.multiplatformSettings.serialization)
                implementation(libs.multiplatformSettings.coroutines)

                // Utility
                implementation(libs.dateTime)
                implementation(libs.immutableCollections)
                implementation(libs.kds)

                // Localization
                implementation(libs.moko.core)
                //implementation(libs.moko.compose)

                // Testing
                /*testImplementation(kotlin("test-junit"))
                testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
                testImplementation(libs.coroutines.test)*/
            }
        }
        val uikitTest by creating {
            dependsOn(commonTest)
        }

        listOf(
            "uikitX64",
            "uikitArm64",
            "uikitSimulatorArm64",
        ).forEach {
            getByName(it + "Main").dependsOn(uikitMain)
            getByName(it + "Test").dependsOn(uikitTest)
        }
    }
}

compose.experimental {
    uikit.application {
        bundleIdPrefix = "ca.gosyer.jui.app.ios"
        projectName = "Tachidesk-JUI"
        // ./gradlew :app:ios:iosDeployIPhone13Debug
        deployConfigurations {
            simulator("IPhone13") {
                device = org.jetbrains.compose.experimental.dsl.IOSDevices.IPHONE_13
            }
        }
    }
}

dependencies {
    listOf(
        "kspUikitArm64",
        "kspUikitSimulatorArm64",
        "kspUikitX64"
    ).forEach {
        add(it, libs.kotlinInject.compiler)
        add(it, libs.ktorfit.ksp)
    }
}

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            // TODO: the current compose binary surprises LLVM, so disable checks for now.
            freeCompilerArgs = freeCompilerArgs + "-Xdisable-phases=VerifyBitcode"
        }
    }
}

buildkonfig {
    packageName = "ca.gosyer.jui.ios.build"
}

// todo: Remove when resolved: https://github.com/icerockdev/moko-resources/issues/372
// copy .bundle from all .klib to .kexe
tasks.withType<KotlinNativeLink>()
    .configureEach {
        val linkTask: KotlinNativeLink = this
        val outputDir: File = this.outputFile.get().parentFile

        @Suppress("ObjectLiteralToLambda") // lambda broke up-to-date
        val action = object : Action<Task> {
            override fun execute(t: Task) {
                (linkTask.libraries + linkTask.sources)
                    .filter { library -> library.extension == "klib" }
                    .filter(File::exists)
                    .forEach { inputFile ->
                        val klibKonan = org.jetbrains.kotlin.konan.file.File(inputFile.path)
                        val klib = org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl(
                            klib = klibKonan,
                            component = "default"
                        )
                        val layout = klib.extractingToTemp

                        // extracting bundles
                        layout
                            .resourcesDir
                            .absolutePath
                            .let(::File)
                            .listFiles { file: File -> file.extension == "bundle" }
                            // copying bundles to app
                            ?.forEach {
                                logger.info("${it.absolutePath} copying to $outputDir")
                                it.copyRecursively(
                                    target = File(outputDir, it.name),
                                    overwrite = true
                                )
                            }
                    }
            }
        }
        doLast(action)
    }

// copy .bundle from .kexe to .app
tasks.withType<ExperimentalPackComposeApplicationForXCodeTask>()
    .configureEach {
        val packTask: ExperimentalPackComposeApplicationForXCodeTask = this

        val kclass = ExperimentalPackComposeApplicationForXCodeTask::class
        val kotlinBinaryField =
            kclass.declaredMemberProperties.single { it.name == "kotlinBinary" }
        val destinationDirField =
            kclass.declaredMemberProperties.single { it.name == "destinationDir" }
        val executablePathField =
            kclass.declaredMemberProperties.single { it.name == "executablePath" }

        @Suppress("ObjectLiteralToLambda") // lambda broke up-to-date
        val action = object : Action<Task> {
            override fun execute(t: Task) {
                val kotlinBinary: RegularFile =
                    (kotlinBinaryField.get(packTask) as RegularFileProperty).get()
                val destinationDir: Directory =
                    (destinationDirField.get(packTask) as DirectoryProperty).get()
                val executablePath: String =
                    (executablePathField.get(packTask) as Provider<String>).get()

                val outputDir: File = File(destinationDir.asFile, executablePath).parentFile

                val bundleSearchDir: File = kotlinBinary.asFile.parentFile
                bundleSearchDir
                    .listFiles { file: File -> file.extension == "bundle" }
                    ?.forEach { file ->
                        file.copyRecursively(File(outputDir, file.name), true)
                    }
            }
        }
        doLast(action)
    }
