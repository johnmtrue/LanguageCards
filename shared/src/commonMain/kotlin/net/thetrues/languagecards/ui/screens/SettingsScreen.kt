package net.thetrues.languagecards.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.thetrues.languagecards.model.GameMode
import net.thetrues.languagecards.model.LanguageCombination
import net.thetrues.languagecards.model.PracticeDirection
import net.thetrues.languagecards.model.PromptDisplay
import net.thetrues.languagecards.model.TextAnswerMode
import net.thetrues.languagecards.settings.AppSettings
import net.thetrues.languagecards.settings.SettingsStore

/**
 * Default practice options and TTS toggle. Persists via [settingsStore] on Save.
 * Only composed while the overlay is visible so [LaunchedEffect] reloads each time it opens.
 */
@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    languageCombination: LanguageCombination?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf(AppSettings.Default) }

    LaunchedEffect(Unit) {
        draft = withContext(Dispatchers.Default) { settingsStore.read() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Defaults for new practice sessions",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Game mode", style = MaterialTheme.typography.titleSmall)
        GameMode.entries.forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = draft.gameMode == mode,
                    onClick = { draft = draft.copy(gameMode = mode) },
                )
                Text(
                    when (mode) {
                        GameMode.GUESS -> "Guess (self-check)"
                        GameMode.TEXT_ANSWER -> "Type answer"
                        GameMode.AUDIO_ANSWER -> "Speak answer (type for now)"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Text("Prompt display", style = MaterialTheme.typography.titleSmall)
        Text(
            "TTS is not enabled yet; text is always shown until then.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RadioButton(
                selected = draft.promptDisplay == PromptDisplay.TEXT_AND_AUDIO,
                onClick = { draft = draft.copy(promptDisplay = PromptDisplay.TEXT_AND_AUDIO) },
            )
            Text("Text (optional TTS later)", style = MaterialTheme.typography.bodyMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RadioButton(
                selected = draft.promptDisplay == PromptDisplay.AUDIO_ONLY,
                onClick = { draft = draft.copy(promptDisplay = PromptDisplay.AUDIO_ONLY) },
            )
            Text("Audio-first when TTS is available", style = MaterialTheme.typography.bodyMedium)
        }

        Text("Answer matching", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RadioButton(
                selected = draft.textAnswerMode == TextAnswerMode.STRICT,
                onClick = { draft = draft.copy(textAnswerMode = TextAnswerMode.STRICT) },
            )
            Text("Strict (accents must match)", style = MaterialTheme.typography.bodyMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RadioButton(
                selected = draft.textAnswerMode == TextAnswerMode.NON_STRICT,
                onClick = { draft = draft.copy(textAnswerMode = TextAnswerMode.NON_STRICT) },
            )
            Text("Ignore accents", style = MaterialTheme.typography.bodyMedium)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Enable TTS when available", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = draft.ttsEnabled,
                onCheckedChange = { draft = draft.copy(ttsEnabled = it) },
            )
        }

        Text("Default practice direction", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RadioButton(
                selected = draft.defaultDirection == PracticeDirection.A_TO_B,
                onClick = { draft = draft.copy(defaultDirection = PracticeDirection.A_TO_B) },
            )
            Text(
                languageCombination?.let { "${it.sideAName} → ${it.sideBName}" } ?: "A → B",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RadioButton(
                selected = draft.defaultDirection == PracticeDirection.B_TO_A,
                onClick = { draft = draft.copy(defaultDirection = PracticeDirection.B_TO_A) },
            )
            Text(
                languageCombination?.let { "${it.sideBName} → ${it.sideAName}" } ?: "B → A",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.Default) { settingsStore.write(draft) }
                        onDismiss()
                    }
                },
            ) {
                Text("Save")
            }
        }
    }
}
