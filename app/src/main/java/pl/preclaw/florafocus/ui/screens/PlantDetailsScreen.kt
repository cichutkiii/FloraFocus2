package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.repository.UserPlant
import pl.preclaw.florafocus.ui.viewmodel.MainViewModel

@Composable
fun PlantDetailsScreen(
    userPlantId: Int,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    // Pobieramy dane rośliny z podanym ID
    val userPlants by viewModel.userPlants.collectAsState(initial = emptyList())
    val userPlant = userPlants.find { it.id == userPlantId }

    // Stan dla dialogu edycji
    var showEditDialog by remember { mutableStateOf(false) }

    // Pobieramy pełne informacje o roślinie z Firebase (jeśli są dostępne)
    val allPlants by viewModel.allPlants.collectAsState()
    val firebasePlant = userPlant?.let {
        allPlants.find { plant -> plant.id == userPlant.plantId }
    }

    // Po wejściu na ekran ustaw akcje w topBar
    LaunchedEffect(userPlantId) {
        userPlant?.let { plant ->
            viewModel.setPlantDetailsNavigation(
                plantName = plant.name,
                onBackPress = onNavigateBack,
                onEditClick = { showEditDialog = true }
            )
        }
    }

    // Obsługa dialogu edycji
    if (showEditDialog && userPlant != null) {
        PlantEditDialog(
            plant = userPlant,
            onDismiss = { showEditDialog = false },
            onConfirm = { variety, quantity, notes, plantingDate, waterReq, lightReq, soilType ->
                viewModel.updateUserPlant(
                    userPlant,
                    variety,
                    quantity,
                    notes,
                    plantingDate,
                    waterReq,
                    lightReq,
                    soilType
                )
                showEditDialog = false
            }
        )
    }

    if (userPlant == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Nie znaleziono rośliny")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Podstawowe informacje
            PlantInfoSection(
                title = "Informacje podstawowe",
                content = {
                    InfoItem(
                        icon = Icons.Default.Eco,
                        label = "Nazwa",
                        value = userPlant.name
                    )
                    if (userPlant.family.isNotBlank()) {
                        InfoItem(
                            icon = Icons.Default.Category,
                            label = "Rodzina",
                            value = userPlant.family
                        )
                    }
                    if (userPlant.growth.isNotBlank()) {
                        InfoItem(
                            icon = Icons.Default.Timeline,
                            label = "Cykl życia",
                            value = userPlant.growth
                        )
                    }
                    InfoItem(
                        icon = Icons.Default.Restaurant,
                        label = "Jadalna",
                        value = if (userPlant.edible) "Tak" else "Nie"
                    )
                    // Dodaj informację o odmianie, jeśli jest dostępna
                    if (userPlant.variety.isNotBlank()) {
                        InfoItem(
                            icon = Icons.Default.LocalFlorist,
                            label = "Odmiana",
                            value = userPlant.variety
                        )
                    }
                    // Dodaj informację o ilości
                    InfoItem(
                        icon = Icons.Default.Numbers,
                        label = "Ilość",
                        value = userPlant.quantity.toString()
                    )
                    // Dodaj datę sadzenia, jeśli jest dostępna
                    if (userPlant.plantingDate.isNotBlank()) {
                        InfoItem(
                            icon = Icons.Default.CalendarMonth,
                            label = "Data sadzenia",
                            value = userPlant.plantingDate
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wymagania uprawowe
            PlantInfoSection(
                title = "Wymagania uprawowe",
                content = {
                    if (userPlant.waterRequirement.isNotBlank()) {
                        InfoItem(
                            icon = Icons.Default.WaterDrop,
                            label = "Zapotrzebowanie na wodę",
                            value = userPlant.waterRequirement
                        )
                    }
                    if (userPlant.lightRequirement.isNotBlank()) {
                        InfoItem(
                            icon = Icons.Default.WbSunny,
                            label = "Zapotrzebowanie na światło",
                            value = userPlant.lightRequirement
                        )
                    }
                    if (userPlant.soilType.isNotBlank()) {
                        InfoItem(
                            icon = Icons.Default.Landscape,
                            label = "Typ gleby",
                            value = userPlant.soilType
                        )
                    }
                    if (userPlant.usdaHardinessZone.isNotBlank()) {
                        InfoItem(
                            icon = Icons.Default.PublishedWithChanges,
                            label = "Strefa USDA",
                            value = userPlant.usdaHardinessZone
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Harmonogram uprawy
            if (userPlant.sowingDate.start.isNotBlank() || userPlant.careSteps.isNotEmpty()) {
                PlantInfoSection(
                    title = "Harmonogram uprawy",
                    content = {
                        if (userPlant.sowingDate.start.isNotBlank()) {
                            InfoItem(
                                icon = Icons.Default.CalendarMonth,
                                label = "Termin siewu",
                                value = "${userPlant.sowingDate.start} - ${userPlant.sowingDate.end}"
                            )
                        }

                        // Kroki pielęgnacyjne
                        if (userPlant.careSteps.isNotEmpty()) {
                            Text(
                                "Kroki pielęgnacyjne",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            userPlant.careSteps.forEach { step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(step.task)
                                        if (step.dateRange.start.isNotBlank()) {
                                            Text(
                                                "Termin: ${step.dateRange.start} - ${step.dateRange.end}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Porady uprawowe
            if (userPlant.companions.isNotEmpty() || userPlant.incompatibles.isNotEmpty() ||
                userPlant.pests.isNotEmpty() || userPlant.diseases.isNotEmpty()) {
                PlantInfoSection(
                    title = "Porady uprawowe",
                    content = {
                        // Rośliny towarzyszące
                        if (userPlant.companions.isNotEmpty()) {
                            ChipSection(
                                title = "Dobre sąsiedztwo",
                                items = userPlant.companions
                            )
                        }

                        // Rośliny niekompatybilne
                        if (userPlant.incompatibles.isNotEmpty()) {
                            ChipSection(
                                title = "Złe sąsiedztwo",
                                items = userPlant.incompatibles
                            )
                        }

                        // Szkodniki
                        if (userPlant.pests.isNotEmpty()) {
                            ChipSection(
                                title = "Typowe szkodniki",
                                items = userPlant.pests
                            )
                        }

                        // Choroby
                        if (userPlant.diseases.isNotEmpty()) {
                            ChipSection(
                                title = "Typowe choroby",
                                items = userPlant.diseases
                            )
                        }
                    }
                )
            }

            // Części jadalne
            if (userPlant.edible && userPlant.edibleParts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                PlantInfoSection(
                    title = "Części jadalne",
                    content = {
                        ChipSection(
                            title = "",
                            items = userPlant.edibleParts
                        )
                    }
                )
            }

            // Notatki użytkownika
            if (userPlant.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                PlantInfoSection(
                    title = "Moje notatki",
                    content = {
                        Text(userPlant.notes)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PlantInfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipSection(
    title: String,
    items: List<String>
) {
    if (title.isNotBlank()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.take(3).forEach { item ->
            AssistChip(
                onClick = { },
                label = { Text(item) }
            )
        }

        if (items.size > 3) {
            AssistChip(
                onClick = { },
                label = { Text("+${items.size - 3}") }
            )
        }
    }

    if (items.size > 3) {
        Column(
            modifier = Modifier.padding(top = 4.dp)
        ) {
            items.drop(3).forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}