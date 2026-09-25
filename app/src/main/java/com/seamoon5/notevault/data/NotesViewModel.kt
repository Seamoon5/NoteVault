package com.seamoon5.notevault.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NoteFilter(
    val query: String = "",
    val folderId: Long? = null,
    val tag: String? = null
)

class NotesViewModel(app: Application) : AndroidViewModel(app) {

    private val db = NoteDatabase.get(app)
    private val noteDao = db.noteDao()
    private val folderDao = db.folderDao()

    private val filter = MutableStateFlow(NoteFilter())
    val filterState: StateFlow<NoteFilter> = filter.asStateFlow()

    val folders: StateFlow<List<Folder>> =
        folderDao.allFolders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Every note, ignoring search/filters. Used for the tag list and export. */
    val allNotes: StateFlow<List<Note>> =
        noteDao.allNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val searched: StateFlow<List<Note>> = filter
        .flatMapLatest { f ->
            if (f.query.isBlank()) noteDao.allNotes() else noteDao.searchNotes(f.query.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = combine(searched, filter) { list, f ->
        list.filter { note ->
            (f.folderId == null || note.folderId == f.folderId) &&
                (f.tag == null || note.tags.split(",").any { it.equals(f.tag, true) })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTags: StateFlow<List<String>> = allNotes
        .map { list ->
            list.flatMap { it.tags.split(",") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) {
        filter.value = filter.value.copy(query = q)
    }

    fun setFolder(id: Long?) {
        filter.value = filter.value.copy(folderId = id)
    }

    fun setTag(tag: String?) {
        filter.value = filter.value.copy(tag = tag)
    }

    fun clearFilters() {
        filter.value = NoteFilter()
    }

    suspend fun load(id: Long): Note? = noteDao.noteById(id)

    fun save(note: Note, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val clean = note.copy(
                title = note.title.trim(),
                tags = normaliseTags(note.tags),
                updatedAt = now,
                createdAt = if (note.id == 0L) now else note.createdAt
            )
            if (clean.id == 0L) {
                onDone(noteDao.insert(clean))
            } else {
                noteDao.update(clean)
                onDone(clean.id)
            }
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch { noteDao.deleteById(note.id) }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteDao.update(note.copy(pinned = !note.pinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun createFolder(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            folderDao.insert(Folder(name = clean, createdAt = System.currentTimeMillis()))
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch { folderDao.deleteById(folder.id) }
    }

    suspend fun notesForExport(): List<Note> = noteDao.allNotesOnce()

    suspend fun foldersForExport(): List<Folder> = folderDao.allFoldersOnce()

    companion object {
        /** "a, b ,,c" -> "a,b,c" (lowercase, de-duplicated, original order kept). */
        fun normaliseTags(raw: String): String =
            raw.split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .distinct()
                .joinToString(",")
    }
}
