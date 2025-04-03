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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.LaunchedEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantToLocationDialog(
    plants: List<Plant>,
    locationId: String,
    onDismiss: () -> Unit,
    onPlantSelected: (Plant, String, Int, String, String) -> Unit,
    mainViewModel: MainViewModel
) {
    var selectedPlant by remember { mutableStateOf<Plant?>(null) }
    var variety by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var plantingDate by remember { mutableStateOf("") }
    var showPlantSelector by remember { mutableStateOf(true) }

    // Informacje debugowe
    LaunchedEffect(Unit) {
        println("AddPlantToLocationDialog: Dostępnych roślin: ${plants.size}")
        plants.forEach { plant ->
            println("  - Roślina: id=${plant.id}, commonName=${plant.commonName}")
        }
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

                    // Filtruj rośliny na podstawie id (które jest nazwą rośliny w Firebase) lub commonName
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
                                // Wybierz nazwę do wyświetlenia: commonName jeśli istnieje, w przeciwnym razie id
                                val displayName = if (plant.commonName.isNotEmpty()) {
                                    plant.commonName
                                } else {
                                    plant.id ?: "Nieznana roślina"
                                }

                                ListItem(
                                    headlineContent = { Text(displayName) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedPlant = plant
                                            showPlantSelector = false
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

                    OutlinedTextField(
                        value = plantingDate,
                        onValueChange = { plantingDate = it },
                        label = { Text("Data posadzenia (DD-MM-RRRR)") },
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

                                // Dodaj roślinę do kolekcji użytkownika z powiązaniem do lokalizacji
                                mainViewModel.addUserPlantToLocation(plant, locationId)

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