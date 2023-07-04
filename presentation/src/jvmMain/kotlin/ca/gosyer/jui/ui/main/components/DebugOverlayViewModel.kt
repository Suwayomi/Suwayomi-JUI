/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.components

import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.milliseconds

actual class DebugOverlayViewModel
    @Inject
    constructor(contextWrapper: ContextWrapper) : ViewModel(contextWrapper) {
        override val scope = MainScope()

        val runtime: Runtime = Runtime.getRuntime()
        actual val maxMemory = runtime.maxMemory().formatSize()
        actual val usedMemoryFlow = MutableStateFlow(runtime.usedMemory().formatSize())

        init {
            scope.launch {
                while (true) {
                    usedMemoryFlow.value = runtime.usedMemory().formatSize()
                    delay(100.milliseconds)
                }
            }
        }

        private fun Long.formatSize(): String {
            if (this < 1024) return "$this B"
            val z = (63 - java.lang.Long.numberOfLeadingZeros(this)) / 10
            return String.format("%.1f %sB", toDouble() / (1L shl z * 10), " KMGTPE"[z])
        }

        private fun Runtime.usedMemory(): Long = totalMemory() - freeMemory()

        override fun onDispose() {
            super.onDispose()
            scope.cancel()
        }
    }
