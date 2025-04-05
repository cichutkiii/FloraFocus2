package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.repository.UserPlant
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.preclaw.florafocus.data.repository.PlantLocationEntity
import pl.preclaw.florafocus.data.repository.PlantPlacementEntity
import pl.preclaw.florafocus.data.repository.SpaceWithAreas
import pl.preclaw.florafocus.ui.viewmodel.GardenViewModel
import pl.preclaw.florafocus.utils.PlantCompatibilityChecker

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
    var selectedPlant by remember { mutableStateOf<Plant?>(null) }
    var showLocationSelectionDialog by remember { mutableStateOf(false) }
    var showPlantDetailsDialog by remember { mutableStateOf(false) }
    var showCompatibilityDialog by remember { mutableStateOf(false) }

    // Stan dla przechowywania szczegółów rośliny
    var plantVariety by remember { mutableStateOf("") }
    var plantQuantity by remember { mutableStateOf(1) }
    var plantNotes by remember { mutableStateOf("") }
    var plantingDate by remember { mutableStateOf("") }

    // Stan dla przechowania wybranej lokalizacji
    var selectedLocation by remember { mutableStateOf<PlantLocationEntity?>(null) }

    // Stany dla informacji o kompatybilności
    var compatiblePlants by remember { mutableStateOf<List<UserPlant>>(emptyList()) }
    var incompatiblePlants by remember { mutableStateOf<List<UserPlant>>(emptyList()) }

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
                            "Dodaj pierwszą roślinę klikając przycisk + poniżej",
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

        // Dialog wyboru rośliny z oznaczeniami kompatybilności
        if (showPlantSelectionDialog) {
            PlantSelectionWithCompatibilityDialog(
                plants = allPlants,
                userPlants = userPlants,
                onDismiss = { showPlantSelectionDialog = false },
                onPlantSelected = { plant, compatible, incompatible ->
                    selectedPlant = plant
                    compatiblePlants = compatible
                    incompatiblePlants = incompatible
                    showPlantSelectionDialog = false

                    // Jeśli są niekompatybilne rośliny, pokaż ostrzeżenie
                    if (incompatible.isNotEmpty()) {
                        showCompatibilityDialog = true
                    } else {
                        // Jeśli nie ma niekompatybilnych roślin, przejdź do szczegółów
                        showPlantDetailsDialog = true
                    }
                }
            )
        }

        // Dialog kompatybilności
        if (showCompatibilityDialog && selectedPlant != null) {
            PlantCompatibilityDialog(
                plant = selectedPlant!!,
                compatiblePlants = compatiblePlants,
                incompatiblePlants = incompatiblePlants,
                onDismiss = {
                    showCompatibilityDialog = false
                    selectedPlant = null // Anuluj wybór rośliny
                },
                onConfirm = {
                    showCompatibilityDialog = false
                    showPlantDetailsDialog = true // Kontynuuj mimo ostrzeżenia
                }
            )
        }

        // Dialog z szczegółami rośliny
        if (showPlantDetailsDialog && selectedPlant != null) {
            PlantDetailsDialog(
                plant = selectedPlant!!,
                onDismiss = {
                    showPlantDetailsDialog = false
                    selectedPlant = null
                },
                onConfirm = { variety, quantity, notes, date ->
                    // Zapisz szczegóły do dalszego przetwarzania
                    plantVariety = variety
                    plantQuantity = quantity
                    plantNotes = notes
                    plantingDate = date

                    // Pytamy użytkownika czy chce dodać roślinę do konkretnej lokalizacji
                    showLocationSelectionDialog = true
                    showPlantDetailsDialog = false
                }
            )
        }

        // Dialog wyboru lokalizacji
        if (showLocationSelectionDialog && selectedPlant != null) {
            LocationSelectionDialog(
                spaces = allSpaces,
                onLocationSelected = { location ->
                    selectedLocation = location
                    showLocationSelectionDialog = false

                    // Po wybraniu lokalizacji, dodaj roślinę z szczegółami
                    addPlantToLocation(
                        selectedPlant!!,
                        location,
                        gardenViewModel,
                        viewModel,
                        plantVariety,
                        plantQuantity,
                        plantNotes,
                        plantingDate
                    )
                    selectedPlant = null
                },
                onDismiss = {
                    // Jeśli anulowano wybór lokalizacji, dodaj roślinę bez lokalizacji
                    viewModel.addUserPlant(
                        selectedPlant!!,
                        plantVariety,
                        plantQuantity,
                        plantNotes,
                        plantingDate
                    )
                    showLocationSelectionDialog = false
                    selectedPlant = null
                }
            )
        }
    }
}

