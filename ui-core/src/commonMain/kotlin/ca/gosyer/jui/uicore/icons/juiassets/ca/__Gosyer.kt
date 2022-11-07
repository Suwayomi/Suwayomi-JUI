package ca.gosyer.jui.uicore.icons.juiassets.ca

import androidx.compose.ui.graphics.vector.ImageVector
import ca.gosyer.jui.uicore.icons.juiassets.CaGroup
import ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.AllAssets
import ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.Jui
import kotlin.collections.List as ____KtList

public object GosyerGroup

public val CaGroup.Gosyer: GosyerGroup
    get() = GosyerGroup

private var __AllAssets: ____KtList<ImageVector>? = null

public val GosyerGroup.AllAssets: ____KtList<ImageVector>
    get() {
        if (__AllAssets != null) {
            return __AllAssets!!
        }
        __AllAssets = Jui.AllAssets + listOf()
        return __AllAssets!!
    }
