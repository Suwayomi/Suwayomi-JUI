/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import androidx.compose.ui.unit.IntOffset
import mu.KotlinLogging
import javax.swing.Icon
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JSeparator

class ContextMenu internal constructor() {
    val logger = KotlinLogging.logger {}
    internal val list = mutableListOf<Pair<Any, (() -> Unit)?>>()

    fun popupMenu() = JPopupMenu().apply {
        fun (() -> Unit)?.andClose() {
            isVisible = false
            this?.invoke()
        }
        list.forEach { (item, block) ->
            when (item) {
                is JMenuItem -> add(item).apply {
                    addActionListener {
                        logger.info { it.actionCommand }
                        logger.info { it.modifiers }
                        block.andClose()
                    }
                }
                is JSeparator -> add(item)
            }
        }
    }

    fun menuItem(name: String, icon: Icon? = null, builder: JMenuItem.() -> Unit = {}, action: () -> Unit) {
        list += JMenuItem(name, icon).apply(builder) to action
    }
    fun separator() {
        list += JSeparator() to null
    }
}

fun contextMenu(offset: IntOffset, contextMenu: ContextMenu.() -> Unit) {
    ContextMenu().apply(contextMenu).popupMenu().show(null, offset.x, offset.y)
}
