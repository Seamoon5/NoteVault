package com.seamoon5.notevault.ui.theme

import androidx.compose.ui.graphics.Color

// ---- brand -------------------------------------------------------------
val Violet10 = Color(0xFF2A0A5E)
val Violet40 = Color(0xFF6D28D9)
val Violet80 = Color(0xFFC4A6FF)
val Violet90 = Color(0xFFEADDFF)
val Violet20 = Color(0xFF4A1E96)
val Violet30 = Color(0xFF5B21B6)

val Teal40 = Color(0xFF0E7C7B)
val Teal80 = Color(0xFF5EEAD4)
val Teal20 = Color(0xFF0B3D3C)
val Teal30 = Color(0xFF115E5D)

val Amber40 = Color(0xFFB45309)
val Amber80 = Color(0xFFFCD34D)

val Ink = Color(0xFF1B1230)
val InkDim = Color(0xFF4B4166)
val Paper = Color(0xFFF7F5FF)
val PaperCard = Color(0xFFFFFFFF)

val NightInk = Color(0xFFEDE7FB)
val NightInkDim = Color(0xFFB9AEDC)
val NightPaper = Color(0xFF0F0A1C)
val NightCard = Color(0xFF1A1230)
val NightVariant = Color(0xFF2A2044)
val NightOutline = Color(0xFF4E4270)

/** Light-theme frosted surfaces: see-through so the gradient shows through. */
val FrostTopLight = Color(0xCCFFFFFF)
val FrostBottomLight = Color(0x99FFFFFF)

/** Dark-theme frosted surfaces. */
val FrostTopDark = Color(0xB31A1230)
val FrostBottomDark = Color(0x8C1A1230)

/**
 * Note background colours, Google-Keep style. Index 0 means "use the normal
 * card colour". Each entry is (background, text) so text always stays
 * readable in both light and dark mode.
 */
data class NotePalette(val bg: Color, val fg: Color)

val LightNotePalette = listOf(
    NotePalette(Color(0xFFFFFFFF), Ink),       // 0 default
    NotePalette(Color(0xFFFFE9A8), Color(0xFF3D2E00)),
    NotePalette(Color(0xFFFFD3C4), Color(0xFF452112)),
    NotePalette(Color(0xFFFFC9DE), Color(0xFF441026)),
    NotePalette(Color(0xFFC9E8FF), Color(0xFF0B2A3D)),
    NotePalette(Color(0xFFCFF5DC), Color(0xFF0F3524)),
    NotePalette(Color(0xFFE2D6FF), Color(0xFF2A1A4D))
)

val DarkNotePalette = listOf(
    NotePalette(NightCard, NightInk),          // 0 default
    NotePalette(Color(0xFF4A3E12), Color(0xFFFFE9A8)),
    NotePalette(Color(0xFF4A2E22), Color(0xFFFFD3C4)),
    NotePalette(Color(0xFF4A2434), Color(0xFFFFC9DE)),
    NotePalette(Color(0xFF1B3A4A), Color(0xFFC9E8FF)),
    NotePalette(Color(0xFF1B4530), Color(0xFFCFF5DC)),
    NotePalette(Color(0xFF342A4A), Color(0xFFE2D6FF))
)
