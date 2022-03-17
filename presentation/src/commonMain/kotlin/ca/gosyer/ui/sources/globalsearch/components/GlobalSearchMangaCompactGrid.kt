/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.globalsearch.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.data.models.Manga
import ca.gosyer.ui.sources.browse.components.SourceMangaBadges
import ca.gosyer.uicore.components.mangaAspectRatio
import ca.gosyer.uicore.image.KamelImage
import io.kamel.image.lazyPainterResource

@Composable
fun GlobalSearchMangaCompactGridItem(
    modifier: Modifier,
    manga: Manga,
    inLibrary: Boolean
) {
    val cover = lazyPainterResource(manga, filterQuality = FilterQuality.Medium)
    val fontStyle = LocalTextStyle.current.merge(
        TextStyle(letterSpacing = 0.sp, fontFamily = FontFamily.SansSerif, fontSize = 14.sp)
    )

    Box(
        modifier = Modifier.padding(4.dp)
            .height(200.dp)
            .aspectRatio(mangaAspectRatio, true)
            .clip(MaterialTheme.shapes.medium) then modifier
    ) {
        KamelImage(
            cover,
            manga.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().then(shadowGradient))
        Text(
            text = manga.title,
            color = Color.White,
            style = fontStyle,
            maxLines = 2,
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
        )
        SourceMangaBadges(
            inLibrary = inLibrary,
            modifier = Modifier.padding(4.dp)
        )
    }
}

private val shadowGradient = Modifier.drawWithCache {
    val gradient = Brush.linearGradient(
        0.75f to Color.Transparent,
        1.0f to Color(0xAA000000),
        start = Offset(0f, 0f),
        end = Offset(0f, size.height)
    )
    onDrawBehind {
        drawRect(gradient)
    }
}
