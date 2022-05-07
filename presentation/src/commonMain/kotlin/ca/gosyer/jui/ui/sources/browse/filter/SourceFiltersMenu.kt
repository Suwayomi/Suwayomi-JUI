/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.browse.filter

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TriStateCheckbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.data.models.sourcefilters.SortFilter
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.prefs.ExpandablePreference
import ca.gosyer.jui.ui.sources.browse.filter.model.SourceFiltersView
import ca.gosyer.jui.uicore.components.Spinner
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.keyboardHandler
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.resources.stringResource
import kotlinx.coroutines.flow.filterIsInstance

@Composable
fun SourceFiltersMenu(
    modifier: Modifier,
    filters: List<SourceFiltersView<*, *>>,
    onSearchClicked: () -> Unit,
    resetFiltersClicked: () -> Unit
) {
    Surface(elevation = 1.dp, modifier = modifier then Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Surface(elevation = 4.dp) {
                Row(
                    Modifier.height(56.dp).fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(resetFiltersClicked) {
                        Text(stringResource(MR.strings.reset_filters))
                    }
                    Button(onSearchClicked) {
                        Text(stringResource(MR.strings.action_filter))
                    }
                }
            }
            val expandedGroups = remember { mutableStateListOf<Int>() }
            Box {
                val lazyListState = rememberLazyListState()
                LazyColumn(Modifier.fillMaxSize(), lazyListState) {
                    items(
                        items = filters,
                        key = { it.filter.hashCode() }
                    ) { item ->
                        item.toView(startExpanded = item.index in expandedGroups) { expanded, index ->
                            if (expanded) {
                                expandedGroups += index
                            } else {
                                expandedGroups -= index
                            }
                        }
                    }
                }
                VerticalScrollbar(
                    rememberScrollbarAdapter(lazyListState),
                    Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SourceFiltersView<*, *>.toView(startExpanded: Boolean = false, onExpandChanged: ((Boolean, Int) -> Unit)? = null) {
    when (this) {
        is SourceFiltersView.CheckBox -> CheckboxView(this)
        is SourceFiltersView.Group -> GroupView(this, startExpanded, onExpandChanged)
        is SourceFiltersView.Header -> HeaderView(this)
        is SourceFiltersView.Select -> SelectView(this)
        is SourceFiltersView.Separator -> SeparatorView()
        is SourceFiltersView.Sort -> SortView(this, startExpanded, onExpandChanged)
        is SourceFiltersView.Text -> TextView(this)
        is SourceFiltersView.TriState -> TriStateView(this)
    }
}

@Composable
fun SourceFilterAction(
    name: String,
    onClick: () -> Unit,
    action: @Composable () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        action()
        Box(Modifier.padding(horizontal = 16.dp).weight(1f)) {
            Text(
                text = name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.subtitle1,
            )
        }
    }
}

@Composable
fun GroupView(group: SourceFiltersView.Group, startExpanded: Boolean, onExpandChanged: ((Boolean, Int) -> Unit)? = null) {
    val state by group.state.collectAsState()
    ExpandablePreference(
        title = group.name,
        startExpanded = startExpanded,
        onExpandedChanged = {
            onExpandChanged?.invoke(it, group.index)
        }
    ) {
        state.fastForEach {
            it.toView()
        }
    }
}

@Composable
fun CheckboxView(checkBox: SourceFiltersView.CheckBox) {
    val state by checkBox.state.collectAsState()
    SourceFilterAction(
        name = checkBox.name,
        onClick = { checkBox.updateState(!state) },
        action = {
            Checkbox(checked = state, onCheckedChange = null)
        }
    )
}

@Composable
fun HeaderView(header: SourceFiltersView.Header) {
    Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
        Text(
            text = header.name,
            fontWeight = FontWeight.Bold,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
            maxLines = 1,
            style = MaterialTheme.typography.subtitle1,
        )
    }
}

@Composable
fun SelectView(select: SourceFiltersView.Select) {
    val state by select.state.collectAsState()
    Row(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = select.name,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.weight(1f)
        )
        Spinner(
            modifier = Modifier.weight(1f),
            // TODO: 2022-05-06 Remove it.values when we hit server version 0.7.0
            items = select.filter.let { it.displayValues ?: it.values.map(Any::toString) },
            selectedItemIndex = state,
            onSelectItem = select::updateState
        )
    }
}

@Composable
fun SeparatorView() {
    Divider(Modifier.fillMaxWidth())
}

@Composable
fun SortRow(name: String, selected: Boolean, asc: Boolean, onClick: () -> Unit) {
    SourceFilterAction(name, onClick) {
        if (selected) {
            val rotation = if (asc) {
                0F
            } else {
                180F
            }
            val angle: Float by animateFloatAsState(
                targetValue = if (rotation > 360 - rotation) { -(360 - rotation) } else rotation,
                animationSpec = tween(
                    durationMillis = 500, // rotation is retrieved with this frequency
                    easing = LinearEasing
                )
            )

            Icon(
                imageVector = Icons.Rounded.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.rotate(angle),
                tint = MaterialTheme.colors.primary
            )
        } else {
            Box(Modifier.size(24.dp))
        }
    }
}

@Composable
fun SortView(sort: SourceFiltersView.Sort, startExpanded: Boolean, onExpandChanged: ((Boolean, Int) -> Unit)?) {
    val state by sort.state.collectAsState()
    ExpandablePreference(
        sort.name,
        startExpanded = startExpanded,
        onExpandedChanged = {
            onExpandChanged?.invoke(it, sort.index)
        }
    ) {
        Column(Modifier.fillMaxWidth()) {
            sort.filter.values.forEachIndexed { index, name ->
                SortRow(
                    name = name,
                    selected = state?.index == index,
                    asc = state?.ascending ?: false
                ) {
                    sort.updateState(
                        value = SortFilter.Selection(
                            index,
                            if (state?.index == index) {
                                state?.ascending?.not() ?: false
                            } else false
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TextView(text: SourceFiltersView.Text) {
    val placeholderText = remember { text.filter.name }
    val state by text.state.collectAsState()
    var stateText by remember(state) {
        mutableStateOf(
            if (state == placeholderText) {
                ""
            } else state
        )
    }
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.filterIsInstance<FocusInteraction.Unfocus>().collect {
            text.updateState(stateText)
        }
    }

    OutlinedTextField(
        value = stateText,
        onValueChange = { stateText = it },
        singleLine = true,
        maxLines = 1,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth()
            .keyboardHandler(singleLine = true) { it.clearFocus() },
        placeholder = if (placeholderText.isNotEmpty()) {
            { Text(placeholderText) }
        } else {
            null
        }
    )
}

@Composable
fun TriStateView(triState: SourceFiltersView.TriState) {
    val state by triState.state.collectAsState()
    SourceFilterAction(
        name = triState.name,
        onClick = {
            triState.updateState(
                when (state) {
                    0 -> 1
                    1 -> 2
                    else -> 0
                }
            )
        },
        action = {
            TriStateCheckbox(
                state = when (state) {
                    1 -> ToggleableState.On
                    2 -> ToggleableState.Indeterminate
                    else -> ToggleableState.Off
                },
                onClick = null
            )
        }
    )
}
