/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.browse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ca.gosyer.i18n.MR
import ca.gosyer.uicore.resources.stringResource

@Composable
fun SourceMangaBadges(
    inLibrary: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!inLibrary) return

    Row(modifier = modifier.clip(MaterialTheme.shapes.medium)) {
        Text(
            text = stringResource(MR.strings.in_library),
            modifier = Modifier.background(MaterialTheme.colors.primary).then(BadgesInnerPadding),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onPrimary
        )
    }
}

private val BadgesInnerPadding = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
