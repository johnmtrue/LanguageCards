package net.thetrues.languagecards.ui.screens

import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.thetrues.languagecards.model.CardStats
import net.thetrues.languagecards.model.LanguageCombination
import net.thetrues.languagecards.model.PracticeDirection

@Composable
fun StatsScreen(
    stats: List<CardStats>,
    languageCombination: LanguageCombination?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalHits = stats.sumOf { it.hits }
    val totalMisses = stats.sumOf { it.misses }
    val totalAttempts = totalHits + totalMisses
    val accuracyPercent = if (totalAttempts > 0) (totalHits * 100.0 / totalAttempts) else 0.0

    val statsByDirection = stats.groupBy { it.direction }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Overall stats",
            style = MaterialTheme.typography.headlineMedium,
        )
        languageCombination?.let { combo ->
            Text("Language combination: ${combo.name}")
        }
        Text("Card+directions practiced: ${stats.size}")
        Text("Total hits: $totalHits")
        Text("Total misses: $totalMisses")
        if (totalAttempts > 0) {
            Text("Accuracy: ${(accuracyPercent * 10).roundToInt() / 10.0}%")
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        if (stats.isNotEmpty()) {
            Text(
                text = "By direction",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        PracticeDirection.entries.forEach { direction ->
            val dirStats = statsByDirection[direction].orEmpty()
            val dirHits = dirStats.sumOf { it.hits }
            val dirMisses = dirStats.sumOf { it.misses }
            val dirAttempts = dirHits + dirMisses
            val dirAccuracy = if (dirAttempts > 0) (dirHits * 100.0 / dirAttempts) else 0.0
            val label = when (direction) {
                PracticeDirection.A_TO_B -> languageCombination?.let { "${it.sideAName} → ${it.sideBName}" } ?: "A → B"
                PracticeDirection.B_TO_A -> languageCombination?.let { "${it.sideBName} → ${it.sideAName}" } ?: "B → A"
            }
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = label, style = MaterialTheme.typography.labelLarge)
                Text("${dirStats.size} card+direction(s) · $dirHits hits, $dirMisses misses")
                if (dirAttempts > 0) {
                    Text("Accuracy: ${(dirAccuracy * 10).roundToInt() / 10.0}%")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onDismiss) {
            Text("Back")
        }
    }
}
