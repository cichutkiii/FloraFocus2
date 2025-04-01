package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.repository.UserPlant
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PlantsScreen(
    plants: List<Plant>,
    onAddPlantClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val userPlants by viewModel.userPlants.collectAsState(initial = emptyList())

    Column {
        Button(
            onClick = onAddPlantClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Dodaj Roślinę"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dodaj nową roślinę")
        }

        if (userPlants.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nie masz jeszcze żadnych roślin")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(userPlants) { plant ->
                    UserPlantItem(
                        plant = plant,
                        onRemove = { viewModel.removeUserPlant(plant) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserPlantItem(
    plant: UserPlant,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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
                    text = plant.name, // Upewnij się, że używasz plant.name
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Kroków pielęgnacyjnych: ${plant.careSteps.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń"
                )
            }
        }
    }
}