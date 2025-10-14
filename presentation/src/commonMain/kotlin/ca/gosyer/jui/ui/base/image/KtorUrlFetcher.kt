package ca.gosyer.jui.ui.base.image

import com.seiko.imageloader.component.fetcher.FetchResult
import com.seiko.imageloader.component.fetcher.Fetcher
import com.seiko.imageloader.model.ImageSource
import com.seiko.imageloader.model.ImageSourceFrom
import com.seiko.imageloader.model.KtorRequestData
import com.seiko.imageloader.model.extraData
import com.seiko.imageloader.model.mimeType
import com.seiko.imageloader.model.toImageSource
import com.seiko.imageloader.option.Options
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import okio.Buffer

class KtorUrlFetcher private constructor(
    private val httpUrl: Url,
    private val httpClient: () -> HttpClient,
    private val ktorRequestData: KtorRequestData?,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        return httpClient().prepareRequest {
            url(httpUrl)
            method = ktorRequestData?.method ?: HttpMethod.Get
            ktorRequestData?.headers?.let {
                headers {
                    it.forEach { (key, value) ->
                        append(key, value)
                    }
                }
            }
        }.execute { response ->
            if (!response.status.isSuccess()) {
                throw KtorUrlRequestException("code:${response.status.value}, ${response.status.description}")
            }

            FetchResult.OfSource(
                imageSource = channelToImageSource(response.bodyAsChannel()),
                imageSourceFrom = ImageSourceFrom.Network,
                extra = extraData {
                    mimeType(response.contentType()?.toString())
                },
            )
        }
    }

    class Factory(
        private val httpClient: () -> HttpClient,
    ) : Fetcher.Factory {
        override fun create(data: Any, options: Options): Fetcher? {
            if (data !is Url) return null
            return KtorUrlFetcher(
                httpUrl = data,
                httpClient = httpClient,
                ktorRequestData = options.extra["KEY_KTOR_REQUEST_DATA"] as? KtorRequestData,
            )
        }
    }
}

private suspend fun channelToImageSource(channel: ByteReadChannel): ImageSource {
    val buffer = Buffer()
    while (!channel.isClosedForRead) {
        val packet = channel.readRemaining(2048)
        while (!packet.exhausted()) {
            val bytes = packet.readByteArray()
            buffer.write(bytes)
        }
    }
    return buffer.toImageSource()
}

private class KtorUrlRequestException(msg: String) : RuntimeException(msg)
