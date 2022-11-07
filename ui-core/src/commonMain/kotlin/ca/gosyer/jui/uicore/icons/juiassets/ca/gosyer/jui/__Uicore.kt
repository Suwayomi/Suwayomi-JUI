package ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.jui

import androidx.compose.ui.graphics.vector.ImageVector
import ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.JuiGroup
import ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.jui.uicore.AllAssets
import ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.jui.uicore.Icons
import kotlin.collections.List as ____KtList

public object UicoreGroup

public val JuiGroup.Uicore: UicoreGroup
    get() = UicoreGroup

private var __AllAssets: ____KtList<ImageVector>? = null

public val UicoreGroup.AllAssets: ____KtList<ImageVector>
    get() {
        if (__AllAssets != null) {
            return __AllAssets!!
        }
        __AllAssets = Icons.AllAssets + listOf()
        return __AllAssets!!
    }
