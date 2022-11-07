package ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer

import androidx.compose.ui.graphics.vector.ImageVector
import ca.gosyer.jui.uicore.icons.juiassets.ca.GosyerGroup
import ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.jui.AllAssets
import ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.jui.Uicore
import kotlin.collections.List as ____KtList

public object JuiGroup

public val GosyerGroup.Jui: JuiGroup
  get() = JuiGroup

private var __AllAssets: ____KtList<ImageVector>? = null

public val JuiGroup.AllAssets: ____KtList<ImageVector>
  get() {
    if (__AllAssets != null) {
      return __AllAssets!!
    }
    __AllAssets= Uicore.AllAssets + listOf()
    return __AllAssets!!
  }
