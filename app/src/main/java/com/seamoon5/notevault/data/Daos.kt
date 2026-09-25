package com.seamoon5.notevault.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC")
    fun allNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun noteById(id: Long): Note?

    @Query(
        """
        SELECT * FROM notes
        WHERE title LIKE '%' || :q || '%'
           OR body  LIKE '%' || :q || '%'
           OR tags  LIKE '%' || :q || '%'
        ORDER BY pinned DESC, updatedAt DESC
        """
    )
    fun searchNotes(q: String): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC")
    suspend fun allNotesOnce(): List<Note>
}

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE ASC")
    fun allFolders(): Flow<List<Folder>>

    @Insert
    suspend fun insert(folder: Folder): Long

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE ASC")
    suspend fun allFoldersOnce(): List<Folder>
}

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_notes ORDER BY pinned DESC, updatedAt DESC")
    fun allNotes(): Flow<List<VaultNote>>

    @Query("SELECT * FROM vault_notes WHERE id = :id")
    suspend fun noteById(id: Long): VaultNote?

    @Insert
    suspend fun insert(note: VaultNote): Long

    @Update
    suspend fun update(note: VaultNote)

    @Query("DELETE FROM vault_notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM vault_notes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM vault_notes")
    suspend fun count(): Int
}
