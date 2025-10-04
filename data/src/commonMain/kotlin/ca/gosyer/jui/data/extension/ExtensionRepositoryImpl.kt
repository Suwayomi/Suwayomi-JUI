/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.extension

import ca.gosyer.jui.data.graphql.FetchExtensionsMutation
import ca.gosyer.jui.data.graphql.InstallExtensionMutation
import ca.gosyer.jui.data.graphql.InstallExternalExtensionMutation
import ca.gosyer.jui.data.graphql.UninstallExtensionMutation
import ca.gosyer.jui.data.graphql.UpdateExtensionMutation
import ca.gosyer.jui.data.graphql.fragment.ExtensionFragment
import ca.gosyer.jui.domain.extension.model.Extension
import ca.gosyer.jui.domain.extension.service.ExtensionRepository
import ca.gosyer.jui.domain.server.Http
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.DefaultUpload
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Source

class ExtensionRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val http: Http,
    private val serverUrl: Url,
) : ExtensionRepository {
    override fun getExtensionList(): Flow<List<Extension>> =
        apolloClient.mutation(
            FetchExtensionsMutation(),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors.fetchExtensions!!.extensions.map { it.extensionFragment.toExtension() }
            }

    override fun installExtension(source: Source): Flow<Unit> =
        apolloClient.mutation(
            InstallExternalExtensionMutation(
                DefaultUpload.Builder()
                    .content {
                        it.writeAll(source)
                    }
                    .fileName("extension.apk")
                    .contentType("application/octet-stream")
                    .build(),
            ),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors.installExternalExtension!!
            }

    override fun installExtension(pkgName: String): Flow<Unit> =
        apolloClient.mutation(
            InstallExtensionMutation(pkgName),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors.updateExtension
            }

    override fun updateExtension(pkgName: String): Flow<Unit> =
        apolloClient.mutation(
            UpdateExtensionMutation(pkgName),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors.updateExtension
            }

    override fun uninstallExtension(pkgName: String): Flow<Unit> =
        apolloClient.mutation(
            UninstallExtensionMutation(pkgName),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors.updateExtension
            }

    companion object {
        internal fun ExtensionFragment.toExtension(): Extension =
            Extension(
                name = name,
                pkgName = pkgName,
                versionName = versionName,
                versionCode = versionCode,
                lang = lang,
                apkName = apkName,
                iconUrl = iconUrl,
                installed = isInstalled,
                hasUpdate = hasUpdate,
                obsolete = isObsolete,
                isNsfw = isNsfw,
            )
    }
}
