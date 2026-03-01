package net.thetrues.languagecards.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.thetrues.languagecards.data.SampleData
import net.thetrues.languagecards.model.SessionState
import net.thetrues.languagecards.repository.StatsRepository
import net.thetrues.languagecards.session.SessionFlow
import net.thetrues.languagecards.ui.screens.CardScreen
import net.thetrues.languagecards.ui.screens.StartScreen
import net.thetrues.languagecards.ui.screens.SummaryScreen
import net.thetrues.languagecards.ui.theme.LanguageCardsTheme

/**
 * Shared entry point for the Language Cards app.
 * [statsStore] is provided by the platform (Android DataStore, iOS DataStore).
 * [onExit] is invoked when the user taps Exit (e.g. activity.finish() on Android).
 */
@Composable
fun App(
    statsStore: StatsRepository,
    onExit: () -> Unit,
) {
    LanguageCardsTheme {
        var sessionState by remember { mutableStateOf<SessionState?>(null) }
        Scaffold(modifier = Modifier.fillMaxSize().safeContentPadding()) { innerPadding ->
            when {
                sessionState == null -> StartScreen(
                    decks = SampleData.decks,
                    onStart = { deck, direction ->
                        sessionState = SessionFlow.startSession(deck, direction)
                    },
                    onExit = onExit,
                    modifier = Modifier.padding(innerPadding),
                )
                sessionState!!.isAtSummary -> SummaryScreen(
                    state = sessionState!!,
                    onPracticeAgain = { sessionState = null },
                    onExit = onExit,
                    modifier = Modifier.padding(innerPadding),
                )
                else -> CardScreen(
                    state = sessionState!!,
                    onAnswer = { cardId, wasHit ->
                        statsStore.record(cardId, wasHit)
                        sessionState = SessionFlow.recordAnswer(sessionState!!, cardId, wasHit)
                    },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
