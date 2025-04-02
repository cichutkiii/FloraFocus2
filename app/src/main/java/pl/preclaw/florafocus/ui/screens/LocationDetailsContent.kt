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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailsContent(
    location: PlantLocationEntity,
    gardenViewModel: GardenViewModel,
    mainViewModel: MainViewModel
) {
    val plants by mainViewModel.allPlants.collectAsState()
    val placements by gardenViewModel.getPlacementsForLocation(location.id)
        .collectAsState(initial = emptyList())

    var showAddPlantDialog by remember { mutableStateOf(false) }

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
                        onDelete = { gardenViewModel.deletePlantPlacement(placement) }
                    )
                }
            }
        }
    }

    // Dialog dodawania rośliny
    if (showAddPlantDialog) {
        AddPlantToLocationDialog(
            plants = plants,
            locationId = location.id, // Przekazanie id lokalizacji
            onDismiss = { showAddPlantDialog = false },
            onPlantSelected = { plant, variety, quantity, notes, plantingDate ->
                // Teraz dodawanie do gardenViewModel i mainViewModel odbywa się jednocześnie
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
            mainViewModel = mainViewModel // Przekazanie viewModelu
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
//
//@Composable
//fun PlantPlacementItem(
//    placement: PlantPlacementEntity,
//    allPlants: List<Plant>,
//    onDelete: () -> Unit
//) {
//    // Znajdź roślinę na podstawie ID
//    val plant = allPlants.find { it.id == placement.plantId }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(
//                    text = plant?.commonName ?: "Nieznana roślina",
//                    style = MaterialTheme.typography.titleMedium
//                )
//
//                if (placement.variety.isNotBlank()) {
//                    Text(
//                        text = "Odmiana: ${placement.variety}",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//
//                Text(
//                    text = "Ilość: ${placement.quantity}",
//                    style = MaterialTheme.typography.bodySmall
//                )
//
//                if (placement.plantingDate.isNotBlank()) {
//                    Text(
//                        text = "Data posadzenia: ${placement.plantingDate}",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//
//                if (placement.notes.isNotBlank()) {
//                    Text(
//                        text = "Notatki: ${placement.notes}",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//            }
//
//            IconButton(onClick = onDelete) {
//                Icon(
//                    imageVector = Icons.Default.Delete,
//                    contentDescription = "Usuń"
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun AddPlantToLocationDialog(
//    plants: List<Plant>,
//    onDismiss: () -> Unit,
//    onPlantSelected: (Plant, String, Int, String, String) -> Unit
//) {
//    var selectedPlant by remember { mutableStateOf<Plant?>(null) }
//    var variety by remember { mutableStateOf("") }
//    var quantity by remember { mutableStateOf("1") }
//    var notes by remember { mutableStateOf("") }
//    var plantingDate by remember { mutableStateOf("") }
//
//    var showPlantSelector by remember { mutableStateOf(true) }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(if (showPlantSelector) "Wybierz roślinę" else "Dodaj szczegóły") },
//        text = {
//            Column(
//                modifier = Modifier.verticalScroll(rememberScrollState())
//            ) {
//                if (showPlantSelector) {
//                    // Lista roślin do wyboru
//                    var searchQuery by remember { mutableStateOf("") }
//
//                    OutlinedTextField(
//                        value = searchQuery,
//                        onValueChange = { searchQuery = it },
//                        label = { Text("Szukaj") },
//                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 8.dp)
//                    )
//
//                    val filteredPlants = plants.filter {
//                        it.commonName.contains(searchQuery, ignoreCase = true)
//                    }
//
//                    if (filteredPlants.isEmpty()) {
//                        Text("Brak pasujących roślin")
//                    } else {
//                        LazyColumn(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(300.dp)
//                        ) {
//                            items(filteredPlants) { plant ->
//                                ListItem(
//                                    headlineContent = { Text(plant.commonName) },
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .clickable {
//                                            selectedPlant = plant
//                                            showPlantSelector = false
//                                        }
//                                )
//                                HorizontalDivider()
//                            }
//                        }
//                    }
//                } else {
//                    // Formularz szczegółów
//                    selectedPlant?.let { plant ->
//                        Text(
//                            text = "Wybrana roślina: ${plant.commonName}",
//                            style = MaterialTheme.typography.titleMedium,
//                            modifier = Modifier.padding(bottom = 16.dp)
//                        )
//                    }
//
//                    OutlinedTextField(
//                        value = variety,
//                        onValueChange = { variety = it },
//                        label = { Text("Odmiana (opcjonalnie)") },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp)
//                    )
//
//                    OutlinedTextField(
//                        value = quantity,
//                        onValueChange = {
//                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
//                                quantity = it
//                            }
//                        },
//                        label = { Text("Ilość") },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp)
//                    )
//
//                    OutlinedTextField(
//                        value = plantingDate,
//                        onValueChange = { plantingDate = it },
//                        label = { Text("Data posadzenia (DD-MM-RRRR)") },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp)
//                    )
//
//                    OutlinedTextField(
//                        value = notes,
//                        onValueChange = { notes = it },
//                        label = { Text("Notatki (opcjonalnie)") },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp)
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            if (showPlantSelector) {
//                Button(
//                    onClick = { onDismiss() },
//                    enabled = true
//                ) {
//                    Text("Anuluj")
//                }
//            } else {
//                Row {
//                    TextButton(
//                        onClick = { showPlantSelector = true }
//                    ) {
//                        Text("Wstecz")
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Button(
//                        onClick = {
//                            selectedPlant?.let { plant ->
//                                val quantityInt = quantity.toIntOrNull() ?: 1
//                                onPlantSelected(
//                                    plant,
//                                    variety,
//                                    quantityInt,
//                                    notes,
//                                    plantingDate
//                                )
//                            }
//                        },
//                        enabled = selectedPlant != null
//                    ) {
//                        Text("Dodaj")
//                    }
//                }
//            }
//        },
//        dismissButton = {
//            if (showPlantSelector) {
//                // Nic tutaj, bo mamy tylko jeden przycisk w tym widoku
//            } else {
//                TextButton(
//                    onClick = { onDismiss() }
//                ) {
//                    Text("Anuluj")
//                }
//            }
//        }
//    )
//}