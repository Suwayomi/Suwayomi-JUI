package ca.gosyer.jui.domain.download.model

data class DownloadUpdate(
    val type: DownloadUpdateType? = null,
    val download: DownloadQueueItem? = null
)

enum class DownloadUpdateType {
    QUEUED,
    DEQUEUED,
    PAUSED,
    STOPPED,
    PROGRESS,
    FINISHED,
    ERROR,
    POSITION
}
