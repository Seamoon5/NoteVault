package com.seamoon5.notevault.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import com.seamoon5.notevault.ui.theme.LocalIsDark
import com.seamoon5.notevault.ui.theme.NightBase
import com.seamoon5.notevault.ui.theme.NoteShapes
import com.seamoon5.notevault.ui.theme.Paper

enum class TabNotes(val label: String, val icon: ImageVector) {
    NOTES("Notes", Icons.Filled.StickyNote2),
    SEARCH("Search", Icons.Filled.Search),
    NEW("", Icons.Filled.Add),
    VAULT("Vault", Icons.Filled.Lock),
    SETTINGS("Settings", Icons.Filled.Settings)
}

/**
 * Floating glass bottom bar with a raised centre button.
 *
 * v1.0 had no bottom navigation at all: four actions crowded into the top bar
 * and a huge empty void underneath, which is the single biggest reason it read
 * as unfinished. A bottom bar anchors the screen and gives the thumb somewhere
 * to live.
 */
@Composable
fun GlassBottomBar(
    selected: TabNotes,
    vaultBadge: Int,
    onSelect: (TabNotes) -> Unit,
    onNewNote: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val dark = LocalIsDark.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                // Scrim so list content does not show through the glass untidily.
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        (if (dark) NightBase else Paper).copy(alpha = 0.60f),
                        (if (dark) NightBase else Paper).copy(alpha = 0.94f)
                    )
                )
            )
            .padding(top = 34.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    if (dark) Color(0xFF16161F).copy(alpha = 0.92f)
                    else Color(0xFFFFFFFF).copy(alpha = 0.88f),
                    NoteShapes.pill
                )
                .border(
                    width = 1.dp,
                    color = if (dark) Color(0x1FFFFFFF) else Color(0x14000000),
                    shape = NoteShapes.pill
                )
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarItem(TabNotes.NOTES, selected == TabNotes.NOTES, 0) { onSelect(TabNotes.NOTES) }

            BarItem(TabNotes.SEARCH, selected == TabNotes.SEARCH, 0) { onSelect(TabNotes.SEARCH) }

            // Raised centre action
            val newInteraction = remember { MutableInteractionSource() }
            val pressed by newInteraction.collectIsPressedAsState()
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(if (pressed) 0.90f else 1f)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable(
                        interactionSource = newInteraction,
                        indication = androidx.compose.material3.ripple(
                            bounded = false,
                            color = MaterialTheme.colorScheme.onPrimary
                        ),
                        role = Role.Button,
                        onClickLabel = "New note",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNewNote()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "New note",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            BarItem(TabNotes.VAULT, selected == TabNotes.VAULT, vaultBadge) { onSelect(TabNotes.VAULT) }

            BarItem(TabNotes.SETTINGS, selected == TabNotes.SETTINGS, 0) { onSelect(TabNotes.SETTINGS) }
        }
    }
}

@Composable
private fun BarItem(
    tab: TabNotes,
    active: Boolean,
    badge: Int,
    onClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(62.dp)
            .scale(if (pressed) 0.92f else 1f)
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(
                    bounded = false,
                    radius = 26.dp,
                    color = MaterialTheme.colorScheme.primary
                ),
                role = Role.Tab,
                onClickLabel = tab.label,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                tab.icon,
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier.size(23.dp)
            )
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1
        )
    }
}
