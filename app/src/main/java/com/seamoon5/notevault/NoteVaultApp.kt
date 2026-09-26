package com.seamoon5.notevault

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seamoon5.notevault.data.AppSession
import com.seamoon5.notevault.data.Note
import com.seamoon5.notevault.data.NotesViewModel
import com.seamoon5.notevault.data.PlainVaultNote
import com.seamoon5.notevault.data.VaultViewModel
import com.seamoon5.notevault.ui.EditorState
import com.seamoon5.notevault.ui.NoteEditorScreen
import com.seamoon5.notevault.ui.NotesScreen
import com.seamoon5.notevault.ui.SettingsScreen
import com.seamoon5.notevault.ui.TabNotes
import com.seamoon5.notevault.ui.VaultListScreen
import com.seamoon5.notevault.ui.VaultLockScreen
import com.seamoon5.notevault.ui.buildMarkdown
import com.seamoon5.notevault.ui.buildPlainText
import com.seamoon5.notevault.ui.fileStamp
import com.seamoon5.notevault.ui.formatDate
import com.seamoon5.notevault.ui.isBlankNote
import com.seamoon5.notevault.ui.safeFileName
import com.seamoon5.notevault.ui.theme.FrostBackground
import com.seamoon5.notevault.ui.theme.NoteVaultTheme
import com.seamoon5.notevault.ui.theme.Prefs
import com.seamoon5.notevault.ui.theme.ThemeMode
import com.seamoon5.notevault.ui.writeToUri
import kotlinx.coroutines.launch

/** Every screen in the app, kept as plain data so the back stack is simple. */
sealed interface Screen {
    data object Notes : Screen
    data object Settings : Screen
    data class Editor(val noteId: Long?) : Screen
    data object Vault : Screen
    data class VaultEditor(val noteId: Long?) : Screen
}

/** TEMPORARY: flipped to true only to design the vault screens. Must be false in the shipped APK. */
private const val TEMP_ALLOW_VAULT_SCREENSHOTS = false

private data class PendingExport(val fileName: String, val content: String)

