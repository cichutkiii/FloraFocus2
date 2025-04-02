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


@Composable
fun PlantPlacementItem(
    placement: PlantPlacementEntity,
    allPlants: List<Plant>,
    onDelete: () -> Unit
) {
    // Znajdź roślinę na podstawie ID
    val plant = allPlants.find { it.id == placement.plantId }

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
                    text = plant?.commonName ?: "Nieznana roślina",
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

@Composable
fun AddPlantToLocationDialog(
    plants: List<Plant>,
    locationId: String, // Dodany parametr locationId
    onDismiss: () -> Unit,
    onPlantSelected: (Plant, String, Int, String, String) -> Unit,
    mainViewModel: MainViewModel // Dodany parameter mainViewModel
) {
    var selectedPlant by remember { mutableStateOf<Plant?>(null) }
    var variety by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var plantingDate by remember { mutableStateOf("") }

    var showPlantSelector by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (showPlantSelector) "Wybierz roślinę" else "Dodaj szczegóły") },
        text = {
            // Reszta kodu dialogu pozostaje bez zmian
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
                                // Dodanie rośliny z powiązaniem do lokalizacji
                                mainViewModel.addUserPlantToLocation(plant, locationId)
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
            // Reszta kodu pozostaje bez zmian
        }
    )
}