/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.domain.library.model.DisplayMode
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.sources.browse.filter.SourceFilterAction
import ca.gosyer.jui.uicore.resources.stringResource

@Composable
fun getLibraryDisplay(vm: LibrarySettingsViewModel): @Composable () -> Unit = remember(vm) {
    @Composable {
        LibraryDisplay(
            displayMode = vm.displayMode.collectAsState().value,
            unreadBadges = vm.unreadBadges.collectAsState().value,
            downloadBadges = vm.downloadBadges.collectAsState().value,
            languageBadges = vm.languageBadges.collectAsState().value,
            localBadges = vm.localBadges.collectAsState().value,
            setDisplayMode = { vm.displayMode.value = it },
            setUnreadBadges = { vm.unreadBadges.value = it },
            setDownloadBadges = { vm.downloadBadges.value = it },
            setLanguageBadges = { vm.languageBadges.value = it },
            setLocalBadges = { vm.localBadges.value = it }
        )
    }
}

@Composable
fun LibraryDisplay(
    displayMode: DisplayMode,
    unreadBadges: Boolean,
    downloadBadges: Boolean,
    languageBadges: Boolean,
    localBadges: Boolean,
    setDisplayMode: (DisplayMode) -> Unit,
    setUnreadBadges: (Boolean) -> Unit,
    setDownloadBadges: (Boolean) -> Unit,
    setLanguageBadges: (Boolean) -> Unit,
    setLocalBadges: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        TitleText(stringResource(MR.strings.display_mode))
        DisplayMode.values.fastForEach {
            RadioSelectionItem(
                text = stringResource(it.res),
                selected = it == displayMode,
                onClick = { setDisplayMode(it) }
            )
        }
        TitleText(stringResource(MR.strings.display_badges))
        CheckboxItem(
            text = stringResource(MR.strings.display_badge_downloaded),
            checked = downloadBadges,
            onClick = { setDownloadBadges(!downloadBadges) }
        )
        CheckboxItem(
            text = stringResource(MR.strings.display_badge_unread),
            checked = unreadBadges,
            onClick = { setUnreadBadges(!unreadBadges) }
        )
        CheckboxItem(
            text = stringResource(MR.strings.display_badge_local),
            checked = localBadges,
            onClick = { setLocalBadges(!localBadges) }
        )
        // TODO: 2022-04-06 Enable when library contains manga source in manga object
        /*CheckboxItem(
            text = stringResource(MR.strings.display_badge_language),
            checked = languageBadges,
            onClick = { setLanguageBadges(!languageBadges) }
        )*/
    }
}

@Composable
private fun TitleText(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun RadioSelectionItem(text: String, selected: Boolean, onClick: () -> Unit) {
    SourceFilterAction(
        name = text,
        onClick = onClick,
        action = {
            RadioButton(
                selected = selected,
                onClick = null
            )
        }
    )
}

@Composable
private fun CheckboxItem(text: String, checked: Boolean, onClick: () -> Unit) {
    SourceFilterAction(
        name = text,
        onClick = onClick,
        action = {
            Checkbox(
                checked = checked,
                onCheckedChange = null
            )
        }
    )
}
