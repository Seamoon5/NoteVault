package com.seamoon5.notevault.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seamoon5.notevault.data.Folder
import com.seamoon5.notevault.data.Note
import com.seamoon5.notevault.data.NoteFilter
import com.seamoon5.notevault.ui.theme.LocalIsDark
import com.seamoon5.notevault.ui.theme.NightBase
import com.seamoon5.notevault.ui.theme.NoteShapes
import com.seamoon5.notevault.ui.theme.Paper

@Composable
fun NotesScreen(
    notes: List<Note>,
    folders: List<Folder>,
    tags: List<String>,
    filter: NoteFilter,
    gridMode: Boolean,
    onGridToggle: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onFolderPick: (Long?) -> Unit,
    onTagPick: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onOpen: (Long) -> Unit,
    onPin: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    onExportOne: (Note) -> Unit,
    onTabSelect: (TabNotes) -> Unit,
    onNewNote: () -> Unit,
    vaultBadge: Int
) {
    var searching by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Note?>(null) }
    val dark = LocalIsDark.current
    val groups = remember(notes) { groupNotes(notes) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {

        // ---------------- header ----------------
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                (if (dark) NightBase else Paper).copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 12.dp, top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "NoteVault",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { searching = !searching }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = if (searching) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    IconButton(onClick = { onGridToggle(!gridMode) }) {
                        Text(
                            text = if (gridMode) "▦" else "☰",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (searching) {
                OutlinedTextField(
                    value = filter.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    singleLine = true,
                    placeholder = { Text("Search notes...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null, Modifier.size(19.dp)) },
                    trailingIcon = {
                        if (filter.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Close, "Clear", Modifier.size(18.dp))
                            }
                        }
                    },
                    shape = NoteShapes.field,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            if (folders.isNotEmpty() || tags.isNotEmpty()) {
                FilterRow(
                    folders = folders,
                    tags = tags,
                    filter = filter,
                    onFolderPick = onFolderPick,
                    onTagPick = onTagPick
                )
            }

            if (notes.isEmpty()) {
                EmptyNotes(
                    isFiltered = filter.query.isNotBlank() || filter.folderId != null || filter.tag != null,
                    onClear = onClearFilters
                )
            } else if (gridMode) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    groups.forEach { (group, items) ->
                        item(key = "head-${group.name}", span = { GridItemSpan(maxLineSpan) }) {
                            GroupHeader(group, items.size)
                        }
                        gridItems(items, key = { "n${it.id}" }) { note ->
                            SimpleNoteCard(
                                title = note.title,
                                body = note.body,
                                tags = note.tags,
                                colorIndex = note.colorIndex,
                                pinned = note.pinned,
                                updatedAt = note.updatedAt,
                                compact = true,
                                onOpen = { onOpen(note.id) },
                                onPin = { onPin(note) },
                                onDeleteRequest = { pendingDelete = note },
                                onExport = { onExportOne(note) }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    groups.forEach { (group, items) ->
                        item(key = "head-${group.name}") { GroupHeader(group, items.size) }
                        listItems(items, key = { "n${it.id}" }) { note ->
                            SimpleNoteCard(
                                title = note.title,
                                body = note.body,
                                tags = note.tags,
                                colorIndex = note.colorIndex,
                                pinned = note.pinned,
                                updatedAt = note.updatedAt,
                                compact = false,
                                onOpen = { onOpen(note.id) },
                                onPin = { onPin(note) },
                                onDeleteRequest = { pendingDelete = note },
                                onExport = { onExportOne(note) }
                            )
                        }
                    }
                }
            }
        }

        // ---------------- bottom bar ----------------
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            GlassBottomBar(
                selected = TabNotes.NOTES,
                vaultBadge = vaultBadge,
                onSelect = onTabSelect,
                onNewNote = onNewNote
            )
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete note?") },
            text = {
                Text(
                    if (target.title.isBlank()) "This note will be permanently removed."
                    else "\"${target.title}\" will be permanently removed."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(target)
                    pendingDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun GroupHeader(group: NoteGroup, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp, start = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = group.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .size(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilterRow(
    folders: List<Folder>,
    tags: List<String>,
    filter: NoteFilter,
    onFolderPick: (Long?) -> Unit,
    onTagPick: (String?) -> Unit
) {
    var folderMenu by remember { mutableStateOf(false) }
    val activeFolder = folders.firstOrNull { it.id == filter.folderId }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Folder chip with a management menu
        Box {
            CompactChip(
                label = activeFolder?.name ?: "All",
                selected = filter.folderId == null,
                leading = if (activeFolder == null) "▤" else null,
                onClick = { if (activeFolder == null) onFolderPick(null) else folderMenu = true }
            )
            DropdownMenu(
                expanded = folderMenu,
                onDismissRequest = { folderMenu = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                DropdownMenuItem(
                    text = { Text("All notes") },
                    onClick = { folderMenu = false; onFolderPick(null) }
                )
                folders.forEach { f ->
                    DropdownMenuItem(
                        text = { Text(f.name) },
                        leadingIcon = { Icon(Icons.Filled.Folder, null, Modifier.size(16.dp)) },
                        onClick = { folderMenu = false; onFolderPick(f.id) }
                    )
                }
            }
        }

        if (filter.folderId != null) {
            CompactChip(label = "✕ ${activeFolder?.name ?: ""}", selected = false, onClick = { onFolderPick(null) })
        }

        tags.forEach { t ->
            CompactChip(
                label = "#$t",
                selected = filter.tag == t,
                onClick = { onTagPick(if (filter.tag == t) null else t) }
            )
        }
    }
}

@Composable
private fun CompactChip(
    label: String,
    selected: Boolean,
    leading: String? = null,
    onClick: () -> Unit
) {
    Text(
        text = if (leading != null) "$leading  $label" else label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .clip(NoteShapes.pill)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun EmptyNotes(isFiltered: Boolean, onClear: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 90.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(NoteShapes.pill)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text("✎", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = if (isFiltered) "Nothing here" else "No notes yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = if (isFiltered) "Try a different search or filter."
                else "Tap the + button to write your first note.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isFiltered) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onClear) { Text("Clear filters") }
            }
        }
    }
}
