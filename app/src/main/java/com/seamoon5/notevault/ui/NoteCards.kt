package com.seamoon5.notevault.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * The one note card used everywhere. It takes plain values (not database
 * objects) so the same card works for normal notes, vault notes and exported
 * previews.
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
    var menu by remember { mutableStateOf(false) }

    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.bg, contentColor = palette.fg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (compact) 1.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (compact) 108.dp else 84.dp)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    if (broken) {
                        Text("Locked note", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "This note cannot be decrypted any more.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        if (title.isNotBlank()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val preview = previewOf(body)
                        if (preview.isNotEmpty()) {
                            Text(
                                text = preview,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = if (compact) 4 else 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (title.isBlank()) {
                            Text("Empty note", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (pinned) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.size(15.dp)
                    )
                }
                Box {
                    IconButton(onClick = { menu = true }, modifier = Modifier.size(26.dp)) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "Note options",
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
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

            Spacer(Modifier.size(6.dp))
            TagChips(tags, max = if (compact) 2 else 4)

            if (isVault && !broken) {
                Spacer(Modifier.size(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("Encrypted", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.size(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formatDateTime(updatedAt), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
