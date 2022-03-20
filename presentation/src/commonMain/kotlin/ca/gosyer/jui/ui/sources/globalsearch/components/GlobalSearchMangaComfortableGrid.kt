/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.globalsearch.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import ca.gosyer.jui.data.models.Manga
import ca.gosyer.jui.ui.sources.browse.components.SourceMangaBadges
import ca.gosyer.jui.uicore.components.mangaAspectRatio
import ca.gosyer.jui.uicore.image.KamelImage
import io.kamel.image.lazyPainterResource

@Composable
fun GlobalSearchMangaComfortableGridItem(
    modifier: Modifier,
    manga: Manga,
    inLibrary: Boolean
) {
    val cover = lazyPainterResource(manga, filterQuality = FilterQuality.Medium)
    val fontStyle = LocalTextStyle.current.merge(
        TextStyle(letterSpacing = 0.sp, fontFamily = FontFamily.SansSerif, fontSize = 14.sp)
    )

    Box(
        modifier = Modifier
            .padding(4.dp)
            .width((mangaAspectRatio * 200.dp))
            .clip(MaterialTheme.shapes.medium) then modifier
    ) {
        Column {
            KamelImage(
                cover,
                contentDescription = manga.title,
                modifier = Modifier
                    .height(200.dp)
                    .aspectRatio(mangaAspectRatio, true)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Text(
                text = manga.title,
                style = fontStyle,
                maxLines = 3,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
            )
        }
        SourceMangaBadges(
            inLibrary = inLibrary,
            modifier = Modifier.padding(4.dp)
        )
    }
}
