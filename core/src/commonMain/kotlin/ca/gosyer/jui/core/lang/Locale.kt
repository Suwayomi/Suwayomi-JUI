/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.lang

import androidx.compose.ui.text.intl.Locale

expect fun Locale.getDisplayLanguage(displayLocale: Locale): String

expect fun Locale.getDisplayName(displayLocale: Locale): String

expect val Locale.displayName: String
