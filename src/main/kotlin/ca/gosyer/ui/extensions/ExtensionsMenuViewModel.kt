/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.extensions

import ca.gosyer.backend.models.Extension
import ca.gosyer.backend.network.interactions.ExtensionInteractionHandler
import ca.gosyer.backend.preferences.PreferenceHelper
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.system.inject
import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging

class ExtensionsMenuViewModel: ViewModel() {
    private val preferences: PreferenceHelper by inject()
    private val httpClient: HttpClient by inject()
    private val logger = KotlinLogging.logger {}

    val serverUrl = preferences.serverUrl.asStateFlow(scope)

    private val _extensions = MutableStateFlow(emptyList<Extension>())
    val extensions = _extensions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()


    init {
        scope.launch {
            getExtensions()
        }
    }

    private suspend fun getExtensions() {
        try {
            _isLoading.value = true
            val enabledLangs = preferences.enabledLangs.get()
            val extensions = ExtensionInteractionHandler(httpClient).getExtensionList()
            _extensions.value = extensions.filter { it.lang in enabledLangs }.sortedWith(compareBy({ it.lang }, { it.pkgName }))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        } finally {
            _isLoading.value = false
        }
    }

    fun install(extension: Extension) {
        logger.info { "Install clicked" }
        scope.launch {
            try {
                ExtensionInteractionHandler(httpClient).installExtension(extension)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
            getExtensions()
        }
    }

    fun uninstall(extension: Extension) {
        logger.info { "Uninstall clicked" }
        scope.launch {
            try {
                ExtensionInteractionHandler(httpClient).uninstallExtension(extension)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
            getExtensions()
        }
    }
}