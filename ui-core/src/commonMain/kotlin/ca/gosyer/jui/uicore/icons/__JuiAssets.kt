package ca.gosyer.jui.uicore.icons

import androidx.compose.ui.graphics.vector.ImageVector
import ca.gosyer.jui.uicore.icons.juiassets.AllAssets
import ca.gosyer.jui.uicore.icons.juiassets.Ca
import ca.gosyer.jui.uicore.icons.juiassets.DonePrev
import kotlin.collections.List as ____KtList

public object JuiAssets

private var __AllAssets: ____KtList<ImageVector>? = null

public val JuiAssets.AllAssets: ____KtList<ImageVector>
  get() {
    if (__AllAssets != null) {
      return __AllAssets!!
    }
    __AllAssets= Ca.AllAssets + listOf(DonePrev)
    return __AllAssets!!
  }
