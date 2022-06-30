/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.extensions

import androidx.compose.ui.text.intl.Locale
import ca.gosyer.jui.core.lang.displayName
import ca.gosyer.jui.data.extension.ExtensionRepositoryImpl
import ca.gosyer.jui.domain.extension.model.Extension
import ca.gosyer.jui.domain.extension.service.ExtensionPreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class ExtensionsScreenViewModel @Inject constructor(
    private val extensionHandler: ExtensionRepositoryImpl,
    extensionPreferences: ExtensionPreferences,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
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
        langs.map { it.lang }.distinct()
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        getExtensions()
    }

    private fun getExtensions() {
        extensionHandler.getExtensionList()
            .onEach {
                extensionList.value = it
                _isLoading.value = false
            }
            .catch {
                log.warn(it) { "Error getting extensions" }
                emit(emptyList())
                _isLoading.value = false
            }
            .launchIn(scope)
    }

    fun install(extension: Extension) {
        log.info { "Install clicked" }
        extensionHandler.installExtension(extension)
            .onEach {
                getExtensions()
            }
            .catch {
                log.warn(it) { "Error installing extension ${extension.apkName}" }
                getExtensions()
            }
            .launchIn(scope)
    }

    fun update(extension: Extension) {
        log.info { "Update clicked" }
        extensionHandler.updateExtension(extension)
            .onEach {
                getExtensions()
            }
            .catch {
                log.warn(it) { "Error updating extension ${extension.apkName}" }
                getExtensions()
            }
            .launchIn(scope)
    }

    fun uninstall(extension: Extension) {
        log.info { "Uninstall clicked" }
        extensionHandler.uninstallExtension(extension)
            .onEach {
                getExtensions()
            }
            .catch {
                log.warn(it) { "Error uninstalling extension ${extension.apkName}" }
                getExtensions()
            }
            .launchIn(scope)
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
                filteredExtensions.removeAll { !it.name.contains(query, true) }
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
            MR.strings.installed.toPlatformString() to (obsolete + updates + installed),
        ).filterNot { it.value.isEmpty() } + available.groupBy { it.lang }.mapKeys {
            if (it.key == "all") {
                MR.strings.all.toPlatformString()
            } else {
                Locale(it.key).displayName
            }
        }
    }

    private companion object {
        private val log = logging()
    }
}