@Composable
fun PlantSelectionWithCompatibilityDialog(
    plants: List<Plant>,
    userPlants: List<UserPlant>,
    onDismiss: () -> Unit,
    onPlantSelected: (Plant, List<UserPlant>, List<UserPlant>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wybierz roślinę") },
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
                    val displayName = if (it.commonName.isNotEmpty()) it.commonName else it.id ?: ""
                    displayName.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(filteredPlants) { plant ->
                        val displayName = if (plant.commonName.isNotEmpty()) {
                            plant.commonName
                        } else {
                            plant.id ?: "Nieznana roślina"
                        }

                        // Sprawdź kompatybilność
                        val (compatible, incompatible) = PlantCompatibilityChecker.checkCompatibility(
                            plant, userPlants
                        )

                        // Określ kolorowanie elementu listy na podstawie kompatybilności
                        val itemColor = when {
                            incompatible.isNotEmpty() -> MaterialTheme.colorScheme.error
                            compatible.isNotEmpty() -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        // Określ ikony dla kompatybilności
                        val compatibilityIcon = when {
                            incompatible.isNotEmpty() -> Icons.Default.Close
                            compatible.isNotEmpty() -> Icons.Default.CheckCircle
                            else -> null
                        }

                        val compatibilityIconTint = when {
                            incompatible.isNotEmpty() -> MaterialTheme.colorScheme.error
                            compatible.isNotEmpty() -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = displayName,
                                    color = itemColor
                                )
                            },
                            trailingContent = {
                                if (compatibilityIcon != null) {
                                    Icon(
                                        imageVector = compatibilityIcon,
                                        contentDescription = when {
                                            incompatible.isNotEmpty() -> "Niekompatybilna"
                                            compatible.isNotEmpty() -> "Kompatybilna"
                                            else -> null
                                        },
                                        tint = compatibilityIconTint
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPlantSelected(plant, compatible, incompatible)
                                }
                        )
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

fun addPlantToLocation(
    plant: Plant,
    location: PlantLocationEntity,
    gardenViewModel: GardenViewModel,
    mainViewModel: MainViewModel,
    variety: String = "",
    quantity: Int = 1,
    notes: String = "",
    plantingDate: String = ""
) {
    // Dodaj roślinę do kolekcji użytkownika z odpowiednimi szczegółami
    mainViewModel.addUserPlantToLocation(
        plant,
        location.id,
        variety,
        quantity,
        notes,
        plantingDate
    )

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

// Funkcja pomocnicza do pobrania nazwy lokalizacji
private fun getLocationName(locationId: String, spaces: List<SpaceWithAreas>): String {
    if (locationId.isEmpty()) return "Brak lokalizacji"

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
    onRemove: () -> Unit,
    gardenViewModel: GardenViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

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

                if (plant.variety.isNotBlank()) {
                    Text(
                        text = "Odmiana: ${plant.variety}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "Lokalizacja: $locationName",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (plant.plantingDate.isNotBlank()) {
                        Text(
                            text = "Posadzono: ${plant.plantingDate}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = "Kroków: ${plant.careSteps.size}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (plant.quantity > 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ilość: ${plant.quantity}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    // Jeśli roślina ma przypisaną lokalizację, znajdź i usuń również powiązanie
                    if (plant.locationId.isNotEmpty()) {
                        // Pobierz wszystkie placements dla danej lokalizacji
                        val placements = gardenViewModel.getPlacementsForLocation(plant.locationId).first()

                        // Znajdź placement, który odpowiada tej roślinie
                        val placementToDelete = placements.find { placement ->
                            placement.plantId == plant.plantId ||
                                    placement.plantId.contains(plant.name, ignoreCase = true) ||
                                    plant.name.contains(placement.plantId, ignoreCase = true)
                        }

                        // Jeśli znaleziono powiązanie, usuń je
                        placementToDelete?.let {
                            gardenViewModel.deletePlantPlacement(it)
                            println("Usunięto powiązanie rośliny ${plant.name} z lokalizacją ${plant.locationId}")
                        }
                    }

                    // Na koniec usuń roślinę z listy użytkownika
                    onRemove()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń"
                )
            }
        }
    }
}