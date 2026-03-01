package net.thetrues.languagecards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import net.thetrues.languagecards.platform.AndroidStatsStore
import net.thetrues.languagecards.repository.StatsRepository
import net.thetrues.languagecards.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val statsStore: StatsRepository = remember {
                AndroidStatsStore(applicationContext, lifecycleScope)
            }
            App(
                statsStore = statsStore,
                onExit = { finish() },
                authorName = "John True",
            )
        }
    }
}
