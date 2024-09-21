package ca.gosyer.jui.core.io

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Job
import okio.Source
import okio.source
import kotlin.coroutines.CoroutineContext

actual suspend fun ByteReadChannel.toSource(context: CoroutineContext): Source = toInputStream(context[Job]).source()
