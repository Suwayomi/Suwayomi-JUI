/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.util.reflect.TypeInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

actual suspend fun decodeType(response: HttpResponse, typeInfo: TypeInfo, json: Json): Any {
    return json.decodeFromString(serializer(typeInfo.kotlinType!!), response.bodyAsText())!!
}