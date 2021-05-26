/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import androidx.compose.desktop.AppManager
import androidx.compose.ui.unit.IntOffset
import ca.gosyer.util.lang.launchUI
import com.github.weisj.darklaf.listener.MouseClickListener
import kotlinx.coroutines.DelicateCoroutinesApi
import javax.swing.Icon
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JSeparator

class ContextMenu internal constructor() {
    internal val items = mutableListOf<Pair<Any, (() -> Unit)?>>()

    @OptIn(DelicateCoroutinesApi::class)
    internal fun popupMenu() = JPopupMenu().apply {
        val window = AppManager.focusedWindow
        var mouseListener: MouseClickListener? = null
        fun close() {
            isVisible = false
            mouseListener?.let { window?.removeMouseListener(it) }
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
        window?.addMouseListener(mouseListener)
        window?.events?.let {
            val oldFocusLost = it.onFocusLost
            it.onFocusLost = {
                it.onFocusLost.andClose()
                it.onFocusLost = oldFocusLost
            }
        }

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

fun contextMenu(offset: IntOffset, contextMenu: ContextMenu.() -> Unit) {
    ContextMenu().apply(contextMenu).popupMenu().show(null, offset.x, offset.y)
}
