package com.seamoon5.notevault.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val MIN_PIN = 4
private const val MAX_PIN = 8

@Composable
fun VaultLockScreen(
    isSetUp: Boolean,
    hasNotes: Int,
    onSubmitPin: (String) -> Boolean,
    onSetupPin: (String) -> Unit,
    onBiometricSuccess: () -> Unit,
    onResetVault: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var pin by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf(if (isSetUp) 0 else 1) } // 0 unlock, 1 new, 2 confirm
    var firstPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var shake by remember { mutableStateOf(0f) }
    var showReset by remember { mutableStateOf(false) }
    var resetArmed by remember { mutableStateOf(false) }
    var biometricError by remember { mutableStateOf("") }

    val biometricAvailable = remember {
        activity != null &&
            BiometricManager.from(context)
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    val shakeOffset by animateFloatAsState(if (shake != 0f) 1f else 0f, label = "shake")

    fun fail(message: String) {
        error = message
        pin = ""
        shake = if (shake == 0f) 1f else 0f
    }

    fun submit() {
        when (stage) {
            0 -> if (!onSubmitPin(pin)) fail("Wrong PIN. Try again.")
            1 -> {
                firstPin = pin
                pin = ""
                stage = 2
                error = ""
            }
            2 -> {
                if (pin != firstPin) {
                    stage = 1
                    firstPin = ""
                    fail("PINs did not match. Start again.")
                } else {
                    onSetupPin(pin)
                }
            }
        }
    }

    // Auto-submit as soon as the PIN reaches the longest sensible length.
    LaunchedEffect(pin, stage) {
        if (stage == 0 && pin.length == 6) submit()
        if (stage == 1 && pin.length == 6) submit()
        if (stage == 2 && pin.length == 6) submit()
    }

    fun launchBiometric() {
        val frag = activity ?: return
        biometricError = ""
        val prompt = BiometricPrompt(
            frag,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onBiometricSuccess()
                }

                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    // "User canceled" and negative-button presses are normal, not errors.
                    if (code != BiometricPrompt.ERROR_USER_CANCELED &&
                        code != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        code != BiometricPrompt.ERROR_CANCELED
                    ) {
                        biometricError = msg.toString()
                    }
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock NoteVault")
            .setSubtitle("Touch the fingerprint sensor to open your vault")
            .setNegativeButtonText("Use PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
        runCatching { prompt.authenticate(info) }
            .onFailure { biometricError = "Fingerprint not available. Use your PIN." }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(84.dp)
                .scale(1f + shakeOffset * 0.06f)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSetUp) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = when (stage) {
                0 -> "Vault locked"
                1 -> "Create your vault"
                else -> "Confirm your PIN"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = when (stage) {
                0 -> "Enter your $MIN_PIN to $MAX_PIN digit PIN"
                1 -> "Choose a $MIN_PIN to $MAX_PIN digit PIN. This cannot be recovered."
                else -> "Type the same PIN again"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (!isSetUp && hasNotes == 0) {
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(20.dp))

        PinDots(length = pin.length, max = MAX_PIN)

        Spacer(Modifier.height(8.dp))

        if (error.isNotBlank()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        } else if (biometricError.isNotBlank()) {
            Text(
                text = biometricError,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(12.dp))

        Keypad(
            onDigit = { d ->
                error = ""
                if (pin.length < MAX_PIN) pin += d
            },
            onDelete = {
                error = ""
                if (pin.isNotEmpty()) pin = pin.dropLast(1)
            },
            onSubmit = { if (pin.length >= MIN_PIN) submit() }
        )

        if (biometricAvailable && stage == 0) {
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        RoundedCornerShape(24.dp)
                    )
                    .clickable { launchBiometric() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Icon(
                    Icons.Filled.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Use fingerprint", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.weight(1f))

        if (isSetUp && stage == 0) {
            TextButton(onClick = { showReset = true }) {
                Text(
                    "Forgot PIN?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showReset) {
        if (!resetArmed) {
            AlertDialog(
                onDismissRequest = { showReset = false },
                title = { Text("Forgot your PIN?") },
                text = {
                    Text(
                        "A NoteVault PIN cannot be recovered by anyone, including us. " +
                            "The only way in is to erase the vault, which deletes all " +
                            "$hasNotes secret note(s) forever."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { resetArmed = true }) {
                        Text("Erase my vault", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReset = false }) { Text("Go back") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showReset = false },
                title = { Text("Last chance") },
                text = {
                    Text(
                        "This permanently deletes the vault key and all $hasNotes secret " +
                            "note(s). There is no undo."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showReset = false
                        resetArmed = false
                        pin = ""
                        stage = 1
                        firstPin = ""
                        onResetVault()
                    }) {
                        Text("Yes, erase everything", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { resetArmed = false }) { Text("Keep my vault") }
                }
            )
        }
    }
}

@Composable
private fun PinDots(length: Int, max: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(max) { i ->
            val filled = i < length
            Box(
                modifier = Modifier
                    .size(if (filled) 15.dp else 12.dp)
                    .background(
                        color = if (filled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun Keypad(onDigit: (String) -> Unit, onDelete: () -> Unit, onSubmit: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { d ->
                    KeyBox(onClick = { onDigit(d) }) {
                        Text(d, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Spacer(Modifier.size(72.dp))
            KeyBox(onClick = { onDigit("0") }) {
                Text("0", style = MaterialTheme.typography.headlineSmall)
            }
            KeyBox(onClick = onDelete) {
                Icon(
                    Icons.Filled.Backspace,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Surface(
            onClick = onSubmit,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .width(236.dp)
                .height(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Continue", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun KeyBox(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(72.dp)
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
fun VaultBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Text("Vault", style = MaterialTheme.typography.labelSmall)
        }
    }
}
