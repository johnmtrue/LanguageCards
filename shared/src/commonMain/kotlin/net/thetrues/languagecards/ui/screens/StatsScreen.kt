package net.thetrues.languagecards.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.thetrues.languagecards.model.CardStats

@Composable
fun StatsScreen(
    stats: List<CardStats>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalHits = stats.sumOf { it.hits }
    val totalMisses = stats.sumOf { it.misses }
    val totalAttempts = totalHits + totalMisses
    val accuracyPercent = if (totalAttempts > 0) (totalHits * 100.0 / totalAttempts) else 0.0

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
        Text("Cards practiced: ${stats.size}")
        Text("Total hits: $totalHits")
        Text("Total misses: $totalMisses")
        if (totalAttempts > 0) {
            Text("Accuracy: ${"%.1f".format(accuracyPercent)}%")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onDismiss) {
            Text("Back")
        }
    }
}
