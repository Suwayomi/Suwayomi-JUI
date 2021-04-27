/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import androidx.compose.ui.graphics.ImageBitmap
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.http.Parameters
import kotlinx.coroutines.CancellationException

open class BaseInteractionHandler(
    protected val client: Http,
    serverPreferences: ServerPreferences
) {
    private val _serverUrl = serverPreferences.server()
    val serverUrl get() = _serverUrl.get()

    protected suspend inline fun <reified T> Http.getRepeat(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        var attempt = 1
        var lastException: Exception
        do {
            try {
                return get(urlString, block)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                lastException = e
            }
            attempt++
        } while (attempt <= 3)
        throw lastException
    }

    protected suspend inline fun <reified T> Http.deleteRepeat(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        var attempt = 1
        var lastException: Exception
        do {
            try {
                return delete(urlString, block)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                lastException = e
            }
            attempt++
        } while (attempt <= 3)
        throw lastException
    }

    protected suspend inline fun <reified T> Http.patchRepeat(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        var attempt = 1
        var lastException: Exception
        do {
            try {
                return patch(urlString, block)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                lastException = e
            }
            attempt++
        } while (attempt <= 3)
        throw lastException
    }

    protected suspend inline fun <reified T> Http.postRepeat(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        var attempt = 1
        var lastException: Exception
        do {
            try {
                return post(urlString, block)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                lastException = e
            }
            attempt++
        } while (attempt <= 3)
        throw lastException
    }

    protected suspend inline fun <reified T> Http.submitFormRepeat(
        urlString: String,
        formParameters: Parameters = Parameters.Empty,
        encodeInQuery: Boolean = false,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        var attempt = 1
        var lastException: Exception
        do {
            try {
                return submitForm(urlString, formParameters, encodeInQuery, block)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                lastException = e
            }
            attempt++
        } while (attempt <= 3)
        throw lastException
    }

    suspend fun imageFromUrl(client: Http, imageUrl: String): ImageBitmap {
        var attempt = 1
        var lastException: Exception
        do {
            try {
                return ca.gosyer.util.compose.imageFromUrl(client, imageUrl)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                lastException = e
            }
            attempt++
        } while (attempt <= 3)
        throw lastException
    }
}
