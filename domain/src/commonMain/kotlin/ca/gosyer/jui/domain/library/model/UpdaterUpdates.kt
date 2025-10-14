package ca.gosyer.jui.domain.library.model

data class UpdaterUpdates(
    val initial: UpdateStatus?,
    val categoryUpdates: List<CategoryUpdate>,
    val mangaUpdates: List<MangaUpdate>,
    val jobsInfo: UpdaterJobsInfo,
    val omittedUpdates: Boolean,
)
