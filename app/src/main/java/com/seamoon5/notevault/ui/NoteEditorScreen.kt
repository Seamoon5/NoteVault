package com.seamoon5.notevault.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seamoon5.notevault.data.Folder
import com.seamoon5.notevault.ui.theme.LocalIsDark
import com.seamoon5.notevault.ui.theme.NightBase
import com.seamoon5.notevault.ui.theme.NoteShapes
import com.seamoon5.notevault.ui.theme.Paper

/**
 * The note editor.
 *
 * v2.0 changes: the paper now fills the whole screen instead of a fixed 260dp
 * box with dead space under it, and colour/folder/tags moved into a compact
 * toolbar instead of three stacked default-looking Material fields.
 */
@Composable
fun NoteEditorScreen(
    state: EditorState,
    folders: List<Folder>,
    isVault: Boolean,
    onChange: (EditorState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val palette = notePalette(state.colorIndex)
    val titleFocus = remember { FocusRequester() }
    var menu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var folderMenu by remember { mutableStateOf(false) }
    var tagDialog by remember { mutableStateOf(false) }
    var draftTags by remember { mutableStateOf(state.tags) }

    LaunchedEffect(Unit) {
        if (state.id == 0L) runCatching { titleFocus.requestFocus() }
    }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {

        ScreenHeader(
            title = when {
                state.id == 0L && isVault -> "New secret note"
                state.id == 0L -> "New note"
                isVault -> "Secret note"
                else -> "Edit note"
            },
            subtitle = if (isVault) "Encrypted on this phone" else "Tap the check to save",
            onBack = {
                if (!isBlankNote(state.title, state.body)) onSave()
                onBack()
            }
        ) {
            IconButton(onClick = onSave) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Filled.MoreVert, "More", modifier = Modifier.size(21.dp))
                }
                DropdownMenu(
                    expanded = menu,
                    onDismissRequest = { menu = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    DropdownMenuItem(
                        text = { Text(if (state.pinned) "Unpin" else "Pin to top") },
                        leadingIcon = { Icon(Icons.Filled.PushPin, null) },
                        onClick = { menu = false; onChange(state.copy(pinned = !state.pinned)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete note") },
                        leadingIcon = { Icon(Icons.Filled.Delete, null) },
                        enabled = state.id != 0L,
                        onClick = { menu = false; confirmDelete = true }
                    )
                }
            }
        }

        // The paper takes all the remaining height.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(NoteShapes.card)
                .background(palette.bg)
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {

                TextField(
                    value = state.title,
                    onValueChange = { onChange(state.copy(title = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocus),
                    placeholder = {
                        Text(
                            "Title",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.muted
                        )
                    },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.fg
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = palette.fg,
                        unfocusedTextColor = palette.fg,
                        cursorColor = palette.fg
                    )
                )

                TextField(
                    value = state.body,
                    onValueChange = { onChange(state.copy(body = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = {
                        Text(
                            "Start writing...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.muted
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = palette.fg),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = palette.fg,
                        unfocusedTextColor = palette.fg,
                        cursorColor = palette.fg
                    )
                )
            }
        }

        // Compact toolbar: colours on their own row so the chips below are not
        // squeezed into an unreadable sliver.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    (if (LocalIsDark.current) NightBase else Paper)
                        .copy(alpha = 0.90f)
                )
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .navigationBarsPadding()
        ) {
            notePaletteSwatches(state.colorIndex) { onChange(state.copy(colorIndex = it)) }

            Spacer(Modifier.height(9.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isVault) {
                    Box {
                        ToolbarChip(
                            icon = Icons.Filled.Folder,
                            label = folders.firstOrNull { it.id == state.folderId }?.name ?: "Folder"
                        ) { folderMenu = true }
                        DropdownMenu(
                            expanded = folderMenu,
                            onDismissRequest = { folderMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(
                                text = { Text("No folder") },
                                onClick = { folderMenu = false; onChange(state.copy(folderId = null)) }
                            )
                            folders.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.name) },
                                    onClick = { folderMenu = false; onChange(state.copy(folderId = f.id)) }
                                )
                            }
                        }
                    }
                }

                ToolbarChip(icon = Icons.Filled.Sell, label = tagLabel(state.tags)) {
                    draftTags = state.tags
                    tagDialog = true
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = if (state.updatedAt == 0L) "Not saved" else formatDate(state.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (tagDialog) {
        AlertDialog(
            onDismissRequest = { tagDialog = false },
            title = { Text("Tags") },
            text = {
                Column {
                    Text(
                        "Separate tags with commas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draftTags,
                        onValueChange = { draftTags = it },
                        singleLine = true,
                        shape = NoteShapes.field,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onChange(state.copy(tags = draftTags))
                    tagDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { tagDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete note?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

private fun tagLabel(tags: String): String {
    val list = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    return when {
        list.isEmpty() -> "Tags"
        list.size == 1 -> "#${list[0]}"
        else -> "#${list[0]} +${list.size - 1}"
    }
}

@Composable
private fun ToolbarChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(NoteShapes.pill)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun notePaletteSwatches(selected: Int, onSelect: (Int) -> Unit) {
    val dark = LocalIsDark.current
    val list = if (dark) com.seamoon5.notevault.ui.theme.DarkNotePalette
    else com.seamoon5.notevault.ui.theme.LightNotePalette
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        list.forEachIndexed { index, p ->
            val isOn = index == selected
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .clip(CircleShape)
                    .background(p.bg)
                    .border(
                        width = if (isOn) 2.dp else 1.dp,
                        color = if (isOn) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                if (isOn) {
                    Text("✓", color = p.fg, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
