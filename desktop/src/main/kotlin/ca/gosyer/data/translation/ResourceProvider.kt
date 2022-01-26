/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.translation

import java.util.Locale
import javax.inject.Inject
import javax.inject.Provider

class ResourceProvider @Inject constructor() : Provider<XmlResourceBundle> {
    override fun get(): XmlResourceBundle {
        return XmlResourceBundle.forLocale(Locale.getDefault())
    }
}
