package ca.gosyer.jui.domain.library.model

data class CategoryUpdate(
    val category: UpdateCategory,
    val status: Status,
) {
    enum class Status {
        UPDATING,
        SKIPPED
    }

    data class UpdateCategory(
        val id: Long,
        val name: String,
    )
}

