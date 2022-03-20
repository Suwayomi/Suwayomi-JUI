/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.components

import ca.gosyer.jui.core.lang.getDefault
import ca.gosyer.jui.core.lang.getDisplayLanguage
import io.fluidsonic.locale.Locale

fun localeToString(locale: String) = Locale.forLanguageTag(locale)
    .getDisplayLanguage(Locale.getDefault())
    .ifBlank { locale.uppercase() }
