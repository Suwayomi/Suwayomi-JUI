/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.categories

import ca.gosyer.backend.models.Category
import ca.gosyer.backend.network.interactions.CategoryInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.system.inject
import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging

class CategoriesMenuViewModel : ViewModel() {
    private val httpClient: HttpClient by inject()
    private val logger = KotlinLogging.logger {}
    private var originalCategories = emptyList<Category>()
    private val _categories = MutableStateFlow(emptyList<MenuCategory>())
    val categories = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        getCategories()
    }

    fun getCategories() {
        scope.launch {
            _categories.value = emptyList()
            _isLoading.value = true
            try {
                _categories.value = CategoryInteractionHandler(httpClient).getCategories()
                    .sortedBy { it.order }
                    .also { originalCategories = it }
                    .map { it.toMenuCategory() }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategories(manualUpdate: Boolean = false) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            logger.debug { throwable }
        }
        GlobalScope.launch(handler) {
            val categories = _categories.value
            val newCategories = categories.filter { it.id == null }
            newCategories.forEach {
                CategoryInteractionHandler(httpClient).createCategory(it.name)
            }
            originalCategories.forEach { originalCategory ->
                val category = categories.find { it.id == originalCategory.id }
                if (category == null) {
                    CategoryInteractionHandler(httpClient).deleteCategory(originalCategory)
                } else if (category.name != originalCategory.name) {
                    CategoryInteractionHandler(httpClient).modifyCategory(originalCategory, category.name)
                }
            }
            val updatedCategories = CategoryInteractionHandler(httpClient).getCategories()
            updatedCategories.forEach { updatedCategory ->
                val category = categories.find { it.id == updatedCategory.id || it.name == updatedCategory.name } ?: return@forEach
                if (category.order != updatedCategory.order) {
                    logger.debug { "${category.order} to ${updatedCategory.order}" }
                    CategoryInteractionHandler(httpClient).reorderCategory(updatedCategory, category.order, updatedCategory.order)
                }
            }

            if (manualUpdate) {
                getCategories()
            }
        }
    }

    fun renameCategory(category: MenuCategory, newName: String) {
        _categories.value = (_categories.value - category + category.copy(name = newName)).sortedBy { it.order }
    }

    fun deleteCategory(category: MenuCategory) {
        _categories.value = _categories.value - category
    }

    fun createCategory(name: String) {
        _categories.value += MenuCategory(order = categories.value.size + 1, name = name, landing = false)
    }

    fun moveUp(category: MenuCategory) {
        val categories = _categories.value
        val index = categories.indexOf(category)
        if (index == -1) throw Exception("Invalid index")
        categories[index].order = category.order - 1
        categories[index - 1].order = category.order + 1
        _categories.value = categories.sortedBy { it.order }
    }

    fun moveDown(category: MenuCategory) {
        val categories = _categories.value
        val index = categories.indexOf(category)
        if (index == -1) throw Exception("Invalid index")
        categories[index].order = category.order + 1
        categories[index + 1].order = category.order - 1
        _categories.value = categories.sortedBy { it.order }
    }

    fun Category.toMenuCategory() = MenuCategory(id, order, name, landing)

    data class MenuCategory(val id: Long? = null, var order: Int, val name: String, val landing: Boolean = false)
}