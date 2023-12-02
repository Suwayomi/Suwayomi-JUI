/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.core.lang.IO
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FlowConverterFactory : Converter.Factory {
    private class FlowResponseConverter(
        val typeData: TypeData,
        val ktorfit: Ktorfit,
    ) : Converter.ResponseConverter<HttpResponse, Flow<Any?>> {
        override fun convert(getResponse: suspend () -> HttpResponse): Flow<Any?> {
            return flow {
                val response = getResponse()

                val convertedBody = ktorfit.nextSuspendResponseConverter(
                    null,
                    typeData.typeArgs.first(),
                )?.convert(response)
                    ?: response.body(typeData.typeArgs.first().typeInfo)
                emit(convertedBody)
            }.flowOn(Dispatchers.IO)
        }
    }

    override fun responseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit,
    ): Converter.ResponseConverter<HttpResponse, *>? {
        if (typeData.typeInfo.type == Flow::class) {
            return FlowResponseConverter(typeData, ktorfit)
        }
        return null
    }

    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit,
    ): Converter.SuspendResponseConverter<HttpResponse, *>? = null
}
