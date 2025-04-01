package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.model.Plant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantSelectionScreen(
    availablePlants: List<Plant>,
    onPlantSelected: (Plant) -> Unit,
    onDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wybierz roślinę") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Powrót")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (availablePlants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(availablePlants) { plant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onPlantSelected(plant) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = plant.id.toString(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Kroków: ${plant.careSteps.size}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Dodaj"
                            )
                        }
                    }
                }
            }
        }
    }
}