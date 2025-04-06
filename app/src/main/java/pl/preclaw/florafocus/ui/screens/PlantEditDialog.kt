package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.repository.UserPlant
import pl.preclaw.florafocus.ui.components.DatePickerDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantEditDialog(
    plant: UserPlant,
    onDismiss: () -> Unit,
    onConfirm: (variety: String, quantity: Int, notes: String, plantingDate: String, waterRequirement: String, lightRequirement: String, soilType: String) -> Unit
) {
    // Podstawowe dane
    var variety by remember { mutableStateOf(plant.variety) }
    var quantity by remember { mutableStateOf(plant.quantity.toString()) }
    var notes by remember { mutableStateOf(plant.notes) }

    // Format daty sadzenia
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    var plantingDate by remember { mutableStateOf(plant.plantingDate) }

    // Stany dla rozszerzonych danych, które możemy edytować
    var name by remember { mutableStateOf(plant.name) }
    var waterReq by remember { mutableStateOf(plant.waterRequirement) }
    var lightReq by remember { mutableStateOf(plant.lightRequirement) }
    var soilType by remember { mutableStateOf(plant.soilType) }

    // Wybór wymagań wodnych
    var waterRequirementOptions = listOf("Low", "Medium", "High", "Very High")
    var expandedWaterMenu by remember { mutableStateOf(false) }

    // Wybór wymagań świetlnych
    var lightRequirementOptions = listOf("Full sun", "Partial shade", "Full shade", "Variable")
    var expandedLightMenu by remember { mutableStateOf(false) }

    // Wybór typu gleby
    var soilTypeOptions = listOf(
        "Sandy", "Clay", "Loam", "Silty", "Peaty", "Rocky", "Mixed",
        "Well-draining", "Rich", "Acidic", "Alkaline", "Neutral"
    )
    var expandedSoilMenu by remember { mutableStateOf(false) }

    // Konwersja String na LocalDate
    fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, formatter)
        } catch (e: Exception) {
            null
        }
    }

    // Ustaw domyślną datę na dzisiaj, jeśli nie ma zapisanej daty
    val initialDate = if (plantingDate.isNotBlank()) {
        parseDate(plantingDate) ?: LocalDate.now()
    } else {
        LocalDate.now()
    }

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
            initialDate = initialDate
        )
    }

    // Zakładki dla różnych kategorii edycji
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Podstawowe", "Wymagania", "Szczegóły")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj roślinę") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Nazwa rośliny
                Text(
                    text = "Roślina: ${plant.name}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Zakładki
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Zawartość zależna od wybranej zakładki
                when (selectedTabIndex) {
                    0 -> {
                        // Zakładka podstawowa

                        // Odmiana
                        OutlinedTextField(
                            value = variety,
                            onValueChange = { variety = it },
                            label = { Text("Odmiana") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        // Ilość
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

                        // Notatki
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notatki") },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                    1 -> {
                        // Zakładka wymagań uprawowych

                        // Wymagania wodne
                        ExposedDropdownMenuBox(
                            expanded = expandedWaterMenu,
                            onExpandedChange = { expandedWaterMenu = !expandedWaterMenu },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = waterReq,
                                onValueChange = { },
                                label = { Text("Wymagania wodne") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWaterMenu) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedWaterMenu,
                                onDismissRequest = { expandedWaterMenu = false }
                            ) {
                                waterRequirementOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            waterReq = option
                                            expandedWaterMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Wymagania świetlne
                        ExposedDropdownMenuBox(
                            expanded = expandedLightMenu,
                            onExpandedChange = { expandedLightMenu = !expandedLightMenu },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = lightReq,
                                onValueChange = { },
                                label = { Text("Wymagania świetlne") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLightMenu) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedLightMenu,
                                onDismissRequest = { expandedLightMenu = false }
                            ) {
                                lightRequirementOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            lightReq = option
                                            expandedLightMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Typ gleby
                        ExposedDropdownMenuBox(
                            expanded = expandedSoilMenu,
                            onExpandedChange = { expandedSoilMenu = !expandedSoilMenu },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = soilType,
                                onValueChange = { },
                                label = { Text("Typ gleby") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSoilMenu) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedSoilMenu,
                                onDismissRequest = { expandedSoilMenu = false }
                            ) {
                                soilTypeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            soilType = option
                                            expandedSoilMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // Zakładka dodatkowych szczegółów

                        // Tutaj można dodać więcej pól, jeśli będą potrzebne
                        Text(
                            text = "Dodatkowe informacje o roślinie:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (plant.careSteps.isNotEmpty()) {
                            Text(
                                text = "Kroki pielęgnacyjne: ${plant.careSteps.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        if (plant.companions.isNotEmpty()) {
                            Text(
                                text = "Dobre sąsiedztwo: ${plant.companions.size} roślin",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        if (plant.incompatibles.isNotEmpty()) {
                            Text(
                                text = "Złe sąsiedztwo: ${plant.incompatibles.size} roślin",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // Informacja o edycji pełnego profilu
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Uwaga:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Edycja pełnego profilu rośliny (poza podstawowymi informacjami) będzie dostępna w przyszłej wersji aplikacji.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantityInt = quantity.toIntOrNull() ?: 1
                    onConfirm(variety, quantityInt, notes, plantingDate, waterReq, lightReq, soilType)
                }
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}