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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.thetrues.languagecards.model.CardLine
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.SessionState

private enum class CardPhase {
    PROMPT,
    SHOWING_AFTER_SHOW_ANSWER,
    SHOWING_AFTER_KNOW,
}

@Composable
fun CardScreen(
    state: SessionState,
    onAnswer: (cardId: String, wasHit: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val card = state.currentCard ?: return
    var phase by remember(card.id) { mutableStateOf(CardPhase.PROMPT) }
    val contextLines = card.lines.dropLast(1)
    val (prompt, answer) = when (state.direction) {
        PracticeDirection.A_TO_B -> card.sideA to card.sideB.joinToString(" / ")
        PracticeDirection.B_TO_A -> remember(card.id) { card.sideB.random() } to card.sideA
    }
    val answerSideText: (CardLine) -> String = when (state.direction) {
        PracticeDirection.A_TO_B -> { line -> line.sideB.joinToString(" / ") }
        PracticeDirection.B_TO_A -> { line -> line.sideA }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = if (contextLines.isEmpty()) Arrangement.Center else Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (line in contextLines) {
            Text(
                text = answerSideText(line),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (contextLines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
        }
        when (phase) {
            CardPhase.PROMPT -> {
                Text(
                    text = prompt,
                    style = if (contextLines.isNotEmpty()) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                )
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

