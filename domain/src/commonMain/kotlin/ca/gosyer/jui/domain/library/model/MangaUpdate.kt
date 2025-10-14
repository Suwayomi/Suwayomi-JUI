package ca.gosyer.jui.domain.library.model

data class MangaUpdate(
    val manga: UpdateManga,
    val status: Status,
) {
    enum class Status {
        PENDING,
        RUNNING,
        COMPLETE,
        FAILED,
        SKIPPED
    }

    data class UpdateManga(
        val id: Long,
        val title: String,
    )
}
