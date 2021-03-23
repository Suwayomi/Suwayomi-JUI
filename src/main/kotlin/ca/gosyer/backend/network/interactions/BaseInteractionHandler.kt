/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.network.interactions

import androidx.compose.ui.graphics.ImageBitmap
import ca.gosyer.backend.preferences.PreferenceHelper
import ca.gosyer.util.system.inject
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.http.Parameters
import kotlinx.coroutines.CancellationException

open class BaseInteractionHandler {
    val preferences: PreferenceHelper by inject()
    val serverUrl get() = preferences.serverUrl.get()

    protected suspend inline fun <reified T> HttpClient.getRepeat(
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

    protected suspend inline fun <reified T> HttpClient.deleteRepeat(
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

    protected suspend inline fun <reified T> HttpClient.patchRepeat(
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

    protected suspend inline fun <reified T> HttpClient.postRepeat(
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

    protected suspend inline fun <reified T> HttpClient.submitFormRepeat(
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

    suspend fun imageFromUrl(client: HttpClient, imageUrl: String): ImageBitmap {
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