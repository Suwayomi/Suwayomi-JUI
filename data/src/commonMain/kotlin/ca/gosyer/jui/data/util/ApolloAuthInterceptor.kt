package ca.gosyer.jui.data.util

import ca.gosyer.jui.domain.server.model.Auth
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.user.interactor.UserRefreshUI
import ca.gosyer.jui.domain.user.service.UserPreferences
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import kotlin.io.encoding.Base64

@Inject
class ApolloAuthInterceptor(
    private val userPreferences: UserPreferences,
    private val serverPreferences: ServerPreferences,
    private val userRefreshUI: Lazy<UserRefreshUI>
) : ApolloInterceptor {
    override fun <D : Operation.Data> intercept(
        request: ApolloRequest<D>,
        chain: ApolloInterceptorChain,
    ): Flow<ApolloResponse<D>> {
        return when (serverPreferences.auth().get()) {
            Auth.NONE, Auth.DIGEST -> chain.proceed(request)
            Auth.BASIC -> {
                val username = serverPreferences.authUsername().get()
                val password = serverPreferences.authPassword().get()
                val authHeader = "Basic ${Base64.encode("${username}:${password}".encodeToByteArray())}"
                val requestWithAuth = request.newBuilder()
                    .addHttpHeader("Authorization", authHeader)
                    .build()
                chain.proceed(requestWithAuth)
            }
            Auth.SIMPLE -> {
                val session = userPreferences.simpleSession().get()
                val requestWithAuth = if (session.isNotEmpty()) {
                    request.newBuilder().addHttpHeader("Cookie", "JSESSIONID=$session")
                        .build()
                } else {
                    request
                }
                chain.proceed(requestWithAuth)
            }
            Auth.UI -> {
                val token = userPreferences.uiAccessToken().get()
                return if (token.isNotEmpty()) {
                    chain.proceed(
                        request.newBuilder()
                            .httpHeaders(
                                request.httpHeaders
                                    ?.filterNot { it.name.equals("Authorization", true) }
                                    .orEmpty()
                                    .plus(HttpHeader("Authorization", "Bearer $token"))
                            )
                            .build()
                    ).map { response ->
                        if (response.errors?.any { it.message.contains("unauthorized", true) } == true) {
                            log.warn { "${request.operation.name()} - Token expired, refreshing..." }
                            val newToken = userRefreshUI.value.await()
                            if (newToken != null) {
                                chain.proceed(
                                    request.newBuilder()
                                        .httpHeaders(
                                            request.httpHeaders
                                                ?.filterNot { it.name.equals("Authorization", true) }
                                                .orEmpty()
                                                .plus(HttpHeader("Authorization", "Bearer $newToken"))
                                        )
                                        .build()
                                ).first()
                            } else {
                                response
                            }
                        } else {
                            response
                        }
                    }
                } else {
                    chain.proceed(request)
                }
            }
        }
    }

    companion object {
        private val log = logging()
    }
}
