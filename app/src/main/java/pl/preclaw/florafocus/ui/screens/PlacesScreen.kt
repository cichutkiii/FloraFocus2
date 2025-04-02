package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.model.LocationType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import pl.preclaw.florafocus.data.model.LightCondition
import pl.preclaw.florafocus.data.model.SoilType
import pl.preclaw.florafocus.data.repository.GardenAreaEntity
import pl.preclaw.florafocus.data.repository.GardenSpaceEntity
import pl.preclaw.florafocus.data.repository.PlantLocationEntity
import pl.preclaw.florafocus.ui.viewmodel.GardenViewModel
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton

enum class NavigationLevel {
    SPACES, AREAS, LOCATIONS, LOCATION_DETAILS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
    gardenViewModel: GardenViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    val spacesWithAreas by gardenViewModel.allSpaces.collectAsState(initial = emptyList())

    // Stan dla wybranego obszaru i lokalizacji
    var currentLevel by remember { mutableStateOf(NavigationLevel.SPACES) }
    var selectedSpaceIndex by remember { mutableStateOf(0) }
    var currentArea by remember { mutableStateOf<GardenAreaEntity?>(null) }
    var currentLocation by remember { mutableStateOf<PlantLocationEntity?>(null) }

    // Pobierz aktualną przestrzeń
    val currentSpace = if (spacesWithAreas.isNotEmpty()) {
        spacesWithAreas.getOrNull(selectedSpaceIndex)
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (currentLevel) {
                        NavigationLevel.SPACES -> Text("Miejsca")
                        NavigationLevel.AREAS -> Text(currentSpace?.space?.name ?: "")
                        NavigationLevel.LOCATIONS -> Text(currentArea?.name ?: "")
                        NavigationLevel.LOCATION_DETAILS -> Text(currentLocation?.name ?: "")
                    }
                },
                navigationIcon = {
                    if (currentLevel != NavigationLevel.SPACES) {
                        IconButton(onClick = {
                            when (currentLevel) {
                                NavigationLevel.AREAS -> {
                                    currentLevel = NavigationLevel.SPACES
                                }
                                NavigationLevel.LOCATIONS -> {
                                    currentArea = null
                                    currentLevel = NavigationLevel.AREAS
                                }
                                NavigationLevel.LOCATION_DETAILS -> {
                                    currentLocation = null
                                    currentLevel = NavigationLevel.LOCATIONS
                                }
                                else -> {}
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Powrót")
                        }
                    }
                }


            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentLevel) {
                NavigationLevel.SPACES -> {
                    // Lista przestrzeni
                    if (currentSpace == null) {
                        EmptySpacesMessage(onAddClick = { /* Pokaż dialog dodawania przestrzeni */ })
                    } else {
                        val spaces = spacesWithAreas.map { it.space }
                        SpacesList(
                            spaces = spaces,
                            onSpaceClick = { space ->
                                // Znajdź indeks wybranej przestrzeni
                                val index = spaces.indexOf(space)
                                if (index >= 0) {
                                    selectedSpaceIndex = index
                                    currentLevel = NavigationLevel.AREAS
                                }
                            },
                            onDeleteClick = { /* Obsługa usuwania */ }
                        )
                    }
                }

                NavigationLevel.AREAS -> {
                    // Lista obszarów w wybranej przestrzeni
                    if (currentSpace != null) {
                        val areas by gardenViewModel.getAreasForSpace(currentSpace.space.id)
                            .collectAsState(initial = emptyList())

                        if (areas.isEmpty()) {
                            EmptyListMessage("obszarów w ${currentSpace.space.name}")
                        } else {
                            AreasList(
                                areas = areas,
                                onAreaClick = { area ->
                                    currentArea = area
                                    currentLevel = NavigationLevel.LOCATIONS
                                },
                                onDeleteClick = { /* Obsługa usuwania */ }
                            )
                        }
                    }
                }

                NavigationLevel.LOCATIONS -> {
                    // Lista lokalizacji w wybranym obszarze
                    if (currentArea != null) {
                        val locations by gardenViewModel.getLocationsForArea(currentArea!!.id)
                            .collectAsState(initial = emptyList())

                        if (locations.isEmpty()) {
                            EmptyListMessage("lokalizacji w ${currentArea!!.name}")
                        } else {
                            LocationsList(
                                locations = locations,
                                onLocationClick = { location ->
                                    currentLocation = location
                                    currentLevel = NavigationLevel.LOCATION_DETAILS
                                },
                                onDeleteClick = { /* Obsługa usuwania */ }
                            )
                        }
                    }
                }

                NavigationLevel.LOCATION_DETAILS -> {
                    // Szczegóły lokalizacji
                    if (currentLocation != null) {
                        LocationDetailsContent(
                            location = currentLocation!!,
                            gardenViewModel = gardenViewModel,
                            mainViewModel = mainViewModel
                        )
                    }
                }
            }
        }
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
    spaces: List<GardenSpaceEntity>,
    onSpaceClick: (GardenSpaceEntity) -> Unit,
    onDeleteClick: (GardenSpaceEntity) -> Unit
) {
    ListItems(
        items = spaces,
        icon = Icons.Default.Yard,
        onItemClick = onSpaceClick,
        onDeleteClick = onDeleteClick,
        itemContent = { space ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, description: String, type: LocationType,
            light: String, soil: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(LocationType.BED) }
    var selectedLightCondition by remember { mutableStateOf<LightCondition?>(null) }
    var selectedSoilType by remember { mutableStateOf<SoilType?>(null) }
    var notes by remember { mutableStateOf("") }

    val locationTypes = LocationType.values().toList()
    val lightConditions = LightCondition.values().toList()
    val soilTypes = SoilType.values().toList()

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
                                        LocationType.SHRUB_SPOT -> "Krzew"
                                        LocationType.GENERAL_AREA -> "Ogólna"
                                        LocationType.OTHER -> "Inne"
                                    }
                                )
                            }
                        )
                    }
                }

                // Dropdown dla warunków świetlnych
                Text(
                    text = "Warunki świetlne",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                var lightExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = lightExpanded,
                    onExpandedChange = { lightExpanded = !lightExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedLightCondition?.displayName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Wybierz warunki świetlne") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = lightExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = lightExpanded,
                        onDismissRequest = { lightExpanded = false }
                    ) {
                        lightConditions.forEach { condition ->
                            DropdownMenuItem(
                                text = { Text(condition.displayName) },
                                onClick = {
                                    selectedLightCondition = condition
                                    lightExpanded = false
                                }
                            )
                        }
                    }
                }

                // Dropdown dla typu gleby
                Text(
                    text = "Typ gleby",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                var soilExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = soilExpanded,
                    onExpandedChange = { soilExpanded = !soilExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSoilType?.displayName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Wybierz typ gleby") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = soilExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = soilExpanded,
                        onDismissRequest = { soilExpanded = false }
                    ) {
                        soilTypes.forEach { soil ->
                            DropdownMenuItem(
                                text = { Text(soil.displayName) },
                                onClick = {
                                    selectedSoilType = soil
                                    soilExpanded = false
                                }
                            )
                        }
                    }
                }

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
                        onAdd(
                            name,
                            description,
                            selectedType,
                            selectedLightCondition?.displayName ?: "",
                            selectedSoilType?.displayName ?: "",
                            notes
                        )
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
fun ChangeSpaceDialog(
    spaces: List<GardenSpaceEntity>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    var tempSelectedIndex by remember { mutableStateOf(selectedIndex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wybierz przestrzeń główną") },
        text = {
            LazyColumn {
                itemsIndexed(spaces) { index, space ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempSelectedIndex = index }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempSelectedIndex == index,
                            onClick = { tempSelectedIndex = index }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = space.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (space.description.isNotBlank()) {
                                Text(
                                    text = space.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelect(tempSelectedIndex) }
            ) {
                Text("Wybierz")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Anuluj")
            }
        }
    )
}
@Composable
fun EmptySpacesMessage(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Yard,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Text(
            text = "Nie masz jeszcze żadnych przestrzeni",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Dodaj swoją pierwszą przestrzeń, np. \"Ogródek\" lub \"Działka ROD\"",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = onAddClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Dodaj przestrzeń")
        }
    }
}