package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.preclaw.florafocus.data.repository.PlantLocationEntity
import pl.preclaw.florafocus.data.repository.PlantPlacementEntity
import pl.preclaw.florafocus.data.repository.SpaceWithAreas
import pl.preclaw.florafocus.ui.viewmodel.GardenViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    // Zmienne do obsługi szczegółów rośliny
    var selectedUserPlantId by remember { mutableStateOf<Int?>(null) }
    var showPlantDetails by remember { mutableStateOf(false) }

    // Funkcja nawigacji wstecz ze szczegółów rośliny
    val navigateBackFromPlantDetails = {
        showPlantDetails = false
        selectedUserPlantId = null
    }

    if (showPlantDetails && selectedUserPlantId != null) {
        // Pokaż ekran szczegółów rośliny
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val userPlants by viewModel.userPlants.collectAsState(initial = emptyList())
                        val userPlant = userPlants.find { it.id == selectedUserPlantId }
                        Text(userPlant?.name ?: "Szczegóły rośliny")
                    },
                    navigationIcon = {
                        IconButton(onClick = navigateBackFromPlantDetails) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Powrót"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                PlantDetailsScreen(
                    userPlantId = selectedUserPlantId!!,
                    onNavigateBack = navigateBackFromPlantDetails
                )
            }
        }
    } else {
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
                                onRemove = { viewModel.removeUserPlant(plant) },
                                onClick = {
                                    selectedUserPlantId = plant.id
                                    showPlantDetails = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Dialog wyboru rośliny
        if (showPlantSelectionDialog) {
            PlantSelectionDialog(
                plants = allPlants,
                onDismiss = { showPlantSelectionDialog = false },
                onPlantSelected = { plant ->
                    selectedPlant = plant
                    showPlantSelectionDialog = false
                    showLocationSelectionDialog = true  // Po wybraniu rośliny, pokaż dialog lokalizacji
                }
            )
        }

        // Dialog wyboru lokalizacji
        if (showLocationSelectionDialog && selectedPlant != null) {
            LocationSelectionDialog(
                spaces = allSpaces,
                onLocationSelected = { location ->
                    // Dodaj roślinę do lokalizacji
                    addPlantToLocation(selectedPlant!!, location, gardenViewModel, viewModel)
                    showLocationSelectionDialog = false
                    selectedPlant = null
                },
                onDismiss = {
                    showLocationSelectionDialog = false
                    selectedPlant = null
                }
            )
        }
    }
}


fun addPlantToLocation(
    plant: Plant,
    location: PlantLocationEntity,
    gardenViewModel: GardenViewModel,
    mainViewModel: MainViewModel
) {
    // WAŻNE: Wywołaj tylko raz dodawanie rośliny
    // Dodaj roślinę do kolekcji użytkownika
    mainViewModel.addUserPlantToLocation(plant, location.id)

    // Dodaj powiązanie rośliny z lokalizacją
    gardenViewModel.addPlantPlacement(
        PlantPlacementEntity(
            plantId = plant.id ?: "",
            locationId = location.id,
            quantity = 1
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


@Composable
fun PlantSelectionDialog(
    plants: List<Plant>,
    onDismiss: () -> Unit,
    onPlantSelected: (Plant) -> Unit
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlantSelected(plant) }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = displayName,
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
    onClick: () -> Unit = {},
    gardenViewModel: GardenViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
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
            IconButton(onClick = {
                coroutineScope.launch {
                    // Jeśli roślina ma przypisaną lokalizację, znajdź i usuń również powiązanie
                    if (plant.locationId.isNotEmpty()) {
                        // Pobierz wszystkie placements dla danej lokalizacji
                        val placements = gardenViewModel.getPlacementsForLocation(plant.locationId).first()

                        // Znajdź placement, który odpowiada tej roślinie
                        val placementToDelete = placements.find { placement ->
                            placement.plantId == plant.name ||
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