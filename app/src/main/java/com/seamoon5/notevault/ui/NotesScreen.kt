package com.seamoon5.notevault.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seamoon5.notevault.data.Folder
import com.seamoon5.notevault.data.Note
import com.seamoon5.notevault.data.NoteFilter

@OptIn(ExperimentalMaterial3Api::class)
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
    onOpen: (Long) -> Unit,
    onCreate: () -> Unit,
    onPin: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    onExportOne: (Note) -> Unit,
    onOpenVault: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var searching by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NoteVault", fontWeight = FontWeight.Bold)
                        Text(
                            text = subtitleFor(notes.size, folders.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { searching = !searching }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = if (searching) "Close search" else "Search notes"
                        )
                    }
                    IconButton(onClick = { onGridToggle(!gridMode) }) {
                        Icon(
                            if (gridMode) Icons.Filled.ViewAgenda else Icons.Filled.GridView,
                            contentDescription = if (gridMode) "List view" else "Grid view"
                        )
                    }
                    IconButton(onClick = onOpenVault) {
                        Icon(Icons.Filled.Lock, contentDescription = "Open vault")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New note") }
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { pad ->

        Column(modifier = Modifier.padding(pad).fillMaxSize()) {

            AnimatedVisibility(visible = searching) {
                OutlinedTextField(
                    value = filter.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    singleLine = true,
                    placeholder = { Text("Search title, text, tags...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (filter.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            FolderRow(folders, filter.folderId, onFolderPick)

            if (tags.isNotEmpty()) {
                TagRow(tags, filter.tag, onTagPick)
            }

            if (notes.isEmpty()) {
                EmptyState(filter)
            } else if (gridMode) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    gridItems(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            compact = true,
                            onOpen = onOpen,
                            onPin = onPin,
                            onDeleteRequest = { pendingDelete = it },
                            onExportOne = onExportOne
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    listItems(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            compact = false,
                            onOpen = onOpen,
                            onPin = onPin,
                            onDeleteRequest = { pendingDelete = it },
                            onExportOne = onExportOne
                        )
                    }
                }
            }
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

private fun subtitleFor(noteCount: Int, folderCount: Int): String {
    val notes = if (noteCount == 1) "1 note" else "$noteCount notes"
    val folders = if (folderCount == 1) "1 folder" else "$folderCount folders"
    return "$notes  -  $folders"
}

@Composable
private fun FolderRow(folders: List<Folder>, selected: Long?, onPick: (Long?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onPick(null) },
            label = { Text("All") },
            leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        folders.forEach { f ->
            FilterChip(
                selected = selected == f.id,
                onClick = { onPick(if (selected == f.id) null else f.id) },
                label = { Text(f.name) }
            )
        }
    }
}

@Composable
private fun TagRow(tags: List<String>, selected: String?, onPick: (String?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { t ->
            AssistChip(
                onClick = { onPick(if (selected == t) null else t) },
                label = { Text("#$t") }
            )
        }
    }
}

@Composable
private fun EmptyState(filter: NoteFilter) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (filter.query.isBlank() && filter.folderId == null && filter.tag == null)
                    "No notes yet" else "Nothing matches",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = if (filter.query.isBlank() && filter.folderId == null && filter.tag == null)
                    "Tap New note to write your first one."
                else "Try clearing the search or filters.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    compact: Boolean,
    onOpen: (Long) -> Unit,
    onPin: (Note) -> Unit,
    onDeleteRequest: (Note) -> Unit,
    onExportOne: (Note) -> Unit
) = SimpleNoteCard(
    title = note.title,
    body = note.body,
    tags = note.tags,
    colorIndex = note.colorIndex,
    pinned = note.pinned,
    updatedAt = note.updatedAt,
    compact = compact,
    onOpen = { onOpen(note.id) },
    onPin = { onPin(note) },
    onDeleteRequest = { onDeleteRequest(note) },
    onExport = { onExportOne(note) }
)
