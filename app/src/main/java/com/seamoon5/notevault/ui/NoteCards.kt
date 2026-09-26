package com.seamoon5.notevault.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seamoon5.notevault.ui.theme.NoteShapes
import com.seamoon5.notevault.ui.theme.Violet60

/**
 * The one note card used everywhere. Takes plain values rather than database
 * objects, so the same card works for normal notes, vault notes and previews.
 *
 * v2.0 changes from v1.0: a clear title/preview/footer hierarchy, muted footer
 * text, the pin moved into the footer, a press-scale spring, and a softer
 * overflow affordance.
 */
@Composable
fun SimpleNoteCard(
    title: String,
    body: String,
    tags: String,
    colorIndex: Int,
    pinned: Boolean,
    updatedAt: Long,
    compact: Boolean,
    isVault: Boolean = false,
    broken: Boolean = false,
    onOpen: () -> Unit,
    onPin: () -> Unit,
    onDeleteRequest: () -> Unit,
    onExport: (() -> Unit)? = null
) {
    val palette = notePalette(colorIndex)
    val haptics = LocalHapticFeedback.current
    var menu by remember { mutableStateOf(false) }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 900f),
        label = "cardPress"
    )

    val clipShape = if (compact) NoteShapes.cardCompact else NoteShapes.card

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(clipShape)
            .background(palette.bg)
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(
                    bounded = true,
                    color = palette.fg.copy(alpha = 0.12f)
                ),
                role = Role.Button,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpen()
                }
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            if (broken) {
                Text(
                    "Locked note",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    "This note cannot be decrypted any more.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = if (title.isBlank()) "Untitled" else title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (title.isBlank()) palette.muted else palette.fg,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.size(4.dp))
                    Box {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(NoteShapes.pill)
                                .clickable { menu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⋯",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.muted
                            )
                        }
                        DropdownMenu(
                            expanded = menu,
                            onDismissRequest = { menu = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (pinned) "Unpin" else "Pin to top") },
                                leadingIcon = { Icon(Icons.Filled.PushPin, null) },
                                onClick = { menu = false; onPin() }
                            )
                            if (onExport != null) {
                                DropdownMenuItem(
                                    text = { Text("Export as Markdown") },
                                    leadingIcon = { Icon(Icons.Filled.Share, null) },
                                    onClick = { menu = false; onExport() }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = { Icon(Icons.Filled.Delete, null) },
                                onClick = { menu = false; onDeleteRequest() }
                            )
                        }
                    }
                }

                if (title.isNotBlank()) Spacer(Modifier.size(5.dp))

                // Don't repeat the body when it is just a copy of the title,
                // otherwise the card shows the same sentence twice.
                val preview = previewOf(body).takeIf { it.isNotEmpty() && it != title.trim() }
                if (preview != null) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                        maxLines = if (compact) 6 else 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.heightIn(min = 1.dp)
                    )
                }
            }

            Spacer(Modifier.size(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (pinned) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        tint = Violet60,
                        modifier = Modifier.size(12.dp)
                    )
                }
                TagPillRow(tags, palette.muted, max = if (compact) 2 else 3)
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatDateTime(updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TagPillRow(tags: String, muted: androidx.compose.ui.graphics.Color, max: Int) {
    val list = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    if (list.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        list.take(max).forEach { tag ->
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelSmall,
                color = muted,
                maxLines = 1,
                modifier = Modifier
                    .clip(NoteShapes.tag)
                    .background(muted.copy(alpha = 0.10f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}
