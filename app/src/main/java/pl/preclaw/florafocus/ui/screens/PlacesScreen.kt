package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.model.LocationType
import pl.preclaw.florafocus.data.repository.*
import pl.preclaw.florafocus.ui.viewmodel.GardenViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(gardenViewModel: GardenViewModel = viewModel()) {
    val spaces by gardenViewModel.allSpaces.collectAsState(initial = emptyList())
    var selectedSpace by remember { mutableStateOf<GardenSpaceEntity?>(null) }
    var selectedArea by remember { mutableStateOf<GardenAreaEntity?>(null) }
    var showAddSpaceDialog by remember { mutableStateOf(false) }
    var showAddAreaDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }

    // Tytuł zależny od aktualnego widoku
    val screenTitle = when {
        selectedArea != null -> selectedArea!!.name
        selectedSpace != null -> selectedSpace!!.name
        else -> "Miejsca"
    }

    Column {
        // Pasek nawigacji
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Przycisk powrotu
            if (selectedArea != null || selectedSpace != null) {
                IconButton(onClick = {
                    if (selectedArea != null) {
                        selectedArea = null
                    } else {
                        selectedSpace = null
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, "Powrót")
                }
            } else {
                // Placeholder dla zachowania układu
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Tytuł
            Text(
                text = screenTitle,
                style = MaterialTheme.typography.titleLarge
            )

            // Przycisk dodawania
            IconButton(onClick = {
                when {
                    selectedArea != null -> showAddLocationDialog = true
                    selectedSpace != null -> showAddAreaDialog = true
                    else -> showAddSpaceDialog = true
                }
            }) {
                Icon(Icons.Default.Add, "Dodaj")
            }
        }

        // Zawartość zależna od aktualnego widoku
        when {
            selectedArea != null -> {
                // Widok lokalizacji w obszarze
                val locations by gardenViewModel.getLocationsForArea(selectedArea!!.id)
                    .collectAsState(initial = emptyList())

                if (locations.isEmpty()) {
                    EmptyListMessage("lokalizacji")
                } else {
                    LocationsList(
                        locations = locations,
                        onLocationClick = { /* Obsługa kliknięcia lokalizacji */ },
                        onDeleteClick = { gardenViewModel.deleteLocation(it) }
                    )
                }
            }
            selectedSpace != null -> {
                // Widok obszarów w przestrzeni
                val areas by gardenViewModel.getAreasForSpace(selectedSpace!!.id)
                    .collectAsState(initial = emptyList())

                if (areas.isEmpty()) {
                    EmptyListMessage("obszarów")
                } else {
                    AreasList(
                        areas = areas,
                        onAreaClick = { selectedArea = it },
                        onDeleteClick = { gardenViewModel.deleteArea(it) }
                    )
                }
            }
            else -> {
                // Widok wszystkich przestrzeni
                if (spaces.isEmpty()) {
                    EmptyListMessage("przestrzeni")
                } else {
                    SpacesList(
                        spaces = spaces,
                        onSpaceClick = { selectedSpace = it },
                        onDeleteClick = { gardenViewModel.deleteSpace(it) }
                    )
                }
            }
        }
    }

    // Dialogi
    if (showAddSpaceDialog) {
        AddSpaceDialog(
            onDismiss = { showAddSpaceDialog = false },
            onAdd = { name, description ->
                gardenViewModel.addSpace(GardenSpaceEntity(
                    name = name,
                    description = description
                ))
                showAddSpaceDialog = false
            }
        )
    }

    if (showAddAreaDialog && selectedSpace != null) {
        AddAreaDialog(
            onDismiss = { showAddAreaDialog = false },
            onAdd = { name, description ->
                gardenViewModel.addArea(GardenAreaEntity(
                    name = name,
                    description = description,
                    parentId = selectedSpace!!.id
                ))
                showAddAreaDialog = false
            }
        )
    }

    if (showAddLocationDialog && selectedArea != null) {
        AddLocationDialog(
            onDismiss = { showAddLocationDialog = false },
            onAdd = { name, description, type, light, soil, notes ->
                gardenViewModel.addLocation(PlantLocationEntity(
                    name = name,
                    description = description,
                    parentId = selectedArea!!.id,
                    type = type,
                    lightConditions = light,
                    soilType = soil,
                    notes = notes
                ))
                showAddLocationDialog = false
            }
        )
    }
}

