package com.seamoon5.notevault.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seamoon5.notevault.crypto.VaultCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A vault note after decryption, safe to show in the UI. */
data class PlainVaultNote(
    val id: Long,
    val title: String,
    val body: String,
    val tags: String,
    val colorIndex: Int,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val broken: Boolean = false
)

/**
 * Tracks whether the vault is on screen, so MainActivity can turn FLAG_SECURE
 * (blocks screenshots and blanks the Recents thumbnail) on and off, and can
 * lock the vault the moment Salman leaves the app.
 */
object AppSession {
    var vaultOnScreen: Boolean = false
        private set

    private var lockHandler: (() -> Unit)? = null

    fun setVaultOnScreen(value: Boolean) {
        vaultOnScreen = value
    }

    /** The vault screen registers how to lock itself; the Activity calls it. */
    fun registerLockHandler(handler: () -> Unit) {
        lockHandler = handler
    }

    /** Called from Activity.onStop(). */
    fun onAppBackgrounded() {
        if (vaultOnScreen) lockHandler?.invoke()
    }
}

class VaultViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx: Context = app.applicationContext
    private val dao = VaultDatabase.get(ctx).vaultDao()

    private val unlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = unlocked.asStateFlow()

    private val setUp = MutableStateFlow(VaultCrypto.isVaultSetUp(ctx))
    val isSetUp: StateFlow<Boolean> = setUp.asStateFlow()

    private val rawNotes = dao.allNotes()

    val notes: StateFlow<List<PlainVaultNote>> =
        combine(rawNotes, unlocked) { rows, isUnlocked ->
            if (!isUnlocked) emptyList() else rows.map { it.decrypt(ctx) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noteCount: StateFlow<Int> =
        rawNotes.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ---- unlock / lock --------------------------------------------------

    fun unlockWithPin(pin: String): Boolean {
        val ok = VaultCrypto.verifyPin(ctx, pin)
        if (ok) unlocked.value = true
        return ok
    }

    /** Call only after BiometricPrompt has reported success. */
    fun unlockWithBiometric() {
        if (VaultCrypto.isVaultSetUp(ctx)) unlocked.value = true
    }

    fun lock() {
        unlocked.value = false
    }

    fun createVault(pin: String) {
        VaultCrypto.setPin(ctx, pin)
        unlocked.value = true
        setUp.value = true
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        val ok = VaultCrypto.changePin(ctx, oldPin, newPin)
        if (ok) setUp.value = VaultCrypto.isVaultSetUp(ctx)
        return ok
    }

    /** Wipes the PIN, the Keystore key and every vault note. Irreversible. */
    fun destroyVault(onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { dao.deleteAll() }
            VaultCrypto.destroyKeys(ctx)
            unlocked.value = false
            setUp.value = false
            onDone()
        }
    }

    // ---- notes ----------------------------------------------------------

    suspend fun load(id: Long): PlainVaultNote? =
        withContext(Dispatchers.IO) { dao.noteById(id)?.decrypt(ctx) }

    fun save(note: PlainVaultNote, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val row = VaultNote(
                id = note.id,
                titleEnc = VaultCrypto.encryptOrEmpty(ctx, note.title),
                bodyEnc = VaultCrypto.encryptOrEmpty(ctx, note.body),
                tagsEnc = VaultCrypto.encryptOrEmpty(ctx, note.tags),
                colorIndex = note.colorIndex,
                pinned = note.pinned,
                createdAt = if (note.id == 0L) now else note.createdAt,
                updatedAt = now,
                lastError = false
            )
            if (row.id == 0L) {
                onDone(dao.insert(row))
            } else {
                dao.update(row)
                onDone(row.id)
            }
        }
    }

    fun delete(note: PlainVaultNote) {
        viewModelScope.launch { dao.deleteById(note.id) }
    }

    fun togglePin(note: PlainVaultNote) {
        viewModelScope.launch {
            val row = dao.noteById(note.id) ?: return@launch
            dao.update(row.copy(pinned = !row.pinned, updatedAt = System.currentTimeMillis()))
        }
    }

    private fun VaultNote.decrypt(ctx: Context): PlainVaultNote {
        val title = VaultCrypto.decrypt(ctx, titleEnc)
        val body = VaultCrypto.decrypt(ctx, bodyEnc)
        if (title == null || body == null) {
            return PlainVaultNote(
                id = id,
                title = "",
                body = "",
                tags = "",
                colorIndex = colorIndex,
                pinned = pinned,
                createdAt = createdAt,
                updatedAt = updatedAt,
                broken = true
            )
        }
        return PlainVaultNote(
            id = id,
            title = title,
            body = body,
            tags = VaultCrypto.decryptOrEmpty(ctx, tagsEnc),
            colorIndex = colorIndex,
            pinned = pinned,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
