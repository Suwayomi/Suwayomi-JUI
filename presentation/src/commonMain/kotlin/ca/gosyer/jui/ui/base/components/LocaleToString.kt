/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.components

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import ca.gosyer.jui.core.lang.getDisplayLanguage

fun localeToString(locale: String) =
    Locale(locale)
        .getDisplayLanguage(Locale.current)
        .ifBlank { locale.toUpperCase(Locale.current) }
