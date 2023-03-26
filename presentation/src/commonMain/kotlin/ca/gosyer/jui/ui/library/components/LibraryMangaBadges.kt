/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.components.Badge
import ca.gosyer.jui.uicore.components.BadgeGroup
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.theme.extraColors

@Composable
fun LibraryMangaBadges(
    modifier: Modifier = Modifier,
    manga: Manga,
    showUnread: Boolean,
    showDownloaded: Boolean,
    showLanguage: Boolean,
    showLocal: Boolean,
) {
    val unread = manga.unreadCount?.takeIf { showUnread && it > 0 }
    val downloaded = manga.downloadCount?.takeIf { showDownloaded && it > 0 }
    val isLocal = (manga.sourceId == Source.LOCAL_SOURCE_ID).takeIf { showLocal } ?: false
    val language = manga.source?.lang?.takeIf { showLanguage }?.toUpperCase(Locale.current)

    Row(modifier then Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        if (unread != null || downloaded != null || isLocal) {
            BadgeGroup {
                if (downloaded != null) {
                    Badge(text = downloaded.toString())
                }
                if (unread != null) {
                    Badge(
                        text = unread.toString(),
                        color = MaterialTheme.extraColors.tertiary,
                        textColor = MaterialTheme.extraColors.onTertiary,
                    )
                }
            }
        } else {
            Spacer(Modifier)
        }

        if (isLocal) {
            Badge(text = stringResource(MR.strings.local_badge))
        } else if (language != null) {
            Badge(text = language)
        }
    }
}

private val BadgesInnerPadding = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
