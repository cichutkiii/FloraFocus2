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
import pl.preclaw.florafocus.data.repository.UserPlant

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

    // Zawartość kart informacyjnych i układu ekranu - bez zmian
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Karta informacyjna lokalizacji - bez zmian
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Zawartość karty - bez zmian
            // ...
        }

        // Lista roślin - bez zmian
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
                        userPlants = userPlants,
                        mainViewModel = mainViewModel,
                        onDelete = {
                            coroutineScope.launch {
                                // Usuń powiązanie w bazie danych
                                gardenViewModel.deletePlantPlacement(placement)

                                // Znajdź roślinę użytkownika powiązaną z tą lokalizacją i tym ID rośliny
                                val userPlantToDelete = userPlants.find {
                                    it.locationId == location.id &&
                                            (it.plantId == placement.plantId ||
                                                    it.name == plants.find { p -> p.id == placement.plantId }?.commonName)
                                }

                                // Jeśli znaleziono, usuń również roślinę użytkownika
                                userPlantToDelete?.let {
                                    mainViewModel.removeUserPlant(it)
                                    println("Usunięto również roślinę użytkownika: ${it.name}")
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialog dodawania rośliny - TUTAJ JEST ZMIANA
    if (showAddPlantDialog) {
        AddPlantToLocationDialog(
            plants = plants,
            locationId = location.id,
            onDismiss = { showAddPlantDialog = false },
            onPlantSelected = { plant, variety, quantity, notes, plantingDate ->
                // Dodaj roślinę do kolekcji użytkownika
                mainViewModel.addUserPlantToLocation(plant, location.id)

                // TYLKO JEDEN RAZ wywołaj dodawanie - dodaj powiązanie rośliny z lokalizacją
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
    userPlants: List<UserPlant>,  // Dodany parametr
    mainViewModel: MainViewModel, // Dodany parametr
    onDelete: () -> Unit
) {
    // Znajdź roślinę na podstawie ID (które jest nazwą rośliny w Firebase)
    val plant = allPlants.find { it.id == placement.plantId }

    // Wyznacz nazwę do wyświetlenia - jeśli nie znaleziono rośliny, użyj plantId jako nazwy
    val displayName = plant?.commonName?.takeIf { it.isNotEmpty() }
        ?: placement.plantId

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