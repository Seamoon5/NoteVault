package com.seamoon5.notevault.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Single source of truth for "is the app currently dark".
 *
 * Everything that needs to know (note card colours, the mesh background, the
 * glass tint) must read THIS, not isSystemInDarkTheme(). v2.0 had a bug where
 * choosing Light in Settings left the background and note cards dark, because
 * they were still reading the phone's setting while Material switched to light.
 */
val LocalIsDark = androidx.compose.runtime.staticCompositionLocalOf { true }

/** Tiny settings store, shared between the Compose UI and the theme. */
object Prefs {
    private const val FILE = "notevault_prefs"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_GRID = "grid_layout"

    private fun prefs(ctx: android.content.Context) =
        ctx.applicationContext.getSharedPreferences(FILE, android.content.Context.MODE_PRIVATE)

    fun themeMode(ctx: android.content.Context): ThemeMode {
        val raw = prefs(ctx).getString(KEY_THEME, null) ?: return ThemeMode.SYSTEM
        return runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
    }

    fun setThemeMode(ctx: android.content.Context, mode: ThemeMode) {
        prefs(ctx).edit().putString(KEY_THEME, mode.name).apply()
    }

    fun gridLayout(ctx: android.content.Context, fallback: Boolean): Boolean =
        prefs(ctx).getBoolean(KEY_GRID, fallback)

    fun setGridLayout(ctx: android.content.Context, value: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_GRID, value).apply()
    }
}

private val LightColors = lightColorScheme(
    primary = Violet40,
    onPrimary = Color.White,
    primaryContainer = Violet90,
    onPrimaryContainer = Violet20,
    secondary = Violet50,
    onSecondary = Color.White,
    secondaryContainer = Violet95,
    onSecondaryContainer = Violet30,
    tertiary = Amber40,
    onTertiary = Color.White,
    tertiaryContainer = Amber90,
    onTertiaryContainer = Amber40,
    background = Paper,
    onBackground = Ink,
    surface = PaperCard,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF0EEF8),
    onSurfaceVariant = InkDim,
    outline = Color(0xFFC9C4DE),
    outlineVariant = Color(0xFFE6E2F2),
    error = Danger
)

private val DarkColors = darkColorScheme(
    primary = Violet80,
    onPrimary = Violet20,
    primaryContainer = Violet30,
    onPrimaryContainer = Violet90,
    secondary = Violet60,
    onSecondary = Violet20,
    secondaryContainer = Violet30,
    onSecondaryContainer = Violet95,
    tertiary = Amber80,
    onTertiary = Color(0xFF3A2500),
    tertiaryContainer = Color(0xFF4A3208),
    onTertiaryContainer = Amber90,
    background = NightBase,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk,
    surfaceVariant = NightSurfaceHi,
    onSurfaceVariant = NightInkDim,
    outline = NightOutline,
    outlineVariant = Color(0xFF262636),
    error = DangerDark
)

@Composable
fun NoteVaultTheme(
    mode: ThemeMode,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalIsDark provides dark) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = NoteVaultTypography,
            shapes = NoteVaultShapes,
            content = content
        )
    }
}

/**
 * The app's backdrop: a soft mesh of coloured blobs, genuinely blurred.
 *
 * v1.0 had a flat gradient that was so subtle it just looked like a slightly
 * off-white background. Because there is real colour behind the panels now,
 * the see-through surfaces in front of it read as actual frosted glass.
 *
 * The blur is a real RenderEffect, which needs Android 12+. On older phones we
 * fall back to the same mesh without blur, which still looks fine.
 */
@Composable
fun FrostBackground(content: @Composable () -> Unit) {
    val dark = LocalIsDark.current
    val mesh = if (dark) MeshDark else MeshLight
    val base = if (dark) NightBase else Paper
    val glass = if (dark) listOf(GlassTopDark, GlassBottomDark) else listOf(GlassTopLight, GlassBottomLight)
    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(base)
    ) {
        // Mesh of colour. Blurred hard so it becomes a soft wash of light.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (canBlur) Modifier.blur(70.dp) else Modifier
                )
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val blobs = listOf(
                        Triple(Offset(w * 0.10f, h * 0.06f), w * 0.85f, mesh[0]),
                        Triple(Offset(w * 0.95f, h * 0.22f), w * 0.75f, mesh[1]),
                        Triple(Offset(w * 0.15f, h * 0.55f), w * 0.80f, mesh[2]),
                        Triple(Offset(w * 0.90f, h * 0.88f), w * 0.85f, mesh[3])
                    )
                    blobs.forEach { (centre, radius, color) ->
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(color.copy(alpha = if (dark) 1.0f else 0.85f), Color.Transparent),
                                center = centre,
                                radius = radius
                            ),
                            radius = radius,
                            center = centre
                        )
                    }
                }
        )

        // Frosted sheet in front of the mesh.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(glass))
        ) {
        // The Surface is not decoration: outside a Surface or Scaffold,
        // Compose's LocalContentColor defaults to BLACK, so any Text without an
        // explicit colour turns invisible on a dark theme. Setting the content
        // colour here fixes that for every screen at once.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            content()
        }
        }
    }
}

/** A soft accent glow, used behind headers. */
@Composable
fun GlowOrb(color: Color, diameter: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(diameter.dp)
            .offset(x = 0.dp, y = 0.dp)
            .background(color.copy(alpha = 0.18f), androidx.compose.foundation.shape.CircleShape)
    )
}

/** Kept for the few places that need a raw Size. */
internal fun gradientFill(size: Size, colors: List<Color>, vertical: Boolean = true): Brush =
    if (vertical) Brush.verticalGradient(colors)
    else Brush.linearGradient(colors, start = Offset.Zero, end = Offset(size.width, size.height))
