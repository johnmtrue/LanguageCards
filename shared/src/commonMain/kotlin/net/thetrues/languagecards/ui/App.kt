package net.thetrues.languagecards.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.thetrues.languagecards.data.DeckRepository
import net.thetrues.languagecards.model.LanguageCombination
import net.thetrues.languagecards.model.SessionState
import net.thetrues.languagecards.repository.StatsRepository
import net.thetrues.languagecards.session.SessionFlow
import net.thetrues.languagecards.ui.screens.CardScreen
import net.thetrues.languagecards.ui.screens.StartScreen
import net.thetrues.languagecards.ui.screens.StatsScreen
import net.thetrues.languagecards.ui.screens.SummaryScreen
import net.thetrues.languagecards.ui.theme.LanguageCardsTheme

private const val APP_VERSION = "0.1"

/**
 * Shared entry point for the Language Cards app.
 * [deckRepository] loads decks from SQLite.
 * [statsStore] persists stats to SQLite (SqlDelightStatsRepository).
 * [onExit] is invoked when the user taps Exit (e.g. activity.finish() on Android).
 * [authorName] is shown in the About dialog (default: "Your Name").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    deckRepository: DeckRepository,
    statsStore: StatsRepository,
    onExit: () -> Unit,
    authorName: String = "Your Name",
) {
    LanguageCardsTheme {
        var sessionState by remember { mutableStateOf<SessionState?>(null) }
        var languageCombinations by remember { mutableStateOf<List<LanguageCombination>?>(null) }
        var menuExpanded by remember { mutableStateOf(false) }
        var aboutDialogShown by remember { mutableStateOf(false) }
        var statsScreenShown by remember { mutableStateOf(false) }
        var clearStatsDialogShown by remember { mutableStateOf(false) }
        val year = 2026

        LaunchedEffect(deckRepository) {
            languageCombinations = withContext(Dispatchers.Default) {
                deckRepository.getLanguageCombinations()
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize().safeContentPadding(),
            topBar = {
                TopAppBar(
                    title = { Text("Language Cards") },
                    actions = {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Menu",
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("About") },
                                    onClick = {
                                        menuExpanded = false
                                        aboutDialogShown = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Stats") },
                                    onClick = {
                                        menuExpanded = false
                                        statsScreenShown = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear stats") },
                                    onClick = {
                                        menuExpanded = false
                                        clearStatsDialogShown = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Exit") },
                                    onClick = {
                                        menuExpanded = false
                                        onExit()
                                    },
                                )
                            }
                        }
                    },
                )
            },
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when {
                    sessionState == null -> when (val combos = languageCombinations) {
                        null -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                        else -> StartScreen(
                            languageCombinations = combos,
                            onStart = { deck, direction ->
                                sessionState = SessionFlow.startSession(deck, direction)
                            },
                            onExit = onExit,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    sessionState!!.isAtSummary -> SummaryScreen(
                        state = sessionState!!,
                        onPracticeAgain = { sessionState = null },
                        onExit = onExit,
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> CardScreen(
                        state = sessionState!!,
                        onAnswer = { cardId, wasHit ->
                            statsStore.record(cardId, wasHit)
                            sessionState = SessionFlow.recordAnswer(sessionState!!, cardId, wasHit)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                if (statsScreenShown) {
                    StatsScreen(
                        stats = statsStore.getAllStats(),
                        onDismiss = { statsScreenShown = false },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        if (aboutDialogShown) {
            AlertDialog(
                onDismissRequest = { aboutDialogShown = false },
                title = { Text("About Language Cards") },
                text = {
                    Column {
                        Text("Version $APP_VERSION")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(authorName)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("© $year $authorName. Licensed under MIT.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { aboutDialogShown = false }) {
                        Text("OK")
                    }
                },
            )
        }
        if (clearStatsDialogShown) {
            AlertDialog(
                onDismissRequest = { clearStatsDialogShown = false },
                title = { Text("Clear all stats?") },
                text = {
                    Text("This will permanently delete all practice history. This cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            statsStore.clearAllStats()
                            clearStatsDialogShown = false
                        },
                    ) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { clearStatsDialogShown = false }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}
