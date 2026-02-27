package net.thetrues.languagecards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.PracticeDirection
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
                            decks = SampleData.decks,
                            onStart = { deck, direction ->
                                sessionState = SessionFlow.startSession(deck, direction)
                            },
                            modifier = Modifier.padding(innerPadding),
                        )
                        sessionState!!.isAtSummary -> SummaryScreen(
                            state = sessionState!!,
                            onPracticeAgain = { sessionState = null },
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
    decks: List<Deck>,
    onStart: (Deck, PracticeDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedDeck by remember { mutableStateOf(decks.first()) }
    var selectedDirection by remember { mutableStateOf(PracticeDirection.A_TO_B) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Language Cards",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Deck",
            style = MaterialTheme.typography.titleSmall,
        )
        decks.forEach { deck ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                RadioButton(
                    selected = selectedDeck.id == deck.id,
                    onClick = { selectedDeck = deck },
                )
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Practice direction",
            style = MaterialTheme.typography.titleSmall,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            RadioButton(
                selected = selectedDirection == PracticeDirection.A_TO_B,
                onClick = { selectedDirection = PracticeDirection.A_TO_B },
            )
            Text(
                text = "English → French",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 16.dp),
            )
            RadioButton(
                selected = selectedDirection == PracticeDirection.B_TO_A,
                onClick = { selectedDirection = PracticeDirection.B_TO_A },
            )
            Text(
                text = "French → English",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onStart(selectedDeck, selectedDirection) }) {
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
    val (prompt, answer) = when (state.direction) {
        PracticeDirection.A_TO_B -> card.sideA to card.sideB.joinToString(" / ")
        PracticeDirection.B_TO_A -> remember(card.id) { card.sideB.random() } to card.sideA
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (phase) {
            CardPhase.PROMPT -> {
                Text(text = prompt)
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
                Text(text = answer)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onAnswer(card.id, false) }) {
                    Text("Continue")
                }
            }
            CardPhase.SHOWING_AFTER_KNOW -> {
                Text(text = answer)
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
    val (promptSide, answerSide) = when (state.direction) {
        PracticeDirection.A_TO_B -> (
            { c: Card -> c.sideA } to
            { c: Card -> c.sideB.joinToString(" / ") }
        )
        PracticeDirection.B_TO_A -> (
            { c: Card -> c.sideB.first() } to
            { c: Card -> c.sideA }
        )
    }

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
                Text(text = "${promptSide(card)} → ${answerSide(card)}")
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
