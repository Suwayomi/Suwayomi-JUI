/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data

import ca.gosyer.jui.data.backup.BackupRepositoryImpl
import ca.gosyer.jui.data.category.CategoryRepositoryImpl
import ca.gosyer.jui.data.chapter.ChapterRepositoryImpl
import ca.gosyer.jui.data.download.DownloadRepositoryImpl
import ca.gosyer.jui.data.extension.ExtensionRepositoryImpl
import ca.gosyer.jui.data.library.LibraryRepositoryImpl
import ca.gosyer.jui.data.manga.MangaRepositoryImpl
import ca.gosyer.jui.data.settings.SettingsRepositoryImpl
import ca.gosyer.jui.data.source.SourceRepositoryImpl
import ca.gosyer.jui.data.updates.UpdatesRepositoryImpl
import ca.gosyer.jui.domain.backup.service.BackupRepository
import ca.gosyer.jui.domain.category.service.CategoryRepository
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.download.service.DownloadRepository
import ca.gosyer.jui.domain.extension.service.ExtensionRepository
import ca.gosyer.jui.domain.library.service.LibraryRepository
import ca.gosyer.jui.domain.manga.service.MangaRepository
import ca.gosyer.jui.domain.settings.service.SettingsRepository
import ca.gosyer.jui.domain.source.service.SourceRepository
import ca.gosyer.jui.domain.updates.service.UpdatesRepository
import me.tatarka.inject.annotations.Provides

interface DataComponent {

    val BackupRepositoryImpl.bind: BackupRepository
        @Provides get() = this

    val CategoryRepositoryImpl.bind: CategoryRepository
        @Provides get() = this

    val ChapterRepositoryImpl.bind: ChapterRepository
        @Provides get() = this

    val DownloadRepositoryImpl.bind: DownloadRepository
        @Provides get() = this

    val ExtensionRepositoryImpl.bind: ExtensionRepository
        @Provides get() = this

    val LibraryRepositoryImpl.bind: LibraryRepository
        @Provides get() = this

    val MangaRepositoryImpl.bind: MangaRepository
        @Provides get() = this

    val SettingsRepositoryImpl.bind: SettingsRepository
        @Provides get() = this

    val SourceRepositoryImpl.bind: SourceRepository
        @Provides get() = this

    val UpdatesRepositoryImpl.bind: UpdatesRepository
        @Provides get() = this
}
