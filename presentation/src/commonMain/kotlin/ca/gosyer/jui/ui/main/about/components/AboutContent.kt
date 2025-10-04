/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.domain.settings.model.About
import ca.gosyer.jui.domain.updates.interactor.UpdateChecker
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.presentation.build.BuildKonfig
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.PreferenceRow
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.resources.toPainter
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource

@Composable
fun AboutContent(
    about: About?,
    formattedBuildTime: String,
    checkForUpdates: () -> Unit,
    openSourceLicenses: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
            ),
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.location_about))
        },
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                contentPadding = WindowInsets.bottomNav.add(
                    WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom,
                    ),
                ).asPaddingValues(),
            ) {
                item {
                    IconImage()
                }
                item {
                    Divider()
                }
                item {
                    CheckForUpdates(checkForUpdates)
                }
                item {
                    ClientVersionInfo()
                }
                item {
                    ServerVersionInfo(about, formattedBuildTime)
                }
                item {
                    WhatsNew()
                }
                item {
                    HelpTranslate()
                }
                item {
                    OpenSourceLicenses(openSourceLicenses)
                }
                item {
                    LinkDisplay()
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding()
                    .windowInsetsPadding(
                        WindowInsets.bottomNav.add(
                            WindowInsets.navigationBars.only(
                                WindowInsetsSides.Bottom,
                            ),
                        ),
                    ),
                adapter = rememberScrollbarAdapter(state),
            )
        }
    }
}

@Composable
private fun IconImage() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Image(
            painter = MR.images.icon.toPainter(),
            contentDescription = "icon",
            modifier = Modifier.height(140.dp).padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun CheckForUpdates(checkForUpdates: () -> Unit) {
    PreferenceRow(
        title = stringResource(MR.strings.update_checker),
        onClick = checkForUpdates,
    )
}

expect fun getDebugInfo(): String

@Composable
private fun ClientVersionInfo() {
    val clipboardManager = LocalClipboardManager.current
    PreferenceRow(
        title = stringResource(MR.strings.version),
        subtitle = if (BuildKonfig.IS_PREVIEW) {
            "Preview r${BuildKonfig.PREVIEW_BUILD}"
        } else {
            "Stable ${BuildKonfig.VERSION}"
        },
        onClick = {
            clipboardManager.setText(
                buildAnnotatedString {
                    append(getDebugInfo())
                },
            )
        },
    )
}

@Composable
private fun ServerVersionInfo(
    about: About?,
    formattedBuildTime: String,
) {
    if (about == null) {
        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        PreferenceRow(
            title = stringResource(MR.strings.server_version),
            subtitle = "${about.buildType.name} ${about.version} ($formattedBuildTime)",
        )
    }
}

@Composable
private fun WhatsNew() {
    val uriHandler = LocalUriHandler.current
    PreferenceRow(
        title = stringResource(MR.strings.whats_new),
        onClick = {
            uriHandler.openUri(UpdateChecker.RELEASE_URL)
        },
    )
}

@Composable
private fun HelpTranslate() {
    val uriHandler = LocalUriHandler.current
    PreferenceRow(
        title = stringResource(MR.strings.help_translate),
        onClick = {
            uriHandler.openUri("https://hosted.weblate.org/projects/suwayomi-jui/app/")
        },
    )
}

@Composable
private fun OpenSourceLicenses(openSourceLicenses: () -> Unit) {
    PreferenceRow(
        title = stringResource(MR.strings.open_source_licenses),
        onClick = openSourceLicenses,
    )
}

sealed class LinkIcon {
    data class Resource(
        val res: ImageResource,
    ) : LinkIcon()

    data class Icon(
        val icon: ImageVector,
    ) : LinkIcon()
}

enum class Link(
    val nameRes: StringResource,
    val icon: LinkIcon,
    val uri: String,
) {
    Github(MR.strings.github, LinkIcon.Resource(MR.images.github), "https://github.com/Suwayomi/Suwayomi-JUI"),
    Discord(MR.strings.discord, LinkIcon.Resource(MR.images.discord), "https://discord.gg/DDZdqZWaHA"),
    Reddit(MR.strings.reddit, LinkIcon.Resource(MR.images.reddit), "https://reddit.com/r/Tachidesk/"),
}

@Composable
private fun LinkDisplay() {
    val uriHandler = LocalUriHandler.current
    BoxWithConstraints {
        FlowRow(Modifier.fillMaxWidth(), mainAxisAlignment = FlowMainAxisAlignment.Center) {
            if (maxWidth > 720.dp) {
                Link.entries.fastForEach {
                    Column(
                        Modifier
                            .width(92.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { uriHandler.openUri(it.uri) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val name = stringResource(it.nameRes)
                        val modifier = Modifier.size(48.dp)
                        when (it.icon) {
                            is LinkIcon.Resource -> Icon(
                                painter = it.icon.res.toPainter(),
                                contentDescription = name,
                                modifier = modifier,
                            )

                            is LinkIcon.Icon -> Icon(
                                imageVector = it.icon.icon,
                                contentDescription = name,
                                modifier = modifier,
                            )
                        }
                        Text(name)
                    }
                }
            } else {
                Link.entries.fastForEach {
                    Box(
                        modifier = Modifier.clickable { uriHandler.openUri(it.uri) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .size(32.dp),
                    ) {
                        val name = stringResource(it.nameRes)
                        when (it.icon) {
                            is LinkIcon.Resource -> Icon(
                                painter = it.icon.res.toPainter(),
                                contentDescription = name,
                                modifier = Modifier.fillMaxSize(),
                            )

                            is LinkIcon.Icon -> Icon(
                                imageVector = it.icon.icon,
                                contentDescription = name,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }
}
