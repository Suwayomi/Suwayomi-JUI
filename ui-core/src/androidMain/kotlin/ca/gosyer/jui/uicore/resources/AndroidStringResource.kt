/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

@Composable
actual fun stringResource(resource: StringResource): String {
    val context = LocalContext.current
    return StringDesc.Resource(resource).toString(context)
}

@Composable
actual fun stringResource(resource: StringResource, vararg args: Any): String {
    val context = LocalContext.current
    return StringDesc.ResourceFormatted(resource, *args).toString(context)
}

@Composable
actual fun stringResource(resource: PluralsResource, quantity: Int): String {
    val context = LocalContext.current
    return StringDesc.Plural(resource, quantity).toString(context)
}

@Composable
actual fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String {
    val context = LocalContext.current
    return StringDesc.PluralFormatted(resource, quantity, *args).toString(context)
}
