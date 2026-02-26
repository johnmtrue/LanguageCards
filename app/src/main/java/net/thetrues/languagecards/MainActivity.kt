package net.thetrues.languagecards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.SampleData
import net.thetrues.languagecards.model.SessionState
import net.thetrues.languagecards.model.StatsStore
import net.thetrues.languagecards.session.SessionFlow
import net.thetrues.languagecards.ui.theme.LanguageCardsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanguageCardsTheme {
                // Session state: null = show Start screen; non-null = in session (cards or summary).
                // Hit/miss values persist for the session (state.results + statsStore).
                var sessionState by remember { mutableStateOf<SessionState?>(null) }
                val statsStore = remember { StatsStore() }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        sessionState == null -> StartScreen(
                            deckName = SampleData.defaultDeck.name,
                            onStart = { sessionState = SessionFlow.startSession(SampleData.defaultDeck) },
                            modifier = Modifier.padding(innerPadding),
                        )
                        sessionState!!.isAtSummary -> SummaryScreen(
                            state = sessionState!!,
                            onPracticeAgain = {
                                sessionState = SessionFlow.startSession(SampleData.defaultDeck)
                            },
                            onExit = { finish() },
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
    }
}

@Composable
private fun StartScreen(
    deckName: String,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Language Cards",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = deckName, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStart) {
            Text("Start")
        }
    }
}

private enum class CardPhase {
    PROMPT,
    SHOWING_AFTER_SHOW_ANSWER,
    SHOWING_AFTER_KNOW,
}

@Composable
private fun CardScreen(
    state: SessionState,
    onAnswer: (cardId: String, wasHit: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val card = state.currentCard ?: return
    var phase by remember(card.id) { mutableStateOf(CardPhase.PROMPT) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (phase) {
            CardPhase.PROMPT -> {
                Text(text = card.sideA)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { phase = CardPhase.SHOWING_AFTER_KNOW }) {
                    Text("I know")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { phase = CardPhase.SHOWING_AFTER_SHOW_ANSWER }) {
                    Text("Show answer")
                }
            }
            CardPhase.SHOWING_AFTER_SHOW_ANSWER -> {
                Text(text = card.sideB)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onAnswer(card.id, false) }) {
                    Text("Continue")
                }
            }
            CardPhase.SHOWING_AFTER_KNOW -> {
                Text(text = card.sideB)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onAnswer(card.id, true) }) {
                    Text("I was right")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onAnswer(card.id, false) }) {
                    Text("I was wrong")
                }
            }
        }
    }
}

@Composable
private fun SummaryScreen(
    state: SessionState,
    onPracticeAgain: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val missedCards = state.cards.zip(state.results)
        .filter { (_, result) -> !result.wasHit }
        .map { (card, _) -> card }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Session complete",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text("Cards: ${state.cards.size}")
        Text("Hits: ${state.sessionHits}")
        Text("Misses: ${state.sessionMisses}")

        if (missedCards.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Missed words",
                style = MaterialTheme.typography.titleMedium,
            )
            for (card in missedCards) {
                Text(text = "${card.sideA} → ${card.sideB}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onPracticeAgain) {
            Text("Practice again")
        }
        Button(onClick = onExit) {
            Text("Exit")
        }
    }
}
