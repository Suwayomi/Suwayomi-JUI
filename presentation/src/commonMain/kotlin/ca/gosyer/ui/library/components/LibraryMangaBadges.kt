/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun LibraryMangaBadges(
    unread: Int?,
    downloaded: Int?,
    modifier: Modifier = Modifier,
) {
    if (unread == null && downloaded == null) return

    Row(modifier = modifier.clip(MaterialTheme.shapes.medium)) {
        if (unread != null && unread > 0) {
            Text(
                text = unread.toString(),
                modifier = Modifier.background(MaterialTheme.colors.primary).then(BadgesInnerPadding),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onPrimary
            )
        }
        if (downloaded != null && downloaded > 0) {
            Text(
                text = downloaded.toString(),
                modifier = Modifier.background(MaterialTheme.colors.secondary).then(
                    BadgesInnerPadding
                ),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSecondary
            )
        }
    }
}

private val BadgesInnerPadding = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
