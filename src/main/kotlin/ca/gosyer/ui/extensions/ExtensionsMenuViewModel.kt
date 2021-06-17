/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.extensions

import ca.gosyer.data.extension.ExtensionPreferences
import ca.gosyer.data.models.Extension
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.interactions.ExtensionInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.lang.throwIfCancellation
import ca.gosyer.util.system.CKLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExtensionsMenuViewModel @Inject constructor(
    private val extensionHandler: ExtensionInteractionHandler,
    serverPreferences: ServerPreferences,
    private val extensionPreferences: ExtensionPreferences
) : ViewModel() {
    val serverUrl = serverPreferences.serverUrl().stateIn(scope)
    private val _enabledLangs = extensionPreferences.languages().asStateFlow()
    val enabledLangs = _enabledLangs.asStateFlow()

    private lateinit var extensionList: List<Extension>

    private val _extensions = MutableStateFlow(emptyList<Extension>())
    val extensions = _extensions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val searchQuery = MutableStateFlow<String?>(null)

    init {
        scope.launch {
            getExtensions()
        }

        enabledLangs.drop(1).onEach {
            search(searchQuery.value.orEmpty())
        }.launchIn(scope)
    }

    private suspend fun getExtensions() {
        try {
            _isLoading.value = true
            extensionList = extensionHandler.getExtensionList()
            search(searchQuery.value.orEmpty())
        } catch (e: Exception) {
            e.throwIfCancellation()
            extensionList = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun install(extension: Extension) {
        info { "Install clicked" }
        scope.launch {
            try {
                extensionHandler.installExtension(extension)
            } catch (e: Exception) {
                e.throwIfCancellation()
            }
            getExtensions()
        }
    }

    fun update(extension: Extension) {
        info { "Update clicked" }
        scope.launch {
            try {
                extensionHandler.updateExtension(extension)
            } catch (e: Exception) {
                e.throwIfCancellation()
            }
            getExtensions()
        }
    }

    fun uninstall(extension: Extension) {
        info { "Uninstall clicked" }
        scope.launch {
            try {
                extensionHandler.uninstallExtension(extension)
            } catch (e: Exception) {
                e.throwIfCancellation()
            }
            getExtensions()
        }
    }

    fun getSourceLanguages() = extensionList.map { it.lang }.toSet()

    fun setEnabledLanguages(langs: Set<String>) {
        info { langs }
        _enabledLangs.value = langs
    }

    fun search(searchQuery: String) {
        this.searchQuery.value = searchQuery.takeUnless { it.isBlank() }
        val extensionList = extensionList.filter { it.lang in enabledLangs.value }
        if (searchQuery.isBlank()) {
            _extensions.value = extensionList.splitSort()
        } else {
            val queries = searchQuery.split(" ")
            val extensions = extensionList.toMutableList()
            queries.forEach { query ->
                extensions.removeIf { !it.name.contains(query, true) }
            }
            _extensions.value = extensions.toList().splitSort()
        }
    }

    private fun List<Extension>.splitSort(): List<Extension> {
        val comparator = compareBy<Extension>({ it.lang }, { it.pkgName })
        val obsolete = filter { it.obsolete }.sortedWith(comparator)
        val updates = filter { it.hasUpdate }.sortedWith(comparator)
        val installed = filter { it.installed && !it.hasUpdate && !it.obsolete }.sortedWith(comparator)
        val available = filter { !it.installed }.sortedWith(comparator)
        return obsolete + updates + installed + available
    }

    private companion object : CKLogger({})
}
