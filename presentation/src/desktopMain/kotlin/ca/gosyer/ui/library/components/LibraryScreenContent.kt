/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import ca.gosyer.data.library.model.DisplayMode
import ca.gosyer.data.models.Category
import ca.gosyer.data.models.Manga
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.uicore.components.LoadingScreen
import ca.gosyer.uicore.resources.stringResource

@Composable
fun LibraryScreenContent(
    categories: List<Category>,
    selectedCategoryIndex: Int,
    displayMode: DisplayMode,
    isLoading: Boolean,
    error: String?,
    query: String,
    updateQuery: (String) -> Unit,
    getLibraryForPage: @Composable (Long) -> State<List<Manga>>,
    onPageChanged: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit
) {
    // val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    if (categories.isEmpty()) {
        LoadingScreen(isLoading, errorMessage = error)
    } else {
        /*ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetContent = { *//*LibrarySheet()*//* }
        ) {*/
        Column(Modifier.fillMaxWidth()) {
            /*Toolbar(
                title = {
                    val text = if (vm.showCategoryTabs) {
                        stringResource(R.string.library_label)
                    } else {
                        vm.selectedCategory?.visibleName.orEmpty()
                    }
                    Text(text)
                },
                actions = {
                    IconButton(onClick = { scope.launch { sheetState.show() }}) {
                        Icon(Icons.Rounded.FilterList, contentDescription = null)
                    }
                }
            )*/
            Toolbar(
                stringResource(MR.strings.location_library),
                searchText = query,
                search = updateQuery
            )
            LibraryTabs(
                visible = true, // vm.showCategoryTabs,
                categories = categories,
                selectedPage = selectedCategoryIndex,
                onPageChanged = onPageChanged
            )
            LibraryPager(
                categories = categories,
                displayMode = displayMode,
                selectedPage = selectedCategoryIndex,
                getLibraryForPage = getLibraryForPage,
                onPageChanged = onPageChanged,
                onClickManga = onClickManga,
                onRemoveMangaClicked = onRemoveMangaClicked
            )
        }
        // }
    }
}
