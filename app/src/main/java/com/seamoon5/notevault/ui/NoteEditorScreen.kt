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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seamoon5.notevault.data.Folder

/**
 * One editor shape used for both normal notes and vault notes, so the code is
 * written (and fixed) only once.
 */
data class EditorState(
    val id: Long = 0L,
    val title: String = "",
    val body: String = "",
    val tags: String = "",
    val colorIndex: Int = 0,
    val folderId: Long? = null,
    val pinned: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(Unit) {
        if (state.id == 0L) runCatching { titleFocus.requestFocus() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            state.id == 0L && isVault -> "New secret note"
                            state.id == 0L -> "New note"
                            isVault -> "Secret note"
                            else -> "Edit note"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isBlankNote(state.title, state.body)) {
                            onSave()
                        }
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSave) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                    Box {
                        IconButton(onClick = { menu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (state.pinned) "Unpin" else "Pin to top") },
                                leadingIcon = { Icon(Icons.Filled.PushPin, null) },
                                onClick = {
                                    menu = false
                                    onChange(state.copy(pinned = !state.pinned))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete note") },
                                leadingIcon = { Icon(Icons.Filled.Delete, null) },
                                enabled = state.id != 0L,
                                onClick = { menu = false; confirmDelete = true }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {

            Surface(
                color = palette.bg,
                contentColor = palette.fg,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    TextField(
                        value = state.title,
                        onValueChange = { onChange(state.copy(title = it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(titleFocus),
                        placeholder = { Text("Title", style = MaterialTheme.typography.titleLarge) },
                        textStyle = MaterialTheme.typography.titleLarge,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    TextField(
                        value = state.body,
                        onValueChange = { onChange(state.copy(body = it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        placeholder = { Text("Write something...") },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Colour", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            ColorPicker(
                selected = state.colorIndex,
                onSelect = { onChange(state.copy(colorIndex = it)) }
            )

            if (!isVault) {
                Spacer(Modifier.height(16.dp))
                Text("Folder", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { folderMenu = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = folders.firstOrNull { it.id == state.folderId }?.name ?: "No folder",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    DropdownMenu(
                        expanded = folderMenu,
                        onDismissRequest = { folderMenu = false }
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

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.tags,
                onValueChange = { onChange(state.copy(tags = it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Tags (comma separated)") },
                placeholder = { Text("work, shopping, idea") },
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (state.updatedAt == 0L) "Not saved yet" else "Updated ${formatDate(state.updatedAt)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
        }
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

@Composable
private fun ColorPicker(selected: Int, onSelect: (Int) -> Unit) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val list = if (dark) com.seamoon5.notevault.ui.theme.DarkNotePalette
    else com.seamoon5.notevault.ui.theme.LightNotePalette
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        list.forEachIndexed { index, p ->
            val isOn = index == selected
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(p.bg, CircleShape)
                    .border(
                        width = if (isOn) 3.dp else 1.dp,
                        color = if (isOn) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                if (isOn) {
                    Text("✓", color = p.fg, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
