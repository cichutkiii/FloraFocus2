package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.repository.UserPlant
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.preclaw.florafocus.data.repository.SpaceWithAreas
import pl.preclaw.florafocus.ui.viewmodel.GardenViewModel

@Composable
fun PlantsScreen(
    plants: List<Plant>,
    viewModel: MainViewModel = viewModel(),
    gardenViewModel: GardenViewModel = viewModel()
) {
    val userPlants by viewModel.userPlants.collectAsState(initial = emptyList())
    val allSpaces by gardenViewModel.allSpaces.collectAsState(initial = emptyList())

    Column {
        if (userPlants.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Nie masz jeszcze żadnych roślin")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Przejdź do zakładki 'Miejsca', aby dodać roślinę do konkretnej lokalizacji",
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(userPlants) { plant ->
                    UserPlantItem(
                        plant = plant,
                        locationName = getLocationName(plant.locationId, allSpaces),
                        onRemove = { viewModel.removeUserPlant(plant) }
                    )
                }
            }
        }
    }
}

// Funkcja pomocnicza do pobrania nazwy lokalizacji
private fun getLocationName(locationId: String, spaces: List<SpaceWithAreas>): String {
    spaces.forEach { space ->
        space.areas.forEach { area ->
            area.locations.forEach { location ->
                if (location.id == locationId) {
                    return "${space.space.name} > ${area.area.name} > ${location.name}"
                }
            }
        }
    }
    return "Nieznana lokalizacja"
}

@Composable
fun UserPlantItem(
    plant: UserPlant,
    locationName: String,
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
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Lokalizacja: $locationName",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Kroków pielęgnacyjnych: ${plant.careSteps.size}",
                    style = MaterialTheme.typography.bodySmall
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