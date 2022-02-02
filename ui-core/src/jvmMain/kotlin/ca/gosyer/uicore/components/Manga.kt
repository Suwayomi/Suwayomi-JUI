/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.uicore.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.uicore.image.KamelImage
import io.kamel.core.Resource

@Composable
fun MangaGridItem(
    title: String,
    cover: Resource<Painter>,
    onClick: () -> Unit = {},
) {
    val fontStyle = LocalTextStyle.current.merge(
        TextStyle(letterSpacing = 0.sp, fontFamily = FontFamily.SansSerif, fontSize = 14.sp)
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(mangaAspectRatio)
            .padding(8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            KamelImage(cover, title, contentScale = ContentScale.Crop)
            Box(modifier = Modifier.fillMaxSize().then(shadowGradient))
            Text(
                text = title,
                color = Color.White,
                style = fontStyle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.wrapContentHeight(Alignment.CenterVertically)
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
            )
        }
    }
}

const val mangaAspectRatio = 12.8F / 18.2F

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