@Composable
fun NoteVaultApp() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val notesVm: NotesViewModel = viewModel()
    val vaultVm: VaultViewModel = viewModel()

    var themeMode by remember { mutableStateOf(Prefs.themeMode(ctx)) }
    var gridMode by remember { mutableStateOf(Prefs.gridLayout(ctx, true)) }
    val backStack = remember { mutableStateListOf<Screen>(Screen.Notes) }
    val current = backStack.last()

    var editor by remember { mutableStateOf(EditorState()) }
    var pendingMarkdown by remember { mutableStateOf<PendingExport?>(null) }
    var pendingText by remember { mutableStateOf<PendingExport?>(null) }

    fun toast(message: String) = Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()

    fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    // ---- export -----------------------------------------------------------

    val markdownLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        val p = pendingMarkdown
        if (uri != null && p != null) {
            toast(if (writeToUri(ctx, uri, p.content)) "Saved ${p.fileName}" else "Could not save the file")
        }
        pendingMarkdown = null
    }

    val textLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        val p = pendingText
        if (uri != null && p != null) {
            toast(if (writeToUri(ctx, uri, p.content)) "Saved ${p.fileName}" else "Could not save the file")
        }
        pendingText = null
    }

    fun exportVaultNote(note: PlainVaultNote) {
        val base = "secret_${safeFileName(note.title.ifBlank { "note" })}"
        pendingMarkdown = PendingExport("$base.md", buildVaultMarkdown(note))
        markdownLauncher.launch("${base}_${fileStamp()}.md")
    }

    // ---- data -------------------------------------------------------------

    val notes by notesVm.notes.collectAsStateWithLifecycle()
    val folders by notesVm.folders.collectAsStateWithLifecycle()
    val tags by notesVm.allTags.collectAsStateWithLifecycle()
    val filter by notesVm.filterState.collectAsStateWithLifecycle()

    val vaultUnlocked by vaultVm.isUnlocked.collectAsStateWithLifecycle()
    val vaultSetUp by vaultVm.isSetUp.collectAsStateWithLifecycle()
    val vaultNotes by vaultVm.notes.collectAsStateWithLifecycle()
    val vaultCount by vaultVm.noteCount.collectAsStateWithLifecycle()

    // ---- navigation -------------------------------------------------------

    fun openRegular(id: Long?) {
        if (id == null) {
            editor = EditorState()
            backStack.add(Screen.Editor(null))
            return
        }
        scope.launch {
            val n = notesVm.load(id)
            if (n == null) {
                toast("That note no longer exists")
            } else {
                editor = EditorState(
                    id = n.id, title = n.title, body = n.body, tags = n.tags,
                    colorIndex = n.colorIndex, folderId = n.folderId, pinned = n.pinned,
                    createdAt = n.createdAt, updatedAt = n.updatedAt
                )
                backStack.add(Screen.Editor(n.id))
            }
        }
    }

    fun openVaultNote(id: Long?) {
        if (id == null) {
            editor = EditorState()
            backStack.add(Screen.VaultEditor(null))
            return
        }
        scope.launch {
            val n = vaultVm.load(id)
            if (n == null) {
                toast("That secret note no longer exists")
            } else {
                editor = EditorState(
                    id = n.id, title = n.title, body = n.body, tags = n.tags,
                    colorIndex = n.colorIndex, pinned = n.pinned,
                    createdAt = n.createdAt, updatedAt = n.updatedAt
                )
                backStack.add(Screen.VaultEditor(n.id))
            }
        }
    }

    fun saveEditor() {
        if (isBlankNote(editor.title, editor.body)) {
            goBack()
            return
        }
        val now = System.currentTimeMillis()
        if (current is Screen.VaultEditor) {
            vaultVm.save(
                PlainVaultNote(
                    id = editor.id,
                    title = editor.title.trim(),
                    body = editor.body,
                    tags = NotesViewModel.normaliseTags(editor.tags),
                    colorIndex = editor.colorIndex,
                    pinned = editor.pinned,
                    createdAt = if (editor.createdAt == 0L) now else editor.createdAt,
                    updatedAt = now
                )
            )
        } else {
            notesVm.save(
                Note(
                    id = editor.id,
                    title = editor.title,
                    body = editor.body,
                    colorIndex = editor.colorIndex,
                    folderId = editor.folderId,
                    tags = editor.tags,
                    pinned = editor.pinned,
                    createdAt = if (editor.createdAt == 0L) now else editor.createdAt,
                    updatedAt = now
                )
            )
        }
        goBack()
    }

    fun deleteEditor() {
        if (editor.id == 0L) {
            goBack()
            return
        }
        if (current is Screen.VaultEditor) {
            vaultVm.delete(
                PlainVaultNote(
                    id = editor.id, title = editor.title, body = editor.body,
                    tags = editor.tags, colorIndex = editor.colorIndex, pinned = editor.pinned,
                    createdAt = editor.createdAt, updatedAt = editor.updatedAt
                )
            )
        } else {
            notesVm.delete(
                Note(
                    id = editor.id, title = editor.title, body = editor.body,
                    colorIndex = editor.colorIndex, folderId = editor.folderId,
                    tags = editor.tags, pinned = editor.pinned,
                    createdAt = editor.createdAt, updatedAt = editor.updatedAt
                )
            )
        }
        goBack()
    }

    // ---- back button ------------------------------------------------------

    BackHandler(enabled = true) {
        if (backStack.size > 1) goBack() else (ctx as? android.app.Activity)?.finish()
    }

    // ---- vault security ---------------------------------------------------

    LaunchedEffect(Unit) {
        AppSession.registerLockHandler { vaultVm.lock() }
    }

    /**
     * While any vault screen is showing we turn on FLAG_SECURE, which blocks
     * screenshots and blanks the app's thumbnail in the Recent Apps switcher.
     * Normal notes stay screenshottable.
     */
    LaunchedEffect(current) {
        val onVaultScreen = current is Screen.Vault || current is Screen.VaultEditor
        AppSession.setVaultOnScreen(onVaultScreen)
        val window = (ctx as? android.app.Activity)?.window
        if (window != null) {
            if (onVaultScreen) {
                if (TEMP_ALLOW_VAULT_SCREENSHOTS) {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            } else {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    // ---- render -----------------------------------------------------------

    NoteVaultTheme(mode = themeMode) {
        FrostBackground {
            when (current) {
                is Screen.Notes -> NotesScreen(
                    notes = notes,
                    folders = folders,
                    tags = tags,
                    filter = filter,
                    gridMode = gridMode,
                    onGridToggle = {
                        gridMode = it
                        Prefs.setGridLayout(ctx, it)
                    },
                    onQueryChange = notesVm::setQuery,
                    onFolderPick = notesVm::setFolder,
                    onTagPick = notesVm::setTag,
                    onClearFilters = notesVm::clearFilters,
                    onOpen = { openRegular(it) },
                    onPin = notesVm::togglePin,
                    onDelete = notesVm::delete,
                    onExportOne = { note ->
                        val base = safeFileName(note.title.ifBlank { "note" })
                        pendingMarkdown = PendingExport(
                            "$base.md",
                            buildMarkdown(listOf(note), folders)
                        )
                        markdownLauncher.launch("${base}_${fileStamp()}.md")
                    },
                    onTabSelect = { tab ->
                        when (tab) {
                            TabNotes.NOTES -> if (current !is Screen.Notes) backStack.add(Screen.Notes)
                            TabNotes.SEARCH -> {
                                if (current !is Screen.Notes) backStack.add(Screen.Notes)
                            }
                            TabNotes.SETTINGS -> if (current !is Screen.Settings) backStack.add(Screen.Settings)
                            TabNotes.VAULT -> if (current !is Screen.Vault) backStack.add(Screen.Vault)
                            TabNotes.NEW -> openRegular(null)
                        }
                    },
                    onNewNote = { openRegular(null) },
                    vaultBadge = vaultCount
                )

                is Screen.Settings -> SettingsScreen(
                    themeMode = themeMode,
                    onThemeChange = {
                        themeMode = it
                        Prefs.setThemeMode(ctx, it)
                    },
                    gridMode = gridMode,
                    onGridChange = {
                        gridMode = it
                        Prefs.setGridLayout(ctx, it)
                    },
                    folders = folders,
                    onCreateFolder = notesVm::createFolder,
                    onDeleteFolder = notesVm::deleteFolder,
                    onExportAll = { asMarkdown ->
                        scope.launch {
                            val allNotes = notesVm.notesForExport()
                            val allFolders = notesVm.foldersForExport()
                            if (allNotes.isEmpty()) {
                                toast("There are no notes to export")
                                return@launch
                            }
                            val stamp = fileStamp()
                            if (asMarkdown) {
                                pendingMarkdown = PendingExport(
                                    "NoteVault_$stamp.md",
                                    buildMarkdown(allNotes, allFolders)
                                )
                                markdownLauncher.launch("NoteVault_$stamp.md")
                            } else {
                                pendingText = PendingExport(
                                    "NoteVault_$stamp.txt",
                                    buildPlainText(allNotes, allFolders)
                                )
                                textLauncher.launch("NoteVault_$stamp.txt")
                            }
                        }
                    },
                    vaultIsSetUp = vaultSetUp,
                    onChangePin = { old, new -> vaultVm.changePin(old, new) },
                    onEraseVault = { vaultVm.destroyVault { toast("Vault erased") } },
                    onBack = { goBack() }
                )

                is Screen.Editor -> NoteEditorScreen(
                    state = editor,
                    folders = folders,
                    isVault = false,
                    onChange = { editor = it },
                    onSave = { saveEditor() },
                    onDelete = { deleteEditor() },
                    onBack = { goBack() }
                )

                is Screen.VaultEditor -> NoteEditorScreen(
                    state = editor,
                    folders = emptyList(),
                    isVault = true,
                    onChange = { editor = it },
                    onSave = { saveEditor() },
                    onDelete = { deleteEditor() },
                    onBack = { goBack() }
                )

                is Screen.Vault -> if (vaultUnlocked) {
                    VaultListScreen(
                        notes = vaultNotes,
                        onBack = { goBack() },
                        onLock = {
                            vaultVm.lock()
                            goBack()
                        },
                        onOpen = { openVaultNote(it) },
                        onCreate = { openVaultNote(null) },
                        onPin = vaultVm::togglePin,
                        onDelete = vaultVm::delete,
                        onExportOne = { exportVaultNote(it) }
                    )
                } else {
                    VaultLockScreen(
                        isSetUp = vaultSetUp,
                        hasNotes = vaultCount,
                        onSubmitPin = { pin -> vaultVm.unlockWithPin(pin) },
                        onSetupPin = { pin -> vaultVm.createVault(pin) },
                        onBiometricSuccess = { vaultVm.unlockWithBiometric() },
                        onResetVault = { vaultVm.destroyVault { toast("Vault erased") } }
                    )
                }
            }
        }
    }
}

private fun buildVaultMarkdown(note: PlainVaultNote): String {
    val sb = StringBuilder()
    sb.append("## ").append(if (note.title.isBlank()) "Untitled note" else note.title).append("\n")
    sb.append("*").append(formatDate(note.updatedAt))
    if (note.tags.isNotBlank()) sb.append(" - Tags: ").append(note.tags)
    if (note.pinned) sb.append(" - Pinned")
    sb.append("*\n\n")
    if (note.body.isNotBlank()) sb.append(note.body.trim()).append("\n\n")
    sb.append("---\n\n")
    return sb.toString()
}
