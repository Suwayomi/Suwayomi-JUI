package ca.gosyer.jui.uicore.icons.juiassets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.uicore.icons.JuiAssets

public val JuiAssets.DonePrev: ImageVector
    get() {
        if (_donePrev != null) {
            return _donePrev!!
        }
        _donePrev = Builder(
            name = "DonePrev",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero,
            ) {
                moveTo(9.0f, 16.2f)
                lineTo(4.8f, 12.0f)
                lineToRelative(-1.4f, 1.4f)
                lineTo(9.0f, 19.0f)
                lineTo(21.0f, 7.0f)
                lineToRelative(-1.4f, -1.4f)
                lineTo(9.0f, 16.2f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero,
            ) {
                moveTo(22.0f, 18.0f)
                lineToRelative(-3.0f, 0.0f)
                lineToRelative(0.0f, -4.0f)
                lineToRelative(-2.0f, 0.0f)
                lineToRelative(0.0f, 4.0f)
                lineToRelative(-3.0f, 0.0f)
                lineToRelative(4.0f, 4.0f)
                close()
            }
        }
            .build()
        return _donePrev!!
    }

private var _donePrev: ImageVector? = null
