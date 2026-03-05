package net.thetrues.languagecards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import net.thetrues.languagecards.data.SqlDelightDeckRepository
import net.thetrues.languagecards.data.createDatabase
import net.thetrues.languagecards.repository.SqlDelightStatsRepository
import net.thetrues.languagecards.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val database = remember { createDatabase(applicationContext) }
            val deckRepository = remember(database) { SqlDelightDeckRepository(database) }
            val statsStore = remember(database) { SqlDelightStatsRepository(database) }
            App(
                deckRepository = deckRepository,
                statsStore = statsStore,
                onExit = { finish() },
                authorName = "John True",
            )
        }
    }
}
