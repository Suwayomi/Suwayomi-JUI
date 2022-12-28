/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.jui.core.util.replace
import ca.gosyer.jui.domain.manga.model.MangaMeta
import ca.gosyer.jui.domain.reader.model.Direction
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.reader.model.ReaderChapter
import ca.gosyer.jui.ui.reader.model.ReaderItem
import ca.gosyer.jui.uicore.components.AroundLayout
import ca.gosyer.jui.uicore.components.Spinner
import ca.gosyer.jui.uicore.resources.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlin.math.roundToInt

@Composable
fun ReaderSideMenu(
    chapter: ReaderChapter,
    pages: ImmutableList<ReaderItem>,
    currentPage: ReaderItem?,
    readerModes: ImmutableList<String>,
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
            ReaderMenuToolbar(onCloseSideMenuClicked = onCloseSideMenuClicked)
            ReaderModeSetting(
                readerModes = readerModes,
                selectedMode = selectedMode,
                onSetReaderMode = onSetReaderMode
            )
            ReaderProgressSlider(
                pages = pages,
                currentPage = currentPage,
                pageCount = pageCount,
                onNewPageClicked = onNewPageClicked,
                isRtL = false
            )
            NavigateChapters(
                loadPrevChapter = onPrevChapterClicked,
                loadNextChapter = onNextChapterClicked
            )
        }
    }
}

@Composable
fun ReaderExpandBottomMenu(
    modifier: Modifier,
    previousChapter: ReaderChapter?,
    chapter: ReaderChapter,
    nextChapter: ReaderChapter?,
    direction: Direction,
    pages: ImmutableList<ReaderItem>,
    currentPage: ReaderItem?,
    navigate: (Int) -> Unit,
    readerMenuOpen: Boolean,
    movePrevChapter: () -> Unit,
    moveNextChapter: () -> Unit
) {
    AnimatedVisibility(
        readerMenuOpen,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        val isRtL = direction == Direction.Left
        AroundLayout(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp).height(48.dp),
            startLayout = {
                val (text, onClick, enabled) = if (!isRtL) {
                    Triple(stringResource(MR.strings.previous_chapter), movePrevChapter, previousChapter != null)
                } else {
                    Triple(stringResource(MR.strings.next_chapter), moveNextChapter, nextChapter != null)
                }
                Card(
                    onClick = onClick,
                    modifier = Modifier.fillMaxHeight()
                        .aspectRatio(1F, true),
                    shape = CircleShape,
                    backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5F),
                    enabled = enabled
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.SkipPrevious, text)
                    }
                }
            },
            endLayout = {
                val (text, onClick, enabled) = if (isRtL) {
                    Triple(stringResource(MR.strings.previous_chapter), movePrevChapter, previousChapter != null)
                } else {
                    Triple(stringResource(MR.strings.next_chapter), moveNextChapter, nextChapter != null)
                }
                Card(
                    onClick = onClick,
                    modifier = Modifier.fillMaxHeight()
                        .aspectRatio(1F, true),
                    shape = CircleShape,
                    backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5F),
                    enabled = enabled
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.SkipNext, text)
                    }
                }
            }
        ) {
            Card(
                modifier = Modifier.fillMaxSize().padding(it).padding(horizontal = 8.dp),
                shape = CircleShape,
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5F)
            ) {
                AroundLayout(
                    Modifier.padding(horizontal = 8.dp),
                    startLayout = {
                        Box(Modifier.fillMaxHeight().width(32.dp), contentAlignment = Alignment.Center) {
                            val text = if (!isRtL) {
                                pages.indexOf(currentPage)
                            } else {
                                chapter.chapter.pageCount!!
                            }.toString()
                            Text(text, fontSize = 15.sp)
                        }
                    },
                    endLayout = {
                        Box(Modifier.fillMaxHeight().width(32.dp), contentAlignment = Alignment.Center) {
                            val text = if (isRtL) {
                                pages.indexOf(currentPage)
                            } else {
                                chapter.chapter.pageCount!!
                            }.toString()
                            Text(text, fontSize = 15.sp)
                        }
                    }
                ) { paddingValues ->
                    ReaderProgressSlider(
                        modifier = Modifier.fillMaxWidth()
                            .padding(paddingValues)
                            .padding(horizontal = 4.dp),
                        pages = pages,
                        currentPage = currentPage,
                        pageCount = chapter.chapter.pageCount!!,
                        onNewPageClicked = navigate,
                        isRtL = isRtL
                    )
                }
            }
        }
    }
}

@Composable
fun ReaderSheet(
    readerModes: ImmutableList<String>,
    selectedMode: String,
    onSetReaderMode: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        ReaderModeSetting(readerModes, selectedMode, onSetReaderMode)
    }
}

@Composable
fun ReaderModeSetting(readerModes: ImmutableList<String>, selectedMode: String, onSetReaderMode: (String) -> Unit) {
    val modes by derivedStateOf { persistentListOf(MangaMeta.DEFAULT_READER_MODE) + readerModes }
    val defaultModeString = stringResource(MR.strings.default_reader_mode)
    val displayModes by derivedStateOf { modes.replace(0, defaultModeString) }
    val selectedModeIndex by derivedStateOf { modes.indexOf(selectedMode) }
    Row(
        Modifier.fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(MR.strings.reader_mode), Modifier.weight(0.25f), maxLines = 2, fontSize = 14.sp)
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
    modifier: Modifier = Modifier,
    pages: ImmutableList<ReaderItem>,
    currentPage: ReaderItem?,
    pageCount: Int,
    onNewPageClicked: (Int) -> Unit,
    isRtL: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = pages.indexOf(currentPage).toFloat(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    var isValueChanging by remember { mutableStateOf(false) }
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
        onValueChangeFinished = { isValueChanging = false },
        modifier = modifier.let {
            if (isRtL) {
                it then Modifier.rotate(180F)
            } else {
                it
            }
        }
    )
}

@Composable
private fun NavigateChapters(loadPrevChapter: () -> Unit, loadNextChapter: () -> Unit) {
    Divider(Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
    Row(horizontalArrangement = Arrangement.SpaceBetween) {
        OutlinedButton(loadPrevChapter, Modifier.weight(0.5F)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val nextChapter = stringResource(MR.strings.nav_prev_chapter)
                Icon(Icons.Rounded.NavigateBefore, nextChapter)
                Text(nextChapter, fontSize = 10.sp)
            }
        }
        OutlinedButton(loadNextChapter, Modifier.weight(0.5F)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val nextChapter = stringResource(MR.strings.nav_next_chapter)
                Text(nextChapter, fontSize = 10.sp)
                Icon(Icons.Rounded.NavigateNext, nextChapter)
            }
        }
    }
}

/*@Preview
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
}*/
