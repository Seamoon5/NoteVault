package com.seamoon5.notevault.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.seamoon5.notevault.data.Folder
import com.seamoon5.notevault.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    gridMode: Boolean,
    onGridChange: (Boolean) -> Unit,
    folders: List<Folder>,
    onCreateFolder: (String) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    onExportAll: (Boolean) -> Unit,
    vaultIsSetUp: Boolean,
    onChangePin: (String, String) -> Boolean,
    onEraseVault: () -> Unit,
    onBack: () -> Unit
) {
    var newFolder by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(false) }
    var showErase by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        LazyColumn(
            modifier = Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item { SectionLabel("Appearance") }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Theme", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.size(4.dp))
                        Text(
                            "Follow the phone, or force light or dark.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.entries.forEach { m ->
                                FilterChip(
                                    selected = themeMode == m,
                                    onClick = { onThemeChange(m) },
                                    label = {
                                        Text(
                                            when (m) {
                                                ThemeMode.SYSTEM -> "System"
                                                ThemeMode.LIGHT -> "Light"
                                                ThemeMode.DARK -> "Dark"
                                            }
                                        )
                                    },
                                    leadingIcon = {
                                        if (m == ThemeMode.LIGHT || m == ThemeMode.DARK) {
                                            Icon(
                                                Icons.Filled.LightMode,
                                                null,
                                                Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.GridView, null, Modifier.size(20.dp))
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Open in grid view", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Two notes per row, Google Keep style.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = gridMode, onCheckedChange = onGridChange)
                    }
                }
            }

            item { SectionLabel("Folders") }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newFolder,
                                onValueChange = { newFolder = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("New folder name") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.size(8.dp))
                            IconButton(
                                onClick = {
                                    onCreateFolder(newFolder)
                                    newFolder = ""
                                },
                                enabled = newFolder.isNotBlank()
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Create folder",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (folders.isNotEmpty()) {
                            Spacer(Modifier.size(8.dp))
                            folders.forEach { f ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Folder,
                                        null,
                                        Modifier.size(17.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.size(10.dp))
                                    Text(f.name, Modifier.weight(1f))
                                    IconButton(onClick = { onDeleteFolder(f) }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete folder ${f.name}",
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { SectionLabel("Backup and export") }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Export all regular notes", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.size(4.dp))
                        Text(
                            "Saves every note to a file you choose. Nothing is uploaded to " +
                                "the internet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { onExportAll(true) }) {
                                Icon(Icons.Filled.Share, null, Modifier.size(17.dp))
                                Spacer(Modifier.size(6.dp))
                                Text("Markdown")
                            }
                            OutlinedButton(onClick = { onExportAll(false) }) {
                                Icon(Icons.Filled.Share, null, Modifier.size(17.dp))
                                Spacer(Modifier.size(6.dp))
                                Text("Plain text")
                            }
                        }
                    }
                }
            }

            item { SectionLabel("Security") }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Encrypted vault", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.size(4.dp))
                        Text(
                            "Vault notes are encrypted with AES-256 using a key held in " +
                                "this phone's secure hardware. Screenshots are blocked and " +
                                "the vault locks itself when you leave the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(12.dp))
                        if (vaultIsSetUp) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { showPinDialog = true }) {
                                    Text("Change PIN")
                                }
                                OutlinedButton(onClick = { showErase = true }) {
                                    Text(
                                        "Erase vault",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else {
                            Text(
                                "No vault PIN set yet. Open the vault from the notes screen to create one.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { SectionLabel("About") }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("NoteVault 1.0", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.size(4.dp))
                        Text(
                            "Notes, folders, tags, search and an encrypted vault. " +
                                "Everything stays on this phone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(Modifier.size(24.dp)) }
        }
    }

    if (showPinDialog) {
        PinChangeDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { old, new ->
                val ok = onChangePin(old, new)
                if (ok) showPinDialog = false
                ok
            }
        )
    }

    if (showErase) {
        AlertDialog(
            onDismissRequest = { showErase = false },
            title = { Text("Erase the whole vault?") },
            text = {
                Text("This deletes the PIN, the encryption key and every secret note. There is no undo.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showErase = false
                    onEraseVault()
                }) { Text("Erase everything", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showErase = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun PinChangeDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Boolean) {
    var old by remember { mutableStateOf("") }
    var fresh by remember { mutableStateOf("") }
    var again by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change vault PIN") },
        text = {
            Column {
                PinField("Current PIN", old) { old = it }
                Spacer(Modifier.size(8.dp))
                PinField("New PIN", fresh) { fresh = it }
                Spacer(Modifier.size(8.dp))
                PinField("Repeat new PIN", again) { again = it }
                if (error.isNotBlank()) {
                    Spacer(Modifier.size(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    old.length < 4 -> error = "Enter your current PIN."
                    fresh.length < 4 -> error = "The new PIN needs at least 4 digits."
                    fresh != again -> error = "The two new PINs do not match."
                    else -> {
                        val ok = onConfirm(old, fresh)
                        error = if (ok) "" else "That current PIN was not correct."
                    }
                }
            }) { Text("Change") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PinField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) onChange(it) },
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
