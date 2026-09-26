package com.seamoon5.notevault.ui.theme

import androidx.compose.ui.graphics.Color

// ---- brand -------------------------------------------------------------
// One accent only. v1.0 had a teal "secondary" that fought with the violet
// and made every chip and icon a different colour. v2.0 uses violet for
// everything interactive, and reserves amber for the vault.

val Violet20 = Color(0xFF2B1A6B)
val Violet30 = Color(0xFF4A2FA8)
val Violet40 = Color(0xFF6D4AFF)
val Violet50 = Color(0xFF7C5CFF)
val Violet60 = Color(0xFF9B82FF)
val Violet80 = Color(0xFFC3B0FF)
val Violet90 = Color(0xFFE6DDFF)
val Violet95 = Color(0xFFF3EEFF)

val Amber40 = Color(0xFFB45309)
val Amber60 = Color(0xFFF59E0B)
val Amber80 = Color(0xFFFCD34D)
val Amber90 = Color(0xFFFEF0D4)

// ---- light surfaces ----------------------------------------------------
val Paper = Color(0xFFFBFAFF)
val PaperCard = Color(0xFFFFFFFF)
val Ink = Color(0xFF14131F)
val InkDim = Color(0xFF5C5872)

// ---- dark surfaces -----------------------------------------------------
// True near-black, not purple-grey. v1.0's #1A1230 read as muddy.
val NightBase = Color(0xFF0A0A11)
val NightSurface = Color(0xFF15151F)
val NightSurfaceHi = Color(0xFF1E1E2B)
val NightInk = Color(0xFFEDECF6)
val NightInkDim = Color(0xFF9E9AB5)
val NightOutline = Color(0xFF33324A)

val Danger = Color(0xFFE5484D)
val DangerDark = Color(0xFFFF6369)

/** Glass layers: see-through so the mesh background shows through. */
val GlassTopLight = Color(0xE6FFFFFF)
val GlassBottomLight = Color(0x99FFFFFF)
val GlassTopDark = Color(0xB3121220)
val GlassBottomDark = Color(0x8C0A0A11)
val GlassBorderLight = Color(0x66FFFFFF)
val GlassBorderDark = Color(0x2EFFFFFF)

/**
 * Note card colours, Keep-style. Each entry is (background, text).
 *
 * v1.0's dark palette used mid-tone saturated colours (#1B4530, #4A3E12) which
 * looked like dirty swatches on a black background. v2.0 uses very dark tinted
 * surfaces with bright text instead, so the colour still reads as colour but
 * the card stays calm and the text stays readable.
 */
data class NotePalette(val bg: Color, val fg: Color, val muted: Color = Color(0xFF6A6580))

val LightNotePalette = listOf(
    NotePalette(Color(0xFFFFFFFF), Ink, Color(0xFF6A6580)),          // 0 default
    NotePalette(Color(0xFFFDF0C4), Color(0xFF4A3B00), Color(0xFF8A6A00)),
    NotePalette(Color(0xFFFFE1D0), Color(0xFF5A2E17), Color(0xFF9A5334)),
    NotePalette(Color(0xFFFFDCE9), Color(0xFF5A1A33), Color(0xFF9C3A5E)),
    NotePalette(Color(0xFFD8EDFC), Color(0xFF0C3247), Color(0xFF2C6B8C)),
    NotePalette(Color(0xFFD4F6E3), Color(0xFF10402A), Color(0xFF2A7A50)),
    NotePalette(Color(0xFFE8DEFF), Color(0xFF33205E), Color(0xFF6144A8))
)

val DarkNotePalette = listOf(
    NotePalette(Color(0xFF17171F), NightInk, Color(0xFF8E8AA3)),      // 0 default
    NotePalette(Color(0xFF2A2512), Color(0xFFF7E7A6), Color(0xFFA08C4E)),
    NotePalette(Color(0xFF2C1D16), Color(0xFFF8CFB2), Color(0xFFAE7E5E)),
    NotePalette(Color(0xFF2C1622), Color(0xFFF7C4D9), Color(0xFFB2708B)),
    NotePalette(Color(0xFF12232F), Color(0xFFB8DEF6), Color(0xFF5E93B4)),
    NotePalette(Color(0xFF102A1E), Color(0xFFB4E9C9), Color(0xFF5A9E7B)),
    NotePalette(Color(0xFF231B3A), Color(0xFFD9CAF8), Color(0xFF8E7CC4))
)

/** Soft colour blobs behind the glass. Two sets, one per theme. */
val MeshLight = listOf(
    Color(0xFFEDE4FF),  // violet
    Color(0xFFFFE9F2),  // pink
    Color(0xFFE2F4FF),  // blue
    Color(0xFFFFF6DC)   // amber
)

val MeshDark = listOf(
    Color(0xFF2B1D5E),
    Color(0xFF3B1630),
    Color(0xFF102A44),
    Color(0xFF3A2A0E)
)
