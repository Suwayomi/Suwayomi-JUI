/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.util.reflect.TypeInfo
import kotlinx.serialization.json.Json

actual suspend fun decodeType(response: HttpResponse, typeInfo: TypeInfo, json: Json): Any {
    return response.body(typeInfo)
}
