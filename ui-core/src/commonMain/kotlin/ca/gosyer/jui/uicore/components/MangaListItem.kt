/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.image.ImageLoaderImage
import ca.gosyer.jui.uicore.resources.stringResource

@Composable
fun MangaListItem(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
fun MangaListItemImage(
    modifier: Modifier = Modifier,
    data: Any,
    contentDescription: String,
) {
    ImageLoaderImage(
        data,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.Medium,
    )
}

@Composable
fun MangaListItemColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        content()
    }
}

@Composable
fun MangaListItemTitle(
    modifier: Modifier = Modifier,
    text: String,
    bookmarked: Boolean = false,
    maxLines: Int = 1,
    fontWeight: FontWeight = FontWeight.Normal,
    textColor: Color = Color.Unspecified,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        var textHeight by remember { mutableStateOf(0) }
        if (bookmarked) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = stringResource(MR.strings.action_filter_bookmarked),
                modifier = Modifier
                    .sizeIn(maxHeight = with(LocalDensity.current) { textHeight.toDp() - 2.dp }),
                tint = MaterialTheme.colors.primary,
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
        Text(
            modifier = modifier,
            text = text,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.h5,
            fontWeight = fontWeight,
            color = textColor,
            onTextLayout = {
                textHeight = it.size.height
            },
        )
    }
}

@Composable
fun MangaListItemSubtitle(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = Color.Unspecified,
) {
    Text(
        modifier = modifier,
        text = text,
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.body1,
    )
}
