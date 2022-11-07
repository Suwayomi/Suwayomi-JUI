package ca.gosyer.jui.uicore.icons.juiassets

import androidx.compose.ui.graphics.vector.ImageVector
import ca.gosyer.jui.uicore.icons.JuiAssets
import ca.gosyer.jui.uicore.icons.juiassets.ca.AllAssets
import ca.gosyer.jui.uicore.icons.juiassets.ca.Gosyer
import kotlin.collections.List as ____KtList

public object CaGroup

public val JuiAssets.Ca: CaGroup
  get() = CaGroup

private var __AllAssets: ____KtList<ImageVector>? = null

public val CaGroup.AllAssets: ____KtList<ImageVector>
  get() {
    if (__AllAssets != null) {
      return __AllAssets!!
    }
    __AllAssets= Gosyer.AllAssets + listOf()
    return __AllAssets!!
  }
