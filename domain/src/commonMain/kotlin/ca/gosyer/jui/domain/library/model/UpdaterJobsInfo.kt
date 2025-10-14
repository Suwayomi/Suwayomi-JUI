package ca.gosyer.jui.domain.library.model

data class UpdaterJobsInfo(
    val finishedJobs: Int,
    val isRunning: Boolean,
    val skippedCategoriesCount: Int,
    val skippedMangasCount: Int,
    val totalJobs: Int,
)
