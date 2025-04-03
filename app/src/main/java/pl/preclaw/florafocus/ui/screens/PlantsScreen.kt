package pl.preclaw.florafocus.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.repository.UserPlant
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.preclaw.florafocus.data.repository.PlantLocationEntity
import pl.preclaw.florafocus.data.repository.PlantPlacementEntity
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
    val allPlants by viewModel.allPlants.collectAsState()

    var showPlantSelectionDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<PlantLocationEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPlantSelectionDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj roślinę")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
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

        // Dialog wyboru rośliny
        if (selectedLocation != null) {
            PlantSelectionDialog(
                plants = allPlants,
                location = selectedLocation!!,
                onDismiss = {
                    selectedLocation = null
                    showPlantSelectionDialog = false
                },
                onPlantSelected = { selectedPlant ->
                    // Dodaj roślinę do konkretnej lokalizacji
                    viewModel.addUserPlantToLocation(selectedPlant, selectedLocation!!.id)

                    // Dodaj wpis do PlantPlacement
                    gardenViewModel.addPlantPlacement(
                        PlantPlacementEntity(
                            plantId = selectedPlant.id ?: "",
                            locationId = selectedLocation!!.id
                        )
                    )

                    selectedLocation = null
                    showPlantSelectionDialog = false
                }
            )
        } else if (showPlantSelectionDialog) {
            // Dialog wyboru lokalizacji
            LocationSelectionDialog(
                spaces = allSpaces,
                onLocationSelected = { location ->
                    selectedLocation = location
                },
                onDismiss = { showPlantSelectionDialog = false }
            )
        }
    }
}


@Composable
fun LocationSelectionDialog(
    spaces: List<SpaceWithAreas>,
    onLocationSelected: (PlantLocationEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wybierz lokalizację") },
        text = {
            LazyColumn {
                spaces.forEach { space ->
                    item {
                        Text(
                            text = space.space.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(space.areas) { area ->
                        area.locations.forEach { location ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLocationSelected(location) }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "${area.area.name} - ${location.name}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

// Dodaj nowy composable dla dialogu wyboru rośliny

@Composable
fun PlantSelectionDialog(
    plants: List<Plant>,
    location: PlantLocationEntity,
    onDismiss: () -> Unit,
    onPlantSelected: (Plant) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Wybierz roślinę dla lokalizacji:\n${location.name}")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Szukaj roślin") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Szukaj") },
                    modifier = Modifier.fillMaxWidth()
                )

                val filteredPlants = plants.filter {
                    it.id?.contains(searchQuery, ignoreCase = true) ?: false
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(filteredPlants) { plant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlantSelected(plant) }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = plant.id.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
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