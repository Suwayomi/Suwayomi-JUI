package ca.gosyer.jui.domain.download.model

data class DownloadUpdates(
    val initial: List<DownloadQueueItem>? = null,
    val omittedUpdates: Boolean,
    val state: DownloaderState,
    val updates: List<DownloadUpdate>? = null
)
