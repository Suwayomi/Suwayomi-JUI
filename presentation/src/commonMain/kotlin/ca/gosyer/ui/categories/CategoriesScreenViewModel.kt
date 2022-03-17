/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.categories

import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.models.Category
import ca.gosyer.data.server.interactions.CategoryInteractionHandler
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject

class CategoriesScreenViewModel @Inject constructor(
    private val categoryHandler: CategoryInteractionHandler,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
    private var originalCategories = emptyList<Category>()
    private val _categories = MutableStateFlow(emptyList<MenuCategory>())
    val categories = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        getCategories()
    }

    private fun getCategories() {
        _categories.value = emptyList()
        _isLoading.value = true
        categoryHandler.getCategories(true)
            .onEach {
                _categories.value = it
                    .sortedBy { it.order }
                    .also { originalCategories = it }
                    .map { it.toMenuCategory() }
                _isLoading.value = false
            }
            .catch {
                info(it) { "Error getting categories" }
                _isLoading.value = false
            }
            .launchIn(scope)
    }

    suspend fun updateRemoteCategories(manualUpdate: Boolean = false) {
        val categories = _categories.value
        val newCategories = categories.filter { it.id == null }
        newCategories.forEach {
            categoryHandler.createCategory(it.name)
                .catch {
                    info(it) { "Error creating category" }
                }
                .collect()
        }
        originalCategories.forEach { originalCategory ->
            val category = categories.find { it.id == originalCategory.id }
            if (category == null) {
                categoryHandler.deleteCategory(originalCategory)
                    .catch {
                        info(it) { "Error deleting category $originalCategory" }
                    }
                    .collect()
            } else if (category.name != originalCategory.name) {
                categoryHandler.modifyCategory(originalCategory, category.name)
                    .catch {
                        info(it) { "Error modifying category $category" }
                    }
                    .collect()
            }
        }
        var updatedCategories = categoryHandler.getCategories(true)
            .catch {
                info(it) { "Error getting updated categories" }
            }
            .singleOrNull()
        categories.forEach { category ->
            val updatedCategory = updatedCategories?.find { it.id == category.id || it.name == category.name } ?: return@forEach
            if (category.order != updatedCategory.order) {
                debug { "${category.name}: ${updatedCategory.order} to ${category.order}" }
                categoryHandler.reorderCategory(category.order, updatedCategory.order)
                    .catch {
                        info(it) { "Error re-ordering categories" }
                    }
                    .singleOrNull()
            }
            updatedCategories = categoryHandler.getCategories(true)
                .catch {
                    info(it) { "Error getting updated categories" }
                }
                .singleOrNull()
        }

        if (manualUpdate) {
            getCategories()
        }
    }

    fun renameCategory(category: MenuCategory, newName: String) {
        _categories.value = (_categories.value - category + category.copy(name = newName)).sortedBy { it.order }
    }

    fun deleteCategory(category: MenuCategory) {
        _categories.value = _categories.value - category
    }

    fun createCategory(name: String) {
        _categories.value += MenuCategory(order = categories.value.size + 1, name = name, default = false)
    }

    fun moveUp(category: MenuCategory) {
        val categories = _categories.value.toMutableList()
        val index = categories.indexOf(category)
        if (index == -1) throw Exception("Invalid index")
        categories.add(index - 1, categories.removeAt(index))
        categories.forEachIndexed { i, _ ->
            categories[i].order = i + 1
        }
        _categories.value = categories.sortedBy { it.order }.toList()
    }

    fun moveDown(category: MenuCategory) {
        val categories = _categories.value.toMutableList()
        val index = categories.indexOf(category)
        if (index == -1) throw Exception("Invalid index")
        categories.add(index + 1, categories.removeAt(index))
        categories.forEachIndexed { i, _ ->
            categories[i].order = i + 1
        }
        _categories.value = categories.sortedBy { it.order }.toList()
    }

    private fun Category.toMenuCategory() = MenuCategory(id, order, name, default)

    data class MenuCategory(val id: Long? = null, var order: Int, val name: String, val default: Boolean = false)

    private companion object : CKLogger({})
}
