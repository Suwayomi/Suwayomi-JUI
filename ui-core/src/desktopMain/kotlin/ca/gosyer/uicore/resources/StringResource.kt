/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.uicore.resources

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

@Composable
fun stringResource(resource: StringResource): String =
    StringDesc.Resource(resource).localized()

@Composable
fun stringResource(resource: StringResource, vararg args: Any): String =
    StringDesc.ResourceFormatted(resource, *args).localized()

@Composable
fun stringResource(resource: PluralsResource, quantity: Int): String =
    StringDesc.Plural(resource, quantity).localized()

@Composable
fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String =
    StringDesc.PluralFormatted(resource, quantity, *args).localized()
