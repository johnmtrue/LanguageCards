package net.thetrues.languagecards.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import net.thetrues.languagecards.answer.AnswerMatcher
import net.thetrues.languagecards.model.Card
import net.thetrues.languagecards.model.CardLine
import net.thetrues.languagecards.model.GameMode
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.PromptDisplay
import net.thetrues.languagecards.model.SessionState

private enum class GuessPhase {
    PROMPT,
    SHOWING_AFTER_SHOW_ANSWER,
    SHOWING_AFTER_KNOW,
}

private enum class TypedAnswerPhase {
    ENTRY,
    FEEDBACK_CORRECT,
    FEEDBACK_WRONG,
    SHOW_ANSWER_SURRENDER,
}

@Composable
fun CardScreen(
    state: SessionState,
    onAnswer: (cardId: String, wasHit: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val card = state.currentCard ?: return
    when (state.options.gameMode) {
        GameMode.GUESS -> GuessCardScreen(
            state = state,
            card = card,
            onAnswer = onAnswer,
            modifier = modifier,
        )
        GameMode.TEXT_ANSWER,
        GameMode.AUDIO_ANSWER,
        -> TypedAnswerCardScreen(
            state = state,
            card = card,
            onAnswer = onAnswer,
            modifier = modifier,
        )
    }
}

@Composable
private fun GuessCardScreen(
    state: SessionState,
    card: Card,
    onAnswer: (cardId: String, wasHit: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var phase by remember(card.id) { mutableStateOf(GuessPhase.PROMPT) }
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
        PromptCaption(state.options.promptDisplay)
        Text(
            text = prompt,
            style = if (contextLines.isNotEmpty()) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(24.dp))
        when (phase) {
            GuessPhase.PROMPT -> {
                Button(onClick = { phase = GuessPhase.SHOWING_AFTER_KNOW }) {
                    Text("I know")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { phase = GuessPhase.SHOWING_AFTER_SHOW_ANSWER }) {
                    Text("Show answer")
                }
            }
            GuessPhase.SHOWING_AFTER_SHOW_ANSWER -> {
                Text(text = answer)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onAnswer(card.id, false) }) {
                    Text("Continue")
                }
            }
            GuessPhase.SHOWING_AFTER_KNOW -> {
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
private fun TypedAnswerCardScreen(
    state: SessionState,
    card: Card,
    onAnswer: (cardId: String, wasHit: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var phase by remember(card.id) { mutableStateOf(TypedAnswerPhase.ENTRY) }
    var userInput by remember(card.id) { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    val contextLines = card.lines.dropLast(1)
    val (prompt, answerDisplay) = when (state.direction) {
        PracticeDirection.A_TO_B -> card.sideA to card.sideB.joinToString(" / ")
        PracticeDirection.B_TO_A -> remember(card.id) { card.sideB.random() } to card.sideA
    }
    val acceptableAnswers = when (state.direction) {
        PracticeDirection.A_TO_B -> card.sideB
        PracticeDirection.B_TO_A -> listOf(card.sideA)
    }
    val answerSideText: (CardLine) -> String = when (state.direction) {
        PracticeDirection.A_TO_B -> { line -> line.sideB.joinToString(" / ") }
        PracticeDirection.B_TO_A -> { line -> line.sideA }
    }

    val modeLabel = when (state.options.gameMode) {
        GameMode.TEXT_ANSWER -> null
        GameMode.AUDIO_ANSWER -> "Voice input will be added in a future update. Type your answer for now."
        else -> null
    }

    fun submitAnswer() {
        keyboard?.hide()
        if (AnswerMatcher.matches(userInput, acceptableAnswers, state.options.textAnswerMode)) {
            phase = TypedAnswerPhase.FEEDBACK_CORRECT
        } else {
            phase = TypedAnswerPhase.FEEDBACK_WRONG
        }
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
        PromptCaption(state.options.promptDisplay)
        Text(
            text = prompt,
            style = if (contextLines.isNotEmpty()) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        )
        modeLabel?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        when (phase) {
            TypedAnswerPhase.ENTRY -> {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Your answer") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { submitAnswer() },
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { submitAnswer() },
                    enabled = userInput.isNotBlank(),
                ) {
                    Text("Check answer")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { phase = TypedAnswerPhase.SHOW_ANSWER_SURRENDER }) {
                    Text("Show answer")
                }
            }
            TypedAnswerPhase.FEEDBACK_CORRECT -> {
                Text(
                    text = "Correct",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = answerDisplay)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onAnswer(card.id, true) }) {
                    Text("Continue")
                }
            }
            TypedAnswerPhase.FEEDBACK_WRONG -> {
                Text(
                    text = "Incorrect",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Correct answer: $answerDisplay")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onAnswer(card.id, false) }) {
                    Text("Continue")
                }
            }
            TypedAnswerPhase.SHOW_ANSWER_SURRENDER -> {
                Text(text = answerDisplay)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onAnswer(card.id, false) }) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun PromptCaption(display: PromptDisplay) {
    if (display == PromptDisplay.AUDIO_ONLY) {
        Text(
            text = "Audio-only prompts will use TTS when available; text is shown for now.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
