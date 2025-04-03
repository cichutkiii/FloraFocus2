package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.repository.PlantLocationEntity
import pl.preclaw.florafocus.data.repository.PlantPlacementEntity
import pl.preclaw.florafocus.ui.viewmodel.GardenViewModel
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailsContent(
    location: PlantLocationEntity,
    gardenViewModel: GardenViewModel,
    mainViewModel: MainViewModel
) {
    val plants by mainViewModel.allPlants.collectAsState()
    val userPlants by mainViewModel.userPlants.collectAsState(initial = emptyList())
    val placements by gardenViewModel.getPlacementsForLocation(location.id)
        .collectAsState(initial = emptyList())

    var showAddPlantDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Log dla debugowania
    LaunchedEffect(location.id) {
        println("LocationDetailsContent: Lokalizacja ID = ${location.id}")
        println("LocationDetailsContent: Liczba roślin w lokalizacji: ${placements.size}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Informacje o lokalizacji
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Szczegóły lokalizacji",
                    style = MaterialTheme.typography.titleMedium
                )

                if (location.description.isNotBlank()) {
                    Text(
                        text = location.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (location.lightConditions.isNotBlank()) {
                        AssistChip(
                            onClick = { },
                            label = { Text(location.lightConditions) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    if (location.soilType.isNotBlank()) {
                        AssistChip(
                            onClick = { },
                            label = { Text(location.soilType) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Grass,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                if (location.notes.isNotBlank()) {
                    Text(
                        text = "Notatki: ${location.notes}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Lista roślin w tej lokalizacji
        Text(
            text = "Rośliny w tej lokalizacji",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (placements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Brak roślin w tej lokalizacji")
            }
        } else {
            LazyColumn {
                items(placements) { placement ->
                    PlantPlacementItem(
                        placement = placement,
                        allPlants = plants,
                        onDelete = {
                            coroutineScope.launch {
                                // Usuń powiązanie w bazie danych
                                gardenViewModel.deletePlantPlacement(placement)

                                // Znajdź roślinę użytkownika powiązaną z tą lokalizacją i tym ID rośliny
                                val userPlantToDelete = userPlants.find {
                                    it.locationId == location.id && it.name == plants.find { p -> p.id == placement.plantId }?.commonName
                                }

                                // Jeśli znaleziono, usuń również roślinę użytkownika
                                userPlantToDelete?.let {
                                    mainViewModel.removeUserPlant(it)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialog dodawania rośliny
    if (showAddPlantDialog) {
        AddPlantToLocationDialog(
            plants = plants,
            locationId = location.id,
            onDismiss = { showAddPlantDialog = false },
            onPlantSelected = { plant, variety, quantity, notes, plantingDate ->
                // Dodaj powiązanie rośliny z lokalizacją
                gardenViewModel.addPlantPlacement(
                    PlantPlacementEntity(
                        plantId = plant.id ?: "",
                        locationId = location.id,
                        variety = variety,
                        quantity = quantity,
                        notes = notes,
                        plantingDate = plantingDate
                    )
                )
                showAddPlantDialog = false
            },
            mainViewModel = mainViewModel
        )
    }

    // Przycisk dodawania rośliny (floating action button)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showAddPlantDialog = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj roślinę")
        }
    }
}

@Composable
fun PlantPlacementItem(
    placement: PlantPlacementEntity,
    allPlants: List<Plant>,
    onDelete: () -> Unit
) {
    // Znajdź roślinę na podstawie ID (które jest nazwą rośliny w Firebase)
    val plant = allPlants.find { it.id == placement.plantId }

    // Wyznacz nazwę do wyświetlenia - jeśli nie znaleziono rośliny, użyj plantId jako nazwy
    val displayName = plant?.commonName?.takeIf { it.isNotEmpty() }
        ?: placement.plantId

    // Informacje debugowe
    LaunchedEffect(placement.plantId) {
        println("PlantPlacementItem: plantId=${placement.plantId}, znaleziona roślina=${plant?.commonName}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium
                )

                if (placement.variety.isNotBlank()) {
                    Text(
                        text = "Odmiana: ${placement.variety}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "Ilość: ${placement.quantity}",
                    style = MaterialTheme.typography.bodySmall
                )

                if (placement.plantingDate.isNotBlank()) {
                    Text(
                        text = "Data posadzenia: ${placement.plantingDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (placement.notes.isNotBlank()) {
                    Text(
                        text = "Notatki: ${placement.notes}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń"
                )
            }
        }
    }
}