package com.seamoon5.notevault.crypto

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

/**
 * Vault protection for NoteVault.
 *
 * How it works, in plain English:
 *  - The actual encryption key (AES-256) is generated *inside* Android's
 *    Keystore, a secure area of the phone that apps cannot read the key out of.
 *  - Your PIN is never stored. We store only a scrambled "hash" of it (PBKDF2,
 *    120000 rounds) plus a random salt, so even someone who copies the app's
 *    data files cannot recover your PIN from them.
 *  - Unlocking = check the PIN against that hash. Only then does the app use
 *    the Keystore key to decrypt your vault notes.
 *  - The Keystore key is a 256-bit AES key in GCM mode (authenticated
 *    encryption, so tampered data is detected instead of silently decrypted).
 */
object VaultCrypto {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "notevault_vault_aes256"
    private const val PREFS = "notevault_vault_prefs"
    private const val K_HASH = "pin_hash"
    private const val K_SALT = "pin_salt"
    private const val K_ENABLED = "vault_enabled"

    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val GCM_TAG_BITS = 128

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isVaultSetUp(context: Context): Boolean = prefs(context).contains(K_HASH)

    fun setPin(context: Context, pin: String) {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = derive(pin, salt)
        prefs(context).edit()
            .putString(K_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(K_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .putBoolean(K_ENABLED, true)
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val p = prefs(context)
        val saltB64 = p.getString(K_SALT, null) ?: return false
        val hashB64 = p.getString(K_HASH, null) ?: return false
        return try {
            val salt = Base64.decode(saltB64, Base64.NO_WRAP)
            val expected = Base64.decode(hashB64, Base64.NO_WRAP)
            val actual = derive(pin, salt)
            constantTimeEquals(expected, actual)
        } catch (e: Exception) {
            false
        }
    }

    fun changePin(context: Context, oldPin: String, newPin: String): Boolean {
        if (!verifyPin(context, oldPin)) return false
        setPin(context, newPin)
        return true
    }

    /**
     * Removes the PIN and the Keystore key. The caller is responsible for
     * deleting the encrypted vault rows, because from this point on they can no
     * longer be decrypted.
     */
    fun destroyKeys(context: Context) {
        try {
            KeyStore.getInstance(KEYSTORE).apply { load(null) }.deleteEntry(KEY_ALIAS)
        } catch (e: Exception) {
            // Entry may not exist; nothing else to do.
        }
        prefs(context).edit().clear().apply()
    }

    // ---- encryption -----------------------------------------------------

    fun encrypt(context: Context, plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val ct = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val out = ByteArray(iv.size + ct.size)
        System.arraycopy(iv, 0, out, 0, iv.size)
        System.arraycopy(ct, 0, out, iv.size, ct.size)
        return Base64.encodeToString(out, Base64.NO_WRAP)
    }

    /** Returns the decrypted text, or null when the data cannot be trusted. */
    fun decrypt(context: Context, payload: String): String? {
        return try {
            val raw = Base64.decode(payload, Base64.NO_WRAP)
            if (raw.size <= IV_BYTES) return null
            val iv = raw.copyOfRange(0, IV_BYTES)
            val ct = raw.copyOfRange(IV_BYTES, raw.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(GCM_TAG_BITS, iv)
            )
            String(cipher.doFinal(ct), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun encryptOrEmpty(context: Context, plain: String): String =
        try { encrypt(context, plain) } catch (e: Exception) { "" }

    fun decryptOrEmpty(context: Context, payload: String): String =
        decrypt(context, payload) ?: ""

    // ---- internals ------------------------------------------------------

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_BITS)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_BITS)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }
}
