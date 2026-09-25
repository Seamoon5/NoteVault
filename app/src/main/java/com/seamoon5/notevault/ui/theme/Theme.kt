package com.seamoon5.notevault.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Tiny settings store, shared between the Compose UI and the theme. */
object Prefs {
    private const val FILE = "notevault_prefs"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_GRID = "grid_layout"
    private const val KEY_LOCK_BUTTON = "vault_on_lock_screen"

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

    fun vaultButtonOnLockScreen(ctx: android.content.Context): Boolean =
        prefs(ctx).getBoolean(KEY_LOCK_BUTTON, true)
}

private val LightColors = lightColorScheme(
    primary = Violet40,
    onPrimary = PaperCard,
    primaryContainer = Violet90,
    onPrimaryContainer = Violet10,
    secondary = Teal40,
    onSecondary = PaperCard,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Teal20,
    tertiary = Amber40,
    onTertiary = PaperCard,
    background = Paper,
    onBackground = Ink,
    surface = PaperCard,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEFEAFB),
    onSurfaceVariant = InkDim,
    outline = Color(0xFFC9C0E6),
    outlineVariant = Color(0xFFE4DDF5),
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = Violet80,
    onPrimary = Violet10,
    primaryContainer = Violet20,
    onPrimaryContainer = Violet90,
    secondary = Teal80,
    onSecondary = Teal20,
    secondaryContainer = Teal30,
    onSecondaryContainer = Color(0xFFB6F5EA),
    tertiary = Amber80,
    onTertiary = Color(0xFF3A2500),
    background = NightPaper,
    onBackground = NightInk,
    surface = NightCard,
    onSurface = NightInk,
    surfaceVariant = NightVariant,
    onSurfaceVariant = NightInkDim,
    outline = NightOutline,
    outlineVariant = Color(0xFF3A2F58),
    error = Color(0xFFFFB4AB)
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
    val colors = if (dark) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = NoteVaultTypography,
        content = content
    )
}

/**
 * The app's signature background: a soft violet glow with a frosted sheet on
 * top. Real blur is not available on Android 11, so the "glass" look is done
 * with a see-through gradient layer instead. Same visual intent, almost zero
 * cost on the Snapdragon 680.
 */
@Composable
fun FrostBackground(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val base = if (dark) {
        listOf(NightPaper, Color(0xFF1B1038), NightPaper)
    } else {
        listOf(Color(0xFFF7F5FF), Color(0xFFEDE4FF), Color(0xFFFFF1F6))
    }
    val frost = if (dark) listOf(FrostTopDark, FrostBottomDark)
    else listOf(FrostTopLight, FrostBottomLight)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(base))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(frost))
        ) { content() }
    }
}
