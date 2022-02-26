/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.extensions

import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.extension.ExtensionPreferences
import ca.gosyer.data.models.Extension
import ca.gosyer.data.server.interactions.ExtensionInteractionHandler
import ca.gosyer.i18n.MR
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import java.util.Locale

class ExtensionsScreenViewModel @Inject constructor(
    private val extensionHandler: ExtensionInteractionHandler,
    extensionPreferences: ExtensionPreferences
) : ViewModel() {
    private val extensionList = MutableStateFlow<List<Extension>?>(null)

    private val _enabledLangs = extensionPreferences.languages().asStateFlow()
    val enabledLangs = _enabledLangs.asStateFlow()

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    val extensions = combine(
        searchQuery,
        extensionList,
        enabledLangs
    ) { searchQuery, extensions, enabledLangs ->
        search(searchQuery, extensions, enabledLangs)
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())

    val availableLangs = extensionList.filterNotNull().map { langs ->
        langs.map { it.lang }.toSet()
    }.stateIn(scope, SharingStarted.Eagerly, emptySet())

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
            extensionList.value = extensionHandler.getExtensionList()
        } catch (e: Exception) {
            e.throwIfCancellation()
            extensionList.value = emptyList()
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

    fun setEnabledLanguages(langs: Set<String>) {
        _enabledLangs.value = langs
    }

    fun setQuery(query: String) {
        _searchQuery.value = query
    }

    private fun search(searchQuery: String?, extensionList: List<Extension>?, enabledLangs: Set<String>): Map<String, List<Extension>> {
        val extensions = extensionList?.filter { it.lang in enabledLangs }
            .orEmpty()
        return if (searchQuery.isNullOrBlank()) {
            extensions.splitSort()
        } else {
            val queries = searchQuery.split(" ")
            val filteredExtensions = extensions.toMutableList()
            queries.forEach { query ->
                filteredExtensions.removeIf { !it.name.contains(query, true) }
            }
            filteredExtensions.toList().splitSort()
        }
    }

    private fun List<Extension>.splitSort(): Map<String, List<Extension>> {
        val comparator = compareBy<Extension>({ it.lang }, { it.pkgName })
        val obsolete = filter { it.obsolete }.sortedWith(comparator)
        val updates = filter { it.hasUpdate }.sortedWith(comparator)
        val installed = filter { it.installed && !it.hasUpdate && !it.obsolete }.sortedWith(comparator)
        val available = filter { !it.installed }.sortedWith(comparator)

        return mapOf(
            MR.strings.installed.localized() to (obsolete + updates + installed),
        ).filterNot { it.value.isEmpty() } + available.groupBy { it.lang }.mapKeys {
            if (it.key == "all") {
                MR.strings.all.localized()
            } else {
                Locale.forLanguageTag(it.key).displayName
            }
        }
    }

    private companion object : CKLogger({})
}
