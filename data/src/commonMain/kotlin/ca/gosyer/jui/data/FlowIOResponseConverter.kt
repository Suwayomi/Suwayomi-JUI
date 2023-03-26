/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.core.lang.IO
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.request.ResponseConverter
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.statement.HttpResponse
import io.ktor.util.reflect.TypeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

class FlowIOResponseConverter @Inject constructor(private val json: Json) : ResponseConverter {

    override fun supportedType(typeData: TypeData, isSuspend: Boolean): Boolean {
        return typeData.qualifiedName == "kotlinx.coroutines.flow.Flow"
    }

    override fun <RequestType : Any?> wrapResponse(
        typeData: TypeData,
        requestFunction: suspend () -> Pair<TypeInfo, HttpResponse?>,
        ktorfit: Ktorfit,
    ): Any {
        return flow {
            try {
                val (info, response) = requestFunction()
                if (info.type == HttpResponse::class) {
                    emit(response!!)
                } else {
                    emit(decodeType(response!!, info, json))
                }
            } catch (exception: Exception) {
                throw exception
            }
        }.flowOn(Dispatchers.IO)
    }
}

expect suspend fun decodeType(response: HttpResponse, typeInfo: TypeInfo, json: Json): Any
