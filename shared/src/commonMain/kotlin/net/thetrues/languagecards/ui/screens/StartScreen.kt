package net.thetrues.languagecards.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.GameMode
import net.thetrues.languagecards.model.LanguageCombination
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.PromptDisplay
import net.thetrues.languagecards.model.SessionOptions
import net.thetrues.languagecards.model.TextAnswerMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    languageCombinations: List<LanguageCombination>,
    selectedLanguageCombinationId: String?,
    onSelectLanguageCombination: (LanguageCombination) -> Unit,
    onStart: (Deck, PracticeDirection, SessionOptions) -> Unit,
    onExit: () -> Unit,
    defaultSessionOptions: SessionOptions = SessionOptions.Default,
    defaultPracticeDirection: PracticeDirection = PracticeDirection.A_TO_B,
    modifier: Modifier = Modifier,
) {
    if (languageCombinations.isEmpty()) {
        EmptyDecksScreen(onExit = onExit, modifier = modifier)
        return
    }

    val initialCombo = languageCombinations.firstOrNull { it.id == selectedLanguageCombinationId }
        ?: languageCombinations.firstOrNull()
        ?: return
    var selectedComboState by remember { mutableStateOf(initialCombo) }
    var selectedDeck by remember(selectedComboState) { mutableStateOf(selectedComboState.decks.firstOrNull()) }
    LaunchedEffect(languageCombinations) {
        val currentCombo = selectedComboState
        val comboInList = languageCombinations.find { it.id == currentCombo.id }
        if (comboInList == null) {
            val first = languageCombinations.firstOrNull() ?: return@LaunchedEffect
            selectedComboState = first
            selectedDeck = first.decks.firstOrNull()
            onSelectLanguageCombination(first)
        } else {
            selectedComboState = comboInList
            val currentDeck = selectedDeck
            val deckInList = comboInList.decks.find { it.id == currentDeck?.id }
            if (deckInList == null) {
                selectedDeck = comboInList.decks.firstOrNull()
            } else {
                selectedDeck = deckInList
            }
            onSelectLanguageCombination(comboInList)
        }
    }
    val combo = selectedComboState
    val deck = selectedDeck
    if (deck == null) {
        EmptyDecksScreen(onExit = onExit, modifier = modifier)
        return
    }

    var selectedDirection by remember { mutableStateOf(defaultPracticeDirection) }
    var gameMode by remember { mutableStateOf(defaultSessionOptions.gameMode) }
    var promptDisplay by remember { mutableStateOf(defaultSessionOptions.promptDisplay) }
    var textAnswerMode by remember { mutableStateOf(defaultSessionOptions.textAnswerMode) }

    LaunchedEffect(defaultSessionOptions, defaultPracticeDirection) {
        gameMode = defaultSessionOptions.gameMode
        promptDisplay = defaultSessionOptions.promptDisplay
        textAnswerMode = defaultSessionOptions.textAnswerMode
        selectedDirection = defaultPracticeDirection
    }

    var comboExpanded by remember { mutableStateOf(false) }
    var deckExpanded by remember { mutableStateOf(false) }

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
        ExposedDropdownMenuBox(
            expanded = comboExpanded,
            onExpandedChange = { comboExpanded = it },
        ) {
            OutlinedTextField(
                value = combo.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Language combination") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = comboExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = comboExpanded,
                onDismissRequest = { comboExpanded = false },
            ) {
                languageCombinations.forEach { combination ->
                    DropdownMenuItem(
                        text = { Text(combination.name) },
                        onClick = {
                            selectedComboState = combination
                            onSelectLanguageCombination(combination)
                            comboExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = deckExpanded,
            onExpandedChange = { deckExpanded = it },
        ) {
            OutlinedTextField(
                value = deck.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Deck") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deckExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = deckExpanded,
                onDismissRequest = { deckExpanded = false },
            ) {
                combo.decks.forEach { d ->
                    DropdownMenuItem(
                        text = { Text(d.name) },
                        onClick = {
                            selectedDeck = d
                            deckExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Game mode",
            style = MaterialTheme.typography.titleSmall,
        )
        GameModeRadioRow(
            selected = gameMode,
            onSelect = { gameMode = it },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Prompt display",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "TTS is not enabled yet; text is always shown until then.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PromptDisplayRadioRow(
            selected = promptDisplay,
            onSelect = { promptDisplay = it },
        )
        if (gameMode == GameMode.TEXT_ANSWER || gameMode == GameMode.AUDIO_ANSWER) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Answer matching",
                style = MaterialTheme.typography.titleSmall,
            )
            TextAnswerModeRadioRow(
                selected = textAnswerMode,
                onSelect = { textAnswerMode = it },
            )
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
                text = "${combo.sideAName} → ${combo.sideBName}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 16.dp),
            )
            RadioButton(
                selected = selectedDirection == PracticeDirection.B_TO_A,
                onClick = { selectedDirection = PracticeDirection.B_TO_A },
            )
            Text(
                text = "${combo.sideBName} → ${combo.sideAName}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                onStart(
                    deck,
                    selectedDirection,
                    SessionOptions(
                        gameMode = gameMode,
                        promptDisplay = promptDisplay,
                        textAnswerMode = textAnswerMode,
                    ),
                )
            },
        ) {
            Text("Start")
        }
        Button(onClick = onExit) {
            Text("Exit")
        }
    }
}

@Composable
private fun GameModeRadioRow(
    selected: GameMode,
    onSelect: (GameMode) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GameMode.entries.forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = selected == mode,
                    onClick = { onSelect(mode) },
                )
                Text(
                    text = when (mode) {
                        GameMode.GUESS -> "Guess (self-check)"
                        GameMode.TEXT_ANSWER -> "Type answer"
                        GameMode.AUDIO_ANSWER -> "Speak answer (type for now)"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun PromptDisplayRadioRow(
    selected: PromptDisplay,
    onSelect: (PromptDisplay) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            RadioButton(
                selected = selected == PromptDisplay.TEXT_AND_AUDIO,
                onClick = { onSelect(PromptDisplay.TEXT_AND_AUDIO) },
            )
            Text(
                text = "Text (optional TTS later)",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            RadioButton(
                selected = selected == PromptDisplay.AUDIO_ONLY,
                onClick = { onSelect(PromptDisplay.AUDIO_ONLY) },
            )
            Text(
                text = "Audio-first when TTS is available",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun TextAnswerModeRadioRow(
    selected: TextAnswerMode,
    onSelect: (TextAnswerMode) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            RadioButton(
                selected = selected == TextAnswerMode.STRICT,
                onClick = { onSelect(TextAnswerMode.STRICT) },
            )
            Text(
                text = "Strict (accents must match)",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            RadioButton(
                selected = selected == TextAnswerMode.NON_STRICT,
                onClick = { onSelect(TextAnswerMode.NON_STRICT) },
            )
            Text(
                text = "Ignore accents",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun EmptyDecksScreen(
    onExit: () -> Unit,
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
            text = "No decks",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Import a deck file to get started, or add decks from the menu.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onExit) {
            Text("Exit")
        }
    }
}
