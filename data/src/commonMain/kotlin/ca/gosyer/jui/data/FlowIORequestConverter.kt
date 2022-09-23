/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.core.lang.IO
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.request.RequestConverter
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.util.reflect.TypeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FlowIORequestConverter : RequestConverter {

    override fun supportedType(typeData: TypeData, isSuspend: Boolean): Boolean {
        return typeData.qualifiedName == "kotlinx.coroutines.flow.Flow"
    }

    override fun <RequestType> convertRequest(
        typeData: TypeData,
        requestFunction: suspend () -> Pair<TypeInfo, HttpResponse?>,
        ktorfit: Ktorfit
    ): Any {
        return flow {
            try {
                val (info, response) = requestFunction()
                if (info.type == HttpResponse::class) {
                    emit(response!!)
                } else {
                    emit(response!!.body(info))
                }
            } catch (exception: Exception) {
                throw exception
            }
        }.flowOn(Dispatchers.IO)
    }
}