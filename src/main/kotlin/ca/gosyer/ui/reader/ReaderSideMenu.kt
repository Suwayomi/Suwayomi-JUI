/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.common.util.replace
import ca.gosyer.data.models.Chapter
import ca.gosyer.data.models.ChapterMeta
import ca.gosyer.data.models.MangaMeta
import ca.gosyer.ui.base.components.Spinner
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.util.lang.milliseconds
import ca.gosyer.util.system.kLogger
import kotlin.math.roundToInt

private val logger = kLogger {}

@Composable
fun ReaderSideMenu(
    chapter: ReaderChapter,
    currentPage: Int,
    readerModes: List<String>,
    selectedMode: String,
    onNewPageClicked: (Int) -> Unit,
    onCloseSideMenuClicked: () -> Unit,
    onSetReaderMode: (String) -> Unit,
    onPrevChapterClicked: () -> Unit,
    onNextChapterClicked: () -> Unit
) {
    Surface(Modifier.fillMaxHeight().width(260.dp)) {
        Column(Modifier.fillMaxSize()) {
            val pageCount = chapter.chapter.pageCount!!
            ReaderMenuToolbar(onCloseSideMenuClicked)
            ReaderModeSetting(readerModes, selectedMode, onSetReaderMode)
            ReaderProgressSlider(currentPage, pageCount, onNewPageClicked)
            NavigateChapters(onPrevChapterClicked, onNextChapterClicked)
        }
    }
}

@Composable
fun ReaderModeSetting(readerModes: List<String>, selectedMode: String, onSetReaderMode: (String) -> Unit) {
    val modes = remember(readerModes) { listOf(MangaMeta.DEFAULT_READER_MODE) + readerModes }
    val defaultModeString = stringResource("default_reader_mode")
    val displayModes = remember(modes, defaultModeString) { modes.replace(0, defaultModeString) }
    val selectedModeIndex = remember(modes, selectedMode) { modes.indexOf(selectedMode) }
    Row(
        Modifier.fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource("reader_mode"), Modifier.weight(0.25f), maxLines = 2, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Spinner(
            modifier = Modifier.weight(0.75f),
            items = displayModes,
            selectedItemIndex = selectedModeIndex
        ) {
            onSetReaderMode(modes[it])
        }
    }
}

@Composable
private fun ReaderMenuToolbar(onCloseSideMenuClicked: () -> Unit) {
    Surface(elevation = 2.dp) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onCloseSideMenuClicked) {
                Icon(Icons.Rounded.ChevronLeft, null)
            }
        }
    }
}

@Composable
private fun ReaderProgressSlider(
    currentPage: Int,
    pageCount: Int,
    onNewPageClicked: (Int) -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = currentPage.toFloat(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    var isValueChanging by remember { mutableStateOf(false) }
    Row {
        Slider(
            animatedProgress,
            onValueChange = {
                if (!isValueChanging) {
                    isValueChanging = true
                    onNewPageClicked(it.roundToInt())
                }
            },
            valueRange = 0F..pageCount.toFloat(),
            steps = pageCount,
            onValueChangeFinished = { isValueChanging = false }
        )
    }
}

@Composable
private fun NavigateChapters(loadPrevChapter: () -> Unit, loadNextChapter: () -> Unit) {
    Divider(Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
    Row(horizontalArrangement = Arrangement.SpaceBetween,) {
        OutlinedButton(loadPrevChapter, Modifier.weight(0.5F)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val nextChapter = stringResource("nav_prev_chapter")
                Icon(Icons.Rounded.NavigateBefore, nextChapter)
                Text(nextChapter, fontSize = 10.sp)
            }
        }
        OutlinedButton(loadNextChapter, Modifier.weight(0.5F)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val nextChapter = stringResource("nav_next_chapter")
                Text(nextChapter, fontSize = 10.sp)
                Icon(Icons.Rounded.NavigateNext, nextChapter)
            }
        }
    }
}

@Preview
@Composable
private fun ReaderSideMenuPreview() {
    ReaderSideMenu(
        chapter = remember {
            ReaderChapter(
                Chapter(
                    url = "",
                    name = "Test Chapter",
                    uploadDate = System.currentTimeMillis(),
                    chapterNumber = 15.5F,
                    scanlator = "No Group",
                    mangaId = 100L,
                    read = false,
                    bookmarked = false,
                    lastPageRead = 11,
                    index = 10,
                    fetchedAt = System.currentTimeMillis(),
                    chapterCount = null,
                    pageCount = 20,
                    lastReadAt = System.currentTimeMillis().milliseconds.inWholeSeconds.toInt(),
                    downloaded = false,
                    ChapterMeta()
                )
            )
        },
        currentPage = 11,
        readerModes = listOf("Vertical"),
        selectedMode = "Vertical",
        onNewPageClicked = {},
        onCloseSideMenuClicked = {},
        onSetReaderMode = {},
        onPrevChapterClicked = {},
        onNextChapterClicked = {}
    )
}
