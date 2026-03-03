package net.thetrues.languagecards.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardLine
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.SessionState

@Composable
fun SummaryScreen(
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
                MissedCardDisplay(
                    card = card,
                    direction = state.direction,
                    promptSide = promptSide,
                    answerSide = answerSide,
                )
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

@Composable
private fun MissedCardDisplay(
    card: Card,
    direction: PracticeDirection,
    promptSide: (Card) -> String,
    answerSide: (Card) -> String,
) {
    val contextLines = card.lines.dropLast(1)
    val answerSideText: (CardLine) -> String = when (direction) {
        PracticeDirection.A_TO_B -> { line -> line.sideB.joinToString(" / ") }
        PracticeDirection.B_TO_A -> { line -> line.sideA }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (line in contextLines) {
            Text(
                text = answerSideText(line),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(text = "${promptSide(card)} → ${answerSide(card)}")
    }
}
