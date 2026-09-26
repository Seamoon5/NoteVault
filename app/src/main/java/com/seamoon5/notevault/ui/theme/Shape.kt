package com.seamoon5.notevault.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * v1.0 used Material's default 4/8/12/16dp scale, which is why every card and
 * button looked like an untouched template. v2.0 uses larger, more confident
 * radii and pill-shaped actions.
 */
val NoteVaultShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/** Shapes the M3 scale does not cover, used by our own components. */
object NoteShapes {
    val card = RoundedCornerShape(22.dp)
    val cardCompact = RoundedCornerShape(20.dp)
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val field = RoundedCornerShape(16.dp)
    val pill = RoundedCornerShape(percent = 50)
    val tag = RoundedCornerShape(8.dp)
}
