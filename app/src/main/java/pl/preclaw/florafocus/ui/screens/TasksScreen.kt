package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.model.CareStep
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel

@Composable
fun TasksScreen(viewModel: MainViewModel) {
    val tasks by viewModel.upcomingTasks.collectAsState()

    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Brak nadchodzących zadań",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(tasks) { (plant, careStep) ->
                TaskItem(plant, careStep)
            }
        }
    }
}

@Composable
fun TaskItem(plant: Plant, careStep: CareStep) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = plant.commonName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = careStep.task,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Termin: ${careStep.dateRange.start} - ${careStep.dateRange.end}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}