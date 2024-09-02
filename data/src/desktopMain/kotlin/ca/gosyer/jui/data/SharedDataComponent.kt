package ca.gosyer.jui.data

import ca.gosyer.jui.domain.backup.service.createBackupRepositoryOld
import ca.gosyer.jui.domain.category.service.createCategoryRepositoryOld
import ca.gosyer.jui.domain.chapter.service.createChapterRepositoryOld
import ca.gosyer.jui.domain.download.service.createDownloadRepositoryOld
import ca.gosyer.jui.domain.extension.service.createExtensionRepositoryOld
import ca.gosyer.jui.domain.global.service.createGlobalRepositoryOld
import ca.gosyer.jui.domain.library.service.createLibraryRepositoryOld
import ca.gosyer.jui.domain.manga.service.createMangaRepositoryOld
import ca.gosyer.jui.domain.settings.service.createSettingsRepositoryOld
import ca.gosyer.jui.domain.source.service.createSourceRepositoryOld
import ca.gosyer.jui.domain.updates.service.createUpdatesRepositoryOld
import de.jensklingenberg.ktorfit.Ktorfit
import me.tatarka.inject.annotations.Provides

actual interface SharedDataComponent {
    @Provides
    fun backupRepositoryOld(ktorfit: Ktorfit) = ktorfit.createBackupRepositoryOld()

    @Provides
    fun categoryRepositoryOld(ktorfit: Ktorfit) = ktorfit.createCategoryRepositoryOld()

    @Provides
    fun chapterRepositoryOld(ktorfit: Ktorfit) = ktorfit.createChapterRepositoryOld()

    @Provides
    fun downloadRepositoryOld(ktorfit: Ktorfit) = ktorfit.createDownloadRepositoryOld()

    @Provides
    fun extensionRepositoryOld(ktorfit: Ktorfit) = ktorfit.createExtensionRepositoryOld()

    @Provides
    fun globalRepositoryOld(ktorfit: Ktorfit) = ktorfit.createGlobalRepositoryOld()

    @Provides
    fun libraryRepositoryOld(ktorfit: Ktorfit) = ktorfit.createLibraryRepositoryOld()

    @Provides
    fun mangaRepositoryOld(ktorfit: Ktorfit) = ktorfit.createMangaRepositoryOld()

    @Provides
    fun settingsRepositoryOld(ktorfit: Ktorfit) = ktorfit.createSettingsRepositoryOld()

    @Provides
    fun sourceRepositoryOld(ktorfit: Ktorfit) = ktorfit.createSourceRepositoryOld()

    @Provides
    fun updatesRepositoryOld(ktorfit: Ktorfit) = ktorfit.createUpdatesRepositoryOld()
}
