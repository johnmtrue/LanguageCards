package net.thetrues.languagecards.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.thetrues.languagecards.data.DeckRepository
import net.thetrues.languagecards.shared.generated.resources.Res
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.LanguageCombination
import net.thetrues.languagecards.model.SessionState
import net.thetrues.languagecards.repository.StatsRepository
import net.thetrues.languagecards.session.SessionFlow
import net.thetrues.languagecards.ui.screens.CardScreen
import net.thetrues.languagecards.ui.screens.StartScreen
import net.thetrues.languagecards.ui.screens.StatsScreen
import net.thetrues.languagecards.ui.screens.SummaryScreen
import net.thetrues.languagecards.ui.theme.LanguageCardsTheme

/**
 * Shared entry point for the Language Cards app.
 * [deckRepository] loads decks from SQLite.
 * [statsStore] persists stats to SQLite (SqlDelightStatsRepository).
 * [onExit] is invoked when the user taps Exit (e.g. activity.finish() on Android).
 * [authorName] is shown in the About dialog (default: "Your Name").
 * [versionName] is shown in the About dialog (default: "0.1"). Pass BuildConfig.VERSION_NAME on Android.
 * [onRequestImportDeck] when non-null, enables "Import deck" menu. Platform shows file picker
 * and invokes the callback with the file content. Null on platforms without file picker support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    deckRepository: DeckRepository,
    statsStore: StatsRepository,
    onExit: () -> Unit,
    authorName: String = "Your Name",
    versionName: String = "0.1",
    onRequestImportDeck: (((String) -> Unit) -> Unit)? = null,
) {
    LanguageCardsTheme {
        var sessionState by remember { mutableStateOf<SessionState?>(null) }
        var languageCombinations by remember { mutableStateOf<List<LanguageCombination>?>(null) }
        var refreshTrigger by remember { mutableStateOf(0) }
        var menuExpanded by remember { mutableStateOf(false) }
        var aboutDialogShown by remember { mutableStateOf(false) }
        var statsScreenShown by remember { mutableStateOf(false) }
        var clearStatsDialogShown by remember { mutableStateOf(false) }
        var deleteDeckDialogShown by remember { mutableStateOf(false) }
        var restoreDefaultDecksDialogShown by remember { mutableStateOf(false) }
        var addDeckDialogShown by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val year = 2026

        LaunchedEffect(deckRepository, refreshTrigger) {
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
                                // Group 1: Deck management
                                if (onRequestImportDeck != null) {
                                    val requestImport = onRequestImportDeck
                                    DropdownMenuItem(
                                        text = { Text("Import deck") },
                                        onClick = {
                                            menuExpanded = false
                                            requestImport { json ->
                                                deckRepository.addDeckFromJson(json).onSuccess {
                                                    refreshTrigger++
                                                }
                                            }
                                        },
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Add a deck") },
                                    onClick = {
                                        menuExpanded = false
                                        addDeckDialogShown = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete deck") },
                                    onClick = {
                                        menuExpanded = false
                                        deleteDeckDialogShown = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Restore default decks") },
                                    onClick = {
                                        menuExpanded = false
                                        restoreDefaultDecksDialogShown = true
                                    },
                                )
                                HorizontalDivider()
                                // Group 2: Stats
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
                                HorizontalDivider()
                                // Group 3: About & Exit
                                DropdownMenuItem(
                                    text = { Text("About") },
                                    onClick = {
                                        menuExpanded = false
                                        aboutDialogShown = true
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
                                sessionState = SessionFlow.startSession(deck, direction, statsStore)
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
                            statsStore.record(cardId, sessionState!!.direction, wasHit)
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
                        Text("Version $versionName")
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
        if (deleteDeckDialogShown && languageCombinations != null) {
            DeleteDeckDialog(
                languageCombinations = languageCombinations!!,
                deckRepository = deckRepository,
                onDismiss = { deleteDeckDialogShown = false },
                onDeleted = {
                    refreshTrigger++
                    deleteDeckDialogShown = false
                },
            )
        }
        if (addDeckDialogShown && languageCombinations != null) {
            AddDeckDialog(
                currentDeckIds = languageCombinations!!.flatMap { it.decks }.map { it.id }.toSet(),
                deckRepository = deckRepository,
                onDismiss = { addDeckDialogShown = false },
                onAdded = {
                    refreshTrigger++
                    addDeckDialogShown = false
                },
                scope = scope,
            )
        }
        if (restoreDefaultDecksDialogShown) {
            AlertDialog(
                onDismissRequest = { restoreDefaultDecksDialogShown = false },
                title = { Text("Restore default decks?") },
                text = {
                    Text("This will remove all current decks and restore the four original decks: French Basics, French Past Tense, French Conversation, and Spanish Basics.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            restoreDefaultDecksDialogShown = false
                            scope.launch {
                                withContext(Dispatchers.Default) {
                                    restoreDefaultDecks(deckRepository)
                                }
                                refreshTrigger++
                            }
                        },
                    ) {
                        Text("Restore")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { restoreDefaultDecksDialogShown = false }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}

private data class BundledDeck(val path: String, val displayName: String, val deckId: String)

private val BUNDLED_DECKS = listOf(
    BundledDeck("files/en-fr-french-basics.deck.json", "French — Basics", "french-1"),
    BundledDeck("files/en-fr-french-past-tense.deck.json", "French - Past Tense", "french-past"),
    BundledDeck("files/en-fr-french-conversation.deck.json", "French - Conversation", "french-conversation"),
    BundledDeck("files/en-es-spanish-basics.deck.json", "Spanish — Basics", "spanish-1"),
    BundledDeck("files/en-es-spanish-conversation.deck.json", "Spanish - Conversation", "spanish-conversation"),
    BundledDeck("files/en-de-german-basics.deck.json", "German — Basics", "german-1"),
    BundledDeck("files/en-de-german-conversation.deck.json", "German - Conversation", "german-conversation"),
)

@OptIn(ExperimentalMaterial3Api::class, org.jetbrains.compose.resources.ExperimentalResourceApi::class)
@Composable
private fun AddDeckDialog(
    currentDeckIds: Set<String>,
    deckRepository: DeckRepository,
    onDismiss: () -> Unit,
    onAdded: () -> Unit,
    scope: CoroutineScope,
) {
    val unloadedDecks = BUNDLED_DECKS.filter { it.deckId !in currentDeckIds }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text("Add a deck") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text("Select a deck to add from the bundled decks:")
                Spacer(modifier = Modifier.height(8.dp))
                if (unloadedDecks.isEmpty()) {
                    Text("All bundled decks are already loaded.")
                } else {
                    unloadedDecks.forEach { bundled ->
                        ListItem(
                            headlineContent = { Text(bundled.displayName) },
                            modifier = Modifier.fillMaxWidth().clickable {
                                scope.launch {
                                    withContext(Dispatchers.Default) {
                                        val json = Res.readBytes(bundled.path).decodeToString()
                                        deckRepository.addDeckFromJson(json)
                                    }
                                    onAdded()
                                }
                            },
                        )
                    }
                }
            }
        },
    )
}

@OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
private suspend fun restoreDefaultDecks(deckRepository: DeckRepository) {
    val combos = deckRepository.getLanguageCombinations()
    combos.flatMap { it.decks }.forEach { deckRepository.deleteDeck(it.id) }
    val defaultDeckPaths = listOf(
        "files/en-fr-french-basics.deck.json",
        "files/en-fr-french-past-tense.deck.json",
        "files/en-fr-french-conversation.deck.json",
        "files/en-es-spanish-basics.deck.json",
    )
    for (path in defaultDeckPaths) {
        val json = Res.readBytes(path).decodeToString()
        deckRepository.addDeckFromJson(json)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteDeckDialog(
    languageCombinations: List<LanguageCombination>,
    deckRepository: DeckRepository,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit,
) {
    val allDecks = languageCombinations.flatMap { combo ->
        combo.decks.map { deck -> combo to deck }
    }
    var selectedDeck by remember { mutableStateOf<Pair<LanguageCombination, Deck>?>(allDecks.firstOrNull()) }
    var deckExpanded by remember { mutableStateOf(false) }
    val comboDeck = selectedDeck ?: allDecks.firstOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete deck") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Select a deck to delete:")
                Spacer(modifier = Modifier.height(8.dp))
                if (allDecks.isEmpty()) {
                    Text("No decks available.")
                } else {
                    ExposedDropdownMenuBox(
                        expanded = deckExpanded,
                        onExpandedChange = { deckExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = comboDeck?.let { "${it.second.name} (${it.first.name})" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deckExpanded) },
                        )
                        ExposedDropdownMenu(
                            expanded = deckExpanded,
                            onDismissRequest = { deckExpanded = false },
                        ) {
                            allDecks.forEach { (combo, deck) ->
                                DropdownMenuItem(
                                    text = { Text("${deck.name} (${combo.name})") },
                                    onClick = {
                                        selectedDeck = combo to deck
                                        deckExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    comboDeck?.let { (_, deck) ->
                        deckRepository.deleteDeck(deck.id)
                        onDeleted()
                    }
                },
                enabled = comboDeck != null,
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
