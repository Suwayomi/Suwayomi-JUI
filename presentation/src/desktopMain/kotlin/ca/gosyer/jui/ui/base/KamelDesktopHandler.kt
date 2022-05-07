/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base

import ca.gosyer.jui.uicore.vm.ContextWrapper
import io.kamel.core.config.KamelConfigBuilder
import io.kamel.image.config.imageVectorDecoder
import io.kamel.image.config.resourcesFetcher
import io.kamel.image.config.svgDecoder

actual fun KamelConfigBuilder.kamelPlatformHandler(contextWrapper: ContextWrapper) {
    resourcesFetcher()
    imageVectorDecoder()
    svgDecoder()
}
