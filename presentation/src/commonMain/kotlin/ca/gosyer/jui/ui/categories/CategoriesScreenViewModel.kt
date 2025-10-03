/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.categories

import androidx.compose.runtime.Stable
import ca.gosyer.jui.domain.category.interactor.CreateCategory
import ca.gosyer.jui.domain.category.interactor.DeleteCategory
import ca.gosyer.jui.domain.category.interactor.GetCategories
import ca.gosyer.jui.domain.category.interactor.ModifyCategory
import ca.gosyer.jui.domain.category.interactor.ReorderCategory
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class CategoriesScreenViewModel
    @Inject
    constructor(
        private val getCategories: GetCategories,
        private val createCategory: CreateCategory,
        private val deleteCategory: DeleteCategory,
        private val modifyCategory: ModifyCategory,
        private val reorderCategory: ReorderCategory,
        contextWrapper: ContextWrapper,
    ) : ViewModel(contextWrapper) {
        private var originalCategories = emptyList<Category>()
        private val _categories = MutableStateFlow<ImmutableList<MenuCategory>>(persistentListOf())
        val categories = _categories.asStateFlow()

        init {
            scope.launch {
                getCategories()
            }
        }

        private suspend fun getCategories() {
            _categories.value = persistentListOf()
            val categories = getCategories.await(true, onError = { toast(it.message.orEmpty()) })
            if (categories != null) {
                _categories.value = categories
                    .sortedBy { it.order }
                    .also { originalCategories = it }
                    .map { it.toMenuCategory() }
                    .toImmutableList()
            }
        }

        suspend fun updateRemoteCategories(manualUpdate: Boolean = false) {
            val categories = _categories.value
            val newCategories = categories.filter { it.id == null }
            newCategories.forEach {
                createCategory.await(it.name, onError = { toast(it.message.orEmpty()) })
            }
            originalCategories.forEach { originalCategory ->
                val category = categories.find { it.id == originalCategory.id }
                if (category == null) {
                    deleteCategory.await(originalCategory, onError = { toast(it.message.orEmpty()) })
                } else if (category.name != originalCategory.name) {
                    modifyCategory.await(originalCategory, category.name, onError = { toast(it.message.orEmpty()) })
                }
            }
            var updatedCategories = getCategories.await(true, onError = { toast(it.message.orEmpty()) })
            categories.sortedBy { it.order }.forEach { category ->
                val updatedCategory = updatedCategories?.find { it.id == category.id || it.name == category.name } ?: return@forEach
                if (category.order != updatedCategory.order) {
                    log.debug { "${category.name}: ${updatedCategory.order} to ${category.order}" }
                    reorderCategory.await(category.id!!, category.order, onError = { toast(it.message.orEmpty()) })
                }
                updatedCategories = getCategories.await(true, onError = { toast(it.message.orEmpty()) })
            }

            if (manualUpdate) {
                getCategories()
            }
        }

        fun renameCategory(
            category: MenuCategory,
            newName: String,
        ) {
            _categories.value = (_categories.value.toPersistentList() - category + category.copy(name = newName)).sortedBy { it.order }
                .toImmutableList()
        }

        fun deleteCategory(category: MenuCategory) {
            _categories.value = _categories.value.toPersistentList() - category
        }

        fun createCategory(name: String) {
            _categories.value =
                _categories.value.toPersistentList() + MenuCategory(order = categories.value.size + 1, name = name, default = false)
        }

        fun moveUp(category: MenuCategory) {
            val categories = _categories.value.toMutableList()
            val index = categories.indexOf(category)
            if (index == -1) throw Exception("Invalid index")
            categories.add(index - 1, categories.removeAt(index))
            _categories.value = categories
                .mapIndexed { i, menuCategory ->
                    menuCategory.copy(order = i + 1)
                }
                .sortedBy { it.order }
                .toImmutableList()
        }

        fun moveDown(category: MenuCategory) {
            val categories = _categories.value.toMutableList()
            val index = categories.indexOf(category)
            if (index == -1) throw Exception("Invalid index")
            categories.add(index + 1, categories.removeAt(index))
            _categories.value = categories
                .mapIndexed { i, menuCategory ->
                    menuCategory.copy(order = i + 1)
                }
                .sortedBy { it.order }
                .toImmutableList()
        }

        private fun Category.toMenuCategory() = MenuCategory(id, order, name, default)

        @Stable
        data class MenuCategory(
            val id: Long? = null,
            val order: Int,
            val name: String,
            val default: Boolean = false,
        )

        private companion object {
            private val log = logging()
        }
    }
