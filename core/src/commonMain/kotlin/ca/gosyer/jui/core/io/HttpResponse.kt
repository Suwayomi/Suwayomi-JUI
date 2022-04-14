/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.io

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.discardRemaining
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

suspend fun HttpResponse.asSuccess() : HttpResponse = apply {
    if (!status.isSuccess()) {
        discardRemaining()
        throw HttpException(status)
    }
}

class HttpException(val status: HttpStatusCode) : IllegalStateException("HTTP error $status")