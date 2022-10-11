/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.core.lang.addSuffix
import ca.gosyer.jui.domain.backup.service.BackupRepository
import ca.gosyer.jui.domain.category.service.CategoryRepository
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.createIt
import ca.gosyer.jui.domain.download.service.DownloadRepository
import ca.gosyer.jui.domain.extension.service.ExtensionRepository
import ca.gosyer.jui.domain.library.service.LibraryRepository
import ca.gosyer.jui.domain.manga.service.MangaRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.settings.service.SettingsRepository
import ca.gosyer.jui.domain.source.service.SourceRepository
import ca.gosyer.jui.domain.updates.service.UpdatesRepository
import de.jensklingenberg.ktorfit.Ktorfit
import me.tatarka.inject.annotations.Provides

interface DataComponent {

    @Provides
    fun ktorfit(http: Http, serverPreferences: ServerPreferences) = Ktorfit
        .Builder()
        .httpClient(http)
        .responseConverter(FlowIOResponseConverter())
        .baseUrl(serverPreferences.serverUrl().get().toString().addSuffix('/'))
        .build()

    @Provides
    fun backupRepository(ktorfit: Ktorfit) = ktorfit.createIt<BackupRepository>()

    @Provides
    fun categoryRepository(ktorfit: Ktorfit) = ktorfit.createIt<CategoryRepository>()

    @Provides
    fun chapterRepository(ktorfit: Ktorfit) = ktorfit.createIt<ChapterRepository>()

    @Provides
    fun downloadRepository(ktorfit: Ktorfit) = ktorfit.createIt<DownloadRepository>()

    @Provides
    fun extensionRepository(ktorfit: Ktorfit) = ktorfit.createIt<ExtensionRepository>()

    @Provides
    fun libraryRepository(ktorfit: Ktorfit) = ktorfit.createIt<LibraryRepository>()

    @Provides
    fun mangaRepository(ktorfit: Ktorfit) = ktorfit.createIt<MangaRepository>()

    @Provides
    fun settingsRepository(ktorfit: Ktorfit) = ktorfit.createIt<SettingsRepository>()

    @Provides
    fun sourceRepository(ktorfit: Ktorfit) = ktorfit.createIt<SourceRepository>()

    @Provides
    fun updatesRepository(ktorfit: Ktorfit) = ktorfit.createIt<UpdatesRepository>()
}
