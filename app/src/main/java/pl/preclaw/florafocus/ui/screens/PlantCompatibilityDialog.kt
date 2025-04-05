package pl.preclaw.florafocus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.preclaw.florafocus.data.model.Plant
import pl.preclaw.florafocus.data.repository.UserPlant

/**
 * Dialog wyświetlający informacje o kompatybilności roślin
 */
@Composable
fun PlantCompatibilityDialog(
    plant: Plant,
    compatiblePlants: List<UserPlant>,
    incompatiblePlants: List<UserPlant>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Kompatybilność rośliny",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Informacje o wybranej roślinie
                val displayName = if (plant.commonName.isNotEmpty()) {
                    plant.commonName
                } else {
                    plant.id ?: "Nieznana roślina"
                }

                Text(
                    text = "Informacje o kompatybilności dla rośliny \"$displayName\"",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Ostrzeżenie, jeśli są niezgodne rośliny
                if (incompatiblePlants.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Ostrzeżenie",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Uwaga! Wybrałeś roślinę, która nie jest kompatybilna z ${incompatiblePlants.size} roślinami już zasadzonymi w tej lokalizacji.",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Lista niezgodnych roślin
                if (incompatiblePlants.isNotEmpty()) {
                    Text(
                        text = "Rośliny niekompatybilne:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 120.dp)
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        items(incompatiblePlants) { incompatiblePlant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Niekompatybilna",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = incompatiblePlant.name,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Lista zgodnych roślin
                if (compatiblePlants.isNotEmpty()) {
                    Text(
                        text = "Rośliny kompatybilne:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 120.dp)
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        items(compatiblePlants) { compatiblePlant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Kompatybilna",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = compatiblePlant.name,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Komunikat, jeśli brak roślin do porównania
                if (compatiblePlants.isEmpty() && incompatiblePlants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Brak innych roślin w tej lokalizacji do porównania kompatybilności.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Kontynuuj")
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