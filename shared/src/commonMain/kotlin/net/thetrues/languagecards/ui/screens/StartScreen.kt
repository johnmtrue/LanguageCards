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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.thetrues.languagecards.model.Deck
import net.thetrues.languagecards.model.LanguageCombination
import net.thetrues.languagecards.model.PracticeDirection
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    languageCombinations: List<LanguageCombination>,
    onStart: (Deck, PracticeDirection) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedCombo = languageCombinations.firstOrNull() ?: return
    var selectedComboState by remember { mutableStateOf(selectedCombo) }
    val combo = selectedComboState
    var selectedDeck by remember(combo) { mutableStateOf(combo.decks.firstOrNull()) }
    val deck = selectedDeck ?: return
    var selectedDirection by remember { mutableStateOf(PracticeDirection.A_TO_B) }

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
        Button(onClick = { onStart(deck, selectedDirection) }) {
            Text("Start")
        }
        Button(onClick = onExit) {
            Text("Exit")
        }
    }
}
