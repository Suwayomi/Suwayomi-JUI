/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.theme.extraColors

@Composable
fun LibraryMangaBadges(
    modifier: Modifier = Modifier,
    manga: Manga,
    showUnread: Boolean,
    showDownloaded: Boolean,
    showLanguage: Boolean,
    showLocal: Boolean
) {
    val unread = manga.unreadCount
    val downloaded = manga.downloadCount
    val isLocal = manga.sourceId == Source.LOCAL_SOURCE_ID

    Row(modifier then Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        if ((unread != null && unread > 0) || (downloaded != null && downloaded > 0) || isLocal) {
            Row(modifier = Modifier.clip(MaterialTheme.shapes.medium)) {
                if (showLocal && isLocal) {
                    Text(
                        text = stringResource(MR.strings.local_badge),
                        modifier = Modifier.background(MaterialTheme.colors.secondary).then(BadgesInnerPadding),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSecondary
                    )
                }
                if (showUnread && unread != null && unread > 0) {
                    Text(
                        text = unread.toString(),
                        modifier = Modifier.background(MaterialTheme.extraColors.tertiary).then(BadgesInnerPadding),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.extraColors.onTertiary
                    )
                }
                if (showDownloaded && downloaded != null && downloaded > 0) {
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
        } else {
            Spacer(Modifier)
        }

        val lang = manga.source?.lang
        if (showLanguage && lang != null) {
            Row(modifier = Modifier.clip(MaterialTheme.shapes.medium)) {
                Text(
                    text = lang.toUpperCase(Locale.current),
                    modifier = Modifier.background(MaterialTheme.colors.secondary).then(BadgesInnerPadding),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSecondary
                )
            }
        }
    }
}

private val BadgesInnerPadding = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
