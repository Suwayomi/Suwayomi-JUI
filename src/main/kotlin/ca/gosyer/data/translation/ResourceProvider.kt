/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.translation

import ca.gosyer.data.ui.UiPreferences
import java.util.Locale
import javax.inject.Inject
import javax.inject.Provider

class ResourceProvider @Inject constructor(
    val uiPreferences: UiPreferences
) : Provider<XmlResourceBundle> {
    override fun get(): XmlResourceBundle {
        val languagePref = uiPreferences.language()
        return if (languagePref.isSet()) {
            languagePref.get().let {
                val locale: Locale? = Locale.forLanguageTag(it)
                if (locale != null) {
                    XmlResourceBundle.forLocale(locale)
                } else {
                    XmlResourceBundle.forTag(it)
                }
            }
        } else XmlResourceBundle.forLocale(Locale.getDefault())
    }
}