@Composable
fun EmptyListMessage(itemType: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Nie masz jeszcze żadnych $itemType")
    }
}

@Composable
fun SpacesList(
    spaces: List<SpaceWithAreas>,
    onSpaceClick: (GardenSpaceEntity) -> Unit,
    onDeleteClick: (GardenSpaceEntity) -> Unit
) {
    ListItems(
        items = spaces,
        icon = Icons.Default.Yard,
        onItemClick = { onSpaceClick(it.space) },
        onDeleteClick = { onDeleteClick(it.space) },
        itemContent = { spaceWithAreas ->
            val space = spaceWithAreas.space
            Column {
                Text(
                    text = space.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (space.description.isNotBlank()) {
                    Text(
                        text = space.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                // Możesz również wyświetlić liczbę obszarów
                Text(
                    text = "Obszarów: ${spaceWithAreas.areas.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}
@Composable
fun AreasList(
    areas: List<GardenAreaEntity>,
    onAreaClick: (GardenAreaEntity) -> Unit,
    onDeleteClick: (GardenAreaEntity) -> Unit
) {
    ListItems(
        items = areas,
        icon = Icons.Default.Landscape,
        onItemClick = onAreaClick,
        onDeleteClick = onDeleteClick,
        itemContent = { area ->
            Column {
                Text(
                    text = area.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (area.description.isNotBlank()) {
                    Text(
                        text = area.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}

@Composable
fun LocationsList(
    locations: List<PlantLocationEntity>,
    onLocationClick: (PlantLocationEntity) -> Unit,
    onDeleteClick: (PlantLocationEntity) -> Unit
) {
    ListItems(
        items = locations,
        icon = Icons.Default.Place,
        onItemClick = onLocationClick,
        onDeleteClick = onDeleteClick,
        itemContent = { location ->
            Column {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (location.description.isNotBlank()) {
                    Text(
                        text = location.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row {
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
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationChip(text: String, icon: ImageVector) {
    AssistChip(
        onClick = { },
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = Modifier.padding(end = 4.dp)
    )
}

@Composable
fun <T> ListItems(
    items: List<T>,
    icon: ImageVector,
    onItemClick: (T) -> Unit,
    onDeleteClick: (T) -> Unit,
    itemContent: @Composable (T) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onItemClick(item) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        itemContent(item)
                    }

                    IconButton(onClick = { onDeleteClick(item) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddSpaceDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj nową przestrzeń") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis (opcjonalnie)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, description)
                    }
                }
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun AddAreaDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj nowy obszar") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis (opcjonalnie)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, description)
                    }
                }
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun AddLocationDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, description: String, type: LocationType,
            light: String, soil: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(LocationType.BED) }
    var lightConditions by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val locationTypes = LocationType.values().toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj nową lokalizację") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis (opcjonalnie)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Text(
                    text = "Typ lokalizacji",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(locationTypes) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    when(type) {
                                        LocationType.BED -> "Grządka"
                                        LocationType.RAISED_BED -> "Podwyższona"
                                        LocationType.POT -> "Donica"
                                        LocationType.TREE_SPOT -> "Drzewo"
                                        LocationType.GENERAL_AREA -> "Ogólna"
                                        LocationType.OTHER -> "Inne"
                                    }
                                )
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = lightConditions,
                    onValueChange = { lightConditions = it },
                    label = { Text("Warunki świetlne (opcjonalnie)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = soilType,
                    onValueChange = { soilType = it },
                    label = { Text("Typ gleby (opcjonalnie)") },
                    modifier = Modifier
                        .fillMaxWidth()
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
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, description, selectedType,
                            lightConditions, soilType, notes)
                    }
                }
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}