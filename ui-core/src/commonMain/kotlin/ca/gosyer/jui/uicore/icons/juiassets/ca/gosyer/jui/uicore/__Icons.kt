package ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.jui.uicore

import androidx.compose.ui.graphics.vector.ImageVector
import ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.jui.UicoreGroup
import ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.jui.uicore.icons.AllAssets
import ca.gosyer.jui.uicore.icons.juiassets.ca.gosyer.jui.uicore.icons.Juiassets
import kotlin.collections.List as ____KtList

public object IconsGroup

public val UicoreGroup.Icons: IconsGroup
  get() = IconsGroup

private var __AllAssets: ____KtList<ImageVector>? = null

public val IconsGroup.AllAssets: ____KtList<ImageVector>
  get() {
    if (__AllAssets != null) {
      return __AllAssets!!
    }
    __AllAssets= Juiassets.AllAssets + listOf()
    return __AllAssets!!
  }
