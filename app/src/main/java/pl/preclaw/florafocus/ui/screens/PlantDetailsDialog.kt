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
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.ui.components.DatePickerDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PlantDetailsDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onConfirm: (variety: String, quantity: Int, notes: String, plantingDate: String) -> Unit
) {
    var variety by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    // Format dzisiejszej daty jako domyślną
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    var plantingDate by remember { mutableStateOf(today.format(formatter)) }

    // Konwersja String na LocalDate
    fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, formatter)
        } catch (e: Exception) {
            null
        }
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
            initialDate = today
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj szczegóły rośliny") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Informacja o wybranej roślinie
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

                // Odmiana
                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text("Odmiana (opcjonalnie)") },
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
                    val quantityInt = quantity.toIntOrNull() ?: 1
                    onConfirm(variety, quantityInt, notes, plantingDate)
                }
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

// Używamy teraz komponentu DatePickerDialog z pakietu pl.preclaw.florafocus.ui.components