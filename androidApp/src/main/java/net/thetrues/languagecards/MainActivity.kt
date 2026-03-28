package net.thetrues.languagecards

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.thetrues.languagecards.data.SqlDelightDeckRepository
import net.thetrues.languagecards.data.createDatabase
import net.thetrues.languagecards.repository.SqlDelightStatsRepository
import net.thetrues.languagecards.settings.createSettingsStore
import net.thetrues.languagecards.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val database = remember { createDatabase(applicationContext) }
            val deckRepository = remember(database) { SqlDelightDeckRepository(database) }
            val statsStore = remember(database) { SqlDelightStatsRepository(database) }
            val settingsStore = remember { createSettingsStore(applicationContext) }
            var importCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }
            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
            ) { uri: Uri? ->
                try {
                    uri?.let {
                        contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                            importCallback?.invoke(reader.readText())
                        }
                    }
                } finally {
                    importCallback = null
                }
            }
            val onRequestImportDeck: ((String) -> Unit) -> Unit = { callback ->
                importCallback = callback
                filePickerLauncher.launch("*/*")
            }
            App(
                deckRepository = deckRepository,
                statsStore = statsStore,
                settingsStore = settingsStore,
                onExit = { finish() },
                authorName = "John True",
                versionName = BuildConfig.VERSION_NAME,
                onRequestImportDeck = onRequestImportDeck,
            )
        }
    }
}
