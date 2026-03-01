package net.thetrues.languagecards.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.PracticeDirection

@Composable
fun StartScreen(
    decks: List<Deck>,
    onStart: (Deck, PracticeDirection) -> Unit,
    onExit: () -> Unit,
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
        Button(onClick = onExit) {
            Text("Exit")
        }
    }
}
