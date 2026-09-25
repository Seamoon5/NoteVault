package com.seamoon5.notevault.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId")]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String = "",
    val body: String = "",
    val colorIndex: Int = 0,
    val folderId: Long? = null,
    val tags: String = "",
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Vault notes live in a separate database file. Title, body and tags are never
 * stored as readable text: they are AES-256-GCM ciphertext produced by
 * [com.seamoon5.notevault.crypto.VaultCrypto]. [lastError] is set when a row
 * could not be decrypted (for example after the keystore key was destroyed),
 * so the UI can show a placeholder instead of crashing.
 */
@Entity(tableName = "vault_notes")
data class VaultNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val titleEnc: String = "",
    val bodyEnc: String = "",
    val tagsEnc: String = "",
    val colorIndex: Int = 0,
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastError: Boolean = false
)
