/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.IntOffset
import ca.gosyer.util.lang.launchUI
import com.github.weisj.darklaf.listener.MouseClickListener
import kotlinx.coroutines.DelicateCoroutinesApi
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener
import javax.swing.Icon
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JSeparator

class ContextMenu internal constructor(private val window: ComposeWindow) {
    internal val items = mutableListOf<Pair<Any, (() -> Unit)?>>()

    @OptIn(DelicateCoroutinesApi::class)
    internal fun popupMenu() = JPopupMenu().apply {
        var mouseListener: MouseClickListener? = null
        var focusListener: WindowFocusListener? = null
        fun close() {
            isVisible = false
            mouseListener?.let { window.removeMouseListener(it) }
            focusListener?.let { window.removeWindowFocusListener(it) }
        }
        fun (() -> Unit)?.andClose() {
            launchUI {
                close()
                this@andClose?.invoke()
            }
        }

        mouseListener = MouseClickListener {
            launchUI {
                close()
            }
        }
        window.addMouseListener(mouseListener)

        focusListener = object : WindowFocusListener {
            override fun windowGainedFocus(e: WindowEvent?) {}
            override fun windowLostFocus(e: WindowEvent?) {
                launchUI {
                    close()
                }
            }
        }
        window.addWindowFocusListener(focusListener)

        items.forEach { (item, block) ->
            when (item) {
                is JMenuItem -> add(item).apply {
                    addActionListener {
                        block.andClose()
                    }
                }
                is JSeparator -> add(item)
            }
        }
    }

    fun menuItem(name: String, icon: Icon? = null, builder: JMenuItem.() -> Unit = {}, action: () -> Unit) {
        items += JMenuItem(name, icon).apply(builder) to action
    }
    fun separator() {
        items += JSeparator() to null
    }
}

fun contextMenu(window: ComposeWindow, offset: IntOffset, contextMenu: ContextMenu.() -> Unit) {
    ContextMenu(window).apply(contextMenu).popupMenu().show(null, offset.x, offset.y)
}
