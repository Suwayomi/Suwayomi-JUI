/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.extensions

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.intl.Locale
import ca.gosyer.jui.core.io.saveTo
import ca.gosyer.jui.core.lang.displayName
import ca.gosyer.jui.core.lang.throwIfCancellation
import ca.gosyer.jui.domain.extension.interactor.GetExtensionList
import ca.gosyer.jui.domain.extension.interactor.InstallExtension
import ca.gosyer.jui.domain.extension.interactor.InstallExtensionFile
import ca.gosyer.jui.domain.extension.interactor.UninstallExtension
import ca.gosyer.jui.domain.extension.interactor.UpdateExtension
import ca.gosyer.jui.domain.extension.model.Extension
import ca.gosyer.jui.domain.extension.service.ExtensionPreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import okio.FileSystem
import okio.Source
import org.lighthousegames.logging.logging
import kotlin.random.Random

@Inject
class ExtensionsScreenViewModel(
    private val getExtensionList: GetExtensionList,
    private val installExtensionFile: InstallExtensionFile,
    private val installExtension: InstallExtension,
    private val updateExtension: UpdateExtension,
    private val uninstallExtension: UninstallExtension,
    extensionPreferences: ExtensionPreferences,
    contextWrapper: ContextWrapper,
) : ViewModel(contextWrapper) {
    private val extensionList = MutableStateFlow<List<Extension>?>(null)

    private val _enabledLangs = extensionPreferences.languages().asStateFlow()
    val enabledLangs = _enabledLangs.map { it.toImmutableSet() }
        .stateIn(scope, SharingStarted.Eagerly, persistentSetOf())

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    private val workingExtensions = MutableStateFlow<List<String>>(emptyList())

    val extensions = combine(
        searchQuery,
        extensionList,
        enabledLangs,
        workingExtensions,
    ) { searchQuery, extensions, enabledLangs, workingExtensions ->
        search(searchQuery, extensions, enabledLangs, workingExtensions)
    }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

    val availableLangs = extensionList.filterNotNull().map { langs ->
        langs.map { it.lang }.distinct().toImmutableList()
    }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

    val isLoading = extensionList.map { it == null }.stateIn(scope, SharingStarted.Eagerly, true)

    init {
        scope.launch {
            getExtensions()
        }
    }

    private suspend fun getExtensions() {
        extensionList.value = getExtensionList.await(onError = { toast(it.message.orEmpty()) }).orEmpty()
    }

    fun install(source: Source) {
        log.info { "Install file clicked" }
        scope.launch {
            try {
                val file = FileSystem.SYSTEM_TEMPORARY_DIRECTORY
                    .resolve("tachidesk.${Random.nextLong()}.apk")
                    .also { file ->
                        source.saveTo(file)
                    }
                installExtensionFile.await(file, onError = { toast(it.message.orEmpty()) })
            } catch (e: Exception) {
                log.warn(e) { "Error creating apk file" }
                // todo toast if error
                e.throwIfCancellation()
            }

            getExtensions()
        }
    }

    fun install(extension: Extension) {
        log.info { "Install clicked" }
        scope.launch {
            workingExtensions.update { it + extension.apkName }
            installExtension.await(extension, onError = { toast(it.message.orEmpty()) })
            getExtensions()
            workingExtensions.update { it - extension.apkName }
        }
    }

    fun update(extension: Extension) {
        log.info { "Update clicked" }
        scope.launch {
            workingExtensions.update { it + extension.apkName }
            updateExtension.await(extension, onError = { toast(it.message.orEmpty()) })
            getExtensions()
            workingExtensions.update { it - extension.apkName }
        }
    }

    fun uninstall(extension: Extension) {
        log.info { "Uninstall clicked" }
        scope.launch {
            workingExtensions.update { it + extension.apkName }
            uninstallExtension.await(extension, onError = { toast(it.message.orEmpty()) })
            getExtensions()
            workingExtensions.update { it - extension.apkName }
        }
    }

    fun setEnabledLanguages(langs: Set<String>) {
        _enabledLangs.value = langs
    }

    fun setQuery(query: String) {
        _searchQuery.value = query
    }

    private fun search(
        searchQuery: String?,
        extensionList: List<Extension>?,
        enabledLangs: Set<String>,
        workingExtensions: List<String>,
    ): ImmutableList<ExtensionUI> {
        val extensions = extensionList?.filter { it.lang in enabledLangs }
            .orEmpty()
        return if (searchQuery.isNullOrBlank()) {
            extensions.splitSort(workingExtensions)
        } else {
            val queries = searchQuery.split(" ")
            val filteredExtensions = extensions.toMutableList()
            queries.forEach { query ->
                filteredExtensions.removeAll { !it.name.contains(query, true) }
            }
            filteredExtensions.toList().splitSort(workingExtensions)
        }
    }

    private fun List<Extension>.splitSort(workingExtensions: List<String>): ImmutableList<ExtensionUI> {
        val all = MR.strings.all.toPlatformString()
        return this
            .filter(Extension::installed)
            .sortedWith(
                compareBy<Extension> {
                    when {
                        it.obsolete -> 1
                        it.hasUpdate -> 2
                        else -> 3
                    }
                }
                    .thenBy(Extension::lang)
                    .thenBy(String.CASE_INSENSITIVE_ORDER, Extension::name),
            )
            .map { ExtensionUI.ExtensionItem(it, it.apkName in workingExtensions) }
            .let {
                if (it.isNotEmpty()) {
                    listOf(ExtensionUI.Header(MR.strings.installed.toPlatformString())) + it
                } else {
                    it
                }
            }.plus(
                filterNot(Extension::installed)
                    .groupBy(Extension::lang)
                    .mapKeys { (key) ->
                        when (key) {
                            "all" -> all
                            else -> Locale(key).displayName
                        }
                    }
                    .mapValues {
                        it.value
                            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, Extension::name))
                            .map { ExtensionUI.ExtensionItem(it, it.apkName in workingExtensions) }
                    }
                    .toList()
                    .sortedWith(
                        compareBy<Pair<String, *>> { (key) ->
                            when (key) {
                                all -> 1
                                else -> 2
                            }
                        }.thenBy(String.CASE_INSENSITIVE_ORDER, Pair<String, *>::first),
                    )
                    .flatMap { (key, value) ->
                        listOf(ExtensionUI.Header(key)) + value
                    },
            )
            .toImmutableList()
    }

    private companion object {
        private val log = logging()
    }
}

@Immutable
sealed class ExtensionUI {
    data class Header(
        val header: String,
    ) : ExtensionUI()

    data class ExtensionItem(
        val extension: Extension,
        val isWorking: Boolean = false,
    ) : ExtensionUI()
}
