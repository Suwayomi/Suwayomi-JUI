package ca.gosyer.jui.data.util

import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.exception.ApolloWebSocketClosedException
import com.apollographql.apollo.network.websocket.WebSocket
import com.apollographql.apollo.network.websocket.WebSocketEngine
import com.apollographql.apollo.network.websocket.WebSocketListener
import com.diamondedge.logging.logging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@OptIn(ApolloExperimental::class)
class KtorWebSocketEngine(
    private val client: HttpClient,
) : WebSocketEngine {

    constructor() : this(
        HttpClient {
            install(WebSockets)
        }
    )

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    //private val receiveMessageChannel = Channel<String>(Channel.UNLIMITED)
    private val sendFrameChannel = Channel<Frame>(Channel.UNLIMITED)

    override fun newWebSocket(url: String, headers: List<HttpHeader>, listener: WebSocketListener): WebSocket {
        return open(Url(url), headers, listener)
    }

    private fun open(url: Url, headers: List<HttpHeader>, listener: WebSocketListener): WebSocket {
        val newUrl = URLBuilder(url).apply {
            protocol = when (url.protocol) {
                URLProtocol.HTTPS -> URLProtocol.WSS
                URLProtocol.HTTP -> URLProtocol.WS
                URLProtocol.WS, URLProtocol.WSS -> url.protocol
                /* URLProtocol.SOCKS */else -> throw UnsupportedOperationException("SOCKS is not a supported protocol")
            }
        }.build()
        coroutineScope.launch {
            try {
                client.webSocket(
                    request = {
                        headers {
                            headers.forEach {
                                append(it.name, it.value)
                            }
                        }
                        url(newUrl)
                    },
                ) {
                    coroutineScope {
                        launch {
                            sendFrames(this@webSocket, listener)
                        }
                        try {
                            listener.onOpen()
                            receiveFrames(incoming, listener)
                        } catch (e: Throwable) {
                            val closeReason = closeReasonOrNull()
                            val apolloException = if (closeReason != null) {
                                ApolloWebSocketClosedException(
                                    code = closeReason.code.toInt(),
                                    reason = closeReason.message,
                                    cause = e
                                )
                            } else {
                                ApolloNetworkException(
                                    message = "Web socket communication error",
                                    platformCause = e
                                )
                            }

                            listener.onError(apolloException)
                            log.warn(e) { "Closed websocket" }
                            throw e
                        }
                    }
                }
            } catch (e: Throwable) {
                log.warn(e) { "Closed websocket catch" }
                listener.onError(ApolloNetworkException(message = "Web socket communication error", platformCause = e))
            } finally {
                log.warn { "Closed websocket finally" }
                // Not 100% sure this can happen. Better safe than sorry. close() is idempotent so it shouldn't hurt
                listener.onError(ApolloNetworkException(message = "Web socket communication error", platformCause = null))
            }
        }

        return object : WebSocket {
            override fun send(data: ByteArray) {
                log.debug { "send data: $data" }
                sendFrameChannel.trySend(Frame.Binary(true, data))
            }

            override fun send(text: String) {
                log.debug { "send text: $text" }
                sendFrameChannel.trySend(Frame.Text(text))
            }

            override fun close(code: Int, reason: String) {
                log.debug { "send close: code=$code, reason=$reason" }
                sendFrameChannel.trySend(Frame.Close(CloseReason(code.toShort(), reason)))
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.closeReasonOrNull(): CloseReason? {
        return try {
            closeReason.await()
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            }
            null
        }
    }

    private suspend fun sendFrames(session: DefaultClientWebSocketSession, listener: WebSocketListener) {
        while (true) {
            val frame = sendFrameChannel.receive()
            session.send(frame)
            if (frame is Frame.Close) {
                // normal termination
                listener.onClosed(1000, null)
            }
        }
    }

    private suspend fun receiveFrames(incoming: ReceiveChannel<Frame>, listener: WebSocketListener) {
        while (true) {
            when (val frame = incoming.receive()) {
                is Frame.Text -> {
                    listener.onMessage(frame.readText())
                }

                is Frame.Binary -> {
                    listener.onMessage(frame.readBytes())
                }

                else -> error("unknown frame type")
            }
        }
    }

    override fun close() {
        log.info { "Closing websocket" }
        sendFrameChannel.close()
        coroutineScope.cancel()
    }

    companion object {
        private val log = logging()
    }
}
