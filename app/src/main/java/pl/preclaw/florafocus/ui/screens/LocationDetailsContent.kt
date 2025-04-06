package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.repository.PlantLocationEntity
import pl.preclaw.florafocus.data.repository.PlantPlacementEntity
import pl.preclaw.florafocus.data.repository.UserPlant
import pl.preclaw.florafocus.ui.components.DatePickerDialog
import pl.preclaw.florafocus.ui.viewmodel.GardenViewModel
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel
import pl.preclaw.florafocus.utils.PlantCompatibilityChecker
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LocationDetailsContent(
    location: PlantLocationEntity,
    gardenViewModel: GardenViewModel,
    mainViewModel: MainViewModel,
    onNavigateToPlantDetails: (Int) -> Unit  // Nowy parametr

) {
    val plants by mainViewModel.allPlants.collectAsState()
    val userPlants by mainViewModel.userPlants.collectAsState(initial = emptyList())
    val placements by gardenViewModel.getPlacementsForLocation(location.id)
        .collectAsState(initial = emptyList())

    var showAddPlantDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()



        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Karta informacyjna lokalizacji
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    if (location.description.isNotBlank()) {
                        Text(
                            text = location.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Informacje o warunkach
                    Row(
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        if (location.lightConditions.isNotBlank()) {
                            LocationChip(
                                text = location.lightConditions,
                                icon = Icons.Default.WbSunny
                            )
                        }
                        if (location.soilType.isNotBlank()) {
                            LocationChip(
                                text = location.soilType,
                                icon = Icons.Default.Grass
                            )
                        }
                    }

                    if (location.notes.isNotBlank()) {
                        Text(
                            text = "Notatki: ${location.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Lista roślin
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
                            },
                            onClick = { plantId ->
                                // Po kliknięciu, ustaw ID rośliny i pokaż szczegóły
                                onNavigateToPlantDetails(plantId)

                            }
                        )
                    }
                }
            }
        }

        // Dialog dodawania rośliny
        if (showAddPlantDialog) {
            // Filtruj rośliny do tych w tym samym miejscu
            val plantsInThisLocation = userPlants.filter { it.locationId == location.id }

            AddPlantToLocationWithCompatibilityDialog(
                plants = plants,
                locationId = location.id,
                existingPlants = plantsInThisLocation,
                onDismiss = { showAddPlantDialog = false },
                onPlantSelected = { plant, variety, quantity, notes, plantingDate ->
                    // Dodaj roślinę do kolekcji użytkownika z powiązaniem do lokalizacji
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantToLocationWithCompatibilityDialog(
    plants: List<Plant>,
    locationId: String,
    existingPlants: List<UserPlant>,
    onDismiss: () -> Unit,
    onPlantSelected: (Plant, String, Int, String, String) -> Unit,
    mainViewModel: MainViewModel
) {
    var selectedPlant by remember { mutableStateOf<Plant?>(null) }
    var showPlantSelector by remember { mutableStateOf(true) }
    var showCompatibilityDialog by remember { mutableStateOf(false) }

    // Stany dla szczegółów rośliny
    var variety by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    // Informacje o kompatybilności
    var compatiblePlants by remember { mutableStateOf<List<UserPlant>>(emptyList()) }
    var incompatiblePlants by remember { mutableStateOf<List<UserPlant>>(emptyList()) }

    // Format dzisiejszej daty jako domyślną
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    var plantingDate by remember { mutableStateOf(today.format(formatter)) }

    // Stan dla dialogu wyboru daty
    var showDatePicker by remember { mutableStateOf(false) }

    // Wyświetl dialog wyboru daty, jeśli showDatePicker jest true
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                plantingDate = selectedDate.format(formatter)
                showDatePicker = false
            },
            initialDate = today
        )
    }

    // Dialog kompatybilności
    if (showCompatibilityDialog && selectedPlant != null) {
        PlantCompatibilityDialog(
            plant = selectedPlant!!,
            compatiblePlants = compatiblePlants,
            incompatiblePlants = incompatiblePlants,
            onDismiss = { showCompatibilityDialog = false },
            onConfirm = {
                showCompatibilityDialog = false
                showPlantSelector = false
            }
        )
    }

    // Informacje debugowe
    LaunchedEffect(Unit) {
        println("AddPlantToLocationDialog: Dostępnych roślin: ${plants.size}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (showPlantSelector) "Wybierz roślinę" else "Dodaj szczegóły") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (showPlantSelector) {
                    // Lista roślin do wyboru
                    var searchQuery by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Szukaj") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    // Filtruj rośliny
                    val filteredPlants = plants.filter {
                        it.id?.contains(searchQuery, ignoreCase = true) == true ||
                                it.commonName.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredPlants.isEmpty()) {
                        Text("Brak pasujących roślin")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            items(filteredPlants) { plant ->
                                // Wybierz nazwę do wyświetlenia
                                val displayName = if (plant.commonName.isNotEmpty()) {
                                    plant.commonName
                                } else {
                                    plant.id ?: "Nieznana roślina"
                                }

                                // Sprawdź kompatybilność dla tej rośliny
                                val (compatible, incompatible) = PlantCompatibilityChecker.checkCompatibility(
                                    plant, existingPlants
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
                                    // Dodaj ikony kompatybilności
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
                                            selectedPlant = plant
                                            // Zapisz wyniki kompatybilności
                                            compatiblePlants = compatible
                                            incompatiblePlants = incompatible

                                            // Jeśli są niekompatybilne rośliny, pokaż ostrzeżenie
                                            if (incompatible.isNotEmpty()) {
                                                showCompatibilityDialog = true
                                            } else {
                                                // Jeśli nie ma niekompatybilnych roślin, przejdź dalej
                                                showPlantSelector = false
                                            }

                                            println("Wybrano roślinę: id=${plant.id}, nazwa=$displayName")
                                        }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                } else {
                    // Formularz szczegółów
                    selectedPlant?.let { plant ->
                        // Wybierz nazwę do wyświetlenia
                        val displayName = if (plant.commonName.isNotEmpty()) {
                            plant.commonName
                        } else {
                            plant.id ?: "Nieznana roślina"
                        }

                        Text(
                            text = "Wybrana roślina: $displayName",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Wskaźniki kompatybilności
                        if (compatiblePlants.isNotEmpty() || incompatiblePlants.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "Kompatybilność: ",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                if (compatiblePlants.isNotEmpty()) {
                                    Text(
                                        text = "${compatiblePlants.size} przyjaznych",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.clickable {
                                            showCompatibilityDialog = true
                                        }
                                    )
                                }

                                if (compatiblePlants.isNotEmpty() && incompatiblePlants.isNotEmpty()) {
                                    Text(
                                        text = " | ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (incompatiblePlants.isNotEmpty()) {
                                    Text(
                                        text = "${incompatiblePlants.size} nieprzyjaznych",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.clickable {
                                            showCompatibilityDialog = true
                                        }
                                    )
                                }
                            }

                            Text(
                                text = "(Kliknij, aby zobaczyć szczegóły)",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = variety,
                        onValueChange = { variety = it },
                        label = { Text("Odmiana (opcjonalnie)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                quantity = it
                            }
                        },
                        label = { Text("Ilość") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    // Data posadzenia z ikoną kalendarza
                    OutlinedTextField(
                        value = plantingDate,
                        onValueChange = { /* Readonly - changes through date picker */ },
                        label = { Text("Data posadzenia") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Wybierz datę"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notatki (opcjonalnie)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (showPlantSelector) {
                Button(
                    onClick = { onDismiss() },
                    enabled = true
                ) {
                    Text("Anuluj")
                }
            } else {
                Row {
                    TextButton(
                        onClick = { showPlantSelector = true }
                    ) {
                        Text("Wstecz")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            selectedPlant?.let { plant ->
                                val quantityInt = quantity.toIntOrNull() ?: 1

                                // Przekaż dane do wywołującego
                                onPlantSelected(
                                    plant,
                                    variety,
                                    quantityInt,
                                    notes,
                                    plantingDate
                                )
                            }
                        },
                        enabled = selectedPlant != null
                    ) {
                        Text("Dodaj")
                    }
                }
            }
        },
        dismissButton = {
            if (showPlantSelector) {
                // Nic tutaj, bo mamy tylko jeden przycisk w tym widoku
            } else {
                TextButton(
                    onClick = { onDismiss() }
                ) {
                    Text("Anuluj")
                }
            }
        }
    )
}

@Composable
fun PlantPlacementItem(
    placement: PlantPlacementEntity,
    allPlants: List<Plant>,
    userPlants: List<UserPlant>,
    mainViewModel: MainViewModel,
    onDelete: () -> Unit,
    onClick: (Int) -> Unit  // Nowy parametr - funkcja do nawigacji do szczegółów
) {
    // Znajdź roślinę na podstawie ID (które jest nazwą rośliny w Firebase)
    val plant = allPlants.find { it.id == placement.plantId }

    // Znajdź odpowiadającą roślinę użytkownika, by przekazać jej ID
    val userPlant = userPlants.find {
        it.locationId == placement.locationId &&
                (it.plantId == placement.plantId || it.name == plant?.commonName)
    }

    // Wyznacz nazwę do wyświetlenia - jeśli nie znaleziono rośliny, użyj plantId jako nazwy
    val displayName = plant?.commonName?.takeIf { it.isNotEmpty() }
        ?: placement.plantId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                // Po kliknięciu, jeśli mamy ID rośliny użytkownika, przejdź do szczegółów
                userPlant?.id?.let { onClick(it) }
            }
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

                Row(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Ilość: ${placement.quantity}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (placement.plantingDate.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Posadzono: ${placement.plantingDate}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (placement.notes.isNotBlank()) {
                    Text(
                        text = "Notatki: ${placement.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Pokaż kompatybilność z innymi roślinami w tej lokalizacji
                plant?.let { currentPlant ->
                    val plantsInThisLocation = userPlants.filter { it.locationId == placement.locationId && it.plantId != currentPlant.id }
                    if (plantsInThisLocation.isNotEmpty()) {
                        val (compatiblePlants, incompatiblePlants) = PlantCompatibilityChecker.checkCompatibility(
                            currentPlant, plantsInThisLocation
                        )

                        if (compatiblePlants.isNotEmpty() || incompatiblePlants.isNotEmpty()) {
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (compatiblePlants.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Przyjazna dla",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = " ${compatiblePlants.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (compatiblePlants.isNotEmpty() && incompatiblePlants.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                if (incompatiblePlants.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Nieprzyjazna dla",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = " ${incompatiblePlants.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
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