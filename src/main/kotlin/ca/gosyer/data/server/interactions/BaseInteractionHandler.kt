/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import androidx.compose.ui.graphics.ImageBitmap
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.util.lang.throwIfCancellation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.http.Parameters

open class BaseInteractionHandler(
    protected val client: Http,
    serverPreferences: ServerPreferences
) {
    private val _serverUrl = serverPreferences.server()
    val serverUrl get() = _serverUrl.get()

    protected inline fun <T> repeat(block: () -> T): T {
        var attempt = 1
        var lastException: Exception
        do {
            try {
                return block()
            } catch (e: Exception) {
                e.throwIfCancellation()
                lastException = e
            }
            attempt++
        } while (attempt <= 3)
        throw lastException
    }

    protected suspend inline fun <reified T> Http.getRepeat(
        urlString: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        return repeat {
            get(urlString, block)
        }
    }

    protected suspend inline fun <reified T> Http.deleteRepeat(
        urlString: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        return repeat {
            delete(urlString, block)
        }
    }

    protected suspend inline fun <reified T> Http.patchRepeat(
        urlString: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        return repeat {
            patch(urlString, block)
        }
    }

    protected suspend inline fun <reified T> Http.postRepeat(
        urlString: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        return repeat {
            post(urlString, block)
        }
    }

    protected suspend inline fun <reified T> Http.submitFormRepeat(
        urlString: String,
        formParameters: Parameters = Parameters.Empty,
        encodeInQuery: Boolean = false,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        return repeat {
            submitForm(urlString, formParameters, encodeInQuery, block)
        }
    }

    suspend fun imageFromUrl(client: Http, imageUrl: String, block: HttpRequestBuilder.() -> Unit): ImageBitmap {
        return repeat {
            ca.gosyer.util.compose.imageFromUrl(client, imageUrl, block)
        }
    }
}
