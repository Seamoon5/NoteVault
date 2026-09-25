package com.seamoon5.notevault

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity

/**
 * NoteVault extends FragmentActivity (not plain ComponentActivity) because the
 * fingerprint prompt from androidx.biometric requires a FragmentActivity.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent { NoteVaultApp() }
    }

    /**
     * The moment Salman switches away from the app, the vault locks itself, so
     * coming back always needs the PIN or fingerprint again.
     */
    override fun onStop() {
        super.onStop()
        com.seamoon5.notevault.data.AppSession.onAppBackgrounded()
    }
}
