package com.seamoon5.notevault.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seamoon5.notevault.ui.theme.GlassBorderDark
import com.seamoon5.notevault.ui.theme.GlassBorderLight
import com.seamoon5.notevault.ui.theme.LocalIsDark
import com.seamoon5.notevault.ui.theme.NightBase
import com.seamoon5.notevault.ui.theme.NoteShapes
import com.seamoon5.notevault.ui.theme.Paper

/**
 * Shared screen header and card, so every screen in the app has the same
 * structure instead of each one using a stock Material TopAppBar.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    val dark = LocalIsDark.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        (if (dark) NightBase else Paper).copy(alpha = 0.60f),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(start = if (onBack != null) 6.dp else 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        actions()
    }
}

/**
 * A translucent card. Note: the content colour must be set explicitly. Letting
 * Card work it out from a semi-transparent container produced near-invisible
 * titles in dark mode, which is exactly the kind of bug that makes an app
 * look unfinished.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dark = LocalIsDark.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (dark) Color(0xFF15151F).copy(alpha = 0.86f)
                else Color(0xFFFFFFFF).copy(alpha = 0.82f),
                NoteShapes.card
            )
            .border(
                width = 1.dp,
                color = if (dark) GlassBorderDark else GlassBorderLight,
                shape = NoteShapes.card
            )
    ) {
        Box(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
    )
}

/** Full-screen scrim behind a top header so content scrolls under it neatly. */
@Composable
fun HeaderScrim(modifier: Modifier = Modifier) {
    val dark = LocalIsDark.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        (if (dark) NightBase else Paper).copy(alpha = 0.92f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun RowSpacer(width: Int) = Spacer(Modifier.size(width.dp, 0.dp))
